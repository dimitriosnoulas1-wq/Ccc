# CryptoCycles market hub

One Node process pulls Binance, CoinGecko, news, and the other public feeds.
Phones read this cache instead of each opening their own API storm.

```bash
node hub/server.js
```

Default port is `8080`. Put a HTTPS reverse proxy in front of it (Caddy, nginx, Cloud Run).
Then rebuild the Android app with the hub URL baked in:

```bash
MARKET_HUB_URL=https://YOUR-HUB-HOST ./gradlew :app:assembleRelease
```

Health check: `GET /health`  
AI: `POST /v1/ai` with `{"system": "...", "prompt": "..."}` → `{"text": "..."}`  
Proxy: `GET /v1/proxy?u=<https-url>`  
Derivatives: `GET /v1/derivatives?symbol=BTC`

`/v1/derivatives` pulls public perpetual data from Binance, Bybit, and OKX, caches it for 8s, and returns venue snapshots plus an aggregated score. Missing venue fields stay `null` — the hub never invents liquidations, taker, or OI.

If `MARKET_HUB_URL` is empty, the app talks to the exchanges directly (same as before).
If the hub is down at runtime, each phone falls back to the exchanges.

## AI keys (never in the app)

The app sends AI questions to `/v1/ai`; the Gemini / OpenAI keys exist only as env vars on the hub:

```bash
GEMINI_API_KEY=...          # required for Gemini
OPENAI_API_KEY=...          # optional backup
AI_PER_IP_10MIN=20          # requests per IP per 10 minutes (default 20)
AI_DAILY_LIMIT=3000         # total requests per UTC day (default 3000)
```

On Cloud Run set them as secrets (`--set-secrets`), not in the image. The daily cap bounds the worst-case bill
if someone scripts the endpoint. `GET /health` shows `"ai": true` once a key is set.

Tests: `cd hub && npm test`
