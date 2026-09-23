package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertCategory
import com.example.data.model.AlertHistoryItem
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.holographicCard
import com.example.util.AlertHistoryManager
import com.example.util.NotificationHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertHistorySheet(
    alerts: List<AlertHistoryItem>,
    isGreek: Boolean,
    onDismiss: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val palette = LocalAppColors.current
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(AlertCategory.ALL) }

    val filteredAlerts = remember(alerts, selectedCategory) {
        if (selectedCategory == AlertCategory.ALL) alerts
        else alerts.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = palette.border) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(QuantumCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = QuantumCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isGreek) "30D PUSH ALERT AUDIT LOG" else "30D PUSH ALERT AUDIT LOG",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = palette.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(TachyonMint.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${alerts.size} LOGGED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TachyonMint
                                )
                            }
                        }
                        Text(
                            text = if (isGreek) "Ιστορικό ειδοποιήσεων για Funding, Whales, ETFs & Cycles" else "Verified historical alerts triggered in background & live",
                            fontSize = 10.sp,
                            color = palette.textMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = palette.textSecondary
                    )
                }
            }

            // Quick Actions: Test Alert & Mark All Read
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        NotificationHelper.sendFundingAlertNotification(
                            context = context,
                            title = "⚡ TEST: BINANCE FUNDING SPIKE (>+0.05%)",
                            message = "Δοκιμαστική ειδοποίηση: Ακραία υπερθέρμανση Longs στα παράγωγα.",
                            details = "Η μηχανή ειδοποιήσεων WorkManager λειτουργεί πλήρως σε background κατάσταση."
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QuantumCyan.copy(alpha = 0.15f),
                        contentColor = QuantumCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isGreek) "Δοκιμή Alert" else "Test Push Alert",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { AlertHistoryManager.markAllAsRead() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.surfaceElevated.copy(alpha = 0.5f),
                        contentColor = palette.textSecondary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isGreek) "Ανάγνωση Όλων" else "Mark All Read",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AlertCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) QuantumCyan.copy(alpha = 0.2f)
                                else palette.surfaceElevated.copy(alpha = 0.4f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) QuantumCyan else palette.border.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${cat.emoji} ${if (isGreek) cat.labelEl else cat.labelEn}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) QuantumCyan else palette.textSecondary
                        )
                    }
                }
            }

            // Alert List
            if (filteredAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = palette.textMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (isGreek) "Δεν βρέθηκαν ειδοποιήσεις σε αυτή την κατηγορία" else "No alerts found in this category",
                            fontSize = 12.sp,
                            color = palette.textMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAlerts, key = { it.id }) { alert ->
                        AlertHistoryItemRow(
                            item = alert,
                            isGreek = isGreek,
                            onNavigateToTab = {
                                onDismiss()
                                onNavigateToTab(alert.navTargetTab)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertHistoryItemRow(
    item: AlertHistoryItem,
    isGreek: Boolean,
    onNavigateToTab: () -> Unit
) {
    val palette = LocalAppColors.current

    val categoryColor = when (item.category) {
        AlertCategory.FUNDING_SPIKE -> PhotonGold
        AlertCategory.WHALE_FLOW -> QuantumCyan
        AlertCategory.ETF_EXTREME -> QuantumCyan
        AlertCategory.CYCLE_SHIFT -> TachyonMint
        AlertCategory.LIQUIDATION_CASCADE -> SoftCrimson
        AlertCategory.ALL -> QuantumCyan
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background.copy(alpha = 0.5f))
            .border(
                1.dp,
                if (!item.isRead) categoryColor.copy(alpha = 0.5f) else palette.border.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Top Meta Row
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
                            .background(categoryColor, CircleShape)
                    )
                    Text(
                        text = "${item.category.emoji} ${if (isGreek) item.category.labelEl else item.category.labelEn}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.displayDate,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = palette.textMuted
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(QuantumCyan, CircleShape)
                        )
                    }
                }
            }

            // Title
            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary
            )

            // Message
            Text(
                text = item.message,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = palette.textSecondary
            )

            // Reason / Market Impact Card
            if (item.detailedReason.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.surfaceElevated.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "💡 ${item.detailedReason}",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = palette.textMuted
                    )
                }
            }

            // Bottom Trigger Price & Deep Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.btcPriceAtTrigger > 0) {
                    Text(
                        text = String.format(Locale.US, "BTC Trigger: $%,.0f", item.btcPriceAtTrigger),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = palette.textMuted
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToTab() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isGreek) "Προβολή" else "View",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = QuantumCyan,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}
