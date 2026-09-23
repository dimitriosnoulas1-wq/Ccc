package com.example.data.repository

import android.content.Context
import com.example.data.model.FuturesTrade
import com.example.data.model.WhaleAlert
import com.example.data.model.WhaleAlertSettings
import com.example.data.model.WhaleAlertType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WhaleAlertRepository(
    @Suppress("UNUSED_PARAMETER") context: Context
) {
    private val _settings = MutableStateFlow(WhaleAlertSettings())
    val settings: StateFlow<WhaleAlertSettings> = _settings.asStateFlow()

    private val _isProUser = MutableStateFlow(false)

    private val _alerts = MutableStateFlow<List<WhaleAlert>>(emptyList())
    val alerts: StateFlow<List<WhaleAlert>> = _alerts.asStateFlow()

    fun setProUser(isPro: Boolean) {
        _isProUser.value = isPro
    }

    fun updateSettings(newSettings: WhaleAlertSettings) {
        _settings.value = newSettings
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
    }

    fun setMinThreshold(thresholdUsd: Double) {
        _settings.value = _settings.value.copy(minThresholdUsd = thresholdUsd)
    }

    fun setNotifyZoneChange(enabled: Boolean) {
        _settings.value = _settings.value.copy(notifyZoneChange = enabled)
    }

    fun setNotifyPiCycle(enabled: Boolean) {
        _settings.value = _settings.value.copy(notifyPiCycle = enabled)
    }

    fun setNotifyRainbowBand(enabled: Boolean) {
        _settings.value = _settings.value.copy(notifyRainbowBand = enabled)
    }

    fun setNotify200wSma(enabled: Boolean) {
        _settings.value = _settings.value.copy(notify200wSma = enabled)
    }

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
