package com.example.data.model

/**
 * Data models for the CryptoCycles Comparative Cycle Engine:
 * Compares exact Day Offsets (e.g. Day +330 post-ATH / post-Halving)
 * across historical Bitcoin cycles (2013-15, 2017-18, 2021-22, and current 2025-26).
 */

data class HistoricalCycleComparison(
    val cycleName: String,
    val cyclePeriod: String,
    val athDate: String,
    val athPriceUsd: Double,
    val dateOnDay: String,
    val priceOnDay: Double,
    val drawdownPercentOnDay: Double,
    val statusSummaryEn: String,
    val statusSummaryEl: String,
    val altcoinPerformanceEn: String,
    val altcoinPerformanceEl: String,
    val bottomDayOffset: Int,
    val bottomDate: String,
    val bottomPriceUsd: Double,
    val bottomMaxDrawdownPercent: Double,
    val daysRemainingToBottom: Int
)

data class HistoricalCrashEvent(
    val id: String,
    val cycleYear: String,
    val dayOffsetRange: String,
    val exactDayOffset: Int,
    val approxDate: String,
    val titleEn: String,
    val titleEl: String,
    val severity: String, // "CRITICAL", "HIGH", "MODERATE"
    val btcDropSummary: String,
    val altcoinDropSummary: String,
    val whatHappenedEn: String,
    val whatHappenedEl: String,
    val whyItCrashedEn: String,
    val whyItCrashedEl: String,
    val altcoinImpactEn: String,
    val altcoinImpactEl: String,
    val marketPsychologyEn: String,
    val marketPsychologyEl: String,
    val survivalLessonEn: String,
    val survivalLessonEl: String
)

data class CycleDayComparativeReport(
    val targetDayOffset: Int,
    val isTodayLive: Boolean,
    val daysSinceAth: Int,
    val daysSinceHalving: Int,
    val currentBtcPrice: Double,
    val currentBtcAthPrice: Double,
    val currentAthDate: String,
    val currentDrawdownPercent: Double,
    val daysUntilHistoricalBottom: Int,
    val historicalBottomWindow: String,
    val phaseNameEn: String,
    val phaseNameEl: String,
    val phaseDiagnosisEn: String,
    val phaseDiagnosisEl: String,
    val comparisons: List<HistoricalCycleComparison>,
    val altcoinComparisonSummaryEn: String,
    val altcoinComparisonSummaryEl: String,
    val crashesInPeriod: List<HistoricalCrashEvent>,
    val allHistoricalCrashTimeline: List<HistoricalCrashEvent>
)
