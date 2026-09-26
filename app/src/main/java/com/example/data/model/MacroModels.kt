package com.example.data.model

data class AltcoinSeasonData(
    val score: Int = 0,
    val previousMonthScore: Int = 0,
    val previousYearScore: Int = 0,
    val topOutperformingCoins: List<Pair<String, Double>> = emptyList(),
    val btcGain90d: Double = 0.0,
    val top50OutperformedCount: Int = 0,
    val totalTop50Count: Int = 0,
    val isAvailable: Boolean = false
) {
    val isAltSeason: Boolean get() = score > 75
    val isBtcSeason: Boolean get() = score < 25
    val isNeutral: Boolean get() = score in 25..75

    val zoneNameEnglish: String
        get() = when {
            score > 75 -> "Altcoin Season"
            score < 25 -> "Bitcoin Season"
            else -> "Neutral Zone"
        }

    val zoneNameGreek: String
        get() = when {
            score > 75 -> "Εποχή Altcoin"
            score < 25 -> "Bitcoin Season"
            else -> "Ουδέτερη Ζώνη"
        }
}

data class FearAndGreedData(
    val score: Int = 0,
    val sentiment: String = "—",
    val sentimentEl: String = "—",
    val yesterdayScore: Int = 0,
    val lastWeekScore: Int = 0,
    val lastMonthScore: Int = 0,
    val isLive: Boolean = false
) {
}

data class PiCycleData(
    val currentBtcPrice: Double = 0.0,
    val dma111: Double = 0.0,
    val dma350x2: Double = 0.0,
    val isCrossed: Boolean = false,
    val distanceToTopCrossPct: Double = 0.0,
    val ema150: Double = 0.0,
    val sma471x0745: Double = 0.0,
    val isBottomCrossed: Boolean = false,
    val distanceToBottomCrossPct: Double = 0.0,
    val ma200w: Double = 0.0,
    val distanceAbove200wPct: Double = 0.0,
    val isLive: Boolean = false
)
