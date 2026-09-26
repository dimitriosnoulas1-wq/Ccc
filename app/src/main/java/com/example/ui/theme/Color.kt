package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// CHRONO 2126 QUANTUM SUB-SPACE — UNIFORM COLOR PALETTE
// ============================================================================

// Deep Space Surfaces (4D Quantum Terminal Canvas)
val CosmicVoidBg = Color(0xFF05050F)
val CosmicVoidSurface = Color(0xFF0D0A1D)
val CosmicVoidSurfaceElevated = Color(0xFF140D2E)
val CosmicVoidGlass = Color(0xBF0D0A1D)
val ChronoNavBg = Color(0xFF080614)

// Quantum Violet & Mauve — 4D Chrono tension, secondary highlights
val PhotonGold = Color(0xFFF59E0B)           // Cyber Amber/Gold for Pro
val PhotonGoldBright = Color(0xFFFBBF24)     // Bright Gold for Pro
val PhotonGoldMuted = Color(0x26F59E0B)

val MauveAurora = Color(0xFFC084FC)          // Quantum Violet / Lavender
val MauveDeep = Color(0xFF7928CA)            // Sub-space Violet

// Quantum Cyan & Electric Mint — Primary identity, active tabs, LIVE feed, gains
val TachyonMint = Color(0xFF00FF88)          // Pure Neon Green / Mint
val TachyonMintBright = Color(0xFFD4FFE9)

val QuantumCyan = Color(0xFF00F5FF)          // Holographic Quantum Cyan
val QuantumCyanBright = Color(0xFFE0FFFF)
val QuantumBlue = Color(0xFF00D2FF)

val SoftEmerald = Color(0xFF00FF88)
val SoftCrimson = Color(0xFFF43F5E)          // Neon Coral / Red

val TextPureWhite = Color(0xFFFFFFFF)
val TextCyanSlate = Color(0xFFCBD5E1)
val TextMutedSlate = Color(0xFF64748B)

val CosmicNavyBg = CosmicVoidBg
val CosmicNavySurface = CosmicVoidSurface
val CosmicNavySurfaceElevated = CosmicVoidSurfaceElevated

val CosmicBorder = Color(0x3800F5FF)     // Luminous Quantum Cyan glowing border
val CosmicBorderGlow = Color(0x6600F5FF) // High-contrast border glow

val CosmicDarkBg = CosmicVoidBg
val CosmicSurface = CosmicVoidSurface
val CosmicSurfaceElevated = CosmicVoidSurfaceElevated

val GoldPrimary = PhotonGold
val CopperAccent = QuantumCyan
val NeonAmber = PhotonGold

val SapphireBlueBright = QuantumCyan
val NeonCyan = QuantumCyan
val NeonPurple = MauveAurora

val MarketGreen = SoftEmerald
val MarketRed = SoftCrimson
val GainGreen = SoftEmerald
val DrawdownRed = SoftCrimson
val NeonEmerald = SoftEmerald

val TextPrimary = TextPureWhite
val TextSecondary = TextCyanSlate
val TextMuted = TextMutedSlate

val ProGoldBg = PhotonGoldMuted

fun stitchHorizonBrush(
    startAlpha: Float = 0.85f,
    endAlpha: Float = 0.70f
): Brush = Brush.horizontalGradient(
    colors = listOf(
        QuantumCyan.copy(alpha = startAlpha),
        MauveAurora.copy(alpha = endAlpha)
    )
)

fun stitchHorizonWash(
    startAlpha: Float = 0.18f,
    endAlpha: Float = 0.14f
): Brush = Brush.horizontalGradient(
    colors = listOf(
        QuantumCyan.copy(alpha = startAlpha),
        Color.Transparent,
        MauveAurora.copy(alpha = endAlpha)
    )
)

fun stitchVoidBackground(): Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0C061A),
        Color(0xFF05050F),
        Color(0xFF080414),
        Color(0xFF05050F)
    )
)

data class AppThemePalette(
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val borderGlow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val secondary: Color,
    val isLight: Boolean,
    val isMonochrome: Boolean = false,
    val gainColor: Color = MarketGreen,
    val lossColor: Color = MarketRed,
    val cardBackground: Color = surface,
    val navBarBackground: Color = surface,
    val goldAccent: Color = GoldPrimary,
    val sapphireAccent: Color = SapphireBlueBright
)

val GalaxyDarkPalette = AppThemePalette(
    background = CosmicNavyBg,
    backgroundSecondary = ChronoNavBg,
    surface = CosmicNavySurface,
    surfaceElevated = CosmicNavySurfaceElevated,
    border = CosmicBorder,
    borderGlow = CosmicBorderGlow,
    textPrimary = TextPrimary,
    textSecondary = Color(0xFF94A3B8),
    textMuted = TextMuted,
    primary = QuantumCyan,
    secondary = MauveAurora,
    isLight = false,
    isMonochrome = false,
    gainColor = MarketGreen,
    lossColor = MarketRed,
    cardBackground = CosmicVoidGlass,
    navBarBackground = ChronoNavBg,
    goldAccent = PhotonGold,
    sapphireAccent = QuantumCyan
)

val PureBlackPalette = AppThemePalette(
    background = Color(0xFF05050F),
    backgroundSecondary = Color(0xFF0A0518),
    surface = Color(0xFF14121E),
    surfaceElevated = Color(0xFF1C1A26),
    border = CosmicBorder,
    borderGlow = Color(0x3300F5FF),
    textPrimary = TextPrimary,
    textSecondary = Color(0xFF94A3B8),
    textMuted = TextMuted,
    primary = QuantumCyan,
    secondary = MauveAurora,
    isLight = false,
    isMonochrome = false,
    gainColor = MarketGreen,
    lossColor = MarketRed,
    cardBackground = CosmicVoidGlass,
    navBarBackground = ChronoNavBg,
    goldAccent = PhotonGold,
    sapphireAccent = QuantumCyan
)

val CleanWhitePalette = AppThemePalette(
    background = Color(0xFFF8FAFC),
    backgroundSecondary = Color(0xFFF1F5F9),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF8FAFC),
    border = Color(0xFFE2E8F0),
    borderGlow = Color(0x3300F5FF),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF94A3B8),
    primary = Color(0xFF0284C7),
    secondary = Color(0xFF7C3AED),
    isLight = true,
    isMonochrome = false,
    gainColor = Color(0xFF059669),
    lossColor = MarketRed,
    cardBackground = Color(0xFFFFFFFF),
    navBarBackground = Color(0xFFFFFFFF),
    goldAccent = Color(0xFFD97706),
    sapphireAccent = Color(0xFF0284C7)
)

