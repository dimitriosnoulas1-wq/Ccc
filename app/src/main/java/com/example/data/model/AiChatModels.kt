package com.example.data.model

import java.util.UUID

enum class AiMessageSender {
    USER,
    AI
}

data class AiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: AiMessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isThinking: Boolean = false,
    val isError: Boolean = false
)

data class LiveMarketContextSnapshot(
    val currentTimeString: String = "",
    val btcPrice: Double = 0.0,
    val btc24hChange: Double = 0.0,
    val btcTs: Long = System.currentTimeMillis(),
    val ethPrice: Double = 0.0,
    val eth24hChange: Double = 0.0,
    val ethTs: Long = System.currentTimeMillis(),
    val solPrice: Double = 0.0,
    val sol24hChange: Double = 0.0,
    val solTs: Long = System.currentTimeMillis(),
    val cyclePhase: String? = "Phase 4: Early Bull Expansion",
    val cycleDay: Long = 508L,
    val daysSinceHalving: Long = 0L,
    val rainbowBand: String? = "Accumulation / Support Base",
    val distance200w: String? = "+54.2% above 200W SMA",
    val piCycleGap: String? = "+48.6%",
    val fearAndGreedScore: Int = 50,
    val fearAndGreedSentiment: String = "Neutral",
    val btcDominancePct: Double = 58.0,
    val altcoinSeasonIndex: Int = 31,
    val fundingRatePct: Double = 0.011,
    val futuresMarkPrice: Double = 0.0,
    val openInterestUsd: Double = 21870000000.0,
    val activeFuturesSymbol: String = "BTCUSDT",
    val whaleNet24h: String? = "+$142M Net Accumulation",
    val marketStance: String? = "Historical cycle alignment & institutional net flows",
    val targetedCoinInfo: String? = null,
    val allTrackedCoinsSummary: String = "",
    val topMarketPricesSummary: String = ""
)
