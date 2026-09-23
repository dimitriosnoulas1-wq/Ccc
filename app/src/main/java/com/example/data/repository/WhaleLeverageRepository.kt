package com.example.data.repository

import com.example.data.model.CryptoCoin
import com.example.data.model.LeverageExchange
import com.example.data.model.LeveragePositionSide
import com.example.data.model.WhaleLeveragePosition
import com.example.data.model.WhaleLeverageSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class WhaleLeverageRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    private val _positions = MutableStateFlow<List<WhaleLeveragePosition>>(emptyList())
    val positions: StateFlow<List<WhaleLeveragePosition>> = _positions.asStateFlow()

    private val _summary = MutableStateFlow<WhaleLeverageSummary>(
        WhaleLeverageSummary(
            totalLongVolumeUsd = 148_500_000.0,
            totalShortVolumeUsd = 86_200_000.0,
            longRatioPercent = 63.3,
            shortRatioPercent = 36.7,
            activeMegaPositionsCount = 18,
            largestPositionUsd = 24_800_000.0,
            dominantSide = LeveragePositionSide.LONG
        )
    )
    val summary: StateFlow<WhaleLeverageSummary> = _summary.asStateFlow()

    init {
        startLivePositionsTicker()
    }

    /**
     * Updates and aligns whale leverage positions with the latest live coin market prices
     */
    fun updateWithLiveCoins(coins: List<CryptoCoin>) {
        if (coins.isEmpty()) return
        val currentList = _positions.value
        if (currentList.isEmpty()) {
            _positions.value = generateInitialPositions(coins)
            recalculateSummary(_positions.value)
        } else {
            // Update current prices and unrealized PnL dynamically
            val coinMap = coins.associateBy { it.symbol.uppercase() }
            val updated = currentList.map { pos ->
                val liveCoin = coinMap[pos.coinSymbol.uppercase()]
                if (liveCoin != null) {
                    val curPrice = liveCoin.priceUsd
                    val priceDelta = if (pos.side == LeveragePositionSide.LONG) {
                        (curPrice - pos.entryPrice) / pos.entryPrice
                    } else {
                        (pos.entryPrice - curPrice) / pos.entryPrice
                    }
                    val pnlPct = priceDelta * pos.leverage * 100.0
                    val pnlUsd = (pos.collateralUsd * (pnlPct / 100.0)).coerceAtLeast(-pos.collateralUsd * 0.95)
                    
                    pos.copy(
                        currentPrice = curPrice,
                        pnlUsd = pnlUsd,
                        pnlPercent = pnlPct
                    )
                } else {
                    pos
                }
            }
            _positions.value = updated
            recalculateSummary(updated)
        }
    }

    private fun startLivePositionsTicker() {
        repositoryScope.launch {
            while (isActive) {
                delay(3500L) // Refresh ticks every 3.5s
                val current = _positions.value
                if (current.isNotEmpty()) {
                    // Slight realistic price fluctuation & tick
                    val updated = current.mapIndexed { idx, pos ->
                        if (idx % 3 == 0) {
                            val jitter = (Random.nextDouble(-0.0015, 0.0018))
                            val newCurPrice = pos.currentPrice * (1.0 + jitter)
                            val priceDelta = if (pos.side == LeveragePositionSide.LONG) {
                                (newCurPrice - pos.entryPrice) / pos.entryPrice
                            } else {
                                (pos.entryPrice - newCurPrice) / pos.entryPrice
                            }
                            val pnlPct = priceDelta * pos.leverage * 100.0
                            val pnlUsd = (pos.collateralUsd * (pnlPct / 100.0)).coerceAtLeast(-pos.collateralUsd * 0.95)
                            pos.copy(
                                currentPrice = newCurPrice,
                                pnlUsd = pnlUsd,
                                pnlPercent = pnlPct
                            )
                        } else {
                            pos
                        }
                    }
                    _positions.value = updated
                    recalculateSummary(updated)
                }
            }
        }
    }

    private fun recalculateSummary(list: List<WhaleLeveragePosition>) {
        if (list.isEmpty()) return
        val totalLongs = list.filter { it.side == LeveragePositionSide.LONG }.sumOf { it.notionalUsd }
        val totalShorts = list.filter { it.side == LeveragePositionSide.SHORT }.sumOf { it.notionalUsd }
        val total = (totalLongs + totalShorts).coerceAtLeast(1.0)
        val longRatio = (totalLongs / total) * 100.0
        val shortRatio = (totalShorts / total) * 100.0
        val largest = list.maxOfOrNull { it.notionalUsd } ?: 0.0

        _summary.value = WhaleLeverageSummary(
            totalLongVolumeUsd = totalLongs,
            totalShortVolumeUsd = totalShorts,
            longRatioPercent = longRatio,
            shortRatioPercent = shortRatio,
            activeMegaPositionsCount = list.size,
            largestPositionUsd = largest,
            dominantSide = if (longRatio >= 50.0) LeveragePositionSide.LONG else LeveragePositionSide.SHORT
        )
    }

    private fun generateInitialPositions(coins: List<CryptoCoin>): List<WhaleLeveragePosition> {
        val coinMap = coins.associateBy { it.symbol.uppercase() }

        val btcPrice = coinMap["BTC"]?.priceUsd ?: 96500.0
        val ethPrice = coinMap["ETH"]?.priceUsd ?: 2750.0
        val solPrice = coinMap["SOL"]?.priceUsd ?: 195.0
        val xrpPrice = coinMap["XRP"]?.priceUsd ?: 2.45
        val dogePrice = coinMap["DOGE"]?.priceUsd ?: 0.26
        val bnbPrice = coinMap["BNB"]?.priceUsd ?: 680.0
        val suiPrice = coinMap["SUI"]?.priceUsd ?: 3.20
        val nearPrice = coinMap["NEAR"]?.priceUsd ?: 6.10
        val pepePrice = coinMap["PEPE"]?.priceUsd ?: 0.000012
        val avaxPrice = coinMap["AVAX"]?.priceUsd ?: 32.50

        return listOf(
            createPosition(
                id = "pos-1",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                side = LeveragePositionSide.LONG,
                leverage = 10,
                notionalUsd = 24_850_000.0,
                entryPrice = btcPrice * 0.978,
                currentPrice = btcPrice,
                traderLabel = "Hyperliquid Whale 0x93a...c8",
                exchange = LeverageExchange.HYPERLIQUID,
                timeAgo = "3m ago",
                fundingRate = 0.0115
            ),
            createPosition(
                id = "pos-2",
                coinSymbol = "ETH",
                coinName = "Ethereum",
                side = LeveragePositionSide.LONG,
                leverage = 20,
                notionalUsd = 12_400_000.0,
                entryPrice = ethPrice * 0.982,
                currentPrice = ethPrice,
                traderLabel = "Alpha Capital Syndicate #3",
                exchange = LeverageExchange.BINANCE_FUTURES,
                timeAgo = "7m ago",
                fundingRate = 0.0102
            ),
            createPosition(
                id = "pos-3",
                coinSymbol = "SOL",
                coinName = "Solana",
                side = LeveragePositionSide.LONG,
                leverage = 5,
                notionalUsd = 18_200_000.0,
                entryPrice = solPrice * 0.965,
                currentPrice = solPrice,
                traderLabel = "GMX Mega Whale 0x71b...4f",
                exchange = LeverageExchange.GMX_V2,
                timeAgo = "12m ago",
                fundingRate = 0.0145
            ),
            createPosition(
                id = "pos-4",
                coinSymbol = "BTC",
                coinName = "Bitcoin",
                side = LeveragePositionSide.SHORT,
                leverage = 5,
                notionalUsd = 15_600_000.0,
                entryPrice = btcPrice * 1.018,
                currentPrice = btcPrice,
                traderLabel = "Hedge Desk Macro Short #1",
                exchange = LeverageExchange.BYBIT_PERPS,
                timeAgo = "18m ago",
                fundingRate = -0.0085
            ),
            createPosition(
                id = "pos-5",
                coinSymbol = "XRP",
                coinName = "XRP",
                side = LeveragePositionSide.LONG,
                leverage = 25,
                notionalUsd = 8_900_000.0,
                entryPrice = xrpPrice * 0.974,
                currentPrice = xrpPrice,
                traderLabel = "Bybit VIP Leaderboard Top 5",
                exchange = LeverageExchange.BYBIT_PERPS,
                timeAgo = "24m ago",
                fundingRate = 0.0210
            ),
            createPosition(
                id = "pos-6",
                coinSymbol = "SUI",
                coinName = "Sui",
                side = LeveragePositionSide.LONG,
                leverage = 10,
                notionalUsd = 6_450_000.0,
                entryPrice = suiPrice * 0.962,
                currentPrice = suiPrice,
                traderLabel = "Hyperliquid Momentum 0x2e...11",
                exchange = LeverageExchange.HYPERLIQUID,
                timeAgo = "31m ago",
                fundingRate = 0.0180
            ),
            createPosition(
                id = "pos-7",
                coinSymbol = "DOGE",
                coinName = "Dogecoin",
                side = LeveragePositionSide.LONG,
                leverage = 15,
                notionalUsd = 5_800_000.0,
                entryPrice = dogePrice * 0.985,
                currentPrice = dogePrice,
                traderLabel = "Binance Futures High Roller",
                exchange = LeverageExchange.BINANCE_FUTURES,
                timeAgo = "42m ago",
                fundingRate = 0.0150
            ),
            createPosition(
                id = "pos-8",
                coinSymbol = "BNB",
                coinName = "BNB",
                side = LeveragePositionSide.LONG,
                leverage = 3,
                notionalUsd = 9_300_000.0,
                entryPrice = bnbPrice * 0.988,
                currentPrice = bnbPrice,
                traderLabel = "Institutional Treasury Desk",
                exchange = LeverageExchange.BINANCE_FUTURES,
                timeAgo = "55m ago",
                fundingRate = 0.0090
            ),
            createPosition(
                id = "pos-9",
                coinSymbol = "ETH",
                coinName = "Ethereum",
                side = LeveragePositionSide.SHORT,
                leverage = 10,
                notionalUsd = 7_200_000.0,
                entryPrice = ethPrice * 1.012,
                currentPrice = ethPrice,
                traderLabel = "dYdX Quant Arbitrage 0x5a...99",
                exchange = LeverageExchange.DYDX_V4,
                timeAgo = "1h ago",
                fundingRate = -0.0060
            ),
            createPosition(
                id = "pos-10",
                coinSymbol = "NEAR",
                coinName = "NEAR Protocol",
                side = LeveragePositionSide.LONG,
                leverage = 20,
                notionalUsd = 4_350_000.0,
                entryPrice = nearPrice * 0.970,
                currentPrice = nearPrice,
                traderLabel = "Hyperliquid AI Alpha 0xbb...72",
                exchange = LeverageExchange.HYPERLIQUID,
                timeAgo = "1h 15m ago",
                fundingRate = 0.0165
            ),
            createPosition(
                id = "pos-11",
                coinSymbol = "PEPE",
                coinName = "Pepe",
                side = LeveragePositionSide.LONG,
                leverage = 50,
                notionalUsd = 3_100_000.0,
                entryPrice = pepePrice * 0.992,
                currentPrice = pepePrice,
                traderLabel = "Degen Whale 50x 0x04...d3",
                exchange = LeverageExchange.BYBIT_PERPS,
                timeAgo = "1h 30m ago",
                fundingRate = 0.0350
            ),
            createPosition(
                id = "pos-12",
                coinSymbol = "AVAX",
                coinName = "Avalanche",
                side = LeveragePositionSide.SHORT,
                leverage = 5,
                notionalUsd = 4_800_000.0,
                entryPrice = avaxPrice * 1.025,
                currentPrice = avaxPrice,
                traderLabel = "OKX Institutional Hedging",
                exchange = LeverageExchange.OKX_PERP,
                timeAgo = "1h 45m ago",
                fundingRate = -0.0075
            )
        )
    }

    private fun createPosition(
        id: String,
        coinSymbol: String,
        coinName: String,
        side: LeveragePositionSide,
        leverage: Int,
        notionalUsd: Double,
        entryPrice: Double,
        currentPrice: Double,
        traderLabel: String,
        exchange: LeverageExchange,
        timeAgo: String,
        fundingRate: Double
    ): WhaleLeveragePosition {
        val collateral = notionalUsd / leverage
        val liqDistanceRatio = (1.0 / leverage) * 0.90 // 90% MMR buffer
        val liqPrice = if (side == LeveragePositionSide.LONG) {
            entryPrice * (1.0 - liqDistanceRatio)
        } else {
            entryPrice * (1.0 + liqDistanceRatio)
        }

        val priceDelta = if (side == LeveragePositionSide.LONG) {
            (currentPrice - entryPrice) / entryPrice
        } else {
            (entryPrice - currentPrice) / entryPrice
        }
        val pnlPct = priceDelta * leverage * 100.0
        val pnlUsd = (collateral * (pnlPct / 100.0)).coerceAtLeast(-collateral * 0.95)

        return WhaleLeveragePosition(
            id = id,
            coinSymbol = coinSymbol,
            coinName = coinName,
            side = side,
            leverage = leverage,
            notionalUsd = notionalUsd,
            collateralUsd = collateral,
            entryPrice = entryPrice,
            currentPrice = currentPrice,
            liquidationPrice = liqPrice,
            pnlUsd = pnlUsd,
            pnlPercent = pnlPct,
            traderLabel = traderLabel,
            exchange = exchange,
            timestampMillis = System.currentTimeMillis(),
            timeAgo = timeAgo,
            isMegaWhale = notionalUsd >= 2_000_000.0,
            fundingRate = fundingRate,
            statusText = if (pnlPct > 40.0) "TAKING PROFIT" else if (pnlPct < -50.0) "NEAR LIQUIDATION ⚠️" else "ACTIVE / OPEN"
        )
    }
}
