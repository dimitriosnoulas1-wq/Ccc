package com.example

import com.example.util.CycleSameDayNote
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleSameDayNoteTest {

    @Test
    fun liveDayAndPastClosesAppearWithoutAForecast() {
        val lines = CycleSameDayNote.paragraphs(
            day = 887,
            priceUsd = 84_170.0,
            priceIsLive = true,
            close2012 = 240.0,
            close2016 = 3_700.0,
            close2020 = 19_400.0,
            multiple2012 = 19.4,
            multiple2016 = 5.0,
            multiple2020 = 2.22,
            greek = false
        ).joinToString("\n")
        assertTrue(lines.contains("Day 887") || lines.contains("day 887"))
        assertTrue(lines.contains("84,170") || lines.contains("84170"))
        assertTrue(lines.contains("2012"))
        assertTrue(lines.contains("2016"))
        assertTrue(lines.contains("2020"))
        assertTrue(lines.contains("live"))
        assertFalse(lines.contains("will", ignoreCase = true))
        assertFalse(lines.contains("buy", ignoreCase = true))
        assertFalse(lines.contains("sell", ignoreCase = true))
        assertFalse(lines.contains("target", ignoreCase = true))
    }

    @Test
    fun missingLivePriceShowsADash() {
        val lines = CycleSameDayNote.paragraphs(
            day = 887,
            priceUsd = 0.0,
            priceIsLive = false,
            close2012 = null,
            close2016 = null,
            close2020 = null,
            multiple2012 = null,
            multiple2016 = null,
            multiple2020 = null,
            greek = false
        ).joinToString("\n")
        assertTrue(lines.contains("—"))
        assertFalse(lines.contains("will", ignoreCase = true))
    }
}
