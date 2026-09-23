package com.example.data.model

data class ForwardSignalAuditEntry(
    val id: String,
    val dateIso: String,
    val displayDate: String,
    val signalType: String,
    val regime: MarketRegime,
    val btcPriceAtSignalUsd: Double,
    val verificationOutcomePriceUsd: Double?,
    val performancePercent: Double?,
    val isVerified: Boolean = true,
    val outcomeStatus: AuditOutcomeStatus = AuditOutcomeStatus.VALIDATED,
    val keyEvidenceEn: String,
    val keyEvidenceEl: String,
    val daysElapsedSinceSignal: Int = 0
)

enum class AuditOutcomeStatus(
    val titleEn: String,
    val titleEl: String,
    val isPositive: Boolean
) {
    VALIDATED("Validated (+ Gain / Protected)", "Επαληθεύτηκε (+ Κέρδος / Προστασία)", true),
    ACTIVE_TRACKING("Active (Tracking Forward)", "Σε εξέλιξη (Ενεργό Tracking)", true),
    INVALIDATED("Invalidated", "Ακυρώθηκε", false)
}
