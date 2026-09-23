package com.example.data.derivatives

import com.example.data.model.AggregatedDerivativesMetrics
import com.example.data.model.AggregatedDerivativesSnapshot
import com.example.data.model.DerivativesFreshness
import com.example.data.model.VenueDerivativesSnapshot

object DerivativesAggregator {

    fun assemble(
        symbol: String,
        venues: List<VenueDerivativesSnapshot>,
        nowMs: Long = System.currentTimeMillis()
    ): AggregatedDerivativesSnapshot {
        val up = venues.filter { it.ok }
        val aggregated = aggregate(up)
        val score = DerivativesScoringEngine.score(aggregated)
        return AggregatedDerivativesSnapshot(
            symbol = symbol.uppercase(),
            asOfMs = up.maxOfOrNull { it.asOfMs }?.takeIf { it > 0L } ?: nowMs,
            freshness = freshness(venues, nowMs),
            venues = venues,
            aggregated = aggregated,
            score = score,
            sourceLabel = DerivativesMath.sourceLabel(up.map { it.venue })
        )
    }

    fun aggregate(venues: List<VenueDerivativesSnapshot>): AggregatedDerivativesMetrics {
        val live = venues.filter { it.ok }
        if (live.isEmpty()) return AggregatedDerivativesMetrics()
        return AggregatedDerivativesMetrics(
            markPrice = avg(live) { it.markPrice },
            lastPrice = avg(live) { it.lastPrice },
            change24hPct = avg(live) { it.change24hPct },
            fundingRate = avg(live) { it.fundingRate },
            openInterestUsd = sum(live) { it.openInterestUsd },
            oiChange1hPct = avg(live) { it.oiChange1hPct },
            oiChangeZ = avg(live) { it.oiChangeZ },
            fundingZ = avg(live) { it.fundingZ },
            takerBuySellRatio = avg(live) { it.takerBuySellRatio },
            longShortRatio = avg(live) { it.longShortRatio },
            longLiqUsd = sum(live) { it.longLiqUsd },
            shortLiqUsd = sum(live) { it.shortLiqUsd },
            venueCount = live.size
        )
    }

    fun freshness(venues: List<VenueDerivativesSnapshot>, nowMs: Long): DerivativesFreshness {
        val up = venues.filter { it.ok }
        if (up.isEmpty()) return DerivativesFreshness.UNAVAILABLE
        val maxAge = up.maxOf { venue ->
            val at = if (venue.asOfMs > 0L) venue.asOfMs else nowMs
            (nowMs - at).coerceAtLeast(0L)
        }
        val degraded = up.size < venues.size
        return when {
            maxAge > 60_000L -> DerivativesFreshness.STALE
            degraded -> DerivativesFreshness.DEGRADED
            maxAge > 15_000L -> DerivativesFreshness.DELAYED
            else -> DerivativesFreshness.FRESH
        }
    }

    private fun avg(
        venues: List<VenueDerivativesSnapshot>,
        read: (VenueDerivativesSnapshot) -> Double?
    ): Double? {
        return DerivativesMath.weightedAverage(
            venues.mapNotNull { venue ->
                val value = DerivativesMath.finite(read(venue)) ?: return@mapNotNull null
                value to venue.openInterestUsd
            }
        )
    }

    private fun sum(
        venues: List<VenueDerivativesSnapshot>,
        read: (VenueDerivativesSnapshot) -> Double?
    ): Double? {
        var total = 0.0
        var seen = false
        for (venue in venues) {
            val value = DerivativesMath.finite(read(venue)) ?: continue
            if (value < 0.0) continue
            total += value
            seen = true
        }
        return if (seen) total else null
    }
}
