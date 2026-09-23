package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.WhaleAlert
import com.example.data.model.WhaleAlertType
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.PhotonGoldBright
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.QuantumCyanBright
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.TextCyanSlate
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.holographicCard
import com.example.util.LocalAppStrings
import kotlin.math.sqrt

/**
 * Ultra-Futuristic Whale Alert Item with Gravitational Ripple Shockwave (Year 3026 Aesthetic).
 *
 * Features:
 * 1. holographicCard base container with reactive glow borders.
 * 2. One-shot "Gravitational Ripple" Canvas animation that emits an expanding shockwave from the
 *    origin coin avatar across the card when a new alert is received or rendered.
 * 3. High-contrast typography with Quantum Cyan, Soft Crimson, Photon Gold & Soft Emerald accents.
 */
@Composable
fun HolographicWhaleAlertCard(
    alert: WhaleAlert,
    modifier: Modifier = Modifier,
    triggerRippleOnMount: Boolean = true
) {
    val strings = LocalAppStrings.current

    // Semantic Holographic Color Mapping
    val accentColor = when (alert.type) {
        WhaleAlertType.EXCHANGE_INFLOW -> SoftCrimson
        WhaleAlertType.EXCHANGE_OUTFLOW -> SoftEmerald
        WhaleAlertType.WHALE_BUY -> QuantumCyanBright
        WhaleAlertType.WHALE_TRANSFER -> PhotonGoldBright
    }

    // Relative Time Calculation
    val timeAgoStr = remember(alert.timestamp, strings.language) {
        val diff = (System.currentTimeMillis() - alert.timestamp).coerceAtLeast(0)
        when {
            diff < 60_000 -> strings.timeJustNow
            diff < 3600_000 -> {
                val mins = (diff / 60_000).coerceAtLeast(1)
                String.format(strings.timeMinsAgo, mins)
            }
            else -> {
                val hrs = (diff / 3600_000).coerceAtLeast(1)
                String.format(strings.timeHoursAgo, hrs)
            }
        }
    }

    // One-Shot Gravitational Ripple Animation
    val rippleProgress = remember { Animatable(if (triggerRippleOnMount) 0f else 1f) }
    LaunchedEffect(alert.id, alert.timestamp) {
        rippleProgress.snapTo(0f)
        rippleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
        )
    }

    val sizeMagnitude = remember(alert.amountUsd) {
        (alert.amountUsd / 15_000_000.0).toFloat().coerceIn(0.8f, 3.5f)
    }
    val isMegaWhale = alert.amountUsd >= 50_000_000.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .holographicCard(
                shape = RoundedCornerShape(14.dp),
                glowColor = accentColor,
                pulseColor = if (alert.type == WhaleAlertType.EXCHANGE_INFLOW) SoftCrimson else QuantumCyan
            )
            .testTag("whale_alert_card_${alert.id}")
    ) {
        // Gravitational Ripple Canvas Shockwave Overlay (Magnitude-Scaled)
        if (rippleProgress.value < 1f) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(14.dp))
            ) {
                val p = rippleProgress.value
                val maxDimension = sqrt(size.width * size.width + size.height * size.height)

                // Origin is set to the coin avatar center (top-left approx 24dp, 24dp)
                val originX = 24.dp.toPx()
                val originY = 24.dp.toPx()
                val origin = Offset(originX, originY)

                val primaryRadius = p * (maxDimension * (0.85f * sizeMagnitude.coerceAtMost(2.0f)))
                val primaryAlpha = ((1f - p) * (0.70f * sizeMagnitude.coerceAtMost(1.4f))).coerceIn(0f, 1f)

                // Screen-wide ambient pulse for mega whales ($50M+)
                if (isMegaWhale) {
                    drawRect(
                        color = accentColor.copy(alpha = (1f - p) * 0.18f * (sizeMagnitude / 2f).coerceAtMost(1f))
                    )
                }

                // 1. Expanding Gravitational Wave Glow Fill
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = primaryAlpha * 0.35f),
                            Color.Transparent
                        ),
                        center = origin,
                        radius = primaryRadius.coerceAtLeast(1f)
                    ),
                    radius = primaryRadius.coerceAtLeast(1f),
                    center = origin
                )

                // 2. Primary Shockwave Ring
                drawCircle(
                    color = accentColor.copy(alpha = primaryAlpha),
                    radius = primaryRadius.coerceAtLeast(1f),
                    center = origin,
                    style = Stroke(width = (2.8.dp.toPx() * sizeMagnitude.coerceAtMost(1.8f) * (1f - p * 0.5f)).coerceAtLeast(1f))
                )

                // 3. Secondary Trailing Harmonic Ripple
                if (p > 0.12f) {
                    val p2 = (p - 0.12f) / 0.88f
                    val secondaryRadius = p2 * (maxDimension * (0.75f * sizeMagnitude.coerceAtMost(1.8f)))
                    val secondaryAlpha = ((1f - p2) * 0.50f * sizeMagnitude.coerceAtMost(1.3f)).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = secondaryAlpha),
                        radius = secondaryRadius.coerceAtLeast(1f),
                        center = origin,
                        style = Stroke(width = (1.2.dp.toPx() * sizeMagnitude.coerceAtMost(1.5f)).coerceAtLeast(0.8f))
                    )
                }

                // 4. Tertiary High-Energy Outer Halo for $100M+ mega alerts
                if (isMegaWhale && p > 0.25f) {
                    val p3 = (p - 0.25f) / 0.75f
                    val tertiaryRadius = p3 * (maxDimension * 1.1f)
                    val tertiaryAlpha = ((1f - p3) * 0.40f).coerceIn(0f, 1f)
                    drawCircle(
                        color = PhotonGoldBright.copy(alpha = tertiaryAlpha),
                        radius = tertiaryRadius.coerceAtLeast(1f),
                        center = origin,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
        }

        // Card Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Coin Avatar, Ticker, Amount & USD Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.18f))
                            .border(1.dp, accentColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CoinAvatar(
                            symbol = alert.coinSymbol,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = alert.coinSymbol,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPureWhite,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Transaction Type Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.15f))
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = alert.type.displayName.uppercase(),
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            text = alert.formattedAmount,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = TextCyanSlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // USD Valuation Readout
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = alert.formattedUsd,
                        fontSize = 14.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = timeAgoStr,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextCyanSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Transfer Route (Source -> Destination)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x2B0B132B))
                    .border(1.dp, Color(0x1800D2FF), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${alert.source}  ➔  ${alert.destination}",
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = TextCyanSlate,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Market Impact Analysis Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = alert.getLocalizedImpact(strings.language == AppLanguage.GREEK),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPureWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
