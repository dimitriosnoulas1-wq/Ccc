package com.example.data.repository

import com.example.data.model.GlobalRiskSnapshot
import com.example.data.model.LiveMovingAverages
import com.example.data.network.MarketDataClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class LiveMacroFeedsRepository(
    private val scope: CoroutineScope
) {
    private val _movingAverages = MutableStateFlow(LiveMovingAverages())
    val movingAverages: StateFlow<LiveMovingAverages> = _movingAverages.asStateFlow()

    private val _globalRisk = MutableStateFlow(GlobalRiskSnapshot())
    val globalRisk: StateFlow<GlobalRiskSnapshot> = _globalRisk.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        scope.launch(Dispatchers.IO) {
            runCatching { _movingAverages.value = fetchMovingAverages() }
            runCatching { _globalRisk.value = fetchGlobalRisk() }
        }
    }

    private fun fetchMovingAverages(): LiveMovingAverages {
        val daily = fetchCloses("https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1d&limit=500")
        val weekly = fetchCloses("https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1w&limit=220")
        if (daily.size < 111) return LiveMovingAverages()
        return LiveMovingAverages(
            dma111 = sma(daily, 111),
            dma350 = sma(daily, 350),
            ema150 = ema(daily, 150),
            sma471 = sma(daily, 471),
            sma200d = sma(daily, 200),
            ma200w = sma(weekly, 200),
            isLive = true
        )
    }

    private fun fetchGlobalRisk(): GlobalRiskSnapshot {
        val dxy = fetchYahoo("DX-Y.NYB")
        val tnx = fetchYahoo("%5ETNX")
        val brent = fetchYahoo("BZ=F")
        if (dxy == null && tnx == null && brent == null) return GlobalRiskSnapshot()
        return GlobalRiskSnapshot(
            dxy = dxy?.first,
            dxyChangePct = dxy?.second,
            us10y = tnx?.first,
            us10yChangeBps = tnx?.let { it.second * 100.0 },
            brent = brent?.first,
            brentChangePct = brent?.second,
            isLive = true,
            asOfMs = System.currentTimeMillis()
        )
    }

    private fun fetchCloses(url: String): List<Double> {
        val body = MarketDataClient.getText(url, attempts = 2) ?: return emptyList()
        val arr = JSONArray(body)
        val closes = ArrayList<Double>(arr.length())
        for (i in 0 until arr.length()) {
            val candle = arr.optJSONArray(i) ?: continue
            val close = candle.optDouble(4, Double.NaN)
            if (!close.isNaN() && close > 0.0) closes.add(close)
        }
        return closes
    }

    private fun fetchYahoo(symbol: String): Pair<Double, Double>? {
        val url = "https://query1.finance.yahoo.com/v8/finance/chart/$symbol?interval=1d&range=5d"
        val body = MarketDataClient.getText(url, attempts = 2) ?: return null
        val meta = JSONObject(body)
            .optJSONObject("chart")
            ?.optJSONArray("result")
            ?.optJSONObject(0)
            ?.optJSONObject("meta")
            ?: return null
        val price = meta.optDouble("regularMarketPrice", Double.NaN)
        if (price.isNaN() || price <= 0.0) return null
        val prev = meta.optDouble("chartPreviousClose", price)
        val changePct = if (prev > 0.0) ((price - prev) / prev) * 100.0 else 0.0
        return price to changePct
    }

    private fun sma(values: List<Double>, period: Int): Double? {
        if (values.size < period) return null
        return values.takeLast(period).average()
    }

    private fun ema(values: List<Double>, period: Int): Double? {
        if (values.size < period) return sma(values, values.size)
        val k = 2.0 / (period + 1)
        var ema = values.take(period).average()
        for (i in period until values.size) {
            ema = values[i] * k + ema * (1 - k)
        }
        return ema
    }
}
