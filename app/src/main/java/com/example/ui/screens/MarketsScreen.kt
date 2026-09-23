package com.example.ui.screens

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.ripple
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeOption
import com.example.data.model.CoinCategory
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.viewmodel.MainTab
import com.example.ui.components.CategoryFilterPills
import com.example.ui.components.CoinAvatar
import com.example.ui.components.CryptoCoinRow
import com.example.ui.components.DashboardTopAppBar
import com.example.ui.components.SearchBarField
import com.example.ui.components.TrustBar
import com.example.ui.components.QuantumReticleBadge
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.GainGreen
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.QuantumCornerReticleOverlay
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SapphireBlueBright
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.SyneFont
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.stitchHorizonPanel
import com.example.util.LocalAppStrings

@Composable
fun MarketsScreen(
    coins: List<CryptoCoin>,
    featuredHeroCoin: CryptoCoin?,
    searchQuery: String,
    selectedCategory: CoinCategory,
    currency: Currency,
    selectedTheme: AppThemeOption = AppThemeOption.GALAXY_DARK,
    isProUnlocked: Boolean,
    isRefreshing: Boolean,
    isConnected: Boolean = true,
    isCacheStale: Boolean = false,
    priceSource: String = "Binance",
    lastUpdatedTimestamp: Long = 0L,
    tickerData: com.example.data.model.FuturesTickerData? = null,
    fearAndGreedScore: Int = 0,
    whaleFlow: com.example.data.model.WhaleFlowSnapshot = com.example.data.model.WhaleFlowSnapshot(),
    cycleCommandState: com.example.data.model.CycleCommandState = com.example.data.model.CycleCommandState(),
    derivativesSnapshot: com.example.data.model.AggregatedDerivativesSnapshot? = null,
    onSearchChanged: (String) -> Unit,
    onCategoryChanged: (CoinCategory) -> Unit,
    onCoinClicked: (CryptoCoin) -> Unit,
    onFavoriteToggle: (String) -> Unit,
    onOpenProModal: () -> Unit,
    onOpenAiAssistant: (String?) -> Unit = {},
    onAlertHistoryClick: () -> Unit = {},
    unreadAlertsCount: Int = 0,
    onNavigateToTab: (MainTab) -> Unit = {},
    onRefresh: () -> Unit,
    onBackToCycle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("markets_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onBackToCycle != null) {
                item {
                    Text(
                        text = if (strings.language.code == "el") "← Κύκλος Bitcoin" else "← Bitcoin cycle",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan,
                        modifier = Modifier
                            .clickable { onBackToCycle() }
                            .padding(vertical = 4.dp)
                    )
                }
            }

            // 1. TOP APP BAR
            item {
                val avgMarketVolatility = remember(coins) {
                    if (coins.isNotEmpty()) {
                        coins.take(15).map { kotlin.math.abs(it.change24h) }.average().toFloat().coerceIn(0.5f, 15f)
                    } else 3.5f
                }
                val volatilityMultiplier = (avgMarketVolatility / 3.0f).coerceIn(0.5f, 3.5f)

                DashboardTopAppBar(
                    isProUnlocked = isProUnlocked,
                    isRefreshing = isRefreshing,
                    volatilityMultiplier = volatilityMultiplier,
                    unreadAlertsCount = unreadAlertsCount,
                    onAlertHistoryClick = onAlertHistoryClick,
                    onRefreshClick = onRefresh,
                    onProfileClick = { onOpenProModal() },
                    onProBadgeClick = onOpenProModal
                )
            }

            // Screen Golden Title & Cosmic Accent Line
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.marketsHeader.uppercase(),
                            fontFamily = SpaceGroteskFont,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.4.sp,
                            color = TextPrimary
                        )
                        QuantumReticleBadge(
                            tag = "0x01",
                            label = "LIVE FEED",
                            color = QuantumCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        QuantumCyan.copy(alpha = 0.8f),
                                        MauveAurora.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }

            // Trust Status Bar
            item {
                TrustBar(
                    isRefreshing = isRefreshing,
                    isConnected = isConnected,
                    sourceName = priceSource,
                    lastUpdatedMs = lastUpdatedTimestamp,
                    onRefresh = onRefresh
                )
            }

            // Real-Time Universal Spot & Futures Live Price Bar
            item {
                val btcCoin = coins.firstOrNull { it.symbol == "BTC" }
                val ethCoin = coins.firstOrNull { it.symbol == "ETH" }
                val activeTickerSymbol = tickerData?.symbol?.uppercase()
                    ?.removeSuffix("USDT")
                    ?.removeSuffix("BUSD")
                    ?.removePrefix("1000")
                val activeCoin = if (activeTickerSymbol != null && activeTickerSymbol != "BTC" && activeTickerSymbol != "ETH") {
                    coins.firstOrNull { it.symbol.equals(activeTickerSymbol, ignoreCase = true) }
                } else null

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D0A1D))
                        .border(1.dp, Brush.horizontalGradient(listOf(QuantumCyan.copy(alpha = 0.4f), MauveAurora.copy(alpha = 0.3f))), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (btcCoin != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(QuantumCyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.5.dp)
                            ) {
                                Text(
                                    text = if (btcCoin.isLivePrice) "LIVE" else "STALE",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMonoFont,
                                    color = if (btcCoin.isLivePrice) TachyonMint else palette.textSecondary
                                )
                            }
                            Text(text = "BTC", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFont, color = palette.textPrimary)
                            Text(
                                text = com.example.util.AppNumberFormatter.formatPrice(btcCoin.priceUsd, currency),
                                fontSize = 12.5.sp,
                                fontFamily = JetBrainsMonoFont,
                                fontWeight = FontWeight.SemiBold,
                                color = TachyonMint
                            )
                        }
                    }
                    if (ethCoin != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(text = "ETH", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFont, color = palette.textSecondary)
                            Text(
                                text = com.example.util.AppNumberFormatter.formatPrice(ethCoin.priceUsd, currency),
                                fontSize = 12.sp,
                                fontFamily = JetBrainsMonoFont,
                                fontWeight = FontWeight.Medium,
                                color = QuantumCyan
                            )
                        }
                    }
                    if (activeCoin != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(text = activeCoin.symbol, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFont, color = palette.primary)
                            Text(
                                text = com.example.util.AppNumberFormatter.formatPrice(activeCoin.priceUsd, currency),
                                fontSize = 12.sp,
                                fontFamily = JetBrainsMonoFont,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.primary
                            )
                        }
                    }
                }
            }

            // ==========================================
            // CYBERPUNK BENTO TERMINAL DASHBOARD
            // ==========================================

            // TOP DUAL-WIDGET ROW (Whale Radar + Cycle Scenario Radar)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CyberWhaleRadarWidget(
                        modifier = Modifier.weight(1f),
                        flow = whaleFlow,
                        onClick = { onNavigateToTab(MainTab.SIGNALS) }
                    )
                    CyberScenarioRadarWidget(
                        modifier = Modifier.weight(1f),
                        cycle = cycleCommandState,
                        onClick = { onNavigateToTab(MainTab.MACRO) }
                    )
                }
            }

            // MIDDLE WIDE PANEL (Order Flow & Live Pressure Stream)
            item {
                CyberLivePressurePanel(
                    isProUnlocked = isProUnlocked,
                    snapshot = derivativesSnapshot,
                    onOpenProModal = onOpenProModal,
                    onNavigateToFutures = { onNavigateToTab(MainTab.FUTURES) }
                )
            }

            // ALTCOIN SEASON & MACRO GAUGE PANEL
            item {
                val dynamicAltcoinScore = remember(coins) {
                    com.example.util.AltcoinSeasonCalculator.calculate(coins).score
                }
                CyberAltcoinSeasonPanel(
                    altcoinScore = dynamicAltcoinScore,
                    fearGreedScore = fearAndGreedScore,
                    onClick = { onNavigateToTab(MainTab.MACRO) }
                )
            }

            // Section Title & Description
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp).padding(top = 4.dp)) {
                    Text(
                        text = strings.marketsHeader,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.marketsSubtitle,
                        fontSize = 12.sp,
                        color = palette.textSecondary
                    )
                }
            }

            // Featured Hero Coin Card
            item {
                featuredHeroCoin?.let { hero ->
                    HeroCoinCard(
                        hero = hero,
                        currency = currency,
                        onClick = {
                            onCoinClicked(hero)
                        }
                    )
                }
            }

            // Historical Analog Pro Quiet Callout Link
            item {
                HistoricalAnalogProCard(
                    isProUnlocked = isProUnlocked,
                    onUpgradeClicked = onOpenProModal
                )
            }

            // Search Bar
            item {
                SearchBarField(
                    query = searchQuery,
                    onQueryChanged = onSearchChanged
                )
            }

            // Category Filter Pills
            item {
                CategoryFilterPills(
                    selectedCategory = selectedCategory,
                    onSelectCategory = onCategoryChanged
                )
            }

            // Coins Count & Feed Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countLabel = if (coins.size == 1) strings.cryptocurrencySingular else strings.cryptocurrenciesCount
                    Text(
                        text = "${coins.size} $countLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = palette.textSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(palette.gainColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = strings.realtimeFeed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            color = palette.textSecondary
                        )
                    }
                }
            }

            // Table Columns Header Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ASSET",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color(0xFF64748B)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                        Text(
                            text = "CHART",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "PRICE / 24H",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Coins List
            items(coins, key = { it.id }) { coin ->
                CryptoCoinRow(
                    coin = coin,
                    currency = currency,
                    isProUnlocked = isProUnlocked,
                    isCacheStale = isCacheStale,
                    onCoinClicked = { clickedCoin ->
                        onCoinClicked(clickedCoin)
                    },
                    onFavoriteToggle = onFavoriteToggle
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ==========================================
// TELEMETRY BENTO WIDGET COMPONENTS
// ==========================================

@Composable
fun CyberWhaleRadarWidget(
    modifier: Modifier = Modifier,
    flow: com.example.data.model.WhaleFlowSnapshot = com.example.data.model.WhaleFlowSnapshot(),
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(160.dp)
            .graphicsLayer {
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .stitchHorizonPanel(
                shape = RoundedCornerShape(16.dp),
                borderWidth = if (isPressed) 2.dp else 1.2.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(14.dp)
    ) {
        QuantumCornerReticleOverlay(color = QuantumCyan.copy(alpha = 0.35f))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WHALE FLOW",
                    fontSize = 11.5.sp,
                    fontFamily = SpaceGroteskFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(TachyonMint)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (flow.isLive) {
                        val sign = if (flow.netUsd >= 0) "+" else "-"
                        sign + com.example.util.AppNumberFormatter.formatCompactCurrency(kotlin.math.abs(flow.netUsd))
                    } else "—",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SyneFont,
                    color = if (flow.netUsd >= 0) TachyonMint else Color(0xFFFF5252)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TachyonMint.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when {
                            !flow.isLive -> "WAITING LIVE PRINTS"
                            flow.netUsd >= 0 -> "NET ACCUMULATION"
                            else -> "NET DISTRIBUTION"
                        },
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = TachyonMint
                    )
                }
            }

            Text(
                text = if (flow.isLive) "${flow.alertCount} large prints · ${flow.sourceLabel}" else "Binance USDT-M large prints",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFont,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CyberScenarioRadarWidget(
    modifier: Modifier = Modifier,
    cycle: com.example.data.model.CycleCommandState = com.example.data.model.CycleCommandState(),
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(160.dp)
            .graphicsLayer {
                scaleX = if (isPressed) 0.97f else 1f
                scaleY = if (isPressed) 0.97f else 1f
            }
            .stitchHorizonPanel(
                shape = RoundedCornerShape(16.dp),
                borderWidth = if (isPressed) 2.dp else 1.2.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(14.dp)
    ) {
        QuantumCornerReticleOverlay(color = MauveAurora.copy(alpha = 0.35f))
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CYCLE RISK",
                    fontSize = 11.5.sp,
                    fontFamily = SpaceGroteskFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(QuantumCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = "SYNC",
                        fontSize = 8.sp,
                        fontFamily = JetBrainsMonoFont,
                        fontWeight = FontWeight.Bold,
                        color = QuantumCyan
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (cycle.compositeCycleScore > 0) {
                            String.format(java.util.Locale.US, "%.2f", cycle.compositeCycleScore / 100.0)
                        } else "—",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SyneFont,
                        color = QuantumCyan
                    )
                    Text(
                        text = "/ 1.00",
                        fontSize = 12.sp,
                        fontFamily = JetBrainsMonoFont,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(QuantumCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = cycle.regime.actionEn.uppercase(),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = QuantumCyan
                    )
                }
            }

            Text(
                text = if (cycle.halvingDaysElapsed > 0) {
                    "Halving day ${cycle.halvingDaysElapsed}"
                } else "Waiting live cycle clock",
                fontSize = 10.sp,
                fontFamily = JetBrainsMonoFont,
                color = Color(0xFF94A3B8),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CyberLivePressurePanel(
    isProUnlocked: Boolean,
    snapshot: com.example.data.model.AggregatedDerivativesSnapshot? = null,
    onOpenProModal: () -> Unit,
    onNavigateToFutures: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isPressed) 0.98f else 1f
                scaleY = if (isPressed) 0.98f else 1f
            }
            .stitchHorizonPanel(
                shape = RoundedCornerShape(16.dp),
                borderWidth = if (isPressed) 2.dp else 1.2.dp
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (!isProUnlocked) onOpenProModal() else onNavigateToFutures()
            }
            .padding(14.dp)
    ) {
        QuantumCornerReticleOverlay(color = QuantumCyan.copy(alpha = 0.30f))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(QuantumCyan)
                    )
                    Text(
                        text = "DERIVATIVES HEATMAP",
                        fontSize = 12.5.sp,
                        fontFamily = SpaceGroteskFont,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!isProUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(10.dp))
                            Text(text = "PRO", fontSize = 9.sp, fontFamily = JetBrainsMonoFont, fontWeight = FontWeight.Bold, color = NeonAmber)
                        }
                    }
                } else {
                    Text(text = "BINANCE FEED", fontSize = 9.sp, fontFamily = JetBrainsMonoFont, color = QuantumCyan)
                }
            }

            // 3-Column Telemetry Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "24h Liqs", fontSize = 9.5.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    val longLiq = snapshot?.aggregated?.longLiqUsd
                    val shortLiq = snapshot?.aggregated?.shortLiqUsd
                    val liqTotal = listOfNotNull(longLiq, shortLiq).sum()
                    Text(
                        text = if (liqTotal > 0) com.example.util.AppNumberFormatter.formatCompactCurrency(liqTotal) else "—",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = Color(0xFFFF5252)
                    )
                    val lsr = snapshot?.aggregated?.longShortRatio
                    val shortPct = if (lsr != null && lsr > 0.0) ((1.0 / (1.0 + lsr)) * 100.0) else null
                    Text(
                        text = if (shortPct != null) "Shorts ${"%.0f".format(shortPct)}%" else "Waiting venues",
                        fontSize = 9.sp,
                        fontFamily = JetBrainsMonoFont,
                        color = Color(0xFFCBD5E1)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "Funding 8h", fontSize = 9.5.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    val funding = snapshot?.aggregated?.fundingRate
                    Text(
                        text = if (funding != null) {
                            val pct = funding * 100.0
                            "${if (pct >= 0) "+" else ""}${"%.3f".format(java.util.Locale.US, pct)}%"
                        } else "—",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = TachyonMint
                    )
                    Text(
                        text = snapshot?.sourceLabel ?: "BINANCE / BYBIT / OKX",
                        fontSize = 9.sp,
                        fontFamily = JetBrainsMonoFont,
                        color = Color(0xFFCBD5E1)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.End) {
                    Text(text = "Open Interest", fontSize = 9.5.sp, fontFamily = SpaceGroteskFont, color = Color(0xFF94A3B8))
                    val oi = snapshot?.aggregated?.openInterestUsd
                    Text(
                        text = if (oi != null && oi > 0) com.example.util.AppNumberFormatter.formatCompactCurrency(oi) else "—",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = QuantumCyan
                    )
                    val oiChg = snapshot?.aggregated?.oiChange1hPct
                    Text(
                        text = if (oiChg != null) "${if (oiChg >= 0) "+" else ""}${"%.1f".format(java.util.Locale.US, oiChg)}%" else "live OI",
                        fontSize = 9.sp,
                        fontFamily = JetBrainsMonoFont,
                        color = TachyonMint
                    )
                }
            }

            // Bottom Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF140D2E))
                    .border(0.8.dp, MauveAurora.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonAmber)
                    )
                    Text(
                        text = when {
                            snapshot?.aggregated?.fundingRate == null -> "WAITING LIVE DERIVATIVES"
                            (snapshot.aggregated.fundingRate ?: 0.0) > 0.0003 -> "LONG PREMIUM / HEAT"
                            (snapshot.aggregated.fundingRate ?: 0.0) < -0.0001 -> "SHORT PREMIUM"
                            else -> "NEUTRAL FUNDING"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFont,
                        color = NeonAmber
                    )
                }
                Text(
                    text = "Open Futures →",
                    fontSize = 10.5.sp,
                    fontFamily = SpaceGroteskFont,
                    fontWeight = FontWeight.Bold,
                    color = QuantumCyan
                )
            }
        }
    }
}

@Composable
fun CyberAltcoinSeasonPanel(
    altcoinScore: Int,
    fearGreedScore: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .stitchHorizonPanel(shape = RoundedCornerShape(16.dp), borderWidth = 1.2.dp)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        QuantumCornerReticleOverlay(color = QuantumCyan.copy(alpha = 0.30f))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ Altcoin Season Index",
                    fontSize = 12.5.sp,
                    fontFamily = SpaceGroteskFont,
                    fontWeight = FontWeight.Bold,
                    color = QuantumCyan
                )
                Text(
                    text = "Fear & Greed: $fearGreedScore",
                    fontSize = 11.sp,
                    fontFamily = JetBrainsMonoFont,
                    fontWeight = FontWeight.SemiBold,
                    color = MauveAurora
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$altcoinScore / 100",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SyneFont,
                    color = Color.White
                )
                Text(
                    text = when {
                        altcoinScore >= 75 -> "Altcoin Season"
                        altcoinScore <= 25 -> "Bitcoin Season"
                        else -> "Neutral rotation"
                    },
                    fontSize = 11.sp,
                    fontFamily = SpaceGroteskFont,
                    color = Color(0xFFCBD5E1)
                )
            }

            // Progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CosmicVoidBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(altcoinScore.toFloat().coerceAtLeast(1f))
                        .background(
                            Brush.horizontalGradient(
                                listOf(QuantumCyan, MauveAurora)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight((100 - altcoinScore).toFloat().coerceAtLeast(1f))
                )
            }
        }
    }
}

// ==========================================
// REMAINING CARDS (HERO & ANALOGS)
// ==========================================

@Composable
fun HeroCoinCard(
    hero: CryptoCoin,
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val isGain = hero.change24h >= 0

    var prevPrice by remember { mutableDoubleStateOf(hero.priceUsd) }
    val flashAlpha = remember { Animatable(0f) }
    var flashColor by remember { mutableStateOf(Color.Transparent) }

    LaunchedEffect(hero.priceUsd) {
        if (prevPrice != 0.0 && hero.priceUsd != prevPrice) {
            val isUp = hero.priceUsd > prevPrice
            flashColor = if (isUp) Color(0xFF00E676) else Color(0xFFFF1744)
            flashAlpha.snapTo(0.95f)
            flashAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
            )
        }
        prevPrice = hero.priceUsd
    }

    val volatilityMultiplier = (kotlin.math.abs(hero.change24h).toFloat() / 3.5f).coerceIn(0.6f, 4f)
    val shimmerDuration = (2000 / volatilityMultiplier).toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "hero_coin_shimmer")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shimmerDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_shimmer_phase"
    )

    val borderBrush = if (flashAlpha.value > 0.02f) {
        Brush.horizontalGradient(
            listOf(
                flashColor.copy(alpha = flashAlpha.value),
                flashColor.copy(alpha = flashAlpha.value * 0.4f),
                flashColor.copy(alpha = flashAlpha.value)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                palette.border,
                if (isGain) Color(0xFF00D2FF).copy(alpha = 0.35f) else Color(0xFFFF1744).copy(alpha = 0.30f),
                palette.border
            ),
            startX = shimmerPhase * 400f,
            endX = shimmerPhase * 400f + 300f
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (flashAlpha.value > 0.05f) {
                    flashColor.copy(alpha = flashAlpha.value * 0.18f)
                } else {
                    Color(0xFF0D0A1D)
                }
            )
            .border(
                width = if (flashAlpha.value > 0.05f) 2.dp else 1.2.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("hero_coin_card")
    ) {
        QuantumCornerReticleOverlay(color = QuantumCyan.copy(alpha = 0.40f))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    CoinAvatar(symbol = hero.symbol, modifier = Modifier.size(42.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = hero.name,
                            fontSize = 17.sp,
                            fontFamily = SpaceGroteskFont,
                            fontWeight = FontWeight.Bold,
                            color = palette.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = hero.symbol,
                                fontSize = 12.sp,
                                fontFamily = JetBrainsMonoFont,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.textSecondary,
                                maxLines = 1
                            )
                            if (hero.symbol.equals("BTC", ignoreCase = true)) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(QuantumCyan.copy(alpha = 0.18f))
                                        .border(0.6.dp, QuantumCyan.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "BTC SPOT",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.4.sp,
                                        fontFamily = JetBrainsMonoFont,
                                        color = QuantumCyan
                                    )
                                }
                            } else if (hero.symbol.equals("ETH", ignoreCase = true)) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SapphireBlueBright.copy(alpha = 0.18f))
                                        .border(0.6.dp, SapphireBlueBright.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "ETH SPOT",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.4.sp,
                                        fontFamily = JetBrainsMonoFont,
                                        color = SapphireBlueBright
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        val strings = LocalAppStrings.current
                        Text(
                            text = strings.analogMatch,
                            fontSize = 11.sp,
                            fontFamily = SpaceGroteskFont,
                            fontWeight = FontWeight.Medium,
                            color = QuantumCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = hero.displayPrice(currency),
                        fontSize = if (hero.quoteState == com.example.data.model.QuoteState.LIVE) 24.sp else 18.sp,
                        fontFamily = JetBrainsMonoFont,
                        fontWeight = FontWeight.Bold,
                        color = if (hero.quoteState == com.example.data.model.QuoteState.LIVE) TachyonMint else palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val dd = hero.drawdownPercent
                    val ddColor = if (dd < 0) palette.lossColor else TachyonMint
                    val formattedDd = com.example.util.AppNumberFormatter.formatPercent(dd, includeSign = true, decimals = 1)
                    Text(
                        text = "$formattedDd ATH",
                        fontSize = 12.5.sp,
                        fontFamily = JetBrainsMonoFont,
                        fontWeight = FontWeight.SemiBold,
                        color = ddColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun HistoricalAnalogProCard(
    isProUnlocked: Boolean,
    onUpgradeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppColors.current
    val strings = LocalAppStrings.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .stitchHorizonPanel(shape = RoundedCornerShape(12.dp), borderWidth = 1.2.dp)
            .clickable { onUpgradeClicked() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("pro_analog_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.historicalAnalogProTitle,
                    fontSize = 13.5.sp,
                    fontFamily = SpaceGroteskFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isProUnlocked) strings.historicalAnalogProUnlockedDesc else strings.historicalAnalogProDesc,
                    fontSize = 11.5.sp,
                    fontFamily = SpaceGroteskFont,
                    color = Color(0xFF94A3B8),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isProUnlocked) "${strings.proActive} →" else "${strings.upgradeToPro} →",
                fontSize = 12.sp,
                fontFamily = SpaceGroteskFont,
                fontWeight = FontWeight.Bold,
                color = QuantumCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
