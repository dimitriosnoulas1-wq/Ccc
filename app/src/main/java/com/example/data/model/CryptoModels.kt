package com.example.data.model

import java.util.Locale

/**
 * Short tickers such as "S" must match a whole word. A plain contains() check would treat every
 * sentence that includes the letter as a mention of that coin.
 */
fun queryMentionsSymbol(query: String, symbol: String): Boolean {
    val sym = symbol.lowercase()
    if (sym.isEmpty()) return false
    if (sym.length <= 2) {
        return Regex("\\b${Regex.escape(sym)}\\b").containsMatchIn(query)
    }
    return query.contains(sym)
}

enum class QuoteState {
    /** A live request has not finished yet. */
    PENDING,
    /** priceUsd was written by Binance or CoinGecko. */
    LIVE,
    /** Both exchanges were asked and neither lists this asset. */
    UNAVAILABLE
}

enum class CoinCategory(val displayName: String) {
    ALL("100+"),
    LAYER1("Layer 1"),
    LAYER2("Layer 2"),
    DEFI("DeFi"),
    AI_INFRA("AI & Infra"),
    MEME("Meme"),
    RWA_DEPIN("RWA & DePIN"),
    UTILITY("Utility"),
    FAVORITES("Favorites")
}

enum class CyclePhase(val title: String, val description: String, val colorHex: Long) {
    ACCUMULATION("Accumulation Phase", "Smart money accumulation and cycle bottom establishment.", 0xFF00FF88),
    EXPANSION("Expansion Phase", "Post-halving liquidity growth and momentum breakout.", 0xFF00F5FF),
    EUPHORIA_TOP("Euphoria Peak Window", "Parabolic blow-off top risk zone. Consider scaling profits.", 0xFFF59E0B),
    BEAR_CAPITULATION("Cycle Cooldown / Reset", "Deep post-peak drawdown and market reset.", 0xFFF43F5E)
}

enum class Currency(val symbol: String, val code: String, var rateToUsd: Double) {
    USD("$", "USD", 1.0),
    EUR("€", "EUR", 0.924),
    GBP("£", "GBP", 0.791)
}

enum class AppThemeOption(val key: String) {
    GALAXY_DARK("galaxy"),
    PURE_BLACK("black"),
    CLEAN_WHITE("white")
}

data class HistoricalAnalog(
    val matchingDate2020: String,
    val matchingDate2016: String,
    val matchingCycleDay: Int,
    val gainPostMatchingDate2020: Double,
    val gainPostMatchingDate2016: Double,
    val projectedCyclePeak: Double,
    val projectedCycleBottom: Double,
    val cyclePhase: CyclePhase,
    val cyclePhaseName: String,
    val cycleClockProgress: Float, // 0.0 to 1.0 within 4-year cycle
    val historicalCyclePointsCurrent: List<Float>,
    val historicalCyclePoints2020: List<Float>,
    val historicalCyclePoints2016: List<Float>
)

data class CryptoCoin(
    val id: String,
    val symbol: String,
    val name: String,
    val rank: Int,
    val priceUsd: Double,
    val athUsd: Double,
    val athDaysAgo: Int,
    val athDate: String,
    val atlUsd: Double,
    val atlDate: String,
    val change24h: Double,
    val volume24h: Double,
    val marketCap: Double,
    val circulatingSupply: Double,
    val totalSupply: Double,
    val maxSupply: Double?, // null if infinite/inflationary
    val supplyUnit: String,
    val category: CoinCategory,
    val isPro: Boolean = false,
    val isFavorite: Boolean = false,
    val analog: HistoricalAnalog,
    val sparkline: List<Double> = emptyList(),
    // Deep-dive Cycle & Move Analytics
    val whereItMovesNow: String,
    val whereItMovedPast: String,
    val nextPredictedMoveNarrative: String,
    val projectedNextMove1w: String,
    val projectedNextMove2w: String,
    val projectedNextMove4w: String,
    // Whitepaper & Technical Details (Λευκή Βίβλος & Τεχνολογία)
    val genesisDate: String,
    val founderOrCreator: String,
    val consensusMechanism: String,
    val whitepaperSummary: String,
    val technologyDetails: String,
    val tokenomicsDetails: String,
    val useCases: List<String>,
    /** Wall-clock time a live source last wrote this price. 0 means it is still seed data. */
    val priceUpdatedAtMs: Long = 0L,
    val quoteState: QuoteState = QuoteState.PENDING
) {
    /**
     * Derived from [priceUpdatedAtMs] rather than stored, so a price cannot stay flagged as live
     * once the feed behind it has gone quiet.
     */
    val isLivePrice: Boolean
        get() = priceUpdatedAtMs > 0L &&
            (System.currentTimeMillis() - priceUpdatedAtMs) <= LIVE_PRICE_MAX_AGE_MS

    val priceAgeMs: Long
        get() = if (priceUpdatedAtMs <= 0L) Long.MAX_VALUE else System.currentTimeMillis() - priceUpdatedAtMs

    val drawdownPercent: Double
        get() = if (athUsd > 0) ((priceUsd - athUsd) / athUsd) * 100.0 else 0.0

    val calculatedAthDaysAgo: Int
        get() = com.example.util.HalvingCycleUtils.parseAthDaysAgo(athDate, athDaysAgo)

    val circulatingPercentage: Float
        get() {
            val total = maxSupply ?: totalSupply
            return if (total > 0) ((circulatingSupply / total) * 100f).toFloat().coerceIn(0f, 100f) else 100f
        }

    fun displayPrice(currency: Currency, language: AppLanguage? = null): String = when (quoteState) {
        QuoteState.LIVE -> formattedPrice(currency, language)
        QuoteState.UNAVAILABLE -> "Unavailable"
        QuoteState.PENDING -> "Syncing..."
    }

    fun formattedPrice(currency: Currency, language: AppLanguage? = null): String {
        return com.example.util.AppNumberFormatter.formatPrice(
            price = priceUsd,
            currency = currency,
            language = language ?: com.example.util.AppNumberFormatter.currentLanguage
        )
    }

    fun formattedAth(currency: Currency, language: AppLanguage? = null): String {
        return com.example.util.AppNumberFormatter.formatPrice(
            price = athUsd,
            currency = currency,
            language = language ?: com.example.util.AppNumberFormatter.currentLanguage
        )
    }

    fun formattedAtl(currency: Currency, language: AppLanguage? = null): String {
        return com.example.util.AppNumberFormatter.formatPrice(
            price = atlUsd,
            currency = currency,
            language = language ?: com.example.util.AppNumberFormatter.currentLanguage
        )
    }

    fun formattedMarketCap(currency: Currency, language: AppLanguage? = null): String {
        return com.example.util.AppNumberFormatter.formatCompactCurrency(
            amountUsd = marketCap,
            currency = currency,
            language = language ?: com.example.util.AppNumberFormatter.currentLanguage
        )
    }

    fun formattedVolume(currency: Currency, language: AppLanguage? = null): String {
        return com.example.util.AppNumberFormatter.formatCompactCurrency(
            amountUsd = volume24h,
            currency = currency,
            language = language ?: com.example.util.AppNumberFormatter.currentLanguage
        )
    }

    fun formattedFdv(currency: Currency, language: AppLanguage? = null): String {
        val maxOrTotal = maxSupply ?: totalSupply
        val fdv = maxOrTotal * priceUsd
        return com.example.util.AppNumberFormatter.formatCompactCurrency(
            amountUsd = fdv,
            currency = currency,
            language = language ?: com.example.util.AppNumberFormatter.currentLanguage
        )
    }

    fun formattedSupply(amount: Double, language: AppLanguage? = null): String {
        val lang = language ?: com.example.util.AppNumberFormatter.currentLanguage
        val formatted = com.example.util.AppNumberFormatter.formatCompactNumber(amount, lang)
        return "$formatted $supplyUnit"
    }

    fun formattedChange(includePlus: Boolean = true, language: AppLanguage? = null): String {
        return com.example.util.AppNumberFormatter.formatPercent(
            percent = change24h,
            includeSign = includePlus,
            decimals = 2,
            language = language ?: com.example.util.AppNumberFormatter.currentLanguage
        )
    }

    companion object {
        /** Matches PriceTick.isStale so the coin list and the price bus age out together. */
        const val LIVE_PRICE_MAX_AGE_MS = 120_000L

        /**
         * A quiet feed is re-stamped only once per this interval. Re-stamping on every tick would
         * make each copy unequal, so the conflating coins StateFlow would emit continuously and
         * spin the coins -> futures-snapshot -> coins loop.
         */
        const val LIVE_PRICE_RESTAMP_MS = 20_000L
    }
}

data class MacroCycleSignal(
    val halvingDaysPassed: Int = 186,
    val totalCycleDays: Int = 1460, // 4-year cycle (~1460 days)
    val cycleClockPhase: String = "Post-Halving Markup Wave",
    val riskScore: Int = 54, // 1 to 100
    val dominanceBtc: Double = 57.8,
    val fearGreedIndex: Int = 68,
    val fearGreedSentiment: String = "Greed",
    val buyWindowOpen: Boolean = true,
    val sellWindowOpen: Boolean = false,
    val matchingHistoricalDate: String = "October 2020 Analog"
)

enum class WhaleAlertType(val displayName: String, val iconEmoji: String) {
    EXCHANGE_INFLOW("Exchange Inflow (Sell Risk)", "🚨"),
    EXCHANGE_OUTFLOW("Exchange Outflow (Accumulation)", "🟢"),
    WHALE_BUY("Mega Market Buy (Pump Pressure)", "🚀"),
    WHALE_TRANSFER("Whale Wallet Transfer", "🐋")
}

data class WhaleAlert(
    val id: String,
    val timestamp: Long,
    val coinSymbol: String,
    val coinName: String,
    val amountCoin: Double,
    val amountUsd: Double,
    val type: WhaleAlertType,
    val source: String,
    val destination: String,
    val txHash: String,
    val marketImpactVerdict: String,
    val marketImpactVerdictEn: String = "",
    val isImpactCritical: Boolean = false
) {
    fun getLocalizedImpact(isGreek: Boolean): String {
        return if (isGreek) {
            marketImpactVerdict
        } else {
            if (marketImpactVerdictEn.isNotBlank()) marketImpactVerdictEn else marketImpactVerdict
        }
    }

    val formattedUsd: String
        get() = com.example.util.AppNumberFormatter.formatCompactCurrency(amountUsd)

    val formattedAmount: String
        get() = when {
            amountCoin >= 1_000_000.0 -> "${com.example.util.AppNumberFormatter.formatCompactCurrency(amountCoin, currencySymbol = "").trim()} $coinSymbol"
            amountCoin >= 1_000.0 -> "${com.example.util.AppNumberFormatter.formatRawPrice(amountCoin, decimals = 1).removePrefix("$")} $coinSymbol"
            else -> "${com.example.util.AppNumberFormatter.formatRawPrice(amountCoin, decimals = 2).removePrefix("$")} $coinSymbol"
        }
}

data class WhaleAlertSettings(
    val notificationsEnabled: Boolean = false,
    val minThresholdUsd: Double = 50_000_000.0, // Default 50M USD threshold
    val soundAndVibration: Boolean = true,
    val notifyInflows: Boolean = true,
    val notifyOutflows: Boolean = true,
    val notifyMegaBuys: Boolean = true,
    // Cycle & Indicator Push Alerts
    val notifyZoneChange: Boolean = true,
    val notifyPiCycle: Boolean = true,
    val notifyRainbowBand: Boolean = false,
    val notify200wSma: Boolean = false
)

enum class PriceKind {
    SPOT, PERP, MARK, INDEX
}

data class PriceTick(
    val symbol: String, // e.g. "BTCUSDT", "ETHUSDT", "SOLUSDT"
    val venue: String = "Binance",
    val kind: PriceKind = PriceKind.SPOT,
    val price: Double = 0.0,
    val change24h: Double = 0.0,
    val high24h: Double? = null,
    val low24h: Double? = null,
    val volumeQuote: Double? = null,
    val tsMillis: Long = System.currentTimeMillis(),
    val source: String = "https://api.binance.com"
) {
    val isStale: Boolean get() = tsMillis <= 0L || (System.currentTimeMillis() - tsMillis) > 120_000L
    val isDisconnected: Boolean get() = (System.currentTimeMillis() - tsMillis) > 90_000L
    val ageSeconds: Long get() = ((System.currentTimeMillis() - tsMillis) / 1000L).coerceAtLeast(0L)
}

data class PriceBusState(
    val btcSpot: PriceTick = PriceTick("BTCUSDT", "Binance", PriceKind.SPOT, 0.0, 0.0, tsMillis = 0L, source = "https://api.binance.com"),
    val btcPerp: PriceTick = PriceTick("BTCUSDT", "Binance Futures", PriceKind.PERP, 0.0, 0.0, tsMillis = 0L, source = "https://fapi.binance.com"),
    val btcMark: PriceTick = PriceTick("BTCUSDT", "Binance Futures", PriceKind.MARK, 0.0, 0.0, tsMillis = 0L, source = "https://fapi.binance.com"),
    val ethSpot: PriceTick = PriceTick("ETHUSDT", "Binance", PriceKind.SPOT, 0.0, 0.0, tsMillis = 0L, source = "https://api.binance.com"),
    val ethPerp: PriceTick = PriceTick("ETHUSDT", "Binance Futures", PriceKind.PERP, 0.0, 0.0, tsMillis = 0L, source = "https://fapi.binance.com"),
    val solSpot: PriceTick = PriceTick("SOLUSDT", "Binance", PriceKind.SPOT, 0.0, 0.0, tsMillis = 0L, source = "https://api.binance.com"),
    val liveTicks: Map<String, PriceTick> = emptyMap(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
) {
    fun getTickForSymbol(symbol: String): PriceTick? {
        val clean = symbol.uppercase().removeSuffix("USDT").removePrefix("1000000").removePrefix("1000")
        return liveTicks[clean] ?: when (clean) {
            "BTC" -> if (btcPerp.price > 0.0) btcPerp else btcSpot
            "ETH" -> if (ethPerp.price > 0.0) ethPerp else ethSpot
            "SOL" -> solSpot
            else -> null
        }
    }
}

data class CentralizedPriceState(
    val btcSpotPrice: Double = 0.0,
    val btcPerpPrice: Double = 0.0,
    val btcSpotChange24h: Double = 0.0,
    val btcPerpChange24h: Double = 0.0,
    val lastUpdatedTimestamp: Long = 0L,
    val isLiveConnected: Boolean = false,
    val primarySource: String = "Binance",
    val priceBus: PriceBusState = PriceBusState()
) {
    val spreadBasisUsd: Double get() = btcPerpPrice - btcSpotPrice
    val spreadBasisPercent: Double get() = if (btcSpotPrice > 0) ((btcPerpPrice - btcSpotPrice) / btcSpotPrice) * 100.0 else 0.0
}




