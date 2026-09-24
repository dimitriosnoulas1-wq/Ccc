package com.example

import com.example.util.CycleDayLessons
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleDayLessonsTest {

    @Test
    fun nearDay887SpeaksAsPastCycleHistory() {
        val lines = CycleDayLessons.paragraphs(887, greek = false).joinToString("\n")
        assertTrue(lines.contains("Day 887"))
        assertTrue(lines.contains("2016"))
        assertTrue(lines.contains("Tomorrow the count moves"))
        assertFalse(lines.contains("will", ignoreCase = true))
        assertFalse(lines.contains("buy", ignoreCase = true))
        assertFalse(lines.contains("sell", ignoreCase = true))
        assertFalse(lines.contains("target", ignoreCase = true))
    }

    @Test
    fun ftxWeekNamesSolAsHistory() {
        val ftx = CycleDayLessons.nearest(900).first { it.textEn.contains("FTX") }
        val lines = CycleDayLessons.paragraphs(ftx.day, greek = false).joinToString("\n")
        assertTrue(lines.contains("SOL"))
        assertFalse(lines.contains("will", ignoreCase = true))
    }
}
