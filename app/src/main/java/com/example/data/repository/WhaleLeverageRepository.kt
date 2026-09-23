package com.example.data.repository

import com.example.data.model.AggregatedDerivativesSnapshot
import com.example.data.model.FuturesLiquidationOrder
import com.example.data.model.LeverageExchange
import com.example.data.model.LeveragePositionSide
import com.example.data.model.WhaleLeveragePosition
import com.example.data.model.WhaleLeverageSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WhaleLeverageRepository {

    private val _positions = MutableStateFlow<List<WhaleLeveragePosition>>(emptyList())
    val positions: StateFlow<List<WhaleLeveragePosition>> = _positions.asStateFlow()

    private val _summary = MutableStateFlow(
        WhaleLeverageSummary(
            totalLongVolumeUsd = 0.0,
            totalShortVolumeUsd = 0.0,
            longRatioPercent = 0.0,
            shortRatioPercent = 0.0,
            activeMegaPositionsCount = 0,
            largestPositionUsd = 0.0,
            dominantSide = LeveragePositionSide.LONG
        )
    )
    val summary: StateFlow<WhaleLeverageSummary> = _summary.asStateFlow()

    fun applyLiveSnapshot(
        snapshot: AggregatedDerivativesSnapshot,
        liquidations: List<FuturesLiquidationOrder>,
        markPrice: Double
    ) {
        val venueCards = snapshot.venues.filter { it.ok }.mapNotNull { venue ->
            val oi = venue.openInterestUsd ?: return@mapNotNull null
            val lsr = venue.longShortRatio
            val longPct = if (lsr != null && lsr > 0.0) (lsr / (1.0 + lsr)) * 100.0 else 50.0
            val shortPct = 100.0 - longPct
            val side = if (longPct >= shortPct) LeveragePositionSide.LONG else LeveragePositionSide.SHORT
            val exchange = when (venue.venue.lowercase()) {
                "binance" -> LeverageExchange.BINANCE_FUTURES
                "bybit" -> LeverageExchange.BYBIT_PERPS
                "okx" -> LeverageExchange.OKX_PERP
                else -> LeverageExchange.BINANCE_FUTURES
            }
            val longOi = oi * (longPct / 100.0)
            val shortOi = oi * (shortPct / 100.0)
            val notional = if (side == LeveragePositionSide.LONG) longOi else shortOi
            WhaleLeveragePosition(
                id = "venue-${venue.venue}",
                coinSymbol = snapshot.symbol.ifBlank { "BTC" },
                coinName = venue.venue.uppercase(),
                side = side,
                leverage = 0,
                notionalUsd = notional,
                collateralUsd = oi,
                entryPrice = venue.markPrice ?: markPrice,
                currentPrice = venue.lastPrice ?: markPrice,
                liquidationPrice = 0.0,
                pnlUsd = venue.longLiqUsd ?: 0.0,
                pnlPercent = venue.change24hPct ?: 0.0,
                traderLabel = "${venue.venue.uppercase()} OI",
                exchange = exchange,
                timestampMillis = venue.asOfMs,
                timeAgo = if (venue.asOfMs > 0L) "live" else "—",
                isMegaWhale = oi >= 1_000_000.0,
                fundingRate = (venue.fundingRate ?: 0.0) * 100.0,
                statusText = "LIVE OI"
            )
        }

        val liqCards = liquidations
            .filter { it.valueUsd >= 50_000.0 }
            .sortedByDescending { it.valueUsd }
            .take(8)
            .map { liq ->
                val longLiq = liq.isLongLiquidated
                WhaleLeveragePosition(
                    id = "liq-${liq.id}",
                    coinSymbol = liq.symbol.removeSuffix("USDT"),
                    coinName = liq.symbol.removeSuffix("USDT"),
                    side = if (longLiq) LeveragePositionSide.LONG else LeveragePositionSide.SHORT,
                    leverage = 0,
                    notionalUsd = liq.valueUsd,
                    collateralUsd = liq.valueUsd,
                    entryPrice = liq.price,
                    currentPrice = markPrice,
                    liquidationPrice = liq.price,
                    pnlUsd = -liq.valueUsd,
                    pnlPercent = 0.0,
                    traderLabel = if (longLiq) "Long liquidation" else "Short liquidation",
                    exchange = LeverageExchange.BINANCE_FUTURES,
                    timestampMillis = liq.timeMs,
                    timeAgo = "liq",
                    isMegaWhale = liq.valueUsd >= 250_000.0,
                    fundingRate = 0.0,
                    statusText = "LIQUIDATED"
                )
            }

        val cards = venueCards + liqCards
        _positions.value = cards

        val agg = snapshot.aggregated
        val oi = agg.openInterestUsd ?: 0.0
        val lsr = agg.longShortRatio
        val longPct = if (lsr != null && lsr > 0.0) (lsr / (1.0 + lsr)) * 100.0 else 0.0
        val shortPct = if (longPct > 0.0) 100.0 - longPct else 0.0
        _summary.value = WhaleLeverageSummary(
            totalLongVolumeUsd = if (oi > 0.0 && longPct > 0.0) oi * (longPct / 100.0) else 0.0,
            totalShortVolumeUsd = if (oi > 0.0 && shortPct > 0.0) oi * (shortPct / 100.0) else 0.0,
            longRatioPercent = longPct,
            shortRatioPercent = shortPct,
            activeMegaPositionsCount = cards.size,
            largestPositionUsd = cards.maxOfOrNull { it.notionalUsd } ?: 0.0,
            dominantSide = if (longPct >= shortPct) LeveragePositionSide.LONG else LeveragePositionSide.SHORT
        )
    }
}
