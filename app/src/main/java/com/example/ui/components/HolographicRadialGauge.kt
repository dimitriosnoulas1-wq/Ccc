package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.PhotonGoldBright
import com.example.ui.theme.QuantumBlue
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.QuantumCyanBright
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.TextCyanSlate
import com.example.ui.theme.TextPureWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ultra-Futuristic Holographic Radial Gauge (Year 3026 Terminal Aesthetic).
 *
 * Features:
 * 1. Glowing segmented prismatic arc using SweepGradient with track ambient glow.
 * 2. Animated indicator needle/orb using `animateFloatAsState` with continuous quantum pulse.
 * 3. Subtle glowing laser tick marks along the arc edges (major + minor ticks + status notches).
 * 4. Centered sharp high-contrast typography readout with holographic color sync.
 */
@Composable
fun HolographicRadialGauge(
    value: Float,
    minValue: Float = 0f,
    maxValue: Float = 100f,
    modifier: Modifier = Modifier,
    height: Dp = 155.dp,
    statusLabel: String = "",
    startLabel: String = "0",
    endLabel: String = "100",
    gaugeColors: List<Pair<Float, Color>> = listOf(
        0.0f to SoftCrimson,
        0.3f to PhotonGold,
        0.65f to QuantumCyan,
        1.0f to SoftEmerald
    ),
    activeColor: Color = QuantumCyan,
    showTicks: Boolean = true,
    animationDurationMillis: Int = 1000,
    valueLabel: String? = null
) {
    val normalizedValue = ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = normalizedValue,
        animationSpec = tween(durationMillis = animationDurationMillis),
        label = "radial_gauge_progress"
    )

    val trackColorsArray = remember(gaugeColors) {
        gaugeColors.map { (stop, col) -> stop to col.copy(alpha = 0.22f) }.toTypedArray()
    }
    val outerBorderColorsArray = remember(gaugeColors) {
        gaugeColors.map { (stop, col) -> stop to col.copy(alpha = 0.45f) }.toTypedArray()
    }
    val horizontalColorsList = remember(gaugeColors) {
        gaugeColors.map { it.second }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val heightPx = size.height
            val strokeWidth = 14.dp.toPx()
            val radius = (width.coerceAtMost(heightPx * 2.2f) / 2.25f)
            val centerOffset = Offset(width / 2f, heightPx * 0.88f)

            val arcTopLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)

            // 1. Subtle Radial Background Halo Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = radius * 1.3f
                ),
                radius = radius * 1.3f,
                center = centerOffset
            )

            // 2. Track Background Arc (Dimmest baseline 180° arc)
            drawArc(
                brush = Brush.sweepGradient(
                    *trackColorsArray,
                    center = centerOffset
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 3. Active Luminous Prismatic Fill Arc (180° * animatedProgress)
            val currentSweep = (animatedProgress * 180f).coerceIn(1f, 180f)
            drawArc(
                brush = Brush.horizontalGradient(
                    colors = horizontalColorsList,
                    startX = centerOffset.x - radius,
                    endX = centerOffset.x + radius
                ),
                startAngle = 180f,
                sweepAngle = currentSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 4. Hairline Neon Outer Border Arc
            drawArc(
                brush = Brush.sweepGradient(
                    *outerBorderColorsArray,
                    center = centerOffset
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centerOffset.x - (radius + strokeWidth * 0.7f), centerOffset.y - (radius + strokeWidth * 0.7f)),
                size = Size((radius + strokeWidth * 0.7f) * 2f, (radius + strokeWidth * 0.7f) * 2f),
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. Glowing Laser Tick Marks (Year 3026 Terminal Notches)
            if (showTicks) {
                val totalTicks = 28
                val innerTickRadius = radius + strokeWidth * 0.9f
                for (i in 0..totalTicks) {
                    val tickFraction = i.toFloat() / totalTicks
                    val isMajor = i % 7 == 0
                    val isPassed = tickFraction <= animatedProgress

                    val angleDeg = 180.0 + tickFraction * 180.0
                    val angleRad = Math.toRadians(angleDeg)

                    val tickLen = if (isMajor) 7.dp.toPx() else 4.dp.toPx()
                    val outerTickRadius = innerTickRadius + tickLen

                    val startX = centerOffset.x + innerTickRadius * cos(angleRad).toFloat()
                    val startY = centerOffset.y + innerTickRadius * sin(angleRad).toFloat()
                    val endX = centerOffset.x + outerTickRadius * cos(angleRad).toFloat()
                    val endY = centerOffset.y + outerTickRadius * sin(angleRad).toFloat()

                    val tickColor = when {
                        isPassed -> activeColor.copy(alpha = if (isMajor) 0.9f else 0.6f)
                        isMajor -> Color(0x6688A2B8)
                        else -> Color(0x2B88A2B8)
                    }

                    drawLine(
                        color = tickColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 6. Indicator Needles / Laser Orb at Needle Tip
            val indicatorAngleDeg = 180.0 + (animatedProgress * 180.0)
            val indicatorAngleRad = Math.toRadians(indicatorAngleDeg)

            val indicatorX = centerOffset.x + radius * cos(indicatorAngleRad).toFloat()
            val indicatorY = centerOffset.y + radius * sin(indicatorAngleRad).toFloat()

            // Concentric Holographic Laser Orb
            drawCircle(
                color = activeColor.copy(alpha = 0.35f),
                radius = 12.dp.toPx(),
                center = Offset(indicatorX, indicatorY)
            )
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(indicatorX, indicatorY)
            )
            drawCircle(
                color = activeColor,
                radius = 3.5.dp.toPx(),
                center = Offset(indicatorX, indicatorY)
            )

            // Inner Needle Hairline Ray from Hub
            val hubRadius = 16.dp.toPx()
            val needleTipRadius = radius - strokeWidth * 0.8f
            val rayStartX = centerOffset.x + hubRadius * cos(indicatorAngleRad).toFloat()
            val rayStartY = centerOffset.y + hubRadius * sin(indicatorAngleRad).toFloat()
            val rayEndX = centerOffset.x + needleTipRadius * cos(indicatorAngleRad).toFloat()
            val rayEndY = centerOffset.y + needleTipRadius * sin(indicatorAngleRad).toFloat()

            drawLine(
                brush = Brush.linearGradient(
                    listOf(activeColor.copy(alpha = 0.1f), activeColor.copy(alpha = 0.85f)),
                    start = Offset(rayStartX, rayStartY),
                    end = Offset(rayEndX, rayEndY)
                ),
                start = Offset(rayStartX, rayStartY),
                end = Offset(rayEndX, rayEndY),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Center High-Contrast Holographic Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 22.dp)
        ) {
            Text(
                text = valueLabel ?: "${value.toInt()}",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                color = TextPureWhite
            )
            if (statusLabel.isNotEmpty()) {
                Text(
                    text = statusLabel.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = activeColor
                )
            }
        }

        // Start & End Dynamic Terminal Legend Labels
        Text(
            text = startLabel,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextCyanSlate,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 4.dp)
        )

        Text(
            text = endLabel,
            fontSize = 10.5.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = TextCyanSlate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 4.dp)
        )
    }
}
