package com.example

import com.example.data.repository.HistoricalMarketRepository
import com.example.ui.components.ChartTimeframe
import com.example.ui.components.HorizonChartBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HorizonChartBuilderTest {
    @Test
    fun weekClosesStayNearTheLivePriceAndDoNotInventAFuture() {
        val day = 86_400_000L
        val now = 1_727_000_000_000L
        val candles = (0..6).map { i ->
            HistoricalMarketRepository.Candle(now - (6 - i) * day, 110.0 + i)
        }
        val points = HorizonChartBuilder.fromCandles(candles, 116.4f)
        assertTrue(points.size >= 7)
        assertTrue(points.none { it.isFutureProjection })
        assertEquals(1f, points.last().normalizedX, 0.001f)
        assertEquals(116.4f, points.last().price, 0.01f)
        assertTrue(points.maxOf { it.price } < 130f)
        assertTrue(points.minOf { it.price } > 100f)
    }

    @Test
    fun weekLookbackIsSevenDaysNotAWholeCycle() {
        assertEquals(8, HorizonChartBuilder.daysFor(ChartTimeframe.WEEK_1))
        assertEquals(32, HorizonChartBuilder.daysFor(ChartTimeframe.MONTH_1))
    }
}
