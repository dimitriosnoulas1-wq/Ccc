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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FearAndGreedData
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.PhotonGoldBright
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.TextCyanSlate
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.holographicCard
import com.example.util.LocalAppStrings
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FearAndGreedIndexCard(
    fearGreedData: FearAndGreedData,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val isGreek = strings.language.code == "el"

    val sentimentColor = when {
        fearGreedData.score >= 75 -> SoftEmerald // Extreme Greed
        fearGreedData.score >= 55 -> Color(0xFF84CC16) // Greed
        fearGreedData.score >= 45 -> PhotonGoldBright // Neutral
        fearGreedData.score >= 25 -> PhotonGold // Fear
        else -> SoftCrimson // Extreme Fear
    }

    val sentimentLabel = when {
        fearGreedData.score >= 75 -> strings.extremeGreedLabel
        fearGreedData.score >= 55 -> strings.greedLabel
        fearGreedData.score >= 45 -> strings.neutralLabel
        fearGreedData.score >= 25 -> strings.fearLabel
        else -> strings.extremeFearLabel
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .holographicCard(
                shape = RoundedCornerShape(24.dp),
                glowColor = sentimentColor,
                pulseColor = if (fearGreedData.score >= 50) QuantumCyan else SoftCrimson
            )
            .padding(18.dp)
            .testTag("fear_greed_index_card")
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
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(sentimentColor.copy(alpha = 0.15f))
                            .border(1.dp, sentimentColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = "Fear & Greed",
                            tint = sentimentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.fearGreedHeaderTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPureWhite
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = strings.fearGreedSubtitle,
                    fontSize = 12.sp,
                    color = TextCyanSlate
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Futuristic Holographic Speedometer Radial Gauge
        HolographicRadialGauge(
            value = fearGreedData.score.toFloat(),
            height = 155.dp,
            statusLabel = sentimentLabel,
            startLabel = "0 (FEAR)",
            endLabel = "100 (GREED)",
            gaugeColors = listOf(
                0.0f to SoftCrimson,
                0.3f to PhotonGold,
                0.55f to PhotonGoldBright,
                0.75f to Color(0xFF84CC16),
                1.0f to SoftEmerald
            ),
            activeColor = sentimentColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Historical Sentiments Grid (Yesterday, Last Week, Last Month)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HistoricalPill(
                label = strings.fearGreedYesterday,
                score = fearGreedData.yesterdayScore,
                modifier = Modifier.weight(1f),
                palette = palette
            )
            HistoricalPill(
                label = strings.fearGreedLastWeek,
                score = fearGreedData.lastWeekScore,
                modifier = Modifier.weight(1f),
                palette = palette
            )
            HistoricalPill(
                label = strings.fearGreedLastMonth,
                score = fearGreedData.lastMonthScore,
                modifier = Modifier.weight(1f),
                palette = palette
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Psychology Tip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.primary.copy(alpha = 0.08f))
                .border(1.dp, palette.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Text(
                text = strings.fearGreedAccumulationTip,
                fontSize = 11.sp,
                color = palette.textSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun HistoricalPill(
    label: String,
    score: Int,
    modifier: Modifier = Modifier,
    palette: com.example.ui.theme.AppThemePalette
) {
    val pillColor = when {
        score >= 75 -> Color(0xFF00FF88)
        score >= 55 -> Color(0xFF84CC16)
        score >= 45 -> Color(0xFFF59E0B)
        score >= 25 -> Color(0xFFF97316)
        else -> Color(0xFFF43F5E)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (palette.isLight) Color(0xFFF8FAFC) else Color(0xFF111827))
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "$score",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = pillColor
            )
        }
    }
}
