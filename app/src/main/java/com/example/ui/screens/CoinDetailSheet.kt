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
    onOpenAiAssistant: ((String?) -> Unit)? = null
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
                                    text = "#${coin.rank}",
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
                        onOpenAiAssistant = onOpenAiAssistant
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
    modifier: Modifier = Modifier
) {
    val isGain = coin.change24h >= 0
    val absChange = kotlin.math.abs(coin.change24h)
    val trendDirection = if (isGain) "higher" else "lower"
    val isGreek = LocalAppStrings.current is com.example.util.GreekAppStrings
    val change24hFormatted = com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = true, decimals = 2)

    // Deterministic metrics derived from real data
    val spotIntensity = when {
        absChange > 5.0 -> "VERY HIGH"
        absChange > 2.0 -> "HIGH"
        absChange > 0.5 -> "MODERATE"
        else -> "LOW"
    }
    val spotBars = when {
        absChange > 5.0 -> 5
        absChange > 2.0 -> 4
        absChange > 0.5 -> 3
        else -> 1
    }
    val spotColor = if (isGain) TachyonMint else SoftCrimson

    val oiIntensity = "—"
    val oiBars = 0

    val fundingIntensity = "—"
    val fundingBars = 0
    val fundingColor = TextMuted

    val liqIntensity = "—"
    val liqBars = 0
    val liqColor = TextMuted

    val macroIntensity = "—"
    val macroBars = 0

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
                        text = "24h Delta: $change24hFormatted • Real Market Feed",
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

            // 5 Driver Meters (Derived from real market metrics)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DriverMeterRow(
                    label = if (isGreek) "Αγορές Spot" else "Spot Flow",
                    intensity = spotIntensity,
                    activeBars = spotBars,
                    barColor = spotColor
                )
                DriverMeterRow(
                    label = if (isGreek) "Open Interest" else "Open Interest",
                    intensity = oiIntensity,
                    activeBars = oiBars,
                    barColor = PhotonGold
                )
                DriverMeterRow(
                    label = if (isGreek) "Επιτόκιο Funding" else "Funding Rate",
                    intensity = fundingIntensity,
                    activeBars = fundingBars,
                    barColor = fundingColor
                )
                DriverMeterRow(
                    label = if (isGreek) "Ρευστοποιήσεις" else "Liquidations",
                    intensity = liqIntensity,
                    activeBars = liqBars,
                    barColor = liqColor
                )
                DriverMeterRow(
                    label = if (isGreek) "Μακροοικονομικά" else "Macro Context",
                    intensity = macroIntensity,
                    activeBars = macroBars,
                    barColor = TextMuted
                )
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
                    text = if (isGreek)
                        "Το ${coin.symbol} κινήθηκε $trendDirection ($change24hFormatted) στις τελευταίες 24 ώρες. Οι άλλες γραμμές μένουν παύλα όταν δεν υπάρχει live feed."
                    else
                        "${coin.symbol} moved $trendDirection ($change24hFormatted) in the last 24 hours. Other rows stay a dash when that feed is missing.",
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
    onOpenAiAssistant: ((String?) -> Unit)?
) {
    // Historical Cycle Trajectory & Price Chart.
    // Zoom lives inside HistoricalCycleChart so every coin shares one selector.
    HistoricalCycleChart(
        analog = coin.analog,
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
        }
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
                            text = com.example.util.AppNumberFormatter.formatPercent(coin.circulatingPercentage, includeSign = false, decimals = 1),
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
                            text = "Max: ${coin.maxSupply?.let { coin.formattedSupply(it) } ?: strings.infiniteDeflationary}",
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
                    sub = "${strings.rankPrefix} #${coin.rank}",
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
                    sub = "${coin.athDate} (${coin.calculatedAthDaysAgo}d)",
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
                    sub = coin.atlDate,
                    modifier = Modifier.weight(1f)
                )
                val dd = coin.drawdownPercent
                MiniStat(
                    title = strings.drawdownAthMetric,
                    value = com.example.util.AppNumberFormatter.formatPercent(dd, includeSign = true, decimals = 1),
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
                analog = coin.analog,
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
                    sub = "${coin.calculatedAthDaysAgo}d ${if (isGreek) "από το ATH" else "since ATH"}",
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

    // 1. Consensus & Architecture
    SectionContainer(
        title = strings.onChainNetworkHeader,
        icon = Icons.Default.AccountBalance
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
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

            Text(
                text = CoinLocalization.getTechnologyDetails(coin, selectedLanguage),
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
            Text(
                text = CoinLocalization.getTokenomicsDetails(coin, selectedLanguage),
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
                            text = com.example.util.AppNumberFormatter.formatPercent(coin.circulatingPercentage, includeSign = false, decimals = 1),
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
                            text = "Max: ${coin.maxSupply?.let { coin.formattedSupply(it) } ?: strings.infiniteDeflationary}",
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

    // 4. Genesis & Founder Heritage
    SectionContainer(
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
                Text(text = coin.founderOrCreator, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = strings.genesisDateLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Text(text = coin.genesisDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantumCyan)
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

    // 1. Protocol Milestones & Catalysts
    SectionContainer(
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
    SectionContainer(
        title = strings.whitepaperSectionHeader,
        icon = Icons.AutoMirrored.Filled.MenuBook
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    Text(text = coin.founderOrCreator, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = strings.genesisDateLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text(text = coin.genesisDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuantumCyan)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "📖 ${strings.whatCoinDoesLabel}:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PhotonGold
                )
                Text(
                    text = CoinLocalization.getWhitepaperSummary(coin, selectedLanguage),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = TextPrimary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "🎯 ${strings.useCasesLabel}:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TachyonMint
                )
                CoinLocalization.getUseCases(coin, selectedLanguage).forEach { useCase ->
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
    return when (coin.symbol.uppercase()) {
        "BTC" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "4ο Bitcoin Halving (Block 840,000)" else "4th Bitcoin Halving (Block 840,000)",
                timeline = if (isGreek) "Απρίλιος 2024" else "April 2024",
                status = "COMPLETED",
                description = if (isGreek) "Μείωση ανταμοιβής εξόρυξης από 6.25 σε 3.125 BTC ανά μπλοκ." else "Mining block subsidy cut from 6.25 to 3.125 BTC per block."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Spot ETF & Θεσμική Υιοθέτηση" else "Spot ETF & Institutional Adoption",
                timeline = if (isGreek) "Σε εξέλιξη" else "Ongoing",
                status = "ACTIVE",
                description = if (isGreek) "Σταθερές καθαρές εισροές και συσσώρευση από θεσμικά ταμεία και εταιρικά ταμεία." else "Sustained net inflows and custody accumulation across asset managers."
            ),
            ProtocolMilestone(
                title = if (isGreek) "BitVM & Layer 2 Programmability" else "BitVM & Layer 2 Programmability",
                timeline = if (isGreek) "2025 - 2026" else "2025 - 2026",
                status = "UPCOMING",
                description = if (isGreek) "Υλοποίηση smart contracts και zero-knowledge rollups πάνω στο ασφαλές L1 του Bitcoin." else "Zero-knowledge rollups and expressive contracts secured by Bitcoin L1."
            ),
            ProtocolMilestone(
                title = if (isGreek) "5ο Bitcoin Halving (Block 1,050,000)" else "5th Bitcoin Halving (Block 1,050,000)",
                timeline = if (isGreek) "Εκτίμηση Απρίλιος 2028" else "Est. April 2028",
                status = "UPCOMING",
                description = if (isGreek) "Περαιτέρω μείωση του ρυθμού έκδοσης στα 1.5625 BTC ανά μπλοκ." else "Block subsidy drops to 1.5625 BTC per block."
            )
        )
        "ETH" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Dencun Upgrade (EIP-4844)" else "Dencun Upgrade (EIP-4844)",
                timeline = if (isGreek) "Μάρτιος 2024" else "March 2024",
                status = "COMPLETED",
                description = if (isGreek) "Εισαγωγή Proto-Danksharding blobs που μείωσε τα fees των Layer 2 κατά 90%." else "Proto-Danksharding blobs reducing Layer 2 transaction fees by over 90%."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Pectra Hard Fork" else "Pectra Hard Fork",
                timeline = if (isGreek) "Εκτίμηση 2025" else "Est. 2025",
                status = "UPCOMING",
                description = if (isGreek) "Account Abstraction (EIP-7702) και αύξηση μέγιστου υπολοίπου validator σε 2048 ETH." else "Account Abstraction (EIP-7702) and max validator balance raised to 2,048 ETH."
            ),
            ProtocolMilestone(
                title = if (isGreek) "EIP-1559 Μηχανισμός Καύσης" else "EIP-1559 Fee Burn Mechanism",
                timeline = if (isGreek) "Συνεχές" else "Continuous",
                status = "ACTIVE",
                description = if (isGreek) "Αυτόματη καύση του base fee κατά περιόδους υψηλής χρήσης του δικτύου." else "Automated base gas fee burning during high network congestion periods."
            )
        )
        "SOL" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Firedancer Validator Client" else "Firedancer Validator Client",
                timeline = if (isGreek) "Εκτίμηση 2025" else "Est. 2025",
                status = "UPCOMING",
                description = if (isGreek) "Ανεξάρτητος C++ client από την Jump Crypto με στόχο 1.000.000+ TPS." else "Independent C++ validator client by Jump Crypto targeting 1,000,000+ TPS."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Token Extensions & Institutional Standard" else "Token Extensions & Institutional Standard",
                timeline = if (isGreek) "Ολοκληρώθηκε" else "Completed",
                status = "COMPLETED",
                description = if (isGreek) "Εγγενή χαρακτηριστικά συμμόρφωσης, εμπιστευτικές μεταφορές και προσαρμοσμένα hooks." else "Native enterprise compliance features, confidential transfers and metadata hooks."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Solana Actions & Blinks" else "Solana Actions & Blinks",
                timeline = if (isGreek) "Ενεργό" else "Active",
                status = "ACTIVE",
                description = if (isGreek) "Δυνατότητα εκτέλεσης συναλλαγών απευθείας μέσα από social media και websites." else "Execute on-chain transactions directly within social feeds and websites."
            )
        )
        "XRP" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Ripple RLUSD Stablecoin" else "Ripple RLUSD Stablecoin",
                timeline = if (isGreek) "2024 - 2025" else "2024 - 2025",
                status = "UPCOMING",
                description = if (isGreek) "Ρυθμιζόμενο enterprise stablecoin για διατραπεζικές διασυνοριακές πληρωμές." else "Regulated enterprise stablecoin for institutional cross-border settlement."
            ),
            ProtocolMilestone(
                title = if (isGreek) "XLS-30D Automated Market Maker (AMM)" else "XLS-30D Automated Market Maker (AMM)",
                timeline = if (isGreek) "Ολοκληρώθηκε" else "Completed",
                status = "COMPLETED",
                description = if (isGreek) "Ενσωμάτωση native DEX AMM δεξαμενών ρευστότητας στο XRPL." else "Native on-chain AMM pools and continuous auction mechanism on XRPL."
            )
        )
        "BNB" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "BEP-336 Blob Transactions" else "BEP-336 Blob Transactions",
                timeline = if (isGreek) "Ολοκληρώθηκε" else "Completed",
                status = "COMPLETED",
                description = if (isGreek) "Βελτιστοποίηση Layer 2 rollups στο BNB Chain με δραστική μείωση gas fees." else "Blob transaction support delivering massive gas reductions for opBNB rollups."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Τριμηνιαίο Auto-Burn" else "Quarterly Auto-Burn",
                timeline = if (isGreek) "Σε εξέλιξη" else "Ongoing",
                status = "ACTIVE",
                description = if (isGreek) "Αυτόματη φόρμουλα καύσης BNB έως ότου η συνολική προσφορά φτάσει τα 100M tokens." else "Formulaic token burning program until circulating supply contracts to 100M BNB."
            )
        )
        "ADA" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Chang Hard Fork (Voltaire Era)" else "Chang Hard Fork (Voltaire Era)",
                timeline = if (isGreek) "Ολοκληρώθηκε" else "Completed",
                status = "COMPLETED",
                description = if (isGreek) "Μετάβαση σε πλήρη on-chain δημοκρατική διακυβέρνηση και διαχείριση ταμείου κοινότητας." else "Full on-chain democratic governance, delegate representatives and treasury voting."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Hydra Layer 2 Scaling" else "Hydra Layer 2 Scaling",
                timeline = if (isGreek) "Σε εξέλιξη" else "Ongoing",
                status = "ACTIVE",
                description = if (isGreek) "Κανάλια κατάστασης για άμεσες συναλλαγές χαμηλού κόστους σε κλίμακα χιλιάδων TPS." else "State channels enabling isomorphic high-throughput micro-transactions."
            )
        )
        "SUI" -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Mysticeti Consensus Engine" else "Mysticeti Consensus Engine",
                timeline = if (isGreek) "Ολοκληρώθηκε" else "Completed",
                status = "COMPLETED",
                description = if (isGreek) "Υπερταχεία οριστικότητα συναλλαγών κάτω από 400ms σε παράλληλη εκτέλεση." else "Sub-400ms transaction consensus latency under Byzantine fault tolerance."
            ),
            ProtocolMilestone(
                title = if (isGreek) "SuiPlay0X1 Web3 Gaming Device" else "SuiPlay0X1 Web3 Gaming Device",
                timeline = if (isGreek) "2025" else "2025",
                status = "UPCOMING",
                description = if (isGreek) "Κονσόλα χειρός Web3 με ενσωματωμένη ασφάλεια πορτοφολιού και native gaming τίτλους." else "First handheld Web3 gaming console with hardware key management."
            )
        )
        else -> listOf(
            ProtocolMilestone(
                title = if (isGreek) "Αναβάθμιση Κόμβων & Διακυβέρνηση" else "Core Node Upgrades & Governance",
                timeline = if (isGreek) "Συνεχές" else "Continuous",
                status = "ACTIVE",
                description = if (isGreek) "Συνεχής βελτίωση απόδοσης δικτύου, ασφάλειας επικυρωτών και παραμέτρων πρωτοκόλλου." else "Ongoing validator throughput enhancements, client security, and protocol parameters."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Προγράμματα Κινήτρων Οικοσυστήματος" else "Ecosystem & Staking Expansion",
                timeline = if (isGreek) "Σε εξέλιξη" else "Ongoing",
                status = "ACTIVE",
                description = if (isGreek) "Ενίσχυση ρευστότητας, staking rewards και επέκταση κοινότητας προγραμματιστών." else "Liquidity incentives, staking yield stabilization, and developer ecosystem grants."
            ),
            ProtocolMilestone(
                title = if (isGreek) "Διασυνδεσιμότητα & Cross-Chain Bridges" else "Cross-Chain Interoperability & Bridges",
                timeline = if (isGreek) "Εκτίμηση 2025" else "Est. 2025",
                status = "UPCOMING",
                description = if (isGreek) "Επέκταση γεφυρών ρευστότητας και πρωτοκόλλων επικοινωνίας μεταξύ πολλαπλών αλυσίδων." else "Deployment of trust-minimized interoperability bridges across major L1/L2 networks."
            )
        )
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

