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

    // Authentic normalized milestone trajectories for historical cycles
    // Anchored at Halving (Day 0) to Cycle Completion (~Day 850-1000)
    private val raw2016Milestones = listOf(
        Pair(0, 0.05f),
        Pair(45, 0.045f),
        Pair(90, 0.052f),
        Pair(135, 0.065f),
        Pair(180, 0.085f),
        Pair(210, 0.10f),
        Pair(240, 0.13f),
        Pair(270, 0.115f),
        Pair(300, 0.155f),
        Pair(330, 0.22f),
        Pair(360, 0.29f),
        Pair(390, 0.25f),
        Pair(420, 0.38f),
        Pair(450, 0.46f),
        Pair(480, 0.58f),
        Pair(510, 0.82f),
        Pair(525, 1.00f), // Peak ($19.8k)
        Pair(555, 0.58f),
        Pair(585, 0.44f),
        Pair(615, 0.52f),
        Pair(660, 0.38f),
        Pair(720, 0.33f),
        Pair(800, 0.24f),
        Pair(880, 0.155f), // Bottom ($3.1k)
        Pair(960, 0.22f),
        Pair(1050, 0.48f)
    )

    private val raw2020Milestones = listOf(
        Pair(0, 0.12f),
        Pair(30, 0.135f),
        Pair(60, 0.13f),
        Pair(90, 0.165f),
        Pair(120, 0.15f),
        Pair(150, 0.165f),
        Pair(180, 0.23f),
        Pair(210, 0.34f), // $20k breakout
        Pair(240, 0.55f), // Tesla $38k
        Pair(270, 0.71f),
        Pair(300, 0.81f),
        Pair(335, 0.94f), // Peak 1 ($64.8k)
        Pair(365, 0.55f), // China ban
        Pair(410, 0.43f), // Summer bottom ($29k)
        Pair(450, 0.63f),
        Pair(490, 0.80f),
        Pair(545, 1.00f), // Taproot ATH ($69k)
        Pair(600, 0.68f),
        Pair(660, 0.56f),
        Pair(720, 0.42f), // Terra Luna
        Pair(780, 0.28f), // Celsius/3AC
        Pair(840, 0.27f),
        Pair(925, 0.22f), // FTX Bottom ($15.5k)
        Pair(1020, 0.35f),
        Pair(1140, 0.43f),
        Pair(1300, 0.75f)
    )

    // Current Cycle Milestones (from Halving to Current matching day)
    private val rawCurrentMilestones = listOf(
        Pair(0, 0.38f),
        Pair(30, 0.37f),
        Pair(60, 0.41f),
        Pair(90, 0.35f),
        Pair(105, 0.32f), // Yen carry trade dip
        Pair(135, 0.37f),
        Pair(165, 0.40f),
        Pair(190, 0.45f),
        Pair(210, 0.51f) // Current point
    )

    // Projected Fractal Trajectory (continuing from Day 210)
    private val rawProjectedMilestones = listOf(
        Pair(210, 0.51f),
        Pair(240, 0.59f),
        Pair(270, 0.68f),
        Pair(300, 0.79f),
        Pair(340, 0.91f),
        Pair(385, 1.00f), // Projected Peak Zone ($168k target)
        Pair(430, 0.84f),
        Pair(480, 0.69f),
        Pair(540, 0.55f),
        Pair(620, 0.42f),
        Pair(720, 0.28f), // Projected Macro Floor Zone ($48k target)
        Pair(820, 0.34f)
    )

    /**
     * Interpolates between sparse milestone points to produce high-resolution,
     * silky-smooth financial points at standard intervals.
     */
    private fun interpolatePoints(
        milestones: List<Pair<Int, Float>>,
        priceScale: (Float) -> Double,
        stepDays: Int = 15
    ): List<FractalPoint> {
        if (milestones.isEmpty()) return emptyList()
        val result = mutableListOf<FractalPoint>()
        val startDay = milestones.first().first
        val endDay = milestones.last().first

        var currentDay = startDay
        while (currentDay <= endDay) {
            // Find bounding milestones
            val rightIndex = milestones.indexOfFirst { it.first >= currentDay }.let { if (it == -1) milestones.size - 1 else it }
            val leftIndex = (rightIndex - 1).coerceAtLeast(0)

            val left = milestones[leftIndex]
            val right = milestones[rightIndex]

            val t = if (right.first == left.first) 0f else (currentDay - left.first).toFloat() / (right.first - left.first).toFloat()
            // Smooth cosine s-curve interpolation for realistic market dynamics
            val smoothT = (1f - kotlin.math.cos(t * Math.PI.toFloat())) / 2f
            val normValue = left.second + (right.second - left.second) * smoothT

            result.add(
                FractalPoint(
                    day = currentDay,
                    normalizedValue = normValue.coerceIn(0.01f, 1.05f),
                    price = priceScale(normValue)
                )
            )
            currentDay += stepDays
        }
        return result
    }

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
