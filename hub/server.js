'use strict';

const http = require('http');
const https = require('https');
const { URL } = require('url');
const ai = require('./ai');

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
  'farside.co.uk',
  'api.bybit.com',
  'api.bytick.com',
  'www.okx.com',
  'aws.okx.com'
]);

const cache = new Map();
const inflight = new Map();
const derivCache = new Map();
const stats = { hits: 0, misses: 0, origin: 0, blocked: 0, stale: 0 };
const MAX_CACHE = 400;
const DERIV_TTL_MS = 8_000;

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
  if (path.includes('exchangeInfo') || path.includes('/instruments')) return 30 * 60 * 1000;
  if (url.hostname.includes('coingecko')) return 45 * 1000;
  if (path.includes('/fng')) return 5 * 60 * 1000;
  if (path.includes('klines') || path.includes('market_chart')) return 15 * 1000;
  if (path.includes('ticker/24hr') && !url.search.includes('symbol=')) return 2500;
  if (path.includes('ticker/24hr') || path.includes('premiumIndex') || path.includes('bookTicker')) return 2000;
  if (
    path.includes('openInterest') ||
    path.includes('open-interest') ||
    path.includes('aggTrades') ||
    path.includes('/trades') ||
    path.includes('takerlongshort') ||
    path.includes('LongShort') ||
    path.includes('account-ratio') ||
    path.includes('fundingRate') ||
    path.includes('funding/history') ||
    path.includes('liquidation') ||
    path.includes('/rubik/')
  ) {
    return 15 * 1000;
  }
  if (path.includes('rss') || path.includes('feed') || path.includes('cms/article')) return 60 * 1000;
  if (url.hostname.includes('llama')) return 5 * 60 * 1000;
  if (url.hostname.includes('farside')) return 10 * 60 * 1000;
  if (url.hostname.includes('er-api')) return 30 * 60 * 1000;
  if (url.hostname.includes('bybit') || url.hostname.includes('bytick') || url.hostname.includes('okx')) return 8 * 1000;
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
  for (const [key, entry] of derivCache) {
    if (now - entry.at >= DERIV_TTL_MS * 4) derivCache.delete(key);
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

function num(value) {
  if (value == null || value === '') return null;
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

function isBlockedPayload(json, rawText) {
  const msg = String(
    (json && (json.msg || json.message || json.error || json.retMsg)) || rawText || ''
  );
  return (
    msg.includes('restricted location') ||
    msg.includes('Eligibility') ||
    msg.includes('block access from your country')
  );
}

async function cachedJson(urls) {
  const list = Array.isArray(urls) ? urls : [urls];
  let lastErr = 'empty';
  for (const raw of list) {
    try {
      const entry = await loadThroughCache(new URL(raw));
      if (!entry || entry.status < 200 || entry.status >= 300) {
        lastErr = 'http_' + (entry && entry.status);
        continue;
      }
      const text = entry.body.toString('utf8');
      const json = JSON.parse(text);
      if (isBlockedPayload(json, text)) {
        lastErr = 'geo_blocked';
        continue;
      }
      return { json, at: entry.at, text };
    } catch (err) {
      lastErr = err && err.message ? err.message : String(err);
    }
  }
  throw new Error(lastErr);
}

function lastPct(series) {
  if (!series || series.length < 2) return null;
  const prev = series[series.length - 2];
  const cur = series[series.length - 1];
  if (!(prev > 0) || !Number.isFinite(cur)) return null;
  return ((cur - prev) / prev) * 100;
}

function zScore(values) {
  if (!values || values.length < 8) return null;
  const latest = values[values.length - 1];
  const mean = values.reduce((a, b) => a + b, 0) / values.length;
  let variance = 0;
  for (const item of values) variance += (item - mean) ** 2;
  variance /= values.length - 1;
  const sd = Math.sqrt(variance);
  if (sd < 1e-12) return 0;
  return (latest - mean) / sd;
}

function pctSeries(series) {
  const out = [];
  if (!series || series.length < 2) return out;
  for (let i = 1; i < series.length; i += 1) {
    const change = lastPct([series[i - 1], series[i]]);
    if (change != null) out.push(change);
  }
  return out;
}

function weightedAvg(pairs) {
  let wNum = 0;
  let wDen = 0;
  let eNum = 0;
  let eCount = 0;
  for (const pair of pairs) {
    const value = pair[0];
    const weight = pair[1];
    if (!Number.isFinite(value)) continue;
    if (Number.isFinite(weight) && weight > 0) {
      wNum += value * weight;
      wDen += weight;
    } else {
      eNum += value;
      eCount += 1;
    }
  }
  if (wDen > 0) return wNum / wDen;
  if (eCount > 0) return eNum / eCount;
  return null;
}

function sumPositive(values) {
  let total = 0;
  let seen = false;
  for (const value of values) {
    if (!Number.isFinite(value) || value < 0) continue;
    total += value;
    seen = true;
  }
  return seen ? total : null;
}

function displayVenue(venue) {
  if (venue === 'binance') return 'Binance';
  if (venue === 'bybit') return 'Bybit';
  if (venue === 'okx') return 'OKX';
  return venue;
}

function sourceLabel(venuesUp) {
  const names = venuesUp.map(displayVenue);
  if (names.length === 0) return '';
  if (names.length === 1) return names[0];
  if (names.length === 2) return `${names[0]} & ${names[1]}`;
  return `${names.slice(0, -1).join(', ')} & ${names[names.length - 1]}`;
}

function freshnessOf(venues, now) {
  const up = venues.filter((v) => v.ok);
  if (up.length === 0) return 'UNAVAILABLE';
  const maxAge = Math.max(
    ...up.map((v) => Math.max(0, now - (v.asOfMs > 0 ? v.asOfMs : now)))
  );
  if (maxAge > 60_000) return 'STALE';
  if (up.length < venues.length) return 'DEGRADED';
  if (maxAge > 15_000) return 'DELAYED';
  return 'FRESH';
}

function failedVenue(venue, contract, error) {
  return {
    venue,
    contract,
    ok: false,
    asOfMs: 0,
    markPrice: null,
    lastPrice: null,
    change24hPct: null,
    fundingRate: null,
    openInterest: null,
    openInterestUsd: null,
    oiChange1hPct: null,
    oiChangeZ: null,
    fundingZ: null,
    takerBuySellRatio: null,
    longShortRatio: null,
    longLiqUsd: null,
    shortLiqUsd: null,
    missing: ['all'],
    error
  };
}

function normalizeBase(raw) {
  const clean = String(raw || 'BTC')
    .trim()
    .toUpperCase()
    .replace(/-USDT-SWAP$/, '')
    .replace(/USDT$/, '')
    .replace(/[^A-Z0-9]/g, '');
  if (clean.length < 2 || clean.length > 15) return null;
  return clean;
}

async function collectBinance(base) {
  const contract = `${base}USDT`;
  try {
    const [premium, ticker, openInterest, oiHist, taker, longShort, fundingHist] = await Promise.allSettled([
      cachedJson([
        `https://www.binance.com/fapi/v1/premiumIndex?symbol=${contract}`,
        `https://fapi.binance.com/fapi/v1/premiumIndex?symbol=${contract}`
      ]),
      cachedJson([
        `https://www.binance.com/fapi/v1/ticker/24hr?symbol=${contract}`,
        `https://fapi.binance.com/fapi/v1/ticker/24hr?symbol=${contract}`
      ]),
      cachedJson([
        `https://www.binance.com/fapi/v1/openInterest?symbol=${contract}`,
        `https://fapi.binance.com/fapi/v1/openInterest?symbol=${contract}`
      ]),
      cachedJson([
        `https://www.binance.com/futures/data/openInterestHist?symbol=${contract}&period=1h&limit=30`,
        `https://fapi.binance.com/futures/data/openInterestHist?symbol=${contract}&period=1h&limit=30`
      ]),
      cachedJson([
        `https://www.binance.com/futures/data/takerlongshortRatio?symbol=${contract}&period=1h&limit=1`,
        `https://fapi.binance.com/futures/data/takerlongshortRatio?symbol=${contract}&period=1h&limit=1`
      ]),
      cachedJson([
        `https://www.binance.com/futures/data/globalLongShortAccountRatio?symbol=${contract}&period=1h&limit=1`,
        `https://fapi.binance.com/futures/data/globalLongShortAccountRatio?symbol=${contract}&period=1h&limit=1`
      ]),
      cachedJson([
        `https://www.binance.com/fapi/v1/fundingRate?symbol=${contract}&limit=30`,
        `https://fapi.binance.com/fapi/v1/fundingRate?symbol=${contract}&limit=30`
      ])
    ]);
    const prem = premium.status === 'fulfilled' ? premium.value.json : null;
    const tick = ticker.status === 'fulfilled' ? ticker.value.json : null;
    const oi = openInterest.status === 'fulfilled' ? openInterest.value.json : null;
    const hist = oiHist.status === 'fulfilled' ? oiHist.value.json : null;
    const takerJson = taker.status === 'fulfilled' ? taker.value.json : null;
    const lsJson = longShort.status === 'fulfilled' ? longShort.value.json : null;
    const fundHist = fundingHist.status === 'fulfilled' ? fundingHist.value.json : null;
    const mark = num(prem && prem.markPrice);
    const last = num(tick && tick.lastPrice);
    const funding = num(prem && prem.lastFundingRate);
    const oiContracts = num(oi && oi.openInterest);
    const px = mark != null ? mark : last;
    const oiUsd = oiContracts != null && px != null && px > 0 ? oiContracts * px : null;
    const oiSeries = Array.isArray(hist)
      ? hist.map((row) => num(row.sumOpenInterestValue) || num(row.sumOpenInterest)).filter((v) => v != null)
      : [];
    const fundingSeries = Array.isArray(fundHist)
      ? fundHist.map((row) => num(row.fundingRate)).filter((v) => v != null)
      : [];
    const takerRow = Array.isArray(takerJson) ? takerJson[takerJson.length - 1] : null;
    const lsRow = Array.isArray(lsJson) ? lsJson[lsJson.length - 1] : null;
    const missing = [];
    if (funding == null) missing.push('funding');
    if (oiUsd == null) missing.push('openInterest');
    if (num(takerRow && takerRow.buySellRatio) == null) missing.push('taker');
    if (num(lsRow && lsRow.longShortRatio) == null) missing.push('longShort');
    missing.push('liquidations');
    const asOf = num(prem && prem.time) || num(oi && oi.time) || num(tick && tick.closeTime) || 0;
    const ok = mark != null || last != null || funding != null || oiUsd != null;
    return {
      venue: 'binance',
      contract,
      ok,
      asOfMs: asOf,
      markPrice: mark,
      lastPrice: last,
      change24hPct: num(tick && tick.priceChangePercent),
      fundingRate: funding,
      openInterest: oiContracts,
      openInterestUsd: oiUsd,
      oiChange1hPct: lastPct(oiSeries),
      oiChangeZ: zScore(pctSeries(oiSeries)),
      fundingZ: zScore(fundingSeries),
      takerBuySellRatio: num(takerRow && takerRow.buySellRatio),
      longShortRatio: num(lsRow && lsRow.longShortRatio),
      longLiqUsd: null,
      shortLiqUsd: null,
      missing,
      error: ok ? null : 'empty_binance_payload'
    };
  } catch (err) {
    return failedVenue('binance', contract, err.message || 'binance_error');
  }
}

async function collectBybit(base) {
  const contract = `${base}USDT`;
  try {
    const ticker = await cachedJson([
      `https://api.bybit.com/v5/market/tickers?category=linear&symbol=${contract}`,
      `https://api.bytick.com/v5/market/tickers?category=linear&symbol=${contract}`
    ]);
    if (!ticker.json || Number(ticker.json.retCode) !== 0) {
      return failedVenue('bybit', contract, (ticker.json && ticker.json.retMsg) || 'bybit_ret');
    }
    const [oiHist, accountRatio] = await Promise.allSettled([
      cachedJson([
        `https://api.bybit.com/v5/market/open-interest?category=linear&symbol=${contract}&intervalTime=1h&limit=30`,
        `https://api.bytick.com/v5/market/open-interest?category=linear&symbol=${contract}&intervalTime=1h&limit=30`
      ]),
      cachedJson([
        `https://api.bybit.com/v5/market/account-ratio?category=linear&symbol=${contract}&period=1h&limit=1`,
        `https://api.bytick.com/v5/market/account-ratio?category=linear&symbol=${contract}&period=1h&limit=1`
      ])
    ]);
    const row = (((ticker.json.result || {}).list) || [])[0] || null;
    const histList =
      oiHist.status === 'fulfilled' ? (((oiHist.value.json.result || {}).list) || []) : [];
    const ratioRow =
      accountRatio.status === 'fulfilled'
        ? ((((accountRatio.value.json.result || {}).list) || [])[0] || null)
        : null;
    const last = num(row && row.lastPrice);
    const mark = num(row && row.markPrice) != null ? num(row.markPrice) : last;
    const changeFrac = num(row && row.price24hPcnt);
    const funding = num(row && row.fundingRate);
    const oiContracts = num(row && row.openInterest);
    const oiUsd =
      num(row && row.openInterestValue) != null
        ? num(row.openInterestValue)
        : oiContracts != null && mark != null
          ? oiContracts * mark
          : null;
    const oiSeries = histList.map((item) => num(item.openInterest)).filter((v) => v != null);
    const buy = num(ratioRow && ratioRow.buyRatio);
    const sell = num(ratioRow && ratioRow.sellRatio);
    let ls = null;
    if (buy != null && sell != null && sell > 0) ls = buy / sell;
    else if (buy != null && buy > 0 && buy < 1) ls = buy / (1 - buy);
    const missing = ['taker', 'liquidations'];
    if (funding == null) missing.push('funding');
    if (oiUsd == null) missing.push('openInterest');
    if (ls == null) missing.push('longShort');
    const ok = !!(row && (mark != null || last != null || funding != null || oiUsd != null));
    return {
      venue: 'bybit',
      contract,
      ok,
      asOfMs: num(row && row.ts) || num(ratioRow && ratioRow.timestamp) || 0,
      markPrice: mark,
      lastPrice: last,
      change24hPct: changeFrac != null ? changeFrac * 100 : null,
      fundingRate: funding,
      openInterest: oiContracts,
      openInterestUsd: oiUsd,
      oiChange1hPct: lastPct(oiSeries),
      oiChangeZ: zScore(pctSeries(oiSeries)),
      fundingZ: null,
      takerBuySellRatio: null,
      longShortRatio: ls,
      longLiqUsd: null,
      shortLiqUsd: null,
      missing,
      error: ok ? null : 'empty_bybit_payload'
    };
  } catch (err) {
    return failedVenue('bybit', contract, err.message || 'bybit_error');
  }
}

function okxTakerRatio(data) {
  if (!Array.isArray(data) || data.length === 0 || !Array.isArray(data[0]) || data[0].length < 3) return null;
  const sell = num(data[0][1]);
  const buy = num(data[0][2]);
  if (sell == null || buy == null || !(sell > 0)) return null;
  return buy / sell;
}

function okxLongShort(data) {
  if (!Array.isArray(data) || data.length === 0 || !Array.isArray(data[0]) || data[0].length < 2) return null;
  return num(data[0][1]);
}

function okxLiquidations(json, contractUsd, fallbackPx) {
  const blocks = json && Array.isArray(json.data) ? json.data : [];
  let longUsd = 0;
  let shortUsd = 0;
  let seen = false;
  for (const block of blocks) {
    const details = (block && block.details) || [];
    for (const row of details) {
      const sz = num(row && row.sz);
      if (sz == null) continue;
      const px = num(row && row.bkPx) != null ? num(row.bkPx) : fallbackPx;
      let usd = null;
      if (contractUsd != null && contractUsd > 0) usd = sz * contractUsd;
      else if (px != null && px > 0) usd = sz * px;
      if (usd == null || usd <= 0) continue;
      seen = true;
      if (String(row.posSide || '').toLowerCase() === 'long') longUsd += usd;
      if (String(row.posSide || '').toLowerCase() === 'short') shortUsd += usd;
    }
  }
  if (!seen) return { longLiqUsd: null, shortLiqUsd: null };
  return { longLiqUsd: longUsd, shortLiqUsd: shortUsd };
}

async function collectOkx(base) {
  const contract = `${base}-USDT-SWAP`;
  const uly = `${base}-USDT`;
  try {
    const [mark, funding, openInterest, ticker, longShort, taker, liquidations] = await Promise.allSettled([
      cachedJson(`https://www.okx.com/api/v5/public/mark-price?instType=SWAP&instId=${contract}`),
      cachedJson(`https://www.okx.com/api/v5/public/funding-rate?instId=${contract}`),
      cachedJson(`https://www.okx.com/api/v5/public/open-interest?instType=SWAP&instId=${contract}`),
      cachedJson(`https://www.okx.com/api/v5/market/ticker?instId=${contract}`),
      cachedJson(`https://www.okx.com/api/v5/rubik/stat/contracts/long-short-account-ratio?ccy=${base}&period=1H`),
      cachedJson(`https://www.okx.com/api/v5/rubik/stat/taker-volume?ccy=${base}&instType=CONTRACTS&period=1H`),
      cachedJson(`https://www.okx.com/api/v5/public/liquidation-orders?instType=SWAP&uly=${uly}&state=filled`)
    ]);
    const first = (settled) => {
      if (settled.status !== 'fulfilled') return null;
      const data = settled.value.json && settled.value.json.data;
      return Array.isArray(data) ? data[0] : null;
    };
    const markRow = first(mark);
    const fundRow = first(funding);
    const oiRow = first(openInterest);
    const tickRow = first(ticker);
    const lsData = longShort.status === 'fulfilled' ? longShort.value.json.data : null;
    const takerData = taker.status === 'fulfilled' ? taker.value.json.data : null;
    const liqJson = liquidations.status === 'fulfilled' ? liquidations.value.json : null;
    const markPx = num(markRow && markRow.markPx);
    const last = num(tickRow && tickRow.last);
    const open24h = num(tickRow && tickRow.open24h);
    const change = last != null && open24h != null && open24h > 0 ? ((last - open24h) / open24h) * 100 : null;
    const fundingRate = num(fundRow && fundRow.fundingRate);
    const oiContracts = num(oiRow && oiRow.oi);
    const oiUsd = num(oiRow && oiRow.oiUsd);
    const contractUsd = oiContracts != null && oiContracts > 0 && oiUsd != null ? oiUsd / oiContracts : null;
    const liq = okxLiquidations(liqJson, contractUsd, markPx != null ? markPx : last);
    const missing = [];
    if (fundingRate == null) missing.push('funding');
    if (oiUsd == null) missing.push('openInterest');
    if (okxTakerRatio(takerData) == null) missing.push('taker');
    if (okxLongShort(lsData) == null) missing.push('longShort');
    if (liq.longLiqUsd == null && liq.shortLiqUsd == null) missing.push('liquidations');
    const ok = markPx != null || last != null || fundingRate != null || oiUsd != null;
    return {
      venue: 'okx',
      contract,
      ok,
      asOfMs: num(markRow && markRow.ts) || num(oiRow && oiRow.ts) || num(tickRow && tickRow.ts) || 0,
      markPrice: markPx,
      lastPrice: last,
      change24hPct: change,
      fundingRate,
      openInterest: oiContracts,
      openInterestUsd: oiUsd,
      oiChange1hPct: null,
      oiChangeZ: null,
      fundingZ: null,
      takerBuySellRatio: okxTakerRatio(takerData),
      longShortRatio: okxLongShort(lsData),
      longLiqUsd: liq.longLiqUsd,
      shortLiqUsd: liq.shortLiqUsd,
      missing,
      error: ok ? null : 'empty_okx_payload'
    };
  } catch (err) {
    return failedVenue('okx', contract, err.message || 'okx_error');
  }
}

function aggregateVenues(venues) {
  const live = venues.filter((v) => v.ok);
  const pick = (key) => weightedAvg(live.map((v) => [v[key], v.openInterestUsd]));
  return {
    markPrice: pick('markPrice'),
    lastPrice: pick('lastPrice'),
    change24hPct: pick('change24hPct'),
    fundingRate: pick('fundingRate'),
    openInterestUsd: sumPositive(live.map((v) => v.openInterestUsd)),
    oiChange1hPct: pick('oiChange1hPct'),
    oiChangeZ: pick('oiChangeZ'),
    fundingZ: pick('fundingZ'),
    takerBuySellRatio: pick('takerBuySellRatio'),
    longShortRatio: pick('longShortRatio'),
    longLiqUsd: sumPositive(live.map((v) => v.longLiqUsd)),
    shortLiqUsd: sumPositive(live.map((v) => v.shortLiqUsd)),
    venueCount: live.length
  };
}

function scoreMetrics(metrics) {
  const weights = { funding: 20, oi: 25, liq: 20, taker: 20, longShort: 15 };
  const components = [];
  if (Number.isFinite(metrics.fundingRate) || Number.isFinite(metrics.fundingZ)) {
    const raw = Number.isFinite(metrics.fundingZ)
      ? -((metrics.fundingZ / 2) * 100)
      : (-metrics.fundingRate / 0.0005) * 100;
    components.push({ name: 'funding', raw: clamp(raw, -100, 100), configuredWeight: weights.funding, used: true });
  } else {
    components.push({ name: 'funding', raw: 0, configuredWeight: weights.funding, used: false });
  }
  if (Number.isFinite(metrics.oiChange1hPct) || Number.isFinite(metrics.oiChangeZ)) {
    const magnitude = Number.isFinite(metrics.oiChangeZ)
      ? clamp((metrics.oiChangeZ / 2) * 100, -100, 100)
      : clamp((metrics.oiChange1hPct / 2) * 100, -100, 100);
    let signed = magnitude * 0.35;
    if (Number.isFinite(metrics.change24hPct)) {
      signed = metrics.change24hPct >= 0 ? magnitude : -magnitude;
    }
    components.push({ name: 'oi', raw: clamp(signed, -100, 100), configuredWeight: weights.oi, used: true });
  } else {
    components.push({ name: 'oi', raw: 0, configuredWeight: weights.oi, used: false });
  }
  if (Number.isFinite(metrics.longLiqUsd) || Number.isFinite(metrics.shortLiqUsd)) {
    const longs = Math.max(0, metrics.longLiqUsd || 0);
    const shorts = Math.max(0, metrics.shortLiqUsd || 0);
    const total = longs + shorts;
    if (total > 0) {
      components.push({
        name: 'liq',
        raw: clamp(((shorts - longs) / total) * 100, -100, 100),
        configuredWeight: weights.liq,
        used: true
      });
    } else {
      components.push({ name: 'liq', raw: 0, configuredWeight: weights.liq, used: false });
    }
  } else {
    components.push({ name: 'liq', raw: 0, configuredWeight: weights.liq, used: false });
  }
  if (Number.isFinite(metrics.takerBuySellRatio)) {
    components.push({
      name: 'taker',
      raw: clamp(((metrics.takerBuySellRatio - 1) / 0.3) * 100, -100, 100),
      configuredWeight: weights.taker,
      used: true
    });
  } else {
    components.push({ name: 'taker', raw: 0, configuredWeight: weights.taker, used: false });
  }
  if (Number.isFinite(metrics.longShortRatio)) {
    components.push({
      name: 'longShort',
      raw: clamp(((1 - metrics.longShortRatio) / 0.8) * 100, -100, 100),
      configuredWeight: weights.longShort,
      used: true
    });
  } else {
    components.push({ name: 'longShort', raw: 0, configuredWeight: weights.longShort, used: false });
  }
  const used = components.filter((c) => c.used);
  if (used.length === 0) {
    return { value: null, redistributed: false, components };
  }
  const usedSum = used.reduce((a, c) => a + c.configuredWeight, 0);
  const scored = components.map((c) => ({
    ...c,
    usedWeight: c.used && usedSum > 0 ? (c.configuredWeight / usedSum) * 100 : 0
  }));
  const value = clamp(
    scored.filter((c) => c.used).reduce((a, c) => a + c.raw * (c.usedWeight / 100), 0),
    -100,
    100
  );
  return { value, redistributed: used.length < components.length, components: scored };
}

function clamp(n, lo, hi) {
  return Math.min(hi, Math.max(lo, n));
}

function assembleDerivatives(symbol, venues, now) {
  const aggregated = aggregateVenues(venues);
  const up = venues.filter((v) => v.ok).map((v) => v.venue);
  return {
    ok: up.length > 0,
    symbol,
    asOfMs: Math.max(0, ...venues.filter((v) => v.ok).map((v) => v.asOfMs)) || now,
    freshness: freshnessOf(venues, now),
    sourceLabel: sourceLabel(up),
    venues,
    aggregated,
    score: {
      ...scoreMetrics(aggregated),
      formula:
        'score = sum(component * usedWeight) / sum(usedWeight); funding 20 (crowded +funding → negative); oi 25 (1h change z-score, signed by 24h price); liq 20 ((shortLiq-longLiq)/(short+long)); taker 20 ((ratio-1)/0.3); ls 15 ((1-ratio)/0.8); missing weights redistribute; never fabricate a venue field'
    }
  };
}

async function buildDerivatives(symbol) {
  const [binance, bybit, okx] = await Promise.all([
    collectBinance(symbol),
    collectBybit(symbol),
    collectOkx(symbol)
  ]);
  return assembleDerivatives(symbol, [binance, bybit, okx], Date.now());
}

async function loadDerivatives(symbol) {
  const key = `deriv:${symbol}`;
  const hit = derivCache.get(key);
  const now = Date.now();
  if (hit && now - hit.at < DERIV_TTL_MS) {
    stats.hits += 1;
    return hit.body;
  }
  const body = await buildDerivatives(symbol);
  derivCache.set(key, { at: Date.now(), body });
  return body;
}

function warmDerivatives() {
  loadDerivatives('BTC').catch(() => {});
  loadDerivatives('ETH').catch(() => {});
}

const server = http.createServer(async (req, res) => {
  try {
    const incoming = new URL(req.url || '/', `http://127.0.0.1:${PORT}`);
    if (req.method === 'OPTIONS') {
      res.writeHead(204, {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Accept, User-Agent, Content-Type'
      });
      res.end();
      return;
    }
    if (incoming.pathname === '/health' || incoming.pathname === '/') {
      sendJson(res, 200, {
        ok: true,
        service: 'cryptocycles-hub',
        cache: cache.size,
        derivatives: derivCache.size,
        ai: Boolean(ai.config.geminiKey || ai.config.openaiKey),
        stats
      });
      return;
    }
    if (incoming.pathname === '/v1/ai') {
      await ai.handleAi(req, res, sendJson);
      return;
    }
    if (incoming.pathname === '/v1/derivatives') {
      const symbol = normalizeBase(incoming.searchParams.get('symbol') || 'BTC');
      if (!symbol) {
        sendJson(res, 400, { error: 'bad_symbol' });
        return;
      }
      const body = await loadDerivatives(symbol);
      sendJson(res, 200, body);
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
  warmDerivatives();
});

setInterval(() => warm(WARM_FAST), 4_000);
setInterval(() => warm(WARM_SLOW), 40_000);
setInterval(warmDerivatives, 8_000);
setInterval(pruneCache, 60_000);
