'use strict';

const http = require('http');
const https = require('https');
const { URL } = require('url');

const PORT = Number(process.env.PORT || 8080);
const ALLOWED_HOSTS = new Set([
  'data-api.binance.vision',
  'api.binance.com',
  'www.binance.com',
  'fapi.binance.com',
  'api.coingecko.com',
  'open.er-api.com',
  'api.alternative.me',
  'cointelegraph.com',
  'www.coindesk.com',
  'decrypt.co',
  'stablecoins.llama.fi',
  'farside.co.uk'
]);

const cache = new Map();
const inflight = new Map();
const stats = { hits: 0, misses: 0, origin: 0, blocked: 0, stale: 0 };
const MAX_CACHE = 400;

const WARM_FAST = [
  'https://data-api.binance.vision/api/v3/ticker/24hr',
  'https://www.binance.com/fapi/v1/ticker/24hr',
  'https://www.binance.com/fapi/v1/premiumIndex?symbol=BTCUSDT',
  'https://api.alternative.me/fng/?limit=32'
];
const WARM_SLOW = [
  'https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=250&page=1&sparkline=true',
  'https://api.coingecko.com/api/v3/global',
  'https://open.er-api.com/v6/latest/USD',
  'https://stablecoins.llama.fi/stablecoins?includePrices=true',
  'https://data-api.binance.vision/api/v3/exchangeInfo',
  'https://www.binance.com/fapi/v1/exchangeInfo'
];

function ttlFor(url) {
  const path = url.pathname + url.search;
  if (path.includes('exchangeInfo')) return 30 * 60 * 1000;
  if (url.hostname.includes('coingecko')) return 45 * 1000;
  if (path.includes('/fng')) return 5 * 60 * 1000;
  if (path.includes('klines') || path.includes('market_chart')) return 15 * 1000;
  if (path.includes('ticker/24hr') && !url.search.includes('symbol=')) return 2500;
  if (path.includes('ticker/24hr') || path.includes('premiumIndex') || path.includes('bookTicker')) return 2000;
  if (path.includes('openInterest') || path.includes('aggTrades') || path.includes('/trades')) return 2500;
  if (path.includes('rss') || path.includes('feed') || path.includes('cms/article')) return 60 * 1000;
  if (url.hostname.includes('llama')) return 5 * 60 * 1000;
  if (url.hostname.includes('farside')) return 10 * 60 * 1000;
  if (url.hostname.includes('er-api')) return 30 * 60 * 1000;
  return 4000;
}

function cacheKey(url) {
  let host = url.hostname;
  if (host === 'api.binance.com' || host === 'data-api.binance.vision') host = 'binance-spot';
  if (host === 'fapi.binance.com' || (host === 'www.binance.com' && url.pathname.startsWith('/fapi/'))) {
    host = 'binance-fapi';
  }
  return `${host}${url.pathname}${url.search}`;
}

function fetchOrigin(url) {
  return new Promise((resolve, reject) => {
    const lib = url.protocol === 'http:' ? http : https;
    const req = lib.request(
      url,
      {
        method: 'GET',
        headers: {
          'User-Agent': 'CryptoCycles-Hub/1.0',
          Accept: 'application/json, application/rss+xml, application/xml, text/html, */*'
        }
      },
      (res) => {
        const chunks = [];
        res.on('data', (c) => chunks.push(c));
        res.on('end', () => {
          resolve({
            status: res.statusCode || 502,
            body: Buffer.concat(chunks),
            type: res.headers['content-type'] || 'application/octet-stream'
          });
        });
      }
    );
    req.setTimeout(12000, () => req.destroy(new Error('timeout')));
    req.on('error', reject);
    req.end();
  });
}

function pruneCache() {
  const now = Date.now();
  for (const [key, entry] of cache) {
    if (now - entry.at >= entry.ttl * 4) cache.delete(key);
  }
  while (cache.size > MAX_CACHE) {
    const oldest = cache.keys().next().value;
    if (!oldest) break;
    cache.delete(oldest);
  }
}

async function loadThroughCache(target) {
  const key = cacheKey(target);
  const now = Date.now();
  const hit = cache.get(key);
  if (hit && now - hit.at < hit.ttl) {
    stats.hits += 1;
    return hit;
  }
  if (inflight.has(key)) {
    stats.hits += 1;
    return inflight.get(key);
  }
  stats.misses += 1;
  const work = fetchOrigin(target)
    .then((origin) => {
      stats.origin += 1;
      if (origin.status >= 200 && origin.status < 300) {
        const entry = {
          status: origin.status,
          body: origin.body,
          type: origin.type,
          at: Date.now(),
          ttl: ttlFor(target)
        };
        cache.set(key, entry);
        return entry;
      }
      if (hit) {
        stats.stale += 1;
        return hit;
      }
      return {
        status: origin.status,
        body: origin.body,
        type: origin.type,
        at: Date.now(),
        ttl: 0
      };
    })
    .catch((err) => {
      if (hit) {
        stats.stale += 1;
        return hit;
      }
      throw err;
    })
    .finally(() => inflight.delete(key));
  inflight.set(key, work);
  return work;
}

function sendJson(res, code, obj) {
  const body = JSON.stringify(obj);
  res.writeHead(code, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Cache-Control': 'no-store'
  });
  res.end(body);
}

function warm(list) {
  for (const raw of list) {
    loadThroughCache(new URL(raw)).catch(() => {});
  }
}

const server = http.createServer(async (req, res) => {
  try {
    const incoming = new URL(req.url || '/', `http://127.0.0.1:${PORT}`);
    if (req.method === 'OPTIONS') {
      res.writeHead(204, {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, OPTIONS',
        'Access-Control-Allow-Headers': 'Accept, User-Agent'
      });
      res.end();
      return;
    }
    if (incoming.pathname === '/health' || incoming.pathname === '/') {
      sendJson(res, 200, { ok: true, service: 'cryptocycles-hub', cache: cache.size, stats });
      return;
    }
    if (incoming.pathname !== '/v1/proxy') {
      sendJson(res, 404, { error: 'not_found' });
      return;
    }
    const raw = incoming.searchParams.get('u') || '';
    let target;
    try {
      target = new URL(raw);
    } catch (_err) {
      sendJson(res, 400, { error: 'bad_url' });
      return;
    }
    if (target.protocol !== 'https:' || !ALLOWED_HOSTS.has(target.hostname)) {
      stats.blocked += 1;
      sendJson(res, 403, { error: 'host_not_allowed' });
      return;
    }
    const entry = await loadThroughCache(target);
    const fresh = cache.has(cacheKey(target)) && Date.now() - cache.get(cacheKey(target)).at < cache.get(cacheKey(target)).ttl;
    res.writeHead(entry.status, {
      'Content-Type': entry.type,
      'Access-Control-Allow-Origin': '*',
      'X-Hub-Cache': fresh ? 'HIT' : 'MISS'
    });
    res.end(entry.body);
  } catch (err) {
    sendJson(res, 502, { error: 'origin_failed', message: String(err && err.message ? err.message : err) });
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`CryptoCycles hub listening on ${PORT}`);
  warm(WARM_FAST);
  warm(WARM_SLOW);
});

setInterval(() => warm(WARM_FAST), 4_000);
setInterval(() => warm(WARM_SLOW), 40_000);
setInterval(pruneCache, 60_000);
