package com.example.ui.rainbow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RainbowVisualsTest {

    private val day = DateUtil.epochDay(2026, 9, 25)

    @Test fun positionIsZeroToOneAndMonotonic() {
        assertEquals(0.0, RainbowVisuals.position(day, 0.01), 1e-12)
        assertEquals(1.0, RainbowVisuals.position(day, 1e12), 1e-12)
        var last = -1.0
        var p = 1_000.0
        while (p < 10_000_000.0) {
            val v = RainbowVisuals.position(day, p)
            assertTrue(v >= last)
            last = v
            p *= 1.1
        }
    }

    @Test fun positionMatchesBandBoundaries() {
        val bottom = RainbowModel.edgePrice(day.toDouble(), 0)
        val top = RainbowModel.edgePrice(day.toDouble(), RainbowModel.EDGES.lastIndex)
        assertEquals(0.0, RainbowVisuals.position(day, bottom), 1e-9)
        assertEquals(1.0, RainbowVisuals.position(day, top), 1e-9)
    }

    @Test fun bandRunsCoverAllPointsWithoutGaps() {
        val lo = RainbowModel.edgePrice(day.toDouble(), 2)
        val pts = listOf(
            PricePoint(day, lo * 0.9), PricePoint(day, lo * 0.95),   // ζώνη 1
            PricePoint(day, lo * 1.05), PricePoint(day, lo * 1.1),   // ζώνη 2
            PricePoint(day, lo * 0.9),                               // ζώνη 1
        )
        val runs = RainbowVisuals.bandRuns(pts)
        assertEquals(listOf(1, 2, 1), runs.map { it.first })
        // κάθε κομμάτι ξεκινά εκεί που τελείωσε το προηγούμενο
        for (i in 1 until runs.size) assertEquals(runs[i - 1].second.last(), runs[i].second.first())
        assertEquals(pts.toSet(), runs.flatMap { it.second }.toSet())
        assertTrue(RainbowVisuals.bandRuns(emptyList()).isEmpty())
    }

    @Test fun ghostAlignsHalvingsAndStopsAtToday() {
        val h16 = RainbowModel.HALVINGS[1]
        val h24 = RainbowModel.HALVINGS[3]
        val series = (h16.day..h16.day + 1500).map { PricePoint(it, 100.0 + (it - h16.day)) } +
            (h24.day..h24.day + 900).map { PricePoint(it, 60_000.0) }
        val n = 888L
        val g = RainbowVisuals.ghost(series, h16, h24, n)
        assertEquals((n + 1).toInt(), g.size)
        assertEquals(h24.day, g.first().day)
        assertEquals(h24.day + n, g.last().day)
        assertEquals(60_000.0, g.first().price, 1e-9)          // ίδια τιμή στο halving
        assertEquals((100.0 + n) * 600.0, g.last().price, 1e-6) // ίδια σχετική κίνηση
        assertTrue(RainbowVisuals.ghost(series, h24, h24, n).isEmpty())
    }

    @Test fun lerpEnds() {
        assertEquals(10.0, RainbowVisuals.lerp(10.0, 20.0, 0f), 0.0)
        assertEquals(20.0, RainbowVisuals.lerp(10.0, 20.0, 1f), 0.0)
        assertEquals(15.0, RainbowVisuals.lerp(10.0, 20.0, 0.5f), 1e-9)
    }
}
