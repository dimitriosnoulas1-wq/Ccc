package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.CycleComparativeRepository
import com.example.ui.theme.DrawdownRed
import com.example.ui.theme.GainGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PhotonGoldBright
import com.example.util.LocalAppStrings

@Composable
fun CycleDaysTeaserCard(
    onNavigateToMacro: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { CycleComparativeRepository() }
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val isGreek = strings.language.code == "el"

    val daysSinceAth = remember { repository.getLiveDaysSinceAth() }
    val daysUntilHistoricalBottom = remember { (CycleComparativeRepository.HISTORICAL_AVG_BOTTOM_DAY - daysSinceAth).coerceAtLeast(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_orbital_clock")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val orbitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_sweep"
    )
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hud_shimmer"
    )

    val cardBorderBrush = Brush.horizontalGradient(
        colors = listOf(
            GoldPrimary.copy(alpha = 0.40f),
            NeonCyan.copy(alpha = 0.25f),
            GoldPrimary.copy(alpha = 0.50f)
        ),
        startX = shimmerOffset * 400f,
        endX = shimmerOffset * 400f + 300f
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF070F1E).copy(alpha = 0.88f))
            .border(1.dp, cardBorderBrush, RoundedCornerShape(14.dp))
            .clickable {
                com.example.util.AppSoundManager.playTechClick()
                onNavigateToMacro()
            }
            .testTag("cycle_days_teaser_card")
    ) {
        // Cosmic Orbital Radial HUD Lines & Orbit Geometry Background Canvas
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
        ) {
            val w = size.width
            val h = size.height
            val center = Offset(w * 0.88f, h * 0.35f)

            // 1. Subtle Golden & Cyan Orbital Ring Arcs
            val ring1Radius = 55.dp.toPx()
            val ring2Radius = 90.dp.toPx()
            val ring3Radius = 135.dp.toPx()

            val dashedStroke = Stroke(
                width = 0.8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), orbitPhase)
            )

            drawCircle(
                color = GoldPrimary.copy(alpha = 0.12f),
                radius = ring1Radius,
                center = center,
                style = dashedStroke
            )
            drawCircle(
                color = NeonCyan.copy(alpha = 0.08f),
                radius = ring2Radius,
                center = center,
                style = dashedStroke
            )
            drawCircle(
                color = GoldPrimary.copy(alpha = 0.05f),
                radius = ring3Radius,
                center = center,
                style = dashedStroke
            )

            // 2. Orbital Satellite Node on Arc
            val satAngleRad = Math.toRadians((orbitPhase * 0.75).toDouble())
            val satX = (center.x + ring1Radius * Math.cos(satAngleRad)).toFloat()
            val satY = (center.y + ring1Radius * Math.sin(satAngleRad)).toFloat()

            drawCircle(
                color = GoldPrimary.copy(alpha = 0.25f),
                radius = 4.dp.toPx(),
                center = Offset(satX, satY)
            )
            drawCircle(
                color = PhotonGoldBright,
                radius = 1.6.dp.toPx(),
                center = Offset(satX, satY)
            )

            // 3. Ambient Corner Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldPrimary.copy(alpha = 0.08f), Color.Transparent),
                    center = center,
                    radius = ring2Radius
                ),
                radius = ring2Radius,
                center = center
            )
        }

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Cosmic Timekeeper HUD Badge + Monospace Day Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.cycleDaysTeaserBadge.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = NeonCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .border(0.8.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = if (isGreek) "DAY +$daysSinceAth POST-ATH" else "DAY +$daysSinceAth POST-ATH",
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = GoldPrimary
                    )
                }
            }

            // Headline
            Text(
                text = strings.cycleDaysTeaserTitle,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )

            // Subtitle with historical comparison
            Text(
                text = if (isGreek)
                    "Στην Ημέρα $daysSinceAth: Το BTC βρισκόταν στα $19.450 (2022) & $6.320 (2018). Ο ιστορικός πυθμένας σχηματίζεται ~1 έτος μετά το ATH (Ημέρες 363-376). Απομένουν ~$daysUntilHistoricalBottom ημέρες."
                else
                    "On Day $daysSinceAth: BTC was at $19,450 (2022) & $6,320 (2018). Macro bottom forms ~1 year post-ATH (Days 363-376). ~$daysUntilHistoricalBottom days remaining.",
                fontSize = 11.sp,
                color = palette.textSecondary,
                lineHeight = 15.5.sp
            )

            // Dual Glass Pods with Holographic Divider Accent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pod 1: 2022 Analog Glass Pod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C182E).copy(alpha = 0.70f))
                        .border(0.8.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 7.dp)
                ) {
                    Column {
                        Text(
                            text = "2022 (Day $daysSinceAth)",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = NeonCyan.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$19,450",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "-71.7% ATH",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = DrawdownRed
                        )
                    }
                }

                // Holographic Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.75f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    GoldPrimary.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Pod 2: 2018 Analog Glass Pod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C182E).copy(alpha = 0.70f))
                        .border(0.8.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 7.dp)
                ) {
                    Column {
                        Text(
                            text = "2018 (Day $daysSinceAth)",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = GoldPrimary.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$6,320",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary
                        )
                        Text(
                            text = "-68.1% ATH",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = DrawdownRed
                        )
                    }
                }

                // Holographic Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.75f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    GainGreen.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Pod 3: 1-Yr Bottom Window Pod
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GainGreen.copy(alpha = 0.12f))
                        .border(0.8.dp, GainGreen.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 7.dp)
                ) {
                    Column {
                        Text(
                            text = if (isGreek) "ΠΥΘΜΕΝΑΣ" else "1-YR BOTTOM",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GainGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isGreek) "~$daysUntilHistoricalBottom ημ." else "~$daysUntilHistoricalBottom d",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GainGreen
                        )
                        Text(
                            text = "Days 363-376",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = GainGreen.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Bottom telemetry link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isGreek) "Αληθινά γεγονότα κραχ & ανάλυση κύκλων" else "Cosmic cycle breakdown & real crash analogs",
                    fontSize = 10.sp,
                    color = palette.textMuted
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isGreek) "Άνοιγμα Ανάλυσης" else "Explore Analog",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
