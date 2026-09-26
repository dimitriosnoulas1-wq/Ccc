package com.example.engine.forecasting

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class MarketRegime(val label: String, val isBullish: Boolean) {
    STRONG_BULL("STRONG UP", true),
    BULL("UP", true),
    NEUTRAL("MIXED", true),
    BEAR("DOWN", false),
    STRONG_BEAR("STRONG DOWN", false),
    INSUFFICIENT_DATA("INSUFFICIENT DATA", false)
}

enum class ForecastDirection {
    BULLISH,
    BEARISH,
    NEUTRAL,
    UNKNOWN
}

data class ForecastCardModel(
    val asset: String,
    val currentPrice: Double,
    val regime: MarketRegime,
    val direction: ForecastDirection,
    val compositeScore: Int,
    val keySupport: Double,
    val keyResistance: Double,
    val invalidationLevel: Double,
    val simpleExplanation: String,
    val technicalEvidences: List<String>,
    val riskWarning: String,
    val hasLiveTape: Boolean = false,
    val hasLiveFunding: Boolean = false,
    val hasLiveEtf: Boolean = false,
    val hasLiveLevels: Boolean = false
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
        if (highs.any { it <= 0.0 } || lows.any { it <= 0.0 }) return 0.0
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
        fundingRate: Double? = null,
        etfInflowsUsd: Double? = null
    ): ForecastCardModel {
        if (currentPrice <= 0.0 || historicalPrices.size < 30) {
            return ForecastCardModel(
                asset = symbol.uppercase(),
                currentPrice = currentPrice,
                regime = MarketRegime.INSUFFICIENT_DATA,
                direction = ForecastDirection.UNKNOWN,
                compositeScore = 0,
                keySupport = 0.0,
                keyResistance = 0.0,
                invalidationLevel = 0.0,
                simpleExplanation = "Need 30 live daily closes from the exchange. No invented support, resistance, or probabilities.",
                technicalEvidences = listOf(
                    "Daily tape has ${historicalPrices.size} closes (need 30+)",
                    "Waiting for exchange daily OHLC"
                ),
                riskWarning = "No reading until the daily tape arrives. This is not a trade call.",
                hasLiveTape = false,
                hasLiveFunding = fundingRate != null,
                hasLiveEtf = etfInflowsUsd != null,
                hasLiveLevels = false
            )
        }

        val rsi14 = calculateRsi(historicalPrices, 14)
        val ema20 = calculateEma(historicalPrices, min(20, historicalPrices.size))
        val ema50 = calculateEma(historicalPrices, min(50, historicalPrices.size))
        val alignedOhlc = highs.size == historicalPrices.size &&
            lows.size == historicalPrices.size &&
            highs.all { it > 0.0 } &&
            lows.all { it > 0.0 }
        val atr = if (alignedOhlc) calculateAtr(highs, lows, historicalPrices, 14) else 0.0

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
        if (fundingRate != null) {
            if (fundingRate in 0.00001..0.00015) derivScore += 15
            else if (fundingRate > 0.0003) derivScore -= 20
            else if (fundingRate < -0.0001) derivScore += 10
        }

        var macroScore = 0
        if (etfInflowsUsd != null) {
            if (etfInflowsUsd > 50_000_000.0) macroScore += 20
            else if (etfInflowsUsd < -50_000_000.0) macroScore -= 20
        }

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

        val lookbackCloses = historicalPrices.takeLast(20)
        val lookbackHighs = if (alignedOhlc) highs.takeLast(20) else emptyList()
        val lookbackLows = if (alignedOhlc) lows.takeLast(20) else emptyList()
        val support = lookbackLows.filter { it > 0.0 }.minOrNull()
            ?: lookbackCloses.minOrNull()
            ?: 0.0
        val resistance = lookbackHighs.filter { it > 0.0 }.maxOrNull()
            ?: lookbackCloses.maxOrNull()
            ?: 0.0
        val hasLevels = support > 0.0 && resistance > 0.0
        val invalidation = if (hasLevels) support else 0.0

        val simpleText = buildString {
            append("Reading of the last ${historicalPrices.size} daily closes")
            if (alignedOhlc) append(" plus exchange high/low")
            append(". ")
            append(
                when {
                    trendBull -> "Price is above EMA20 and EMA20 is above EMA50."
                    trendBear -> "Price is below EMA20 and EMA20 is below EMA50."
                    else -> "EMA20 and EMA50 are mixed versus spot."
                }
            )
            append(" RSI(14) is ${String.format(java.util.Locale.US, "%.1f", rsi14)}.")
            if (fundingRate == null) append(" Funding is offline.")
            if (etfInflowsUsd == null) append(" ETF flow is offline.")
            append(" Score of these inputs only — not a forecast or a trade.")
        }

        val evidences = mutableListOf<String>()
        evidences.add(
            "EMA structure: " + when {
                trendBull -> "Price > EMA20 > EMA50"
                trendBear -> "Price < EMA20 < EMA50"
                else -> "Mixed"
            }
        )
        evidences.add("RSI (14): ${String.format(java.util.Locale.US, "%.1f", rsi14)}")
        if (atr > 0.0) {
            evidences.add("ATR (14) from daily high/low: $${String.format(java.util.Locale.US, "%.2f", atr)}")
        } else {
            evidences.add("ATR offline — daily high/low not on this tape")
        }
        if (fundingRate != null) {
            evidences.add("8h funding: ${String.format(java.util.Locale.US, "%.4f", fundingRate * 100)}%")
        } else {
            evidences.add("Funding: —")
        }
        if (etfInflowsUsd != null) {
            evidences.add("US spot ETF 1-day net: ${String.format(java.util.Locale.US, "$%.1fM", etfInflowsUsd / 1_000_000.0)}")
        } else {
            evidences.add("ETF flow: —")
        }

        val riskWarning = if (hasLevels) {
            "Support and resistance are the min/max of the last 20 daily prints. Not a trade setup."
        } else {
            "No support or resistance until daily prints arrive."
        }

        return ForecastCardModel(
            asset = symbol.uppercase(),
            currentPrice = currentPrice,
            regime = regime,
            direction = direction,
            compositeScore = compositeScore,
            keySupport = support,
            keyResistance = resistance,
            invalidationLevel = invalidation,
            simpleExplanation = simpleText,
            technicalEvidences = evidences,
            riskWarning = riskWarning,
            hasLiveTape = true,
            hasLiveFunding = fundingRate != null,
            hasLiveEtf = etfInflowsUsd != null,
            hasLiveLevels = hasLevels
        )
    }
}
