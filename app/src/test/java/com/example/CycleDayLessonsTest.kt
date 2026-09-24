package com.example

import com.example.util.CycleDayLessons
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleDayLessonsTest {

    @Test
    fun nearDay887SpeaksAsPastCycleHistory() {
        val lessons = CycleDayLessons.nearest(887)
        val lines = lessons.joinToString("\n") { CycleDayLessons.line(it, 887, greek = false) }
        assertTrue(lines.contains("2016"))
        assertTrue(lines.contains("days after") || lines.contains("days before") || lines.contains("this day-count"))
        assertFalse(lines.contains("will", ignoreCase = true))
        assertFalse(lines.contains("buy", ignoreCase = true))
        assertFalse(lines.contains("sell", ignoreCase = true))
        assertFalse(lines.contains("target", ignoreCase = true))
    }
}
