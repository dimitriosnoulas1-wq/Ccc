package com.example.data.repository

import android.content.Context
import com.example.data.model.AuditOutcomeStatus
import com.example.data.model.ForwardSignalAuditEntry
import com.example.data.model.MarketRegime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ForwardAuditTrailRepository(
    context: Context
) {
    private val prefs = context.getSharedPreferences("crypto_cycles_forward_audit", Context.MODE_PRIVATE)
    private val _auditLogs = MutableStateFlow(loadPersisted())
    val auditLogs: StateFlow<List<ForwardSignalAuditEntry>> = _auditLogs.asStateFlow()

    fun logCurrentLiveRegime(
        currentPriceUsd: Double,
        regime: MarketRegime,
        evidenceEn: String,
        evidenceEl: String
    ) {
        if (currentPriceUsd <= 0.0) return
        val currentList = _auditLogs.value.toMutableList()
        val latest = currentList.firstOrNull()
        if (latest != null && latest.regime == regime) {
            val updated = currentList.mapIndexed { index, entry ->
                if (index != 0 || entry.verificationOutcomePriceUsd != null) return@mapIndexed entry
                val days = ((System.currentTimeMillis() - parseIso(entry.dateIso)) / 86_400_000L).toInt().coerceAtLeast(0)
                val perf = if (entry.btcPriceAtSignalUsd > 0.0) {
                    ((currentPriceUsd - entry.btcPriceAtSignalUsd) / entry.btcPriceAtSignalUsd) * 100.0
                } else null
                entry.copy(daysElapsedSinceSignal = days, performancePercent = perf)
            }
            _auditLogs.value = updated
            persist(updated)
            return
        }
        val now = Date()
        val iso = ISO.format(now)
        val display = DISPLAY.format(now)
        val newEntry = ForwardSignalAuditEntry(
            id = "sig-${now.time}",
            dateIso = iso,
            displayDate = display,
            signalType = "${regime.name} LIVE",
            regime = regime,
            btcPriceAtSignalUsd = currentPriceUsd,
            verificationOutcomePriceUsd = null,
            performancePercent = null,
            isVerified = false,
            outcomeStatus = AuditOutcomeStatus.ACTIVE_TRACKING,
            keyEvidenceEn = evidenceEn,
            keyEvidenceEl = evidenceEl,
            daysElapsedSinceSignal = 0
        )
        val next = (listOf(newEntry) + currentList).take(20)
        _auditLogs.value = next
        persist(next)
    }

    private fun loadPersisted(): List<ForwardSignalAuditEntry> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        ForwardSignalAuditEntry(
                            id = o.getString("id"),
                            dateIso = o.getString("dateIso"),
                            displayDate = o.getString("displayDate"),
                            signalType = o.getString("signalType"),
                            regime = MarketRegime.valueOf(o.getString("regime")),
                            btcPriceAtSignalUsd = o.getDouble("btcPriceAtSignalUsd"),
                            verificationOutcomePriceUsd = if (o.has("verificationOutcomePriceUsd") && !o.isNull("verificationOutcomePriceUsd")) o.getDouble("verificationOutcomePriceUsd") else null,
                            performancePercent = if (o.has("performancePercent") && !o.isNull("performancePercent")) o.getDouble("performancePercent") else null,
                            isVerified = o.optBoolean("isVerified", false),
                            outcomeStatus = AuditOutcomeStatus.valueOf(o.optString("outcomeStatus", AuditOutcomeStatus.ACTIVE_TRACKING.name)),
                            keyEvidenceEn = o.optString("keyEvidenceEn"),
                            keyEvidenceEl = o.optString("keyEvidenceEl"),
                            daysElapsedSinceSignal = o.optInt("daysElapsedSinceSignal")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun persist(entries: List<ForwardSignalAuditEntry>) {
        val arr = JSONArray()
        entries.forEach { entry ->
            arr.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("dateIso", entry.dateIso)
                    put("displayDate", entry.displayDate)
                    put("signalType", entry.signalType)
                    put("regime", entry.regime.name)
                    put("btcPriceAtSignalUsd", entry.btcPriceAtSignalUsd)
                    if (entry.verificationOutcomePriceUsd != null) put("verificationOutcomePriceUsd", entry.verificationOutcomePriceUsd)
                    if (entry.performancePercent != null) put("performancePercent", entry.performancePercent)
                    put("isVerified", entry.isVerified)
                    put("outcomeStatus", entry.outcomeStatus.name)
                    put("keyEvidenceEn", entry.keyEvidenceEn)
                    put("keyEvidenceEl", entry.keyEvidenceEl)
                    put("daysElapsedSinceSignal", entry.daysElapsedSinceSignal)
                }
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private fun parseIso(value: String): Long = runCatching { ISO.parse(value)?.time }.getOrNull() ?: 0L

    companion object {
        private const val KEY = "audit_logs"
        private val ISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        private val DISPLAY = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US)
    }
}
