package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuditOutcomeStatus
import com.example.data.model.ForwardSignalAuditEntry
import com.example.data.model.MarketRegime
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.holographicCard
import java.util.Locale

@Composable
fun ForwardAuditTrailCard(
    auditLogs: List<ForwardSignalAuditEntry>,
    isGreek: Boolean
) {
    val palette = LocalAppColors.current
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .holographicCard(
                shape = RoundedCornerShape(16.dp),
                glowColor = NeonEmerald
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
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
                        .background(NeonEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isGreek) "FORWARD AUDIT TRAIL" else "FORWARD AUDIT TRAIL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "DEMO DATA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonEmerald
                            )
                        }
                    }
                    Text(
                        text = if (isGreek) "Διαφανές Ζωντανό Αρχείο Σημάτων (Επαλήθευση σε Πραγματικό Χρόνο)" else "Transparent Live Forward Signal Log (Real-Time Verification)",
                        fontSize = 10.sp,
                        color = palette.textMuted
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = palette.textSecondary
            )
        }

        // Trust Summary Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(palette.background.copy(alpha = 0.5f))
                .border(1.dp, palette.border.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "4 / 4",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonEmerald
                    )
                    Text(
                        text = if (isGreek) "Σήματα Κύκλου" else "Major Signals",
                        fontSize = 9.sp,
                        color = palette.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(palette.border.copy(alpha = 0.3f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+28.2% Avg",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonCyan
                    )
                    Text(
                        text = if (isGreek) "Μέση Απόδοση/Προστασία" else "Avg Gain/Protection",
                        fontSize = 9.sp,
                        color = palette.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(palette.border.copy(alpha = 0.3f))
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "100%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonEmerald
                    )
                    Text(
                        text = if (isGreek) "Ακρίβεια Risk Calls" else "Risk Accuracy",
                        fontSize = 9.sp,
                        color = palette.textMuted
                    )
                }
            }
        }

        // List of entries (Show top 2 if collapsed, all if expanded)
        val entriesToShow = if (isExpanded) auditLogs else auditLogs.take(2)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            entriesToShow.forEach { entry ->
                AuditEntryItem(
                    entry = entry,
                    isGreek = isGreek
                )
            }
        }

        if (!isExpanded && auditLogs.size > 2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = true }
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isGreek) "Προβολή Όλων των Σημάτων (${auditLogs.size}) ▼" else "View Full Audit History (${auditLogs.size}) ▼",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = QuantumCyan
                )
            }
        }
    }
}

@Composable
private fun AuditEntryItem(
    entry: ForwardSignalAuditEntry,
    isGreek: Boolean
) {
    val palette = LocalAppColors.current

    val tagColor = when (entry.regime) {
        MarketRegime.ACCUMULATION -> NeonEmerald
        MarketRegime.CYCLE_EXPANSION -> NeonCyan
        MarketRegime.LEVERAGE_DISTRIBUTION -> NeonAmber
        MarketRegime.CYCLE_PEAK_EXIT -> Color(0xFFFF5252)
    }

    val isPending = entry.outcomeStatus == AuditOutcomeStatus.ACTIVE_TRACKING

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background.copy(alpha = 0.4f))
            .border(1.dp, tagColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(tagColor, CircleShape)
                    )
                    Text(
                        text = entry.displayDate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textSecondary
                    )
                }

                // Outcome Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPending) QuantumCyan.copy(alpha = 0.15f) else NeonEmerald.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPending) Icons.Default.HourglassTop else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isPending) QuantumCyan else NeonEmerald,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = if (isGreek) entry.outcomeStatus.titleEl else entry.outcomeStatus.titleEn,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPending) QuantumCyan else NeonEmerald
                        )
                    }
                }
            }

            // Signal Title & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.signalType,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = tagColor
                )
                Text(
                    text = String.format(Locale.US, "BTC: $%,.0f", entry.btcPriceAtSignalUsd),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = palette.textPrimary
                )
            }

            // Outcome / Result Row
            if (entry.performancePercent != null && entry.verificationOutcomePriceUsd != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(palette.background.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGreek) "Αποτέλεσμα Έπειτα:" else "Outcome Follow-Through:",
                        fontSize = 10.sp,
                        color = palette.textMuted
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "$%,.0f  (+%.2f%% Gain/Prot)",
                            entry.verificationOutcomePriceUsd,
                            entry.performancePercent
                        ),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonEmerald
                    )
                }
            }

            // Key Evidence Note
            Text(
                text = if (isGreek) entry.keyEvidenceEl else entry.keyEvidenceEn,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = palette.textSecondary
            )
        }
    }
}
