package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CryptoCoin
import org.json.JSONObject

object PriceCacheManager {
    private const val PREFS_NAME = "crypto_cycles_price_cache"
    private const val CACHE_VERSION = 2
    private const val SAVE_MIN_INTERVAL_MS = 15_000L
    private var prefs: SharedPreferences? = null
    @Volatile private var lastSaveMs = 0L

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getCacheTimestamp(): Long {
        val p = prefs ?: return 0L
        return p.getLong("cache_timestamp", 0L)
    }

    fun isCacheStale(): Boolean {
        val ts = getCacheTimestamp()
        if (ts == 0L) return true
        return (System.currentTimeMillis() - ts) > 900_000L // 15 minutes = 900,000 ms
    }

    fun savePrices(coins: List<CryptoCoin>) {
        val p = prefs ?: return
        val now = System.currentTimeMillis()
        if (now - lastSaveMs < SAVE_MIN_INTERVAL_MS) return
        lastSaveMs = now
        try {
            val json = JSONObject()
            for (coin in coins) {
                if (coin.quoteState != com.example.data.model.QuoteState.LIVE || coin.priceUsd <= 0.0) continue
                val coinObj = JSONObject()
                coinObj.put("price", coin.priceUsd)
                coinObj.put("change", coin.change24h)
                coinObj.put("volume", coin.volume24h)
                coinObj.put("ath", coin.athUsd)
                json.put(coin.symbol.uppercase(), coinObj)
            }
            p.edit()
                .putString("cached_prices_json", json.toString())
                .putLong("cache_timestamp", System.currentTimeMillis())
                .putInt("cache_version", CACHE_VERSION)
                .apply()
        } catch (_: Exception) {}
    }

    fun applyCachedPrices(coins: List<CryptoCoin>): List<CryptoCoin> {
        val p = prefs ?: return coins
        if (p.getInt("cache_version", 0) != CACHE_VERSION) return coins
        val str = p.getString("cached_prices_json", null) ?: return coins

        // Carry the real cache age through, rather than asserting the restored prices are live.
        // A cached price then ages out on its own and cannot block a live source from correcting it.
        val cachedAtMs = getCacheTimestamp()

        try {
            val json = JSONObject(str)
            return coins.map { coin ->
                val sym = coin.symbol.uppercase()
                if (json.has(sym)) {
                    val coinObj = json.getJSONObject(sym)
                    val price = coinObj.optDouble("price", coin.priceUsd)
                    val change = coinObj.optDouble("change", coin.change24h)
                    val volume = coinObj.optDouble("volume", coin.volume24h)
                    val ath = coinObj.optDouble("ath", coin.athUsd)
                    coin.copy(
                        priceUsd = if (price > 0.0) price else coin.priceUsd,
                        change24h = change,
                        volume24h = if (volume > 0.0) volume else coin.volume24h,
                        athUsd = if (ath > 0.0) ath else coin.athUsd,
                        priceUpdatedAtMs = cachedAtMs,
                        quoteState = com.example.data.model.QuoteState.LIVE
                    )
                } else {
                    coin
                }
            }
        } catch (_: Exception) {
            return coins
        }
    }
}
