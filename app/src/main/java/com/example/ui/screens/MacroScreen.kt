package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.JetBrainsMonoFont
import com.example.ui.theme.QuantumCornerReticleOverlay
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.SapphireBlueBright
import com.example.ui.theme.SpaceGroteskFont
import com.example.ui.theme.SyneFont
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.CosmicVoidBg
import com.example.ui.theme.CosmicVoidSurface
import com.example.ui.theme.CosmicVoidSurfaceElevated
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.TachyonMint
import com.example.ui.theme.SoftCrimson
import com.example.ui.theme.MauveAurora
import com.example.ui.theme.stitchHorizonPanel
import com.example.ui.components.QuantumReticleBadge
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AltcoinSeasonData
import com.example.data.model.BitcoinEtfFlowData
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.data.model.CycleCommandState
import com.example.data.model.FearAndGreedData
import com.example.data.model.ForwardSignalAuditEntry
import com.example.data.model.PiCycleData
import com.example.data.model.StablecoinLiquidityData
import com.example.ui.components.CycleCommandPanel
import com.example.ui.components.BitcoinEtfFlowsCard
import com.example.ui.components.DefiLlamaStablecoinsCard
import com.example.ui.components.ForwardAuditTrailCard
import com.example.ui.components.AltcoinSeasonIndexCard
import com.example.ui.components.BitcoinRainbowChart
import com.example.ui.components.CycleDayComparisonCard
import com.example.ui.components.FearAndGreedIndexCard
import com.example.ui.components.MacroProGatekeeper
import com.example.ui.components.MetricExplainerBox
import com.example.ui.components.PiCycleTopCard
import com.example.ui.components.TrustBar
import com.example.ui.components.WhalePortfolioRiskSimulatorCard
import com.example.ui.theme.GainGreen
import com.example.ui.theme.DrawdownRed
import com.example.ui.theme.AppThemePalette
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.util.LocalAppStrings

enum class MacroSectionFilter(val titleEn: String, val titleEl: String) {
    ALL("All Macro", "Όλοι οι Δείκτες"),
    CYCLE_DAYS_ANALOG("⏳ Cycle Days & Analog", "⏳ Ημέρες Κύκλου & Αναλογία"),
    RISK_BACKDROP("🌐 DXY, 10Y Yields", "🌐 DXY, 10Y"),
    RAINBOW("🌈 Rainbow Chart", "🌈 Rainbow Chart"),
    ALT_SEASON("⚡ Alt Season", "⚡ Alt Season"),
    FEAR_GREED("😱 Fear & Greed", "😱 Fear & Greed"),
    PI_CYCLE("🎯 Pi Cycle & 200W", "🎯 Pi Cycle & 200W");

    fun localizedTitle(isGreek: Boolean): String = if (isGreek) titleEl else titleEn
}

@Composable
fun MacroScreen(
    coins: List<CryptoCoin>,
    currency: Currency,
    isProUnlocked: Boolean,
    centralizedBtcPrice: Double = 0.0,
    etfFlowData: BitcoinEtfFlowData = BitcoinEtfFlowData(),
    cycleCommandState: CycleCommandState = CycleCommandState(),
    stablecoinLiquidityData: StablecoinLiquidityData = StablecoinLiquidityData(),
    forwardAuditLogs: List<ForwardSignalAuditEntry> = emptyList(),
    liveMovingAverages: com.example.data.model.LiveMovingAverages = com.example.data.model.LiveMovingAverages(),
    globalRiskSnapshot: com.example.data.model.GlobalRiskSnapshot = com.example.data.model.GlobalRiskSnapshot(),
    fearAndGreedScore: Int = 0,
    fearAndGreedClassification: String = "—",
    macroSentiment: com.example.data.model.MacroMarketSentiment = com.example.data.model.MacroMarketSentiment(),
    isRefreshing: Boolean = false,
    isConnected: Boolean = true,
    priceSource: String = "Binance",
    lastUpdatedTimestamp: Long = 0L,
    onOpenProModal: () -> Unit,
    onOpenAiAssistant: (String?) -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = LocalAppColors.current
    val isGreek = strings.language.code == "el"

    var selectedFilter by remember { mutableStateOf(MacroSectionFilter.ALL) }

    // Collapsible section state for compact layout
    var cycleDaysExpanded by remember { mutableStateOf(true) }
    var riskBackdropExpanded by remember { mutableStateOf(true) }
    var rainbowExpanded by remember { mutableStateOf(true) }
    var altSeasonExpanded by remember { mutableStateOf(true) }
    var fearGreedExpanded by remember { mutableStateOf(true) }
    var piCycleExpanded by remember { mutableStateOf(true) }

    // Pulsing live dot
    val infiniteTransition = rememberInfiniteTransition(label = "macro_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val refreshTransition = rememberInfiniteTransition(label = "macro_refresh_spin")
    val refreshRotation by refreshTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val btcCoin = remember(coins) {
        coins.firstOrNull { it.symbol.equals("BTC", ignoreCase = true) }
    }
    val btcPriceUsd = if (centralizedBtcPrice > 0.0) centralizedBtcPrice else (btcCoin?.priceUsd ?: 0.0)

    var altSeasonData by remember { 
        mutableStateOf(com.example.util.AltcoinSeasonCalculator.calculateLocalFallback(coins, btcPriceUsd)) 
    }
    
    androidx.compose.runtime.LaunchedEffect(coins, btcPriceUsd) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val fetched = com.example.util.AltcoinSeasonCalculator.calculate(coins, btcPriceUsd)
            altSeasonData = fetched
        }
    }

    val activeZoneColor = when {
        altSeasonData.isAltSeason -> NeonEmerald
        altSeasonData.isBtcSeason -> NeonAmber
        else -> NeonCyan
    }

    val isFgLive = fearAndGreedScore > 0
    val resolvedFgScore = if (isFgLive) fearAndGreedScore else 0
    val resolvedFgSent = if (isFgLive && !fearAndGreedClassification.isNullOrBlank()) {
        fearAndGreedClassification
    } else {
        "—"
    }

    val fearGreedData = remember(resolvedFgScore, resolvedFgSent, isFgLive, macroSentiment) {
        val sentimentEl = when {
            !isFgLive -> "—"
            resolvedFgScore < 25 -> "Ακραίος Φόβος"
            resolvedFgScore < 45 -> "Φόβος"
            resolvedFgScore < 55 -> "Ουδέτερο"
            resolvedFgScore < 75 -> "Απληστία"
            else -> "Ακραία Απληστία"
        }
        FearAndGreedData(
            score = resolvedFgScore,
            sentiment = resolvedFgSent,
            sentimentEl = sentimentEl,
            yesterdayScore = macroSentiment.fearAndGreedYesterday ?: 0,
            lastWeekScore = macroSentiment.fearAndGreedLastWeek ?: 0,
            lastMonthScore = macroSentiment.fearAndGreedLastMonth ?: 0,
            isLive = isFgLive
        )
    }

    val piCycleData = remember(btcPriceUsd, liveMovingAverages) {
        val dma111 = liveMovingAverages.dma111 ?: 0.0
        val dma350x2 = liveMovingAverages.dma350x2 ?: 0.0
        val ema150 = liveMovingAverages.ema150 ?: 0.0
        val sma471x0745 = liveMovingAverages.sma471x0745 ?: 0.0
        val ma200w = liveMovingAverages.ma200w ?: 0.0
        val topGap = if (dma111 > 0.0 && dma350x2 > 0.0) ((dma350x2 - dma111) / dma111 * 100.0) else 0.0
        val bottomGap = if (ema150 > 0.0 && sma471x0745 > 0.0) ((ema150 - sma471x0745) / sma471x0745 * 100.0) else 0.0
        PiCycleData(
            currentBtcPrice = btcPriceUsd,
            dma111 = dma111,
            dma350x2 = dma350x2,
            isCrossed = dma111 > 0.0 && dma350x2 > 0.0 && dma111 >= dma350x2,
            distanceToTopCrossPct = topGap,
            ema150 = ema150,
            sma471x0745 = sma471x0745,
            isBottomCrossed = ema150 > 0.0 && sma471x0745 > 0.0 && ema150 <= sma471x0745,
            distanceToBottomCrossPct = bottomGap,
            ma200w = ma200w,
            distanceAbove200wPct = if (ma200w > 0.0 && btcPriceUsd > 0.0) ((btcPriceUsd - ma200w) / ma200w * 100.0) else 0.0,
            isLive = liveMovingAverages.isLive
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("macro_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Screen Title Header + Live Status
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.macroScreenTitle.uppercase(),
                        fontFamily = SpaceGroteskFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 1.2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp)
                    )

                    // Header Right Actions: Refresh + Live timestamp pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuantumReticleBadge(
                            tag = "0x03",
                            label = "MACRO",
                            color = MauveAurora
                        )

                        // Refresh Button
                        IconButton(
                            onClick = {
                                com.example.util.AppSoundManager.playTechClick()
                                onRefresh()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF140D2E))
                                .border(1.dp, MauveAurora.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = strings.refreshTooltip,
                                tint = if (isRefreshing) QuantumCyan else palette.textSecondary,
                                modifier = Modifier
                                    .size(15.dp)
                                    .then(if (isRefreshing) Modifier.rotate(refreshRotation) else Modifier)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = strings.macroScreenSubtitle,
                    fontSize = 12.sp,
                    fontFamily = SpaceGroteskFont,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
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

        // Cycle Command Panel ("Where are we" in under 5 seconds)
        item {
            CycleCommandPanel(
                commandState = cycleCommandState,
                isGreek = isGreek,
                palette = palette
            )
        }

        // US Spot BTC ETF Net Flows Card (Real-Time & Verified Institutional Public Records)
        item {
            BitcoinEtfFlowsCard(
                etfFlowData = etfFlowData,
                isGreek = isGreek
            )
        }

        // Global Stablecoin Supply & 7D Flow (DefiLlama Real-Time Liquidity Feed)
        item {
            DefiLlamaStablecoinsCard(
                liquidityData = stablecoinLiquidityData,
                isGreek = isGreek
            )
        }

        // Live Forward Signal Audit Trail Card (Proven Track Record Moat)
        item {
            ForwardAuditTrailCard(
                auditLogs = forwardAuditLogs,
                isGreek = isGreek
            )
        }

        // Weekly Macro Briefing Card (Current Zone + One Action Word + 3-Line Why, Gated with Lock Overlay for Free tier)
        item {
            WeeklyMacroBriefingCard(
                isProUnlocked = isProUnlocked,
                onOpenProModal = onOpenProModal,
                isGreek = isGreek,
                strings = strings,
                palette = palette,
                cycle = cycleCommandState
            )
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

        // 3 Scenario Analysis Cards (Neutral Educational Market Conditions)
        item {
            MacroActionableStrategyCards(
                isGreek = isGreek,
                btcPrice = btcPriceUsd,
                currency = currency,
                cycle = cycleCommandState,
                sma200d = liveMovingAverages.sma200d
            )
        }

        // 2. Horizontal Filter Segmented Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroSectionFilter.values().forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) palette.primary
                                else if (palette.isLight) Color(0xFFF1F5F9)
                                else CosmicVoidSurface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) palette.primary else if (palette.isLight) palette.border else CosmicBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter.localizedTitle(isGreek),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) (if (palette.isLight) Color.White else CosmicVoidBg) else palette.textPrimary
                        )
                    }
                }
            }
        }

        // Pro Gatekeeper Banner (if Free user)
        if (!isProUnlocked) {
            item {
                MacroProGatekeeper(
                    onOpenProModal = onOpenProModal
                )
            }
        }

        // Institutional Whale Portfolio Risk & Exit Strategy Simulator
        item {
            WhalePortfolioRiskSimulatorCard(
                isProUnlocked = isProUnlocked,
                onOpenProModal = onOpenProModal
            )
        }

        // 1. Cycle Days & Historical Bear Market Analog
        if (selectedFilter == MacroSectionFilter.ALL || selectedFilter == MacroSectionFilter.CYCLE_DAYS_ANALOG) {
            item {
                CollapsibleCardContainer(
                    title = if (isGreek) "⏳ Ημέρες Κύκλου & Ιστορικό Ανάλογο" else "⏳ Cycle Days & Bear Market Analog",
                    subtitle = if (isGreek) "Σύγκριση ημερών από το ATH, ιστορικός πυθμένας 1 έτους & γεγονότα κραχ" else "Days post-ATH vs past cycles, 1-year bottom window & crash dossier",
                    badge = "CYCLE CLOCK",
                    badgeColor = NeonCyan,
                    isExpanded = cycleDaysExpanded,
                    onToggle = { cycleDaysExpanded = !cycleDaysExpanded }
                ) {
                    CycleDayComparisonCard(
                        currentBtcPrice = btcPriceUsd,
                        onOpenAiAnalysis = { prompt -> onOpenAiAssistant(prompt) }
                    )
                }
            }
        }

        // 2b. Global Risk Backdrop: DXY & US 10Y Yield
        if (selectedFilter == MacroSectionFilter.ALL || selectedFilter == MacroSectionFilter.RISK_BACKDROP) {
            item {
                CollapsibleCardContainer(
                    title = if (isGreek) "🌐 DXY, 10Y Yields" else "🌐 DXY, 10Y Yields",
                    subtitle = if (isGreek) "Παγκόσμια Ρευστότητα & Αποδόσεις Ομολόγων" else "Global USD Liquidity & Sovereign Yields",
                    badge = "MACRO BACKDROP",
                    badgeColor = NeonPurple,
                    isExpanded = riskBackdropExpanded,
                    onToggle = { riskBackdropExpanded = !riskBackdropExpanded }
                ) {
                    GlobalRiskBackdropCard(
                        isProUnlocked = isProUnlocked,
                        onOpenProModal = onOpenProModal,
                        isGreek = isGreek,
                        risk = globalRiskSnapshot
                    )
                }
            }
        }

        // 3. Bitcoin Rainbow Chart (Dynamic Power Law Model)
        if (selectedFilter == MacroSectionFilter.ALL || selectedFilter == MacroSectionFilter.RAINBOW) {
            item {
                CollapsibleCardContainer(
                    title = if (isGreek) "🌈 Bitcoin Rainbow Price Model" else "🌈 Bitcoin Rainbow Price Model",
                    subtitle = if (isGreek) "Ζώνες Αποτίμησης & Power Law" else "Logarithmic Valuation Bands",
                    badge = "HISTORY",
                    badgeColor = NeonCyan,
                    isExpanded = rainbowExpanded,
                    onToggle = { rainbowExpanded = !rainbowExpanded }
                ) {
                    BitcoinRainbowChart(
                        btcPriceUsd = btcPriceUsd,
                        currency = currency,
                        isProUnlocked = isProUnlocked,
                        onOpenProModal = onOpenProModal,
                        btcSparkline = btcCoin?.sparkline ?: emptyList()
                    )
                }
            }
        }

        // 4. Altcoin Season Index (0-100)
        if (selectedFilter == MacroSectionFilter.ALL || selectedFilter == MacroSectionFilter.ALT_SEASON) {
            item {
                CollapsibleCardContainer(
                    title = if (isGreek) "⚡ Altcoin Season Index" else "⚡ Altcoin Season Index",
                    subtitle = if (isGreek) "${altSeasonData.score}/100 · ${altSeasonData.zoneNameGreek}" else "${altSeasonData.score}/100 · ${altSeasonData.zoneNameEnglish}",
                    badge = "${altSeasonData.score}/100",
                    badgeColor = activeZoneColor,
                    isExpanded = altSeasonExpanded,
                    onToggle = { altSeasonExpanded = !altSeasonExpanded }
                ) {
                    AltcoinSeasonIndexCard(
                        altData = altSeasonData,
                        coins = coins,
                        isProUnlocked = isProUnlocked,
                        onOpenProModal = onOpenProModal
                    )
                }
            }
        }

        // 5. Fear & Greed Index
        if (selectedFilter == MacroSectionFilter.ALL || selectedFilter == MacroSectionFilter.FEAR_GREED) {
            item {
                CollapsibleCardContainer(
                    title = if (isGreek) "😱 Fear & Greed Index" else "😱 Fear & Greed Index",
                    subtitle = if (isGreek) "${fearGreedData.score}/100 · ${fearGreedData.sentimentEl} (${fearGreedData.sentiment})" else "${fearGreedData.score}/100 · ${fearGreedData.sentiment} Sentiment",
                    badge = "${fearGreedData.score}/100",
                    badgeColor = GainGreen,
                    isExpanded = fearGreedExpanded,
                    onToggle = { fearGreedExpanded = !fearGreedExpanded }
                ) {
                    FearAndGreedIndexCard(
                        fearGreedData = fearGreedData
                    )
                }
            }
        }

        // 6. Pi Cycle Top Indicator & 200W MA Floor
        if (selectedFilter == MacroSectionFilter.ALL || selectedFilter == MacroSectionFilter.PI_CYCLE) {
            item {
                PiCycleTopCard(
                    piData = piCycleData,
                    currency = currency,
                    isProUnlocked = isProUnlocked,
                    onOpenProModal = onOpenProModal
                )
            }
        }

        // Mandatory Financial & Regulatory Disclaimer
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (palette.isLight) Color(0xFFFFFBEB) else CosmicVoidSurface)
                    .border(1.dp, if (palette.isLight) Color(0xFFFDE68A) else CosmicBorder, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Disclaimer",
                            tint = NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.financialDisclaimerTitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
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

        // Bottom Safe Spacing
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * 3 Scenario Analysis Cards: Base Case (Accumulation Zone), Downside Scenario, Upside Scenario
 */
@Composable
private fun MacroActionableStrategyCards(
    isGreek: Boolean,
    btcPrice: Double,
    currency: Currency,
    cycle: CycleCommandState,
    sma200d: Double?
) {
    val palette = LocalAppColors.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isGreek) "ΣΕΝΑΡΙΑ ΚΥΚΛΟΥ & ΣΥΝΘΗΚΕΣ ΑΓΟΡΑΣ" else "CYCLE SCENARIOS & MARKET CONDITIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = palette.primary
            )
            Text(
                text = if (isGreek) "Φάση: ${cycle.regime.titleEl}" else "Phase: ${cycle.regime.titleEn}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GainGreen
            )
        }

        // Card 1: 🟢 BASE CASE: Accumulation Zone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (palette.isLight) Color(0xFFF0FDF4) else CosmicVoidSurface)
                .border(1.dp, TachyonMint.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(TachyonMint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🟢", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isGreek) "ΒΑΣΙΚΟ ΣΕΝΑΡΙΟ: Ζώνη Συσσώρευσης" else "BASE CASE: Accumulation Zone",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TachyonMint
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TachyonMint.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isGreek) "ΧΑΜΗΛΟ ΡΙΣΚΟ" else "LOW RISK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TachyonMint
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isGreek)
                            "Το μοντέλο Rainbow και ο 200W MA καταδεικνύουν ιστορική δομική στήριξη. Ιστορικά δεδομένα υποδεικνύουν σταθερή συσσώρευση σε BTC, ETH και κορυφαία L1s."
                        else
                            "Rainbow valuation model and 200W MA confirm structural baseline. Historical data reflects steady accumulation behavior across BTC, ETH & Top L1s.",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = palette.textSecondary
                    )
                }
            }
        }

        // Card 2: 🟡 DOWNSIDE SCENARIO: Support Tests
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (palette.isLight) Color(0xFFFFFBEB) else CosmicVoidSurface)
                .border(1.dp, NeonAmber.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(NeonAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🟡", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isGreek) "ΠΤΩΤΙΚΟ ΣΕΝΑΡΙΟ: Δοκιμή Στηρίξεων" else "DOWNSIDE SCENARIO: Support Tests",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                        Text(
                            text = "SUPPORT ZONE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = run {
                            val maText = if (sma200d != null && sma200d > 0.0) {
                                com.example.util.AppNumberFormatter.formatPrice(sma200d, currency)
                            } else "—"
                            if (isGreek)
                                "Οι διορθώσεις συχνά δοκιμάζουν τον live 200D MA ($maText) ή εμφανίζονται όταν το Fear & Greed πέφτει κάτω από 45."
                            else
                                "Pullbacks often retest the live 200D MA ($maText) or appear when Fear & Greed drops below 45."
                        },
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = palette.textSecondary
                    )
                }
            }
        }

        // Card 3: 🔴 UPSIDE SCENARIO: Distribution Levels
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (palette.isLight) Color(0xFFFEF2F2) else CosmicVoidSurface)
                .border(1.dp, SoftCrimson.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(SoftCrimson.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔴", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isGreek) "ΑΝΟΔΙΚΟ ΣΕΝΑΡΙΟ: Ζώνες Διανομής" else "UPSIDE SCENARIO: Distribution Levels",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftCrimson
                        )
                        Text(
                            text = "DISTRIBUTION ZONE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftCrimson
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isGreek)
                            "Ιστορικά σημεία υπερθέρμανσης σηματοδοτούνται από το Pi Cycle Top Cross ή όταν ο δείκτης Alt Season υπερβεί το 80 (Ζώνη Ευφορίας)."
                        else
                            "Macro cycle peak patterns historically align with Pi Cycle Top cross confirmation or Altcoin Season Index crossing above 80 (Euphoria zone).",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = palette.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Collapsible Card Wrapper with clean summary and expand/collapse animation
 */
@Composable
private fun CollapsibleCardContainer(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val palette = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(palette.surfaceElevated)
            .border(1.dp, palette.border, RoundedCornerShape(20.dp))
    ) {
        // Collapsible Header Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.18f))
                            .border(0.5.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            color = badgeColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = palette.textSecondary
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = palette.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        // Expandable Content Body
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun WeeklyMacroBriefingCard(
    isProUnlocked: Boolean,
    onOpenProModal: () -> Unit,
    isGreek: Boolean,
    strings: com.example.util.AppStrings,
    palette: AppThemePalette,
    cycle: CycleCommandState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceElevated)
            .border(
                1.dp,
                if (isProUnlocked) palette.primary.copy(alpha = 0.4f) else NeonAmber.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row: Badge + Title + Pro Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isProUnlocked) GainGreen.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = if (isProUnlocked) GainGreen else NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.weeklyBriefingTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = palette.textPrimary
                        )
                        Text(
                            text = strings.weeklyBriefingSub,
                            fontSize = 10.5.sp,
                            color = palette.textMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isProUnlocked) GainGreen.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isProUnlocked) strings.macroRainbowBadge else strings.macroProLockedBadge,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isProUnlocked) GainGreen else NeonAmber
                    )
                }
            }

            // Current Zone & One Action Word Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Zone pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.background)
                        .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = strings.weeklyBriefingCurrentZone,
                            fontSize = 10.sp,
                            color = palette.textMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(GainGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isGreek) cycle.regime.titleEl else cycle.regime.titleEn,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GainGreen
                            )
                        }
                    }
                }

                // Recommended Posture (One Action Word: Accumulate / Hold / Reduce)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GainGreen.copy(alpha = 0.12f))
                        .border(1.dp, GainGreen.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = strings.weeklyBriefingActionTitle,
                            fontSize = 10.sp,
                            color = palette.textMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isGreek) cycle.regime.actionEl else cycle.regime.actionEn,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = GainGreen
                        )
                    }
                }
            }

            // 3-Line Synthesis "Why" Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.background)
                    .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = strings.weeklyBriefingReasonTitle,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = palette.primary
                )
                Text(
                    text = if (isGreek) cycle.keyStanceSummaryEl else cycle.keyStanceSummaryEn,
                    fontSize = 11.5.sp,
                    color = palette.textSecondary,
                    lineHeight = 16.sp
                )
                Text(
                    text = if (cycle.halvingDaysElapsed > 0) {
                        if (isGreek) "Ημέρα ${cycle.halvingDaysElapsed} μετά το 4ο Halving. Rainbow: ${cycle.rainbowBandName}."
                        else "Day ${cycle.halvingDaysElapsed} after the 4th Halving. Rainbow: ${cycle.rainbowBandName}."
                    } else if (isGreek) "Αναμονή ζωντανού ρολογιού κύκλου." else "Waiting for live cycle clock.",
                    fontSize = 11.5.sp,
                    color = palette.textSecondary,
                    lineHeight = 16.sp
                )
                Text(
                    text = buildString {
                        if (cycle.fundingIsLive) append(if (isGreek) "Funding ${"%.3f".format(cycle.fundingRatePercent)}%. " else "Funding ${"%.3f".format(cycle.fundingRatePercent)}%. ")
                        if (cycle.etfFlowIsLive) append(if (isGreek) "ETF 5D ${"%.1f".format(cycle.etf5dNetFlowMillionUsd)}M. " else "ETF 5D ${"%.1f".format(cycle.etf5dNetFlowMillionUsd)}M. ")
                        if (cycle.fearAndGreedIndex >= 0) append("Fear & Greed ${cycle.fearAndGreedIndex}.")
                        if (isEmpty()) append(if (isGreek) "Χωρίς επιπλέον live επιβεβαίωση." else "No extra live confirmation yet.")
                    },
                    fontSize = 11.5.sp,
                    color = palette.textSecondary,
                    lineHeight = 16.sp
                )
            }

            // Pro Locked Overlay (Non-intrusive preview + upgrade action)
            if (!isProUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonAmber.copy(alpha = 0.10f))
                        .border(1.dp, NeonAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onOpenProModal() }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = NeonAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.weeklyBriefingProUnlockText,
                                fontSize = 11.sp,
                                color = NeonAmber,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonAmber)
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = strings.upgradeToPro,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF05050F)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Global Risk Backdrop: FedWatch, DXY Dollar Index, US 10-Year Treasury Yield, Brent Crude
 * Pro-gated live values with 3-line objective micro-copy
 */
@Composable
fun GlobalRiskBackdropCard(
    isProUnlocked: Boolean,
    onOpenProModal: () -> Unit,
    isGreek: Boolean,
    risk: com.example.data.model.GlobalRiskSnapshot
) {
    val palette = LocalAppColors.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 2. DXY (US Dollar Index)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (palette.isLight) Color(0xFFF8FAFC) else CosmicVoidSurface)
                .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGreek) "DXY: Δείκτης Δολαρίου ΗΠΑ" else "DXY: US Dollar Index",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = if (risk.dxy != null) {
                            val chg = risk.dxyChangePct ?: 0.0
                            String.format(java.util.Locale.US, "%.2f (%s%.2f%%)", risk.dxy, if (chg >= 0) "+" else "", chg)
                        } else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                }

                MetricExplainerBox(
                    whatItIs = "Geometrically weighted index measuring USD strength against 6 major fiat currencies.",
                    whatItShows = "Global dollar liquidity conditions, capital flight, and relative dollar purchasing power.",
                    whatItDoesNotMean = "Macro liquidity tide, not an isolated determinant of crypto market bottoms or tops."
                )
            }
        }

        // 3. US 10-Year Treasury Yield (US10Y)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (palette.isLight) Color(0xFFF8FAFC) else CosmicVoidSurface)
                .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGreek) "US 10Y: Απόδοση 10ετούς Ομολόγου ΗΠΑ" else "US 10Y: 10-Year Treasury Yield",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = if (risk.us10y != null) {
                            val bps = risk.us10yChangeBps ?: 0.0
                            String.format(java.util.Locale.US, "%.2f%% (%s%.1f bps)", risk.us10y, if (bps >= 0) "+" else "", bps)
                        } else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primary
                    )
                }

                MetricExplainerBox(
                    whatItIs = "Annual interest yield on 10-year United States government debt securities.",
                    whatItShows = "Risk-free global cost of capital and long-term economic growth/inflation expectations.",
                    whatItDoesNotMean = "Macro discount hurdle rate for risk assets, not a short-term execution trigger."
                )
            }
        }

        // 4. Brent Crude Oil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (palette.isLight) Color(0xFFF8FAFC) else CosmicVoidSurface)
                .border(1.dp, if (palette.isLight) palette.border else CosmicBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGreek) "Brent Crude: Πετρέλαιο Brent" else "Brent Crude Oil",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                    Text(
                        text = if (risk.brent != null) {
                            val chg = risk.brentChangePct ?: 0.0
                            String.format(java.util.Locale.US, "$%.2f/bbl (%s%.2f%%)", risk.brent, if (chg >= 0) "+" else "", chg)
                        } else "—",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary
                    )
                }

                MetricExplainerBox(
                    whatItIs = "International pricing benchmark for physical crude oil contracts.",
                    whatItShows = "Headline commodity energy inflation pressures and global industrial activity.",
                    whatItDoesNotMean = "Upstream component of inflation indices, not a direct crypto price driver."
                )
            }
        }
    }
}

