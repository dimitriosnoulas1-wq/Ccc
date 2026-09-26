'use strict';

// AI proxy: the Gemini / OpenAI keys live only here (env vars), never in the app.
// Anyone can call a public endpoint, so every request is size-capped, rate-limited
// per IP and counted against a daily cap that bounds the worst-case bill.

const GEMINI_URL = 'https://generativelanguage.googleapis.com/v1beta/models';
const OPENAI_URL = 'https://api.openai.com/v1/chat/completions';

function list(env, fallback) {
  return (env || fallback).split(',').map((s) => s.trim()).filter(Boolean);
}

const config = {
  geminiKey: (process.env.GEMINI_API_KEY || '').trim(),
  openaiKey: (process.env.OPENAI_API_KEY || '').trim(),
  // Cheapest reliable models first.
  geminiModels: list(process.env.GEMINI_MODELS, 'gemini-3.5-flash-lite,gemini-3.1-flash-lite,gemini-3.6-flash'),
  openaiModels: list(process.env.OPENAI_MODELS, 'gpt-5-nano,gpt-4.1-nano,gpt-4o-mini'),
  perIpPer10Min: Number(process.env.AI_PER_IP_10MIN || 20),
  perDay: Number(process.env.AI_DAILY_LIMIT || 3000),
  maxBodyBytes: 64_000,
  maxPromptChars: 2_000,
  maxSystemChars: 40_000,
  timeoutMs: 20_000
};

const WINDOW_MS = 10 * 60_000;
const ipHits = new Map();
const daily = { day: '', count: 0 };

function reset() {
  ipHits.clear();
  daily.day = '';
  daily.count = 0;
}

/** Returns null when allowed, or the reason it is refused. Counts the request when allowed. */
function admit(ip, now = Date.now()) {
  const day = new Date(now).toISOString().slice(0, 10);
  if (daily.day !== day) {
    daily.day = day;
    daily.count = 0;
  }
  if (daily.count >= config.perDay) return 'daily_limit';
  const recent = (ipHits.get(ip) || []).filter((t) => now - t < WINDOW_MS);
  if (recent.length >= config.perIpPer10Min) {
    ipHits.set(ip, recent);
    return 'rate_limited';
  }
  recent.push(now);
  ipHits.set(ip, recent);
  daily.count += 1;
  if (ipHits.size > 50_000) {
    for (const [key, hits] of ipHits) {
      if (!hits.some((t) => now - t < WINDOW_MS)) ipHits.delete(key);
    }
  }
  return null;
}

/** Validates the app's request body: { system, prompt }. Returns { system, prompt } or { error }. */
function parseRequest(raw) {
  let body;
  try {
    body = JSON.parse(raw);
  } catch (_e) {
    return { error: 'bad_json' };
  }
  const system = typeof body.system === 'string' ? body.system : '';
  const prompt = typeof body.prompt === 'string' ? body.prompt.trim() : '';
  if (!prompt) return { error: 'empty_prompt' };
  if (prompt.length > config.maxPromptChars) return { error: 'prompt_too_long' };
  if (system.length > config.maxSystemChars) return { error: 'system_too_long' };
  return { system, prompt };
}

function clientIp(req) {
  const fwd = String(req.headers['x-forwarded-for'] || '').split(',')[0].trim();
  return fwd || (req.socket && req.socket.remoteAddress) || 'unknown';
}

function readBody(req, limit) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    req.on('data', (c) => {
      size += c.length;
      if (size > limit) {
        reject(new Error('too_large'));
        req.destroy();
        return;
      }
      chunks.push(c);
    });
    req.on('end', () => resolve(Buffer.concat(chunks).toString('utf8')));
    req.on('error', reject);
  });
}

async function callGemini(model, system, prompt) {
  const res = await fetch(`${GEMINI_URL}/${encodeURIComponent(model)}:generateContent`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'x-goog-api-key': config.geminiKey },
    body: JSON.stringify({
      systemInstruction: { parts: [{ text: system }] },
      contents: [{ role: 'user', parts: [{ text: prompt }] }],
      generationConfig: { temperature: 0.7, maxOutputTokens: 2048 }
    }),
    signal: AbortSignal.timeout(config.timeoutMs)
  });
  if (!res.ok) throw new Error(`gemini_${res.status}`);
  const json = await res.json();
  const parts = (((json.candidates || [])[0] || {}).content || {}).parts || [];
  return parts.map((p) => p.text || '').join('').trim();
}

async function callOpenAi(model, system, prompt) {
  const res = await fetch(OPENAI_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${config.openaiKey}` },
    body: JSON.stringify({
      model,
      messages: [
        { role: 'system', content: system },
        { role: 'user', content: prompt }
      ],
      max_completion_tokens: 1024
    }),
    signal: AbortSignal.timeout(config.timeoutMs)
  });
  if (!res.ok) throw new Error(`openai_${res.status}`);
  const json = await res.json();
  return String((((json.choices || [])[0] || {}).message || {}).content || '').trim();
}

async function answer(system, prompt) {
  if (config.geminiKey) {
    for (const model of config.geminiModels) {
      try {
        const text = await callGemini(model, system, prompt);
        if (text) return { text, provider: 'gemini', model };
      } catch (_e) {
        // try the next model
      }
    }
  }
  if (config.openaiKey) {
    for (const model of config.openaiModels) {
      try {
        const text = await callOpenAi(model, system, prompt);
        if (text) return { text, provider: 'openai', model };
      } catch (_e) {
        // try the next model
      }
    }
  }
  return null;
}

async function handleAi(req, res, sendJson) {
  if (req.method !== 'POST') {
    sendJson(res, 405, { error: 'method_not_allowed' });
    return;
  }
  if (!config.geminiKey && !config.openaiKey) {
    sendJson(res, 503, { error: 'ai_not_configured' });
    return;
  }
  let raw;
  try {
    raw = await readBody(req, config.maxBodyBytes);
  } catch (_e) {
    sendJson(res, 413, { error: 'too_large' });
    return;
  }
  const parsed = parseRequest(raw);
  if (parsed.error) {
    sendJson(res, 400, { error: parsed.error });
    return;
  }
  const refused = admit(clientIp(req));
  if (refused) {
    sendJson(res, 429, { error: refused });
    return;
  }
  const result = await answer(parsed.system, parsed.prompt);
  if (!result) {
    sendJson(res, 503, { error: 'ai_unavailable' });
    return;
  }
  sendJson(res, 200, result);
}

module.exports = { handleAi, admit, parseRequest, clientIp, config, reset };
