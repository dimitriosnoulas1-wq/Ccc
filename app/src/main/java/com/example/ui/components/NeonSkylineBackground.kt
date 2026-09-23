package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.AppThemeOption
import com.example.ui.theme.LocalAppColors

private data class BuildingSpec(
    val relX: Float,         // 0.0 to 1.0 (left to right)
    val relWidth: Float,     // width fraction
    val relHeight: Float,    // height fraction from bottom
    val roofType: RoofType,  // FLAT, SLANTED_LEFT, SLANTED_RIGHT, SPIRE, STEPPED
    val hasAntenna: Boolean = false,
    val antennaHeight: Float = 0f,
    val windowRows: Int = 0,
    val windowCols: Int = 0,
    val isDistant: Boolean = false
)

private enum class RoofType {
    FLAT,
    SLANTED_LEFT,
    SLANTED_RIGHT,
    SPIRE,
    STEPPED
}

@Composable
fun NeonSkylineBackground(
    theme: AppThemeOption = AppThemeOption.GALAXY_DARK,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current

    // Predefined architectural layout for a uniform, balanced skyline
    val buildings = remember {
        listOf(
            // Layer 1: Distant Background Silhouette Towers
            BuildingSpec(relX = 0.02f, relWidth = 0.12f, relHeight = 0.68f, roofType = RoofType.SPIRE, hasAntenna = true, antennaHeight = 0.08f, isDistant = true),
            BuildingSpec(relX = 0.15f, relWidth = 0.10f, relHeight = 0.58f, roofType = RoofType.SLANTED_RIGHT, isDistant = true),
            BuildingSpec(relX = 0.26f, relWidth = 0.14f, relHeight = 0.76f, roofType = RoofType.STEPPED, hasAntenna = true, antennaHeight = 0.10f, isDistant = true),
            BuildingSpec(relX = 0.42f, relWidth = 0.11f, relHeight = 0.62f, roofType = RoofType.FLAT, isDistant = true),
            BuildingSpec(relX = 0.54f, relWidth = 0.15f, relHeight = 0.82f, roofType = RoofType.SPIRE, hasAntenna = true, antennaHeight = 0.12f, isDistant = true),
            BuildingSpec(relX = 0.70f, relWidth = 0.12f, relHeight = 0.65f, roofType = RoofType.SLANTED_LEFT, isDistant = true),
            BuildingSpec(relX = 0.83f, relWidth = 0.15f, relHeight = 0.74f, roofType = RoofType.STEPPED, hasAntenna = true, antennaHeight = 0.09f, isDistant = true),

            // Layer 2: Midground Sharp Neon Glass Towers
            BuildingSpec(relX = 0.00f, relWidth = 0.11f, relHeight = 0.48f, roofType = RoofType.FLAT, windowRows = 7, windowCols = 2),
            BuildingSpec(relX = 0.10f, relWidth = 0.13f, relHeight = 0.56f, roofType = RoofType.SLANTED_RIGHT, hasAntenna = true, antennaHeight = 0.07f, windowRows = 8, windowCols = 3),
            BuildingSpec(relX = 0.22f, relWidth = 0.14f, relHeight = 0.46f, roofType = RoofType.FLAT, windowRows = 6, windowCols = 3),
            BuildingSpec(relX = 0.35f, relWidth = 0.16f, relHeight = 0.64f, roofType = RoofType.SPIRE, hasAntenna = true, antennaHeight = 0.10f, windowRows = 10, windowCols = 4),
            BuildingSpec(relX = 0.50f, relWidth = 0.13f, relHeight = 0.52f, roofType = RoofType.SLANTED_LEFT, windowRows = 7, windowCols = 3),
            BuildingSpec(relX = 0.62f, relWidth = 0.15f, relHeight = 0.68f, roofType = RoofType.STEPPED, hasAntenna = true, antennaHeight = 0.08f, windowRows = 11, windowCols = 4),
            BuildingSpec(relX = 0.76f, relWidth = 0.12f, relHeight = 0.50f, roofType = RoofType.FLAT, windowRows = 8, windowCols = 3),
            BuildingSpec(relX = 0.87f, relWidth = 0.14f, relHeight = 0.58f, roofType = RoofType.SPIRE, hasAntenna = true, antennaHeight = 0.06f, windowRows = 9, windowCols = 3)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val horizonY = h * 0.88f // Baseline horizon near the bottom
        val skylineTopLimit = h * 0.20f
        val maxBuildingH = horizonY - skylineTopLimit

        // Colors per Theme
        val (glowColor1, glowColor2, wireColor, windowColor, beaconColor, distantFillColor, mainFillColor) = when (theme) {
            AppThemeOption.GALAXY_DARK -> {
                Tuple7(
                    Color(0xFF00F5FF).copy(alpha = 0.30f), // Cyan glow
                    Color(0xFFA855F7).copy(alpha = 0.22f), // Violet glow
                    Color(0xFF00F5FF).copy(alpha = 0.20f), // Wireframe
                    Color(0xFF38BDF8).copy(alpha = 0.28f), // Windows
                    Color(0xFF00F5FF),                     // Beacon
                    Color(0xFF0A0F1D).copy(alpha = 0.80f), // Distant fill
                    Color(0xFF0E1528).copy(alpha = 0.90f)  // Main fill
                )
            }
            AppThemeOption.PURE_BLACK -> {
                Tuple7(
                    Color(0xFFFFFFFF).copy(alpha = 0.15f),
                    Color(0xFF888888).copy(alpha = 0.10f),
                    Color(0xFFFFFFFF).copy(alpha = 0.15f),
                    Color(0xFFFFFFFF).copy(alpha = 0.18f),
                    Color(0xFFFFFFFF),
                    Color(0xFF050505),
                    Color(0xFF0A0A0A)
                )
            }
            AppThemeOption.CLEAN_WHITE -> {
                Tuple7(
                    Color(0xFF0284C7).copy(alpha = 0.10f),
                    Color(0xFF94A3B8).copy(alpha = 0.12f),
                    Color(0xFF94A3B8).copy(alpha = 0.20f),
                    Color(0xFF0284C7).copy(alpha = 0.16f),
                    Color(0xFF0284C7),
                    Color(0xFFF1F5F9).copy(alpha = 0.70f),
                    Color(0xFFE2E8F0).copy(alpha = 0.85f)
                )
            }
        }

        // 1. Cyber Ambient Atmosphere Gradient behind towers
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    glowColor1.copy(alpha = glowColor1.alpha * 0.4f),
                    glowColor2.copy(alpha = glowColor2.alpha * 0.6f),
                    Color.Transparent
                ),
                startY = skylineTopLimit * 0.7f,
                endY = horizonY
            ),
            topLeft = Offset(0f, 0f),
            size = Size(w, horizonY)
        )

        // 2. Draw Distant Towers First
        buildings.filter { it.isDistant }.forEach { b ->
            drawSkyscraper(
                b = b,
                w = w,
                horizonY = horizonY,
                maxBuildingH = maxBuildingH,
                fillColor = distantFillColor,
                strokeColor = wireColor.copy(alpha = wireColor.alpha * 0.6f),
                windowColor = windowColor.copy(alpha = windowColor.alpha * 0.4f),
                beaconColor = beaconColor,
                beaconAlpha = 0.65f,
                beaconRadius = 3.5f
            )
        }

        // 3. Draw Midground Towers with glowing neon edges & lit windows
        buildings.filter { !it.isDistant }.forEach { b ->
            drawSkyscraper(
                b = b,
                w = w,
                horizonY = horizonY,
                maxBuildingH = maxBuildingH,
                fillColor = mainFillColor,
                strokeColor = wireColor,
                windowColor = windowColor,
                beaconColor = beaconColor,
                beaconAlpha = 0.85f,
                beaconRadius = 4.0f
            )
        }

        // 4. Subtle Perspective Cyber Grid on the Ground
        val gridLines = 5
        for (i in 0..gridLines) {
            val frac = i / gridLines.toFloat()
            val y = horizonY + (h - horizonY) * (frac * frac)
            drawLine(
                color = wireColor.copy(alpha = wireColor.alpha * (1f - frac * 0.6f) * 0.7f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        // Soft gradient overlay at the bottom for smooth content contrast
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    palette.background.copy(alpha = 0.85f),
                    palette.background
                ),
                startY = horizonY * 0.65f,
                endY = h
            ),
            topLeft = Offset(0f, horizonY * 0.65f),
            size = Size(w, h - (horizonY * 0.65f))
        )
    }
}

private fun DrawScope.drawSkyscraper(
    b: BuildingSpec,
    w: Float,
    horizonY: Float,
    maxBuildingH: Float,
    fillColor: Color,
    strokeColor: Color,
    windowColor: Color,
    beaconColor: Color,
    beaconAlpha: Float,
    beaconRadius: Float
) {
    val left = b.relX * w
    val bWidth = b.relWidth * w
    val right = left + bWidth
    val bHeight = b.relHeight * maxBuildingH
    val top = horizonY - bHeight

    val path = Path()

    when (b.roofType) {
        RoofType.FLAT -> {
            path.moveTo(left, horizonY)
            path.lineTo(left, top)
            path.lineTo(right, top)
            path.lineTo(right, horizonY)
            path.close()
        }
        RoofType.SLANTED_RIGHT -> {
            path.moveTo(left, horizonY)
            path.lineTo(left, top - (bHeight * 0.08f))
            path.lineTo(right, top)
            path.lineTo(right, horizonY)
            path.close()
        }
        RoofType.SLANTED_LEFT -> {
            path.moveTo(left, horizonY)
            path.lineTo(left, top)
            path.lineTo(right, top - (bHeight * 0.08f))
            path.lineTo(right, horizonY)
            path.close()
        }
        RoofType.SPIRE -> {
            val midX = left + bWidth / 2f
            path.moveTo(left, horizonY)
            path.lineTo(left, top + (bHeight * 0.12f))
            path.lineTo(midX, top)
            path.lineTo(right, top + (bHeight * 0.12f))
            path.lineTo(right, horizonY)
            path.close()
        }
        RoofType.STEPPED -> {
            val stepW = bWidth * 0.2f
            val stepH = bHeight * 0.08f
            path.moveTo(left, horizonY)
            path.lineTo(left, top + stepH * 2)
            path.lineTo(left + stepW, top + stepH * 2)
            path.lineTo(left + stepW, top + stepH)
            path.lineTo(right - stepW, top + stepH)
            path.lineTo(right - stepW, top + stepH * 2)
            path.lineTo(right, top + stepH * 2)
            path.lineTo(right, horizonY)
            path.close()
        }
    }

    // Fill building body
    drawPath(path = path, color = fillColor, style = Fill)

    // Outline neon stroke
    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(width = 1.2f, cap = StrokeCap.Round)
    )

    // Vertical structural neon line down the middle
    val midX = left + bWidth / 2f
    drawLine(
        color = strokeColor.copy(alpha = strokeColor.alpha * 0.6f),
        start = Offset(midX, top + 10f),
        end = Offset(midX, horizonY),
        strokeWidth = 1f
    )

    // Draw Window Matrix (Geometric lit digital windows)
    if (b.windowRows > 0 && b.windowCols > 0) {
        val padX = bWidth * 0.18f
        val innerW = bWidth - padX * 2f
        val windowW = (innerW / b.windowCols) * 0.55f
        val colGap = innerW / b.windowCols

        val startY = top + (bHeight * 0.22f)
        val endY = horizonY - 15f
        val availableH = endY - startY
        val rowGap = availableH / b.windowRows
        val windowH = rowGap * 0.45f

        for (r in 0 until b.windowRows) {
            val wy = startY + r * rowGap
            for (c in 0 until b.windowCols) {
                val hash = ((b.relX * 100).toInt() + r * 17 + c * 31) % 10
                if (hash > 2) {
                    val wx = left + padX + c * colGap
                    val winAlpha = if (hash % 2 == 0) windowColor.alpha else windowColor.alpha * 0.5f
                    drawRect(
                        color = windowColor.copy(alpha = winAlpha),
                        topLeft = Offset(wx, wy),
                        size = Size(windowW, windowH)
                    )
                }
            }
        }
    }

    // Antenna & Neon Beacon light
    if (b.hasAntenna) {
        val antX = left + bWidth / 2f
        val antH = b.antennaHeight * maxBuildingH
        val antTop = top - antH

        // Antenna pole
        drawLine(
            color = strokeColor,
            start = Offset(antX, top),
            end = Offset(antX, antTop),
            strokeWidth = 1.5f
        )

        // Glowing Beacon
        drawCircle(
            color = beaconColor.copy(alpha = beaconAlpha * 0.35f),
            radius = beaconRadius * 2.2f,
            center = Offset(antX, antTop)
        )
        drawCircle(
            color = beaconColor.copy(alpha = beaconAlpha),
            radius = beaconRadius,
            center = Offset(antX, antTop)
        )
    }
}

private data class Tuple7<A, B, C, D, E, F, G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G
)

