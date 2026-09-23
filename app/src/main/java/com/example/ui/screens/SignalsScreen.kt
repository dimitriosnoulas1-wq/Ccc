package com.example.ui.screens

import java.util.Locale
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.data.model.MacroCycleSignal
import com.example.data.model.WhaleAlert
import com.example.data.model.WhaleLeveragePosition
import com.example.data.model.WhaleLeverageSummary
import com.example.ui.components.ChartTimeframe
import com.example.ui.components.DataFreshnessBadge
import com.example.ui.components.DataFreshnessStatus
import com.example.ui.components.formatPriceAge
import com.example.ui.components.freshnessFor
import com.example.ui.components.GrokZigZagChart
import com.example.ui.components.LiveOrderFlowSection
import com.example.ui.components.SignalsProGatekeeper
import com.example.ui.components.TrustBar
import com.example.ui.components.WhaleLeverageTrackerSection
import com.example.ui.components.WhaleRadarSection
import com.example.ui.components.QuantForecastCard
import com.example.viewmodel.CryptoViewModel
import androidx.compose.runtime.collectAsState
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.DrawdownRed
import com.example.ui.theme.GainGreen
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.QuantumCornerReticleOverlay
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.SyneFont
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.stitchHorizonPanel
import com.example.ui.components.QuantumReticleBadge
import com.example.util.CoinLocalization
import com.example.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalsScreen(
    signal: MacroCycleSignal,
    coins: List<CryptoCoin>,
    selectedCoin: CryptoCoin?,
    onSelectCoin: (CryptoCoin) -> Unit,
    currency: Currency,
    isProUnlocked: Boolean,
    onOpenProModal: () -> Unit,
    whaleAlerts: List<WhaleAlert> = emptyList(),
    whaleLeveragePositions: List<WhaleLeveragePosition> = emptyList(),
    whaleLeverageSummary: WhaleLeverageSummary = WhaleLeverageSummary(
        totalLongVolumeUsd = 0.0,
        totalShortVolumeUsd = 0.0,
        longRatioPercent = 0.0,
        shortRatioPercent = 0.0,
        activeMegaPositionsCount = 0,
        largestPositionUsd = 0.0,
        dominantSide = com.example.data.model.LeveragePositionSide.LONG
    ),
    recentTrades: List<com.example.data.model.FuturesTrade> = emptyList(),
    isRefreshing: Boolean = false,
    isConnected: Boolean = false,
    priceSource: String = "Binance",
    lastUpdatedTimestamp: Long = 0L,
    onOpenAiAssistant: (String?) -> Unit = {},
    onRefresh: () -> Unit = {},
    viewModel: CryptoViewModel? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val isGreek = strings.language.code == "el"

    val btcForecast by (viewModel?.btcForecast?.collectAsState() ?: remember {
        mutableStateOf(
            com.example.engine.forecasting.QuantForecastEngine.computeForecast(
                symbol = "BTC",
                currentPrice = 0.0,
                historicalPrices = emptyList(),
                highs = emptyList(),
                lows = emptyList()
            )
        )
    })
    val isPro by (viewModel?.isProUnlocked?.collectAsState() ?: remember { mutableStateOf(isProUnlocked) })
    val isLogChart by (viewModel?.logCharts?.collectAsState() ?: remember { mutableStateOf(true) })
    var selectedTimeframe by remember { mutableStateOf(ChartTimeframe.CYCLE) }
    var showAllCoinsPicker by remember { mutableStateOf(false) }
    var selectedSignalsTab by remember { mutableStateOf(0) } // 0 = Top Signals, 1 = My Signals, 2 = Performance

    val refreshTransition = rememberInfiniteTransition(label = "signals_refresh_spin")
    val refreshRotation by refreshTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    // Live real-time ticking halving countdown
    var halvingCountdown by remember { mutableStateOf(com.example.util.HalvingCycleUtils.getLiveHalvingCountdown()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            halvingCountdown = com.example.util.HalvingCycleUtils.getLiveHalvingCountdown()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Active coin selection
    val activeCoin = selectedCoin ?: coins.firstOrNull { it.symbol == "BTC" } ?: coins.firstOrNull()

    if (activeCoin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Loading Signals...", color = TextMuted)
        }
        return
    }

    val isBullish = activeCoin.change24h >= 0
    val daysAfterAth = activeCoin.calculatedAthDaysAgo
    val cycleProfile = remember(activeCoin.symbol) { com.example.util.CoinCycleHistoryManager.getProfile(activeCoin) }
    val typicalDays = cycleProfile.typicalCorrectionDays
    val isBottomReached = daysAfterAth >= typicalDays
    val daysToBottom = if (!isBottomReached) (typicalDays - daysAfterAth).coerceAtLeast(0) else 0
    val cycleProgressRatio = if (typicalDays > 0) (daysAfterAth.toFloat() / typicalDays.toFloat()).coerceIn(0.05f, 1f) else 0.5f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("signals_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Top Bar: Logo + CryptoCycles + Refresh + Pro button
        item {
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CosmicSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonChecked,
                            contentDescription = "Logo",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.navSignals.uppercase(),
                        fontFamily = SpaceGroteskFont,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuantumReticleBadge(
                        tag = "0x04",
                        label = "SIGNALS",
                        color = QuantumCyan
                    )

                    // Refresh Button
                    IconButton(
                        onClick = {
                            com.example.util.AppSoundManager.playTechClick()
                            onRefresh()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF140D2E))
                            .border(1.dp, QuantumCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = strings.refreshTooltip,
                            tint = if (isRefreshing) QuantumCyan else TextSecondary,
                            modifier = Modifier
                                .size(16.dp)
                                .then(if (isRefreshing) Modifier.rotate(refreshRotation) else Modifier)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isProUnlocked) TachyonMint else QuantumCyan)
                            .clickable { onOpenProModal() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isProUnlocked) strings.proActive else strings.proBadge,
                            fontSize = 11.sp,
                            fontFamily = JetBrainsMonoFont,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D0A1D),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Trust Bar
        item {
            TrustBar(
                isRefreshing = isRefreshing,
                isConnected = isConnected,
                sourceName = priceSource,
                lastUpdatedMs = lastUpdatedTimestamp,
                onRefresh = onRefresh
            )
        }

        // Quant Forecast Card
        item {
            QuantForecastCard(
                model = btcForecast,
                isProUser = isPro,
                onUpgradeClick = { if (viewModel != null) viewModel.openProModal() else onOpenProModal() },
                modifier = Modifier.padding(16.dp)
            )
        }

        // Pro Gatekeeper Top Banner for free users
        if (!isProUnlocked) {
            item {
                SignalsProGatekeeper(
                    onOpenProModal = onOpenProModal
                )
            }
        } else {
            // PRO UNLOCKED -> COMPACT LIVE ON-CHAIN WHALE RADAR
            item {
                WhaleRadarSection(
                    alerts = whaleAlerts
                )
            }

            // Live 24/7 Order Flow Engine (Real-time Buy / Sell Dominance & Ticker Executions)
            item {
                LiveOrderFlowSection(
                    activeCoin = activeCoin,
                    currency = currency,
                    recentTrades = recentTrades
                )
            }
        }

        // Live venue open interest + liquidation tape
        item {
            WhaleLeverageTrackerSection(
                positions = whaleLeveragePositions,
                summary = whaleLeverageSummary,
                currency = currency,
                isProUnlocked = isProUnlocked,
                onOpenProModal = onOpenProModal
            )
        }

        // Sub-tabs: Top Signals, My Signals, Performance
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CosmicSurface)
                    .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    if (isGreek) "Κορυφαία Σήματα" else "Top Signals",
                    if (isGreek) "Τα Σήματά μου" else "My Signals",
                    if (isGreek) "Απόδοση" else "Performance"
                ).forEachIndexed { idx, title ->
                    val isSelected = selectedSignalsTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) NeonCyan.copy(alpha = 0.4f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                com.example.util.AppSoundManager.playTechClick()
                                selectedSignalsTab = idx
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NeonCyan else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Horizontal Quick Coin Selector Carousel (Accessible to all users, with locks on Pro coins)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val selectorTitle = when (selectedSignalsTab) {
                    1 -> if (isGreek) "Αποθηκευμένα νομίσματα" else "Saved coins"
                    2 -> if (isGreek) "Μεγαλύτερες κινήσεις 24ω" else "Largest 24h moves"
                    else -> strings.signalsCoinSelectorTitle
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectorTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = NeonCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicSurfaceElevated)
                            .clickable { showAllCoinsPicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.signalsAllCoins,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                val quickSymbols = listOf(
                    "BTC", "ETH", "SOL", "XRP", "XLM", "BNB", "DOGE", "ADA", "SUI",
                    "AVAX", "LINK", "NEAR", "INJ", "KAS", "RENDER", "TAO", "PEPE", "HYPE"
                )
                val quickCoins = when (selectedSignalsTab) {
                    1 -> coins.filter { it.isFavorite }
                    2 -> coins.sortedByDescending { kotlin.math.abs(it.change24h) }.take(12)
                    else -> quickSymbols.mapNotNull { sym -> coins.firstOrNull { it.symbol == sym } }
                }
                if (quickCoins.isEmpty()) {
                    Text(
                        text = if (isGreek) "Δεν υπάρχουν αποθηκευμένα νομίσματα ακόμα." else "No saved coins yet. Star a coin to keep it here.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickCoins.forEach { coinItem ->
                        val isSelected = activeCoin.id == coinItem.id
                        val isCoinLocked = coinItem.isPro && !isProUnlocked
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) NeonCyan else CosmicSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else CosmicBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    if (isCoinLocked) {
                                        onOpenProModal()
                                    } else {
                                        onSelectCoin(coinItem)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCoinLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = if (isSelected) Color(0xFF05050F) else NeonAmber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = coinItem.symbol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF05050F) else TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = coinItem.formattedChange(includePlus = true),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) {
                                        if (coinItem.change24h >= 0) Color(0xFF065F46) else Color(0xFF991B1B)
                                    } else {
                                        if (coinItem.change24h >= 0) GainGreen else DrawdownRed
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Coin Content: Locked Card if coin is PRO and user is FREE, else show full analytics
        val isActiveCoinLocked = !activeCoin.symbol.equals("BTC", ignoreCase = true) && !isProUnlocked
        if (isActiveCoinLocked) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(CosmicSurface)
                        .border(1.2.dp, NeonAmber, RoundedCornerShape(22.dp))
                        .clickable { onOpenProModal() }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonAmber.copy(alpha = 0.2f))
                                .border(1.dp, NeonAmber, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Pro Signal Detected (High Confidence) · Unlock Pro to Reveal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(NeonAmber.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Pro Locked",
                                tint = NeonAmber,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = "${activeCoin.name} (${activeCoin.symbol}) Signal Pro Locked",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isGreek)
                                "Τα σήματα, οι προβλέψεις και τα τεχνικά δεδομένα για το ${activeCoin.name} (${activeCoin.symbol}) είναι διαθέσιμα αποκλειστικά σε Pro μέλη."
                            else
                                "Signals, AI price projections, and quantitative indicators for ${activeCoin.name} (${activeCoin.symbol}) are exclusive to Pro members.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = onOpenProModal,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isGreek) "Αναβάθμιση σε Pro για ${activeCoin.symbol}" else "Unlock Pro for ${activeCoin.symbol}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF05050F)
                            )
                        }
                    }
                }
            }
        } else {

        // Live Definitive Market Signal Verdict Card (100% verified status)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            if (isBullish) listOf(Color(0xFF0B2920), Color(0xFF061814))
                            else listOf(Color(0xFF2E0F16), Color(0xFF19060B))
                        )
                    )
                    .border(
                        1.dp,
                        if (isBullish) GainGreen.copy(alpha = 0.6f) else DrawdownRed.copy(alpha = 0.6f),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DataFreshnessBadge(
                                status = freshnessFor(activeCoin.priceUpdatedAtMs),
                                timeAgo = formatPriceAge(activeCoin.priceUpdatedAtMs),
                                source = priceSource
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Rank #${activeCoin.rank}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }

                    // Main Verdict Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = if (isBullish) strings.signalBullish else strings.signalBearish,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                color = if (isBullish) GainGreen else DrawdownRed,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${activeCoin.name} (${activeCoin.symbol}) · ${activeCoin.displayPrice(currency)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background((if (isBullish) GainGreen else DrawdownRed).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBullish) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = "Trend",
                                tint = if (isBullish) GainGreen else DrawdownRed,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    val live24h = if (activeCoin.priceUpdatedAtMs > 0L) activeCoin.change24h else null
                    val live24hLabel = live24h?.let {
                        com.example.util.AppNumberFormatter.formatPercent(it, includeSign = true, decimals = 2)
                    } ?: "—"

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isGreek) "Ζωντανή κίνηση 24ω" else "Live 24h move",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = live24hLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (live24h == null) TextSecondary else if (isBullish) GainGreen else NeonAmber
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(CosmicVoidBg)
                        ) {
                            val barFill = live24h?.let { (kotlin.math.abs(it) / 15.0).toFloat().coerceIn(0.04f, 1f) } ?: 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(barFill)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isBullish) GainGreen else NeonAmber)
                            )
                        }

                        Text(
                            text = if (isGreek)
                                "Η μπάρα δείχνει μόνο την πραγματική μεταβολή 24ω από το live feed. Όχι πρόβλεψη."
                            else
                                "Bar shows the live 24h print from the market feed. Not a forecast.",
                            fontSize = 9.5.sp,
                            lineHeight = 13.sp,
                            color = TextMuted
                        )
                    }

                    Text(
                        text = CoinLocalization.getCycleStatisticsNarrative(activeCoin, strings.language),
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = TextSecondary
                    )

                    val liveDriverTags = buildList {
                        if (activeCoin.priceUpdatedAtMs > 0L) {
                            add(
                                (if (activeCoin.change24h >= 0) "+ 24h" else "- 24h") to
                                    if (activeCoin.change24h >= 0) GainGreen else NeonAmber
                            )
                        }
                        if (activeCoin.volume24h > 0.0) {
                            add("Vol" to NeonCyan)
                        }
                        if (activeCoin.athUsd > 0.0 && activeCoin.priceUsd > 0.0) {
                            add("ATH" to TextMuted)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (liveDriverTags.isEmpty()) {
                            Text(text = "—", fontSize = 9.5.sp, color = TextMuted)
                        } else {
                            liveDriverTags.forEach { (tag, col) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(col.copy(alpha = 0.12f))
                                        .border(0.8.dp, col.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = col
                                    )
                                }
                            }
                        }
                    }

                    // Invalidation Level & Expected Scenario
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicVoidSurface)
                            .border(0.8.dp, CosmicBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isGreek) "Χαμηλό παραθύρου:" else "Window low:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonAmber
                                )
                                Text(
                                    text = if (isGreek) "Ελάχιστο από το live sparkline. Όχι ATR / πρόβλεψη." else "Minimum of the live sparkline. Not an ATR forecast.",
                                    fontSize = 9.sp,
                                    color = TextMuted
                                )
                            }
                            val sparkLow = activeCoin.sparkline.minOrNull()
                                ?.takeIf { activeCoin.priceUpdatedAtMs > 0L && activeCoin.sparkline.size >= 2 && it > 0.0 }
                            val invPrice = sparkLow?.let {
                                com.example.util.AppNumberFormatter.formatPrice(it, currency)
                            } ?: "—"
                            Text(
                                text = invPrice,
                                fontSize = 11.5.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = if (isGreek)
                                "Δεν εμφανίζεται εφευρεμένο επίπεδο ακύρωσης. Μόνο ζωντανά prints."
                            else
                                "No invented invalidation level. Live prints only.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )

                        Text(
                            text = if (isGreek)
                                "Όχι επενδυτική συμβουλή."
                            else
                                "Not a personalized trade or investment recommendation.",
                            fontSize = 9.sp,
                            lineHeight = 12.sp,
                            color = TextMuted
                        )
                    }


                }
            }
        }

        // Hero Cycle Progress Card (Sleek, Compact, Futuristic Gauge)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CosmicSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeW = 4.5.dp.toPx()
                            val radius = (size.width - strokeW) / 2f
                            drawCircle(
                                color = CosmicBorder,
                                radius = radius,
                                style = Stroke(width = strokeW)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(NeonCyan, Color(0xFF38BDF8), Color(0xFF818CF8), NeonCyan)
                                ),
                                startAngle = -90f,
                                sweepAngle = (cycleProgressRatio * 360f).coerceIn(30f, 360f),
                                useCenter = false,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${(cycleProgressRatio * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.weekOfFallTitle} · ${activeCoin.symbol}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F243A))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Week ${daysAfterAth / 7}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isGreek)
                                "εβδομάδα ${daysAfterAth / 7} από ATH · $daysAfterAth ημέρες"
                            else
                                "week ${daysAfterAth / 7} since ATH · $daysAfterAth days",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Section Title: Cycle analysis
        item {
            Text(
                text = "${strings.cycleAnalysisHeader} · ${activeCoin.name}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }

        // Big Main Card: FROM THE HIGH TO A POSSIBLE LOW (Dynamically calculated for active coin)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(CosmicSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = strings.fromHighToLowTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = NeonCyan
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "$daysAfterAth",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1.5).sp,
                                color = TextPrimary
                            )
                            Text(
                                text = strings.daysAfterAthLabel,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBottomReached) "0" else "$daysToBottom",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1.5).sp,
                                color = if (isBottomReached) NeonEmerald else NeonAmber
                            )
                            Text(
                                text = if (isBottomReached) "days to bottom (Completed)" else strings.daysToBottomLabel,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Progress line with High / typical ~Xd / Low markers
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF16233B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(cycleProgressRatio)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isBottomReached) NeonEmerald else Color(0xFFFB7185))
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = strings.highMarker, fontSize = 11.sp, color = TextMuted)
                            Text(text = "~${typicalDays}d bottom", fontSize = 11.sp, color = TextMuted)
                            Text(text = if (isBottomReached) "Expansion" else strings.lowMarker, fontSize = 11.sp, color = if (isBottomReached) NeonEmerald else TextMuted)
                        }
                    }

                    // Narrative description
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = com.example.util.CoinLocalization.getFromHighToLowNarrative(
                                coin = activeCoin,
                                daysAfterAth = daysAfterAth,
                                daysToBottom = daysToBottom,
                                typicalDays = typicalDays,
                                isBottomReached = isBottomReached,
                                currency = currency,
                                language = strings.language
                            ),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = TextPrimary
                        )
                        val ddFormatted = com.example.util.AppNumberFormatter.formatPercent(activeCoin.drawdownPercent, includeSign = true, decimals = 1)
                        Text(
                            text = "${activeCoin.athDate} · ${activeCoin.formattedAth(currency)} · $ddFormatted from peak",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Text(
                            text = strings.fromHighToLowDisclaimer,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Section: THIS CYCLE VS THE LAST ONES
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(CosmicSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = strings.thisCycleVsLastTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = NeonCyan
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "${strings.daysAfterHighTitle} (${activeCoin.symbol})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = com.example.util.CoinLocalization.getDaysAfterHighSubtitle(activeCoin, daysAfterAth, typicalDays, strings.language),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        CycleBarRow(label = strings.nowLabel, days = daysAfterAth, progress = cycleProgressRatio, isCurrent = true)
                        cycleProfile.pastCorrectionRows.forEach { row ->
                            CycleBarRow(label = row.label, days = row.days, progress = row.progress)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = strings.daysOfRiseTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = strings.daysOfRiseSub,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        cycleProfile.pastRiseRows.forEach { row ->
                            HistoricRiseRow(label = row.label, days = row.days)
                        }
                    }
                }
            }
        }

        // Section: BITCOIN HALVING Countdown
        item {
            val isBtc = activeCoin.symbol == "BTC"
            val cardTitle = if (isBtc) strings.halvingTitle else "${activeCoin.symbol} Macro Cycle Milestones"
            val cardBadge = if (isBtc) "~2028 (Block 1,050,000)" else "ATH: ${activeCoin.athDate.ifBlank { "N/A" }}"
            val box1Val = if (isBtc) halvingCountdown.totalDaysString else activeCoin.calculatedAthDaysAgo.toString()
            val box1Lbl = if (isBtc) strings.halvingDaysLabel else "Days Post-Token-ATH"
            val box2Val = if (isBtc) halvingCountdown.hoursString else daysToBottom.toString()
            val box2Lbl = if (isBtc) strings.halvingHoursLabel else "Days to Cycle Low"
            val subTickerText = if (isBtc) "Live Ticker: ${halvingCountdown.minutesString}m ${halvingCountdown.secondsString}s" else "Macro Anchor: ATH $${activeCoin.athUsd}"
            val rightTagText = if (isBtc) "Post-Halving Day ${com.example.util.HalvingCycleUtils.getDaysSince4thHalving()}" else "Rel. BTC Halving D${com.example.util.HalvingCycleUtils.getDaysSince4thHalving()}"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF101B30), Color(0xFF09101E))
                        )
                    )
                    .border(1.dp, CosmicBorder, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cardTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = NeonCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CosmicSurface)
                                .border(1.dp, CosmicBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = cardBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Box 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CosmicSurface)
                                .border(1.dp, CosmicBorder, RoundedCornerShape(14.dp))
                                .padding(vertical = 12.dp, horizontal = 14.dp)
                        ) {
                            Column {
                                Text(
                                    text = box1Val,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = box1Lbl,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Box 2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(NeonCyan.copy(alpha = 0.08f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                .padding(vertical = 12.dp, horizontal = 14.dp)
                        ) {
                            Column {
                                Text(
                                    text = box2Val,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = box2Lbl,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NeonCyan.copy(alpha = 0.85f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Sub-ticker
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
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GainGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = subTickerText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rightTagText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeonCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Section: WHAT CAME NEXT (Empirical Statistics for active coin)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = strings.whatCameNextTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = NeonCyan
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val liveMoves = CoinLocalization.liveRealizedMoves(activeCoin)
                    WhatCameNextCard(timeframe = strings.timeframe1wk, gain = liveMoves.first, winRate = if (isGreek) "live 24ω" else "live 24h", modifier = Modifier.weight(1f))
                    WhatCameNextCard(timeframe = strings.timeframe2wk, gain = liveMoves.second, winRate = if (isGreek) "live 7ημ" else "live 7d", modifier = Modifier.weight(1f))
                    WhatCameNextCard(timeframe = strings.timeframe4wk, gain = liveMoves.third, winRate = if (isGreek) "από ATH" else "from ATH", modifier = Modifier.weight(1f))
                }
            }
        }

        // Section: WHY WE SAY THIS (Algorithmic Logic Breakdown for active coin)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(CosmicSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = strings.whyWeSayThisTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = NeonCyan
                    )

                    WhyWeSayThisPoint(
                        number = 1,
                        text = CoinLocalization.getCycleAlignmentNarrative(activeCoin, strings.language)
                    )
                    WhyWeSayThisPoint(
                        number = 2,
                        text = CoinLocalization.getCycleStatisticsNarrative(activeCoin, strings.language)
                    )
                    WhyWeSayThisPoint(
                        number = 3,
                        text = if (isGreek) {
                            "Η τρέχουσα φάση είναι ${signal.cycleClockPhase} με live δείκτη κύκλου ${signal.riskScore}/100."
                        } else {
                            "Current phase is ${signal.cycleClockPhase} with live cycle score ${signal.riskScore}/100."
                        }
                    )
                    WhyWeSayThisPoint(
                        number = 4,
                        text = CoinLocalization.getNextPredictedMoveNarrative(activeCoin, strings.language)
                    )
                }
            }
        }

        // Range Slider Card: All-time low to All-time high
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(CosmicSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = strings.allTimeLowMetric, fontSize = 11.sp, color = TextMuted)
                        Text(text = strings.allTimeHighMetric, fontSize = 11.sp, color = TextMuted)
                    }

                    // Progress track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CosmicVoidBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .height(6.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(GainGreen, NeonCyan, Color(0xFFFB7185))
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(text = activeCoin.formattedAtl(currency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = activeCoin.atlDate, fontSize = 10.sp, color = TextMuted)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = activeCoin.displayPrice(currency), fontSize = if (activeCoin.quoteState == com.example.data.model.QuoteState.LIVE) 13.sp else 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            val ddPctStr = com.example.util.AppNumberFormatter.formatPercent(activeCoin.drawdownPercent, includeSign = true, decimals = 1)
                            Text(text = ddPctStr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (activeCoin.drawdownPercent < 0) Color(0xFFFB7185) else GainGreen)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = activeCoin.formattedAth(currency), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = activeCoin.athDate, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // 4 Key Metric Cards (2x2 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KeyMetricCard(
                        title = strings.allTimeHighMetric,
                        primaryValue = activeCoin.formattedAth(currency),
                        subValue = activeCoin.athDate,
                        modifier = Modifier.weight(1f)
                    )
                    KeyMetricCard(
                        title = strings.allTimeLowMetric,
                        primaryValue = activeCoin.formattedAtl(currency),
                        subValue = activeCoin.atlDate,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KeyMetricCard(
                        title = strings.startMetric,
                        primaryValue = activeCoin.genesisDate,
                        subValue = "Founder: ${activeCoin.founderOrCreator}",
                        modifier = Modifier.weight(1f)
                    )
                    val ddAthStr = com.example.util.AppNumberFormatter.formatPercent(activeCoin.drawdownPercent, includeSign = true, decimals = 1)
                    KeyMetricCard(
                        title = strings.daysSinceHighMetric,
                        primaryValue = "$daysAfterAth",
                        primaryValueColor = Color(0xFFFB7185),
                        subValue = "$ddAthStr from ATH",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Price · highs and lows (Grok ZigZag Interactive Multi-Cycle Chart)
        item {
            GrokZigZagChart(
                coin = activeCoin,
                currency = currency,
                selectedTimeframe = selectedTimeframe,
                onTimeframeSelected = { selectedTimeframe = it },
                isLogScale = isLogChart,
                onToggleLogScale = { viewModel?.setLogCharts(!isLogChart) },
                isProUnlocked = isProUnlocked,
                onOpenProModal = onOpenProModal,
                showProjection = false
            )
        }
        }

        // Mandatory Financial & Regulatory Disclaimer (Glassmorphic Container with Subtle Amber/Gold Warning Glow)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (palette.isLight) Color(0xFFFFFBEB)
                        else Color(0xFF0C0B14).copy(alpha = 0.90f)
                    )
                    .border(
                        1.2.dp,
                        if (palette.isLight) Brush.horizontalGradient(listOf(Color(0xFFFDE68A), Color(0xFF00F5FF)))
                        else Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00F5FF).copy(alpha = 0.65f),
                                Color(0xFF00F5FF).copy(alpha = 0.35f),
                                Color(0xFF00F5FF).copy(alpha = 0.65f)
                            )
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF00F5FF).copy(alpha = 0.15f))
                                .border(0.8.dp, Color(0xFF00F5FF).copy(alpha = 0.40f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Disclaimer",
                                tint = Color(0xFF00F5FF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.financialDisclaimerTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (palette.isLight) Color(0xFFB45309) else Color(0xFF00F5FF)
                        )
                    }
                    Text(
                        text = strings.financialDisclaimerText,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = palette.textSecondary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Modal Sheet to select any of the 100+ coins for deep signal diagnostics
    if (showAllCoinsPicker) {
        var pickerSearch by remember { mutableStateOf("") }
        val filteredList = remember(pickerSearch, coins) {
            val q = pickerSearch.trim()
            if (q.isBlank()) coins
            else coins.filter {
                it.name.contains(q, ignoreCase = true) ||
                it.symbol.contains(q, ignoreCase = true) ||
                it.id.contains(q, ignoreCase = true)
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showAllCoinsPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CosmicSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(550.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "100+ " + strings.signalsCoinSelectorTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { showAllCoinsPicker = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pickerSearch,
                    onValueChange = { pickerSearch = it },
                    placeholder = { Text(text = strings.searchPlaceholder, color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NeonCyan) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CosmicSurface,
                        unfocusedContainerColor = CosmicSurface,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { coinItem ->
                        val isSelected = activeCoin.id == coinItem.id
                        val isCoinLocked = coinItem.isPro && !isProUnlocked
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else CosmicSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonCyan else CosmicBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    if (isCoinLocked) {
                                        showAllCoinsPicker = false
                                        onOpenProModal()
                                    } else {
                                        onSelectCoin(coinItem)
                                        showAllCoinsPicker = false
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
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
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(CosmicSurfaceElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = coinItem.symbol.take(3),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = coinItem.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (isCoinLocked) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(NeonAmber.copy(alpha = 0.2f))
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "PRO",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NeonAmber
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${coinItem.symbol} • Rank #${coinItem.rank}",
                                            fontSize = 11.sp,
                                            color = TextMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = coinItem.displayPrice(currency),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = coinItem.formattedChange(includePlus = true),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (coinItem.change24h >= 0) GainGreen else DrawdownRed,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CycleBarRow(
    label: String,
    days: Int,
    progress: Float,
    isCurrent: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = if (isCurrent) NeonCyan else TextPrimary
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$days",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) NeonCyan else TextPrimary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "d",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
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
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isCurrent) NeonCyan else Color(0xFF475569))
            )
        }
    }
}

@Composable
private fun HistoricRiseRow(
    label: String,
    days: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$days",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "d",
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun WhatCameNextCard(
    timeframe: String,
    gain: String,
    winRate: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = timeframe,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = gain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (gain.startsWith("-")) DrawdownRed else GainGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = winRate,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WhyWeSayThisPoint(
    number: Int,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E2D4A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$number",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun KeyMetricCard(
    title: String,
    primaryValue: String,
    primaryValueColor: Color = TextPrimary,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CosmicSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = primaryValue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primaryValueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subValue,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
