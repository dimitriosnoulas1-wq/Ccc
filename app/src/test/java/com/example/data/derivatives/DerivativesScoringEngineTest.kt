package com.example.data.derivatives

import com.example.data.model.AggregatedDerivativesMetrics
import com.example.data.model.VenueDerivativesSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DerivativesScoringEngineTest {

    @Test
    fun weightsSumToOneHundred() {
        assertEquals(100.0, DerivativesWeights.TOTAL, 0.0)
    }

    @Test
    fun emptyMetricsStayUnavailable() {
        val score = DerivativesScoringEngine.score(AggregatedDerivativesMetrics())
        assertNull(score.value)
        assertFalse(score.redistributed)
        assertTrue(score.components.none { it.used })
    }

    @Test
    fun crowdedPositiveFundingIsNegative() {
        val score = DerivativesScoringEngine.score(
            AggregatedDerivativesMetrics(fundingRate = 0.0005)
        )
        assertEquals(-100.0, score.value!!, 0.0001)
        assertTrue(score.redistributed)
        assertEquals(100.0, score.components.first { it.name == "funding" }.usedWeight, 0.0001)
    }

    @Test
    fun negativeFundingIsPositive() {
        val score = DerivativesScoringEngine.score(
            AggregatedDerivativesMetrics(fundingRate = -0.00025)
        )
        assertEquals(50.0, score.value!!, 0.0001)
    }

    @Test
    fun missingComponentsRedistributeProportionally() {
        val score = DerivativesScoringEngine.score(
            AggregatedDerivativesMetrics(
                fundingRate = 0.0005,
                takerBuySellRatio = 1.3
            )
        )
        val funding = score.components.first { it.name == "funding" }
        val taker = score.components.first { it.name == "taker" }
        assertTrue(score.redistributed)
        assertEquals(50.0, funding.usedWeight, 0.0001)
        assertEquals(50.0, taker.usedWeight, 0.0001)
        assertEquals(0.0, score.value!!, 0.0001)
    }

    @Test
    fun risingOpenInterestWithRisingPriceIsPositive() {
        val score = DerivativesScoringEngine.score(
            AggregatedDerivativesMetrics(
                change24hPct = 1.4,
                oiChange1hPct = 2.0
            )
        )
        assertEquals(100.0, score.value!!, 0.0001)
    }

    @Test
    fun risingOpenInterestWithFallingPriceIsNegative() {
        val score = DerivativesScoringEngine.score(
            AggregatedDerivativesMetrics(
                change24hPct = -1.4,
                oiChange1hPct = 2.0
            )
        )
        assertEquals(-100.0, score.value!!, 0.0001)
    }

    @Test
    fun liquidationImbalanceUsesShortMinusLong() {
        val score = DerivativesScoringEngine.score(
            AggregatedDerivativesMetrics(
                longLiqUsd = 25_000.0,
                shortLiqUsd = 75_000.0
            )
        )
        assertEquals(50.0, score.value!!, 0.0001)
    }

    @Test
    fun longShortCrowdingIsContrarian() {
        val score = DerivativesScoringEngine.score(
            AggregatedDerivativesMetrics(longShortRatio = 1.8)
        )
        assertEquals(-100.0, score.value!!, 0.0001)
    }

    @Test
    fun aggregatorSumsOpenInterestAndWeightsFunding() {
        val venues = listOf(
            VenueDerivativesSnapshot(
                venue = "binance",
                contract = "BTCUSDT",
                ok = true,
                fundingRate = 0.0002,
                openInterestUsd = 8_000_000_000.0
            ),
            VenueDerivativesSnapshot(
                venue = "okx",
                contract = "BTC-USDT-SWAP",
                ok = true,
                fundingRate = 0.0001,
                openInterestUsd = 2_000_000_000.0
            ),
            VenueDerivativesSnapshot(
                venue = "bybit",
                contract = "BTCUSDT",
                ok = false
            )
        )
        val snap = DerivativesAggregator.assemble("btc", venues, nowMs = 1_000L)
        assertEquals(10_000_000_000.0, snap.aggregated.openInterestUsd!!, 0.1)
        assertEquals(0.00018, snap.aggregated.fundingRate!!, 1e-9)
        assertEquals(2, snap.aggregated.venueCount)
        assertEquals("Binance & OKX", snap.sourceLabel)
        assertEquals(com.example.data.model.DerivativesFreshness.DEGRADED, snap.freshness)
        assertTrue(snap.score.value != null)
    }

    @Test
    fun neverInventAVenueField() {
        val venues = listOf(
            VenueDerivativesSnapshot(
                venue = "binance",
                contract = "BTCUSDT",
                ok = true,
                fundingRate = 0.0001,
                openInterestUsd = 1_000.0,
                missing = listOf("liquidations", "taker")
            )
        )
        val metrics = DerivativesAggregator.aggregate(venues)
        assertNull(metrics.takerBuySellRatio)
        assertNull(metrics.longLiqUsd)
        assertNull(metrics.shortLiqUsd)
        assertNull(metrics.longShortRatio)
    }
}
