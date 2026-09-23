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
Proxy: `GET /v1/proxy?u=<https-url>`

If `MARKET_HUB_URL` is empty, the app talks to the exchanges directly (same as before).
If the hub is down at runtime, each phone falls back to the exchanges.
