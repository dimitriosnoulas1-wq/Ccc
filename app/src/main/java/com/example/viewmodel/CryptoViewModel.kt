package com.example.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.billing.BillingManager
import com.example.data.engine.CycleCommandEngine
import com.example.data.model.AppLanguage
import com.example.data.model.BitcoinEtfFlowData
import com.example.data.model.CoinCategory
import com.example.data.model.CryptoCoin
import com.example.data.model.AlertCategory
import com.example.data.model.AlertHistoryItem
import com.example.data.model.Currency
import com.example.data.model.CycleCommandState
import com.example.data.model.ForwardSignalAuditEntry
import com.example.data.model.FuturesBookTicker
import com.example.data.model.FuturesConnectionStatus
import com.example.data.model.FuturesLiquidationOrder
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.FuturesOpenInterest
import com.example.data.model.FuturesTickerData
import com.example.data.model.FuturesTrade
import com.example.data.model.MacroCycleSignal
import com.example.data.model.MacroMarketSentiment
import com.example.data.model.StablecoinLiquidityData
import com.example.data.model.WhaleAlert
import com.example.data.model.WhaleAlertSettings
import com.example.data.model.WhaleLeveragePosition
import com.example.data.model.WhaleLeverageSummary
import com.example.data.repository.BitcoinEtfRepository
import com.example.data.repository.CryptoRepository
import com.example.data.repository.DefiLlamaLiquidityRepository
import com.example.data.repository.ForwardAuditTrailRepository
import com.example.data.repository.FuturesTerminalRepository
import com.example.data.repository.WhaleAlertRepository
import com.example.data.repository.WhaleLeverageRepository
import com.example.engine.forecasting.QuantForecastEngine
import com.example.engine.forecasting.ForecastCardModel
import com.example.service.CryptoMarketWatchWorker
import com.example.util.AlertHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

enum class MainTab {
    MARKETS,
    FUTURES,
    MACRO,
    SIGNALS,
    LEARN,
    SETTINGS
}

class CryptoViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: CryptoRepository = CryptoRepository(application.applicationContext),
    private val whaleRepository: WhaleAlertRepository = WhaleAlertRepository(application.applicationContext),
    private val whaleLeverageRepository: WhaleLeverageRepository = WhaleLeverageRepository(),
    private val futuresRepository: FuturesTerminalRepository = FuturesTerminalRepository(application.applicationContext),
    val billingManager: BillingManager = BillingManager(application.applicationContext)
) : AndroidViewModel(application) {

    val isProUnlocked: StateFlow<Boolean> = billingManager.isProUnlocked
    val monthlyPrice: StateFlow<String> = billingManager.monthlyPrice
    val yearlyPrice: StateFlow<String> = billingManager.yearlyPrice
    val isBillingLoading: StateFlow<Boolean> = billingManager.isLoading

    private val aiPrefs = application.applicationContext.getSharedPreferences("crypto_cycles_ai_queries", android.content.Context.MODE_PRIVATE)
    private fun getTodayAiKey(): String {
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        return "ai_queries_" + sdf.format(java.util.Date())
    }

    private val _aiDailyQueryCount = MutableStateFlow(aiPrefs.getInt(getTodayAiKey(), 0))
    val aiDailyQueryCount: StateFlow<Int> = _aiDailyQueryCount.asStateFlow()

    val aiQueriesRemaining: StateFlow<Int> = combine(_aiDailyQueryCount, isProUnlocked) { count, pro ->
        if (pro) 999 else (3 - count).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val isAiLimitReached: StateFlow<Boolean> = combine(_aiDailyQueryCount, isProUnlocked) { count, pro ->
        !pro && count >= 3
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val selectedCurrency: StateFlow<Currency> = repository.selectedCurrency
    val selectedLanguage: StateFlow<AppLanguage> = repository.selectedLanguage
    val selectedTheme: StateFlow<com.example.data.model.AppThemeOption> = repository.selectedTheme
    val lastUpdatedTimestamp: StateFlow<Long> = repository.lastUpdatedTimestamp
    val isLiveConnected: StateFlow<Boolean> = repository.isLiveConnected
    val isCacheStale: StateFlow<Boolean> = repository.isCacheStale
    val priceSource: StateFlow<String> = repository.priceSource
    val macroSignal: StateFlow<MacroCycleSignal> = repository.macroSignal
    val allCoins: StateFlow<List<CryptoCoin>> = repository.coins

    // Centralized Price State & Price Bus (Single Source of Truth across Screens)
    val centralizedPriceState: StateFlow<com.example.data.model.CentralizedPriceState> = repository.centralizedPriceState
    val priceBus: StateFlow<com.example.data.model.PriceBusState> = repository.priceBus

    val whaleAlerts: StateFlow<List<WhaleAlert>> = whaleRepository.alerts
    val whaleSettings: StateFlow<WhaleAlertSettings> = whaleRepository.settings
    val whaleLeveragePositions: StateFlow<List<WhaleLeveragePosition>> = whaleLeverageRepository.positions
    val whaleLeverageSummary: StateFlow<WhaleLeverageSummary> = whaleLeverageRepository.summary

    // Futures Terminal streams
    val activeFuturesSymbol: StateFlow<String> = futuresRepository.currentSymbol
    val futuresTickerData: StateFlow<FuturesTickerData?> = futuresRepository.tickerData
    val futuresBookTicker: StateFlow<FuturesBookTicker?> = futuresRepository.bookTicker
    val futuresMarkFunding: StateFlow<FuturesMarkFunding?> = futuresRepository.markFunding
    val futuresOpenInterest: StateFlow<FuturesOpenInterest?> = futuresRepository.openInterest
    val futuresRecentTrades: StateFlow<List<FuturesTrade>> = futuresRepository.recentTrades
    val futuresRecentLiquidations: StateFlow<List<FuturesLiquidationOrder>> = futuresRepository.recentLiquidations
    val futuresConnectionStatus: StateFlow<FuturesConnectionStatus> = futuresRepository.connectionStatus
    val futuresMacroSentiment: StateFlow<MacroMarketSentiment> = futuresRepository.macroSentiment
    val futuresMarketIntelligenceReport: StateFlow<com.example.data.model.MarketIntelligenceReport> = futuresRepository.marketIntelligenceReport

    // US Spot BTC ETF Net Flows Repository
    private val etfRepository: BitcoinEtfRepository = BitcoinEtfRepository(viewModelScope)
    val etfFlowData: StateFlow<BitcoinEtfFlowData> = etfRepository.etfFlowData

    // DefiLlama Stablecoin Liquidity Repository
    private val liquidityRepository: DefiLlamaLiquidityRepository = DefiLlamaLiquidityRepository(viewModelScope)
    val stablecoinLiquidityData: StateFlow<StablecoinLiquidityData> = liquidityRepository.liquidityData

    // Institutional Cycle Command Center Synthesizer
    val cycleCommandState: StateFlow<CycleCommandState> = combine(
        centralizedPriceState,
        etfFlowData,
        stablecoinLiquidityData,
        futuresMarkFunding
    ) { priceState, etf, liq, funding ->
        CycleCommandEngine.computeCycleCommandState(
            btcPrice = priceState.btcSpotPrice,
            etfFlowData = etf,
            liquidityData = liq,
            futuresMarkFunding = funding
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CycleCommandState()
    )

    // Quantitative Forecast Engine for BTC
    val btcForecast: StateFlow<ForecastCardModel> = combine(
        centralizedPriceState,
        futuresMarkFunding,
        etfFlowData,
        allCoins
    ) { priceState, funding, etf, coins ->
        val currentPrice = if (priceState.btcPerpPrice > 0.0) priceState.btcPerpPrice else priceState.btcSpotPrice
        val liveSpark = coins.firstOrNull { it.symbol.equals("BTC", ignoreCase = true) }
            ?.takeIf { it.priceUpdatedAtMs > 0L && it.sparkline.size >= 30 }
            ?.sparkline
            .orEmpty()
        val fundingRate = funding?.fundingRate ?: 0.0
        val etfInflows = if (etf.isLive) etf.oneDayNetFlowMillionUsd * 1_000_000.0 else 0.0

        QuantForecastEngine.computeForecast(
            symbol = "BTC",
            currentPrice = currentPrice,
            historicalPrices = liveSpark,
            highs = liveSpark.map { it * 1.008 },
            lows = liveSpark.map { it * 0.992 },
            fundingRate = fundingRate,
            etfInflowsUsd = etfInflows
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuantForecastEngine.computeForecast(
            symbol = "BTC",
            currentPrice = 0.0,
            historicalPrices = emptyList(),
            highs = emptyList(),
            lows = emptyList()
        )
    )

    // Forward Signal Audit Trail Repository
    private val forwardAuditRepo: ForwardAuditTrailRepository = ForwardAuditTrailRepository()
    val forwardAuditLogs: StateFlow<List<ForwardSignalAuditEntry>> = forwardAuditRepo.auditLogs

    // 30-Day In-App Alert History Log
    val alertHistory: StateFlow<List<AlertHistoryItem>> = AlertHistoryManager.alerts
    val unreadAlertsCount: StateFlow<Int> = AlertHistoryManager.alerts.map { list ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _showAlertHistorySheet = MutableStateFlow(false)
    val showAlertHistorySheet: StateFlow<Boolean> = _showAlertHistorySheet.asStateFlow()

    fun openAlertHistory() {
        _showAlertHistorySheet.value = true
    }

    fun closeAlertHistory() {
        _showAlertHistorySheet.value = false
    }

    private val chartPrefs = application.getSharedPreferences("crypto_cycles_chart", android.content.Context.MODE_PRIVATE)
    private val _logCharts = MutableStateFlow(chartPrefs.getBoolean("log_charts", true))
    val logCharts: StateFlow<Boolean> = _logCharts.asStateFlow()

    fun setLogCharts(enabled: Boolean) {
        _logCharts.value = enabled
        chartPrefs.edit().putBoolean("log_charts", enabled).apply()
    }

    private val _selectedTab = MutableStateFlow(MainTab.MARKETS)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // AI Market Analyst Assistant
    private val geminiAiService: com.example.data.network.GeminiAiService = com.example.data.network.GeminiAiService()
    private val _aiMessages = MutableStateFlow<List<com.example.data.model.AiChatMessage>>(emptyList())
    val aiMessages: StateFlow<List<com.example.data.model.AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _showAiAssistantModal = MutableStateFlow(false)
    val showAiAssistantModal: StateFlow<Boolean> = _showAiAssistantModal.asStateFlow()

    fun openAiAssistant(initialPrompt: String? = null) {
        _showAiAssistantModal.value = true
        if (_aiMessages.value.isEmpty()) {
            val strings = com.example.util.getAppStrings(selectedLanguage.value)
            _aiMessages.value = listOf(
                com.example.data.model.AiChatMessage(
                    sender = com.example.data.model.AiMessageSender.AI,
                    text = strings.aiGreeting
                )
            )
        }
        if (!initialPrompt.isNullOrBlank()) {
            sendAiMessage(initialPrompt)
        }
    }

    fun closeAiAssistant() {
        _showAiAssistantModal.value = false
    }

    fun clearAiChat() {
        val strings = com.example.util.getAppStrings(selectedLanguage.value)
        _aiMessages.value = listOf(
            com.example.data.model.AiChatMessage(
                sender = com.example.data.model.AiMessageSender.AI,
                text = strings.aiGreeting
            )
        )
    }

    fun sendAiMessage(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isEmpty() || _isAiLoading.value) return

        if (!isProUnlocked.value && _aiDailyQueryCount.value >= 3) {
            return
        }

        if (!isProUnlocked.value) {
            val newCount = _aiDailyQueryCount.value + 1
            _aiDailyQueryCount.value = newCount
            aiPrefs.edit().putInt(getTodayAiKey(), newCount).apply()
        }

        val userMessage = com.example.data.model.AiChatMessage(
            sender = com.example.data.model.AiMessageSender.USER,
            text = trimmed
        )
        val thinkingMessage = com.example.data.model.AiChatMessage(
            sender = com.example.data.model.AiMessageSender.AI,
            text = "",
            isThinking = true
        )

        _aiMessages.value = _aiMessages.value + userMessage + thinkingMessage
        _isAiLoading.value = true

        viewModelScope.launch {
            val liveTokenData = repository.queryLiveTokenData(trimmed)
            val snapshot = buildCurrentMarketSnapshot(trimmed, liveTokenData)
            val language = selectedLanguage.value
            try {
                val aiResponseText = geminiAiService.analyzeMarketQuery(trimmed, snapshot, language)
                val aiMessage = com.example.data.model.AiChatMessage(
                    sender = com.example.data.model.AiMessageSender.AI,
                    text = aiResponseText
                )
                // Remove thinking placeholder and append real response
                _aiMessages.value = _aiMessages.value.filterNot { it.isThinking } + aiMessage
            } catch (e: Exception) {
                val errorMessage = com.example.data.model.AiChatMessage(
                    sender = com.example.data.model.AiMessageSender.AI,
                    text = "Analysis unavailable. Tap to retry.",
                    isError = true
                )
                _aiMessages.value = _aiMessages.value.filterNot { it.isThinking } + errorMessage
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun sendAiUserMessage(userPrompt: String) = sendAiMessage(userPrompt)

    private fun buildCurrentMarketSnapshot(userQuery: String = "", onDemandTokenData: String? = null): com.example.data.model.LiveMarketContextSnapshot {
        val coins = allCoins.value
        val btc = coins.firstOrNull { it.symbol.equals("BTC", ignoreCase = true) }
        val eth = coins.firstOrNull { it.symbol.equals("ETH", ignoreCase = true) }
        val sol = coins.firstOrNull { it.symbol.equals("SOL", ignoreCase = true) }
        val markFunding = futuresMarkFunding.value
        val openInterest = futuresOpenInterest.value
        val macro = futuresMacroSentiment.value
        val topAlert = whaleAlerts.value.firstOrNull()

        val daysSinceHalving = com.example.util.HalvingCycleUtils.getDaysSince4thHalving().toLong()

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", java.util.Locale.getDefault())
        val currentTimeFormatted = sdf.format(java.util.Date())

        val cleanQuery = userQuery.lowercase(java.util.Locale.ROOT)
        val matchedCoins = if (cleanQuery.isNotBlank() && onDemandTokenData == null) {
            coins.filter { coin ->
                val sym = coin.symbol.lowercase(java.util.Locale.ROOT)
                val name = coin.name.lowercase(java.util.Locale.ROOT)
                com.example.data.model.queryMentionsSymbol(cleanQuery, sym) || cleanQuery.contains(name) ||
                (sym == "xrp" && (cleanQuery.contains("ριπλ") || cleanQuery.contains("ripple") || cleanQuery.contains("xrp"))) ||
                (sym == "sol" && (cleanQuery.contains("σολανα") || cleanQuery.contains("solana") || cleanQuery.contains("sol"))) ||
                (sym == "eth" && (cleanQuery.contains("αιθεριο") || cleanQuery.contains("ethereum") || cleanQuery.contains("eth"))) ||
                (sym == "btc" && (cleanQuery.contains("μπιτκοιν") || cleanQuery.contains("bitcoin") || cleanQuery.contains("btc"))) ||
                (sym == "ada" && (cleanQuery.contains("καρντανο") || cleanQuery.contains("cardano") || cleanQuery.contains("ada"))) ||
                (sym == "doge" && (cleanQuery.contains("ντοτζ") || cleanQuery.contains("dogecoin") || cleanQuery.contains("doge")))
            }
        } else emptyList()

        val targetedInfo = onDemandTokenData ?: if (matchedCoins.isNotEmpty()) {
            matchedCoins.joinToString("\n\n") { coin ->
                val lang = com.example.util.AppNumberFormatter.currentLanguage
                val formattedPrice = com.example.util.AppNumberFormatter.formatPrice(coin.priceUsd, language = lang)
                val sign = if (coin.change24h >= 0) "+" else ""
                val formattedChange = com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = false, language = lang).removeSuffix("%")
                val formattedVol = com.example.util.AppNumberFormatter.formatCompactCurrency(coin.volume24h, language = lang)
                val formattedMcap = com.example.util.AppNumberFormatter.formatCompactCurrency(coin.marketCap, language = lang)
                val formattedAth = com.example.util.AppNumberFormatter.formatPrice(coin.athUsd, language = lang)
                val pctFromAth = com.example.util.AppNumberFormatter.formatPercent(if (coin.athUsd > 0) ((coin.priceUsd - coin.athUsd) / coin.athUsd) * 100.0 else 0.0, includeSign = false, language = lang).removeSuffix("%")
                "• COIN: ${coin.name} (${coin.symbol.uppercase()})\n  - Live Spot Price: $formattedPrice USD\n  - 24h Change: $sign$formattedChange%\n  - 24h Volume: $formattedVol USD\n  - Market Cap: $formattedMcap USD\n  - All-Time High (ATH): $formattedAth ($pctFromAth% from ATH)"
            }
        } else null

        val allSummary = coins.joinToString("; ") { coin ->
            val lang = com.example.util.AppNumberFormatter.currentLanguage
            val sign = if (coin.change24h >= 0) "+" else ""
            val formattedChange = com.example.util.AppNumberFormatter.formatPercent(coin.change24h, includeSign = false, language = lang).removeSuffix("%")
            val formattedPrice = com.example.util.AppNumberFormatter.formatPrice(coin.priceUsd, language = lang)
            "${coin.name} (${coin.symbol}): $formattedPrice ($sign$formattedChange% 24h)"
        }

        val bus = priceBus.value
        val btcPrice = if (bus.btcSpot.price > 0.0) bus.btcSpot.price else (btc?.priceUsd ?: 0.0)
        val btcChange = if (bus.btcSpot.price > 0.0) bus.btcSpot.change24h else (btc?.change24h ?: 0.0)
        val ethPrice = if (bus.ethSpot.price > 0.0) bus.ethSpot.price else (eth?.priceUsd ?: 0.0)
        val ethChange = if (bus.ethSpot.price > 0.0) bus.ethSpot.change24h else (eth?.change24h ?: 0.0)
        val solPrice = if (bus.solSpot.price > 0.0) bus.solSpot.price else (sol?.priceUsd ?: 0.0)
        val solChange = if (bus.solSpot.price > 0.0) bus.solSpot.change24h else (sol?.change24h ?: 0.0)

        val calculatedAltSeason = com.example.util.AltcoinSeasonCalculator.calculate(coins, btcPrice).score

        return com.example.data.model.LiveMarketContextSnapshot(
            currentTimeString = currentTimeFormatted,
            btcPrice = btcPrice,
            btc24hChange = btcChange,
            btcTs = bus.btcSpot.tsMillis,
            ethPrice = ethPrice,
            eth24hChange = ethChange,
            ethTs = bus.ethSpot.tsMillis,
            solPrice = solPrice,
            sol24hChange = solChange,
            solTs = bus.solSpot.tsMillis,
            cyclePhase = "Phase 4: Early Bull Expansion (Mid-Cycle)",
            cycleDay = daysSinceHalving + 14,
            daysSinceHalving = daysSinceHalving,
            rainbowBand = "Accumulation / Support Base",
            distance200w = "+54.2% above 200W SMA",
            piCycleGap = "+48.6%",
            fearAndGreedScore = macro.fearAndGreedValue ?: 55,
            fearAndGreedSentiment = macro.fearAndGreedClassification ?: "Greed",
            btcDominancePct = macro.btcDominance ?: 58.0,
            altcoinSeasonIndex = calculatedAltSeason,
            fundingRatePct = if (markFunding?.fromExchange == true && markFunding.fundingRate != null) {
                (markFunding.fundingRate ?: 0.0) * 100.0
            } else {
                0.0
            },
            futuresMarkPrice = if (bus.btcMark.price > 0.0) bus.btcMark.price else (markFunding?.markPrice ?: 0.0),
            openInterestUsd = if (openInterest?.isAvailable == true) openInterest.openInterestUsd ?: 0.0 else 0.0,
            activeFuturesSymbol = activeFuturesSymbol.value,
            whaleNet24h = "+$142M Net Accumulation",
            marketStance = "Historical cycle alignment & institutional net flows",
            targetedCoinInfo = targetedInfo,
            allTrackedCoinsSummary = allSummary,
            topMarketPricesSummary = allSummary
        )
    }

    private val _selectedCategory = MutableStateFlow(CoinCategory.ALL)
    val selectedCategory: StateFlow<CoinCategory> = _selectedCategory.asStateFlow()

    private val _selectedCoinId = MutableStateFlow<String?>(null)
    val selectedCoin: StateFlow<CryptoCoin?> = combine(
        repository.coins,
        _selectedCoinId
    ) { coins, id ->
        if (id == null) null
        else coins.firstOrNull { it.id == id || it.symbol.equals(id, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedSignalCoinId = MutableStateFlow("solana")
    val selectedSignalCoinId: StateFlow<String> = _selectedSignalCoinId.asStateFlow()

    val selectedSignalCoin: StateFlow<CryptoCoin?> = combine(
        repository.coins,
        _selectedSignalCoinId
    ) { coins, signalId ->
        coins.firstOrNull { it.id == signalId || it.symbol.equals(signalId, ignoreCase = true) }
            ?: coins.firstOrNull { it.symbol == "SOL" }
            ?: coins.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showProModal = MutableStateFlow(false)
    val showProModal: StateFlow<Boolean> = _showProModal.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val featuredHeroCoin: StateFlow<CryptoCoin?> = combine(
        repository.coins,
        _selectedCoinId
    ) { coins, selectedId ->
        val selected = if (selectedId != null) coins.firstOrNull { it.id == selectedId || it.symbol.equals(selectedId, ignoreCase = true) } else null
        selected ?: coins.firstOrNull { it.symbol == "SOL" } ?: coins.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val filteredCoins: StateFlow<List<CryptoCoin>> = combine(
        repository.coins,
        _searchQuery,
        _selectedCategory
    ) { coins, query, category ->
        var list = coins
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.symbol.contains(query, ignoreCase = true)
            }
        }
        when (category) {
            CoinCategory.ALL -> list
            CoinCategory.FAVORITES -> list.filter { it.isFavorite }
            else -> list.filter { it.category == category }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                com.example.util.AppSoundManager.initialize(application.applicationContext)
                AlertHistoryManager.initialize(application.applicationContext)
                CryptoMarketWatchWorker.schedulePeriodic(application.applicationContext)
            } catch (_: Exception) {}
        }

        viewModelScope.launch {
            billingManager.isProUnlocked.collect { unlocked ->
                repository.setProUnlocked(unlocked)
                whaleRepository.setProUser(unlocked)
            }
        }

        viewModelScope.launch {
            repository.coins.collect { coinList ->
                whaleLeverageRepository.updateWithLiveCoins(coinList)
                futuresRepository.updateLiveCoins(coinList)
            }
        }

        viewModelScope.launch {
            futuresRepository.tickerData.collect { ticker ->
                if (ticker != null && ticker.fromExchange && (ticker.lastPrice ?: 0.0) > 0.0) {
                    val sym = ticker.symbol.uppercase()
                    repository.updatePriceTick(
                        com.example.data.model.PriceTick(
                            symbol = sym,
                            venue = "Binance Futures",
                            kind = com.example.data.model.PriceKind.PERP,
                            price = ticker.lastPrice ?: 0.0,
                            change24h = ticker.priceChangePercent24h ?: 0.0,
                            high24h = ticker.high24h,
                            low24h = ticker.low24h,
                            volumeQuote = ticker.volumeQuote24h,
                            tsMillis = ticker.receivedTimeMs,
                            source = "https://fapi.binance.com"
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            futuresRepository.markFunding.collect { funding ->
                if (funding != null && funding.fromExchange && (funding.markPrice ?: 0.0) > 0.0) {
                    val sym = funding.symbol.uppercase()
                    repository.updatePriceTick(
                        com.example.data.model.PriceTick(
                            symbol = sym,
                            venue = "Binance Futures",
                            kind = com.example.data.model.PriceKind.MARK,
                            price = funding.markPrice ?: 0.0,
                            change24h = 0.0,
                            tsMillis = funding.receivedTimeMs,
                            source = "https://fapi.binance.com"
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            futuresRepository.tradesFlow.collect { trade ->
                if (trade.price > 0.0) {
                    repository.updateCoinTradePrice(trade.symbol, trade.price, trade.timeMs)
                }
            }
        }

        viewModelScope.launch {
            futuresRepository.bookTicker.collect { book ->
                if (book != null && book.fromExchange) {
                    val mid = if (book.bidPrice != null && book.askPrice != null && book.bidPrice > 0 && book.askPrice > 0) {
                        (book.bidPrice + book.askPrice) / 2.0
                    } else book.bidPrice ?: book.askPrice
                    if (mid != null && mid > 0.0) {
                        repository.updateCoinTradePrice(book.symbol, mid, book.receivedTimeMs)
                    }
                }
            }
        }

        viewModelScope.launch {
            // Initial parallel fetch
            launch(Dispatchers.IO) { repository.fetchBinanceBtcTickerDirect() }
            launch(Dispatchers.IO) { repository.refreshLivePrices() }
            launch(Dispatchers.IO) { futuresRepository.refresh() }

            // Synchronized background fetch loop so screens do not diverge
            while (isActive) {
                delay(12000)
                try {
                    val pBtc = launch(Dispatchers.IO) { repository.fetchBinanceBtcTickerDirect() }
                    val p1 = launch(Dispatchers.IO) { repository.refreshLivePrices() }
                    val p2 = launch(Dispatchers.IO) { futuresRepository.refresh() }
                    joinAll(pBtc, p1, p2)
                } catch (t: Throwable) {
                    android.util.Log.w("CryptoViewModel", "Sync background fetch warning", t)
                }
            }
        }
    }

    fun setTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: CoinCategory) {
        _selectedCategory.value = category
    }

    fun selectCoin(coin: CryptoCoin?) {
        _selectedCoinId.value = coin?.id
    }

    fun selectSignalCoin(coinId: String) {
        _selectedSignalCoinId.value = coinId
    }

    fun toggleFavorite(coinId: String) {
        repository.toggleFavorite(coinId)
    }

    fun openProModal() {
        _showProModal.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                billingManager.queryProductDetails()
                billingManager.queryActivePurchases()
            } catch (t: Throwable) {
                android.util.Log.w("CryptoViewModel", "Error refreshing billing details in openProModal", t)
            }
        }
    }

    fun closeProModal() {
        _showProModal.value = false
    }

    fun launchPurchase(
        activity: Activity,
        productId: String,
        onLaunched: ((Boolean, String?) -> Unit)? = null
    ) {
        billingManager.launchPurchaseFlow(activity, productId, onLaunched)
    }

    fun restorePurchases(onResult: (Boolean, String) -> Unit) {
        billingManager.restorePurchases(onResult)
    }

    fun setProUnlocked(unlocked: Boolean) {
        billingManager.updateProState(unlocked)
        repository.setProUnlocked(unlocked)
        whaleRepository.setProUser(unlocked)
    }

    fun setWhaleNotificationsEnabled(enabled: Boolean) {
        whaleRepository.setNotificationsEnabled(enabled)
    }

    fun setWhaleMinThreshold(thresholdUsd: Double) {
        whaleRepository.setMinThreshold(thresholdUsd)
    }

    fun setNotifyZoneChange(enabled: Boolean) {
        whaleRepository.setNotifyZoneChange(enabled)
    }

    fun setNotifyPiCycle(enabled: Boolean) {
        whaleRepository.setNotifyPiCycle(enabled)
    }

    fun setNotifyRainbowBand(enabled: Boolean) {
        whaleRepository.setNotifyRainbowBand(enabled)
    }

    fun setNotify200wSma(enabled: Boolean) {
        whaleRepository.setNotify200wSma(enabled)
    }


    fun setCurrency(currency: Currency) {
        repository.setCurrency(currency)
    }

    fun setLanguage(language: AppLanguage) {
        repository.setLanguage(language)
    }

    fun setTheme(theme: com.example.data.model.AppThemeOption) {
        repository.setTheme(theme)
    }

    fun manualRefresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                withTimeoutOrNull(4000L) {
                    val futuresJob = launch(Dispatchers.IO) {
                        try {
                            futuresRepository.refresh()
                        } catch (t: Throwable) {
                            android.util.Log.w("CryptoViewModel", "Futures refresh warning", t)
                        }
                    }
                    val pricesJob = launch(Dispatchers.IO) {
                        try {
                            repository.refreshLivePrices()
                        } catch (t: Throwable) {
                            android.util.Log.w("CryptoViewModel", "Prices refresh warning", t)
                        }
                    }
                    val etfJob = launch(Dispatchers.IO) {
                        try {
                            etfRepository.refreshEtfFlows()
                        } catch (t: Throwable) {
                            android.util.Log.w("CryptoViewModel", "ETF flows refresh warning", t)
                        }
                    }
                    val liqJob = launch(Dispatchers.IO) {
                        try {
                            liquidityRepository.refreshLiquidity()
                        } catch (t: Throwable) {
                            android.util.Log.w("CryptoViewModel", "DefiLlama liquidity refresh warning", t)
                        }
                    }
                    joinAll(futuresJob, pricesJob, etfJob, liqJob)
                }
            } catch (t: Throwable) {
                android.util.Log.w("CryptoViewModel", "Refresh failed", t)
            } finally {
                delay(300)
                _isRefreshing.value = false
            }
        }
    }

    private var selectFuturesJob: kotlinx.coroutines.Job? = null

    fun selectFuturesSymbol(symbol: String) {
        selectFuturesJob?.cancel()
        selectFuturesJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val cleanSym = symbol.uppercase().removeSuffix("USDT").removePrefix("1000")
                val coin = repository.coins.value.firstOrNull { it.symbol.equals(cleanSym, ignoreCase = true) }
                withContext(Dispatchers.IO) {
                    futuresRepository.selectSymbol(symbol, coin)
                }
            } catch (t: Throwable) {
                android.util.Log.e("CryptoViewModel", "Error switching symbol to $symbol", t)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        futuresRepository.onCleared()
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CryptoViewModel(application) as T
                }
            }
    }
}
