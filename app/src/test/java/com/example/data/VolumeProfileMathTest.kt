package com.example.data

import com.example.data.repository.VolumeCandle
import com.example.data.repository.VolumeProfileMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VolumeProfileMathTest {

    @Test
    fun pocSitsWhereMostVolumeTraded() {
        val candles = listOf(
            VolumeCandle(high = 110.0, low = 100.0, close = 105.0, quoteVolume = 100.0, takerBuyQuote = 50.0),
            VolumeCandle(high = 106.0, low = 104.0, close = 105.0, quoteVolume = 1_000.0, takerBuyQuote = 700.0),
            VolumeCandle(high = 102.0, low = 100.0, close = 101.0, quoteVolume = 50.0, takerBuyQuote = 10.0)
        )
        val p = VolumeProfileMath.build(candles, binCount = 10)!!
        assertTrue(p.poc in 104.0..106.0)
        assertEquals(1_150.0, p.totalVolume, 1e-6)
        assertEquals(760.0, p.bins.sumOf { it.buyVolume }, 1e-6)
    }

    @Test
    fun valueAreaHoldsAtLeastSeventyPercentAndContainsPoc() {
        val candles = (0 until 50).map { i ->
            val base = 100.0 + (i % 10)
            VolumeCandle(base + 2, base, base + 1, quoteVolume = 10.0 + i, takerBuyQuote = 5.0)
        }
        val p = VolumeProfileMath.build(candles, binCount = 24)!!
        val inside = p.bins.filterIndexed { i, _ -> i in p.vaLowIndex..p.vaHighIndex }.sumOf { it.volume }
        assertTrue(inside >= p.totalVolume * VolumeProfileMath.VALUE_AREA_SHARE - 1e-6)
        assertTrue(p.poc in p.valueAreaLow..p.valueAreaHigh)
        assertTrue(p.valueAreaLow < p.valueAreaHigh)
    }

    @Test
    fun parsesBinanceKlinesWithDivisor() {
        val body = """[[1,"0","2000","1000","1500","10","2","15000","3","4","6000","0"]]"""
        val c = VolumeProfileMath.parseKlines(body, divisor = 1000.0).single()
        assertEquals(2.0, c.high, 1e-9)
        assertEquals(1.0, c.low, 1e-9)
        assertEquals(1.5, c.close, 1e-9)
        assertEquals(15_000.0, c.quoteVolume, 1e-9)
        assertEquals(6_000.0, c.takerBuyQuote, 1e-9)
    }

    @Test
    fun noProfileWithoutData() {
        assertNull(VolumeProfileMath.build(emptyList()))
    }
}
