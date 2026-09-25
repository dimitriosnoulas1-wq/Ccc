package com.example

import com.example.util.HalvingCycleUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class HalvingTimestampsTest {

    @Test
    fun twentyTwentyHalvingMatchesBlock630000() {
        assertEquals(1_589_225_023_000L, HalvingCycleUtils.HALVING_2020_TIMESTAMP)
        assertEquals(1_354_116_278_000L, HalvingCycleUtils.HALVING_2012_TIMESTAMP)
        assertEquals(1_468_082_773_000L, HalvingCycleUtils.HALVING_2016_TIMESTAMP)
        assertEquals(1_713_571_740_000L, HalvingCycleUtils.HALVING_4TH_TIMESTAMP)
    }

    @Test
    fun utcDayCountOnTheAuditScreenshotDateIs888() {
        val shot = utc(2026, 9, 25, 12, 17)
        assertEquals(
            888,
            HalvingCycleUtils.utcCalendarDaysSince(HalvingCycleUtils.HALVING_4TH_TIMESTAMP, shot)
        )
    }

    @Test
    fun sameCycleDay888LandsOnTheAuditedDates() {
        assertEquals(
            utc(2015, 5, 5),
            HalvingCycleUtils.utcDatePlusDays(HalvingCycleUtils.HALVING_2012_TIMESTAMP, 888)
        )
        assertEquals(
            utc(2018, 12, 14),
            HalvingCycleUtils.utcDatePlusDays(HalvingCycleUtils.HALVING_2016_TIMESTAMP, 888)
        )
        assertEquals(
            utc(2022, 10, 16),
            HalvingCycleUtils.utcDatePlusDays(HalvingCycleUtils.HALVING_2020_TIMESTAMP, 888)
        )
    }

    private fun utc(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(year, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
