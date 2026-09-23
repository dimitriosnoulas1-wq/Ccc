package com.example.data.model

enum class FeedState {
    LIVE,
    STALE,
    OFFLINE,
    UNAVAILABLE
}

data class MetricValue<T>(
    val value: T?,
    val source: String = "Binance Futures",
    val eventTimeMs: Long = 0L,
    val receivedTimeMs: Long = 0L,
    val isAvailable: Boolean = true
) {
    val ageSeconds: Double
        get() = if (receivedTimeMs > 0) ((System.currentTimeMillis() - receivedTimeMs) / 1000.0).coerceAtLeast(0.0) else 0.0

    val feedState: FeedState
        get() {
            if (!isAvailable || value == null) return FeedState.UNAVAILABLE
            if (receivedTimeMs == 0L) return FeedState.OFFLINE
            val ageMs = System.currentTimeMillis() - receivedTimeMs
            return if (ageMs > 5000) FeedState.STALE else FeedState.LIVE
        }

    val ageDisplay: String
        get() {
            val age = ageSeconds
            return if (age < 1.0) "0.${(age * 10).toInt() % 10}s" else "${"%.1f".format(java.util.Locale.US, age)}s"
        }
}

data class FuturesTickerData(
    val symbol: String,
    val lastPrice: Double?,
    val priceChange24h: Double?,
    val priceChangePercent24h: Double?,
    val high24h: Double?,
    val low24h: Double?,
    val volumeBase24h: Double?,
    val volumeQuote24h: Double?,
    val eventTimeMs: Long,
    val receivedTimeMs: Long,
    val fromExchange: Boolean = false
)

data class FuturesBookTicker(
    val symbol: String,
    val bidPrice: Double?,
    val bidQty: Double?,
    val askPrice: Double?,
    val askQty: Double?,
    val spread: Double?,
    val spreadPercent: Double?,
    val eventTimeMs: Long,
    val receivedTimeMs: Long,
    val fromExchange: Boolean = false
)

data class FuturesMarkFunding(
    val symbol: String,
    val markPrice: Double?,
    val indexPrice: Double?,
    val basis: Double?, // markPrice - indexPrice
    val basisPercent: Double?,
    val fundingRate: Double?,
    val nextFundingTimeMs: Long?,
    val approxApr: Double?, // rate * 3 * 365 * 100
    val eventTimeMs: Long,
    val receivedTimeMs: Long,
    val fromExchange: Boolean = false
)

data class FuturesOpenInterest(
    val symbol: String,
    val openInterest: Double?,
    val openInterestUsd: Double?,
    val lastRefreshTimeMs: Long,
    val isAvailable: Boolean = true
)

data class FuturesTrade(
    val id: Long,
    val symbol: String,
    val price: Double,
    val qty: Double,
    val isBuyerMaker: Boolean, // true = Market Sell (Red), false = Market Buy (Green)
    val timeMs: Long
) {
    val isSell: Boolean get() = isBuyerMaker
    val valueUsd: Double get() = price * qty
}

data class FuturesLiquidationOrder(
    val id: String,
    val symbol: String,
    val side: String, // "BUY" (short liquidated) or "SELL" (long liquidated)
    val price: Double,
    val qty: Double,
    val timeMs: Long,
    val valueUsd: Double
) {
    val isLongLiquidated: Boolean get() = side.equals("SELL", ignoreCase = true)
}

data class FuturesConnectionStatus(
    val isConnected: Boolean = false,
    val latencyMs: Long = 0L,
    val activeSymbol: String = "BTCUSDT",
    val reconnectAttempts: Int = 0,
    val lastEventTimeMs: Long = 0L
)

data class MacroMarketSentiment(
    val fearAndGreedValue: Int? = null,
    val fearAndGreedClassification: String? = null,
    val fearAndGreedYesterday: Int? = null,
    val fearAndGreedLastWeek: Int? = null,
    val fearAndGreedLastMonth: Int? = null,
    val fearAndGreedUpdatedMs: Long = 0L,
    val btcDominance: Double? = null,
    val btcDominanceUpdatedMs: Long = 0L
)
