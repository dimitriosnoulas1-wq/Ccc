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
        descriptionEn = "Extreme euphoria, historical cycle top blow-off territory. Heavy profit taking zone.",
        descriptionEl = "Ακραία ευφορία, ιστορική ζώνη κορυφής κύκλου. Περιοχή μαζικής κατοχύρωσης κερδών."
    ),
    SELL_SERIOUSLY(
        id = 7,
        nameEn = "Sell. Seriously, SELL!",
        nameEl = "Πούλα. Σοβαρά, ΠΟΥΛΑ!",
        colorHex = 0xFFEF4444,
        color = Color(0xFFEF4444),
        multiplier = 6.00,
        descriptionEn = "Severe overvaluation. Historically optimal zone for scaling out substantial positions.",
        descriptionEl = "Έντονη υπερτίμηση. Ιστορικά βέλτιστη ζώνη για κλιμακωτή έξοδο και ρευστοποίηση."
    ),
    FOMO_INTENSIFIES(
        id = 6,
        nameEn = "FOMO intensifies",
        nameEl = "Το FOMO εντείνεται",
        colorHex = 0xFFF97316,
        color = Color(0xFFF97316),
        multiplier = 4.45,
        descriptionEn = "Retail mania and exponential greed entering the market. Late-stage bull market markup.",
        descriptionEl = "Είσοδος μαζικής ψυχολογίας FOMO και απληστίας. Προχωρημένο στάδιο bull market."
    ),
    IS_THIS_A_BUBBLE(
        id = 5,
        nameEn = "Is this a bubble?",
        nameEl = "Μήπως είναι φούσκα;",
        colorHex = 0xFFF59E0B,
        color = Color(0xFFF59E0B),
        multiplier = 3.30,
        descriptionEn = "Price accelerates past fair value. First warning sign for long-term cycle holders.",
        descriptionEl = "Η τιμή επιταχύνει πάνω από τη δίκαιη αξία. Πρώτη ένδειξη προσοχής για μακροπρόθεσμους κατόχους."
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
        descriptionEn = "Undervalued territory. Favorable risk/reward for long-term dollar-cost averaging.",
        descriptionEl = "Υποτιμημένη περιοχή. Εξαιρετική αναλογία ρίσκου/απόδοσης για σταδιακές αγορές DCA."
    ),
    ACCUMULATE(
        id = 2,
        nameEn = "Accumulate",
        nameEl = "Συσσώρευση",
        colorHex = 0xFF10B981,
        color = Color(0xFF10B981),
        multiplier = 1.35,
        descriptionEn = "Prime smart money accumulation zone. Strong multi-year asymmetry to the upside.",
        descriptionEl = "Προνομιακή ζώνη έξυπνου χρήματος για συσσώρευση. Ισχυρή ασυμμετρία ανόδου."
    ),
    BUY(
        id = 1,
        nameEn = "BUY!",
        nameEl = "ΑΓΟΡΑ!",
        colorHex = 0xFF06B6D4,
        color = Color(0xFF06B6D4),
        multiplier = 1.00,
        descriptionEn = "Generational buying opportunity. Deep bear market capitulation bottom zone.",
        descriptionEl = "Εξαιρετική ευκαιρία αγοράς. Ζώνη πυθμένα bear market και συνθηκολόγησης."
    ),
    FIRE_SALE(
        id = 0,
        nameEn = "Basically a Fire Sale",
        nameEl = "Κυριολεκτικά Ξεπούλημα",
        colorHex = 0xFF3B82F6,
        color = Color(0xFF3B82F6),
        multiplier = 0.75,
        descriptionEn = "Historic multi-year bottom floor. Maximum asymmetry and lowest cycle valuation.",
        descriptionEl = "Ιστορικός πυθμένας πολλαπλών ετών. Μέγιστη ασυμμετρία κέρδους και χαμηλότερη αποτίμηση."
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
    val score: Int = 68,
    val sentiment: String = "Greed",
    val sentimentEl: String = "Απληστία",
    val yesterdayScore: Int = 65,
    val lastWeekScore: Int = 72,
    val lastMonthScore: Int = 48
) {
    fun localizedSentiment(isGreek: Boolean): String = if (isGreek) sentimentEl else sentiment
}

data class PiCycleData(
    val currentBtcPrice: Double = 96420.0,
    // Top Indicator (111 SMA vs 350 SMA x 2)
    val dma111: Double = 84200.0,
    val dma350x2: Double = 142800.0,
    val isCrossed: Boolean = false,
    val distanceToTopCrossPct: Double = 48.2,
    // Bottom Indicator (150 EMA vs 471 SMA x 0.745)
    val ema150: Double = 82400.0,
    val sma471x0745: Double = 49800.0,
    val isBottomCrossed: Boolean = false,
    val distanceToBottomCrossPct: Double = 65.4,
    // 200-Week Moving Average Generational Floor
    val ma200w: Double = 43500.0,
    val distanceAbove200wPct: Double = 121.6
)
