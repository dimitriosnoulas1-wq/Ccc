package com.example

import com.example.ui.rainbow.DateUtil
import com.example.ui.rainbow.RainbowModel
import com.example.util.HalvingCycleUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/** "Where we are" and the Rainbow cycle cards must name the same halving day. */
class HalvingDayCountTest {

    private val dayMs = 86_400_000L

    @Test
    fun whereWeAreAndRainbowAgreeRightAfterMidnight() {
        // 00:02 UTC on 27 Sep 2026: the halving was at 00:09 UTC, so elapsed 24h periods
        // would still say 889 while the calendar (and the Rainbow) says 890.
        val today = DateUtil.epochDay(2026, 9, 27)
        val now = today * dayMs + 2 * 60_000L
        val rainbow = RainbowModel.daysSinceHalving(today)
        assertEquals(890L, rainbow)
        assertEquals(rainbow, HalvingCycleUtils.calendarDaysBetween(HalvingCycleUtils.HALVING_4TH_TIMESTAMP, now).toLong())
    }

    @Test
    fun agreeAcrossAWholeDay() {
        val today = DateUtil.epochDay(2026, 9, 27)
        for (minute in 0 until 24 * 60 step 7) {
            val now = today * dayMs + minute * 60_000L
            assertEquals(
                RainbowModel.daysSinceHalving(today),
                HalvingCycleUtils.calendarDaysBetween(HalvingCycleUtils.HALVING_4TH_TIMESTAMP, now).toLong()
            )
        }
    }

    @Test
    fun halvingDayIsDayZero() {
        val halvingDay = DateUtil.epochDay(2024, 4, 20)
        assertEquals(0, HalvingCycleUtils.calendarDaysBetween(HalvingCycleUtils.HALVING_4TH_TIMESTAMP, halvingDay * dayMs))
        assertEquals(1, HalvingCycleUtils.calendarDaysBetween(HalvingCycleUtils.HALVING_4TH_TIMESTAMP, (halvingDay + 1) * dayMs))
    }
}
