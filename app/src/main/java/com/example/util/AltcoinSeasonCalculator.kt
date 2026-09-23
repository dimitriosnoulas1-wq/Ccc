package com.example.util

import com.example.data.model.AltcoinSeasonData
import com.example.data.model.CryptoCoin
import com.example.data.network.MarketDataClient
import org.json.JSONArray
import kotlin.math.roundToInt

object AltcoinSeasonCalculator {

    private var cachedData: AltcoinSeasonData? = null
    private var lastFetchTimestamp: Long = 0L
    private const val CACHE_DURATION_MS = 3600_000L // 1 hour in-memory cache

    private val excludedSymbols = setOf(
        "USDT", "USDC", "DAI", "FDUSD", "USDD", "USDE", "TUSD", "BUSD", "PYUSD", "USDP", "GUSD", "LUSD", "FRAX",
        "WBTC", "WETH", "STETH", "WEETH", "CBETH", "RETH", "SOLVBTC", "EZETH", "RSETH", "HBTC", "WBNB", "WSOL"
    )

    /**
    * Calculates live Altcoin Season data using supported CoinGecko 30d parameters,
    * filtering stablecoins & wrapped tokens, and falling back reliably to real coin metrics.
    */
    fun calculate(
        coins: List<CryptoCoin>,
        centralizedBtcPrice: Double = 0.0
    ): AltcoinSeasonData {
        val now = System.currentTimeMillis()
        if (cachedData != null && (now - lastFetchTimestamp) < CACHE_DURATION_MS) {
            return cachedData!!
        }

        try {
            // Note: CoinGecko supports 30d (not 90d) for market percentage queries
            val bodyString = MarketDataClient.getText(
                "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=65&sparkline=false&price_change_percentage=30d",
                attempts = 2
            )
            if (!bodyString.isNullOrBlank()) {
                    val jsonArray = JSONArray(bodyString)
                    var btcChange = Double.NaN
                    val marketCoins = mutableListOf<Pair<String, Double>>()

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val symbol = obj.optString("symbol", "").uppercase()
                        val change30d = obj.optDouble("price_change_percentage_30d_in_currency", Double.NaN)

                        if (symbol == "BTC") {
                            if (!change30d.isNaN()) {
                                btcChange = change30d
                            }
                        } else if (!excludedSymbols.contains(symbol) && symbol.isNotEmpty()) {
                            val validChange = if (!change30d.isNaN()) change30d else 0.0
                            marketCoins.add(symbol to validChange)
                        }
                    }

                    val top50Alts = marketCoins.take(50)
                    if (top50Alts.isNotEmpty() && !btcChange.isNaN()) {
                        val outperforming = top50Alts.filter { it.second > btcChange }
                        val outperformingCount = outperforming.size
                        val finalScore = ((outperformingCount.toDouble() / top50Alts.size.toDouble()) * 100.0)
                            .roundToInt()
                            .coerceIn(0, 100)

                        val topPerformers = top50Alts
                            .sortedByDescending { it.second }
                            .take(7)
                            .map { it.first to ((it.second * 10.0).roundToInt() / 10.0) }

                        val result = AltcoinSeasonData(
                            score = finalScore,
                            previousMonthScore = 0,
                            previousYearScore = 0,
                            topOutperformingCoins = topPerformers,
                            btcGain90d = (btcChange * 10.0).roundToInt() / 10.0,
                            top50OutperformedCount = outperformingCount,
                            totalTop50Count = top50Alts.size,
                            isAvailable = true
                        )

                        cachedData = result
                        lastFetchTimestamp = now
                        return result
                    }
            }
        } catch (_: Throwable) {
            // Fallback gracefully on network/rate-limit error
        }

        if (cachedData != null) {
            return cachedData!!
        }

        return calculateLocalFallback(coins, centralizedBtcPrice)
    }

    fun calculateLocalFallback(coins: List<CryptoCoin>, @Suppress("UNUSED_PARAMETER") centralizedBtcPrice: Double): AltcoinSeasonData {
        val liveCoins = coins.filter { it.priceUsd > 0.0 && it.priceUpdatedAtMs > 0L }
        val btcCoin = liveCoins.firstOrNull { it.symbol.equals("BTC", ignoreCase = true) }
        if (liveCoins.size < 10 || btcCoin == null) {
            return AltcoinSeasonData()
        }

        val btcGain = btcCoin.change24h
        val altcoins = liveCoins.filter { coin ->
            !coin.symbol.equals("BTC", ignoreCase = true) &&
            !excludedSymbols.contains(coin.symbol.uppercase())
        }.take(50)
        if (altcoins.isEmpty()) return AltcoinSeasonData()

        val altPerformanceList = altcoins.map { alt ->
            Triple(alt.symbol.uppercase(), alt.change24h, alt.change24h > btcGain)
        }
        val outperformingCount = altPerformanceList.count { it.third }
        val finalScore = ((outperformingCount.toDouble() / altcoins.size.toDouble()) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)

        val topPerformers = altPerformanceList
            .sortedByDescending { it.second }
            .take(7)
            .map { (symbol, gain, _) -> symbol to ((gain * 10.0).roundToInt() / 10.0) }

        return AltcoinSeasonData(
            score = finalScore,
            previousMonthScore = 0,
            previousYearScore = 0,
            topOutperformingCoins = topPerformers,
            btcGain90d = (btcGain * 10.0).roundToInt() / 10.0,
            top50OutperformedCount = outperformingCount,
            totalTop50Count = altcoins.size,
            isAvailable = true
        )
    }
}
