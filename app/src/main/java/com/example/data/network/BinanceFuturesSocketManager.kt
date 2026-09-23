package com.example.data.network

import com.example.data.model.CryptoCoin
import com.example.data.model.QuoteState
import com.example.data.model.FuturesBookTicker
import com.example.data.model.FuturesConnectionStatus
import com.example.data.model.FuturesLiquidationOrder
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.FuturesOpenInterest
import com.example.data.model.FuturesTickerData
import com.example.data.model.FuturesTrade
import com.example.data.model.MacroMarketSentiment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.min

class BinanceFuturesSocketManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val httpRestClient = OkHttpClient.Builder()
        .connectTimeout(2500, TimeUnit.MILLISECONDS)
        .readTimeout(2500, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // State Flows
    private val _currentSymbol = MutableStateFlow("BTCUSDT")
    val currentSymbol: StateFlow<String> = _currentSymbol.asStateFlow()

    private val _tickerData = MutableStateFlow<FuturesTickerData?>(null)
    val tickerData: StateFlow<FuturesTickerData?> = _tickerData.asStateFlow()

    private val _bookTicker = MutableStateFlow<FuturesBookTicker?>(null)
    val bookTicker: StateFlow<FuturesBookTicker?> = _bookTicker.asStateFlow()

    private val _markFunding = MutableStateFlow<FuturesMarkFunding?>(null)
    val markFunding: StateFlow<FuturesMarkFunding?> = _markFunding.asStateFlow()

    private val _openInterest = MutableStateFlow<FuturesOpenInterest?>(null)
    val openInterest: StateFlow<FuturesOpenInterest?> = _openInterest.asStateFlow()

    private val _connectionStatus = MutableStateFlow(FuturesConnectionStatus())
    val connectionStatus: StateFlow<FuturesConnectionStatus> = _connectionStatus.asStateFlow()

    private val _macroSentiment = MutableStateFlow(MacroMarketSentiment())
    val macroSentiment: StateFlow<MacroMarketSentiment> = _macroSentiment.asStateFlow()

    // Stream events
    private val _tradesFlow = MutableSharedFlow<FuturesTrade>(extraBufferCapacity = 64)
    val tradesFlow: SharedFlow<FuturesTrade> = _tradesFlow.asSharedFlow()

    private val _liquidationsFlow = MutableSharedFlow<FuturesLiquidationOrder>(extraBufferCapacity = 64)
    val liquidationsFlow: SharedFlow<FuturesLiquidationOrder> = _liquidationsFlow.asSharedFlow()

    // Active WebSocket
    private var webSocket: WebSocket? = null

    // Jobs
    private var pollingJob: Job? = null
    private var macroPollingJob: Job? = null
    private var reconnectJob: Job? = null
    private var restBackupJob: Job? = null

    // Single source of truth synchronized live coin prices
    private val liveCoinsMap = java.util.concurrent.ConcurrentHashMap<String, CryptoCoin>()

    fun updateLiveCoins(coins: List<CryptoCoin>) {
        for (c in coins) {
            liveCoinsMap[c.symbol.uppercase()] = c
        }
        val currentSym = _currentSymbol.value
        val clean = currentSym.uppercase().removeSuffix("USDT").removePrefix("1000")
        val liveCoin = liveCoinsMap[clean] ?: liveCoinsMap[currentSym.uppercase()]
    }


    private var reconnectDelayMs = 1000L
    private var isManualDisconnect = false
    private var lastWsMessageTime = 0L

    init {
        val initialSym = _currentSymbol.value
        startMacroPolling()
        initiateSymbol(initialSym)
    }

    fun setSymbol(rawSymbol: String, coin: CryptoCoin? = null) {
        val formatted = formatSymbol(rawSymbol)
        _currentSymbol.value = formatted

        initiateSymbol(formatted)
    }

    fun applyCoinSnapshot(symbol: String, coin: CryptoCoin? = null) {
        // Live socket/REST fills the terminal. Never invent ticker, book, funding, or OI.
        _connectionStatus.value = _connectionStatus.value.copy(activeSymbol = symbol)
    }

    /**
     * Binance quotes some low-priced assets in bundles of 1000 (1000PEPEUSDT is priced per
     * 1000 PEPE). Consumers divide incoming prices by this multiplier to recover the per-token
     * price, so any price we synthesise for such a symbol must use the same convention.
     * Mirrors CryptoRepository.extractBaseSymbolAndMultiplier.
     */
    fun contractMultiplierFor(symbol: String): Double = SymbolMath.canonical(symbol).second

    /** Returns a price in the quoting convention of [sym], not the per-token price. */
    fun getFallbackPriceForSymbol(sym: String): Double {
        val (base, multiplier) = SymbolMath.canonical(sym)
        val live = liveCoinsMap[base] ?: liveCoinsMap[sym.uppercase()]
        val unit = live?.takeIf { it.quoteState == QuoteState.LIVE && it.priceUsd > 0.0 }?.priceUsd ?: 0.0
        return unit * multiplier
    }

    fun formatSpotSymbol(symbol: String): String {
        // Spot is the 1:1 USDT pair after canonicalization. RAYSOLUSDT is futures-only;
        // the live spot market is RAYUSDT.
        val (base, _) = SymbolMath.canonical(symbol)
        return "${base}USDT"
    }

    private fun initiateSymbol(symbol: String) {
        // 1. Instant REST fetch to populate data in milliseconds
        scope.launch {
            fetchRestSnapshot(symbol)
        }

        // 2. Connect live WebSocket combined stream
        connectSocket(symbol)

        // 3. Start background pollers
        startOpenInterestPolling(symbol)
        startRestBackupPolling(symbol)
    }

    fun formatSymbol(symbol: String): String = ExchangeDirectory.futuresSymbolFor(symbol)

    @Synchronized
    private fun connectSocket(symbol: String) {
        reconnectJob?.cancel()
        closeSocket()

        isManualDisconnect = false
        val symLower = symbol.lowercase()

        // Official Binance Futures Combined Stream Endpoint
        val combinedStreamUrl = "wss://fstream.binance.com/stream?streams=" +
                "${symLower}@markPrice@1s/${symLower}@aggTrade/${symLower}@ticker/${symLower}@bookTicker/!forceOrder@arr"

        val request = Request.Builder()
            .url(combinedStreamUrl)
            .header("User-Agent", "CryptoCycles/55.0")
            .build()

        webSocket = okHttpClient.newWebSocket(request, createSocketListener(symbol))
    }

    private fun createSocketListener(targetSymbol: String): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectDelayMs = 1000L
                lastWsMessageTime = System.currentTimeMillis()
                updateConnectionState(isConnected = true, symbol = targetSymbol)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    lastWsMessageTime = System.currentTimeMillis()
                    handleCombinedStreamMessage(text)
                } catch (_: Throwable) {
                    // Fail gracefully
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!isManualDisconnect) {
                    scheduleReconnect(targetSymbol)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                // If WS fails, try spot socket fallback first before scheduling reconnect
                if (!isManualDisconnect) {
                    trySpotSocketFallback(targetSymbol)
                }
            }
        }
    }

    private fun trySpotSocketFallback(symbol: String) {
        if (isManualDisconnect) return
        val spotLower = formatSpotSymbol(symbol).lowercase()
        val spotUrl = "wss://stream.binance.com:9443/stream?streams=${spotLower}@ticker/${spotLower}@bookTicker/${spotLower}@trade"
        val request = Request.Builder()
            .url(spotUrl)
            .header("User-Agent", "CryptoCycles/55.0")
            .build()
        try {
            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    reconnectDelayMs = 1000L
                    lastWsMessageTime = System.currentTimeMillis()
                    updateConnectionState(isConnected = true, symbol = symbol)
                }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    lastWsMessageTime = System.currentTimeMillis()
                    handleCombinedStreamMessage(text)
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    scheduleReconnect(symbol)
                }
            })
        } catch (_: Throwable) {
            scheduleReconnect(symbol)
        }
    }

    private fun handleCombinedStreamMessage(jsonString: String) {
        val root = JSONObject(jsonString)
        val stream = root.optString("stream", "")
        val data = if (root.has("data")) root.getJSONObject("data") else root
        val eventType = data.optString("e", "")
        val eventTime = data.optLong("E", System.currentTimeMillis())
        val now = System.currentTimeMillis()
        val latency = maxOf(0L, now - eventTime)

        _connectionStatus.value = _connectionStatus.value.copy(
            isConnected = true,
            latencyMs = latency,
            lastEventTimeMs = eventTime,
            activeSymbol = _currentSymbol.value
        )

        if (eventType == "markPriceUpdate" || stream.endsWith("@markPrice@1s")) {
            val markPrice = data.optString("p", "").toDoubleOrNull()
            val indexPrice = data.optString("i", "").toDoubleOrNull()
            val fundingRate = data.optString("r", "").toDoubleOrNull()
            val nextFundingTime = if (data.has("T")) data.optLong("T", 0L) else null

            val basis = if (markPrice != null && indexPrice != null) markPrice - indexPrice else null
            val basisPercent = if (basis != null && indexPrice != null && indexPrice > 0) {
                (basis / indexPrice) * 100.0
            } else null

            val approxApr = if (fundingRate != null) fundingRate * 3.0 * 365.0 * 100.0 else null

            _markFunding.value = FuturesMarkFunding(
                symbol = data.optString("s", _currentSymbol.value),
                markPrice = markPrice,
                indexPrice = indexPrice,
                basis = basis,
                basisPercent = basisPercent,
                fundingRate = fundingRate,
                nextFundingTimeMs = nextFundingTime,
                approxApr = approxApr,
                eventTimeMs = eventTime,
                receivedTimeMs = now,
                fromExchange = true
            )
        } else if (eventType == "24hrTicker" || stream.endsWith("@ticker")) {
            val lastPrice = data.optString("c", "").toDoubleOrNull()
            val priceChange24h = data.optString("p", "").toDoubleOrNull()
            val priceChangePercent24h = data.optString("P", "").toDoubleOrNull()
            val high24h = data.optString("h", "").toDoubleOrNull()
            val low24h = data.optString("l", "").toDoubleOrNull()
            val volumeBase24h = data.optString("v", "").toDoubleOrNull()
            val volumeQuote24h = data.optString("q", "").toDoubleOrNull()

            _tickerData.value = FuturesTickerData(
                symbol = data.optString("s", _currentSymbol.value),
                lastPrice = lastPrice,
                priceChange24h = priceChange24h,
                priceChangePercent24h = priceChangePercent24h,
                high24h = high24h,
                low24h = low24h,
                volumeBase24h = volumeBase24h,
                volumeQuote24h = volumeQuote24h,
                eventTimeMs = eventTime,
                receivedTimeMs = now,
                fromExchange = true
            )
        } else if (eventType == "bookTicker" || stream.endsWith("@bookTicker") || data.has("b")) {
            val bidPrice = data.optString("b", "").toDoubleOrNull()
            val bidQty = data.optString("B", "").toDoubleOrNull()
            val askPrice = data.optString("a", "").toDoubleOrNull()
            val askQty = data.optString("A", "").toDoubleOrNull()

            val spread = if (bidPrice != null && askPrice != null) maxOf(0.0, askPrice - bidPrice) else null
            val spreadPercent = if (spread != null && bidPrice != null && bidPrice > 0) {
                (spread / bidPrice) * 100.0
            } else null

            _bookTicker.value = FuturesBookTicker(
                symbol = data.optString("s", _currentSymbol.value),
                bidPrice = bidPrice,
                bidQty = bidQty,
                askPrice = askPrice,
                askQty = askQty,
                spread = spread,
                spreadPercent = spreadPercent,
                eventTimeMs = eventTime,
                receivedTimeMs = now,
                fromExchange = true
            )
        } else if (eventType == "aggTrade" || eventType == "trade" || stream.endsWith("@aggTrade") || stream.endsWith("@trade")) {
            val tradeId = data.optLong("a", data.optLong("t", System.currentTimeMillis()))
            val price = data.optString("p", "0").toDoubleOrNull() ?: 0.0
            val qty = data.optString("q", "0").toDoubleOrNull() ?: 0.0
            val isBuyerMaker = data.optBoolean("m", false)
            val tradeTime = data.optLong("T", eventTime)

            if (price > 0 && qty > 0) {
                val trade = FuturesTrade(
                    id = tradeId,
                    symbol = data.optString("s", _currentSymbol.value),
                    price = price,
                    qty = qty,
                    isBuyerMaker = isBuyerMaker,
                    timeMs = tradeTime
                )
                _tradesFlow.tryEmit(trade)
            }
        } else if (eventType == "forceOrder" || stream.contains("forceOrder")) {
            val orderObj = data.optJSONObject("o")
            if (orderObj != null) {
                val sym = orderObj.optString("s", "")
                val side = orderObj.optString("S", "SELL")
                val price = orderObj.optString("p", "0").toDoubleOrNull() ?: 0.0
                val qty = orderObj.optString("q", "0").toDoubleOrNull() ?: 0.0
                val time = orderObj.optLong("T", eventTime)

                if (price > 0 && qty > 0) {
                    val liquidation = FuturesLiquidationOrder(
                        id = UUID.randomUUID().toString(),
                        symbol = sym,
                        side = side,
                        price = price,
                        qty = qty,
                        timeMs = time,
                        valueUsd = price * qty
                    )
                    _liquidationsFlow.tryEmit(liquidation)
                }
            }
        }
    }

    // --- High-Speed REST Fallback & Snapshot Layer ---

    private suspend fun fetchRestSnapshot(symbol: String) {
        scope.launch {
            fetchTickerRest(symbol)
        }
        scope.launch {
            fetchPremiumIndexRest(symbol)
        }
        scope.launch {
            fetchBookTickerRest(symbol)
        }
        scope.launch {
            fetchOpenInterest(symbol)
        }
        scope.launch {
            fetchRecentTradesRest(symbol)
        }
    }

    private suspend fun fetchTickerRest(symbol: String) {
        try {
            val spotSym = formatSpotSymbol(symbol)
            val futuresUrl = "https://fapi.binance.com/fapi/v1/ticker/24hr?symbol=$symbol"
            val spotUrl = "https://api.binance.com/api/v3/ticker/24hr?symbol=$spotSym"
            var scale = 1.0
            var json = getJsonObject(futuresUrl)
            if (json == null) {
                json = getJsonObject(spotUrl)
                // The spot pair quotes per token, so rescale into the futures symbol's convention.
                if (json != null) scale = contractMultiplierFor(symbol)
            }

            if (json != null) {
                val lastPrice = json.optString("lastPrice", "").toDoubleOrNull()
                val priceChange = json.optString("priceChange", "").toDoubleOrNull()
                val priceChangePercent = json.optString("priceChangePercent", "").toDoubleOrNull()
                val highPrice = json.optString("highPrice", "").toDoubleOrNull()
                val lowPrice = json.optString("lowPrice", "").toDoubleOrNull()
                val volume = json.optString("volume", "").toDoubleOrNull()
                val quoteVolume = json.optString("quoteVolume", "").toDoubleOrNull()
                val closeTime = json.optLong("closeTime", System.currentTimeMillis())

                if (lastPrice != null && lastPrice > 0) {
                    _tickerData.value = FuturesTickerData(
                        symbol = symbol,
                        lastPrice = lastPrice * scale,
                        priceChange24h = priceChange?.times(scale),
                        priceChangePercent24h = priceChangePercent,
                        high24h = highPrice?.times(scale),
                        low24h = lowPrice?.times(scale),
                        volumeBase24h = volume?.div(scale),
                        volumeQuote24h = quoteVolume,
                        eventTimeMs = closeTime,
                        receivedTimeMs = System.currentTimeMillis(),
                        fromExchange = true
                    )
                    updateConnectionState(isConnected = true, symbol = symbol)
                }
            }
        } catch (_: Throwable) {}
    }

    private suspend fun fetchPremiumIndexRest(symbol: String) {
        try {
            val url = "https://fapi.binance.com/fapi/v1/premiumIndex?symbol=$symbol"
            val json = getJsonObject(url)
            if (json != null) {
                val markPrice = json.optString("markPrice", "").toDoubleOrNull()
                val indexPrice = json.optString("indexPrice", "").toDoubleOrNull()
                val lastFundingRate = json.optString("lastFundingRate", "").toDoubleOrNull()
                val nextFundingTime = json.optLong("nextFundingTime", 0L)
                val time = json.optLong("time", System.currentTimeMillis())

                val basis = if (markPrice != null && indexPrice != null) markPrice - indexPrice else null
                val basisPercent = if (basis != null && indexPrice != null && indexPrice > 0) {
                    (basis / indexPrice) * 100.0
                } else null

                val approxApr = if (lastFundingRate != null) lastFundingRate * 3.0 * 365.0 * 100.0 else null

                _markFunding.value = FuturesMarkFunding(
                    symbol = symbol,
                    markPrice = markPrice,
                    indexPrice = indexPrice,
                    basis = basis,
                    basisPercent = basisPercent,
                    fundingRate = lastFundingRate,
                    nextFundingTimeMs = nextFundingTime,
                    approxApr = approxApr,
                    eventTimeMs = time,
                    receivedTimeMs = System.currentTimeMillis(),
                    fromExchange = true
                )
            }
        } catch (_: Throwable) {}
    }

    private suspend fun fetchBookTickerRest(symbol: String) {
        try {
            val spotSym = formatSpotSymbol(symbol)
            val futuresUrl = "https://fapi.binance.com/fapi/v1/ticker/bookTicker?symbol=$symbol"
            val spotUrl = "https://api.binance.com/api/v3/ticker/bookTicker?symbol=$spotSym"
            var scale = 1.0
            var json = getJsonObject(futuresUrl)
            if (json == null) {
                json = getJsonObject(spotUrl)
                if (json != null) scale = contractMultiplierFor(symbol)
            }

            if (json != null) {
                val bidPrice = json.optString("bidPrice", "").toDoubleOrNull()?.times(scale)
                val bidQty = json.optString("bidQty", "").toDoubleOrNull()?.div(scale)
                val askPrice = json.optString("askPrice", "").toDoubleOrNull()?.times(scale)
                val askQty = json.optString("askQty", "").toDoubleOrNull()?.div(scale)
                val time = json.optLong("time", System.currentTimeMillis())

                val spread = if (bidPrice != null && askPrice != null) maxOf(0.0, askPrice - bidPrice) else null
                val spreadPercent = if (spread != null && bidPrice != null && bidPrice > 0) {
                    (spread / bidPrice) * 100.0
                } else null

                _bookTicker.value = FuturesBookTicker(
                    symbol = symbol,
                    bidPrice = bidPrice,
                    bidQty = bidQty,
                    askPrice = askPrice,
                    askQty = askQty,
                    spread = spread,
                    spreadPercent = spreadPercent,
                    eventTimeMs = time,
                    receivedTimeMs = System.currentTimeMillis(),
                    fromExchange = true
                )
            }
        } catch (_: Throwable) {}
    }

    private suspend fun fetchRecentTradesRest(symbol: String) {
        try {
            val spotSym = formatSpotSymbol(symbol)
            val futuresUrl = "https://fapi.binance.com/fapi/v1/trades?symbol=$symbol&limit=20"
            val spotUrl = "https://api.binance.com/api/v3/trades?symbol=$spotSym&limit=20"
            var scale = 1.0
            var jsonArray = getJsonArray(futuresUrl)
            if (jsonArray == null) {
                jsonArray = getJsonArray(spotUrl)
                if (jsonArray != null) scale = contractMultiplierFor(symbol)
            }

            if (jsonArray != null) {
                for (i in (jsonArray.length() - 1) downTo 0) {
                    val obj = jsonArray.getJSONObject(i)
                    val trade = FuturesTrade(
                        id = obj.optLong("id", obj.optLong("a", 0L)),
                        symbol = symbol,
                        price = (obj.optString("price", "0").toDoubleOrNull() ?: 0.0) * scale,
                        qty = (obj.optString("qty", "0").toDoubleOrNull() ?: 0.0) / scale,
                        isBuyerMaker = obj.optBoolean("isBuyerMaker", false),
                        timeMs = obj.optLong("time", System.currentTimeMillis())
                    )
                    _tradesFlow.tryEmit(trade)
                }
            }
        } catch (_: Throwable) {}
    }

    private suspend fun fetchOpenInterest(symbol: String) {
        try {
            val url = "https://fapi.binance.com/fapi/v1/openInterest?symbol=${symbol.uppercase()}"
            val json = getJsonObject(url)
            if (json != null) {
                val oi = json.optString("openInterest", "").toDoubleOrNull()
                val time = json.optLong("time", System.currentTimeMillis())

                val ticker = _tickerData.value
                val mark = _markFunding.value
                val currentPrice = when {
                    ticker?.fromExchange == true && (ticker.lastPrice ?: 0.0) > 0.0 -> ticker.lastPrice
                    mark?.fromExchange == true && (mark.markPrice ?: 0.0) > 0.0 -> mark.markPrice
                    else -> null
                }
                val oiUsd = if (oi != null && currentPrice != null && currentPrice > 0) oi * currentPrice else null

                _openInterest.value = FuturesOpenInterest(
                    symbol = symbol,
                    openInterest = oi,
                    openInterestUsd = oiUsd,
                    lastRefreshTimeMs = time,
                    isAvailable = true
                )
            }
        } catch (_: Throwable) {
            if (_openInterest.value == null) {
                _openInterest.value = FuturesOpenInterest(
                    symbol = symbol,
                    openInterest = null,
                    openInterestUsd = null,
                    lastRefreshTimeMs = System.currentTimeMillis(),
                    isAvailable = false
                )
            }
        }
    }

    private fun startRestBackupPolling(symbol: String) {
        restBackupJob?.cancel()
        restBackupJob = scope.launch {
            while (isActive) {
                delay(1800)
                // If websocket hasn't received anything in the last 2.5 seconds, poll REST
                val now = System.currentTimeMillis()
                if (now - lastWsMessageTime > 2500) {
                    fetchRestSnapshot(symbol)
                }
            }
        }
    }

    private fun startOpenInterestPolling(symbol: String) {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (isActive) {
                fetchOpenInterest(symbol)
                delay(12_000)
            }
        }
    }

    private fun startMacroPolling() {
        macroPollingJob?.cancel()
        macroPollingJob = scope.launch {
            while (isActive) {
                fetchFearAndGreed()
                fetchBtcDominance()
                delay(300_000)
            }
        }
    }

    private suspend fun fetchFearAndGreed() {
        try {
            val json = getJsonObject("https://api.alternative.me/fng/?limit=32")
            val dataArr = json?.optJSONArray("data")
            if (dataArr != null && dataArr.length() > 0) {
                val first = dataArr.getJSONObject(0)
                val value = first.optString("value", "").toIntOrNull()
                val classification = first.optString("value_classification", "Neutral")
                
                var yesterday: Int? = null
                var lastWeek: Int? = null
                var lastMonth: Int? = null
                
                if (dataArr.length() > 1) yesterday = dataArr.getJSONObject(1).optString("value", "").toIntOrNull()
                if (dataArr.length() > 7) lastWeek = dataArr.getJSONObject(7).optString("value", "").toIntOrNull()
                if (dataArr.length() > 30) lastMonth = dataArr.getJSONObject(30).optString("value", "").toIntOrNull()

                _macroSentiment.value = _macroSentiment.value.copy(
                    fearAndGreedValue = value,
                    fearAndGreedClassification = classification,
                    fearAndGreedYesterday = yesterday,
                    fearAndGreedLastWeek = lastWeek,
                    fearAndGreedLastMonth = lastMonth,
                    fearAndGreedUpdatedMs = System.currentTimeMillis()
                )
            }
        } catch (_: Throwable) {}
    }

    private suspend fun fetchBtcDominance() {
        try {
            val json = getJsonObject("https://api.coingecko.com/api/v3/global")
            val data = json?.optJSONObject("data")
            val marketCapPct = data?.optJSONObject("market_cap_percentage")
            val btcDom = marketCapPct?.optDouble("btc", 0.0)
            if (btcDom != null && btcDom > 0) {
                _macroSentiment.value = _macroSentiment.value.copy(
                    btcDominance = btcDom,
                    btcDominanceUpdatedMs = System.currentTimeMillis()
                )
            }
        } catch (_: Throwable) {}
    }

    private fun getJsonObject(url: URL): JSONObject? = getJsonObject(url.toString())

    private fun getJsonObject(urlStr: String): JSONObject? {
        val body = MarketDataClient.getText(urlStr) ?: return null
        return try {
            JSONObject(body)
        } catch (_: Throwable) {
            null
        }
    }

    private fun getJsonArray(url: URL): JSONArray? = getJsonArray(url.toString())

    private fun getJsonArray(urlStr: String): JSONArray? {
        val body = MarketDataClient.getText(urlStr) ?: return null
        return try {
            JSONArray(body)
        } catch (_: Throwable) {
            null
        }
    }

    private fun scheduleReconnect(symbol: String) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val attempts = _connectionStatus.value.reconnectAttempts + 1
            _connectionStatus.value = _connectionStatus.value.copy(
                reconnectAttempts = attempts
            )
            delay(reconnectDelayMs)
            reconnectDelayMs = min(reconnectDelayMs * 2, 16000L)
            connectSocket(symbol)
        }
    }

    private fun updateConnectionState(isConnected: Boolean, symbol: String) {
        _connectionStatus.value = _connectionStatus.value.copy(
            isConnected = isConnected,
            activeSymbol = symbol
        )
    }

    @Synchronized
    private fun closeSocket() {
        try {
            webSocket?.close(1000, "Symbol changed or disconnected")
            webSocket = null
        } catch (_: Throwable) {}
    }

    fun forceRefresh() {
        scope.launch(Dispatchers.IO) {
            val sym = _currentSymbol.value
            // 1. Fetch live REST snapshot concurrently in parallel for immediate data
            launch { fetchTickerRest(sym) }
            launch { fetchPremiumIndexRest(sym) }
            launch { fetchBookTickerRest(sym) }
            launch { fetchOpenInterest(sym) }
            launch { fetchRecentTradesRest(sym) }

            // 2. Fetch macro sentiment
            launch { fetchFearAndGreed() }
            launch { fetchBtcDominance() }

            // 3. Connect socket ONLY if not connected or null, never drop a live stream
            if (webSocket == null || !_connectionStatus.value.isConnected) {
                connectSocket(sym)
            }
        }
    }

    /**
     * Forces immediate reconnection and snapshot refresh if connection dropped or stale.
     */
    fun checkConnectionAndRefresh() {
        val now = System.currentTimeMillis()
        val isWsDead = (webSocket == null || !_connectionStatus.value.isConnected)
        val isStale = (now - lastWsMessageTime > 15_000L) // > 15 seconds without message

        if (isWsDead || isStale) {
            android.util.Log.d("BinanceSocket", "Connection stale or dead. Triggering automatic reconnect & refresh.")
            forceRefresh()
        }
    }

    fun cleanup() {
        isManualDisconnect = true
        pollingJob?.cancel()
        macroPollingJob?.cancel()
        reconnectJob?.cancel()
        restBackupJob?.cancel()
        closeSocket()
    }
}
