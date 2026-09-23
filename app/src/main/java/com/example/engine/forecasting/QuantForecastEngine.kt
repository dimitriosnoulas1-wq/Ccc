package com.example.engine.forecasting

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class MarketRegime(val label: String, val isBullish: Boolean) {
    STRONG_BULL("STRONG BULL ACCUMULATION", true),
    BULL("BULL EXPANSION", true),
    NEUTRAL("NEUTRAL CONSOLIDATION", true),
    BEAR("BEAR COMPRESSION", false),
    STRONG_BEAR("STRONG BEAR DISTRIBUTION", false),
    INSUFFICIENT_DATA("INSUFFICIENT DATA", false)
}

enum class ForecastDirection {
    BULLISH,
    BEARISH,
    NEUTRAL,
    UNKNOWN
}

data class ProbabilityScenario(
    val bullPct: Int,
    val basePct: Int,
    val bearPct: Int
)

data class ForecastCardModel(
    val asset: String,
    val currentPrice: Double,
    val regime: MarketRegime,
    val direction: ForecastDirection,
    val compositeScore: Int,
    val probabilities: ProbabilityScenario,
    val confidencePct: Int,
    val keySupport: Double,
    val keyResistance: Double,
    val invalidationLevel: Double,
    val simpleExplanation: String,
    val technicalEvidences: List<String>,
    val riskWarning: String,
    val dataQualityScore: String = "Illustrative model",
    val modelVersion: String = "v105-QuantEngine-STABLE"
)

object QuantForecastEngine {

    fun calculateRsi(prices: List<Double>, period: Int = 14): Double {
        if (prices.size <= period) return 50.0
        var gains = 0.0
        var losses = 0.0
        for (i in 1..period) {
            val diff = prices[i] - prices[i - 1]
            if (diff >= 0) gains += diff else losses += abs(diff)
        }
        var avgGain = gains / period
        var avgLoss = losses / period
        if (avgLoss == 0.0) return 100.0

        for (i in (period + 1) until prices.size) {
            val diff = prices[i] - prices[i - 1]
            if (diff >= 0) {
                avgGain = (avgGain * (period - 1) + diff) / period
                avgLoss = (avgLoss * (period - 1)) / period
            } else {
                avgGain = (avgGain * (period - 1)) / period
                avgLoss = (avgLoss * (period - 1) + abs(diff)) / period
            }
        }
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    fun calculateEma(prices: List<Double>, period: Int): Double {
        if (prices.isEmpty()) return 0.0
        if (prices.size < period) return prices.last()
        val multiplier = 2.0 / (period + 1)
        var ema = prices.subList(0, period).average()
        for (i in period until prices.size) {
            ema = (prices[i] - ema) * multiplier + ema
        }
        return ema
    }

    fun calculateAtr(highs: List<Double>, lows: List<Double>, closes: List<Double>, period: Int = 14): Double {
        if (highs.size < period || lows.size < period || closes.size < period) return 0.0
        val trList = mutableListOf<Double>()
        trList.add(highs[0] - lows[0])
        for (i in 1 until min(highs.size, min(lows.size, closes.size))) {
            val hl = highs[i] - lows[i]
            val hc = abs(highs[i] - closes[i - 1])
            val lc = abs(lows[i] - closes[i - 1])
            trList.add(max(hl, max(hc, lc)))
        }
        if (trList.size < period) return trList.average()
        var atr = trList.subList(0, period).average()
        for (i in period until trList.size) {
            atr = (atr * (period - 1) + trList[i]) / period
        }
        return atr
    }

    fun computeForecast(
        symbol: String,
        currentPrice: Double,
        historicalPrices: List<Double>,
        highs: List<Double>,
        lows: List<Double>,
        fundingRate: Double = 0.0001,
        etfInflowsUsd: Double = 0.0
    ): ForecastCardModel {
        if (currentPrice <= 0.0 || historicalPrices.size < 30) {
            return ForecastCardModel(
                asset = symbol,
                currentPrice = currentPrice,
                regime = MarketRegime.INSUFFICIENT_DATA,
                direction = ForecastDirection.UNKNOWN,
                compositeScore = 0,
                probabilities = ProbabilityScenario(33, 34, 33),
                confidencePct = 0,
                keySupport = currentPrice * 0.95,
                keyResistance = currentPrice * 1.05,
                invalidationLevel = currentPrice * 0.90,
                simpleExplanation = "Market data quality or history depth is currently insufficient to build a verified prediction.",
                technicalEvidences = listOf("Insufficient sample size (< 30 intervals)", "Awaiting exchange sync"),
                riskWarning = "Do not take trades until minimum data depth requirements are fulfilled."
            )
        }

        val rsi14 = calculateRsi(historicalPrices, 14)
        val ema20 = calculateEma(historicalPrices, min(20, historicalPrices.size))
        val ema50 = calculateEma(historicalPrices, min(50, historicalPrices.size))
        val atr = calculateAtr(highs, lows, historicalPrices, 14)

        var trendScore = 0
        val trendBull = currentPrice > ema20 && ema20 > ema50
        val trendBear = currentPrice < ema20 && ema20 < ema50
        if (trendBull) trendScore += 40 else if (trendBear) trendScore -= 40

        var momScore = 0
        if (rsi14 in 45.0..65.0) momScore += 25
        else if (rsi14 > 75.0) momScore -= 15
        else if (rsi14 < 30.0) momScore += 10
        else if (rsi14 < 45.0) momScore -= 20

        var derivScore = 0
        if (fundingRate in 0.00001..0.00015) derivScore += 15
        else if (fundingRate > 0.0003) derivScore -= 20
        else if (fundingRate < -0.0001) derivScore += 10

        var macroScore = 0
        if (etfInflowsUsd > 50_000_000.0) macroScore += 20
        else if (etfInflowsUsd < -50_000_000.0) macroScore -= 20

        val totalRaw = trendScore + momScore + derivScore + macroScore
        val compositeScore = max(-100, min(100, totalRaw))

        val regime = when {
            compositeScore >= 60 -> MarketRegime.STRONG_BULL
            compositeScore in 25..59 -> MarketRegime.BULL
            compositeScore in -24..24 -> MarketRegime.NEUTRAL
            compositeScore in -59..-25 -> MarketRegime.BEAR
            else -> MarketRegime.STRONG_BEAR
        }

        val direction = when {
            compositeScore >= 25 -> ForecastDirection.BULLISH
            compositeScore <= -25 -> ForecastDirection.BEARISH
            else -> ForecastDirection.NEUTRAL
        }

        val (bullP, baseP, bearP) = when (direction) {
            ForecastDirection.BULLISH -> {
                val b = min(75, 45 + (compositeScore / 3))
                val br = max(10, 20 - (compositeScore / 10))
                Triple(b, 100 - b - br, br)
            }
            ForecastDirection.BEARISH -> {
                val br = min(75, 45 + (abs(compositeScore) / 3))
                val b = max(10, 20 - (abs(compositeScore) / 10))
                Triple(b, 100 - b - br, br)
            }
            else -> Triple(30, 45, 25)
        }

        val effectiveAtr = if (atr > 0.0) atr else (currentPrice * 0.02)
        val support = currentPrice - (effectiveAtr * 1.5)
        val resistance = currentPrice + (effectiveAtr * 1.8)
        val invalidation = if (direction == ForecastDirection.BULLISH) support - effectiveAtr else resistance + effectiveAtr

        val simpleText = when (direction) {
            ForecastDirection.BULLISH -> "The market shows buyers in control. Big trends point upward, momentum is healthy, and institutional flows support higher prices."
            ForecastDirection.BEARISH -> "Sellers currently have the advantage. Prices are slipping below key safety levels and short-term pressure remains heavy."
            else -> "Market forces are balanced in a holding zone. Neither buyers nor sellers are committed to an immediate breakthrough."
        }

        val evidences = mutableListOf<String>()
        evidences.add("EMA Structure: " + if (trendBull) "Bullish Alignment (Price > EMA20 > EMA50)" else if (trendBear) "Bearish Compression (Price < EMA20 < EMA50)" else "Mixed Trajectory")
        evidences.add("RSI (14): ${String.format(java.util.Locale.US, "%.1f", rsi14)} (" + if (rsi14 > 70) "Overheated" else if (rsi14 < 30) "Oversold" else "Healthy Momentum" + ")")
        evidences.add("Derivatives: 8h Funding Rate at ${String.format(java.util.Locale.US, "%.4f", fundingRate * 100)}%")
        if (etfInflowsUsd != 0.0) {
            evidences.add("Institutional Inflow: ${String.format(java.util.Locale.US, "$%.1fM", etfInflowsUsd / 1_000_000.0)}")
        }

        val riskWarning = "Volatility (ATR: $${String.format(java.util.Locale.US, "%.2f", effectiveAtr)}) can invalidate setups quickly. Strictly respect $${String.format(java.util.Locale.US, "%.2f", invalidation)} as structural invalidation."

        return ForecastCardModel(
            asset = symbol.uppercase(),
            currentPrice = currentPrice,
            regime = regime,
            direction = direction,
            compositeScore = compositeScore,
            probabilities = ProbabilityScenario(bullP, baseP, bearP),
            confidencePct = min(92, 50 + (abs(compositeScore) / 2)),
            keySupport = support,
            keyResistance = resistance,
            invalidationLevel = invalidation,
            simpleExplanation = simpleText,
            technicalEvidences = evidences,
            riskWarning = riskWarning
        )
    }
}
