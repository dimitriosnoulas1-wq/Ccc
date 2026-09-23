package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class RainbowBand(
    val id: Int,
    val nameEn: String,
    val nameEl: String,
    val colorHex: Long,
    val color: Color,
    val multiplier: Double,
    val descriptionEn: String,
    val descriptionEl: String
) {
    MAXIMUM_BUBBLE(
        id = 8,
        nameEn = "Maximum Bubble Territory",
        nameEl = "Περιοχή Απόλυτης Φούσκας",
        colorHex = 0xFFBE123C,
        color = Color(0xFFBE123C),
        multiplier = 8.10,
        descriptionEn = "Historically the highest rainbow band. A record of past cycle tops, not a sell order.",
        descriptionEl = "Ιστορικά η υψηλότερη ζώνη Rainbow. Καταγραφή παλιών κορυφών, όχι εντολή πώλησης."
    ),
    SELL_SERIOUSLY(
        id = 7,
        nameEn = "Historically overheated",
        nameEl = "Ιστορικά υπερθερμασμένη",
        colorHex = 0xFFEF4444,
        color = Color(0xFFEF4444),
        multiplier = 6.00,
        descriptionEn = "Historically an overheated band on prior cycles. A record, not a sell order.",
        descriptionEl = "Ιστορικά υπερθερμασμένη ζώνη σε παλιούς κύκλους. Καταγραφή, όχι εντολή πώλησης."
    ),
    FOMO_INTENSIFIES(
        id = 6,
        nameEn = "FOMO intensifies",
        nameEl = "Το FOMO εντείνεται",
        colorHex = 0xFFF97316,
        color = Color(0xFFF97316),
        multiplier = 4.45,
        descriptionEn = "Historically a late-cycle band on prior bull markets. A record, not a trade.",
        descriptionEl = "Ιστορικά ζώνη τέλους κύκλου σε παλιά bull markets. Καταγραφή, όχι συναλλαγή."
    ),
    IS_THIS_A_BUBBLE(
        id = 5,
        nameEn = "Is this a bubble?",
        nameEl = "Μήπως είναι φούσκα;",
        colorHex = 0xFFF59E0B,
        color = Color(0xFFF59E0B),
        multiplier = 3.30,
        descriptionEn = "Historically above the mid-cycle band. A record of past prints, not a warning to sell.",
        descriptionEl = "Ιστορικά πάνω από τη μεσαία ζώνη. Καταγραφή παλιών τιμών, όχι προειδοποίηση πώλησης."
    ),
    HODL(
        id = 4,
        nameEn = "HODL!",
        nameEl = "HODL!",
        colorHex = 0xFFEAB308,
        color = Color(0xFFEAB308),
        multiplier = 2.45,
        descriptionEn = "Equilibrium fair value zone. Healthy expansion, neutral market equilibrium.",
        descriptionEl = "Ζώνη ισορροπίας και δίκαιης αξίας. Υγιής επέκταση κύκλου, ουδέτερη ισορροπία."
    ),
    STILL_CHEAP(
        id = 3,
        nameEn = "Still cheap",
        nameEl = "Ακόμα φθηνό",
        colorHex = 0xFF84CC16,
        color = Color(0xFF84CC16),
        multiplier = 1.80,
        descriptionEn = "Historically a lower-mid band. A record, not a dollar-cost-average order.",
        descriptionEl = "Ιστορικά χαμηλότερη-μεσαία ζώνη. Καταγραφή, όχι εντολή DCA."
    ),
    ACCUMULATE(
        id = 2,
        nameEn = "Accumulate",
        nameEl = "Συσσώρευση",
        colorHex = 0xFF10B981,
        color = Color(0xFF10B981),
        multiplier = 1.35,
        descriptionEn = "Historically a lower band on prior cycles. A record, not an accumulate order.",
        descriptionEl = "Ιστορικά χαμηλότερη ζώνη σε παλιούς κύκλους. Καταγραφή, όχι εντολή συσσώρευσης."
    ),
    BUY(
        id = 1,
        nameEn = "Historically deep floor",
        nameEl = "Ιστορικά βαθύς πυθμένας",
        colorHex = 0xFF06B6D4,
        color = Color(0xFF06B6D4),
        multiplier = 1.00,
        descriptionEn = "Historically a deep floor band on prior bear markets. A record, not a buy order.",
        descriptionEl = "Ιστορικά βαθύς πυθμένας σε παλιά bear markets. Καταγραφή, όχι εντολή αγοράς."
    ),
    FIRE_SALE(
        id = 0,
        nameEn = "Basically a Fire Sale",
        nameEl = "Κυριολεκτικά Ξεπούλημα",
        colorHex = 0xFF3B82F6,
        color = Color(0xFF3B82F6),
        multiplier = 0.75,
        descriptionEn = "Historically the lowest rainbow band. A record of past floors, not a buy order.",
        descriptionEl = "Ιστορικά η χαμηλότερη ζώνη Rainbow. Καταγραφή παλιών πυθμένων, όχι εντολή αγοράς."
    );

    fun localizedName(isGreek: Boolean): String = if (isGreek) nameEl else nameEn
    fun localizedDesc(isGreek: Boolean): String = if (isGreek) descriptionEl else descriptionEn
}

data class RainbowPoint(
    val year: Double,
    val dayNumber: Int,
    val btcPrice: Double?, // null if projection into future
    val bandPrices: List<Double> // Prices from Fire Sale (0) up to Max Bubble (8)
)

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
    fun localizedSentiment(isGreek: Boolean): String = if (isGreek) sentimentEl else sentiment
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
