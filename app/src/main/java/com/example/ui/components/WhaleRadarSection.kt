package com.example.ui.components

import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WhaleAlert
import com.example.data.model.WhaleAlertType
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.SoftEmerald
import com.example.ui.theme.TextCyanSlate
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.holographicCard
import com.example.util.LocalAppStrings

@Composable
fun WhaleRadarSection(
    alerts: List<WhaleAlert>,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var selectedFilter by remember { mutableStateOf<WhaleAlertType?>(null) }
    var isExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val filteredAlerts = if (selectedFilter == null) {
        alerts
    } else {
        alerts.filter { it.type == selectedFilter }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .holographicCard(
                shape = RoundedCornerShape(16.dp),
                glowColor = QuantumCyan,
                pulseColor = QuantumCyan
            )
            .padding(14.dp)
            .testTag("whale_radar_section")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Compact Header Row
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
                            .size(9.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(QuantumCyan)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = strings.whaleRadarTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = QuantumCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(QuantumCyan.copy(alpha = 0.15f))
                        .border(1.dp, QuantumCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (alerts.isNotEmpty()) "LIVE" else "WAITING",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan,
                        maxLines = 1
                    )
                }
            }

            // Compact Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChipItem(
                    label = strings.whaleFilterAll,
                    isSelected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
                FilterChipItem(
                    label = strings.whaleFilterInflow,
                    isSelected = selectedFilter == WhaleAlertType.EXCHANGE_INFLOW,
                    onClick = { selectedFilter = WhaleAlertType.EXCHANGE_INFLOW }
                )
                FilterChipItem(
                    label = strings.whaleFilterOutflow,
                    isSelected = selectedFilter == WhaleAlertType.EXCHANGE_OUTFLOW,
                    onClick = { selectedFilter = WhaleAlertType.EXCHANGE_OUTFLOW }
                )
                FilterChipItem(
                    label = strings.whaleFilterBuy,
                    isSelected = selectedFilter == WhaleAlertType.WHALE_BUY,
                    onClick = { selectedFilter = WhaleAlertType.WHALE_BUY }
                )
            }

            // 3-Metric Summary Card (24h Inflow | 24h Outflow | Net Flow)
            val inflow24h = remember(alerts) {
                alerts.filter { it.type == WhaleAlertType.EXCHANGE_INFLOW }.sumOf { it.amountUsd }
            }
            val outflow24h = remember(alerts) {
                alerts.filter { it.type == WhaleAlertType.EXCHANGE_OUTFLOW }.sumOf { it.amountUsd }
            }
            val netFlow = outflow24h - inflow24h

            fun formatFlowUsd(amount: Double): String {
                val prefix = if (amount < 0) "-" else ""
                val absVal = kotlin.math.abs(amount)
                return prefix + com.example.util.AppNumberFormatter.formatCompactCurrency(absVal)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 24h Inflow (Sell risk)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "24h Inflow",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextCyanSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatFlowUsd(inflow24h),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = SoftCrimson,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(CosmicBorder)
                )

                // 24h Outflow (Accumulation)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "24h Outflow",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextCyanSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatFlowUsd(outflow24h),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = SoftEmerald,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(CosmicBorder)
                )

                // Net Flow
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Net Flow",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextCyanSlate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${if (netFlow >= 0) "+" else "-"}${formatFlowUsd(netFlow)}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (netFlow >= 0) SoftEmerald else SoftCrimson,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Display top 2 alerts compactly, or all if expanded
            val displayList = if (isExpanded) filteredAlerts.take(6) else filteredAlerts.take(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayList.forEach { alert ->
                    HolographicWhaleAlertCard(alert = alert)
                }
            }

            // Expand / Collapse toggle button if more items exist
            if (filteredAlerts.size > 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) strings.whaleCollapse else String.format(strings.whaleViewAll, filteredAlerts.size),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = QuantumCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) QuantumCyan else CosmicVoidSurfaceElevated)
            .border(
                1.dp,
                if (isSelected) QuantumCyan else CosmicBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            color = if (isSelected) Color(0xFF05050F) else TextCyanSlate,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

