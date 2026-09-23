package com.example

import com.example.data.model.DailyCycleLogEntry
import com.example.data.repository.upsertDailyLog
import com.example.util.CycleReadingText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleReadingTextTest {
    @Test
    fun alertStatesBothPastClosesAndSaysWhenOneIsMissing() {
        assertEquals(
            "Day 900. In 2016 this day-count was 6.00x, in 2020 it was 3.00x.",
            CycleReadingText.alertLine(900, 6.0, 3.0, greek = false)
        )
        assertEquals(
            "Ημέρα 900. Το 2016 αυτή η μέρα ήταν 6.00x. Το κλείσιμο του 2020 λείπει.",
            CycleReadingText.alertLine(900, 6.0, null, greek = true)
        )
        assertTrue(CycleReadingText.band(1.2, null, null, greek = false).contains("No 2016"))
        assertEquals("Between the 2016 and 2020 cycles.", CycleReadingText.band(4.0, 6.0, 2.0, greek = false))
        assertEquals("Above both past cycles.", CycleReadingText.band(7.0, 6.0, 3.0, greek = false))
    }

    @Test
    fun dailyLogKeepsOneRowPerDayAndReplacesThatDay() {
        val first = entry("2026-09-22", 1.1)
        val second = entry("2026-09-23", 1.2)
        val updated = entry("2026-09-23", 1.4)
        val stored = upsertDailyLog(listOf(second, first), updated)
        assertEquals(listOf("2026-09-23", "2026-09-22"), stored.map { it.dateKey })
        assertEquals(1.4, stored.first().multiple!!, 0.001)
    }

    private fun entry(day: String, multiple: Double) = DailyCycleLogEntry(
        dateKey = day,
        displayDate = day,
        cycleDay = 800,
        axisDays = 1458,
        multiple = multiple,
        multiple2016 = 5.0,
        multiple2020 = 2.0,
        priceUsd = 80_000.0
    )
}
