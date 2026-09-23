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
    val cyclePhase: String? = null,
    val cycleDay: Long = 0L,
    val daysSinceHalving: Long = 0L,
    val rainbowBand: String? = null,
    val distance200w: String? = null,
    val piCycleGap: String? = null,
    val fearAndGreedScore: Int = 0,
    val fearAndGreedSentiment: String = "—",
    val btcDominancePct: Double = 0.0,
    val altcoinSeasonIndex: Int = 0,
    val fundingRatePct: Double = 0.0,
    val futuresMarkPrice: Double = 0.0,
    val openInterestUsd: Double = 0.0,
    val activeFuturesSymbol: String = "BTCUSDT",
    val whaleNet24h: String? = null,
    val marketStance: String? = null,
    val targetedCoinInfo: String? = null,
    val allTrackedCoinsSummary: String = "",
    val topMarketPricesSummary: String = ""
)
