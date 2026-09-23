package com.example.data.repository

import com.example.data.model.AppLanguage
import com.example.data.model.CentralizedPriceState
import com.example.data.model.CoinCategory
import com.example.data.model.CryptoCoin
import com.example.data.model.Currency
import com.example.data.model.MacroCycleSignal
import com.example.data.model.PriceBusState
import com.example.data.model.PriceKind
import com.example.data.model.PriceTick
import com.example.data.model.QuoteState
import com.example.data.network.ExchangeDirectory
import com.example.data.network.MarketDataClient
import com.example.data.network.SymbolMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.random.Random

class CryptoRepository(context: android.content.Context? = null) {

    private companion object {
        /** Keeps the /simple/price query string well inside CoinGecko's URL length limit. */
        const val COINGECKO_ID_BATCH_SIZE = 60
    }

    private val _coins = MutableStateFlow<List<CryptoCoin>>(
        if (context != null) {
            com.example.util.PriceCacheManager.init(context)
            com.example.util.PriceCacheManager.applyCachedPrices(CoinDatabaseFull.get100Coins())
        } else {
            CoinDatabaseFull.get100Coins()
        }
    )
    val coins: StateFlow<List<CryptoCoin>> = _coins.asStateFlow()

    private val _isCacheStale = MutableStateFlow(
        if (context != null) com.example.util.PriceCacheManager.isCacheStale() else true
    )
    val isCacheStale: StateFlow<Boolean> = _isCacheStale.asStateFlow()

    private val repoScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())
    private var miniTickerWebSocket: okhttp3.WebSocket? = null
    private var miniTickerHostIndex = 0
    private var lastMarketCapRefreshMs = 0L
    private val miniTickerHosts = listOf(
        "wss://data-stream.binance.vision/ws/!miniTicker@arr",
        "wss://stream.binance.com:9443/ws/!miniTicker@arr"
    )

    init {
        // Let the first Compose frame paint before opening sockets. The AI Studio
        // streaming emulator freezes if GPU + websockets all start in onCreate.
        repoScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(1500)
            connectBinanceMiniTickerSocket()
        }
        repoScope.launch(Dispatchers.IO) {
            kotlinx.coroutines.delay(1500)
            fetchBinanceBtcTickerDirect()
            refreshLivePrices()
        }
    }

    private fun connectBinanceMiniTickerSocket() {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .pingInterval(15, java.util.concurrent.TimeUnit.SECONDS)
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()
            val request = okhttp3.Request.Builder()
                .url(miniTickerHosts[miniTickerHostIndex % miniTickerHosts.size])
                .header("User-Agent", "CryptoCycles/1.0")
                .build()
            miniTickerWebSocket = client.newWebSocket(request, object : okhttp3.WebSocketListener() {
                override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                    _isLiveConnected.value = true
                }
                override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                    try {
                        val arr = org.json.JSONArray(text)
                        val quotes = HashMap<String, Triple<Double, Double, Double>>(arr.length())
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            val pair = obj.optString("s")
                            if (!pair.endsWith("USDT")) continue
                            val raw = obj.optDouble("c", 0.0)
                            val open = obj.optDouble("o", raw)
                            if (raw <= 0.0) continue
                            val (base, multiplier) = SymbolMath.canonical(pair)
                            val unit = raw / multiplier
                            val change = if (open > 0.0) ((raw - open) / open) * 100.0 else 0.0
                            val quoteVol = obj.optDouble("q", 0.0)
                            val existing = quotes[base]
                            // A 1:1 pair (divisor 1) wins over a 1000x or 1000000x bundle in the same frame.
                            if (existing == null || multiplier == 1.0) {
                                quotes[base] = Triple(unit, change, quoteVol)
                            }
                        }
                        if (quotes.isEmpty()) return
                        val todayStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(java.util.Date())
                        val nowMs = System.currentTimeMillis()
                        var updated = false
                        val newCoins = _coins.value.map { coin ->
                            val quote = quotes[coin.symbol.uppercase()] ?: return@map coin
                            val (price, change, quoteVol) = quote
                            if (price <= 0.0) return@map coin
                            updated = true
                            val isNewAth = price > coin.athUsd
                            stampLive(
                                coin,
                                coin.copy(
                                    priceUsd = price,
                                    change24h = change,
                                    volume24h = if (quoteVol > 0) quoteVol else coin.volume24h,
                                    athUsd = if (isNewAth) price else coin.athUsd,
                                    athDate = if (isNewAth) todayStr else coin.athDate
                                ),
                                nowMs
                            )
                        }
                        if (updated) {
                            _coins.value = newCoins
                            _isCacheStale.value = false
                            _lastUpdatedTimestamp.value = nowMs
                            com.example.util.PriceCacheManager.savePrices(newCoins)
                            syncCentralizedBtcPrice()
                        }
                    } catch (_: Exception) {}
                }
                override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?) {
                    _isLiveConnected.value = false
                    miniTickerHostIndex = (miniTickerHostIndex + 1) % miniTickerHosts.size
                    repoScope.launch {
                        kotlinx.coroutines.delay(2_000)
                        connectBinanceMiniTickerSocket()
                    }
                }
            })
        } catch (_: Exception) {}
    }

    private val _macroSignal = MutableStateFlow(MacroCycleSignal())
    val macroSignal: StateFlow<MacroCycleSignal> = _macroSignal.asStateFlow()

    private val _isProUnlocked = MutableStateFlow(false)
    val isProUnlocked: StateFlow<Boolean> = _isProUnlocked.asStateFlow()

    private val _selectedCurrency = MutableStateFlow(Currency.USD)
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _selectedTheme = MutableStateFlow(com.example.data.model.AppThemeOption.GALAXY_DARK)
    val selectedTheme: StateFlow<com.example.data.model.AppThemeOption> = _selectedTheme.asStateFlow()

    private val _lastUpdatedTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastUpdatedTimestamp: StateFlow<Long> = _lastUpdatedTimestamp.asStateFlow()

    private val _isLiveConnected = MutableStateFlow(false)
    val isLiveConnected: StateFlow<Boolean> = _isLiveConnected.asStateFlow()

    private val _priceSource = MutableStateFlow("Binance")
    val priceSource: StateFlow<String> = _priceSource.asStateFlow()

    private val initialBtc = _coins.value.firstOrNull { it.symbol.equals("BTC", ignoreCase = true) }
    private val initialEth = _coins.value.firstOrNull { it.symbol.equals("ETH", ignoreCase = true) }
    private val initialSol = _coins.value.firstOrNull { it.symbol.equals("SOL", ignoreCase = true) }

    private val _priceBus = MutableStateFlow(
        PriceBusState(
            btcSpot = PriceTick("BTCUSDT", "Binance", PriceKind.SPOT, 0.0, 0.0, tsMillis = 0L, source = "https://api.binance.com"),
            btcPerp = PriceTick("BTCUSDT", "Binance Futures", PriceKind.PERP, 0.0, 0.0, tsMillis = 0L, source = "https://fapi.binance.com"),
            btcMark = PriceTick("BTCUSDT", "Binance Futures", PriceKind.MARK, 0.0, 0.0, tsMillis = 0L, source = "https://fapi.binance.com"),
            ethSpot = PriceTick("ETHUSDT", "Binance", PriceKind.SPOT, 0.0, 0.0, tsMillis = 0L, source = "https://api.binance.com"),
            ethPerp = PriceTick("ETHUSDT", "Binance Futures", PriceKind.PERP, 0.0, 0.0, tsMillis = 0L, source = "https://fapi.binance.com"),
            solSpot = PriceTick("SOLUSDT", "Binance", PriceKind.SPOT, 0.0, 0.0, tsMillis = 0L, source = "https://api.binance.com"),
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
    )
    val priceBus: StateFlow<PriceBusState> = _priceBus.asStateFlow()

    private val _centralizedPriceState = MutableStateFlow(
        CentralizedPriceState(
            btcSpotPrice = 0.0,
            btcPerpPrice = 0.0,
            btcSpotChange24h = 0.0,
            btcPerpChange24h = 0.0,
            lastUpdatedTimestamp = System.currentTimeMillis(),
            isLiveConnected = false,
            primarySource = "Binance",
            priceBus = _priceBus.value
        )
    )
    val centralizedPriceState: StateFlow<CentralizedPriceState> = _centralizedPriceState.asStateFlow()

    /**
     * Records that [rebuilt] came from a live observation of [original].
     *
     * Returns [original] untouched when nothing actually moved and its existing stamp is still
     * comfortably fresh. That keeps repeated identical updates equal, so the conflating coins
     * StateFlow stops emitting and the coins -> updateLiveCoins -> applyCoinSnapshot -> coins
     * loop terminates instead of spinning.
     */
    private fun stampLive(original: CryptoCoin, rebuilt: CryptoCoin, nowMs: Long): CryptoCoin {
        val nothingMoved = rebuilt.priceUsd == original.priceUsd &&
            rebuilt.change24h == original.change24h &&
            rebuilt.volume24h == original.volume24h &&
            rebuilt.marketCap == original.marketCap
        val stampStillFresh = original.priceUpdatedAtMs > 0L &&
            (nowMs - original.priceUpdatedAtMs) < CryptoCoin.LIVE_PRICE_RESTAMP_MS
        return if (nothingMoved && stampStillFresh) {
            original
        } else {
            rebuilt.copy(priceUpdatedAtMs = nowMs, quoteState = QuoteState.LIVE)
        }
    }

    fun extractBaseSymbolAndMultiplier(rawSymbol: String): Pair<String, Double> = SymbolMath.canonical(rawSymbol)

    fun coinMatchesBaseSymbol(coin: CryptoCoin, baseSymbol: String): Boolean {
        val coinSym = coin.symbol.uppercase()
        val target = baseSymbol.uppercase()
        if (coinSym == target) return true
        if (coin.id.equals(target, ignoreCase = true)) return true
        if ((target == "FET" || target == "ASI") && (coinSym == "FET" || coinSym == "ASI")) return true
        if ((target == "POL" || target == "MATIC") && (coinSym == "POL" || coinSym == "MATIC")) return true
        if ((target == "RENDER" || target == "RNDR") && (coinSym == "RENDER" || coinSym == "RNDR")) return true
        if ((target == "PEPE" || target == "1000PEPE") && coinSym == "PEPE") return true
        if ((target == "SHIB" || target == "1000SHIB") && coinSym == "SHIB") return true
        if ((target == "BONK" || target == "1000BONK") && coinSym == "BONK") return true
        if ((target == "FLOKI" || target == "1000FLOKI") && coinSym == "FLOKI") return true
        if ((target == "LUNC" || target == "1000LUNC") && coinSym == "LUNC") return true
        return false
    }

    fun updatePriceTick(tick: PriceTick) {
        if (tick.price <= 0.0) return
        val (baseSymbol, multiplier) = extractBaseSymbolAndMultiplier(tick.symbol)
        val unitPrice = tick.price / multiplier

        val curBus = _priceBus.value
        val updatedTicks = curBus.liveTicks.toMutableMap()
        val adjustedTick = tick.copy(price = unitPrice)
        updatedTicks[baseSymbol] = adjustedTick

        val updatedBus = when (baseSymbol) {
            "BTC" -> when (tick.kind) {
                PriceKind.SPOT -> curBus.copy(btcSpot = adjustedTick, lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
                PriceKind.PERP -> curBus.copy(btcPerp = adjustedTick, lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
                PriceKind.MARK -> curBus.copy(btcMark = adjustedTick, lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
                PriceKind.INDEX -> curBus.copy(btcMark = curBus.btcMark.copy(price = unitPrice), lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
            }
            "ETH" -> when (tick.kind) {
                PriceKind.SPOT -> curBus.copy(ethSpot = adjustedTick, lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
                PriceKind.PERP -> curBus.copy(ethPerp = adjustedTick, lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
                else -> curBus.copy(ethSpot = adjustedTick, lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
            }
            "SOL" -> curBus.copy(solSpot = adjustedTick, lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
            else -> curBus.copy(lastUpdatedTimestamp = tick.tsMillis, liveTicks = updatedTicks)
        }
        _priceBus.value = updatedBus

        val curCent = _centralizedPriceState.value
        _centralizedPriceState.value = curCent.copy(
            btcSpotPrice = updatedBus.btcSpot.price,
            btcPerpPrice = if (updatedBus.btcPerp.price > 0.0) updatedBus.btcPerp.price else updatedBus.btcSpot.price,
            btcSpotChange24h = updatedBus.btcSpot.change24h,
            btcPerpChange24h = updatedBus.btcPerp.change24h,
            lastUpdatedTimestamp = tick.tsMillis,
            isLiveConnected = true,
            primarySource = tick.venue,
            priceBus = updatedBus
        )

        val isMarkOnly = tick.kind == PriceKind.MARK
        if (!isMarkOnly) {
            // Instant propagation across EVERY coin in _coins for traded/perp/spot prices
            val todayStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(java.util.Date())
            var coinUpdated = false
            val newCoins = _coins.value.map { coin ->
                if (coinMatchesBaseSymbol(coin, baseSymbol)) {
                    coinUpdated = true
                    val ch = if (tick.change24h != 0.0) tick.change24h else coin.change24h
                    val isNewAth = unitPrice > coin.athUsd
                    val divisor = if (ch <= -100.0) 0.0001 else (1.0 + ch / 100.0)
                    val openP = unitPrice / divisor
                    val intradaySpark = if (ch >= 0) {
                        listOf(openP, openP * 0.995, openP * 1.008, unitPrice * 0.998, unitPrice * 1.012, unitPrice)
                    } else {
                        listOf(openP, openP * 1.005, openP * 0.992, unitPrice * 1.008, unitPrice * 0.994, unitPrice)
                    }
                    stampLive(
                        coin,
                        coin.copy(
                            priceUsd = unitPrice,
                            change24h = ch,
                            volume24h = if ((tick.volumeQuote ?: 0.0) > 0) tick.volumeQuote!! else coin.volume24h,
                            athUsd = if (isNewAth) unitPrice else coin.athUsd,
                            athDate = if (isNewAth) todayStr else coin.athDate,
                            sparkline = if (intradaySpark.isNotEmpty()) intradaySpark else coin.sparkline
                        ),
                        tick.tsMillis
                    )
                } else {
                    coin
                }
            }
            if (coinUpdated) {
                _coins.value = newCoins
                _lastUpdatedTimestamp.value = tick.tsMillis
                com.example.util.PriceCacheManager.savePrices(newCoins)
            }
        }
    }

    fun updateCoinTradePrice(symbol: String, price: Double, tsMillis: Long = System.currentTimeMillis()) {
        if (price <= 0.0) return
        val (baseSymbol, multiplier) = extractBaseSymbolAndMultiplier(symbol)
        val unitPrice = price / multiplier

        val curBus = _priceBus.value
        val existingTick = curBus.liveTicks[baseSymbol]
        val updatedTick = (existingTick ?: PriceTick(
            symbol = symbol,
            venue = "Binance Futures",
            kind = PriceKind.PERP,
            price = unitPrice,
            tsMillis = tsMillis
        )).copy(price = unitPrice, tsMillis = tsMillis)

        val updatedTicks = curBus.liveTicks.toMutableMap()
        updatedTicks[baseSymbol] = updatedTick
        _priceBus.value = curBus.copy(liveTicks = updatedTicks, lastUpdatedTimestamp = tsMillis)

        val todayStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(java.util.Date())
        var coinUpdated = false
        val newCoins = _coins.value.map { coin ->
            if (coinMatchesBaseSymbol(coin, baseSymbol)) {
                coinUpdated = true
                val isNewAth = unitPrice > coin.athUsd
                stampLive(
                    coin,
                    coin.copy(
                        priceUsd = unitPrice,
                        athUsd = if (isNewAth) unitPrice else coin.athUsd,
                        athDate = if (isNewAth) todayStr else coin.athDate
                    ),
                    tsMillis
                )
            } else {
                coin
            }
        }
        if (coinUpdated) {
            _coins.value = newCoins
            _lastUpdatedTimestamp.value = tsMillis
            com.example.util.PriceCacheManager.savePrices(newCoins)
        }
    }

    fun updateBtcPriceUnified(
        price: Double,
        change24h: Double,
        high24h: Double? = null,
        low24h: Double? = null,
        volumeQuote: Double? = null,
        source: String = "Binance"
    ) {
        if (price <= 0.0) return
        val kind = if (source.contains("Futures", ignoreCase = true) || source.contains("fapi", ignoreCase = true)) {
            PriceKind.PERP
        } else {
            PriceKind.SPOT
        }
        val tick = PriceTick(
            symbol = "BTCUSDT",
            venue = "Binance",
            kind = kind,
            price = price,
            change24h = change24h,
            high24h = high24h,
            low24h = low24h,
            volumeQuote = volumeQuote,
            tsMillis = System.currentTimeMillis(),
            source = if (kind == PriceKind.PERP) "https://fapi.binance.com" else "https://api.binance.com"
        )
        updatePriceTick(tick)
    }

    private fun syncCentralizedBtcPrice() {
        val btc = _coins.value.firstOrNull { it.symbol.equals("BTC", ignoreCase = true) }
        val bus = _priceBus.value
        val perpP = if (bus.btcPerp.price > 0.0) bus.btcPerp.price else (btc?.takeIf { it.quoteState == QuoteState.LIVE }?.priceUsd ?: 0.0)
        val spotP = if (bus.btcSpot.price > 0.0) bus.btcSpot.price else (btc?.takeIf { it.quoteState == QuoteState.LIVE }?.priceUsd ?: 0.0)
        val cur = _centralizedPriceState.value
        _centralizedPriceState.value = cur.copy(
            btcSpotPrice = spotP,
            btcSpotChange24h = if (bus.btcSpot.change24h != 0.0) bus.btcSpot.change24h else (btc?.change24h ?: -0.32),
            btcPerpPrice = perpP,
            btcPerpChange24h = if (bus.btcPerp.change24h != 0.0) bus.btcPerp.change24h else (btc?.change24h ?: -0.32),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            isLiveConnected = _isLiveConnected.value,
            primarySource = _priceSource.value
        )
    }

    fun updatePerpPrice(perpPrice: Double, perpChange: Double) {
        if (perpPrice > 0.0) {
            val tick = PriceTick(
                symbol = "BTCUSDT",
                venue = "Binance Futures",
                kind = PriceKind.PERP,
                price = perpPrice,
                change24h = perpChange,
                tsMillis = System.currentTimeMillis(),
                source = "https://fapi.binance.com"
            )
            updatePriceTick(tick)
        }
    }

    suspend fun fetchBinanceBtcTickerDirect(): Boolean = withContext(Dispatchers.IO) {
        var anySuccess = false
        // 1. Direct Binance Spot 24hr Ticker API for BTC, ETH, SOL
        val symbols = listOf("BTCUSDT", "ETHUSDT", "SOLUSDT")
        for (sym in symbols) {
            try {
                val res = MarketDataClient.getText(
                    listOf(
                        "https://data-api.binance.vision/api/v3/ticker/24hr?symbol=$sym",
                        "https://api.binance.com/api/v3/ticker/24hr?symbol=$sym"
                    ),
                    attempts = 2
                )
                if (!res.isNullOrBlank()) {
                    val obj = JSONObject(res)
                    val lastPrice = obj.optDouble("lastPrice", 0.0)
                    val priceChangePercent = obj.optDouble("priceChangePercent", 0.0)
                    val quoteVolume = obj.optDouble("quoteVolume", 0.0)
                    val highPrice = obj.optDouble("highPrice", lastPrice)
                    val lowPrice = obj.optDouble("lowPrice", lastPrice)
                    if (lastPrice > 0.0) {
                        updatePriceTick(
                            PriceTick(
                                symbol = sym,
                                venue = "Binance",
                                kind = PriceKind.SPOT,
                                price = lastPrice,
                                change24h = priceChangePercent,
                                high24h = highPrice,
                                low24h = lowPrice,
                                volumeQuote = quoteVolume,
                                tsMillis = System.currentTimeMillis(),
                                source = "https://api.binance.com"
                            )
                        )
                        anySuccess = true
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Direct Binance Futures 24hr Ticker API for BTC & ETH PERP
        val perpSymbols = listOf("BTCUSDT", "ETHUSDT")
        for (sym in perpSymbols) {
            try {
                val res = MarketDataClient.getText(
                    listOf(
                        "https://www.binance.com/fapi/v1/ticker/24hr?symbol=$sym",
                        "https://fapi.binance.com/fapi/v1/ticker/24hr?symbol=$sym"
                    ),
                    attempts = 2
                )
                if (!res.isNullOrBlank()) {
                    val obj = JSONObject(res)
                    val lastPrice = obj.optDouble("lastPrice", 0.0)
                    val priceChangePercent = obj.optDouble("priceChangePercent", 0.0)
                    val quoteVolume = obj.optDouble("quoteVolume", 0.0)
                    val highPrice = obj.optDouble("highPrice", lastPrice)
                    val lowPrice = obj.optDouble("lowPrice", lastPrice)
                    if (lastPrice > 0.0) {
                        updatePriceTick(
                            PriceTick(
                                symbol = sym,
                                venue = "Binance Futures",
                                kind = PriceKind.PERP,
                                price = lastPrice,
                                change24h = priceChangePercent,
                                high24h = highPrice,
                                low24h = lowPrice,
                                volumeQuote = quoteVolume,
                                tsMillis = System.currentTimeMillis(),
                                source = "https://fapi.binance.com"
                            )
                        )
                        anySuccess = true
                    }
                }
            } catch (_: Exception) {}
        }

        anySuccess
    }


    fun setProUnlocked(unlocked: Boolean) {
        _isProUnlocked.value = unlocked
    }

    fun setCurrency(currency: Currency) {
        _selectedCurrency.value = currency
    }

    fun setLanguage(language: AppLanguage) {
        _selectedLanguage.value = language
    }

    fun setTheme(theme: com.example.data.model.AppThemeOption) {
        _selectedTheme.value = theme
    }

    fun toggleFavorite(coinId: String) {
        _coins.value = _coins.value.map { coin ->
            if (coin.id == coinId) coin.copy(isFavorite = !coin.isFavorite) else coin
        }
    }

    suspend fun refreshLivePrices() {
        withContext(Dispatchers.IO) {
            // Guarantee Bitcoin is updated with the real-time unified Binance price first
            fetchBinanceBtcTickerDirect()

            // Spot ticker from the vision mirror (api.binance.com returns 451 in some regions),
            // then USDT-M futures for assets that only have a perpetual.
            var success = false
            try {
                ExchangeDirectory.ensureLoaded()
                val spotBody = MarketDataClient.getText(
                    listOf(
                        "https://data-api.binance.vision/api/v3/ticker/24hr",
                        "https://api.binance.com/api/v3/ticker/24hr"
                    )
                )
                val futuresBody = MarketDataClient.getText(
                    listOf(
                        "https://www.binance.com/fapi/v1/ticker/24hr",
                        "https://fapi.binance.com/fapi/v1/ticker/24hr"
                    )
                )
                val spotRows = parseTickerRows(spotBody)
                val futuresRows = parseTickerRows(futuresBody)
                spotRows["EURUSDT"]?.let { row ->
                    if (row.price in 0.5..2.0) Currency.EUR.rateToUsd = 1.0 / row.price
                }
                spotRows["GBPUSDT"]?.let { row ->
                    if (row.price in 0.5..2.0) Currency.GBP.rateToUsd = 1.0 / row.price
                }
                val spotTrading = ExchangeDirectory.spotPairs().ifEmpty { spotRows.keys }
                val futuresTrading = ExchangeDirectory.futuresPairs().ifEmpty { futuresRows.keys }
                val priceMap = mutableMapOf<String, Pair<Double, Double>>()
                val volumeMap = mutableMapOf<String, Double>()
                for (coin in _coins.value) {
                    val listing = SymbolMath.spotFirstListing(coin.symbol, spotTrading, futuresTrading) ?: continue
                    val row = if (listing.spot) spotRows[listing.pair] else futuresRows[listing.pair]
                    if (row == null || row.price <= 0.0) continue
                    val unit = row.price / listing.divisor
                    if (unit <= 0.0) continue
                    priceMap[coin.symbol.uppercase()] = Pair(unit, row.changePct)
                    if (row.quoteVolume > 0.0) volumeMap[coin.symbol.uppercase()] = row.quoteVolume
                }

                if (priceMap.isNotEmpty()) {
                        val todayStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(java.util.Date())
                        val curBus = _priceBus.value
                        val newTicks = curBus.liveTicks.toMutableMap()
                        val nowMs = System.currentTimeMillis()
                        priceMap.forEach { (sym, data) ->
                            val (p, ch) = data
                            if (p > 0.0) {
                                val existing = newTicks[sym]
                                if (existing == null || existing.isStale) {
                                    newTicks[sym] = PriceTick(
                                        symbol = "${sym}USDT",
                                        venue = "Binance",
                                        kind = PriceKind.SPOT,
                                        price = p,
                                        change24h = ch,
                                        volumeQuote = volumeMap[sym],
                                        tsMillis = nowMs,
                                        source = "https://api.binance.com"
                                    )
                                }
                            }
                        }
                        _priceBus.value = curBus.copy(liveTicks = newTicks)

                        _coins.value = applyPriceBusToCoins(_coins.value.map { coin ->
                            val isBtc = coin.symbol.equals("BTC", ignoreCase = true) || coin.id.equals("bitcoin", ignoreCase = true)
                            val symKey = coin.symbol.uppercase()
                            val live = priceMap[symKey] 
                                ?: if (symKey == "FET") priceMap["ASI"] else null
                                ?: if (symKey == "POL") priceMap["MATIC"] else null
                                ?: if (symKey == "RENDER") priceMap["RNDR"] else null
                            if (live != null) {
                                val newPrice = if (isBtc && curBus.btcPerp.price > 0.0 && !curBus.btcPerp.isStale) {
                                    curBus.btcPerp.price
                                } else {
                                    live.first
                                }
                                val newChange = if (isBtc && curBus.btcPerp.change24h != 0.0 && !curBus.btcPerp.isStale) {
                                    curBus.btcPerp.change24h
                                } else {
                                    live.second
                                }
                                val newVolume = quotedVolume(symKey, volumeMap) ?: coin.volume24h
                                val isNewAth = newPrice > coin.athUsd
                                val updatedAth = if (isNewAth) newPrice else coin.athUsd
                                val updatedAthDate = if (isNewAth) todayStr else coin.athDate
                                val openPrice = newPrice / (1.0 + newChange / 100.0)
                                val intradaySpark = if (newChange >= 0) {
                                    listOf(openPrice, openPrice * 0.995, openPrice * 1.008, newPrice * 0.998, newPrice * 1.012, newPrice)
                                } else {
                                    listOf(openPrice, openPrice * 1.005, openPrice * 0.992, newPrice * 1.008, newPrice * 0.994, newPrice)
                                }
                                stampLive(
                                    coin,
                                    coin.copy(
                                        priceUsd = newPrice,
                                        change24h = newChange,
                                        volume24h = if (newVolume > 0) newVolume else coin.volume24h,
                                        athUsd = updatedAth,
                                        athDate = updatedAthDate,
                                        sparkline = intradaySpark
                                    ),
                                    nowMs
                                )
                            } else {
                                coin
                            }
                        })
                        _lastUpdatedTimestamp.value = System.currentTimeMillis()
                        _isLiveConnected.value = true
                        _priceSource.value = "Binance"
                        syncCentralizedBtcPrice()
                        success = true
                        // Binance has no market for every tracked asset, and its ticker has no
                        // market cap. CoinGecko fills both, without replacing a fresh Binance price.
                        val now = System.currentTimeMillis()
                        val dueForCaps = now - lastMarketCapRefreshMs > 60_000L
                        val ids = if (dueForCaps) {
                            _coins.value.map { it.id }
                        } else {
                            _coins.value.filter { it.quoteState != QuoteState.LIVE }.map { it.id }
                        }
                        if (ids.isNotEmpty() && fetchCoinGeckoSimplePrices(ids, asPrimarySource = false) && dueForCaps) {
                            lastMarketCapRefreshMs = now
                        }
                    }
            } catch (_: Exception) {
                // Ignore and try fallback
            }

            if (!success) {
                try {
                    val forexRes = MarketDataClient.getText("https://open.er-api.com/v6/latest/USD", attempts = 2)
                    if (!forexRes.isNullOrBlank()) {
                        val rates = JSONObject(forexRes).optJSONObject("rates")
                        if (rates != null) {
                            val eur = rates.optDouble("EUR", 0.0)
                            val gbp = rates.optDouble("GBP", 0.0)
                            if (eur in 0.5..2.0) Currency.EUR.rateToUsd = eur
                            if (gbp in 0.5..2.0) Currency.GBP.rateToUsd = gbp
                        }
                    }
                } catch (_: Exception) {}

                try {
                    // Fallback to CoinGecko Markets API for Top 100 metadata/sparklines
                    val response = MarketDataClient.getText(
                        "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=250&page=1&sparkline=true",
                        attempts = 2
                    )
                    if (!response.isNullOrBlank()) {
                        val jsonArray = org.json.JSONArray(response)
                        val marketMap = mutableMapOf<String, JSONObject>()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val id = item.optString("id")
                            val symbol = item.optString("symbol").uppercase()
                            if (id.isNotBlank()) marketMap[id] = item
                            if (symbol.isNotBlank()) marketMap[symbol] = item
                        }

                        _coins.value = applyPriceBusToCoins(_coins.value.map { coin ->
                            val isBtc = coin.symbol.equals("BTC", ignoreCase = true) || coin.id.equals("bitcoin", ignoreCase = true)
                            val item = marketMap[coin.id] 
                                ?: marketMap[coin.symbol.uppercase()]
                                ?: (if (coin.symbol.equals("FET", ignoreCase = true)) marketMap["artificial-superintelligence-alliance"] ?: marketMap["fetch-ai"] ?: marketMap["ASI"] else null)
                            if (item != null) {
                                val geckoPrice = item.optDouble("current_price", coin.priceUsd)
                                val geckoChange = item.optDouble("price_change_percentage_24h", coin.change24h)
                                val liveVol = item.optDouble("total_volume", coin.volume24h)
                                val liveAth = item.optDouble("ath", coin.athUsd)
                                val liveMcap = item.optDouble("market_cap", coin.marketCap)
                                
                                val rawAthDate = item.optString("ath_date", "")
                                var formattedAthDate = coin.athDate
                                if (rawAthDate.length >= 10) {
                                    try {
                                        val iso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).parse(rawAthDate.substring(0, 10))
                                        if (iso != null) {
                                            formattedAthDate = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(iso)
                                        }
                                    } catch (_: Exception) {}
                                }

                                val sparklineObj = item.optJSONObject("sparkline_in_7d")
                                val sparklineArray = sparklineObj?.optJSONArray("price")
                                val parsedSparkline = if (sparklineArray != null && sparklineArray.length() > 0) {
                                    (0 until sparklineArray.length()).map { sparklineArray.getDouble(it) }
                                } else emptyList()

                                // Defer to the existing price only while it is genuinely fresh,
                                // so a Binance tick wins but a stale one never blocks CoinGecko.
                                val keepExisting = coin.isLivePrice
                                val finalPrice = if (keepExisting) coin.priceUsd else geckoPrice
                                val finalChange = if (keepExisting) coin.change24h else geckoChange

                                stampLive(
                                    coin,
                                    coin.copy(
                                        priceUsd = finalPrice,
                                        change24h = finalChange,
                                        volume24h = if (liveVol > 0) liveVol else coin.volume24h,
                                        marketCap = if (liveMcap > 0) liveMcap else coin.marketCap,
                                        athUsd = if (liveAth > 0) liveAth else coin.athUsd,
                                        athDate = formattedAthDate,
                                        sparkline = if (parsedSparkline.isNotEmpty()) parsedSparkline else coin.sparkline
                                    ),
                                    System.currentTimeMillis()
                                )
                            } else {
                                coin
                            }
                        })
                        _lastUpdatedTimestamp.value = System.currentTimeMillis()
                        _isLiveConnected.value = true
                        _priceSource.value = "CoinGecko / Binance"
                        success = true
                    }
                } catch (_: Exception) {
                    // Ignore and fall through to the per-id lookup
                }
            }

            if (!success) {
                // api.coincap.io/v2 was retired and no longer resolves in DNS, so the last tier is
                // CoinGecko's per-id endpoint. Unlike /coins/markets it is not limited to a page of
                // the top 250, so it can reach coins of any rank.
                success = fetchCoinGeckoSimplePrices(_coins.value.map { it.id })
                if (!success) {
                    _isLiveConnected.value = false
                }
            }

            _coins.value = _coins.value.map { coin ->
                if (coin.quoteState == QuoteState.PENDING) coin.copy(quoteState = QuoteState.UNAVAILABLE) else coin
            }
        }
    }

    private data class TickerRow(val price: Double, val changePct: Double, val quoteVolume: Double)

    /** Last price, 24h percent and quote volume keyed by the full USDT pair. */
    private fun parseTickerRows(body: String?): Map<String, TickerRow> {
        if (body.isNullOrBlank()) return emptyMap()
        return try {
            val arr = org.json.JSONArray(body)
            buildMap {
                for (i in 0 until arr.length()) {
                    val item = arr.optJSONObject(i) ?: continue
                    val sym = item.optString("symbol")
                    if (!sym.endsWith("USDT")) continue
                    val price = item.optDouble("lastPrice", 0.0)
                    if (price <= 0.0) continue
                    put(
                        sym,
                        TickerRow(
                            price = price,
                            changePct = item.optDouble("priceChangePercent", 0.0),
                            quoteVolume = item.optDouble("quoteVolume", 0.0)
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    /**
     * Looks prices up by CoinGecko id in batches. Returns true if at least one coin was updated.
     */
    private suspend fun fetchCoinGeckoSimplePrices(
        ids: List<String>,
        asPrimarySource: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        val wanted = ids.filter { it.isNotBlank() }.distinct()
        if (wanted.isEmpty()) return@withContext false

        val quotes = mutableMapOf<String, JSONObject>()
        for (batch in wanted.chunked(COINGECKO_ID_BATCH_SIZE)) {
            try {
                val joined = batch.joinToString(",") { java.net.URLEncoder.encode(it, "UTF-8") }
                val response = MarketDataClient.getText(
                    "https://api.coingecko.com/api/v3/simple/price?ids=$joined&vs_currencies=usd" +
                        "&include_24hr_vol=true&include_24hr_change=true&include_market_cap=true",
                    attempts = 2
                )
                if (!response.isNullOrBlank()) {
                    val json = JSONObject(response)
                    for (key in json.keys()) {
                        json.optJSONObject(key)?.let { quotes[key.lowercase()] = it }
                    }
                }
            } catch (_: Exception) {
                // Skip this batch and keep whatever the others returned
            }
        }
        if (quotes.isEmpty()) return@withContext false

        val nowMs = System.currentTimeMillis()
        _coins.value = applyPriceBusToCoins(
            _coins.value.map { coin ->
                val quote = quotes[coin.id.lowercase()] ?: return@map coin
                val price = quote.optDouble("usd", 0.0)
                if (price <= 0.0) return@map coin

                val change = quote.optDouble("usd_24h_change", coin.change24h)
                val vol = quote.optDouble("usd_24h_vol", coin.volume24h)
                val mcap = quote.optDouble("usd_market_cap", coin.marketCap)
                val keepExisting = coin.isLivePrice

                stampLive(
                    coin,
                    coin.copy(
                        priceUsd = if (keepExisting) coin.priceUsd else price,
                        change24h = if (keepExisting) coin.change24h else change,
                        volume24h = if (keepExisting) coin.volume24h else if (vol > 0) vol else coin.volume24h,
                        marketCap = if (mcap > 0) mcap else coin.marketCap,
                        athUsd = if (price > coin.athUsd) price else coin.athUsd
                    ),
                    nowMs
                )
            }
        )
        _lastUpdatedTimestamp.value = nowMs
        _isLiveConnected.value = true
        if (asPrimarySource) {
            _priceSource.value = "CoinGecko"
        }
        true
    }

    /** Binance volume for this ticker, following only that ticker's own alias. */
    private fun quotedVolume(symbol: String, volumeMap: Map<String, Double>): Double? {
        val sym = symbol.uppercase()
        volumeMap[sym]?.takeIf { it > 0.0 }?.let { return it }
        val alias = when (sym) {
            "FET" -> "ASI"
            "ASI" -> "FET"
            "POL" -> "MATIC"
            "MATIC" -> "POL"
            "RENDER" -> "RNDR"
            "RNDR" -> "RENDER"
            else -> null
        }
        return alias?.let { volumeMap[it] }?.takeIf { it > 0.0 }
    }

    private fun applyPriceBusToCoins(coinsList: List<CryptoCoin>): List<CryptoCoin> {
        val bus = _priceBus.value
        val todayStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH).format(java.util.Date())
        val nowMs = System.currentTimeMillis()
        return coinsList.map { coin ->
            val (baseSym, _) = extractBaseSymbolAndMultiplier(coin.symbol)
            val liveTick = bus.liveTicks[baseSym]
                ?: (if (baseSym == "BTC") (if (bus.btcPerp.price > 0.0) bus.btcPerp else bus.btcSpot) else if (baseSym == "ETH") (if (bus.ethPerp.price > 0.0) bus.ethPerp else bus.ethSpot) else if (baseSym == "SOL") bus.solSpot else null)

            if (liveTick != null && liveTick.price > 0.0 && !liveTick.isStale) {
                val p = liveTick.price
                val ch = if (liveTick.change24h != 0.0) liveTick.change24h else coin.change24h
                val isNewAth = p > coin.athUsd
                val divisor = if (ch <= -100.0) 0.0001 else (1.0 + ch / 100.0)
                val openP = p / divisor
                val intradaySpark = if (ch >= 0) {
                    listOf(openP, openP * 0.995, openP * 1.008, p * 0.998, p * 1.012, p)
                } else {
                    listOf(openP, openP * 1.005, openP * 0.992, p * 1.008, p * 0.994, p)
                }
                stampLive(
                    coin,
                    coin.copy(
                        priceUsd = p,
                        change24h = ch,
                        volume24h = if ((liveTick.volumeQuote ?: 0.0) > 0) liveTick.volumeQuote!! else coin.volume24h,
                        athUsd = if (isNewAth) p else coin.athUsd,
                        athDate = if (isNewAth) todayStr else coin.athDate,
                        sparkline = if (intradaySpark.isNotEmpty()) intradaySpark else coin.sparkline
                    ),
                    nowMs
                )
            } else {
                coin
            }
        }
    }

    suspend fun queryLiveTokenData(rawQuery: String): String? = withContext(Dispatchers.IO) {
        val cleanQuery = rawQuery.lowercase(java.util.Locale.ROOT)
        val coins = _coins.value

        // 1. Check local database match
        val matched = coins.filter { coin ->
            val sym = coin.symbol.lowercase(java.util.Locale.ROOT)
            val name = coin.name.lowercase(java.util.Locale.ROOT)
            com.example.data.model.queryMentionsSymbol(cleanQuery, sym) || cleanQuery.contains(name) ||
            (sym == "xrp" && (cleanQuery.contains("ριπλ") || cleanQuery.contains("ripple") || cleanQuery.contains("xrp"))) ||
            (sym == "sol" && (cleanQuery.contains("σολανα") || cleanQuery.contains("solana") || cleanQuery.contains("sol"))) ||
            (sym == "eth" && (cleanQuery.contains("αιθεριο") || cleanQuery.contains("ethereum") || cleanQuery.contains("eth"))) ||
            (sym == "btc" && (cleanQuery.contains("μπιτκοιν") || cleanQuery.contains("bitcoin") || cleanQuery.contains("btc"))) ||
            (sym == "ada" && (cleanQuery.contains("καρντανο") || cleanQuery.contains("cardano") || cleanQuery.contains("ada"))) ||
            (sym == "doge" && (cleanQuery.contains("ντοτζ") || cleanQuery.contains("dogecoin") || cleanQuery.contains("doge"))) ||
            (sym == "sui" && (cleanQuery.contains("σουι") || cleanQuery.contains("sui"))) ||
            (sym == "kas" && (cleanQuery.contains("κασπα") || cleanQuery.contains("kaspa") || cleanQuery.contains("kas"))) ||
            (sym == "pepe" && (cleanQuery.contains("πεπε") || cleanQuery.contains("pepe")))
        }

        if (matched.isNotEmpty()) {
            return@withContext matched.joinToString("\n\n") { coin ->
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
        }

        // 2. Extract potential crypto ticker from query and query Binance Public API on the fly
        val words = rawQuery.split(Regex("[^a-zA-Z0-9]")).filter { it.length in 2..8 }
        for (word in words) {
            val upper = word.uppercase(java.util.Locale.US)
            if (upper in listOf("THE", "AND", "FOR", "NOW", "WHAT", "HOW", "WHY", "CAN", "YOU", "ARE", "PRICE", "LIVE", "COIN", "TIME", "DATE", "WITH", "THIS", "THAT", "POIA", "EINAI", "POSO", "KANEI", "TWRA", "EXEI", "TIMH")) continue
            try {
                val response = MarketDataClient.getText(
                    listOf(
                        "https://data-api.binance.vision/api/v3/ticker/24hr?symbol=${upper}USDT",
                        "https://api.binance.com/api/v3/ticker/24hr?symbol=${upper}USDT"
                    ),
                    attempts = 2
                )
                if (!response.isNullOrBlank()) {
                    val item = JSONObject(response)
                    val lastPrice = item.optDouble("lastPrice", 0.0)
                    val priceChangePercent = item.optDouble("priceChangePercent", 0.0)
                    val quoteVolume = item.optDouble("quoteVolume", 0.0)
                    val highPrice = item.optDouble("highPrice", lastPrice)
                    val lowPrice = item.optDouble("lowPrice", lastPrice)
                    if (lastPrice > 0) {
                        val lang = com.example.util.AppNumberFormatter.currentLanguage
                        val formattedPrice = com.example.util.AppNumberFormatter.formatPrice(lastPrice, language = lang)
                        val sign = if (priceChangePercent >= 0) "+" else ""
                        val formattedChange = com.example.util.AppNumberFormatter.formatPercent(priceChangePercent, includeSign = false, language = lang).removeSuffix("%")
                        val formattedVol = com.example.util.AppNumberFormatter.formatCompactCurrency(quoteVolume, language = lang)
                        val formattedHigh = com.example.util.AppNumberFormatter.formatPrice(highPrice, language = lang)
                        val formattedLow = com.example.util.AppNumberFormatter.formatPrice(lowPrice, language = lang)
                        return@withContext "• COIN: $upper (Binance Spot)\n  - Live Spot Price: $formattedPrice USD\n  - 24h Change: $sign$formattedChange%\n  - 24h Range: $formattedLow - $formattedHigh\n  - 24h Volume: $formattedVol USD"
                    }
                }
            } catch (_: Exception) {}
        }

        null
    }
}
