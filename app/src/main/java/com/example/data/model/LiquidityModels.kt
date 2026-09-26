package com.example.data.model

data class StablecoinLiquidityData(
    val totalCirculatingUsd: Double = 0.0,
    val change7dUsd: Double = 0.0,
    val change7dPercent: Double = 0.0,
    val usdtDominancePercent: Double = 0.0,
    val usdcCirculatingUsd: Double = 0.0,
    val isLiquidityExpanding: Boolean = false,
    val sourceName: String = "DefiLlama",
    val asOfDate: String = "—",
    val isLive: Boolean = false
)

enum class MarketRegime(
    val titleEn: String,
    val titleEl: String,
    val actionEn: String,
    val actionEl: String,
    val zoneNumber: Int
) {
    ACCUMULATION(
        titleEn = "Zone 1: Fear",
        titleEl = "Ζώνη 1: Φόβος",
        actionEn = "HISTORY",
        actionEl = "ΙΣΤΟΡΙΚΟ",
        zoneNumber = 1
    ),
    CYCLE_EXPANSION(
        titleEn = "Zone 2: Neutral mood",
        titleEl = "Ζώνη 2: Ουδέτερο κλίμα",
        actionEn = "HISTORY",
        actionEl = "ΙΣΤΟΡΙΚΟ",
        zoneNumber = 2
    ),
    LEVERAGE_DISTRIBUTION(
        titleEn = "Zone 3: Hot funding",
        titleEl = "Ζώνη 3: Ζεστό funding",
        actionEn = "HISTORY",
        actionEl = "ΙΣΤΟΡΙΚΟ",
        zoneNumber = 3
    ),
    CYCLE_PEAK_EXIT(
        titleEn = "Zone 4: Extreme greed or funding",
        titleEl = "Ζώνη 4: Ακραία απληστία ή funding",
        actionEn = "HISTORY",
        actionEl = "ΙΣΤΟΡΙΚΟ",
        zoneNumber = 4
    )
}

data class CycleCommandState(
    val regime: MarketRegime = MarketRegime.CYCLE_EXPANSION,
    val compositeCycleScore: Int = 0,
    val halvingDaysElapsed: Int = 0,
    val halvingCycleLength: Int = 1460,
    val rainbowBandName: String = "—",
    val fundingRatePercent: Double = 0.0,
    val isFundingHeated: Boolean = false,
    val etf5dNetFlowMillionUsd: Double = 0.0,
    val stablecoinTotalUsdBillion: Double = 0.0,
    val stablecoin7dChangeBillion: Double = 0.0,
    val fearAndGreedIndex: Int = -1,
    val altcoinSeasonIndex: Int = -1,
    val etfFlowIsLive: Boolean = false,
    val fundingIsLive: Boolean = false,
    val keyStanceSummaryEn: String = "Waiting for live market data.",
    val keyStanceSummaryEl: String = "Αναμονή ζωντανών δεδομένων αγοράς."
)
