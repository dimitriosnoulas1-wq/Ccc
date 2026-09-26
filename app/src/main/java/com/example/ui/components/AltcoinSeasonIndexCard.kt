package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AltcoinSeasonData
import com.example.data.model.CryptoCoin
import com.example.ui.theme.GainGreen
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.PhotonGoldBright
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.QuantumCyanBright
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.TextCyanSlate
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.holographicCard
import com.example.util.LocalAppStrings
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AltcoinSeasonIndexCard(
    altData: AltcoinSeasonData,
    coins: List<CryptoCoin>,
    isProUnlocked: Boolean = true,
    onOpenProModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val isGreek = strings.language.code == "el"

    val activeZoneColor = when {
        altData.isAltSeason -> SoftEmerald
        altData.isBtcSeason -> PhotonGoldBright
        else -> QuantumCyan
    }

    val activeZoneName = when {
        altData.isAltSeason -> strings.altSeasonAltSeasonLabel
        altData.isBtcSeason -> strings.altSeasonBtcSeasonLabel
        else -> strings.altSeasonNeutralLabel
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .holographicCard(
                shape = RoundedCornerShape(22.dp),
                glowColor = activeZoneColor,
                pulseColor = if (altData.isBtcSeason) PhotonGold else QuantumCyan
            )
            .padding(18.dp)
            .testTag("altcoin_season_index_card")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(activeZoneColor.copy(alpha = 0.15f))
                            .border(1.dp, activeZoneColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Altcoin Season",
                            tint = activeZoneColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.altSeasonHeaderTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPureWhite
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.altSeasonSubtitle,
                    fontSize = 11.5.sp,
                    color = TextCyanSlate
                )
            }

            // Score Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(activeZoneColor.copy(alpha = 0.15f))
                    .border(1.dp, activeZoneColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (altData.isAvailable) "${altData.score} / 100" else "—",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = activeZoneColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Futuristic Holographic Radial Gauge
        HolographicRadialGauge(
            value = altData.score.toFloat(),
            height = 145.dp,
            statusLabel = activeZoneName,
            startLabel = "⚡ " + strings.altSeasonBtcSeasonLabel,
            endLabel = strings.altSeasonAltSeasonLabel + " 🚀",
            gaugeColors = listOf(
                0.0f to PhotonGoldBright,
                0.5f to QuantumCyan,
                1.0f to SoftEmerald
            ),
            activeColor = activeZoneColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Glass Stat Badges Row: Regime | 90D BTC | Top 50 Outperformed | Total Tested
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x330B132B))
                    .border(1.dp, Color(0x3300D2FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "STATUS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCyanSlate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (altData.isBtcSeason) "BTC Reg." else if (altData.isAltSeason) "Alt Season" else "Neutral",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = activeZoneColor
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x330B132B))
                    .border(1.dp, Color(0x3300D2FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BTC 90D",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCyanSlate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (altData.isAvailable) "+${String.format("%.1f", altData.btcGain90d)}%" else "—",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = PhotonGoldBright
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x330B132B))
                    .border(1.dp, Color(0x3300D2FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOP 50",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCyanSlate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${altData.top50OutperformedCount}/${altData.totalTop50Count}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPureWhite
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x330B132B))
                    .border(1.dp, Color(0x3300D2FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BENCHMARK",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextCyanSlate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "75% Target",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = QuantumCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Top 90D Outperformers List
        Text(
            text = strings.topPerformers90d.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextCyanSlate,
            letterSpacing = 0.6.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            altData.topOutperformingCoins.take(5).forEach { (sym, gainPct) ->
                val relativeGainVsBtc = gainPct - altData.btcGain90d
                val sign = if (relativeGainVsBtc >= 0) "+" else ""
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x22131F3F))
                        .border(1.dp, Color(0x1800D2FF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sym,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPureWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "vs BTC ($sign${String.format("%.1f", relativeGainVsBtc)}%)",
                            fontSize = 11.sp,
                            color = TextCyanSlate
                        )
                    }

                    Text(
                        text = "+${String.format("%.1f", gainPct)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = SoftEmerald
                    )
                }
            }
        }
    }
}
