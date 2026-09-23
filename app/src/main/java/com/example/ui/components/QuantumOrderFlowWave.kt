package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PhotonGoldBright
import com.example.ui.theme.QuantumBlue
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.QuantumCyanBright
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SoftEmerald
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Canvas Composable for Order Flow.
 *
 * Features:
 * - Dual liquid plasma waves modeled with smooth cubic / quadratic Bezier curves.
 * - Horizontal liquid drift animation simulating live institutional liquidity pressure.
 * - Top wave: Quantum Cyan luminous buy pressure flow with volumetric gradient fill.
 * - Bottom wave: Deep Crimson / Ruby sell pressure counter-wave with plasma glow.
 * - Decoupled signature: directly accepts [buyRatio] (0f..1f or 0..100) and [sellRatio].
 */
@Composable
fun QuantumOrderFlowWave(
    buyRatio: Float,
    sellRatio: Float,
    volumeSpikeFactor: Float = 1.0f,
    tradeVelocity: Float = 1.0f,
    modifier: Modifier = Modifier,
    height: Dp = 90.dp,
    showTelemetryGrid: Boolean = true
) {
    // Normalize ratios safely
    val total = (buyRatio + sellRatio).coerceAtLeast(0.001f)
    val normBuy = (buyRatio / total).coerceIn(0.08f, 0.92f)
    val normSell = 1f - normBuy

    // Smooth transition when buy/sell ratio shifts
    val animatedBuyRatio by animateFloatAsState(
        targetValue = normBuy,
        animationSpec = tween(durationMillis = 550),
        label = "animated_buy_ratio"
    )

    // Data-driven drift duration scaled inversely to trade velocity (trades per sec)
    val driftDuration1 = (3600 / tradeVelocity.coerceIn(0.3f, 4.5f)).toInt()
    val driftDuration2 = (4800 / tradeVelocity.coerceIn(0.3f, 4.5f)).toInt()

    // Horizontal drift phase animations driven by trade velocity
    val infiniteTransition = rememberInfiniteTransition(label = "quantum_wave_drift")
    val driftPhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = driftDuration1, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase_primary"
    )

    val driftPhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = driftDuration2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase_secondary"
    )

    // Volumetric energy pulse
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quantum_pulse_glow"
    )

    // Reusable Path instances to prevent allocations in onDraw
    val strokePath1 = remember { Path() }
    val fillPath1 = remember { Path() }
    val strokePath2 = remember { Path() }
    val fillPath2 = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF040711))
            .border(1.dp, Color(0x2200D2FF), RoundedCornerShape(14.dp))
            .padding(vertical = 2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val heightPx = size.height
            val equilibriumY = heightPx * (1f - animatedBuyRatio)
            val splitX = width * animatedBuyRatio

            // Wave height directly proportional to volume spikes
            val ampScale = (heightPx * 0.22f) * volumeSpikeFactor.coerceIn(0.5f, 2.5f)
            val buyAmp = ampScale * animatedBuyRatio * 1.5f
            val sellAmp = ampScale * normSell * 1.5f

            // 1. Holographic Quantum Telemetry Grid
            if (showTelemetryGrid) {
                drawQuantumGrid(width, heightPx, equilibriumY)
            }

            // 2. Bottom Plasma Wave: Crimson Sell Pressure (Counter-current)
            drawPlasmaWave(
                width = width,
                height = heightPx,
                baseY = equilibriumY,
                amplitude = sellAmp,
                phase = driftPhase2,
                isBuyWave = false,
                strokeColor = SoftCrimson,
                path = strokePath2,
                fillPath = fillPath2,
                fillGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x00EF4444),
                        SoftCrimson.copy(alpha = 0.15f * pulseGlow),
                        SoftCrimson.copy(alpha = 0.35f * pulseGlow)
                    ),
                    startY = equilibriumY,
                    endY = heightPx
                )
            )

            // 3. Top Plasma Wave: Quantum Cyan Buy Pressure (Main flow)
            drawPlasmaWave(
                width = width,
                height = heightPx,
                baseY = equilibriumY,
                amplitude = buyAmp,
                phase = driftPhase1,
                isBuyWave = true,
                strokeColor = QuantumCyan,
                path = strokePath1,
                fillPath = fillPath1,
                fillGradient = Brush.verticalGradient(
                    colors = listOf(
                        QuantumCyan.copy(alpha = 0.38f * pulseGlow),
                        QuantumBlue.copy(alpha = 0.18f),
                        Color(0x0000D2FF)
                    ),
                    startY = 0f,
                    endY = equilibriumY
                )
            )

            // 4. Color Ratio Split: Horizontal boundary physical slide matching real-time Buy/Sell ratio
            drawLine(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0.0f to QuantumCyanBright.copy(alpha = 0.85f * pulseGlow),
                        (animatedBuyRatio - 0.08f).coerceAtLeast(0f) to QuantumCyan.copy(alpha = 0.90f * pulseGlow),
                        animatedBuyRatio to PhotonGoldBright,
                        (animatedBuyRatio + 0.08f).coerceAtMost(1f) to SoftCrimson.copy(alpha = 0.90f * pulseGlow),
                        1.0f to SoftCrimson.copy(alpha = 0.75f * pulseGlow)
                    )
                ),
                start = Offset(0f, equilibriumY),
                end = Offset(width, equilibriumY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 5. Sliding Boundary Pillar Indicator
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x00FFFFFF),
                        PhotonGoldBright.copy(alpha = 0.80f * pulseGlow),
                        Color(0x00FFFFFF)
                    ),
                    startY = (equilibriumY - 20.dp.toPx()).coerceAtLeast(0f),
                    endY = (equilibriumY + 20.dp.toPx()).coerceAtMost(heightPx)
                ),
                start = Offset(splitX, (equilibriumY - 20.dp.toPx()).coerceAtLeast(0f)),
                end = Offset(splitX, (equilibriumY + 20.dp.toPx()).coerceAtMost(heightPx)),
                strokeWidth = 1.5.dp.toPx()
            )

            // 6. Sliding Partition Node at exact Buy/Sell ratio
            drawCircle(
                color = PhotonGoldBright.copy(alpha = 0.40f * pulseGlow),
                radius = 6.dp.toPx(),
                center = Offset(splitX, equilibriumY)
            )
            drawCircle(
                color = PhotonGoldBright,
                radius = 3.dp.toPx(),
                center = Offset(splitX, equilibriumY)
            )
            drawCircle(
                color = Color.White,
                radius = 1.5.dp.toPx(),
                center = Offset(splitX, equilibriumY)
            )

            // 7. Particulate Liquidity Sparkles along Equilibrium
            val sparkleCount = 6
            for (i in 0 until sparkleCount) {
                val progress = ((i.toFloat() / sparkleCount) + (driftPhase1 / (2 * PI).toFloat())) % 1f
                val sparkX = width * progress
                val waveOffset = sin(progress * 4 * PI + driftPhase1).toFloat() * (heightPx * 0.10f)
                val sparkY = equilibriumY + waveOffset
                val sparkAlpha = (sin(progress * PI).toFloat()).coerceIn(0f, 1f)
                val isBuySide = sparkX < splitX

                drawCircle(
                    color = if (isBuySide) QuantumCyanBright.copy(alpha = sparkAlpha * 0.85f) else SoftCrimson.copy(alpha = sparkAlpha * 0.85f),
                    radius = (2.2.dp * pulseGlow).toPx(),
                    center = Offset(sparkX, sparkY)
                )
            }
        }
    }
}

/**
 * Draws a volumetric Bezier plasma wave with smooth cubic control segments.
 */
private fun DrawScope.drawPlasmaWave(
    width: Float,
    height: Float,
    baseY: Float,
    amplitude: Float,
    phase: Float,
    isBuyWave: Boolean,
    strokeColor: Color,
    path: Path,
    fillPath: Path,
    fillGradient: Brush
) {
    val segments = 6
    val segmentWidth = width / segments

    path.reset()
    fillPath.reset()

    var prevX = 0f
    var prevY = 0f

    for (i in 0..segments) {
        val x = i * segmentWidth
        val normalizedX = i.toFloat() / segments
        val sinFactor = sin(normalizedX * 2 * PI + phase).toFloat()
        val cosFactor = cos(normalizedX * 4 * PI - phase * 0.5f).toFloat() * 0.35f
        val offset = (sinFactor + cosFactor) * amplitude
        val y = if (isBuyWave) {
            (baseY - offset).coerceIn(4f, height - 4f)
        } else {
            (baseY + offset).coerceIn(4f, height - 4f)
        }

        if (i == 0) {
            path.moveTo(x, y)
            fillPath.moveTo(x, if (isBuyWave) 0f else height)
            fillPath.lineTo(x, y)
        } else {
            val controlPoint1X = prevX + segmentWidth * 0.5f
            val controlPoint1Y = prevY
            val controlPoint2X = x - segmentWidth * 0.5f
            val controlPoint2Y = y

            path.cubicTo(
                controlPoint1X, controlPoint1Y,
                controlPoint2X, controlPoint2Y,
                x, y
            )
            fillPath.cubicTo(
                controlPoint1X, controlPoint1Y,
                controlPoint2X, controlPoint2Y,
                x, y
            )
        }
        prevX = x
        prevY = y
    }

    if (isBuyWave) {
        fillPath.lineTo(width, 0f)
        fillPath.close()
    } else {
        fillPath.lineTo(width, height)
        fillPath.close()
    }

    // Draw volumetric liquid gradient fill
    drawPath(path = fillPath, brush = fillGradient)

    // Outer plasma neon glow stroke
    drawPath(
        path = path,
        color = strokeColor.copy(alpha = 0.35f),
        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
    )

    // Core sharp laser stroke
    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * Draws subtle holographic telemetry grid and depth coordinates.
 */
private fun DrawScope.drawQuantumGrid(width: Float, height: Float, equilibriumY: Float) {
    val gridColor = Color(0x1400D2FF)
    val verticalDivisions = 6
    val horizontalDivisions = 4

    // Vertical scan lines
    for (i in 1 until verticalDivisions) {
        val x = (width / verticalDivisions) * i
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Horizontal scan lines
    for (i in 1 until horizontalDivisions) {
        val y = (height / horizontalDivisions) * i
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}
