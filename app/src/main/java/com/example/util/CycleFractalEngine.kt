package com.example.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.roundToInt

data class FractalPoint(
    val day: Int,
    val normalizedValue: Float, // 0.0f (cycle floor) to 1.0f (cycle peak)
    val price: Double,
    val phaseTag: String = ""
)

data class CycleFractalData(
    val currentPoints: List<FractalPoint>,
    val projectedPoints: List<FractalPoint>,
    val projectedBandUpper: List<FractalPoint>,
    val projectedBandLower: List<FractalPoint>,
    val points2020: List<FractalPoint>,
    val points2016: List<FractalPoint>,
    val correlationScore2020: Double = 0.0,
    val correlationScore2016: Double = 0.0,
    val currentDay: Int = 210,
    val peakDay: Int = 390,
    val floorDay: Int = 720,
    val pastCycleLabel: String = "Past cycle",
    val earlierCycleLabel: String = "Earlier cycle",
    val usesHalving: Boolean = false,
    val eventDays: List<Pair<Int, String>> = emptyList(),
    val windowLabel: String = "",
    val axisDays: Int = 800,
    val multipleNow: Double? = null,
    val multiple2012: Double? = null,
    val multiple2016: Double? = null,
    val multiple2020: Double? = null,
    val close2012: Double? = null,
    val close2016: Double? = null,
    val close2020: Double? = null
)

object CycleFractalEngine {

    /**
     * Builds a smooth Bézier cubic spline Path through a sequence of canvas Offsets.
     * Prevents harsh angular elbow joints and renders organic financial waves.
     */
    fun buildSmoothSplinePath(offsets: List<Offset>, path: Path) {
        if (offsets.isEmpty()) return
        path.moveTo(offsets.first().x, offsets.first().y)
        if (offsets.size == 1) return

        for (i in 1 until offsets.size) {
            val prev = offsets[i - 1]
            val curr = offsets[i]
            val midX = (prev.x + curr.x) / 2f
            path.cubicTo(
                midX, prev.y,
                midX, curr.y,
                curr.x, curr.y
            )
        }
    }
}
