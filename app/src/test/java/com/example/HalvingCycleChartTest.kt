package com.example

import com.example.data.repository.HistoricalMarketRepository
import com.example.util.HalvingCycleUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HalvingCycleChartTest {
    @Test
    fun pastHalvingCyclesStayOnTheChartAndThisCycleStopsToday() {
        val day = 86_400_000L
        val now = HalvingCycleUtils.HALVING_4TH_TIMESTAMP + 800L * day
        val candles = mutableListOf<HistoricalMarketRepository.Candle>()
        var time = 1467331200000L
        var price = 600.0
        while (time <= now) {
            candles += HistoricalMarketRepository.Candle(time, price)
            time += day
            price += 8.0
        }

        val data = HistoricalMarketRepository.buildHalvingOverlay("BTC", candles, now)
        assertTrue(data != null)
        data!!
        assertTrue(data.projectedPoints.isEmpty())
        assertTrue(data.points2016.size > 10)
        assertTrue(data.points2020.size > 10)
        assertTrue(data.currentPoints.size > 10)
        assertTrue(data.points2020.last().day > data.currentDay)
        assertTrue(data.currentPoints.last().day <= data.currentDay + 1)
        assertEquals(
            ((HalvingCycleUtils.HALVING_5TH_TIMESTAMP - HalvingCycleUtils.HALVING_4TH_TIMESTAMP) / day).toInt(),
            data.axisDays
        )
        assertEquals(800, data.currentDay)
        assertEquals(candles.last().close, data.currentPoints.last().price, 0.01)
    }
}
