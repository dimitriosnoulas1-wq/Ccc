package com.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CoinDetailSheet
import com.example.ui.screens.FuturesTerminalScreen
import com.example.ui.screens.LearnBlockchainScreen
import com.example.ui.screens.MacroScreen
import com.example.ui.screens.MarketsScreen
import com.example.ui.screens.PrivacyPolicySheet
import com.example.ui.screens.ProUpgradeModal
import com.example.ui.screens.QuantumLaunchSequenceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.components.AlertHistorySheet
import com.example.ui.screens.SignalsScreen
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicDarkBg
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.LocalAppStrings
import com.example.util.getAppStrings
import com.example.viewmodel.CryptoViewModel
import com.example.viewmodel.MainTab

import com.example.ui.theme.cosmicBackground
import com.example.ui.theme.ChronoNavBg
import com.example.ui.theme.QuantumCyan
import com.example.ui.theme.stitchHorizonBrush

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.example.util.AppSoundManager.initialize(applicationContext)
        } catch (_: Throwable) {
            // Prevent audio crash on launch (emulators without audio)
        }
        enableEdgeToEdge()
        try {
            setContent {
                CryptoCyclesApp()
            }
        } catch (t: Throwable) {
            setContent {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF05050F))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CryptoCycles failed to start\n${t.message ?: t.javaClass.simpleName}",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CryptoCyclesApp(
    viewModel: CryptoViewModel = viewModel(
        factory = CryptoViewModel.provideFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val coins by viewModel.filteredCoins.collectAsState()
    val allCoins by viewModel.allCoins.collectAsState()
    val featuredHeroCoin by viewModel.featuredHeroCoin.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCoin by viewModel.selectedCoin.collectAsState()
    val selectedSignalCoin by viewModel.selectedSignalCoin.collectAsState()
    val showProModal by viewModel.showProModal.collectAsState()
    val isProUnlocked by viewModel.isProUnlocked.collectAsState()
    val monthlyPrice by viewModel.monthlyPrice.collectAsState()
    val yearlyPrice by viewModel.yearlyPrice.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val macroSignal by viewModel.macroSignal.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLiveConnected by viewModel.isLiveConnected.collectAsState()
    val isCacheStale by viewModel.isCacheStale.collectAsState()
    val priceSource by viewModel.priceSource.collectAsState()
    val lastUpdatedTimestamp by viewModel.lastUpdatedTimestamp.collectAsState()
    val whaleAlerts by viewModel.whaleAlerts.collectAsState()
    val whaleSettings by viewModel.whaleSettings.collectAsState()
    val whaleLeveragePositions by viewModel.whaleLeveragePositions.collectAsState()
    val whaleLeverageSummary by viewModel.whaleLeverageSummary.collectAsState()

    // Futures Terminal States used outside the Futures tab (slow streams only)
    val futuresTickerData by viewModel.futuresTickerData.collectAsState()
    val futuresMacroSentiment by viewModel.futuresMacroSentiment.collectAsState()
    val centralizedPriceState by viewModel.centralizedPriceState.collectAsState()

    // AI Analyst State
    val aiMessages by viewModel.aiMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val showAiAssistantModal by viewModel.showAiAssistantModal.collectAsState()
    val aiQueriesRemaining by viewModel.aiQueriesRemaining.collectAsState()
    val isAiLimitReached by viewModel.isAiLimitReached.collectAsState()

    // Alert History States
    val unreadAlertsCount by viewModel.unreadAlertsCount.collectAsState()
    val showAlertHistorySheet by viewModel.showAlertHistorySheet.collectAsState()
    val alertHistory by viewModel.alertHistory.collectAsState()

    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showLaunchSequence by remember { mutableStateOf(true) }

    val appStrings = remember(selectedLanguage) {
        com.example.util.AppNumberFormatter.currentLanguage = selectedLanguage
        getAppStrings(selectedLanguage)
    }

    MyApplicationTheme(theme = selectedTheme) {
        CompositionLocalProvider(LocalAppStrings provides appStrings) {
            val palette = com.example.ui.theme.LocalAppColors.current
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as? Activity)?.window
                    if (window != null) {
                        val insetsController = WindowCompat.getInsetsController(window, view)
                        insetsController.isAppearanceLightStatusBars = palette.isLight
                        insetsController.isAppearanceLightNavigationBars = palette.isLight
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .cosmicBackground(palette.isLight)
            ) {
                // Main Scaffold - Single root bottom navigation bar & floating action button
                Scaffold(
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    bottomBar = {
                        CustomBottomNavBar(
                            selectedTab = selectedTab,
                            onSelectTab = {
                                com.example.util.AppSoundManager.playTechClick()
                                viewModel.setTab(it)
                            },
                            modifier = Modifier.navigationBarsPadding()
                        )
                    },
                    floatingActionButton = {
                        // Floating AI Market Analyst Button (Bottom Right)
                        if (selectedTab != MainTab.SETTINGS) {
                            com.example.ui.components.AiMarketFloatingButton(
                                onClick = { viewModel.openAiAssistant() }
                            )
                        }
                    },
                    floatingActionButtonPosition = androidx.compose.material3.FabPosition.End
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        when (selectedTab) {
                            MainTab.MARKETS -> MarketsScreen(
                                coins = coins,
                                featuredHeroCoin = featuredHeroCoin,
                                searchQuery = searchQuery,
                                selectedCategory = selectedCategory,
                                currency = selectedCurrency,
                                selectedTheme = selectedTheme,
                                isProUnlocked = isProUnlocked,
                                isRefreshing = isRefreshing,
                                isConnected = isLiveConnected,
                                isCacheStale = isCacheStale,
                                priceSource = priceSource,
                                lastUpdatedTimestamp = lastUpdatedTimestamp,
                                tickerData = futuresTickerData,
                                fearAndGreedScore = futuresMacroSentiment.fearAndGreedValue ?: 57,
                                onSearchChanged = { viewModel.setSearchQuery(it) },
                                onCategoryChanged = {
                                    com.example.util.AppSoundManager.playTechClick()
                                    viewModel.setCategory(it)
                                },
                                onCoinClicked = {
                                    com.example.util.AppSoundManager.playSelectionPop()
                                    viewModel.selectCoin(it)
                                },
                                onFavoriteToggle = {
                                    com.example.util.AppSoundManager.playSuccessChime()
                                    viewModel.toggleFavorite(it)
                                },
                                onOpenProModal = { viewModel.openProModal() },
                                onOpenAiAssistant = { prompt -> viewModel.openAiAssistant(prompt) },
                                onAlertHistoryClick = { viewModel.openAlertHistory() },
                                unreadAlertsCount = unreadAlertsCount,
                                onNavigateToTab = { targetTab ->
                                    com.example.util.AppSoundManager.playTechClick()
                                    viewModel.setTab(targetTab)
                                },
                                onRefresh = { viewModel.manualRefresh() }
                            )

                            MainTab.FUTURES -> FuturesTerminalRoute(
                                viewModel = viewModel,
                                coins = allCoins,
                                selectedCoin = selectedCoin,
                                tickerData = futuresTickerData,
                                macroSentiment = futuresMacroSentiment,
                                currency = selectedCurrency,
                                isProUnlocked = isProUnlocked,
                                isRefreshing = isRefreshing
                            )

                            MainTab.MACRO -> MacroScreen(
                                coins = allCoins,
                                currency = selectedCurrency,
                                isProUnlocked = isProUnlocked,
                                centralizedBtcPrice = if (centralizedPriceState.btcPerpPrice > 0.0) centralizedPriceState.btcPerpPrice else centralizedPriceState.btcSpotPrice,
                                fearAndGreedScore = futuresMacroSentiment.fearAndGreedValue ?: 55,
                                fearAndGreedClassification = futuresMacroSentiment.fearAndGreedClassification ?: "Greed",
                                macroSentiment = futuresMacroSentiment,
                                isRefreshing = isRefreshing,
                                isConnected = isLiveConnected,
                                priceSource = priceSource,
                                lastUpdatedTimestamp = lastUpdatedTimestamp,
                                onOpenProModal = { viewModel.openProModal() },
                                onOpenAiAssistant = { prompt -> viewModel.openAiAssistant(prompt) },
                                onRefresh = { viewModel.manualRefresh() }
                            )

                            MainTab.SIGNALS -> SignalsScreen(
                                signal = macroSignal,
                                coins = allCoins,
                                selectedCoin = selectedSignalCoin,
                                onSelectCoin = { viewModel.selectSignalCoin(it.id) },
                                currency = selectedCurrency,
                                isProUnlocked = isProUnlocked,
                                isRefreshing = isRefreshing,
                                isConnected = isLiveConnected,
                                priceSource = priceSource,
                                lastUpdatedTimestamp = lastUpdatedTimestamp,
                                onOpenProModal = { viewModel.openProModal() },
                                whaleAlerts = whaleAlerts,
                                whaleLeveragePositions = whaleLeveragePositions,
                                whaleLeverageSummary = whaleLeverageSummary,
                                onOpenAiAssistant = { prompt -> viewModel.openAiAssistant(prompt) },
                                onRefresh = { viewModel.manualRefresh() },
                                viewModel = viewModel
                            )

                            MainTab.LEARN -> LearnBlockchainScreen(
                                currentLanguage = selectedLanguage,
                                isProUnlocked = isProUnlocked,
                                onOpenProModal = { viewModel.openProModal() }
                            )

                            MainTab.SETTINGS -> SettingsScreen(
                                currency = selectedCurrency,
                                selectedLanguage = selectedLanguage,
                                isProUnlocked = isProUnlocked,
                                monthlyPrice = monthlyPrice,
                                yearlyPrice = yearlyPrice,
                                whaleSettings = whaleSettings,
                                btcPrice = centralizedPriceState.btcSpotPrice,
                                onCurrencyChanged = { viewModel.setCurrency(it) },
                                onLanguageChanged = { viewModel.setLanguage(it) },
                                onOpenProModal = { viewModel.openProModal() },
                                onRestorePurchases = { onDone -> viewModel.restorePurchases(onDone) },
                                onOpenPrivacyPolicy = { showPrivacyPolicy = true },
                                onWhaleNotificationsChanged = { viewModel.setWhaleNotificationsEnabled(it) },
                                onWhaleThresholdChanged = { viewModel.setWhaleMinThreshold(it) },
                                onNotifyZoneChangeChanged = { viewModel.setNotifyZoneChange(it) },
                                onNotifyPiCycleChanged = { viewModel.setNotifyPiCycle(it) },
                                onNotifyRainbowBandChanged = { viewModel.setNotifyRainbowBand(it) },
                                onNotify200wSmaChanged = { viewModel.setNotify200wSma(it) },
                                logCharts = viewModel.logCharts.collectAsState().value,
                                onLogChartsChanged = { viewModel.setLogCharts(it) },
                                onTogglePro = { viewModel.setProUnlocked(it) }
                            )
                        }
                    }
                }

                // Privacy Policy Sheet
                if (showPrivacyPolicy) {
                    PrivacyPolicySheet(
                        onDismiss = { showPrivacyPolicy = false }
                    )
                }

                // Coin Detail Modal Sheet
                if (selectedCoin != null) {
                    CoinDetailSheet(
                        coin = selectedCoin,
                        currency = selectedCurrency,
                        selectedLanguage = selectedLanguage,
                        isProUnlocked = isProUnlocked,
                        onDismiss = { viewModel.selectCoin(null) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onOpenProModal = { viewModel.openProModal() },
                        onOpenAiAssistant = { query -> viewModel.openAiAssistant(query) }
                    )
                }

                // Pro Upgrade Modal Sheet
                if (showProModal) {
                    val context = LocalContext.current
                    val activity = context as? android.app.Activity
                    ProUpgradeModal(
                        isProUnlocked = isProUnlocked,
                        monthlyPrice = monthlyPrice,
                        yearlyPrice = yearlyPrice,
                        onDismiss = { viewModel.closeProModal() },
                        onPurchaseMonthly = {
                            activity?.let {
                                viewModel.launchPurchase(it, com.example.billing.BillingManager.PRODUCT_PRO_MONTHLY)
                            }
                        },
                        onPurchaseYearly = {
                            activity?.let {
                                viewModel.launchPurchase(it, com.example.billing.BillingManager.PRODUCT_PRO_YEARLY)
                            }
                        },
                        onRestorePurchases = { onDone ->
                            viewModel.restorePurchases(onDone)
                        }
                    )
                }

                // Alert History Sheet
                if (showAlertHistorySheet) {
                    AlertHistorySheet(
                        alerts = alertHistory,
                        isGreek = selectedLanguage == com.example.data.model.AppLanguage.GREEK,
                        onDismiss = { viewModel.closeAlertHistory() },
                        onNavigateToTab = { tabStr ->
                            viewModel.closeAlertHistory()
                            when (tabStr.uppercase()) {
                                "MARKETS" -> viewModel.setTab(MainTab.MARKETS)
                                "FUTURES" -> viewModel.setTab(MainTab.FUTURES)
                                "SIGNALS" -> viewModel.setTab(MainTab.SIGNALS)
                                else -> viewModel.setTab(MainTab.MARKETS)
                            }
                        }
                    )
                }

                // AI Market Analyst Dialog
                if (showAiAssistantModal) {
                    com.example.ui.components.AiMarketAnalystDialog(
                        messages = aiMessages,
                        isLoading = isAiLoading,
                        onSendMessage = { query -> viewModel.sendAiUserMessage(query) },
                        onClearChat = { viewModel.clearAiChat() },
                        onDismiss = { viewModel.closeAiAssistant() },
                        aiQueriesRemaining = aiQueriesRemaining,
                        isAiLimitReached = isAiLimitReached,
                        isProUnlocked = isProUnlocked,
                        onOpenProModal = { viewModel.openProModal() }
                    )
                }

                // 2126 Quantum Launch Sequence (Splash / Boot Screen)
                AnimatedVisibility(
                    visible = showLaunchSequence,
                    enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
                    exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
                ) {
                    QuantumLaunchSequenceScreen(
                        onLaunchComplete = { showLaunchSequence = false }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavBar(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = com.example.ui.theme.LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ChronoNavBg.copy(alpha = 0.95f))
            .border(1.dp, stitchHorizonBrush(startAlpha = 0.40f, endAlpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                label = strings.navMarkets,
                activeIcon = Icons.Filled.GridView,
                inactiveIcon = Icons.Outlined.GridView,
                isSelected = selectedTab == MainTab.MARKETS,
                onClick = { onSelectTab(MainTab.MARKETS) },
                testTag = "nav_markets",
                modifier = Modifier.weight(1f)
            )

            BottomNavItem(
                label = strings.navFutures,
                activeIcon = Icons.Filled.QueryStats,
                inactiveIcon = Icons.Outlined.QueryStats,
                isSelected = selectedTab == MainTab.FUTURES,
                onClick = { onSelectTab(MainTab.FUTURES) },
                testTag = "nav_futures",
                modifier = Modifier.weight(1f)
            )

            BottomNavItem(
                label = strings.navMacro,
                activeIcon = Icons.Filled.Insights,
                inactiveIcon = Icons.Outlined.Insights,
                isSelected = selectedTab == MainTab.MACRO,
                onClick = { onSelectTab(MainTab.MACRO) },
                testTag = "nav_macro",
                modifier = Modifier.weight(1f)
            )

            BottomNavItem(
                label = strings.navSignals,
                activeIcon = Icons.Filled.Sensors,
                inactiveIcon = Icons.Outlined.Sensors,
                isSelected = selectedTab == MainTab.SIGNALS,
                onClick = { onSelectTab(MainTab.SIGNALS) },
                testTag = "nav_signals",
                modifier = Modifier.weight(1f)
            )

            BottomNavItem(
                label = strings.navLearn,
                activeIcon = Icons.Filled.School,
                inactiveIcon = Icons.Outlined.School,
                isSelected = selectedTab == MainTab.LEARN,
                onClick = { onSelectTab(MainTab.LEARN) },
                testTag = "nav_learn",
                modifier = Modifier.weight(1f)
            )

            BottomNavItem(
                label = strings.navSettings,
                activeIcon = Icons.Filled.Settings,
                inactiveIcon = Icons.Outlined.Settings,
                isSelected = selectedTab == MainTab.SETTINGS,
                onClick = { onSelectTab(MainTab.SETTINGS) },
                testTag = "nav_settings",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FuturesTerminalRoute(
    viewModel: CryptoViewModel,
    coins: List<com.example.data.model.CryptoCoin>,
    selectedCoin: com.example.data.model.CryptoCoin?,
    tickerData: com.example.data.model.FuturesTickerData?,
    macroSentiment: com.example.data.model.MacroMarketSentiment,
    currency: com.example.data.model.Currency,
    isProUnlocked: Boolean,
    isRefreshing: Boolean
) {
    val activeFuturesSymbol by viewModel.activeFuturesSymbol.collectAsState()
    val bookTicker by viewModel.futuresBookTicker.collectAsState()
    val markFunding by viewModel.futuresMarkFunding.collectAsState()
    val openInterest by viewModel.futuresOpenInterest.collectAsState()
    val recentTrades by viewModel.futuresRecentTrades.collectAsState()
    val recentLiquidations by viewModel.futuresRecentLiquidations.collectAsState()
    val connectionStatus by viewModel.futuresConnectionStatus.collectAsState()
    val marketIntelligenceReport by viewModel.futuresMarketIntelligenceReport.collectAsState()

    FuturesTerminalScreen(
        coins = coins,
        selectedCoin = selectedCoin,
        activeFuturesSymbol = activeFuturesSymbol,
        tickerData = tickerData,
        bookTicker = bookTicker,
        markFunding = markFunding,
        openInterest = openInterest,
        recentTrades = recentTrades,
        recentLiquidations = recentLiquidations,
        connectionStatus = connectionStatus,
        macroSentiment = macroSentiment,
        marketIntelligenceReport = marketIntelligenceReport,
        currency = currency,
        isProUnlocked = isProUnlocked,
        isRefreshing = isRefreshing,
        onSelectSymbol = { viewModel.selectFuturesSymbol(it) },
        onOpenProModal = { viewModel.openProModal() },
        onOpenAiAssistant = { prompt -> viewModel.openAiAssistant(prompt) },
        onRefresh = { viewModel.manualRefresh() }
    )
}

@Composable
fun BottomNavItem(
    label: String,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val palette = com.example.ui.theme.LocalAppColors.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) QuantumCyan.copy(alpha = 0.12f) else Color.Transparent)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(QuantumCyan)
            )
            Spacer(modifier = Modifier.height(3.dp))
        } else {
            Spacer(modifier = Modifier.height(5.5.dp))
        }
        Icon(
            imageVector = if (isSelected) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = if (isSelected) QuantumCyan else Color(0xFF64748B),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) QuantumCyan else Color(0xFF64748B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
