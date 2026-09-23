package com.example.data.derivatives

import com.example.data.model.AggregatedDerivativesMetrics
import com.example.data.model.DerivativesScore
import com.example.data.model.DerivativesScoreComponent

/**
 * Deterministic public-derivatives score in [-100, +100].
 *
 * History: when a z-score is present it is used.
 * No history: documented fallbacks — never a fabricated print.
 *   funding: extreme +funding = crowded longs = negative
 *   oi: 1h % change vs a 2% scale, then signed by 24h price direction
 *   liq: short minus long, over total (null if neither side exists)
 *   taker: buy/sell ratio centered at 1.0
 *   long/short: ratio centered at 1.0, high = crowded longs = negative
 */
object DerivativesScoringEngine {

    fun score(metrics: AggregatedDerivativesMetrics): DerivativesScore {
        val drafts = listOf(
            fundingComponent(metrics),
            openInterestComponent(metrics),
            liquidationComponent(metrics),
            takerComponent(metrics),
            longShortComponent(metrics)
        )
        val used = drafts.filter { it.used }
        if (used.isEmpty()) {
            return DerivativesScore(
                value = null,
                components = drafts,
                redistributed = false,
                formula = DerivativesWeights.FORMULA
            )
        }
        val usedWeightSum = used.sumOf { it.configuredWeight }
        val components = drafts.map { draft ->
            if (!draft.used || usedWeightSum <= 0.0) {
                draft.copy(usedWeight = 0.0)
            } else {
                draft.copy(usedWeight = draft.configuredWeight / usedWeightSum * 100.0)
            }
        }
        val value = components
            .filter { it.used }
            .sumOf { it.raw * (it.usedWeight / 100.0) }
            .coerceIn(-100.0, 100.0)
        return DerivativesScore(
            value = value,
            components = components,
            redistributed = used.size < drafts.size,
            formula = DerivativesWeights.FORMULA
        )
    }

    private fun unused(name: String, weight: Double): DerivativesScoreComponent {
        return DerivativesScoreComponent(
            name = name,
            raw = 0.0,
            configuredWeight = weight,
            usedWeight = 0.0,
            used = false
        )
    }

    private fun used(name: String, raw: Double, weight: Double): DerivativesScoreComponent {
        return DerivativesScoreComponent(
            name = name,
            raw = raw.coerceIn(-100.0, 100.0),
            configuredWeight = weight,
            usedWeight = 0.0,
            used = true
        )
    }

    private fun fundingComponent(metrics: AggregatedDerivativesMetrics): DerivativesScoreComponent {
        val rate = DerivativesMath.finite(metrics.fundingRate) ?: return unused("funding", DerivativesWeights.FUNDING)
        val z = DerivativesMath.finite(metrics.fundingZ)
        val raw = if (z != null) {
            -DerivativesMath.zToScore(z)
        } else {
            (-rate / 0.0005 * 100.0)
        }
        return used("funding", raw, DerivativesWeights.FUNDING)
    }

    private fun openInterestComponent(metrics: AggregatedDerivativesMetrics): DerivativesScoreComponent {
        val change = DerivativesMath.finite(metrics.oiChange1hPct)
        val z = DerivativesMath.finite(metrics.oiChangeZ)
        if (change == null && z == null) {
            return unused("oi", DerivativesWeights.OPEN_INTEREST)
        }
        val magnitude = if (z != null) {
            DerivativesMath.zToScore(z)
        } else {
            (change!! / 2.0 * 100.0).coerceIn(-100.0, 100.0)
        }
        val signed = when (val px = DerivativesMath.finite(metrics.change24hPct)) {
            null -> magnitude * 0.35
            else -> if (px >= 0.0) magnitude else -magnitude
        }
        return used("oi", signed, DerivativesWeights.OPEN_INTEREST)
    }

    private fun liquidationComponent(metrics: AggregatedDerivativesMetrics): DerivativesScoreComponent {
        val longLiq = DerivativesMath.finite(metrics.longLiqUsd)
        val shortLiq = DerivativesMath.finite(metrics.shortLiqUsd)
        if (longLiq == null && shortLiq == null) {
            return unused("liq", DerivativesWeights.LIQUIDATIONS)
        }
        val longs = (longLiq ?: 0.0).coerceAtLeast(0.0)
        val shorts = (shortLiq ?: 0.0).coerceAtLeast(0.0)
        val total = longs + shorts
        if (total <= 0.0) return unused("liq", DerivativesWeights.LIQUIDATIONS)
        return used("liq", (shorts - longs) / total * 100.0, DerivativesWeights.LIQUIDATIONS)
    }

    private fun takerComponent(metrics: AggregatedDerivativesMetrics): DerivativesScoreComponent {
        val ratio = DerivativesMath.finite(metrics.takerBuySellRatio) ?: return unused("taker", DerivativesWeights.TAKER)
        return used("taker", (ratio - 1.0) / 0.3 * 100.0, DerivativesWeights.TAKER)
    }

    private fun longShortComponent(metrics: AggregatedDerivativesMetrics): DerivativesScoreComponent {
        val ratio = DerivativesMath.finite(metrics.longShortRatio) ?: return unused("longShort", DerivativesWeights.LONG_SHORT)
        return used("longShort", (1.0 - ratio) / 0.8 * 100.0, DerivativesWeights.LONG_SHORT)
    }
}
