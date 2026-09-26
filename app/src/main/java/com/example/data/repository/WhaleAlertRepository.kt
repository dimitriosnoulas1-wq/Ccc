package com.example.data.repository

import android.content.Context
import com.example.data.model.FuturesTrade
import com.example.data.model.WhaleAlert
import com.example.data.model.WhaleAlertSettings
import com.example.data.model.WhaleAlertType
import com.example.util.AlertSettingsStore
import com.example.util.CycleAlertRules
import com.example.util.CycleDayAlertPrefs
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WhaleAlertRepository(
    context: Context
) {
    private val appContext = context.applicationContext
    private val _settings = MutableStateFlow(AlertSettingsStore.load(appContext))
    val settings: StateFlow<WhaleAlertSettings> = _settings.asStateFlow()

    private val _isProUser = MutableStateFlow(false)

    private val _alerts = MutableStateFlow<List<WhaleAlert>>(emptyList())
    val alerts: StateFlow<List<WhaleAlert>> = _alerts.asStateFlow()

    fun setProUser(isPro: Boolean) {
        _isProUser.value = isPro
    }

    fun updateSettings(newSettings: WhaleAlertSettings) {
        _settings.value = newSettings
        AlertSettingsStore.save(appContext, newSettings)
    }

    fun setNotificationsEnabled(enabled: Boolean) = updateSettings(_settings.value.copy(notificationsEnabled = enabled))

    fun setMinThreshold(thresholdUsd: Double) = updateSettings(_settings.value.copy(minThresholdUsd = thresholdUsd))

    fun setNotifyZoneChange(enabled: Boolean) = updateSettings(_settings.value.copy(notifyZoneChange = enabled))

    fun setNotifyPiCycle(enabled: Boolean) = updateSettings(_settings.value.copy(notifyPiCycle = enabled))

    fun setNotifyRainbowBand(enabled: Boolean) = updateSettings(_settings.value.copy(notifyRainbowBand = enabled))

    fun setNotify200wSma(enabled: Boolean) = updateSettings(_settings.value.copy(notify200wSma = enabled))

    fun setNotifyFunding(enabled: Boolean) = updateSettings(_settings.value.copy(notifyFunding = enabled))

    fun ingestLargePrints(trades: List<FuturesTrade>, minUsd: Double = 100_000.0) {
        val large = trades.filter { it.valueUsd >= minUsd }
        if (large.isEmpty()) return
        val existing = _alerts.value
        val known = existing.associateBy { it.id }
        val incoming = large.map { trade -> known["bn-${trade.id}"] ?: trade.toWhaleAlert() }
        val merged = (incoming + existing)
            .distinctBy { it.id }
            .sortedByDescending { it.timestamp }
            .take(30)
        if (merged.map { it.id } != existing.map { it.id }) {
            _alerts.value = merged
        }
        notifyLargePrints(large.filter { known["bn-${it.id}"] == null })
    }

    private fun notifyLargePrints(fresh: List<FuturesTrade>) {
        val s = _settings.value
        if (!s.notificationsEnabled || !_isProUser.value) return
        val greek = CycleDayAlertPrefs.isGreek(appContext)
        val recent = System.currentTimeMillis() - 5 * 60_000L
        fresh.filter { it.valueUsd >= s.minThresholdUsd && it.timeMs >= recent }.forEach { trade ->
            val symbol = trade.symbol.removeSuffix("USDT").removeSuffix("BUSD").removePrefix("1000")
            val msg = CycleAlertRules.whaleMessage(symbol, trade.isSell, trade.valueUsd, greek)
            NotificationHelper.recordAndNotify(
                context = appContext,
                eventId = "whale-bn-${trade.id}",
                channelId = NotificationHelper.CHANNEL_WHALES,
                title = msg.title,
                body = msg.body,
                details = if (greek) "Μία συναλλαγή, όχι σήμα." else "One trade print, not a signal.",
                navTab = "TAPE",
                type = "WHALE"
            )
        }
    }

    private fun FuturesTrade.toWhaleAlert(): WhaleAlert {
        val symbol = symbol.removeSuffix("USDT").removeSuffix("BUSD").removePrefix("1000")
        val buy = !isSell
        return WhaleAlert(
            id = "bn-$id",
            timestamp = timeMs,
            coinSymbol = symbol,
            coinName = symbol,
            amountCoin = qty,
            amountUsd = valueUsd,
            type = if (buy) WhaleAlertType.WHALE_BUY else WhaleAlertType.EXCHANGE_INFLOW,
            source = "Binance USDT-M",
            destination = if (buy) "Aggressive buy" else "Aggressive sell",
            txHash = id.toString(),
            marketImpactVerdict = if (buy) {
                "Μεγάλο Binance futures buy ${symbol}."
            } else {
                "Μεγάλο Binance futures sell ${symbol}."
            },
            marketImpactVerdictEn = if (buy) {
                "Large Binance futures buy $symbol."
            } else {
                "Large Binance futures sell $symbol."
            },
            isImpactCritical = valueUsd >= 500_000.0
        )
    }
}
