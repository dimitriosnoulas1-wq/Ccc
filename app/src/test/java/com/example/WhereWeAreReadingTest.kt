package com.example

import com.example.util.WhereWeAreReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhereWeAreReadingTest {

    @Test
    fun liveMultipleUsesHalvingCloseAndDashesWhenPriceIsOffline() {
        val live = WhereWeAreReading.liveMultiple(83_535.0, priceIsLive = true)
        assertTrue(live != null && live > 1.2 && live < 1.4)
        assertEquals(null, WhereWeAreReading.liveMultiple(83_535.0, priceIsLive = false))
    }

    @Test
    fun cardNamesPositionNotATradeOrATop() {
        val view = WhereWeAreReading.build(
            day = 887,
            priceUsd = 83_535.0,
            priceIsLive = true,
            multiple2012 = 19.4,
            multiple2016 = 5.0,
            multiple2020 = 2.22,
            fundingRate = 0.0001,
            openInterestUsd = 5_400_000_000.0,
            liquidationUsd = 12_000_000.0
        )
        val text = listOf(
            view.dayText,
            view.nowMultiple,
            view.multiple2012,
            view.multiple2016,
            view.multiple2020,
            view.tape.fundingPct,
            view.familyEn,
            view.familyEl
        ).joinToString(" ")
        assertEquals("887", view.dayText)
        assertTrue(view.nowMultiple.contains("x"))
        assertEquals("19.40x", view.multiple2012)
        assertFalse(text.contains("buy", ignoreCase = true))
        assertFalse(text.contains("sell", ignoreCase = true))
        assertFalse(text.contains("target", ignoreCase = true))
        assertFalse(text.contains("unwind", ignoreCase = true))
        assertEquals("Live", view.familyEn)
    }

    @Test
    fun missingTapeIsADash() {
        val view = WhereWeAreReading.build(
            day = null,
            priceUsd = 0.0,
            priceIsLive = false,
            multiple2012 = null,
            multiple2016 = null,
            multiple2020 = null,
            fundingRate = null,
            openInterestUsd = null,
            liquidationUsd = null
        )
        assertEquals("—", view.dayText)
        assertEquals("—", view.nowMultiple)
        assertEquals("—", view.tape.fundingPct)
        assertEquals("—", view.tape.openInterest)
        assertEquals("—", view.tape.liquidations)
        assertEquals("No data", view.familyEn)
        assertEquals("Χωρίς δεδομένα", view.familyEl)
    }
}
