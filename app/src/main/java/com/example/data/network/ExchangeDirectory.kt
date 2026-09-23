package com.example.data.network

import org.json.JSONArray
import org.json.JSONObject

/**
 * TRADING USDT markets on Binance spot and USDT-M futures.
 * A ticker row is not enough: delisted symbols such as TONUSDT still return a last price.
 */
object ExchangeDirectory {
    @Volatile private var spotPairs: Set<String> = emptySet()
    @Volatile private var futuresPairs: Set<String> = emptySet()
    @Volatile private var loadedAtMs: Long = 0L

    private const val TTL_MS = 30 * 60 * 1000L

    fun ensureLoaded() {
        val now = System.currentTimeMillis()
        if (spotPairs.isNotEmpty() && now - loadedAtMs < TTL_MS) return
        val spot = loadPairs(
            listOf(
                "https://data-api.binance.vision/api/v3/exchangeInfo",
                "https://api.binance.com/api/v3/exchangeInfo"
            ),
            futures = false
        )
        val futures = loadPairs(
            listOf(
                "https://www.binance.com/fapi/v1/exchangeInfo",
                "https://fapi.binance.com/fapi/v1/exchangeInfo"
            ),
            futures = true
        )
        if (spot.isNotEmpty()) spotPairs = spot
        if (futures.isNotEmpty()) futuresPairs = futures
        if (spot.isNotEmpty() || futures.isNotEmpty()) loadedAtMs = now
    }

    fun spotPairs(): Set<String> = spotPairs

    fun futuresPairs(): Set<String> = futuresPairs

    fun listingFor(symbol: String): SymbolMath.Listing? {
        ensureLoaded()
        return SymbolMath.spotFirstListing(symbol, spotPairs, futuresPairs)
    }

    fun futuresSymbolFor(symbol: String): String {
        ensureLoaded()
        return SymbolMath.futuresPair(symbol, futuresPairs)
    }

    private fun loadPairs(urls: List<String>, futures: Boolean): Set<String> {
        val body = MarketDataClient.getText(urls) ?: return emptySet()
        return try {
            val symbols = JSONObject(body).optJSONArray("symbols") ?: JSONArray()
            buildSet {
                for (i in 0 until symbols.length()) {
                    val item = symbols.optJSONObject(i) ?: continue
                    val pair = item.optString("symbol")
                    if (!pair.endsWith("USDT")) continue
                    if (item.optString("status") != "TRADING") continue
                    if (futures) {
                        val contract = item.optString("contractType")
                        if (contract.isNotEmpty() && contract != "PERPETUAL") continue
                    }
                    add(pair)
                }
            }
        } catch (_: Exception) {
            emptySet()
        }
    }
}
