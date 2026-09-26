package com.example.util

import android.content.Context
import com.example.data.engine.CycleCommandEngine
import com.example.data.model.BitcoinEtfFlowData
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.MarketRegime
import com.example.data.model.StablecoinLiquidityData
import com.example.data.network.MarketDataClient
import com.example.data.repository.DailyCycleLogRepository
import com.example.ui.rainbow.DateUtil
import com.example.ui.rainbow.RainbowModel
import org.json.JSONArray
import org.json.JSONObject

/**
 * Runs the Settings push alerts from the background worker: Rainbow band, market-mood zone,
 * Pi Cycle, 200W SMA and funding. Each alert remembers its last state and notifies only when
 * that state changes. Free users get the first two cycle alerts, as in Settings.
 */
object CycleAlertDispatcher {
    private const val RAINBOW = "rainbow"
    private const val ZONE = "zone"
    private const val PI = "pi"
    private const val SMA = "sma200w"
    private const val FUNDING = "funding"
    private const val FUNDING_MIN_GAP_MS = 6 * 60 * 60 * 1000L

    fun run(context: Context) {
        val ctx = context.applicationContext
        val settings = AlertSettingsStore.load(ctx)
        val greek = CycleDayAlertPrefs.isGreek(ctx)
        val cycleOrder = listOf(
            ZONE to settings.notifyZoneChange,
            PI to settings.notifyPiCycle,
            RAINBOW to settings.notifyRainbowBand,
            SMA to settings.notify200wSma
        )
        val allowedCount = if (CycleDayAlertPrefs.isProCached(ctx)) cycleOrder.size else 2
        val allowed = cycleOrder.filter { it.second }.map { it.first }.take(allowedCount).toSet()
        val fundingOn = settings.notifyFunding

        if (allowed.isEmpty() && !fundingOn) {
            (cycleOrder.map { it.first } + FUNDING).forEach { AlertSettingsStore.setLastState(ctx, it, null) }
            return
        }

        val premium = fetchPremium()
        val price = premium?.first ?: 0.0
        val fundingRate = premium?.second
        val day = DailyCycleLogRepository.dayKey(System.currentTimeMillis())

        step(ctx, RAINBOW, RAINBOW in allowed, price.takeIf { it > 0.0 }?.let { rainbowBand(it) }, price, day) { prev, cur ->
            CycleAlertRules.rainbowMessage(prev, cur, price, greek)
        }

        val zone = if (ZONE in allowed && price > 0.0 && fundingRate != null) {
            fetchFearGreed()?.let { fg -> zoneOf(price, fundingRate, fg) }
        } else null
        step(ctx, ZONE, ZONE in allowed, zone?.name, price, day) { prev, cur ->
            val from = runCatching { MarketRegime.valueOf(prev) }.getOrNull()
            val to = MarketRegime.valueOf(cur)
            CycleAlertRules.zoneMessage(
                from?.let { if (greek) it.titleEl else it.titleEn } ?: prev,
                if (greek) to.titleEl else to.titleEn,
                greek
            )
        }

        val needMas = (PI in allowed || SMA in allowed) && AlertSettingsStore.lastRun(ctx, "mas") != day
        if (needMas) {
            val daily = fetchCloses("https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1d&limit=400")
            val weekly = fetchCloses("https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1w&limit=210")
            val dma111 = CycleAlertRules.sma(daily, 111)
            val dma350x2 = CycleAlertRules.sma(daily, 350)?.times(2.0)
            val ma200w = CycleAlertRules.sma(weekly, 200)
            val spot = price.takeIf { it > 0.0 } ?: daily.lastOrNull() ?: 0.0

            val pi = if (dma111 != null && dma350x2 != null) CycleAlertRules.piState(dma111, dma350x2) else null
            step(ctx, PI, PI in allowed, pi?.name, spot, day) { _, cur ->
                CycleAlertRules.piMessage(CycleAlertRules.PiState.valueOf(cur), dma111!!, dma350x2!!, greek)
            }
            val sma = if (ma200w != null && spot > 0.0) CycleAlertRules.smaState(spot, ma200w) else null
            step(ctx, SMA, SMA in allowed, sma?.name, spot, day) { _, cur ->
                CycleAlertRules.smaMessage(CycleAlertRules.SmaState.valueOf(cur), spot, ma200w!!, greek)
            }
            if (pi != null || sma != null) AlertSettingsStore.setLastRun(ctx, "mas", day)
        } else {
            if (PI !in allowed) AlertSettingsStore.setLastState(ctx, PI, null)
            if (SMA !in allowed) AlertSettingsStore.setLastState(ctx, SMA, null)
        }

        val fundingPct = fundingRate?.times(100.0)
        val lastFundingSent = AlertSettingsStore.lastRun(ctx, "funding_sent").toLongOrNull() ?: 0L
        val fundingQuiet = System.currentTimeMillis() - lastFundingSent < FUNDING_MIN_GAP_MS
        step(
            ctx, FUNDING, fundingOn, fundingPct?.let { CycleAlertRules.fundingState(it).name }, price, day,
            channel = NotificationHelper.CHANNEL_FUNDING, type = "FUNDING", muted = fundingQuiet
        ) { _, cur ->
            AlertSettingsStore.setLastRun(ctx, "funding_sent", System.currentTimeMillis().toString())
            CycleAlertRules.fundingMessage(CycleAlertRules.FundingState.valueOf(cur), fundingPct!!, greek)
        }
    }

    private fun step(
        ctx: Context,
        key: String,
        enabled: Boolean,
        current: String?,
        price: Double,
        day: String,
        channel: String = NotificationHelper.CHANNEL_CYCLE,
        type: String = "CYCLE",
        muted: Boolean = false,
        message: (prev: String, cur: String) -> CycleAlertRules.Message
    ) {
        if (!enabled) {
            AlertSettingsStore.setLastState(ctx, key, null)
            return
        }
        if (current == null) return
        val prev = AlertSettingsStore.lastState(ctx, key)
        AlertSettingsStore.setLastState(ctx, key, current)
        if (prev == null || !CycleAlertRules.changed(prev, current) || muted) return
        if (!NotificationHelper.canSendNotifications(ctx)) return
        val greek = CycleDayAlertPrefs.isGreek(ctx)
        val m = message(prev, current)
        NotificationHelper.recordAndNotify(
            context = ctx,
            eventId = "$key-$prev-$current-$day",
            channelId = channel,
            title = m.title,
            body = m.body,
            details = CycleReadingText.disclaimer(greek),
            navTab = "MARKETS",
            type = type,
            btcPrice = price
        )
    }

    private fun rainbowBand(price: Double): String =
        RainbowModel.BANDS[RainbowModel.bandIndex(DateUtil.today(), price)].name

    private fun zoneOf(price: Double, fundingRate: Double, fearGreed: Int): MarketRegime {
        val now = System.currentTimeMillis()
        val funding = FuturesMarkFunding(
            symbol = "BTCUSDT",
            markPrice = price,
            indexPrice = null,
            basis = null,
            basisPercent = null,
            fundingRate = fundingRate,
            nextFundingTimeMs = null,
            approxApr = null,
            eventTimeMs = now,
            receivedTimeMs = now,
            fromExchange = true
        )
        return CycleCommandEngine.computeCycleCommandState(
            btcPrice = price,
            etfFlowData = BitcoinEtfFlowData(),
            liquidityData = StablecoinLiquidityData(),
            futuresMarkFunding = funding,
            fearGreedScore = fearGreed
        ).regime
    }

    /** Binance BTCUSDT mark price and last funding rate (as a fraction), or null if either is missing. */
    private fun fetchPremium(): Pair<Double, Double?>? = runCatching {
        val body = MarketDataClient.getText("https://www.binance.com/fapi/v1/premiumIndex?symbol=BTCUSDT", attempts = 2)
            ?: return null
        val json = JSONObject(body)
        val mark = json.optString("markPrice").toDoubleOrNull()?.takeIf { it > 0.0 } ?: return null
        mark to json.optString("lastFundingRate").toDoubleOrNull()
    }.getOrNull()

    private fun fetchFearGreed(): Int? = runCatching {
        val body = MarketDataClient.getText("https://api.alternative.me/fng/?limit=1", attempts = 2) ?: return null
        JSONObject(body).optJSONArray("data")?.optJSONObject(0)?.optString("value")?.toIntOrNull()
    }.getOrNull()

    private fun fetchCloses(url: String): List<Double> = runCatching {
        val body = MarketDataClient.getText(url, attempts = 2) ?: return emptyList()
        val arr = JSONArray(body)
        buildList {
            for (i in 0 until arr.length()) {
                val close = arr.optJSONArray(i)?.optString(4)?.toDoubleOrNull() ?: continue
                if (close > 0.0) add(close)
            }
        }
    }.getOrDefault(emptyList())
}
