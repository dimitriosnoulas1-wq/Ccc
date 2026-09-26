package com.example.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AppThemeOption

val LocalAppColors = compositionLocalOf { GalaxyDarkPalette }

private fun getAppColorScheme(theme: AppThemeOption) = when (theme) {
    AppThemeOption.GALAXY_DARK -> darkColorScheme(
        primary = QuantumCyan,
        onPrimary = Color(0xFF05050F),
        primaryContainer = Color(0xFF0C222E),
        onPrimaryContainer = Color(0xFFE0FFFF),
        secondary = MauveAurora,
        onSecondary = Color(0xFF140822),
        secondaryContainer = Color(0xFF26123F),
        onSecondaryContainer = Color(0xFFF3E8FF),
        tertiary = TachyonMint,
        onTertiary = Color(0xFF04140C),
        background = GalaxyDarkPalette.background,
        onBackground = GalaxyDarkPalette.textPrimary,
        surface = GalaxyDarkPalette.surface,
        onSurface = GalaxyDarkPalette.textPrimary,
        surfaceVariant = GalaxyDarkPalette.surfaceElevated,
        onSurfaceVariant = GalaxyDarkPalette.textSecondary,
        outline = GalaxyDarkPalette.border,
        outlineVariant = GalaxyDarkPalette.borderGlow
    )
    AppThemeOption.PURE_BLACK -> darkColorScheme(
        primary = PureBlackPalette.primary,
        onPrimary = Color(0xFF05050F),
        primaryContainer = Color(0xFF0C222E),
        onPrimaryContainer = Color(0xFFE0FFFF),
        secondary = PureBlackPalette.secondary,
        onSecondary = Color(0xFF140822),
        secondaryContainer = Color(0xFF26123F),
        onSecondaryContainer = Color(0xFFF3E8FF),
        tertiary = TachyonMint,
        onTertiary = Color(0xFF04140C),
        background = PureBlackPalette.background,
        onBackground = PureBlackPalette.textPrimary,
        surface = PureBlackPalette.surface,
        onSurface = PureBlackPalette.textPrimary,
        surfaceVariant = PureBlackPalette.surfaceElevated,
        onSurfaceVariant = PureBlackPalette.textSecondary,
        outline = PureBlackPalette.border,
        outlineVariant = PureBlackPalette.borderGlow
    )
    AppThemeOption.CLEAN_WHITE -> lightColorScheme(
        primary = CleanWhitePalette.primary,
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE0F2FE),
        onPrimaryContainer = Color(0xFF0369A1),
        secondary = CleanWhitePalette.secondary,
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEDE9FE),
        onSecondaryContainer = Color(0xFF4C1D95),
        tertiary = TachyonMint,
        onTertiary = Color(0xFFFFFFFF),
        background = CleanWhitePalette.background,
        onBackground = CleanWhitePalette.textPrimary,
        surface = CleanWhitePalette.surface,
        onSurface = CleanWhitePalette.textPrimary,
        surfaceVariant = CleanWhitePalette.surfaceElevated,
        onSurfaceVariant = CleanWhitePalette.textSecondary,
        outline = CleanWhitePalette.border,
        outlineVariant = CleanWhitePalette.borderGlow
    )
}

@Composable
fun MyApplicationTheme(
    theme: AppThemeOption = AppThemeOption.GALAXY_DARK,
    content: @Composable () -> Unit
) {
    val palette = when (theme) {
        AppThemeOption.GALAXY_DARK -> GalaxyDarkPalette
        AppThemeOption.PURE_BLACK -> PureBlackPalette
        AppThemeOption.CLEAN_WHITE -> CleanWhitePalette
    }

    CompositionLocalProvider(LocalAppColors provides palette) {
        MaterialTheme(
            colorScheme = getAppColorScheme(theme),
            typography = Typography,
            content = content
        )
    }
}

fun Modifier.cosmicBackground(isLight: Boolean = false): Modifier = if (isLight) {
    this.background(CleanWhitePalette.background)
} else {
    this.background(stitchVoidBackground())
}

/**
 * 2126 Quantum Sub-Space Panel:
 * Deep background Color(0xFF0D0A1D) with QuantumCyan -> MauveAurora glowing border.
 */
fun Modifier.stitchHorizonPanel(
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 1.2.dp,
    containerColor: Color = Color(0xFF0D0A1D)
): Modifier = this
    .clip(shape)
    .background(containerColor)
    .background(stitchHorizonWash(startAlpha = 0.18f, endAlpha = 0.14f))
    .border(borderWidth, stitchHorizonBrush(startAlpha = 0.75f, endAlpha = 0.60f), shape)

@Composable
fun Modifier.holographicCard(
    shape: Shape = RoundedCornerShape(18.dp),
    glowColor: Color = QuantumCyan,
    pulseColor: Color = MauveAurora,
    baseContainerColor: Color = Color(0xFF0D0A1D),
    borderWidth: Dp = 1.2.dp
): Modifier {
    val palette = LocalAppColors.current
    val effectiveBase = if (baseContainerColor != Color.Unspecified) {
        baseContainerColor
    } else if (palette.isLight) {
        palette.surface
    } else {
        Color(0xFF0D0A1D)
    }
    val borderBrush = if (palette.isLight) {
        SolidColor(palette.primary.copy(alpha = 0.45f))
    } else {
        Brush.horizontalGradient(
            listOf(glowColor.copy(alpha = 0.75f), pulseColor.copy(alpha = 0.60f))
        )
    }
    val wash: Brush? = if (palette.isLight) {
        null
    } else {
        Brush.horizontalGradient(
            listOf(
                glowColor.copy(alpha = 0.16f),
                Color.Transparent,
                pulseColor.copy(alpha = 0.12f)
            )
        )
    }

    var modifier = this.clip(shape).background(effectiveBase)
    if (wash != null) {
        modifier = modifier.background(wash)
    }
    return modifier.border(borderWidth, borderBrush, shape)
}

@Composable
fun QuantumCornerReticleOverlay(
    color: Color = QuantumCyan.copy(alpha = 0.5f),
    lineLength: Dp = 8.dp,
    strokeWidth: Dp = 1.5.dp
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val len = lineLength.toPx()
        val sw = strokeWidth.toPx()
        val pad = 6.dp.toPx()

        // Top-Left corner ┌
        drawLine(color, Offset(pad, pad), Offset(pad + len, pad), sw)
        drawLine(color, Offset(pad, pad), Offset(pad, pad + len), sw)

        // Top-Right corner ┐
        drawLine(color, Offset(size.width - pad, pad), Offset(size.width - pad - len, pad), sw)
        drawLine(color, Offset(size.width - pad, pad), Offset(size.width - pad, pad + len), sw)

        // Bottom-Left corner └
        drawLine(color, Offset(pad, size.height - pad), Offset(pad + len, size.height - pad), sw)
        drawLine(color, Offset(pad, size.height - pad), Offset(pad, size.height - pad - len), sw)

        // Bottom-Right corner ┘
        drawLine(color, Offset(size.width - pad, size.height - pad), Offset(size.width - pad - len, size.height - pad), sw)
        drawLine(color, Offset(size.width - pad, size.height - pad), Offset(size.width - pad, size.height - pad - len), sw)
    }
}

