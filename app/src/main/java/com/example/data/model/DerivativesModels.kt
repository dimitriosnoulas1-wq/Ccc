package com.example.data.model

enum class DerivativesFreshness {
    FRESH,
    DELAYED,
    STALE,
    DEGRADED,
    UNAVAILABLE
}

data class VenueDerivativesSnapshot(
    val venue: String,
    val contract: String,
    val ok: Boolean,
    val asOfMs: Long = 0L,
    val markPrice: Double? = null,
    val lastPrice: Double? = null,
    val change24hPct: Double? = null,
    val fundingRate: Double? = null,
    val openInterest: Double? = null,
    val openInterestUsd: Double? = null,
    val oiChange1hPct: Double? = null,
    val oiChangeZ: Double? = null,
    val fundingZ: Double? = null,
    val takerBuySellRatio: Double? = null,
    val longShortRatio: Double? = null,
    val longLiqUsd: Double? = null,
    val shortLiqUsd: Double? = null,
    val missing: List<String> = emptyList(),
    val error: String? = null
)

data class AggregatedDerivativesMetrics(
    val markPrice: Double? = null,
    val lastPrice: Double? = null,
    val change24hPct: Double? = null,
    val fundingRate: Double? = null,
    val openInterestUsd: Double? = null,
    val oiChange1hPct: Double? = null,
    val oiChangeZ: Double? = null,
    val fundingZ: Double? = null,
    val takerBuySellRatio: Double? = null,
    val longShortRatio: Double? = null,
    val longLiqUsd: Double? = null,
    val shortLiqUsd: Double? = null,
    val venueCount: Int = 0
)

data class DerivativesScoreComponent(
    val name: String,
    val raw: Double,
    val configuredWeight: Double,
    val usedWeight: Double,
    val used: Boolean
)

data class DerivativesScore(
    val value: Double?,
    val components: List<DerivativesScoreComponent>,
    val redistributed: Boolean,
    val formula: String
)

data class AggregatedDerivativesSnapshot(
    val symbol: String,
    val asOfMs: Long,
    val freshness: DerivativesFreshness,
    val venues: List<VenueDerivativesSnapshot>,
    val aggregated: AggregatedDerivativesMetrics,
    val score: DerivativesScore,
    val sourceLabel: String
) {
    val venuesUp: List<String>
        get() = venues.filter { it.ok }.map { it.venue }
}
