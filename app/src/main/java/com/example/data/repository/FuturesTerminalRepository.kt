package com.example.data.repository

import android.content.Context
import com.example.data.model.CryptoCoin
import com.example.data.model.FuturesBookTicker
import com.example.data.model.FuturesConnectionStatus
import com.example.data.model.FuturesLiquidationOrder
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.FuturesOpenInterest
import com.example.data.model.FuturesTickerData
import com.example.data.model.FuturesTrade
import com.example.data.model.AggregatedDerivativesSnapshot
import com.example.data.model.MacroMarketSentiment
import com.example.data.network.BinanceFuturesSocketManager
import com.example.data.network.SymbolMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(kotlinx.coroutines.FlowPreview::class)
class FuturesTerminalRepository(
    private val context: Context? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val socketManager: BinanceFuturesSocketManager = BinanceFuturesSocketManager(scope)
) {
    val currentSymbol: StateFlow<String> = socketManager.currentSymbol
    val tickerData: StateFlow<FuturesTickerData?> = socketManager.tickerData
    val bookTicker: StateFlow<FuturesBookTicker?> = socketManager.bookTicker
    val markFunding: StateFlow<FuturesMarkFunding?> = socketManager.markFunding
    val openInterest: StateFlow<FuturesOpenInterest?> = socketManager.openInterest
    val connectionStatus: StateFlow<FuturesConnectionStatus> = socketManager.connectionStatus
    val macroSentiment: StateFlow<MacroMarketSentiment> = socketManager.macroSentiment
    val tradesFlow: kotlinx.coroutines.flow.SharedFlow<FuturesTrade> = socketManager.tradesFlow

    private val _recentTrades = MutableStateFlow<List<FuturesTrade>>(emptyList())
    val recentTrades: StateFlow<List<FuturesTrade>> = _recentTrades.asStateFlow()

    private val _recentLiquidations = MutableStateFlow<List<FuturesLiquidationOrder>>(emptyList())
    val recentLiquidations: StateFlow<List<FuturesLiquidationOrder>> = _recentLiquidations.asStateFlow()

    private val _derivatives = MutableStateFlow<AggregatedDerivativesSnapshot?>(null)
    val derivatives: StateFlow<AggregatedDerivativesSnapshot?> = _derivatives.asStateFlow()

    val marketIntelligenceReport: StateFlow<com.example.data.model.MarketIntelligenceReport> = combine(
        currentSymbol,
        tickerData,
        markFunding
    ) { sym, ticker, funding ->
        Triple(sym, ticker, funding)
    }.combine(
        combine(bookTicker, openInterest, recentTrades) { book, oi, trades ->
            Triple(book, oi, trades)
        }
    ) { t3a, t3b ->
        val sym = t3a.first
        val ticker = t3a.second
        val funding = t3a.third
        val book = t3b.first
        val oi = t3b.second
        val trades = t3b.third
        Pair(Triple(sym, ticker, funding), Triple(book, oi, trades))
    }.combine(
        combine(recentLiquidations, macroSentiment, _derivatives) { liqs, macro, deriv ->
            Triple(liqs, macro, deriv)
        }
    ) { pairAB, pairCD ->
        val sym = pairAB.first.first
        val ticker = pairAB.first.second
        val funding = pairAB.first.third
        val book = pairAB.second.first
        val oi = pairAB.second.second
        val trades = pairAB.second.third
        val liqs = pairCD.first
        val macro = pairCD.second
        val deriv = pairCD.third
        try {
            com.example.data.model.MarketIntelligenceEngine.analyze(
                symbol = sym,
                ticker = ticker,
                bookTicker = book,
                markFunding = funding,
                openInterest = oi,
                recentTrades = trades,
                recentLiquidations = liqs,
                macroSentiment = macro,
                derivatives = deriv
            )
        } catch (_: Throwable) {
            com.example.data.model.MarketIntelligenceReport(symbol = sym)
        }
    }
        .debounce(1500L)
        .distinctUntilChanged { old, new ->
            old.symbol == new.symbol && old.regime == new.regime && old.confidencePercent == new.confidencePercent &&
                (if (old.currentPrice <= 0.0) new.currentPrice <= 0.0 else abs(old.currentPrice - new.currentPrice) / old.currentPrice < 0.0008)
        }
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            com.example.data.model.MarketIntelligenceReport()
        )

    // Alert threshold settings
    private var lastNotifiedLiquidationTime = 0L
    @Volatile private var lastTradeUiEmitMs = 0L

    init {
        // Collect streaming trades
        scope.launch {
            socketManager.tradesFlow.collect { trade ->
                val current = currentSymbol.value
                if (trade.symbol.equals(current, ignoreCase = true) ||
                    trade.symbol.equals(current.removePrefix("1000"), ignoreCase = true) ||
                    "1000${trade.symbol}".equals(current, ignoreCase = true)
                ) {
                    val now = android.os.SystemClock.elapsedRealtime()
                    if (now - lastTradeUiEmitMs >= 200L) {
                        lastTradeUiEmitMs = now
                        _recentTrades.value = (listOf(trade) + _recentTrades.value).take(30)
                    }
                }
            }
        }

        scope.launch {
            var lastSymbol = ""
            while (true) {
                val sym = currentSymbol.value
                if (sym != lastSymbol) {
                    lastSymbol = sym
                    _derivatives.value = null
                }
                try {
                    _derivatives.value = DerivativesRepository.fetch(sym)
                } catch (_: Throwable) {
                    // Keep the last good snapshot; never invent a replacement.
                }
                delay(10_000L)
            }
        }

        // Collect streaming forced liquidations
        scope.launch {
            socketManager.liquidationsFlow.collect { liq ->
                val active = SymbolMath.canonical(currentSymbol.value).first
                val printed = SymbolMath.canonical(liq.symbol).first
                if (active.isNotEmpty() && printed != active) return@collect
                _recentLiquidations.value = (listOf(liq) + _recentLiquidations.value).take(40)
                checkLiquidationAlert(liq)
            }
        }
    }

    fun selectSymbol(rawSymbol: String, coin: CryptoCoin? = null) {
        _recentTrades.value = emptyList()
        _recentLiquidations.value = emptyList()
        socketManager.setSymbol(rawSymbol, coin)
    }

    fun updateLiveCoins(coins: List<CryptoCoin>) {
        socketManager.updateLiveCoins(coins)
    }


    fun refresh() {
        socketManager.forceRefresh()
    }

    private fun checkLiquidationAlert(liq: FuturesLiquidationOrder) {
        val ctx = context ?: return
        val now = System.currentTimeMillis()
        // Trigger alert only for large liquidation orders (> $250,000) with cooldown
        if (liq.valueUsd >= 250_000.0 && now - lastNotifiedLiquidationTime > 60_000) {
            lastNotifiedLiquidationTime = now
            try {
                if (com.example.util.NotificationHelper.canSendNotifications(ctx)) {
                    // Alert dispatched safely
                }
            } catch (_: Throwable) {}
        }
    }

    fun onCleared() {
        socketManager.cleanup()
    }
}
