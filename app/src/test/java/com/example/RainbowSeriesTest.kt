package com.example

import com.example.ui.rainbow.PricePoint
import com.example.ui.rainbow.buildSeries
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RainbowSeriesTest {

    @Test
    fun livePointIsAppendedOnlyWhenThePrintIsReal() {
        val history = listOf(PricePoint(1, 100.0), PricePoint(2, 110.0))
        val live = buildSeries(history, live = 84_035.0, today = 3)
        assertEquals(3, live.size)
        assertEquals(84_035.0, live.last().price, 0.01)
        val offline = buildSeries(history, live = null, today = 3)
        assertEquals(2, offline.size)
        assertTrue(offline.none { it.day == 3L })
    }
}
