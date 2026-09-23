package com.example.data.derivatives

import kotlin.math.sqrt

internal object DerivativesMath {

    fun finite(value: Double?): Double? {
        if (value == null) return null
        return if (value.isFinite()) value else null
    }

    fun positive(value: Double?): Double? {
        val n = finite(value) ?: return null
        return if (n > 0.0) n else null
    }

    fun zScore(values: List<Double>, latest: Double): Double? {
        if (values.size < 8 || !latest.isFinite()) return null
        val mean = values.sum() / values.size
        var variance = 0.0
        for (item in values) {
            val d = item - mean
            variance += d * d
        }
        variance /= (values.size - 1).toDouble()
        val sd = sqrt(variance)
        if (sd < 1e-12) return 0.0
        return (latest - mean) / sd
    }

    fun zToScore(z: Double): Double = (z / 2.0 * 100.0).coerceIn(-100.0, 100.0)

    fun pctChange(previous: Double?, current: Double?): Double? {
        val prev = positive(previous) ?: return null
        val cur = finite(current) ?: return null
        return ((cur - prev) / prev) * 100.0
    }

    fun weightedAverage(values: List<Pair<Double, Double?>>): Double? {
        var weightedNum = 0.0
        var weightedDen = 0.0
        var equalNum = 0.0
        var equalCount = 0
        for ((value, weight) in values) {
            if (!value.isFinite()) continue
            val w = weight
            if (w != null && w.isFinite() && w > 0.0) {
                weightedNum += value * w
                weightedDen += w
            } else {
                equalNum += value
                equalCount += 1
            }
        }
        if (weightedDen > 0.0) return weightedNum / weightedDen
        if (equalCount > 0) return equalNum / equalCount.toDouble()
        return null
    }

    fun sourceLabel(venuesUp: List<String>): String {
        val names = venuesUp.map { displayVenue(it) }.distinct()
        return when (names.size) {
            0 -> ""
            1 -> names[0]
            2 -> "${names[0]} & ${names[1]}"
            else -> names.dropLast(1).joinToString(", ") + " & " + names.last()
        }
    }

    fun displayVenue(venue: String): String = when (venue.lowercase()) {
        "binance" -> "Binance"
        "bybit" -> "Bybit"
        "okx" -> "OKX"
        else -> venue
    }
}
