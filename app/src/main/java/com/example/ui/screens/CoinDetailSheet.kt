package com.example.ui.screens

import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.ui.components.CoinAvatar
import com.example.ui.components.CoinLatestNewsSection
import com.example.ui.components.DataFreshnessBadge
import com.example.ui.components.DataFreshnessStatus
import com.example.ui.components.formatPriceAge
import com.example.ui.components.freshnessFor
import com.example.ui.components.HistoricalCycleChart
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.PhotonGold
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CoinLocalization
import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailSheet(
    coin: CryptoCoin?,
    currency: Currency,
    selectedLanguage: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.ENGLISH,
    isProUnlocked: Boolean,
    onDismiss: () -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onOpenProModal: () -> Unit,
    onOpenAiAssistant: ((String?) -> Unit)? = null,
    movementReport: com.example.data.model.MarketIntelligenceReport? = null,
    etfFlowData: com.example.data.model.BitcoinEtfFlowData? = null
) {
    if (coin == null) return

    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isLocked = coin.isPro && !isProUnlocked
    var selectedDetailTab by remember { mutableStateOf(0) } // 0 = Overview, 1 = Analytics, 2 = On-Chain, 3 = Events

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CosmicVoidBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CosmicBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
                .testTag("coin_detail_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinAvatar(symbol = coin.symbol, modifier = Modifier.size(46.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = coin.name,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CosmicVoidSurfaceElevated)
                                    .border(1.dp, CosmicBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = coin.displayRank,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = QuantumCyan
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(com.example.ui.theme.QuantumCyan.copy(alpha = 0.15f))
                                    .border(0.6.dp, com.example.ui.theme.QuantumCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SPOT",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.4.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = com.example.ui.theme.QuantumCyan
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = coin.displayPrice(currency),
                                fontSize = if (coin.quoteState == com.example.data.model.QuoteState.LIVE) 22.sp else 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (coin.isLivePrice) TextPrimary else com.example.ui.theme.QuantumCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val isGain = coin.change24h >= 0
                            val changeFormatted = if (coin.isLivePrice) {
                                com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = true, decimals = 2)
                            } else {
                                "..."
                            }
                            Text(
                                text = changeFormatted,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (coin.isLivePrice) (if (isGain) TachyonMint else SoftCrimson) else TextMuted
                            )
                        }

                        // Real data status & freshness badge
                        DataFreshnessBadge(
                            status = freshnessFor(coin.priceUpdatedAtMs),
                            timeAgo = formatPriceAge(coin.priceUpdatedAtMs),
                            source = if (coin.priceUpdatedAtMs > 0L) "Market price" else "Connecting...",
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onFavoriteToggle(coin.id) }) {
                        Icon(
                            imageVector = if (coin.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (coin.isFavorite) PhotonGold else TextMuted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // 1b. SUITE TABS: [Overview] [Analytics] [On-Chain] [Events]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabLabels = listOf(
                    strings.tabOverview,
                    strings.tabAnalytics,
                    strings.tabOnChain,
                    strings.tabEvents
                )
                tabLabels.forEachIndexed { index, label ->
                    val isSelected = selectedDetailTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) QuantumCyan.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) QuantumCyan.copy(alpha = 0.4f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                com.example.util.AppSoundManager.playTechClick()
                                selectedDetailTab = index
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) QuantumCyan else TextSecondary
                        )
                    }
                }
            }

            when (selectedDetailTab) {
                0 -> {
                    OverviewTabContent(
                        coin = coin,
                        strings = strings,
                        selectedLanguage = selectedLanguage,
                        currency = currency,
                        isProUnlocked = isProUnlocked,
                        onOpenAiAssistant = onOpenAiAssistant,
                        movementReport = movementReport,
                        etfFlowData = etfFlowData
                    )
                }
                1 -> {
                    AnalyticsTabContent(
                        coin = coin,
                        strings = strings,
                        selectedLanguage = selectedLanguage,
                        currency = currency,
                        isLocked = isLocked,
                        onOpenProModal = onOpenProModal
                    )
                }
                2 -> {
                    OnChainTabContent(
                        coin = coin,
                        strings = strings,
                        selectedLanguage = selectedLanguage
                    )
                }
                3 -> {
                    EventsTabContent(
                        coin = coin,
                        strings = strings,
                        selectedLanguage = selectedLanguage
                    )
                }
            }

            // Latest News Feed Section
            CoinLatestNewsSection(coin = coin)

            // Regulatory & Analytical Risk Disclaimer (Shown on all tabs)
            RegulatoryDisclaimerCard(selectedLanguage = selectedLanguage)

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SectionContainer(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CosmicVoidSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = QuantumCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = QuantumCyan
                )
            }
            content()
        }
    }
}

@Composable
private fun PredictionBadgeCard(
    timeframe: String,
    target: String,
    probabilityTag: String = "P1",
    confidence: String = "85%",
    modifier: Modifier = Modifier
) {
    val isNegative = target.trim().startsWith("-") && !target.contains("—")
    val targetColor = if (isNegative) SoftCrimson else TachyonMint
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicVoidSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(QuantumCyan.copy(alpha = 0.15f))
                        .border(0.6.dp, QuantumCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = probabilityTag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = QuantumCyan
                    )
                }
                Text(text = timeframe, fontSize = 9.5.sp, color = TextMuted)
            }
            Text(
                text = target,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = targetColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
                Text(
                    text = confidence,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun MiniStat(
    title: String,
    value: String,
    sub: String,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicVoidSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(text = sub, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun WhyCoinIsMovingCard(
    coin: CryptoCoin,
    onExplain: () -> Unit,
    modifier: Modifier = Modifier,
    movementReport: com.example.data.model.MarketIntelligenceReport? = null,
    etfFlowData: com.example.data.model.BitcoinEtfFlowData? = null
) {
    val isGain = coin.change24h >= 0
    val trendDirection = if (isGain) "higher" else "lower"
    val isGreek = LocalAppStrings.current is com.example.util.GreekAppStrings
    val change24hFormatted = if (coin.isLivePrice) {
        com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = true, decimals = 2)
    } else {
        "—"
    }
    val rows = remember(coin.symbol, coin.change24h, coin.isLivePrice, movementReport, etfFlowData) {
        com.example.data.model.CoinMovementDriverMapper.rows(
            symbol = coin.symbol,
            change24h = coin.change24h,
            hasLiveChange24h = coin.isLivePrice,
            report = movementReport,
            etf = etfFlowData
        )
    }
    val matchedReport = movementReport?.takeIf {
        com.example.data.model.CoinMovementDriverMapper.reportMatchesCoin(it.symbol, coin.symbol)
    }
    val insight = when {
        isGreek && matchedReport != null &&
            matchedReport.interpretationGr.isNotBlank() &&
            !matchedReport.interpretationGr.contains("Αναμονή") ->
            "${matchedReport.interpretationGr} Binance USDT-M. Οι γραμμές μένουν παύλα όταν λείπει το feed."
        !isGreek && matchedReport != null &&
            matchedReport.interpretationEn.isNotBlank() &&
            !matchedReport.interpretationEn.contains("Waiting") ->
            "${matchedReport.interpretationEn} Binance USDT-M. Rows stay a dash when that feed is missing."
        isGreek ->
            "Το ${coin.symbol} κινήθηκε $trendDirection ($change24hFormatted) στις τελευταίες 24 ώρες. Οι άλλες γραμμές μένουν παύλα όταν δεν υπάρχει live feed."
        else ->
            "${coin.symbol} moved $trendDirection ($change24hFormatted) in the last 24 hours. Other rows stay a dash when that feed is missing."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicVoidSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: Title + Data Badge + Explain > button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isGreek) "Γιατί κινείται το ${coin.symbol};" else "Why is ${coin.symbol} moving?",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isGreek)
                            "24ω: $change24hFormatted · Binance USDT-M όταν υπάρχει"
                        else
                            "24h Delta: $change24hFormatted · Binance USDT-M when listed",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(QuantumCyan.copy(alpha = 0.12f))
                        .clickable {
                            com.example.util.AppSoundManager.playTechClick()
                            onExplain()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGreek) "Εξήγηση >" else "Explain >",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { row ->
                    val barColor = when {
                        !row.isLive -> TextMuted
                        row.key == "spot" -> if (isGain) TachyonMint else SoftCrimson
                        row.key == "oi" -> PhotonGold
                        row.key == "funding" -> if ((matchedReport?.fundingRate ?: 0.0) < 0.0) TachyonMint else SoftCrimson
                        row.key == "liq" -> SoftCrimson
                        row.key == "etf" -> if ((etfFlowData?.oneDayNetFlowMillionUsd ?: 0.0) >= 0.0) TachyonMint else SoftCrimson
                        else -> QuantumCyan
                    }
                    DriverMeterRow(
                        label = if (isGreek) row.labelEl else row.labelEn,
                        intensity = row.intensity,
                        activeBars = row.bars,
                        barColor = barColor
                    )
                }
            }

            // Bottom Insight Callout Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CosmicVoidSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = insight,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DriverMeterRow(
    label: String,
    intensity: String,
    activeBars: Int,
    barColor: Color,
    totalBars: Int = 5
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = intensity,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )

            // Segmented bars
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 0 until totalBars) {
                    val isActive = i < activeBars
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isActive) barColor else CosmicVoidBg)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    coin: CryptoCoin,
    strings: com.example.util.AppStrings,
    selectedLanguage: com.example.data.model.AppLanguage,
    currency: Currency,
    isProUnlocked: Boolean,
    onOpenAiAssistant: ((String?) -> Unit)?,
    movementReport: com.example.data.model.MarketIntelligenceReport? = null,
    etfFlowData: com.example.data.model.BitcoinEtfFlowData? = null
) {
    // Historical Cycle Trajectory & Price Chart.
    // Zoom lives inside HistoricalCycleChart so every coin shares one selector.
    HistoricalCycleChart(
        coinSymbol = coin.symbol,
        currentPrice = coin.priceUsd,
        coinName = coin.name,
        coinId = coin.id,
        isProUnlocked = isProUnlocked
    )

    // "Why is Coin moving?" Intelligence Card
    WhyCoinIsMovingCard(
        coin = coin,
        onExplain = {
            onOpenAiAssistant?.invoke("Why is ${coin.name} (${coin.symbol}) moving?")
        },
        movementReport = movementReport,
        etfFlowData = etfFlowData
    )

    Text(
        text = com.example.util.CycleReadingText.disclaimer(
            selectedLanguage == com.example.data.model.AppLanguage.GREEK
        ),
        fontSize = 11.sp,
        lineHeight = 15.sp,
        color = TextMuted
    )

    SectionContainer(
        title = strings.movementAnalysisHeader,
        icon = Icons.Default.Timeline
    ) {
        val live = if (coin.priceUsd > 0.0) coin.formattedPrice(currency) else "—"
        val change = if (coin.priceUpdatedAtMs > 0L) {
            com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = true, decimals = 2)
        } else "—"
        Text(
            text = if (selectedLanguage == com.example.data.model.AppLanguage.GREEK) {
                "Ζωντανή τιμή $live · 24ω $change. Χωρίς έτοιμο κείμενο όταν λείπει το δικό του ιστορικό."
            } else {
                "Live price $live · 24h $change. No template essay. A cycle line is drawn only when that coin has its own closes."
            },
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = TextPrimary
        )
    }

    // Market Stats & Token Supply
    SectionContainer(
        title = strings.marketStatsHeader,
        icon = Icons.Default.PieChart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Supply Gauge Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.circulatingSupplyLabel,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = if (coin.circulatingSupply > 0.0) com.example.util.AppNumberFormatter.formatPercent(coin.circulatingPercentage, includeSign = false, decimals = 1) else "—",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CosmicVoidBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(coin.circulatingPercentage / 100f)
                                .height(8.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(MauveAurora, QuantumCyan)
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${strings.circulatingShort}: ${coin.formattedSupply(coin.circulatingSupply)}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Max: ${when {
                                coin.circulatingSupply <= 0.0 -> "—"
                                else -> coin.maxSupply?.let { coin.formattedSupply(it) } ?: strings.infiniteDeflationary
                            }}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // 4x Grid Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStat(
                    title = strings.marketCapMetric,
                    value = coin.formattedMarketCap(currency),
                    sub = "${strings.rankPrefix} ${coin.displayRank}",
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    title = strings.volume24hMetric,
                    value = coin.formattedVolume(currency),
                    sub = strings.tradingVolumeSub,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStat(
                    title = strings.fdvMetric,
                    value = coin.formattedFdv(currency),
                    sub = strings.fdvSub,
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    title = strings.athMetric,
                    value = coin.formattedAth(currency),
                    sub = coin.athDateWithDays,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStat(
                    title = strings.atlMetric,
                    value = coin.formattedAtl(currency),
                    sub = coin.atlDate.ifBlank { "—" },
                    modifier = Modifier.weight(1f)
                )
                val dd = coin.drawdownPercent
                MiniStat(
                    title = strings.drawdownAthMetric,
                    value = if (coin.athUsd > 0.0 && coin.priceUsd > 0.0)
                        com.example.util.AppNumberFormatter.formatPercent(dd, includeSign = true, decimals = 1)
                    else "—",
                    sub = strings.fromPeakSub,
                    valueColor = if (dd < 0) SoftCrimson else TachyonMint,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AnalyticsTabContent(
    coin: CryptoCoin,
    strings: com.example.util.AppStrings,
    selectedLanguage: com.example.data.model.AppLanguage,
    currency: Currency,
    isLocked: Boolean,
    onOpenProModal: () -> Unit
) {
    // 1. NEXT PREDICTED MOVE & HISTORICAL ANALOGS
    SectionContainer(
        title = strings.nextMoveHeader,
        icon = Icons.Default.Psychology
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (selectedLanguage == com.example.data.model.AppLanguage.GREEK) {
                    "Δεν υπάρχει πρόβλεψη επόμενης κίνησης. Μόνο πραγματοποιημένες live κινήσεις."
                } else {
                    "There is no next-move forecast. Only realized live moves."
                },
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DataFreshnessBadge(
                    status = freshnessFor(coin.priceUpdatedAtMs),
                    timeAgo = formatPriceAge(coin.priceUpdatedAtMs),
                    source = if (coin.priceUpdatedAtMs > 0L) "Market price" else "Connecting..."
                )
                Text(
                    text = if (selectedLanguage == com.example.data.model.AppLanguage.GREEK)
                        "Ζωντανές κινήσεις · όχι πρόβλεψη"
                    else
                        "Live realized moves · not a forecast",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = QuantumCyan.copy(alpha = 0.85f)
                )
            }

            val liveMoves = CoinLocalization.liveRealizedMoves(coin)
            val isGreekMoves = selectedLanguage == com.example.data.model.AppLanguage.GREEK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PredictionBadgeCard(
                    timeframe = if (isGreekMoves) "24ω" else "24h",
                    target = liveMoves.first,
                    probabilityTag = "LIVE",
                    confidence = if (coin.priceUpdatedAtMs > 0L) "live" else "—",
                    modifier = Modifier.weight(1f)
                )
                PredictionBadgeCard(
                    timeframe = if (isGreekMoves) "Spark" else "Spark",
                    target = liveMoves.second,
                    probabilityTag = "LIVE",
                    confidence = if (coin.sparkline.size >= 2) "live" else "—",
                    modifier = Modifier.weight(1f)
                )
                PredictionBadgeCard(
                    timeframe = "ATH",
                    target = liveMoves.third,
                    probabilityTag = "LIVE",
                    confidence = if (coin.athUsd > 0.0 && coin.priceUsd > 0.0) "live" else "—",
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = if (isGreekMoves)
                    "Οι κάρτες δείχνουν πραγματοποιημένες live κινήσεις. Δεν υπάρχει εφευρεμένο win rate ή σενάριο."
                else
                    "Cards show realized live moves. No invented win-rate or scenario distribution.",
                fontSize = 9.5.sp,
                lineHeight = 13.sp,
                color = TextMuted
            )

            HistoricalCycleChart(
                coinSymbol = coin.symbol,
                currentPrice = coin.priceUsd,
                coinName = coin.name,
                coinId = coin.id
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ATH:", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = if (coin.athUsd > 0.0) coin.formattedAth(currency, selectedLanguage) else "—",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TachyonMint
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ATL:", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = if (coin.atlUsd > 0.0) coin.formattedAtl(currency, selectedLanguage) else "—",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftCrimson
                    )
                }
            }
        }
    }

    // 2. CYCLE MOMENTUM & RELATIVE HEALTH
    val momentumMetrics = getCycleMomentumMetrics(coin)
    val isGreek = selectedLanguage == com.example.data.model.AppLanguage.GREEK
    val rsiValue = momentumMetrics.first
    val rsiNumber = rsiValue.toDoubleOrNull()
    SectionContainer(
        title = strings.cycleMomentumTitle,
        icon = Icons.Default.Timeline
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStat(
                    title = if (isGreek) "14D RSI" else "14D RSI",
                    value = rsiValue,
                    sub = when {
                        rsiNumber == null -> if (isGreek) "Αναμονή sparkline" else "Awaiting sparkline"
                        rsiNumber >= 55 -> if (isGreek) "Ανοδικό μομέντουμ" else "Bullish momentum"
                        rsiNumber <= 45 -> if (isGreek) "Υπερπωλημένο / πτωτικό" else "Oversold / weak"
                        else -> if (isGreek) "Ουδέτερη συσσώρευση" else "Neutral consolidation"
                    },
                    valueColor = when {
                        rsiNumber == null -> TextMuted
                        rsiNumber >= 55 -> TachyonMint
                        rsiNumber <= 45 -> SoftCrimson
                        else -> PhotonGold
                    },
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    title = if (isGreek) "Απόσταση από ATH" else "Distance from ATH",
                    value = momentumMetrics.second,
                    sub = if (isGreek) "Από live τιμή / ATH" else "From live price / ATH",
                    valueColor = QuantumCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dd = coin.drawdownPercent
                MiniStat(
                    title = strings.drawdownAthMetric,
                    value = if (coin.athUsd > 0.0 && coin.priceUsd > 0.0)
                        com.example.util.AppNumberFormatter.formatPercent(dd, includeSign = true, decimals = 1)
                    else "—",
                    sub = if (coin.athUsd > 0.0 && coin.athDate.isNotBlank())
                        "${coin.calculatedAthDaysAgo}d ${if (isGreek) "από το ATH" else "since ATH"}"
                    else "—",
                    valueColor = if (dd < 0) SoftCrimson else TachyonMint,
                    modifier = Modifier.weight(1f)
                )
                val change24Formatted = if (coin.priceUpdatedAtMs > 0L)
                    com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = true, decimals = 2)
                else "—"
                MiniStat(
                    title = if (isGreek) "24ωρη Μεταβολή" else "24h Delta",
                    value = change24Formatted,
                    sub = if (isGreek) "Ζωντανή Ροή Αγοράς" else "Real Market Feed",
                    valueColor = if (coin.priceUpdatedAtMs <= 0L) TextMuted else if (coin.change24h >= 0) TachyonMint else SoftCrimson,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OnChainTabContent(
    coin: CryptoCoin,
    strings: com.example.util.AppStrings,
    selectedLanguage: com.example.data.model.AppLanguage
) {
    val isGreek = selectedLanguage == com.example.data.model.AppLanguage.GREEK
    val onChainMetrics = getEstimatedOnChainMetrics(coin)

    // 1. Consensus & Architecture — only when this coin has a written profile
    val techText = CoinLocalization.getTechnologyDetails(coin, selectedLanguage)
    if (coin.consensusMechanism.isNotBlank() || techText.isNotBlank()) SectionContainer(
        title = strings.onChainNetworkHeader,
        icon = Icons.Default.AccountBalance
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (coin.consensusMechanism.isNotBlank()) Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isGreek) "Μηχανισμός Συναίνεσης:" else "Consensus Protocol:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicVoidSurfaceElevated)
                        .border(1.dp, QuantumCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = coin.consensusMechanism,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan
                    )
                }
            }

            if (techText.isNotBlank()) Text(
                text = techText,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = TextPrimary
            )
        }
    }

    // 2. Tokenomics & Issuance Dynamics
    SectionContainer(
        title = strings.tokenomicsLabel,
        icon = Icons.Default.AutoAwesome
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val tokenomicsText = CoinLocalization.getTokenomicsDetails(coin, selectedLanguage)
            if (tokenomicsText.isNotBlank()) Text(
                text = tokenomicsText,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = TextPrimary
            )

            // Circulating Supply Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = strings.circulatingSupplyTitle, fontSize = 11.5.sp, color = TextSecondary)
                        Text(
                            text = if (coin.circulatingSupply > 0.0) com.example.util.AppNumberFormatter.formatPercent(coin.circulatingPercentage, includeSign = false, decimals = 1) else "—",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuantumCyan
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CosmicVoidBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(coin.circulatingPercentage / 100f)
                                .height(6.dp)
                                .background(Brush.horizontalGradient(listOf(MauveAurora, QuantumCyan)))
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${strings.circulatingShort}: ${coin.formattedSupply(coin.circulatingSupply)}",
                            fontSize = 10.5.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Max: ${when {
                                coin.circulatingSupply <= 0.0 -> "—"
                                else -> coin.maxSupply?.let { coin.formattedSupply(it) } ?: strings.infiniteDeflationary
                            }}",
                            fontSize = 10.5.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }

    // 3. Network Health & Whale Distribution
    SectionContainer(
        title = strings.whaleConcentrationLabel,
        icon = Icons.Default.Security
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStat(
                    title = strings.activeAddressesEstimate,
                    value = onChainMetrics.first,
                    sub = if (isGreek) "Χωρίς live feed" else "No live feed",
                    valueColor = QuantumCyan,
                    modifier = Modifier.weight(1f)
                )
                MiniStat(
                    title = strings.exchangeNetFlowLabel,
                    value = onChainMetrics.third,
                    sub = if (isGreek) "Χωρίς live feed" else "No live feed",
                    valueColor = TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = strings.whaleConcentrationLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = onChainMetrics.second,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isGreek)
                            "Δεν υπάρχει δημόσιο live feed για συγκέντρωση πορτοφολιών. Εμφανίζεται — αντί για εφευρεμένο ποσοστό."
                        else
                            "No public live feed for wallet concentration. Showing — instead of an invented share.",
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }

    // 4. Genesis & Founder Heritage — only when written down for this coin
    if (coin.founderOrCreator.isNotBlank() || coin.genesisDate.isNotBlank()) SectionContainer(
        title = if (isGreek) "Ιστορικό & Δημιουργία" else "Genesis & Heritage",
        icon = Icons.Default.CheckCircle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CosmicVoidSurface)
                .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = strings.founderLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(text = coin.founderOrCreator.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = strings.genesisDateLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(text = coin.genesisDate.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantumCyan)
            }
        }
    }
}

@Composable
private fun EventsTabContent(
    coin: CryptoCoin,
    strings: com.example.util.AppStrings,
    selectedLanguage: com.example.data.model.AppLanguage
) {
    val isGreek = selectedLanguage == com.example.data.model.AppLanguage.GREEK
    val milestones = getMilestonesForCoin(coin, isGreek)

    // 1. Protocol Milestones & Catalysts — dated, already-happened events only
    if (milestones.isNotEmpty()) SectionContainer(
        title = strings.eventsMilestonesHeader,
        icon = Icons.Default.CheckCircle
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            milestones.forEach { milestone ->
                val (badgeBg, badgeText, badgeLabel) = when (milestone.status) {
                    "COMPLETED" -> Triple(TachyonMint.copy(alpha = 0.15f), TachyonMint, strings.completedTag)
                    "ACTIVE" -> Triple(QuantumCyan.copy(alpha = 0.15f), QuantumCyan, strings.activeTag)
                    else -> Triple(PhotonGold.copy(alpha = 0.15f), PhotonGold, strings.upcomingTag)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CosmicVoidSurface)
                        .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = milestone.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = badgeText
                                )
                            }
                        }

                        Text(
                            text = "🗓️ ${milestone.timeline}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = QuantumCyan
                        )

                        Text(
                            text = milestone.description,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    // 2. Whitepaper & Real-World Use Cases
    val profileSummary = CoinLocalization.getWhitepaperSummary(coin, selectedLanguage)
    val profileUses = CoinLocalization.getUseCases(coin, selectedLanguage)
    val hasProfile = coin.founderOrCreator.isNotBlank() || coin.genesisDate.isNotBlank() ||
        profileSummary.isNotBlank() || profileUses.isNotEmpty()
    if (!hasProfile && milestones.isEmpty()) {
        Text(
            text = if (isGreek) {
                "Δεν υπάρχει ακόμη γραπτό προφίλ για το ${coin.name}. Τιμή, ATH/ATL και προσφορά έρχονται ζωντανά."
            } else {
                "No written profile for ${coin.name} yet. Price, ATH/ATL and supply are live."
            },
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = TextMuted
        )
    }
    if (hasProfile) SectionContainer(
        title = strings.whitepaperSectionHeader,
        icon = Icons.AutoMirrored.Filled.MenuBook
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (coin.founderOrCreator.isNotBlank() || coin.genesisDate.isNotBlank()) Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CosmicVoidSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = strings.founderLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = coin.founderOrCreator.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = strings.genesisDateLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = coin.genesisDate.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantumCyan)
                }
            }

            val summary = profileSummary
            if (summary.isNotBlank()) Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "📖 ${strings.whatCoinDoesLabel}:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhotonGold
                )
                Text(
                    text = summary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = TextPrimary
                )
            }

            val useCases = profileUses
            if (useCases.isNotEmpty()) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "🎯 ${strings.useCasesLabel}:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TachyonMint
                )
                useCases.forEach { useCase ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", color = TachyonMint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = useCase,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegulatoryDisclaimerCard(
    selectedLanguage: com.example.data.model.AppLanguage
) {
    val isGreek = selectedLanguage == com.example.data.model.AppLanguage.GREEK
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicVoidSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Risk Disclaimer",
                    tint = PhotonGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isGreek) "Θεσμική Ανάλυση & Δήλωση Κινδύνου" else "Institutional Analytics & Risk Disclosure",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhotonGold
                )
            }
            Text(
                text = if (isGreek)
                    "Το CryptoCycles παρέχει δεδομένα αγοράς, αντιστοίχιση ιστορικών κύκλων, στατιστικές κατανομές σεναρίων και εκπαιδευτικό περιεχόμενο. Δεν αποτελεί εξατομικευμένη επενδυτική συμβουλή, δεν εγγυάται μελλοντική απόδοση και δεν εκτελεί συναλλαγές. Διεξάγετε πάντα δική σας ανεξάρτητη έρευνα."
                else
                    "CryptoCycles provides market data, historical cycle matching, statistical scenario distributions, and educational information. It does not provide personalized investment or trading advice, does not guarantee future performance, and does not execute or facilitate trades. Always conduct independent research.",
                fontSize = 10.5.sp,
                lineHeight = 15.sp,
                color = TextSecondary
            )
        }
    }
}

private data class ProtocolMilestone(
    val title: String,
    val timeline: String,
    val status: String,
    val description: String
)

private fun getMilestonesForCoin(coin: CryptoCoin, isGreek: Boolean): List<ProtocolMilestone> {
    // Dated events that already happened (plus Bitcoin's protocol-scheduled halving).
    // No generic roadmap for coins without a verified list.
    return when (coin.symbol.uppercase()) {
        "BTC" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Ενεργοποίηση Taproot" else "Taproot activation",
                timeline = if (isGreek) "Νοέμβριος 2021" else "November 2021",
                status = "COMPLETED",
                description = if (isGreek) "Αναβάθμιση Schnorr υπογραφών και πιο ιδιωτικών/φθηνών scripts." else "Schnorr signatures and cheaper, more private scripts."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Spot Bitcoin ETF στις ΗΠΑ" else "US spot Bitcoin ETFs",
                timeline = if (isGreek) "Ιανουάριος 2024" else "January 2024",
                status = "COMPLETED",
                description = if (isGreek) "Η SEC ενέκρινε τα πρώτα spot Bitcoin ETF." else "The SEC approved the first US spot Bitcoin ETFs."
            ),
            ProtocolMilestone(
                title = if (isGreek) "4ο Bitcoin Halving (Block 840.000)" else "4th Bitcoin Halving (Block 840,000)",
                timeline = if (isGreek) "Απρίλιος 2024" else "April 2024",
                status = "COMPLETED",
                description = if (isGreek) "Η ανταμοιβή μπλοκ μειώθηκε από 6,25 σε 3,125 BTC." else "Block subsidy cut from 6.25 to 3.125 BTC."
            ),
            ProtocolMilestone(
                title = if (isGreek) "5ο Bitcoin Halving (Block 1.050.000)" else "5th Bitcoin Halving (Block 1,050,000)",
                timeline = if (isGreek) "Εκτίμηση 2028" else "Est. 2028",
                status = "UPCOMING",
                description = if (isGreek) "Ορίζεται από το πρωτόκολλο: η ανταμοιβή πέφτει στα 1,5625 BTC. Η ημερομηνία εξαρτάται από τον ρυθμό των μπλοκ." else "Set by the protocol: subsidy drops to 1.5625 BTC. The date depends on block times."
            )
        )
        "ETH" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "The Merge" else "The Merge",
                timeline = if (isGreek) "Σεπτέμβριος 2022" else "September 2022",
                status = "COMPLETED",
                description = if (isGreek) "Μετάβαση από Proof of Work σε Proof of Stake." else "Switch from Proof of Work to Proof of Stake."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Shapella" else "Shapella",
                timeline = if (isGreek) "Απρίλιος 2023" else "April 2023",
                status = "COMPLETED",
                description = if (isGreek) "Ενεργοποίηση αναλήψεων staking." else "Staking withdrawals enabled."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Dencun (EIP-4844)" else "Dencun (EIP-4844)",
                timeline = if (isGreek) "Μάρτιος 2024" else "March 2024",
                status = "COMPLETED",
                description = if (isGreek) "Blob συναλλαγές που μείωσαν το κόστος των Layer 2." else "Blob transactions that cut Layer 2 data costs."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Pectra" else "Pectra",
                timeline = if (isGreek) "Μάιος 2025" else "May 2025",
                status = "COMPLETED",
                description = if (isGreek) "EIP-7702 για λογαριασμούς και μέγιστο υπόλοιπο validator 2.048 ETH." else "EIP-7702 account features and a 2,048 ETH max validator balance."
            )
        )
        "SOL" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Mainnet Beta" else "Mainnet Beta",
                timeline = if (isGreek) "Μάρτιος 2020" else "March 2020",
                status = "COMPLETED",
                description = if (isGreek) "Έναρξη του κύριου δικτύου Solana." else "Solana mainnet beta goes live."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Solana Actions & Blinks" else "Solana Actions & Blinks",
                timeline = if (isGreek) "Ιούνιος 2024" else "June 2024",
                status = "COMPLETED",
                description = if (isGreek) "Συναλλαγές on-chain μέσα από συνδέσμους σε websites και social media." else "On-chain transactions from links on websites and social feeds."
            )
        )
        "XRP" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Έναρξη XRP Ledger" else "XRP Ledger launch",
                timeline = if (isGreek) "Ιούνιος 2012" else "June 2012",
                status = "COMPLETED",
                description = if (isGreek) "Το XRP Ledger ξεκινά με 100 δισ. XRP." else "The XRP Ledger launches with 100 billion XRP."
            ),
            ProtocolMilestone(
                title = if (isGreek) "XLS-30 AMM" else "XLS-30 AMM",
                timeline = if (isGreek) "Μάρτιος 2024" else "March 2024",
                status = "COMPLETED",
                description = if (isGreek) "Εγγενείς δεξαμενές ρευστότητας AMM στο XRPL." else "Native AMM liquidity pools on the XRPL."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Stablecoin RLUSD" else "RLUSD stablecoin",
                timeline = if (isGreek) "Δεκέμβριος 2024" else "December 2024",
                status = "COMPLETED",
                description = if (isGreek) "Η Ripple κυκλοφορεί το ρυθμιζόμενο stablecoin RLUSD." else "Ripple launches its regulated RLUSD stablecoin."
            )
        )
        "BNB" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "BEP-95 καύση σε πραγματικό χρόνο" else "BEP-95 real-time burn",
                timeline = if (isGreek) "Νοέμβριος 2021" else "November 2021",
                status = "COMPLETED",
                description = if (isGreek) "Μέρος των gas fees καίγεται σε κάθε μπλοκ." else "A share of gas fees is burned in every block."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Τριμηνιαίο Auto-Burn" else "Quarterly Auto-Burn",
                timeline = if (isGreek) "Από Δεκέμβριο 2021" else "Since December 2021",
                status = "ACTIVE",
                description = if (isGreek) "Καύση με τύπο έως ότου η προσφορά φτάσει τα 100 εκατ. BNB." else "Formula-based burns until supply reaches 100 million BNB."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Haber (BEP-336 blobs)" else "Haber (BEP-336 blobs)",
                timeline = if (isGreek) "Ιούνιος 2024" else "June 2024",
                status = "COMPLETED",
                description = if (isGreek) "Blob συναλλαγές για φθηνότερα Layer 2 στο BNB Chain." else "Blob transactions for cheaper Layer 2s on BNB Chain."
            )
        )
        "ADA" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Alonzo (smart contracts)" else "Alonzo (smart contracts)",
                timeline = if (isGreek) "Σεπτέμβριος 2021" else "September 2021",
                status = "COMPLETED",
                description = if (isGreek) "Plutus smart contracts στο mainnet." else "Plutus smart contracts on mainnet."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Chang hard fork" else "Chang hard fork",
                timeline = if (isGreek) "Σεπτέμβριος 2024" else "September 2024",
                status = "COMPLETED",
                description = if (isGreek) "Αρχή της on-chain διακυβέρνησης (Voltaire)." else "Start of on-chain governance (Voltaire)."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Plomin hard fork" else "Plomin hard fork",
                timeline = if (isGreek) "Ιανουάριος 2025" else "January 2025",
                status = "COMPLETED",
                description = if (isGreek) "Πλήρης on-chain διακυβέρνηση και ψηφοφορίες ταμείου." else "Full on-chain governance and treasury voting."
            )
        )
        "SUI" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Mainnet" else "Mainnet",
                timeline = if (isGreek) "Μάιος 2023" else "May 2023",
                status = "COMPLETED",
                description = if (isGreek) "Έναρξη του κύριου δικτύου Sui." else "Sui mainnet goes live."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Mysticeti" else "Mysticeti",
                timeline = if (isGreek) "Αύγουστος 2024" else "August 2024",
                status = "COMPLETED",
                description = if (isGreek) "Νέος μηχανισμός συναίνεσης με χαμηλότερη καθυστέρηση." else "New consensus engine with lower latency."
            )
        )
        else -> emptyList()
    }
}

private fun getEstimatedOnChainMetrics(coin: CryptoCoin): Triple<String, String, String> {
    return Triple("—", "—", "—")
}

private fun getCycleMomentumMetrics(coin: CryptoCoin): Pair<String, String> {
    val rsi = if (coin.priceUpdatedAtMs > 0L && coin.sparkline.size > 14) {
        com.example.engine.forecasting.QuantForecastEngine.calculateRsi(coin.sparkline)
    } else {
        null
    }
    val rsiText = rsi?.let { String.format(Locale.US, "%.1f", it) } ?: "—"
    val phase = when {
        coin.priceUsd <= 0.0 || coin.athUsd <= 0.0 -> "—"
        coin.drawdownPercent > -25.0 -> "Near ATH"
        coin.drawdownPercent > -50.0 -> "Mid drawdown"
        coin.drawdownPercent > -75.0 -> "Deep drawdown"
        else -> "Far from ATH"
    }
    return Pair(rsiText, phase)
}

