package com.example.data.model

enum class AlertCategory(
    val labelEn: String,
    val labelEl: String,
    val emoji: String
) {
    ALL("All Alerts", "Όλες", "🔔"),
    FUNDING_SPIKE("Funding Spike", "Υπερθέρμανση Funding", "⚡"),
    WHALE_FLOW("Whale Netflow", "Ροές Φαλαινών", "🐋"),
    ETF_EXTREME("ETF Inflows/Outflows", "Θεσμικά ETFs", "🏛️"),
    CYCLE_SHIFT("Cycle Zone Shift", "Αλλαγή Ζώνης Κύκλου", "🎯"),
    LIQUIDATION_CASCADE("Liquidation Flush", "Εκκαθαρίσεις (Flush)", "💥")
}

data class AlertHistoryItem(
    val id: String,
    val timestamp: Long,
    val displayDate: String,
    val category: AlertCategory,
    val title: String,
    val message: String,
    val detailedReason: String,
    val btcPriceAtTrigger: Double,
    val navTargetTab: String = "MACRO",
    val isUrgent: Boolean = true,
    val isRead: Boolean = false
)
