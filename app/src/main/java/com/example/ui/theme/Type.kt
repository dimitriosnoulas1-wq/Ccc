package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// ============================================================================
// 4D QUANTUM TERMINAL 2126 TYPOGRAPHY SYSTEM
// ============================================================================

// Each file is a single Regular face. Asking the loader for Bold/Medium
// from that same file crashes some emulators on the first screen.
val SpaceGroteskFont = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal)
)

val JetBrainsMonoFont = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal)
)

val SyneFont = FontFamily(
    Font(R.font.syne, FontWeight.Normal)
)

val Typography = Typography(
    // Monumental Display (Big percentages, Singularity metrics & Hero Scores)
    displayLarge = TextStyle(
        fontFamily = SyneFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.5.sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = SyneFont,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.3.sp,
        color = TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily = SyneFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.3.sp,
        color = TextPrimary
    ),

    // Headlines & Titles (Clean futuristic SpaceGrotesk)
    headlineLarge = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.4.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.3.sp,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp,
        color = TextPrimary
    ),

    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),

    // Body Text (SpaceGrotesk)
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = SpaceGroteskFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
        color = TextSecondary
    ),

    // Telemetry, Badges, Timestamps, Numbers & Labels (JetBrains Mono)
    labelLarge = TextStyle(
        fontFamily = JetBrainsMonoFont,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMonoFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMonoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp,
        color = TextMuted
    )
)

// Telemetry & Numeric Data Helpers
val CosmicHeaderHeadline = TextStyle(
    fontFamily = SpaceGroteskFont,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    letterSpacing = 0.5.sp,
    color = TextPrimary
)

val CosmicHeaderSerif = TextStyle(
    fontFamily = SpaceGroteskFont,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    letterSpacing = 0.8.sp,
    color = TextPrimary
)

val CosmicDataMono = TextStyle(
    fontFamily = JetBrainsMonoFont,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    letterSpacing = 0.3.sp,
    color = TextPrimary
)

val QuantumScoreFont = TextStyle(
    fontFamily = SyneFont,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 28.sp,
    letterSpacing = 0.5.sp,
    color = QuantumCyan
)


