'use strict';

const test = require('node:test');
const assert = require('node:assert');
const http = require('node:http');
const ai = require('./ai');

test('cheapest models first', () => {
  assert.deepStrictEqual(ai.config.geminiModels, ['gemini-3.5-flash-lite', 'gemini-3.1-flash-lite', 'gemini-3.6-flash']);
  assert.deepStrictEqual(ai.config.openaiModels, ['gpt-5-nano', 'gpt-4.1-nano', 'gpt-4o-mini']);
});

test('per-IP limit and daily cap', () => {
  ai.reset();
  const t0 = Date.parse('2026-09-26T10:00:00Z');
  for (let i = 0; i < ai.config.perIpPer10Min; i++) assert.strictEqual(ai.admit('1.1.1.1', t0 + i), null);
  assert.strictEqual(ai.admit('1.1.1.1', t0 + 100), 'rate_limited');
  assert.strictEqual(ai.admit('2.2.2.2', t0 + 100), null);
  assert.strictEqual(ai.admit('1.1.1.1', t0 + 11 * 60_000), null);

  ai.reset();
  const saved = ai.config.perDay;
  ai.config.perDay = 3;
  for (let i = 0; i < 3; i++) assert.strictEqual(ai.admit(`ip${i}`, t0), null);
  assert.strictEqual(ai.admit('ip9', t0), 'daily_limit');
  assert.strictEqual(ai.admit('ip9', t0 + 24 * 3600_000), null);
  ai.config.perDay = saved;
  ai.reset();
});

test('request validation', () => {
  assert.deepStrictEqual(ai.parseRequest('nope'), { error: 'bad_json' });
  assert.deepStrictEqual(ai.parseRequest('{"prompt":"  "}'), { error: 'empty_prompt' });
  assert.deepStrictEqual(ai.parseRequest(JSON.stringify({ prompt: 'x'.repeat(2001) })), { error: 'prompt_too_long' });
  assert.deepStrictEqual(ai.parseRequest(JSON.stringify({ prompt: 'hi', system: 'y'.repeat(40_001) })), { error: 'system_too_long' });
  assert.deepStrictEqual(ai.parseRequest('{"system":"s","prompt":" btc? "}'), { system: 's', prompt: 'btc?' });
});

test('client ip prefers the first forwarded address', () => {
  assert.strictEqual(ai.clientIp({ headers: { 'x-forwarded-for': '9.9.9.9, 10.0.0.1' }, socket: {} }), '9.9.9.9');
  assert.strictEqual(ai.clientIp({ headers: {}, socket: { remoteAddress: '8.8.8.8' } }), '8.8.8.8');
});

function send(res, code, obj) {
  res.writeHead(code, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify(obj));
}

async function withServer(fn) {
  const server = http.createServer((req, res) => ai.handleAi(req, res, send));
  await new Promise((r) => server.listen(0, '127.0.0.1', r));
  try {
    return await fn(`http://127.0.0.1:${server.address().port}`);
  } finally {
    server.close();
  }
}

test('endpoint answers with the key held server-side and never echoes it', async () => {
  ai.reset();
  const realFetch = global.fetch;
  const saved = { ...ai.config };
  ai.config.geminiKey = 'SECRET-GEMINI';
  ai.config.openaiKey = '';
  let seenKey = null;
  global.fetch = async (url, init) => {
    if (String(url).startsWith('http://127.0.0.1')) return realFetch(url, init);
    seenKey = init.headers['x-goog-api-key'];
    return new Response(JSON.stringify({ candidates: [{ content: { parts: [{ text: 'BTC is on day 889.' }] } }] }), { status: 200 });
  };
  try {
    await withServer(async (base) => {
      const res = await realFetch(`${base}/v1/ai`, { method: 'POST', body: JSON.stringify({ system: 'ctx', prompt: 'day?' }) });
      const text = await res.text();
      assert.strictEqual(res.status, 200);
      assert.strictEqual(JSON.parse(text).text, 'BTC is on day 889.');
      assert.ok(!text.includes('SECRET-GEMINI'));
      assert.strictEqual(seenKey, 'SECRET-GEMINI');

      const bad = await realFetch(`${base}/v1/ai`, { method: 'GET' });
      assert.strictEqual(bad.status, 405);
    });
  } finally {
    global.fetch = realFetch;
    Object.assign(ai.config, saved);
    ai.reset();
  }
});

test('no keys configured means 503, not a crash', async () => {
  const saved = { ...ai.config };
  ai.config.geminiKey = '';
  ai.config.openaiKey = '';
  try {
    await withServer(async (base) => {
      const res = await fetch(`${base}/v1/ai`, { method: 'POST', body: '{"prompt":"hi"}' });
      assert.strictEqual(res.status, 503);
    });
  } finally {
    Object.assign(ai.config, saved);
  }
});
