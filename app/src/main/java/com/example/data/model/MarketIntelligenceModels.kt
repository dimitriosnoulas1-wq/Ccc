package com.example.data.model

import java.util.Locale

enum class MarketRegimeState(val labelEn: String, val labelGr: String) {
    LEVERAGE_EXPANSION("Leverage Building", "Συσσώρευση Μόχλευσης"),
    SPOT_ACCUMULATION("Spot Accumulation", "Συσσώρευση Spot"),
    SHORT_SQUEEZE("Short Squeeze Pressure", "Πίεση Short Squeeze"),
    LONG_CASCADE("Long Liquidation Flush", "Εκκαθάριση Longs"),
    RANGE_COMPRESSION("Volatility Compression", "Συμπίεση Μεταβλητότητας"),
    CHOPPY_NEUTRAL("Balanced Consolidation", "Ισορροπημένη Συσσώρευση")
}

enum class DriverImpact(val labelEn: String, val labelGr: String) {
    HIGH("HIGH", "ΥΨΗΛΟ"),
    MEDIUM("MEDIUM", "ΜΕΣΑΙΟ"),
    LOW("LOW", "ΧΑΜΗΛΟ"),
    ABSORPTION("ABSORPTION", "ΑΠΟΡΡΟΦΗΣΗ")
}

data class MarketDriver(
    val title: String,
    val impact: DriverImpact,
    val detail: String,
    val isBullish: Boolean? = null
)

data class MarketIntelligenceReport(
    val symbol: String = "BTCUSDT",
    val regime: MarketRegimeState = MarketRegimeState.CHOPPY_NEUTRAL,
    val priceChange24h: Double = 0.0,
    val confidencePercent: Int = 70,
    val drivers: List<MarketDriver> = emptyList(),
    val interpretationEn: String = "Analyzing real-time derivatives order flow and funding rates...",
    val interpretationGr: String = "Ανάλυση πραγματικού χρόνου ροής παραγώγων και funding rates...",
    val cascadeRiskEn: String = "Low systemic leverage risk detected.",
    val cascadeRiskGr: String = "Χαμηλός συστημικός κίνδυνος μόχλευσης.",
    val invalidationLevelEn: String = "Waiting for live boundary confirmation.",
    val invalidationLevelGr: String = "Αναμονή για επιβεβαίωση ορίων.",
    val currentPrice: Double = 0.0,
    val fundingRate: Double = 0.0,
    val openInterestUsd: Double = 0.0,
    val hasLivePrice: Boolean = false,
    val hasLiveChange24h: Boolean = false,
    val hasLiveFunding: Boolean = false,
    val hasLiveOpenInterest: Boolean = false,
    val derivativesScore: Double? = null,
    val derivativesFreshness: DerivativesFreshness = DerivativesFreshness.UNAVAILABLE,
    val derivativesAsOfMs: Long = 0L,
    val sourceLabel: String = ""
)

data class LiquidityCluster(
    val price: Double,
    val volumeUsdMillions: Double,
    val isShortPool: Boolean, // true = short liquidations (above price), false = long liquidations (below price)
    val percentageDistance: Double
)

data class AggregatedLiquidationMap(
    val currentPrice: Double,
    val shortClusters: List<LiquidityCluster>,
    val longClusters: List<LiquidityCluster>,
    val totalShortLiquidityM: Double,
    val totalLongLiquidityM: Double,
    val dominantSideInsightEn: String,
    val dominantSideInsightGr: String,
    val symbol: String = "BTCUSDT"
)

object MarketIntelligenceEngine {

    fun analyze(
        symbol: String,
        ticker: FuturesTickerData?,
        bookTicker: FuturesBookTicker?,
        markFunding: FuturesMarkFunding?,
        openInterest: FuturesOpenInterest?,
        recentTrades: List<FuturesTrade>,
        recentLiquidations: List<FuturesLiquidationOrder>,
        macroSentiment: MacroMarketSentiment,
        coinFallback: CryptoCoin? = null,
        derivatives: AggregatedDerivativesSnapshot? = null
    ): MarketIntelligenceReport {
        val (catalogBase, contractDivisor) = com.example.data.network.SymbolMath.canonical(symbol)
        val agg = derivatives?.aggregated
        val exchangePrice = when {
            ticker?.fromExchange == true && (ticker.lastPrice ?: 0.0) > 0.0 -> (ticker.lastPrice ?: 0.0) / contractDivisor
            markFunding?.fromExchange == true && (markFunding.markPrice ?: 0.0) > 0.0 -> (markFunding.markPrice ?: 0.0) / contractDivisor
            coinFallback?.quoteState == QuoteState.LIVE && coinFallback.priceUsd > 0.0 -> coinFallback.priceUsd
            (agg?.markPrice ?: 0.0) > 0.0 -> agg?.markPrice ?: 0.0
            (agg?.lastPrice ?: 0.0) > 0.0 -> agg?.lastPrice ?: 0.0
            else -> 0.0
        }
        val price = exchangePrice
        val changeObserved: Double? = when {
            ticker?.fromExchange == true && ticker.priceChangePercent24h != null -> ticker.priceChangePercent24h
            coinFallback?.quoteState == QuoteState.LIVE -> coinFallback.change24h
            agg?.change24hPct != null -> agg.change24hPct
            else -> null
        }
        val change24h: Double = changeObserved ?: 0.0
        val fundingObserved: Double? = agg?.fundingRate
            ?: markFunding?.takeIf { it.fromExchange }?.fundingRate
        val fundingRate: Double = fundingObserved ?: 0.0
        val oiObserved: Double? = agg?.openInterestUsd?.takeIf { it > 0.0 }
            ?: openInterest?.openInterestUsd?.takeIf { openInterest.isAvailable && it > 0.0 }
        val oiUsd: Double = oiObserved ?: 0.0
        val liveBook = bookTicker?.takeIf { it.fromExchange }
        val bidQty = liveBook?.bidQty
        val askQty = liveBook?.askQty
        val bidRatio = if (bidQty != null && askQty != null && bidQty + askQty > 0.0) {
            (bidQty / (bidQty + askQty)).coerceIn(0.05, 0.95)
        } else {
            null
        }

        val buyTrades = recentTrades.count { !it.isBuyerMaker }
        val sellTrades = recentTrades.count { it.isBuyerMaker }
        val takerRatio = agg?.takerBuySellRatio
        val buyRatio = when {
            buyTrades + sellTrades > 0 -> buyTrades.toDouble() / (buyTrades + sellTrades)
            takerRatio != null && takerRatio > 0.0 -> takerRatio / (takerRatio + 1.0)
            else -> 0.5
        }

        val symbolLiqs = recentLiquidations.filter { order ->
            val (orderBase, _) = com.example.data.network.SymbolMath.canonical(order.symbol)
            orderBase == catalogBase
        }
        val longLiqs = symbolLiqs.filter { it.isLongLiquidated }.sumOf { it.valueUsd } + (agg?.longLiqUsd ?: 0.0)
        val shortLiqs = symbolLiqs.filter { !it.isLongLiquidated }.sumOf { it.valueUsd } + (agg?.shortLiqUsd ?: 0.0)
        val hasLiquidationPrints = symbolLiqs.isNotEmpty() ||
            ((agg?.longLiqUsd ?: 0.0) + (agg?.shortLiqUsd ?: 0.0)) > 0.0

        val fearGreed = macroSentiment.fearAndGreedValue

        // Determine Market Drivers from live prints only
        val spotImpact = when {
            bidRatio == null && buyTrades + sellTrades == 0 && takerRatio == null -> DriverImpact.LOW
            (bidRatio != null && bidRatio > 0.62) || buyRatio > 0.62 -> DriverImpact.HIGH
            (bidRatio != null && bidRatio < 0.38) || buyRatio < 0.38 -> DriverImpact.LOW
            else -> DriverImpact.MEDIUM
        }
        val spotDetail = when {
            bidRatio == null && buyTrades + sellTrades == 0 && takerRatio == null ->
                "Waiting for live book or prints"
            bidRatio != null ->
                "Book bid share ${(bidRatio * 100).toInt()}%"
            else ->
                "Tape buy share ${(buyRatio * 100).toInt()}%"
        }

        // 2. Open Interest
        val oiImpact = when {
            oiUsd > 100_000_000 -> DriverImpact.HIGH
            oiUsd > 20_000_000 -> DriverImpact.MEDIUM
            else -> DriverImpact.LOW
        }
        val venueNote = derivatives?.sourceLabel?.takeIf { it.isNotBlank() }?.let { " via $it" } ?: ""
        val oiDetail = when {
            oiObserved == null -> "Waiting for live open interest"
            oiImpact == DriverImpact.HIGH -> "Elevated aggregated perpetual open interest$venueNote"
            oiImpact == DriverImpact.MEDIUM -> "Steady aggregated perpetual open interest$venueNote"
            else -> "Moderate open interest participation$venueNote"
        }

        // 3. Funding Skew
        val fundingImpact = when {
            Math.abs(fundingRate) >= 0.0002 -> DriverImpact.HIGH
            Math.abs(fundingRate) >= 0.00008 -> DriverImpact.MEDIUM
            else -> DriverImpact.LOW
        }
        val fundingDetail = when {
            fundingObserved == null -> "Waiting for live funding"
            fundingRate > 0.00015 -> "Longs paying ${"%.4f".format(Locale.US, fundingRate * 100)}%"
            fundingRate < -0.00005 -> "Shorts paying ${"%.4f".format(Locale.US, fundingRate * 100)}%"
            else -> "Funding ${"%.4f".format(Locale.US, fundingRate * 100)}%"
        }

        // 4. Liquidations
        val liqImpact = when {
            shortLiqs > 50_000 || longLiqs > 50_000 -> DriverImpact.HIGH
            shortLiqs > 10_000 || longLiqs > 10_000 -> DriverImpact.MEDIUM
            else -> DriverImpact.LOW
        }
        val liqDetail = when {
            !hasLiquidationPrints -> "Waiting for live liquidation prints"
            else -> "Long prints ${compactUsd(longLiqs)} · Short prints ${compactUsd(shortLiqs)}"
        }

        val macroImpact = when {
            fearGreed == null -> DriverImpact.LOW
            fearGreed >= 70 || fearGreed <= 30 -> DriverImpact.HIGH
            else -> DriverImpact.LOW
        }
        val macroDetail = if (fearGreed != null) {
            "F&G $fearGreed (${macroSentiment.fearAndGreedClassification ?: "—"})"
        } else {
            "Fear & Greed offline"
        }

        val drivers = listOf(
            MarketDriver("Spot book / prints", spotImpact, spotDetail, isBullish = null),
            MarketDriver("Open Interest", oiImpact, oiDetail, isBullish = null),
            MarketDriver("Funding", fundingImpact, fundingDetail, isBullish = null),
            MarketDriver("Liquidations", liqImpact, liqDetail, isBullish = null),
            MarketDriver("Fear & Greed", macroImpact, macroDetail, isBullish = null)
        )

        val regime = when {
            changeObserved != null && changeObserved > 1.2 && fundingObserved != null && fundingObserved > 0.00012 && oiImpact == DriverImpact.HIGH ->
                MarketRegimeState.LEVERAGE_EXPANSION
            changeObserved != null && changeObserved > 0.5 && (fundingObserved == null || fundingObserved <= 0.00012) && spotImpact == DriverImpact.HIGH ->
                MarketRegimeState.SPOT_ACCUMULATION
            changeObserved != null && changeObserved > 1.5 && shortLiqs > 25_000 ->
                MarketRegimeState.SHORT_SQUEEZE
            changeObserved != null && changeObserved < -1.2 && longLiqs > 25_000 ->
                MarketRegimeState.LONG_CASCADE
            changeObserved != null && Math.abs(changeObserved) < 0.8 &&
                (liveBook?.spreadPercent?.let { it < 0.0005 } == true) ->
                MarketRegimeState.RANGE_COMPRESSION
            else -> MarketRegimeState.CHOPPY_NEUTRAL
        }

        val interpretationEn = tapeFacts(
            greek = false,
            price = price,
            changeObserved = changeObserved,
            fundingObserved = fundingObserved,
            oiObserved = oiObserved,
            longLiqs = longLiqs,
            shortLiqs = shortLiqs,
            hasLiquidationPrints = hasLiquidationPrints
        )
        val interpretationGr = tapeFacts(
            greek = true,
            price = price,
            changeObserved = changeObserved,
            fundingObserved = fundingObserved,
            oiObserved = oiObserved,
            longLiqs = longLiqs,
            shortLiqs = shortLiqs,
            hasLiquidationPrints = hasLiquidationPrints
        )
        val cascadeRiskEn = if (hasLiquidationPrints) {
            "Long prints ${compactUsd(longLiqs)} vs short prints ${compactUsd(shortLiqs)}."
        } else {
            "Waiting for live liquidation prints."
        }
        val cascadeRiskGr = if (hasLiquidationPrints) {
            "Εκκαθαρίσεις Long ${compactUsd(longLiqs)} έναντι Short ${compactUsd(shortLiqs)}."
        } else {
            "Αναμονή για ζωντανές εκκαθαρίσεις."
        }
        val invalidationEn = "No invented invalidation. Tape only."
        val invalidationGr = "Χωρίς εφευρεμένο επίπεδο ακύρωσης. Μόνο η ταινία."

        val score = derivatives?.score?.value
        val scoreNoteEn = if (score != null) " Aggregated perps score ${"%.0f".format(Locale.US, score)}." else ""
        val scoreNoteGr = if (score != null) " Συγκεντρωτικό σκορ perpetuals ${"%.0f".format(Locale.US, score)}." else ""

        return MarketIntelligenceReport(
            symbol = symbol,
            regime = regime,
            priceChange24h = change24h,
            confidencePercent = liveConfidence(
                change24h = change24h,
                fundingRate = fundingObserved,
                openInterestUsd = oiObserved,
                bidRatio = bidRatio,
                buyRatio = if (recentTrades.isNotEmpty() || takerRatio != null) buyRatio else null,
                shortLiqsUsd = shortLiqs,
                longLiqsUsd = longLiqs,
                hasLiquidationPrints = hasLiquidationPrints
            ),
            drivers = drivers,
            interpretationEn = interpretationEn + scoreNoteEn,
            interpretationGr = interpretationGr + scoreNoteGr,
            cascadeRiskEn = cascadeRiskEn,
            cascadeRiskGr = cascadeRiskGr,
            invalidationLevelEn = invalidationEn,
            invalidationLevelGr = invalidationGr,
            currentPrice = price,
            fundingRate = fundingRate,
            openInterestUsd = oiUsd,
            hasLivePrice = price > 0.0,
            hasLiveChange24h = changeObserved != null,
            hasLiveFunding = fundingObserved != null,
            hasLiveOpenInterest = oiObserved != null,
            derivativesScore = score,
            derivativesFreshness = derivatives?.freshness ?: DerivativesFreshness.UNAVAILABLE,
            derivativesAsOfMs = derivatives?.asOfMs ?: 0L,
            sourceLabel = derivatives?.sourceLabel.orEmpty()
        )
    }

    private fun compactUsd(value: Double): String {
        if (value <= 0.0) return "—"
        return if (value >= 1_000_000.0) {
            "$" + "%.1f".format(Locale.US, value / 1_000_000.0) + "M"
        } else if (value >= 1_000.0) {
            "$" + "%.1f".format(Locale.US, value / 1_000.0) + "K"
        } else {
            "$" + "%.0f".format(Locale.US, value)
        }
    }

    private fun tapeFacts(
        greek: Boolean,
        price: Double,
        changeObserved: Double?,
        fundingObserved: Double?,
        oiObserved: Double?,
        longLiqs: Double,
        shortLiqs: Double,
        hasLiquidationPrints: Boolean
    ): String {
        val parts = mutableListOf<String>()
        if (price > 0.0) {
            parts += if (greek) "Τελευταία $${"%.2f".format(Locale.US, price)}" else "Last $${"%.2f".format(Locale.US, price)}"
        }
        if (changeObserved != null) {
            parts += "24h ${"%+.2f".format(Locale.US, changeObserved)}%"
        }
        if (fundingObserved != null) {
            parts += if (greek) {
                "Funding ${"%.4f".format(Locale.US, fundingObserved * 100)}%"
            } else {
                "Funding ${"%.4f".format(Locale.US, fundingObserved * 100)}%"
            }
        }
        if (oiObserved != null) {
            parts += if (greek) "OI ${compactUsd(oiObserved)}" else "OI ${compactUsd(oiObserved)}"
        }
        if (hasLiquidationPrints) {
            parts += if (greek) {
                "Long ${compactUsd(longLiqs)} · Short ${compactUsd(shortLiqs)}"
            } else {
                "Long liqs ${compactUsd(longLiqs)} · Short liqs ${compactUsd(shortLiqs)}"
            }
        }
        if (parts.isEmpty()) {
            return if (greek) "Αναμονή για ζωντανή ταινία παραγώγων." else "Waiting for the live derivatives tape."
        }
        return parts.joinToString(". ") + "."
    }

    /**
     * 0 means the derivatives inputs have not arrived. Otherwise the score is the agreement of the
     * live inputs that actually exist for this symbol, so two markets cannot share one constant.
     */
    private fun liveConfidence(
        change24h: Double,
        fundingRate: Double?,
        openInterestUsd: Double?,
        bidRatio: Double?,
        buyRatio: Double?,
        shortLiqsUsd: Double,
        longLiqsUsd: Double,
        hasLiquidationPrints: Boolean
    ): Int {
        val hasAnything = fundingRate != null || openInterestUsd != null || bidRatio != null ||
            buyRatio != null || hasLiquidationPrints || change24h != 0.0
        if (!hasAnything) return 0
        var weighted = 0.0
        var weight = 0.0
        fun add(w: Double, signal: Double) {
            weighted += w * signal.coerceIn(0.0, 1.0)
            weight += w
        }
        add(1.3, kotlin.math.abs(change24h) / 6.0)
        if (fundingRate != null) add(1.6, kotlin.math.abs(fundingRate) / 0.0006)
        if (openInterestUsd != null) {
            val oiSignal = (kotlin.math.ln(openInterestUsd.coerceAtLeast(1.0)) / kotlin.math.ln(2_000_000_000.0))
            add(1.1, oiSignal)
        }
        if (bidRatio != null) add(0.9, kotlin.math.abs(bidRatio - 0.5) * 2.0)
        if (buyRatio != null) add(0.9, kotlin.math.abs(buyRatio - 0.5) * 2.0)
        if (hasLiquidationPrints) {
            val total = (shortLiqsUsd + longLiqsUsd).coerceAtLeast(1.0)
            val skew = kotlin.math.abs(shortLiqsUsd - longLiqsUsd) / total
            val size = (total / 2_000_000.0).coerceIn(0.0, 1.0)
            add(1.4, (0.45 * skew) + (0.55 * size))
        }
        if (weight <= 0.0) return 0
        return (38.0 + 56.0 * (weighted / weight)).toInt().coerceIn(15, 93)
    }

    fun computeLiquidationMap(
        symbol: String,
        currentPrice: Double,
        openInterestUsd: Double
    ): AggregatedLiquidationMap {
        if (currentPrice <= 0.0) {
            return AggregatedLiquidationMap(
                currentPrice = 0.0,
                shortClusters = emptyList(),
                longClusters = emptyList(),
                totalShortLiquidityM = 0.0,
                totalLongLiquidityM = 0.0,
                dominantSideInsightEn = "Waiting for live liquidation prints for this market.",
                dominantSideInsightGr = "Αναμονή για ζωντανές εκκαθαρίσεις αυτής της αγοράς.",
                symbol = symbol
            )
        }
        return AggregatedLiquidationMap(
            currentPrice = currentPrice,
            shortClusters = emptyList(),
            longClusters = emptyList(),
            totalShortLiquidityM = 0.0,
            totalLongLiquidityM = 0.0,
            dominantSideInsightEn = if (openInterestUsd > 0.0) {
                "Open interest is live from the exchange. Cluster sizes are omitted until liquidation prints arrive for this symbol."
            } else {
                "Waiting for live open interest and liquidation prints for this symbol."
            },
            dominantSideInsightGr = "Χωρίς εκτιμώμενα pools ρευστοποίησης. Το ανοιχτό ενδιαφέρον έρχεται από το exchange. Τα clusters εμφανίζονται μόνο από πραγματικές εκκαθαρίσεις.",
            symbol = symbol
        )
    }
}
