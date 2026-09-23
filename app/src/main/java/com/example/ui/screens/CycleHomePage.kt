package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BitcoinEtfFlowData
import com.example.data.model.DailyCycleLogEntry
import com.example.data.model.StablecoinLiquidityData
import com.example.ui.components.BitcoinEtfFlowsCard
import com.example.ui.components.DashboardTopAppBar
import com.example.ui.components.DefiLlamaStablecoinsCard
import com.example.ui.components.SearchBarField
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.TachyonMint
import com.example.util.CycleFractalData
import com.example.util.CycleReadingText
import com.example.util.LocalAppStrings
import java.util.Locale

@Composable
fun CycleHomePage(
    reading: CycleFractalData?,
    latestPriceUsd: Double,
    etfFlowData: BitcoinEtfFlowData,
    stablecoinLiquidityData: StablecoinLiquidityData,
    dailyLogs: List<DailyCycleLogEntry>,
    isProUnlocked: Boolean,
    isRefreshing: Boolean,
    unreadAlertsCount: Int,
    cycleDayAlertEnabled: Boolean,
    onCycleDayAlertChanged: (Boolean) -> Unit,
    onOpenChart: () -> Unit,
    onOpenCoins: () -> Unit,
    onSearchCoins: (String) -> Unit = {},
    onOpenProModal: () -> Unit,
    onRefresh: () -> Unit,
    onAlertHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val greek = strings.language.code == "el"
    val palette = LocalAppColors.current
    val context = LocalContext.current
    var pendingGrant by remember { mutableStateOf<(() -> Unit)?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val next = pendingGrant
        pendingGrant = null
        if (granted) next?.invoke()
    }
    val requestThen: (() -> Unit) -> Unit = { granted ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !com.example.util.NotificationHelper.canSendNotifications(context)
        ) {
            pendingGrant = granted
            runCatching { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
        } else {
            granted()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            DashboardTopAppBar(
                isProUnlocked = isProUnlocked,
                isRefreshing = isRefreshing,
                unreadAlertsCount = unreadAlertsCount,
                onAlertHistoryClick = onAlertHistoryClick,
                onRefreshClick = onRefresh,
                onProfileClick = onOpenProModal,
                onProBadgeClick = onOpenProModal,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = if (greek) "ΚΥΚΛΟΣ BITCOIN" else "BITCOIN CYCLE",
                    fontFamily = SpaceGroteskFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.historicalMatchLabel,
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                SearchBarField(
                    query = "",
                    onQueryChanged = { typed -> onSearchCoins(typed) },
                    onFocus = onOpenCoins
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (greek) "100 νομίσματα · άγγιξε για αναζήτηση" else "100 coins · tap to search",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = QuantumCyan,
                    modifier = Modifier.clickable { onOpenCoins() }
                )
            }
        }
        item {
            CycleReadingCard(
                reading = reading,
                isProUnlocked = isProUnlocked,
                greek = greek,
                onOpenChart = onOpenChart,
                onOpenProModal = onOpenProModal
            )
        }
        item {
            BitcoinEtfFlowsCard(etfFlowData = etfFlowData, isGreek = greek)
        }
        item {
            DefiLlamaStablecoinsCard(liquidityData = stablecoinLiquidityData, isGreek = greek)
        }
        item {
            DailyCycleLogCard(
                logs = dailyLogs,
                latestPriceUsd = latestPriceUsd,
                isProUnlocked = isProUnlocked,
                greek = greek,
                onOpenProModal = onOpenProModal
            )
        }
        item {
            CycleDayAlertRow(
                enabled = cycleDayAlertEnabled && isProUnlocked,
                greek = greek,
                onChecked = { on ->
                    if (on) requestThen { onCycleDayAlertChanged(true) } else onCycleDayAlertChanged(false)
                }
            )
        }
        item {
            Text(
                text = CycleReadingText.disclaimer(greek),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = palette.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CycleReadingCard(
    reading: CycleFractalData?,
    isProUnlocked: Boolean,
    greek: Boolean,
    onOpenChart: () -> Unit,
    onOpenProModal: () -> Unit
) {
    val palette = LocalAppColors.current
    val day = reading?.currentDay
    val axis = reading?.axisDays
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D0A1D))
            .border(1.dp, QuantumCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricBlock(
                label = if (greek) "Ημέρα από το halving" else "Days since halving",
                value = if (day == null || axis == null) "—" else "$day / $axis"
            )
            MetricBlock(
                label = if (greek) "Πολλαπλάσιο από το κλείσιμο" else "Multiple from the close",
                value = CycleReadingText.formatMultiple(reading?.multipleNow),
                alignEnd = true
            )
        }
        if (isProUnlocked && reading != null) {
            Text(
                text = "2016 ${CycleReadingText.formatMultiple(reading.multiple2016)}   ·   2020 ${CycleReadingText.formatMultiple(reading.multiple2020)}",
                fontFamily = JetBrainsMonoFont,
                fontSize = 13.sp,
                color = TachyonMint
            )
            Text(
                text = CycleReadingText.band(reading.multipleNow, reading.multiple2016, reading.multiple2020, greek),
                fontSize = 12.sp,
                color = palette.textSecondary
            )
            Text(
                text = CycleReadingText.alertLine(reading.currentDay, reading.multiple2016, reading.multiple2020, greek),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = palette.textPrimary
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenProModal() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = QuantumCyan, modifier = Modifier.size(16.dp))
                Text(
                    text = if (greek) {
                        "Οι γραμμές 2016 και 2020, και η σύγκριση της ημέρας, είναι στο Pro."
                    } else {
                        "The 2016 and 2020 lines, and this day's comparison, are in Pro."
                    },
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
            }
        }
        Row(
            modifier = Modifier.clickable { onOpenChart() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = QuantumCyan, modifier = Modifier.size(16.dp))
            Text(
                text = if (greek) "Άνοιγμα γραφήματος" else "Open the chart",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = QuantumCyan
            )
        }
    }
}

@Composable
private fun MetricBlock(label: String, value: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(text = label, fontSize = 11.sp, color = LocalAppColors.current.textMuted)
        Text(
            text = value,
            fontFamily = JetBrainsMonoFont,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = LocalAppColors.current.textPrimary
        )
    }
}

@Composable
private fun DailyCycleLogCard(
    logs: List<DailyCycleLogEntry>,
    latestPriceUsd: Double,
    isProUnlocked: Boolean,
    greek: Boolean,
    onOpenProModal: () -> Unit
) {
    val palette = LocalAppColors.current
    var openKey by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D0A1D))
            .border(1.dp, palette.border.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (greek) "ΗΜΕΡΟΛΟΓΙΟ ΚΥΚΛΟΥ" else "CYCLE LOG",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary
        )
        Text(
            text = if (greek) {
                "Κάθε μέρα γράφεται η ημέρα, το πολλαπλάσιο και πού έπεφτε απέναντι στο 2016 και στο 2020."
            } else {
                "Each day records the day-count, the multiple, and where it sat against 2016 and 2020."
            },
            fontSize = 11.sp,
            color = palette.textMuted
        )
        if (!isProUnlocked) {
            Text(
                text = if (greek) "Το αρχείο ανοίγει με Pro." else "The archive opens with Pro.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = QuantumCyan,
                modifier = Modifier.clickable { onOpenProModal() }
            )
            return@Column
        }
        if (logs.isEmpty()) {
            Text(
                text = if (greek) "Καμία ημέρα ακόμη. Η πρώτη γραμμή γράφεται όταν έρθει η τιμή." else "No day yet. The first line is written when a price arrives.",
                fontSize = 12.sp,
                color = palette.textSecondary
            )
            return@Column
        }
        logs.take(30).forEach { entry ->
            val opened = openKey == entry.dateKey
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { openKey = if (opened) null else entry.dateKey }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = "${entry.displayDate}  ·  ${if (greek) "Ημέρα" else "Day"} ${entry.cycleDay}  ·  ${CycleReadingText.formatMultiple(entry.multiple)}",
                    fontFamily = JetBrainsMonoFont,
                    fontSize = 12.sp,
                    color = palette.textPrimary
                )
                if (opened) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2016 ${CycleReadingText.formatMultiple(entry.multiple2016)} · 2020 ${CycleReadingText.formatMultiple(entry.multiple2020)}",
                        fontSize = 12.sp,
                        color = TachyonMint
                    )
                    Text(
                        text = CycleReadingText.band(entry.multiple, entry.multiple2016, entry.multiple2020, greek),
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )
                    val thenPrice = formatUsd(entry.priceUsd)
                    val nowPrice = if (latestPriceUsd > 0.0) formatUsd(latestPriceUsd) else "—"
                    val change = if (latestPriceUsd > 0.0 && entry.priceUsd > 0.0 && entry.dateKey != logs.first().dateKey) {
                        val pct = ((latestPriceUsd - entry.priceUsd) / entry.priceUsd) * 100.0
                        String.format(Locale.US, "%+.1f%%", pct)
                    } else {
                        "—"
                    }
                    Text(
                        text = if (greek) {
                            "Τιμή εκείνη την ημέρα $thenPrice. Τιμή τώρα $nowPrice ($change)."
                        } else {
                            "Price that day $thenPrice. Price now $nowPrice ($change)."
                        },
                        fontSize = 12.sp,
                        color = palette.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun CycleDayAlertRow(
    enabled: Boolean,
    greek: Boolean,
    onChecked: (Boolean) -> Unit
) {
    val palette = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = QuantumCyan, modifier = Modifier.size(18.dp))
            Column {
                Text(
                    text = if (greek) "Ειδοποίηση ημέρας" else "Day-count alert",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                Text(
                    text = if (greek) "Μία γραμμή. Την ανοίγεις εσύ. Πατάς και ανοίγει το γράφημα." else "One line. You turn it on. Tap opens the chart.",
                    fontSize = 11.sp,
                    color = palette.textMuted
                )
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedTrackColor = QuantumCyan)
        )
    }
}

private fun formatUsd(value: Double): String = String.format(Locale.US, "$%,.0f", value)
