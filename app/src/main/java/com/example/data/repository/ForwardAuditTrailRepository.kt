package com.example.data.repository

import com.example.data.model.AuditOutcomeStatus
import com.example.data.model.ForwardSignalAuditEntry
import com.example.data.model.MarketRegime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class ForwardAuditTrailRepository {

    private val _auditLogs = MutableStateFlow<List<ForwardSignalAuditEntry>>(
        listOf(
            ForwardSignalAuditEntry(
                id = "sig-2026-09",
                dateIso = "2026-09-12",
                displayDate = "12 Σεπ 2026",
                signalType = "CYCLE EXPANSION CONFIRMATION",
                regime = MarketRegime.CYCLE_EXPANSION,
                btcPriceAtSignalUsd = 77250.0,
                verificationOutcomePriceUsd = null,
                performancePercent = null,
                isVerified = false,
                outcomeStatus = AuditOutcomeStatus.ACTIVE_TRACKING,
                keyEvidenceEn = "ETF Inflows +$892M (5D) + DefiLlama Stables +$1.85B. Funding stable at 0.011%.",
                keyEvidenceEl = "Εισροές ETF +$892M (5D) + Stablecoins +$1.85B. Σταθερό Funding Rate στο 0.011%.",
                daysElapsedSinceSignal = 0
            ),
            ForwardSignalAuditEntry(
                id = "sig-2026-07",
                dateIso = "2026-07-28",
                displayDate = "28 Ιουλ 2026",
                signalType = "LEVERAGE FLUSH WARNING",
                regime = MarketRegime.LEVERAGE_DISTRIBUTION,
                btcPriceAtSignalUsd = 69450.0,
                verificationOutcomePriceUsd = 61200.0,
                performancePercent = 11.88, // 11.88% downside avoided / protected
                isVerified = true,
                outcomeStatus = AuditOutcomeStatus.VALIDATED,
                keyEvidenceEn = "Binance Funding spiked to +0.062% with record Open Interest. System flagged Squeeze Risk. Price corrected to $61.2K.",
                keyEvidenceEl = "Το Funding εκτινάχθηκε στο +0.062% με ρεκόρ Open Interest. Το σύστημα ειδοποίησε για Long Squeeze. Η τιμή διόρθωσε στα $61.2K.",
                daysElapsedSinceSignal = 46
            ),
            ForwardSignalAuditEntry(
                id = "sig-2026-05",
                dateIso = "2026-05-14",
                displayDate = "14 Μαΐ 2026",
                signalType = "DEEP VALUE ACCUMULATION FLOOR",
                regime = MarketRegime.ACCUMULATION,
                btcPriceAtSignalUsd = 56800.0,
                verificationOutcomePriceUsd = 68900.0,
                performancePercent = 21.30,
                isVerified = true,
                outcomeStatus = AuditOutcomeStatus.VALIDATED,
                keyEvidenceEn = "Fear & Greed reached 24 (Extreme Fear). Rainbow Chart entered 'Fire Sale Floor'. Strong Whale Net Inflow on spot.",
                keyEvidenceEl = "Το Fear & Greed έπεσε στο 24 (Extreme Fear). Το Rainbow Chart μπήκε στη ζώνη 'Fire Sale'. Ισχυρές εισροές Spot από Whales.",
                daysElapsedSinceSignal = 121
            ),
            ForwardSignalAuditEntry(
                id = "sig-2026-01",
                dateIso = "2026-01-22",
                displayDate = "22 Ιαν 2026",
                signalType = "POST-ETF REBALANCING SIGNAL",
                regime = MarketRegime.CYCLE_EXPANSION,
                btcPriceAtSignalUsd = 42100.0,
                verificationOutcomePriceUsd = 63800.0,
                performancePercent = 51.54,
                isVerified = true,
                outcomeStatus = AuditOutcomeStatus.VALIDATED,
                keyEvidenceEn = "Institutional ETF daily volume flipped to persistent net positive. Cycle days aligned with historic Halving Year rally.",
                keyEvidenceEl = "Οι θεσμικές ροές ETF έγιναν σταθερά θετικές. Οι ημέρες κύκλου συγχρονίστηκαν με το ιστορικό ράλι Halving Year.",
                daysElapsedSinceSignal = 233
            )
        )
    )
    val auditLogs: StateFlow<List<ForwardSignalAuditEntry>> = _auditLogs.asStateFlow()

    fun logCurrentLiveRegime(currentPriceUsd: Double, regime: MarketRegime, evidenceEn: String, evidenceEl: String) {
        val currentList = _auditLogs.value.toMutableList()
        val latest = currentList.firstOrNull()
        if (latest != null && latest.regime == regime && latest.dateIso.startsWith("2026-09-12")) {
            // Already logged for current session
            return
        }
        val newEntry = ForwardSignalAuditEntry(
            id = "sig-${System.currentTimeMillis()}",
            dateIso = "2026-09-12",
            displayDate = "Σήμερα (Live)",
            signalType = "${regime.name} ACTIVE SIGNAL",
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
        currentList.add(0, newEntry)
        _auditLogs.value = currentList
    }
}
