package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CycleCommandState
import com.example.data.model.MarketRegime
import com.example.ui.theme.AppThemePalette
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.holographicCard
import java.util.Locale
import kotlin.math.abs

@Composable
fun CycleCommandPanel(
    commandState: CycleCommandState = CycleCommandState(),
    isGreek: Boolean,
    palette: AppThemePalette,
    modifier: Modifier = Modifier
) {
    val regime = commandState.regime

    val regimeColor by animateColorAsState(
        targetValue = when (regime) {
            MarketRegime.ACCUMULATION -> TachyonMint
            MarketRegime.CYCLE_EXPANSION -> QuantumCyan
            MarketRegime.LEVERAGE_DISTRIBUTION -> NeonAmber
            MarketRegime.CYCLE_PEAK_EXIT -> SoftCrimson
        },
        label = "regimeColor"
    )

    val regimeTitle = if (isGreek) regime.titleEl else regime.titleEn
    val actionText = if (isGreek) regime.actionEl else regime.actionEn
    val summaryText = if (isGreek) commandState.keyStanceSummaryEl else commandState.keyStanceSummaryEn

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .holographicCard(
                shape = RoundedCornerShape(18.dp),
                glowColor = regimeColor
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(regimeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = regimeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isGreek) "⚡ CYCLE COMMAND CENTER" else "⚡ CYCLE COMMAND CENTER",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = palette.textPrimary
                    )
                    Text(
                        text = if (isGreek) "Actionable One-Pager • Ενιαία Σύνθεση Αγοράς" else "Actionable One-Pager • Market Regime Synthesis",
                        fontSize = 10.sp,
                        color = palette.textMuted
                    )
                }
            }

            // Regime Zone Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(regimeColor.copy(alpha = 0.15f))
                    .border(1.dp, regimeColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(regimeColor, CircleShape)
                    )
                    Text(
                        text = "ZONE ${regime.zoneNumber}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = regimeColor
                    )
                }
            }
        }

        // Action Recommendation Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(regimeColor.copy(alpha = 0.12f))
                .border(1.5.dp, regimeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isGreek) "ΠΡΟΤΕΙΝΟΜΕΝΗ ΣΤΑΣΗ (ACTION)" else "RECOMMENDED ACTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textMuted
                    )
                    Text(
                        text = actionText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = regimeColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isGreek) "Καθεστώς" else "Regime",
                        fontSize = 10.sp,
                        color = palette.textMuted
                    )
                    Text(
                        text = regimeTitle.substringAfter(":").trim(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1
                    )
                }
            }
        }

        // Composite Cycle Progress Bar (0 to 100)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isGreek) "Συνθετικός Δείκτης Κύκλου" else "Composite Cycle Score",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textSecondary
                )
                Text(
                    text = "${commandState.compositeCycleScore} / 100",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = regimeColor
                )
            }
            LinearProgressIndicator(
                progress = { (commandState.compositeCycleScore.toFloat() / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = regimeColor,
                trackColor = palette.background.copy(alpha = 0.5f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Accumulate (0)", fontSize = 8.sp, color = palette.textMuted)
                Text("Mid-Expansion (50)", fontSize = 8.sp, color = palette.textMuted)
                Text("Cycle Peak (100)", fontSize = 8.sp, color = palette.textMuted)
            }
        }

        // 4 Core Decision Pillars (2x2 Grid)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillarCard(
                    icon = Icons.Outlined.AutoGraph,
                    iconColor = NeonEmerald,
                    title = if (isGreek) "1. Halving Phase" else "1. Halving Phase",
                    mainValue = "Day ${commandState.halvingDaysElapsed} / 1460",
                    subValue = commandState.rainbowBandName,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                val etfPositive = commandState.etf5dNetFlowMillionUsd >= 0
                val etfText = String.format(Locale.US, "%s$%.0fM", if (etfPositive) "+" else "-", abs(commandState.etf5dNetFlowMillionUsd))
                val stChange = commandState.stablecoin7dChangeBillion
                val stText = String.format(Locale.US, "%s$%.2fB 7D", if (stChange >= 0) "+" else "-", abs(stChange))

                PillarCard(
                    icon = Icons.Outlined.AccountBalance,
                    iconColor = QuantumCyan,
                    title = if (isGreek) "2. Ρευστότητα" else "2. Global Liquidity",
                    mainValue = "ETF 5D: $etfText",
                    subValue = "Stables: $stText",
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fundingPct = commandState.fundingRatePercent
                val isHeated = commandState.isFundingHeated
                val fundingText = String.format(Locale.US, "%s%.3f%%", if (fundingPct >= 0) "+" else "-", abs(fundingPct))

                PillarCard(
                    icon = Icons.Outlined.LocalFireDepartment,
                    iconColor = if (isHeated) NeonAmber else NeonEmerald,
                    title = if (isGreek) "3. Μόχλευση / Heat" else "3. Derivatives Heat",
                    mainValue = "Funding: $fundingText",
                    subValue = if (isHeated) "High Squeeze Risk" else "Low Squeeze Risk",
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )

                PillarCard(
                    icon = Icons.Outlined.Psychology,
                    iconColor = NeonAmber,
                    title = if (isGreek) "4. Ψυχολογία" else "4. Sentiment & Alt",
                    mainValue = "F&G: ${commandState.fearAndGreedIndex} (Greed)",
                    subValue = "Alt Season: ${commandState.altcoinSeasonIndex}/100",
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Shared Market Stance Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(palette.background.copy(alpha = 0.5f))
                .border(1.dp, palette.border.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = regimeColor,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Text(
                    text = summaryText,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PillarCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    mainValue: String,
    subValue: String,
    palette: AppThemePalette,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background.copy(alpha = 0.4f))
            .border(1.dp, palette.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textMuted
                )
            }
            Text(
                text = mainValue,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = palette.textPrimary,
                maxLines = 1
            )
            Text(
                text = subValue,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textSecondary,
                maxLines = 1
            )
        }
    }
}
