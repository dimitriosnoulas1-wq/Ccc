package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoCoin
import com.example.data.model.FeedState
import com.example.data.model.FuturesBookTicker
import com.example.data.model.FuturesConnectionStatus
import com.example.data.model.FuturesLiquidationOrder
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.FuturesOpenInterest
import com.example.data.model.FuturesTickerData
import com.example.data.model.FuturesTrade
import com.example.data.model.MacroMarketSentiment
import com.example.data.model.MarketIntelligenceEngine
import com.example.data.model.MarketIntelligenceReport
import com.example.ui.components.LiquidationIntelligenceCard
import com.example.ui.components.MarketIntelligenceCard
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.MauveAurora
import com.example.ui.components.MetricExplainerBox
import com.example.util.LocalAppStrings
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import com.example.data.model.Currency
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

val LocalFuturesCurrency = compositionLocalOf { Currency.USD }

@Composable
fun FuturesTerminalScreen(
    coins: List<CryptoCoin>,
    selectedCoin: CryptoCoin?,
    activeFuturesSymbol: String,
    tickerData: FuturesTickerData?,
    bookTicker: FuturesBookTicker?,
    markFunding: FuturesMarkFunding?,
    openInterest: FuturesOpenInterest?,
    recentTrades: List<FuturesTrade>,
    recentLiquidations: List<FuturesLiquidationOrder>,
    connectionStatus: FuturesConnectionStatus,
    macroSentiment: MacroMarketSentiment,
    marketIntelligenceReport: MarketIntelligenceReport? = null,
    currency: Currency = Currency.USD,
    isProUnlocked: Boolean,
    isRefreshing: Boolean = false,
    onSelectSymbol: (String) -> Unit,
    onOpenProModal: () -> Unit,
    onOpenAiAssistant: (String?) -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalFuturesCurrency provides currency) {
        val palette = LocalAppColors.current

    // Find the currently selected coin object from the list to check pro access
    val cleanSym = activeFuturesSymbol.uppercase().removeSuffix("USDT").removePrefix("1000")
    val currentCoin = coins.firstOrNull {
        it.symbol.equals(cleanSym, ignoreCase = true)
    } ?: selectedCoin ?: coins.firstOrNull { it.symbol == "BTC" } ?: coins.firstOrNull()

    val isLocked = false

    var showPairPickerSheet by remember { mutableStateOf(false) }
    var selectedFuturesTab by remember { mutableStateOf(0) } // 0: Overview, 1: Liquidations, 2: Funding, 3: Open Interest, 4: Basis

    // Compute live Market Intelligence & Why is Asset Moving report off Main thread
    val isExternalReportMatching = marketIntelligenceReport != null &&
        marketIntelligenceReport.symbol.equals(activeFuturesSymbol, ignoreCase = true) &&
        marketIntelligenceReport.currentPrice > 0.0

    val computedIntelligenceReport by produceState(
        initialValue = if (isExternalReportMatching) {
            marketIntelligenceReport!!
        } else {
            MarketIntelligenceEngine.analyze(
                symbol = activeFuturesSymbol,
                ticker = tickerData,
                bookTicker = bookTicker,
                markFunding = markFunding,
                openInterest = openInterest,
                recentTrades = recentTrades,
                recentLiquidations = recentLiquidations,
                macroSentiment = macroSentiment,
                coinFallback = currentCoin
            )
        },
        activeFuturesSymbol, tickerData?.lastPrice, tickerData?.priceChangePercent24h,
        bookTicker?.spread, markFunding?.fundingRate, openInterest?.openInterestUsd,
        recentTrades.size, recentLiquidations.size, macroSentiment.fearAndGreedValue,
        currentCoin?.priceUsd, currentCoin?.change24h
    ) {
        if (marketIntelligenceReport != null && marketIntelligenceReport.symbol.equals(activeFuturesSymbol, ignoreCase = true) && marketIntelligenceReport.currentPrice > 0.0) {
            value = marketIntelligenceReport
        } else {
            value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                try {
                    MarketIntelligenceEngine.analyze(
                        symbol = activeFuturesSymbol,
                        ticker = tickerData,
                        bookTicker = bookTicker,
                        markFunding = markFunding,
                        openInterest = openInterest,
                        recentTrades = recentTrades,
                        recentLiquidations = recentLiquidations,
                        macroSentiment = macroSentiment,
                        coinFallback = currentCoin
                    )
                } catch (_: Throwable) {
                    MarketIntelligenceReport(
                        symbol = activeFuturesSymbol
                    )
                }
            }
        }
    }
    val intelligenceReport = if (isExternalReportMatching) marketIntelligenceReport!! else computedIntelligenceReport

    // Compute aggregated liquidation clusters off Main thread
    val currentPrice = when {
        tickerData?.fromExchange == true && (tickerData.lastPrice ?: 0.0) > 0.0 -> tickerData.lastPrice ?: 0.0
        markFunding?.fromExchange == true && (markFunding.markPrice ?: 0.0) > 0.0 -> markFunding.markPrice ?: 0.0
        currentCoin?.quoteState == com.example.data.model.QuoteState.LIVE -> currentCoin.priceUsd
        else -> 0.0
    }
    val oiUsd = openInterest?.openInterestUsd ?: 0.0
    val liquidationMap by produceState(
        initialValue = MarketIntelligenceEngine.computeLiquidationMap(
            symbol = activeFuturesSymbol,
            currentPrice = currentPrice,
            openInterestUsd = oiUsd
        ),
        activeFuturesSymbol, currentPrice, oiUsd
    ) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            try {
                MarketIntelligenceEngine.computeLiquidationMap(
                    symbol = activeFuturesSymbol,
                    currentPrice = currentPrice,
                    openInterestUsd = oiUsd
                )
            } catch (_: Throwable) {
                MarketIntelligenceEngine.computeLiquidationMap(
                    symbol = activeFuturesSymbol,
                    currentPrice = currentPrice,
                    openInterestUsd = oiUsd
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("futures_terminal_screen")
    ) {
        // Beginner Risk Guardrail Banner
        val strings = LocalAppStrings.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = palette.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = strings.derivativesRiskGuardrail,
                    fontSize = 11.sp,
                    color = palette.textSecondary,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 1. Uncrowded Terminal Top Bar: Screen Title & Header Action Controls
        TerminalTopBar(
            connectionStatus = connectionStatus,
            tickerData = tickerData,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Dedicated Prominent Active Pair Hero Card (Clear, readable, tap to switch)
        ActivePairHeroBar(
            symbol = activeFuturesSymbol,
            coin = currentCoin,
            tickerData = tickerData,
            onOpenPicker = { showPairPickerSheet = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. USDT-M Symbol Selector Chips with All Pairs Search Button
        FuturesSymbolSelector(
            coins = coins,
            activeSymbol = activeFuturesSymbol,
            isProUnlocked = isProUnlocked,
            onSelectSymbol = onSelectSymbol,
            onOpenAllPairs = { showPairPickerSheet = true }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Futures Sub-Navigation Tabs (Overview, Liquidations, Funding, Open Interest, Basis)
        val futuresTabs = listOf(
            "Overview",
            "Liquidations",
            "Funding",
            "Open Interest",
            "Basis"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(palette.surface)
                .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            futuresTabs.forEachIndexed { idx, tabTitle ->
                val isSelected = selectedFuturesTab == idx
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
                            selectedFuturesTab = idx
                        }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabTitle,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) NeonCyan else palette.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLocked) {
            // PRO Paywall Card for locked pair
            LockedFuturesPairCard(
                coin = currentCoin,
                onOpenProModal = onOpenProModal
            )
        } else {
            // Live Terminal Scrollable Content with Clean Information Hierarchy:
            // Filtered cleanly according to selectedFuturesTab (0: Overview, 1: Liquidations, 2: Funding, 3: Open Interest, 4: Basis)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                // TAB 0: OVERVIEW (Full institutional breakdown)
                if (selectedFuturesTab == 0) {
                    // LAYER 1: Market Intelligence & "Why is Asset Moving?" (Big Signal + Drivers + Invalidation)
                    item {
                        MarketIntelligenceCard(
                            report = intelligenceReport,
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            ticker = tickerData,
                            markFunding = markFunding,
                            openInterest = openInterest
                        )
                    }

                    // LAYER 2: Primary Price & 24h Stats
                    item {
                        PrimaryFuturesPriceCard(
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            ticker = tickerData,
                            markFunding = markFunding
                        )
                    }

                    // LAYER 3: Best Bid / Best Ask (BBO) & Spread Depth
                    item {
                        BestBidAskCard(
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            bookTicker = bookTicker
                        )
                    }

                    // LAYER 4: Funding Rate & Settlement Countdown
                    item {
                        FundingRateCard(
                            symbol = activeFuturesSymbol,
                            markFunding = markFunding,
                            isProUnlocked = isProUnlocked
                        )
                    }

                    // LAYER 5: Open Interest Widget (Polled 15s)
                    item {
                        OpenInterestCard(
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            openInterest = openInterest,
                            isProUnlocked = isProUnlocked
                        )
                    }

                    // LAYER 6: Liquidation Intelligence (Aggregated Clusters Map + Live Forced Orders Stream)
                    item {
                        if (isProUnlocked) {
                            LiquidationIntelligenceCard(
                                liquidationMap = liquidationMap,
                                recentLiquidations = recentLiquidations
                            )
                        } else {
                            com.example.ui.components.MacroProGatekeeper(
                                onOpenProModal = onOpenProModal
                            )
                        }
                    }

                    // LAYER 7: Live Aggregated Trades Tape
                    item {
                        LiveTradesTapeCard(
                            trades = recentTrades,
                            symbol = activeFuturesSymbol
                        )
                    }
                }

                // TAB 1: LIQUIDATIONS FOCUS
                if (selectedFuturesTab == 1) {
                    item {
                        if (isProUnlocked) {
                            LiquidationIntelligenceCard(
                                liquidationMap = liquidationMap,
                                recentLiquidations = recentLiquidations,
                                connectionStatus = connectionStatus,
                                onRetry = onRefresh
                            )
                        } else {
                            com.example.ui.components.MacroProGatekeeper(
                                onOpenProModal = onOpenProModal
                            )
                        }
                    }
                    item {
                        MarketIntelligenceCard(
                            report = intelligenceReport,
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            ticker = tickerData,
                            markFunding = markFunding,
                            openInterest = openInterest
                        )
                    }
                    item {
                        LiveTradesTapeCard(
                            trades = recentTrades,
                            symbol = activeFuturesSymbol
                        )
                    }
                }

                // TAB 2: FUNDING FOCUS
                if (selectedFuturesTab == 2) {
                    item {
                        FundingRateCard(
                            symbol = activeFuturesSymbol,
                            markFunding = markFunding,
                            isProUnlocked = isProUnlocked
                        )
                    }
                    item {
                        PrimaryFuturesPriceCard(
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            ticker = tickerData,
                            markFunding = markFunding
                        )
                    }
                }

                // TAB 3: OPEN INTEREST FOCUS
                if (selectedFuturesTab == 3) {
                    item {
                        OpenInterestCard(
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            openInterest = openInterest,
                            isProUnlocked = isProUnlocked
                        )
                    }
                    item {
                        PrimaryFuturesPriceCard(
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            ticker = tickerData,
                            markFunding = markFunding
                        )
                    }
                }

                // TAB 4: BASIS (Mark Price vs Index vs Spot Spread)
                if (selectedFuturesTab == 4) {
                    item {
                        PrimaryFuturesPriceCard(
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            ticker = tickerData,
                            markFunding = markFunding
                        )
                    }
                    item {
                        BestBidAskCard(
                            symbol = activeFuturesSymbol,
                            coin = currentCoin,
                            bookTicker = bookTicker
                        )
                    }
                    item {
                        FundingRateCard(
                            symbol = activeFuturesSymbol,
                            markFunding = markFunding,
                            isProUnlocked = isProUnlocked
                        )
                    }
                }
            }
        }
    }

    if (showPairPickerSheet) {
        FuturesPairPickerSheet(
            coins = coins,
            activeSymbol = activeFuturesSymbol,
            isProUnlocked = isProUnlocked,
            onSelectSymbol = { targetSym ->
                onSelectSymbol(targetSym)
                showPairPickerSheet = false
            },
            onDismiss = { showPairPickerSheet = false }
        )
    }
    }
}

// ---------------------------------------------------------------------------------
// Subcomponents
// ---------------------------------------------------------------------------------

@Composable
private fun TerminalTopBar(
    connectionStatus: FuturesConnectionStatus,
    tickerData: FuturesTickerData?,
    nowMs: Long = System.currentTimeMillis(),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    val isLive = connectionStatus.isConnected || (tickerData != null && (nowMs - tickerData.receivedTimeMs) <= 30000)
    val isStale = !isLive && (tickerData != null && (nowMs - tickerData.receivedTimeMs) > 30000)

    val feedState = when {
        isLive -> FeedState.LIVE
        isStale -> FeedState.STALE
        !connectionStatus.isConnected -> FeedState.OFFLINE
        else -> FeedState.LIVE
    }

    val stateBadgeColor = when (feedState) {
        FeedState.LIVE -> TachyonMint
        FeedState.STALE -> QuantumCyan
        FeedState.OFFLINE -> SoftCrimson
        FeedState.UNAVAILABLE -> palette.textMuted
    }

    val stateBadgeText = when (feedState) {
        FeedState.LIVE -> strings.futuresStateLive
        FeedState.STALE -> strings.futuresStateStale
        FeedState.OFFLINE -> strings.futuresStateOffline
        FeedState.UNAVAILABLE -> strings.futuresStateUnavailable
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val refreshTransition = rememberInfiniteTransition(label = "futures_refresh_spin")
    val refreshRotation by refreshTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title & Source on the left
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(palette.primary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.futuresTerminalTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    color = palette.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = strings.futuresOfficialFeedsSub,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = palette.textSecondary
            )
        }

        // Action controls on the right (neatly aligned, touch-friendly)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (palette.isLight) Color(0xFFF1F5F9) else CosmicVoidSurface)
                    .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(stateBadgeColor.copy(alpha = if (feedState == FeedState.LIVE) pulseAlpha else 1f))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stateBadgeText,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    color = stateBadgeColor
                )
                val now = System.currentTimeMillis()
                val ageSec = if (connectionStatus.lastEventTimeMs > 0) {
                    ((now - connectionStatus.lastEventTimeMs) / 1000).coerceAtLeast(0)
                } else 0
                val updatedLabel = if (ageSec <= 2) "Updated just now" else "Updated ${ageSec}s ago"
                Text(
                    text = " · $updatedLabel",
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    color = palette.textMuted
                )
            }

            // Refresh Button
            IconButton(
                onClick = {
                    com.example.util.AppSoundManager.playTechClick()
                    onRefresh()
                },
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (palette.isLight) Color(0xFFF1F5F9) else CosmicVoidSurface)
                    .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = strings.refreshTooltip,
                    tint = if (isRefreshing) palette.primary else palette.textSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .then(if (isRefreshing) Modifier.rotate(refreshRotation) else Modifier)
                )
            }
        }
    }
}

@Composable
private fun ActivePairHeroBar(
    symbol: String,
    coin: CryptoCoin?,
    tickerData: FuturesTickerData?,
    onOpenPicker: () -> Unit
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current

    val baseSymbol = symbol.uppercase().removeSuffix("USDT").removePrefix("1000")
    val displaySymbol = if (symbol.startsWith("1000")) "1000$baseSymbol/USDT" else "$baseSymbol/USDT"
    val changePct = tickerData?.priceChangePercent24h ?: coin?.change24h ?: 0.0
    val isPositive = changePct >= 0
    val changeColor = if (isPositive) TachyonMint else SoftCrimson

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                com.example.util.AppSoundManager.playTechClick()
                onOpenPicker()
            }
            .testTag("active_pair_hero_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(palette.primary.copy(alpha = 0.15f))
                        .border(1.dp, palette.primary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = baseSymbol.take(3),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = palette.primary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displaySymbol,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(palette.primary.copy(alpha = 0.18f))
                                .border(0.6.dp, palette.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = if (baseSymbol.equals("BTC", ignoreCase = true)) "BTC PERP" else "${baseSymbol} PERP",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.4.sp,
                                fontFamily = FontFamily.Monospace,
                                color = palette.primary
                            )
                        }
                    }
                    Text(
                        text = "${coin?.name ?: baseSymbol} · ${strings.futuresSwitchPair}",
                        fontSize = 11.sp,
                        color = palette.textSecondary
                    )
                }
            }

            // Right side: 24h Change Pill + Switch indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(changeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    val changeFormatted = com.example.util.AppNumberFormatter.formatPercent(changePct, includeSign = true, decimals = 2)
                    Text(
                        text = changeFormatted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = strings.futuresSwitchPair,
                    tint = palette.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FuturesSymbolSelector(
    coins: List<CryptoCoin>,
    activeSymbol: String,
    isProUnlocked: Boolean,
    onSelectSymbol: (String) -> Unit,
    onOpenAllPairs: () -> Unit
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All Pairs" Search Button chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (palette.isLight) Color(0xFFF1F5F9) else CosmicVoidSurface)
                .border(1.dp, if (palette.isLight) palette.primary.copy(alpha = 0.45f) else CosmicBorder, RoundedCornerShape(10.dp))
                .clickable {
                    com.example.util.AppSoundManager.playTechClick()
                    onOpenAllPairs()
                }
                .padding(horizontal = 11.dp, vertical = 7.dp)
                .testTag("symbol_chip_all_pairs")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = strings.futuresAllPairs,
                    tint = palette.primary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = strings.futuresAllPairs,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primary
                )
            }
        }

        // Quick pair chips
        coins.take(30).forEach { coin ->
            val symUsdt = "${coin.symbol.uppercase()}USDT"
            val targetSym = when (coin.symbol.uppercase()) {
                "PEPE" -> "1000PEPEUSDT"
                "SHIB" -> "1000SHIBUSDT"
                "FLOKI" -> "1000FLOKIUSDT"
                "BONK" -> "1000BONKUSDT"
                "LUNC" -> "1000LUNCUSDT"
                else -> symUsdt
            }
            val isSelected = activeSymbol.equals(targetSym, ignoreCase = true) ||
                    activeSymbol.equals(symUsdt, ignoreCase = true)
            val isLocked = false

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) palette.primary.copy(alpha = 0.22f)
                        else if (palette.isLight) Color(0xFFF8FAFC)
                        else CosmicVoidSurface
                    )
                    .border(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) palette.primary else if (palette.isLight) palette.border else CosmicBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { 
                        com.example.util.AppSoundManager.playTechClick()
                        onSelectSymbol(targetSym) 
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("symbol_chip_${coin.symbol}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(palette.primary)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = coin.symbol,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                        color = if (isSelected) palette.primary else palette.textPrimary
                    )
                    if (isLocked) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Pro Pair",
                            tint = QuantumCyan,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuturesPairPickerSheet(
    coins: List<CryptoCoin>,
    activeSymbol: String,
    isProUnlocked: Boolean,
    onSelectSymbol: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredCoins = remember(searchQuery, coins) {
        if (searchQuery.isBlank()) coins
        else coins.filter {
            it.symbol.contains(searchQuery, ignoreCase = true) ||
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (palette.isLight) palette.surface else CosmicVoidBg,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.futuresAllPairs,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = palette.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = strings.futuresSearchPair, fontSize = 14.sp, color = palette.textMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = palette.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = palette.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.primary,
                    unfocusedBorderColor = palette.border,
                    focusedTextColor = palette.textPrimary,
                    unfocusedTextColor = palette.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredCoins, key = { it.id }) { coin ->
                    val symUsdt = "${coin.symbol.uppercase()}USDT"
                    val targetSym = when (coin.symbol.uppercase()) {
                        "PEPE" -> "1000PEPEUSDT"
                        "SHIB" -> "1000SHIBUSDT"
                        "FLOKI" -> "1000FLOKIUSDT"
                        "BONK" -> "1000BONKUSDT"
                        "LUNC" -> "1000LUNCUSDT"
                        else -> symUsdt
                    }
                    val isSelected = activeSymbol.equals(targetSym, ignoreCase = true) ||
                            activeSymbol.equals(symUsdt, ignoreCase = true)
                    val isLocked = false
                    val isPos = coin.change24h >= 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) palette.primary.copy(alpha = 0.15f)
                                else if (palette.isLight) Color(0xFFF8FAFC)
                                else CosmicVoidSurface
                            )
                            .border(
                                1.dp,
                                if (isSelected) palette.primary else if (palette.isLight) palette.border else CosmicBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                com.example.util.AppSoundManager.playTechClick()
                                onSelectSymbol(targetSym)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(palette.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = coin.symbol.take(3),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = palette.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${coin.symbol}/USDT",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) palette.primary else palette.textPrimary
                                    )
                                    if (isLocked) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Pro Pair",
                                            tint = QuantumCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = coin.name,
                                    fontSize = 11.sp,
                                    color = palette.textSecondary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatPrice(coin.priceUsd),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = palette.textPrimary
                            )
                            val changeFormatted = com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = true, decimals = 2)
                            Text(
                                text = changeFormatted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPos) TachyonMint else SoftCrimson
                            )
                        }
                    }
                }
            }
        }
    }
}
