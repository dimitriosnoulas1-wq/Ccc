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
    val openInterestUsd: Double = 0.0
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
        coinFallback: CryptoCoin? = null
    ): MarketIntelligenceReport {
        val (catalogBase, contractDivisor) = com.example.data.network.SymbolMath.canonical(symbol)
        val exchangePrice = when {
            ticker?.fromExchange == true && (ticker.lastPrice ?: 0.0) > 0.0 -> (ticker.lastPrice ?: 0.0) / contractDivisor
            markFunding?.fromExchange == true && (markFunding.markPrice ?: 0.0) > 0.0 -> (markFunding.markPrice ?: 0.0) / contractDivisor
            coinFallback?.quoteState == QuoteState.LIVE && coinFallback.priceUsd > 0.0 -> coinFallback.priceUsd
            else -> 0.0
        }
        val price = exchangePrice
        val change24h: Double = when {
            ticker?.fromExchange == true && ticker.priceChangePercent24h != null -> ticker.priceChangePercent24h ?: 0.0
            coinFallback?.quoteState == QuoteState.LIVE -> coinFallback.change24h
            else -> 0.0
        }
        val fundingObserved: Double? = markFunding?.takeIf { it.fromExchange }?.fundingRate
        val fundingRate: Double = fundingObserved ?: 0.0
        val oiObserved: Double? = openInterest?.openInterestUsd?.takeIf { openInterest.isAvailable && it > 0.0 }
        val oiUsd: Double = oiObserved ?: 0.0
        val bidQty = bookTicker?.bidQty ?: 1.0
        val askQty = bookTicker?.askQty ?: 1.0
        val bidRatio = (bidQty / (bidQty + askQty)).coerceIn(0.05, 0.95)

        val buyTrades = recentTrades.count { !it.isBuyerMaker }
        val sellTrades = recentTrades.count { it.isBuyerMaker }
        val buyRatio = if (buyTrades + sellTrades > 0) buyTrades.toDouble() / (buyTrades + sellTrades) else 0.5

        val symbolLiqs = recentLiquidations.filter { order ->
            val (orderBase, _) = com.example.data.network.SymbolMath.canonical(order.symbol)
            orderBase == catalogBase
        }
        val longLiqs = symbolLiqs.filter { it.isLongLiquidated }.sumOf { it.valueUsd }
        val shortLiqs = symbolLiqs.filter { !it.isLongLiquidated }.sumOf { it.valueUsd }
        val hasLiquidationPrints = symbolLiqs.isNotEmpty()

        val fearGreed = macroSentiment.fearAndGreedValue ?: 55

        // Determine Market Drivers
        // 1. Spot Demand
        val spotImpact = when {
            bidRatio > 0.62 || buyRatio > 0.62 -> DriverImpact.HIGH
            bidRatio < 0.38 || buyRatio < 0.38 -> DriverImpact.LOW
            else -> DriverImpact.MEDIUM
        }
        val spotDetail = when (spotImpact) {
            DriverImpact.HIGH -> "Bids absorbing ask liquidity (${(bidRatio * 100).toInt()}% depth)"
            DriverImpact.LOW -> "Ask inventory dominating order book"
            else -> "Balanced spot book participation"
        }

        // 2. Open Interest
        val oiImpact = when {
            oiUsd > 100_000_000 -> DriverImpact.HIGH
            oiUsd > 20_000_000 -> DriverImpact.MEDIUM
            else -> DriverImpact.LOW
        }
        val oiDetail = when (oiImpact) {
            DriverImpact.HIGH -> "Elevated institutional leverage positions active"
            DriverImpact.MEDIUM -> "Steady perpetual open interest baseline"
            else -> "Moderate open interest participation"
        }

        // 3. Funding Skew
        val fundingImpact = when {
            Math.abs(fundingRate) >= 0.0002 -> DriverImpact.HIGH
            Math.abs(fundingRate) >= 0.00008 -> DriverImpact.MEDIUM
            else -> DriverImpact.LOW
        }
        val fundingDetail = when {
            fundingRate > 0.00015 -> "Longs paying premium (+${"%.4f".format(Locale.US, fundingRate * 100)}%)"
            fundingRate < -0.00005 -> "Shorts paying longs (${"%.4f".format(Locale.US, fundingRate * 100)}%)"
            else -> "Neutral baseline equilibrium (+${"%.4f".format(Locale.US, fundingRate * 100)}%)"
        }

        // 4. Liquidations
        val liqImpact = when {
            shortLiqs > 50_000 || longLiqs > 50_000 -> DriverImpact.HIGH
            shortLiqs > 10_000 || longLiqs > 10_000 -> DriverImpact.MEDIUM
            else -> DriverImpact.LOW
        }
        val liqDetail = when {
            shortLiqs > longLiqs && shortLiqs > 10_000 -> "Short stops swept into market buys"
            longLiqs > shortLiqs && longLiqs > 10_000 -> "Long margin calls triggering forced sells"
            else -> "No major cascading liquidations detected"
        }

        // 5. Macro
        val macroImpact = when {
            fearGreed >= 70 || fearGreed <= 30 -> DriverImpact.HIGH
            else -> DriverImpact.LOW
        }
        val macroDetail = "F&G Index at $fearGreed (${macroSentiment.fearAndGreedClassification ?: "Neutral"})"

        val drivers = listOf(
            MarketDriver("Spot Demand", spotImpact, spotDetail, isBullish = spotImpact == DriverImpact.HIGH),
            MarketDriver("Open Interest", oiImpact, oiDetail, isBullish = change24h >= 0),
            MarketDriver("Funding Skew", fundingImpact, fundingDetail, isBullish = fundingRate >= 0),
            MarketDriver("Liquidations", liqImpact, liqDetail, isBullish = shortLiqs >= longLiqs),
            MarketDriver("Macro Regime", macroImpact, macroDetail, isBullish = fearGreed >= 50)
        )

        // Regime Classification
        val regime: MarketRegimeState
        val interpretationEn: String
        val interpretationGr: String
        val cascadeRiskEn: String
        val cascadeRiskGr: String
        val invalidationEn: String
        val invalidationGr: String
        val confidence: Int

        when {
            change24h > 1.2 && fundingRate > 0.00012 && oiImpact == DriverImpact.HIGH -> {
                regime = MarketRegimeState.LEVERAGE_EXPANSION
                confidence = 82
                interpretationEn = "Long positioning is increasing aggressively alongside derivatives leverage. Buy orders are leading, but positions are becoming crowded."
                interpretationGr = "Οι θέσεις Long αυξάνονται επιθετικά παράλληλα με τη μόχλευση παραγώγων. Οι αγορές κυριαρχούν, αλλά η αγορά γίνεται υπερφορτωμένη."
                cascadeRiskEn = "Above-average long cascade risk if spot bid absorption falters at resistance."
                cascadeRiskGr = "Αυξημένος κίνδυνος εκκαθάρισης Long εάν η απορρόφηση των αγοραστών εξασθενήσει."
                val invPrice = "%.2f".format(Locale.US, price * 0.982)
                invalidationEn = "Slipping below $$invPrice on contracting Open Interest."
                invalidationGr = "Πτώση κάτω από $$invPrice με συρρίκνωση του Ανοικτού Ενδιαφέροντος (OI)."
            }
            change24h > 0.5 && fundingRate <= 0.00012 && spotImpact == DriverImpact.HIGH -> {
                regime = MarketRegimeState.SPOT_ACCUMULATION
                confidence = 86
                interpretationEn = "Spot buyers absorbing available liquidity without over-leveraging perpetual futures. Organic accumulation signature."
                interpretationGr = "Οι αγοραστές spot απορροφούν τη διαθέσιμη ρευστότητα χωρίς υπερβολική μόχλευση. Οργανική συσσώρευση."
                cascadeRiskEn = "Low systemic leverage risk; shallow pullbacks expected to find strong bid support."
                cascadeRiskGr = "Χαμηλός συστημικός κίνδυνος μόχλευσης. Οι διορθώσεις αναμένεται να βρουν ισχυρή στήριξη."
                val invPrice = "%.2f".format(Locale.US, price * 0.975)
                invalidationEn = "Loss of $$invPrice with heavy spot selling walls."
                invalidationGr = "Απώλεια των $$invPrice με έντονους τοίχους πώλησης spot."
            }
            change24h > 1.5 && shortLiqs > 25_000 -> {
                regime = MarketRegimeState.SHORT_SQUEEZE
                confidence = 79
                interpretationEn = "Forced short liquidation orders accelerating upside momentum into overhead resting liquidity."
                interpretationGr = "Αναγκαστικές ρευστοποιήσεις Short επιταχύνουν την ανοδική ορμή προς τη ρευστότητα κορυφής."
                cascadeRiskEn = "Exhaustion risk once trapped shorts are cleared; watch for local blow-off wick."
                cascadeRiskGr = "Κίνδυνος εξάντλησης μόλις εκκαθαριστούν τα παγιδευμένα shorts. Προσοχή σε τοπικό blow-off wick."
                val invPrice = "%.2f".format(Locale.US, price * 0.985)
                invalidationEn = "Rapid volume fade and rejection below $$invPrice."
                invalidationGr = "Ταχεία υποχώρηση όγκου και απόρριψη κάτω από $$invPrice."
            }
            change24h < -1.2 && longLiqs > 25_000 -> {
                regime = MarketRegimeState.LONG_CASCADE
                confidence = 84
                interpretationEn = "Long margin positions getting flushed. Cascading market sell orders triggering localized stop hunts."
                interpretationGr = "Εκκαθάριση θέσεων Long με μόχλευση. Αλλεπάλληλες εντολές market sell προκαλούν τοπικό stop hunt."
                cascadeRiskEn = "High downside volatility until major liquidation cluster is fully absorbed."
                cascadeRiskGr = "Υψηλή μεταβλητότητα πτώσης μέχρι να απορροφηθεί πλήρως το μεγάλο pool ρευστοποιήσεων."
                val invPrice = "%.2f".format(Locale.US, price * 1.018)
                invalidationEn = "Reclaiming $$invPrice with aggressive spot bid book replenishment."
                invalidationGr = "Ανάκτηση των $$invPrice με επιθετική επαναφόρτωση του βιβλίου εντολών αγοράς."
            }
            Math.abs(change24h) < 0.8 && (bookTicker?.spreadPercent ?: 0.0) < 0.0005 -> {
                regime = MarketRegimeState.RANGE_COMPRESSION
                confidence = 74
                interpretationEn = "Volatility compressing within tight boundaries. Liquidity accumulating on both sides of the book."
                interpretationGr = "Συμπίεση μεταβλητότητας σε στενό εύρος. Ρευστότητα συσσωρεύεται και στις δύο πλευρές του βιβλίου."
                cascadeRiskEn = "Pending expansion spike once the local range boundaries are swept."
                cascadeRiskGr = "Επικείμενη εκρηκτική εκτόνωση μόλις εκκαθαριστούν τα τοπικά άκρα του εύρους."
                val highR = "%.2f".format(Locale.US, price * 1.012)
                val lowR = "%.2f".format(Locale.US, price * 0.988)
                invalidationEn = "Decisive breakout and candle close outside $$lowR - $$highR."
                invalidationGr = "Αποφασιστική διάσπαση και κλείσιμο έξω από $$lowR - $$highR."
            }
            else -> {
                regime = MarketRegimeState.CHOPPY_NEUTRAL
                confidence = 70
                interpretationEn = "Mixed signals across spot flow and perpetual funding. Market absorbing two-way order flow."
                interpretationGr = "Ανάμεικτα σήματα σε spot και funding perpetuals. Η αγορά απορροφά αμφίδρομη ροή εντολών."
                cascadeRiskEn = "Moderate chop risk; false breakout wicks common in this regime."
                cascadeRiskGr = "Μέτριος κίνδυνος whipsaw. Συχνά ψευδή ξεσπάσματα σε αυτό το καθεστώς."
                val invPrice = "%.2f".format(Locale.US, price * 0.980)
                invalidationEn = "Sustained directional impulse beyond 1.5% from $$invPrice."
                invalidationGr = "Συνεχής κατευθυντήρια κίνηση άνω του 1.5% από τα $$invPrice."
            }
        }

        return MarketIntelligenceReport(
            symbol = symbol,
            regime = regime,
            priceChange24h = change24h,
            confidencePercent = liveConfidence(
                change24h = change24h,
                fundingRate = fundingObserved,
                openInterestUsd = oiObserved,
                bidRatio = if (bookTicker?.fromExchange == true) bidRatio else null,
                buyRatio = if (recentTrades.isNotEmpty()) buyRatio else null,
                shortLiqsUsd = shortLiqs,
                longLiqsUsd = longLiqs,
                hasLiquidationPrints = hasLiquidationPrints
            ),
            drivers = drivers,
            interpretationEn = interpretationEn,
            interpretationGr = interpretationGr,
            cascadeRiskEn = cascadeRiskEn,
            cascadeRiskGr = cascadeRiskGr,
            invalidationLevelEn = invalidationEn,
            invalidationLevelGr = invalidationGr,
            currentPrice = price,
            fundingRate = fundingRate,
            openInterestUsd = oiUsd
        )
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
