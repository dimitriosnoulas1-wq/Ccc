package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * High-fidelity Neon Gothic Citadel Wallpaper for Learn screen.
 * Tuned with darkened, anti-glare luminance for ultra-comfortable reading:
 * - Soaring Gothic Neon Skyscrapers with subtle Cyan & Purple-Magenta light conduits
 * - Soft luminous Full Moon with dark celestial nebula clouds
 * - Gothic connecting arch bridges
 * - Shimmering vertical water reflections on the dark lake below
 * - Foreground rocky shoreline
 */
@Composable
fun NeonGothicCitadelBackground(
    modifier: Modifier = Modifier,
    dimRatio: Float = 0.52f // Calibrated dark ambient scrim for effortless reading
) {
    val infiniteTransition = rememberInfiniteTransition(label = "citadel_fx")

    // Gentle breathing pulse for neon towers
    val neonPulse by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonPulse"
    )

    // Water shimmer
    val waterPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waterPhase"
    )

    // Star field coordinates
    val stars = remember {
        listOf(
            Triple(0.06f, 0.05f, 1.1f),
            Triple(0.14f, 0.09f, 1.4f),
            Triple(0.22f, 0.04f, 0.9f),
            Triple(0.32f, 0.12f, 1.5f),
            Triple(0.44f, 0.06f, 1.0f),
            Triple(0.56f, 0.03f, 1.3f),
            Triple(0.68f, 0.08f, 0.9f),
            Triple(0.10f, 0.18f, 1.4f),
            Triple(0.94f, 0.14f, 1.2f),
            Triple(0.90f, 0.24f, 1.0f),
            Triple(0.04f, 0.30f, 1.1f),
            Triple(0.28f, 0.22f, 1.3f),
            Triple(0.52f, 0.16f, 1.0f),
            Triple(0.76f, 0.19f, 1.3f)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val waterY = h * 0.74f

            // 1. COSMIC DEEP SKY (Midnight Blue to Deep Obsidian Violet)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF020106), // Deepest pitch
                        Color(0xFF070414), // Dark cosmic violet
                        Color(0xFF0E0722), // Soft atmospheric violet
                        Color(0xFF080518), // Dark midtone
                        Color(0xFF04060E)  // Horizon line
                    ),
                    startY = 0f,
                    endY = waterY
                ),
                topLeft = Offset.Zero,
                size = Size(w, waterY)
            )

            // 2. SOFT PURPLE & MAGENTA NEBULA CLOUDS (Subtle, non-distracting)
            // Left nebula
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C3AED).copy(alpha = 0.22f),
                        Color(0xFFA21CAF).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.20f, h * 0.22f),
                    radius = w * 0.55f
                ),
                center = Offset(w * 0.20f, h * 0.22f),
                radius = w * 0.55f
            )

            // Right nebula surrounding moon
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.20f),
                        Color(0xFF4F46E5).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.80f, h * 0.16f),
                    radius = w * 0.50f
                ),
                center = Offset(w * 0.80f, h * 0.16f),
                radius = w * 0.50f
            )

            // 3. STARS
            stars.forEach { (relX, relY, radius) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = radius,
                    center = Offset(relX * w, relY * h)
                )
            }

            // 4. SOFT ATMOSPHERIC FULL MOON (Upper Right - Muted glow for reading comfort)
            val moonX = w * 0.82f
            val moonY = h * 0.12f
            val moonR = w * 0.080f

            // Outer Moon Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.22f),
                        Color(0xFF818CF8).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(moonX, moonY),
                    radius = moonR * 3.2f
                ),
                center = Offset(moonX, moonY),
                radius = moonR * 3.2f
            )
            // Inner Moon Disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE2E8F0),
                        Color(0xFFCBD5E1),
                        Color(0xFF94A3B8)
                    ),
                    center = Offset(moonX - moonR * 0.25f, moonY - moonR * 0.25f),
                    radius = moonR
                ),
                center = Offset(moonX, moonY),
                radius = moonR
            )
            // Lunar Mare crater shading
            drawCircle(
                color = Color(0xFF64748B).copy(alpha = 0.35f),
                radius = moonR * 0.32f,
                center = Offset(moonX + moonR * 0.2f, moonY + moonR * 0.15f)
            )

            // 5. DISTANT MOUNTAIN SILHOUETTE
            val mountainPath = Path().apply {
                moveTo(0f, waterY)
                lineTo(0f, h * 0.60f)
                lineTo(w * 0.15f, h * 0.54f)
                lineTo(w * 0.32f, h * 0.62f)
                lineTo(w * 0.50f, h * 0.57f)
                lineTo(w * 0.72f, h * 0.63f)
                lineTo(w * 0.88f, h * 0.55f)
                lineTo(w, h * 0.58f)
                lineTo(w, waterY)
                close()
            }
            drawPath(mountainPath, Color(0xFF060710))

            // 6. GOTHIC CATHEDRAL SKYSCRAPERS
            // Tower 1 (Far Left Flank Spire)
            drawTower(
                baseX = w * 0.08f,
                width = w * 0.11f,
                peakY = h * 0.38f,
                waterY = waterY,
                cyanGlow = Color(0xFF00E5FF),
                purpleGlow = Color(0xFFB072F5),
                pulse = neonPulse
            )

            // Tower 2 (Mid-Left Grand Cathedral)
            drawTower(
                baseX = w * 0.24f,
                width = w * 0.17f,
                peakY = h * 0.24f,
                waterY = waterY,
                cyanGlow = Color(0xFF00E5FF),
                purpleGlow = Color(0xFFD946EF),
                pulse = neonPulse
            )

            // Connecting Gothic Bridge 1
            drawGothicBridge(
                leftX = w * 0.32f,
                rightX = w * 0.44f,
                deckY = h * 0.58f,
                waterY = waterY,
                neonCyan = Color(0xFF00E5FF),
                neonPurple = Color(0xFFD946EF)
            )

            // Tower 3 (Center Slender Spire)
            drawTower(
                baseX = w * 0.48f,
                width = w * 0.13f,
                peakY = h * 0.32f,
                waterY = waterY,
                cyanGlow = Color(0xFF00E5FF),
                purpleGlow = Color(0xFF9333EA),
                pulse = neonPulse
            )

            // Connecting Gothic Bridge 2
            drawGothicBridge(
                leftX = w * 0.54f,
                rightX = w * 0.63f,
                deckY = h * 0.60f,
                waterY = waterY,
                neonCyan = Color(0xFF00E5FF),
                neonPurple = Color(0xFFC026D3)
            )

            // Tower 4 (Dominant Grand Monarch Cathedral - Tallest)
            drawTower(
                baseX = w * 0.66f,
                width = w * 0.19f,
                peakY = h * 0.18f,
                waterY = waterY,
                cyanGlow = Color(0xFF00E5FF),
                purpleGlow = Color(0xFFE11D48),
                pulse = neonPulse
            )

            // Tower 5 (Right Cathedral Flank)
            drawTower(
                baseX = w * 0.83f,
                width = w * 0.14f,
                peakY = h * 0.34f,
                waterY = waterY,
                cyanGlow = Color(0xFF38BDF8),
                purpleGlow = Color(0xFFA855F7),
                pulse = neonPulse
            )

            // 7. WATER LAKE SURFACE & REFLECTIONS
            val waterH = h - waterY
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060310),
                        Color(0xFF040510),
                        Color(0xFF020208)
                    ),
                    startY = waterY,
                    endY = h
                ),
                topLeft = Offset(0f, waterY),
                size = Size(w, waterH)
            )

            // Shimmering Vertical Water Reflections
            drawVibrantWaterReflection(w * 0.08f, w * 0.08f, waterY, waterH, Color(0xFF00E5FF), Color(0xFFB072F5), waterPhase)
            drawVibrantWaterReflection(w * 0.24f, w * 0.15f, waterY, waterH, Color(0xFF00E5FF), Color(0xFFD946EF), waterPhase)
            drawVibrantWaterReflection(w * 0.48f, w * 0.12f, waterY, waterH, Color(0xFF00E5FF), Color(0xFF9333EA), waterPhase)
            drawVibrantWaterReflection(w * 0.66f, w * 0.18f, waterY, waterH, Color(0xFF00E5FF), Color(0xFFE11D48), waterPhase)
            drawVibrantWaterReflection(w * 0.83f, w * 0.12f, waterY, waterH, Color(0xFF38BDF8), Color(0xFFA855F7), waterPhase)
            drawVibrantWaterReflection(moonX, w * 0.07f, waterY, waterH * 0.80f, Color(0xFFCBD5E1), Color(0xFF38BDF8), waterPhase)

            // 8. FOREGROUND ROCKY SHORE SILHOUETTE
            val shorePath = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.88f)
                cubicTo(w * 0.12f, h * 0.84f, w * 0.24f, h * 0.92f, w * 0.42f, h * 0.89f)
                cubicTo(w * 0.60f, h * 0.86f, w * 0.78f, h * 0.94f, w, h * 0.88f)
                lineTo(w, h)
                close()
            }
            drawPath(shorePath, Color(0xFF020105))

            // 9. DARK COMFORT OVERLAY (Provides high-contrast, glare-free dark canvas for text)
            if (dimRatio > 0f) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF090D14).copy(alpha = dimRatio * 0.70f),
                            Color(0xFF090D14).copy(alpha = dimRatio * 1.15f), // Darkest in middle reading zone
                            Color(0xFF090D14).copy(alpha = dimRatio * 0.85f)
                        )
                    ),
                    topLeft = Offset.Zero,
                    size = Size(w, h)
                )
            }
        }
    }
}

/**
 * Draws a Gothic Cathedral Skyscraper with glare-free neon lines & window accents.
 */
private fun DrawScope.drawTower(
    baseX: Float,
    width: Float,
    peakY: Float,
    waterY: Float,
    cyanGlow: Color,
    purpleGlow: Color,
    pulse: Float
) {
    val halfW = width / 2f
    val midX = baseX
    val left = midX - halfW
    val right = midX + halfW
    val height = waterY - peakY

    // 1. Tower Body Base Silhouette
    val body = Path().apply {
        moveTo(left, waterY)
        lineTo(left + halfW * 0.22f, peakY + height * 0.28f)
        lineTo(midX - 2f, peakY + height * 0.08f)
        lineTo(midX, peakY)
        lineTo(midX + 2f, peakY + height * 0.08f)
        lineTo(right - halfW * 0.22f, peakY + height * 0.28f)
        lineTo(right, waterY)
        close()
    }
    drawPath(body, Color(0xFF080A12))

    // 2. Right Facet Shading
    val rightFacet = Path().apply {
        moveTo(midX, peakY)
        lineTo(right - halfW * 0.22f, peakY + height * 0.28f)
        lineTo(right, waterY)
        lineTo(midX, waterY)
        close()
    }
    drawPath(rightFacet, Color(0xFF04050A).copy(alpha = 0.7f))

    // 3. Central Neon Conduit (Softened to avoid halation/glare)
    drawLine(
        color = cyanGlow.copy(alpha = 0.30f * pulse),
        start = Offset(midX, peakY + 6f),
        end = Offset(midX, waterY),
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFE0F2FE).copy(alpha = 0.75f * pulse),
        start = Offset(midX, peakY + 4f),
        end = Offset(midX, waterY),
        strokeWidth = 1.8f,
        cap = StrokeCap.Round
    )

    // 4. Purple/Magenta Vertical Accent Channels
    val leftAccentX = left + halfW * 0.40f
    val rightAccentX = right - halfW * 0.40f
    val accentStartY = peakY + height * 0.26f

    drawLine(
        color = purpleGlow.copy(alpha = 0.30f * pulse),
        start = Offset(leftAccentX, accentStartY),
        end = Offset(leftAccentX, waterY),
        strokeWidth = 3f
    )
    drawLine(
        color = purpleGlow.copy(alpha = 0.70f * pulse),
        start = Offset(leftAccentX, accentStartY),
        end = Offset(leftAccentX, waterY),
        strokeWidth = 1.2f
    )

    drawLine(
        color = purpleGlow.copy(alpha = 0.30f * pulse),
        start = Offset(rightAccentX, accentStartY),
        end = Offset(rightAccentX, waterY),
        strokeWidth = 3f
    )
    drawLine(
        color = purpleGlow.copy(alpha = 0.70f * pulse),
        start = Offset(rightAccentX, accentStartY),
        end = Offset(rightAccentX, waterY),
        strokeWidth = 1.2f
    )

    // 5. Window Matrix Grid
    val windowRows = 10
    val rowH = (waterY - (peakY + height * 0.35f)) / windowRows
    for (r in 0 until windowRows) {
        val rowY = peakY + height * 0.35f + r * rowH
        drawCircle(
            color = if (r % 2 == 0) cyanGlow.copy(alpha = 0.5f) else Color(0xFFFDE047).copy(alpha = 0.4f),
            radius = 1.0f,
            center = Offset(left + halfW * 0.25f, rowY)
        )
        drawCircle(
            color = if (r % 3 == 0) purpleGlow.copy(alpha = 0.5f) else cyanGlow.copy(alpha = 0.45f),
            radius = 1.0f,
            center = Offset(right - halfW * 0.25f, rowY)
        )
    }

    // 6. Spire Pinnacle Beacon
    drawCircle(
        color = cyanGlow.copy(alpha = 0.50f * pulse),
        radius = 7f,
        center = Offset(midX, peakY)
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = 2.5f,
        center = Offset(midX, peakY)
    )
}

/**
 * Draws an illuminated Gothic Arch Bridge connecting towers.
 */
private fun DrawScope.drawGothicBridge(
    leftX: Float,
    rightX: Float,
    deckY: Float,
    waterY: Float,
    neonCyan: Color,
    neonPurple: Color
) {
    val bridge = Path().apply {
        moveTo(leftX, deckY)
        lineTo(rightX, deckY)
        lineTo(rightX, waterY)
        lineTo(leftX, waterY)
        close()
    }
    drawPath(bridge, Color(0xFF070810))

    val midX = (leftX + rightX) / 2f
    val arch = Path().apply {
        moveTo(leftX + 4f, waterY)
        quadraticTo(midX, deckY + 6f, rightX - 4f, waterY)
        close()
    }
    drawPath(arch, Color(0xFF030208))

    drawPath(
        arch,
        brush = Brush.horizontalGradient(
            colors = listOf(neonCyan.copy(alpha = 0.60f), neonPurple.copy(alpha = 0.60f)),
            startX = leftX,
            endX = rightX
        ),
        style = Stroke(width = 1.5f)
    )

    drawLine(
        brush = Brush.horizontalGradient(
            colors = listOf(neonCyan.copy(alpha = 0.75f), neonPurple.copy(alpha = 0.75f)),
            startX = leftX,
            endX = rightX
        ),
        start = Offset(leftX, deckY),
        end = Offset(rightX, deckY),
        strokeWidth = 1.8f
    )
}

/**
 * Draws shimmering neon water reflections on the dark lake surface.
 */
private fun DrawScope.drawVibrantWaterReflection(
    centerX: Float,
    width: Float,
    startY: Float,
    height: Float,
    color1: Color,
    color2: Color,
    phase: Float
) {
    val ripples = 16
    val gap = height / ripples

    for (i in 0 until ripples) {
        val y = startY + i * gap + (phase * 2.5f)
        val alpha = (1f - (i.toFloat() / ripples)) * 0.45f
        val rippleW = width * (0.45f + 0.55f * ((i % 4) / 3f))

        val rippleColor = if (i % 2 == 0) color1 else color2

        drawLine(
            color = rippleColor.copy(alpha = alpha * 0.3f),
            start = Offset(centerX - rippleW / 2f, y),
            end = Offset(centerX + rippleW / 2f, y),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = rippleColor.copy(alpha = alpha * 0.75f),
            start = Offset(centerX - rippleW * 0.35f, y),
            end = Offset(centerX + rippleW * 0.35f, y),
            strokeWidth = 1.4f,
            cap = StrokeCap.Round
        )
    }
}
