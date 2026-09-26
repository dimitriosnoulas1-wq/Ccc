package com.example.ui.rainbow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Οι αναμενόμενες τιμές για τις ημερομηνίες υπολογίστηκαν ανεξάρτητα (Python datetime). */
class RainbowLogicTest {

    private fun d(y: Int, m: Int, dd: Int) = DateUtil.epochDay(y, m, dd)

    // ── Ημερομηνίες ──
    @Test fun epochDayMatchesKnownValues() {
        assertEquals(0L, d(1970, 1, 1))
        assertEquals(19833L, d(2024, 4, 20))
        assertEquals(15672L, d(2012, 11, 28))
    }

    @Test fun civilRoundTripsEveryDayFrom2009To2035() {
        var day = d(2009, 1, 1)
        val end = d(2035, 12, 31)
        while (day <= end) {
            val (y, m, dd) = DateUtil.civil(day)
            assertEquals(day, d(y, m, dd))
            day++
        }
    }

    @Test fun formatIsReadable() {
        assertEquals("8 Nov 2022", DateUtil.format(d(2022, 11, 8)))
        assertEquals("8 Νοε 2022", DateUtil.format(d(2022, 11, 8), greek = true))
    }

    // ── Halvings / μέτρηση ημερών ──
    @Test fun halvingDatesAreTheRealOnes() {
        val real = RainbowModel.HALVINGS.filter { !it.estimated }.map { DateUtil.civil(it.day) }
        assertEquals(
            listOf(Triple(2012, 11, 28), Triple(2016, 7, 9), Triple(2020, 5, 11), Triple(2024, 4, 20)),
            real
        )
        assertTrue(RainbowModel.HALVINGS.filter { it.estimated }.map { it.year } == listOf(2028, 2032))
    }

    @Test fun dayCountSinceLastHalving() {
        assertEquals(888L, RainbowModel.daysSinceHalving(d(2026, 9, 25)))
        assertEquals(0L, RainbowModel.daysSinceHalving(d(2024, 4, 20)))
        assertEquals(2020, RainbowModel.currentHalving(d(2024, 4, 19)).year)
    }

    @Test fun sameDayInPreviousCycles() {
        val today = d(2026, 9, 25)
        val series = (d(2012, 1, 1)..today).map { PricePoint(it, 1000.0) }
        val days = CycleInsights.compute(series, today).map { DateUtil.civil(it.day) }
        assertEquals(
            listOf(Triple(2015, 5, 5), Triple(2018, 12, 14), Triple(2022, 10, 16), Triple(2026, 9, 25)),
            days
        )
    }

    // ── Ζώνες ──
    @Test fun nineBandsWithOfficialNamesBottomToTop() {
        assertEquals(9, RainbowModel.BANDS.size)
        assertEquals(10, RainbowModel.EDGES.size)
        assertEquals("Lowest Band", RainbowModel.BANDS.first().name)
        assertEquals("Maximum Bubble", RainbowModel.BANDS.last().name)
        RainbowModel.BANDS.forEach { band ->
            assertFalse(band.name.contains("Buy", ignoreCase = true))
            assertFalse(band.name.contains("Sell", ignoreCase = true))
        }
    }

    @Test fun bandIndexIsMonotonicInPrice() {
        val day = d(2026, 9, 25)
        var last = -1
        var p = 100.0
        while (p < 50_000_000.0) {
            val b = RainbowModel.bandIndex(day, p)
            assertTrue(b >= last)
            last = b
            p *= 1.05
        }
        assertEquals(0, RainbowModel.bandIndex(day, 1.0))
        assertEquals(8, RainbowModel.bandIndex(day, 1e9))
    }

    @Test fun priceInsideEachBandMapsToThatBand() {
        val day = d(2025, 1, 1)
        for (i in RainbowModel.BANDS.indices) {
            val lo = RainbowModel.edgePrice(day.toDouble(), i)
            val hi = RainbowModel.edgePrice(day.toDouble(), i + 1)
            assertEquals(i, RainbowModel.bandIndex(day, kotlin.math.sqrt(lo * hi)))
        }
    }

    // ── Γεγονότα ──
    @Test fun eventsAreSortedDatedAndInsideACycle() {
        val ev = RainbowEvents.ALL
        assertTrue(ev.isNotEmpty())
        assertEquals(ev.sortedBy { it.day }, ev)
        ev.forEach {
            assertNotNull("event before first halving: ${it.en}", RainbowModel.cycleOf(it.day))
            assertTrue(it.el.isNotBlank() && it.en.isNotBlank())
            // Καμία τιμή στο κείμενο πέρα από σταθερά ορόσημα (όλα τα νούμερα έρχονται από δεδομένα)
        }
    }

    @Test fun eventDayCountsMatchIndependentCalculation() {
        fun dayOf(y: Int, m: Int, dd: Int): Long {
            val e = RainbowEvents.ALL.first { it.day == d(y, m, dd) }
            return e.day - RainbowModel.cycleOf(e.day)!!.day
        }
        assertEquals(911L, dayOf(2022, 11, 8))   // FTX, κύκλος 2020
        assertEquals(1342L, dayOf(2020, 3, 12))  // COVID, κύκλος 2016
        assertEquals(728L, dayOf(2022, 5, 9))    // LUNA, κύκλος 2020
    }

    // ── Ανάλυση ──
    private fun synthetic(from: Long, to: Long, f: (Long) -> Double) =
        (from..to).map { PricePoint(it, f(it)) }

    @Test fun extremesAreFoundFromData() {
        val h = RainbowModel.HALVINGS[1] // 2016
        val top = h.day + 500
        val bottom = h.day + 900
        val s = synthetic(h.day - 10, RainbowModel.HALVINGS[2].day + 10) {
            when (it) { top -> 20_000.0; bottom -> 3_000.0; else -> 8_000.0 }
        }
        val (hi, lo) = CycleInsights.extremes(s, h, d(2026, 1, 1))
        assertEquals(top, hi!!.point.day)
        assertTrue(hi.final)
        assertEquals(bottom, lo!!.point.day)
    }

    @Test fun currentCycleHighIsNotFinal() {
        val h = RainbowModel.HALVINGS[3] // 2024
        val today = h.day + 300
        val s = synthetic(h.day, today) { 50_000.0 + it - h.day }
        val (hi, _) = CycleInsights.extremes(s, h, today)
        assertFalse(hi!!.final)
        assertEquals(today, hi.point.day)
    }

    @Test fun insightsUseSameDayAndNearbyWindow() {
        val today = d(2026, 9, 25)
        val s = synthetic(d(2012, 1, 1), today) { 1000.0 }
        val ins = CycleInsights.compute(s, today)
        assertEquals(listOf(2012, 2016, 2020, 2024), ins.map { it.halving.year })
        val c2020 = ins[2]
        assertEquals(d(2022, 10, 16), c2020.day)
        // FTX (8 Nov 2022) είναι 23 μέρες μετά
        val ftx = c2020.nearby.first { it.day == d(2022, 11, 8) }
        assertEquals(23L, ftx.offset)
        c2020.nearby.forEach { assertTrue(kotlin.math.abs(it.offset) <= CycleInsights.NEAR_DAYS) }
        assertTrue(ins.last().isCurrent)
        assertNull(ins.last().change30After)
        assertEquals(1.0, c2020.change30Before!!, 1e-9)
    }

    @Test fun offsetWording() {
        assertEquals("Την ίδια μέρα", CycleInsights.offsetText(0, true))
        assertEquals("1 μέρα πριν", CycleInsights.offsetText(-1, true))
        assertEquals("23 days after", CycleInsights.offsetText(23, false))
    }

    // ── Ενημέρωση 1 φορά τη μέρα / μεγάλη κίνηση ──
    @Test fun liveGateRules() {
        val today = d(2026, 9, 25)
        val shown = PricePoint(today, 100_000.0)
        assertEquals(LiveGate.Reason.NEW_DAY, LiveGate.reason(null, 100_000.0, today))
        assertEquals(LiveGate.Reason.NEW_DAY, LiveGate.reason(shown.copy(day = today - 1), 100_000.0, today))
        assertNull(LiveGate.reason(shown, 106_000.0, today).takeIf { it == LiveGate.Reason.BIG_MOVE })
        assertEquals(LiveGate.Reason.BIG_MOVE, LiveGate.reason(shown, 107_500.0, today))
        assertEquals(LiveGate.Reason.BIG_MOVE, LiveGate.reason(shown, 92_000.0, today))
        assertNull(LiveGate.reason(shown, 0.0, today))
    }

    @Test fun liveGateBandChange() {
        val today = d(2026, 9, 25)
        // Τιμή λίγο κάτω από όριο ζώνης, και λίγο πάνω (<7% διαφορά)
        val edge = RainbowModel.edgePrice(today.toDouble(), 4)
        val shown = PricePoint(today, edge * 0.99)
        assertEquals(LiveGate.Reason.BAND_CHANGE, LiveGate.reason(shown, edge * 1.01, today))
        assertNull(LiveGate.reason(shown, edge * 0.985, today))
    }

    // ── Parsing / cache ──
    @Test fun parsesBlockchainHistory() {
        val json = """{"status":"ok","values":[{"x":1230940800,"y":0.0},{"x":1279584000,"y":0.0858},""" +
            """{"x":1279670400,"y":0.0808},{"x":1279670400,"y":0.09}]}"""
        val h = RainbowParse.history(json)
        assertEquals(2, h.size)
        assertEquals(1279584000L / 86400, h[0].day)
        assertEquals(0.09, h[1].price, 1e-12) // διπλή μέρα → κρατάμε την τελευταία
    }

    @Test fun parsesCoinbaseSpot() {
        assertEquals(83746.12, RainbowParse.spot("""{"data":{"amount":"83746.12","base":"BTC","currency":"USD"}}""")!!, 1e-9)
        assertNull(RainbowParse.spot("""{"errors":[]}"""))
    }

    @Test fun cacheRoundTrips() {
        val pts = listOf(PricePoint(100, 0.05), PricePoint(101, 123456.789))
        assertEquals(pts, RainbowParse.decodeHistory(RainbowParse.encodeHistory(pts)))
        val st = RainbowCacheState(20000, PricePoint(20000, 99999.5))
        assertEquals(st, RainbowCacheState.decode(st.encode()))
        assertEquals(RainbowCacheState(5, null), RainbowCacheState.decode(RainbowCacheState(5, null).encode()))
    }

    @Test fun seriesUsesShownPointOnlyForToday() {
        val today = 20000L
        val h = listOf(PricePoint(today - 2, 1.0), PricePoint(today - 1, 2.0), PricePoint(today, 3.0))
        assertEquals(listOf(1.0, 2.0, 9.0), buildSeries(h, PricePoint(today, 9.0), today).map { it.price })
        assertEquals(listOf(1.0, 2.0), buildSeries(h, PricePoint(today - 1, 9.0), today).map { it.price })
    }
}
