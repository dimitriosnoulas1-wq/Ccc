package com.example.data.model

enum class LeveragePositionSide(val label: String, val shortLabel: String) {
    LONG("Long", "LONG"),
    SHORT("Short", "SHORT")
}

enum class LeverageExchange(val displayName: String, val badgeText: String, val isDex: Boolean) {
    HYPERLIQUID("Hyperliquid", "DEX", true),
    BINANCE_FUTURES("Binance Futures", "CEX", false),
    BYBIT_PERPS("Bybit Perps", "CEX", false),
    GMX_V2("GMX v2 (Arbitrum)", "DEX", true),
    DYDX_V4("dYdX v4", "DEX", true),
    OKX_PERP("OKX Futures", "CEX", false)
}

data class WhaleLeveragePosition(
    val id: String,
    val coinSymbol: String,
    val coinName: String,
    val side: LeveragePositionSide,
    val leverage: Int, // e.g. 2, 3, 5, 10, 20, 25, 50
    val notionalUsd: Double, // e.g. $4,850,000
    val collateralUsd: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val liquidationPrice: Double,
    val pnlUsd: Double,
    val pnlPercent: Double,
    val traderLabel: String,
    val exchange: LeverageExchange,
    val timestampMillis: Long,
    val timeAgo: String,
    val isMegaWhale: Boolean = true, // > $2M
    val fundingRate: Double = 0.012,
    val statusText: String = "ACTIVE / OPEN"
)

data class WhaleLeverageSummary(
    val totalLongVolumeUsd: Double,
    val totalShortVolumeUsd: Double,
    val longRatioPercent: Double,
    val shortRatioPercent: Double,
    val activeMegaPositionsCount: Int,
    val largestPositionUsd: Double,
    val dominantSide: LeveragePositionSide
)
