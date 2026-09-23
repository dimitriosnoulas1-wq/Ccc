package com.example.data.model

data class WhaleFlowSnapshot(
    val inflowUsd: Double = 0.0,
    val outflowUsd: Double = 0.0,
    val netUsd: Double = 0.0,
    val alertCount: Int = 0,
    val isLive: Boolean = false,
    val sourceLabel: String = "Binance USDT-M"
)

data class GlobalRiskSnapshot(
    val dxy: Double? = null,
    val dxyChangePct: Double? = null,
    val us10y: Double? = null,
    val us10yChangeBps: Double? = null,
    val brent: Double? = null,
    val brentChangePct: Double? = null,
    val isLive: Boolean = false,
    val asOfMs: Long = 0L
)

data class LiveMovingAverages(
    val dma111: Double? = null,
    val dma350: Double? = null,
    val ema150: Double? = null,
    val sma471: Double? = null,
    val sma200d: Double? = null,
    val ma200w: Double? = null,
    val isLive: Boolean = false
) {
    val dma350x2: Double? = dma350?.times(2.0)
    val sma471x0745: Double? = sma471?.times(0.745)
}
