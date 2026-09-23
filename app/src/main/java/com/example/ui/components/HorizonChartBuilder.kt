package com.example.ui.components

import com.example.data.model.CryptoCoin
import com.example.data.repository.HistoricalMarketRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Realized closes only. No invented week path and no line after today.
 */
internal object HorizonChartBuilder {
    fun daysFor(timeframe: ChartTimeframe): Int = when (timeframe) {
        ChartTimeframe.DAY_1 -> 2
        ChartTimeframe.WEEK_1 -> 8
        ChartTimeframe.MONTH_1 -> 32
        ChartTimeframe.YEAR_1 -> 370
        ChartTimeframe.CYCLE -> 0
    }

    fun bundle(
        coin: CryptoCoin,
        timeframe: ChartTimeframe,
        candles: List<HistoricalMarketRepository.Candle>
    ): CyclePointsBundle {
        val live = coin.priceUsd.toFloat().coerceAtLeast(0.00000001f)
        val past = when {
            timeframe == ChartTimeframe.DAY_1 && coin.sparkline.size >= 4 ->
                fromValues(coin.sparkline.map { it.toFloat() }, live, dayLabels(coin.sparkline.size))
            candles.size >= 2 -> fromCandles(candles, live)
            coin.sparkline.size >= 2 ->
                fromValues(coin.sparkline.map { it.toFloat() }, live, dayLabels(coin.sparkline.size))
            else -> listOf(ChartPoint(1f, live, isNowMarker = true, label = "Live", timeLabel = "Now"))
        }
        return CyclePointsBundle(
            pastPoints = past,
            futurePoints = emptyList(),
            analog2020Points = emptyList(),
            analog2016Points = emptyList(),
            isProjectedBullish = coin.change24h >= 0.0,
            trajectorySummary = if (coin.priceUpdatedAtMs > 0L) {
                "Live 24h ${String.format(Locale.US, "%+.1f%%", coin.change24h)}"
            } else {
                "Awaiting live price"
            },
            projectedTargetPrice = live,
            projectedTargetLabel = "Live"
        )
    }

    fun fromCandles(
        candles: List<HistoricalMarketRepository.Candle>,
        livePrice: Float
    ): List<ChartPoint> {
        val ordered = candles.filter { it.close > 0.0 }.sortedBy { it.timeMs }
        if (ordered.isEmpty()) {
            return listOf(ChartPoint(1f, livePrice, isNowMarker = true, label = "Live", timeLabel = "Now"))
        }
        val sampled = sample(ordered, 48)
        val last = sampled.last()
        val withLive = if (livePrice > 0f && kotlin.math.abs(last.close - livePrice.toDouble()) / last.close > 0.0001) {
            sampled + HistoricalMarketRepository.Candle(last.timeMs + 1, livePrice.toDouble())
        } else {
            sampled
        }
        val end = (withLive.size - 1).coerceAtLeast(1)
        return withLive.mapIndexed { index, candle ->
            val x = index / end.toFloat()
            val isLast = index == withLive.lastIndex
            ChartPoint(
                normalizedX = x,
                price = candle.close.toFloat(),
                isNowMarker = isLast,
                label = if (isLast) "Live" else null,
                timeLabel = dayLabel(candle.timeMs)
            )
        }
    }

    private fun fromValues(values: List<Float>, livePrice: Float, labels: List<String>): List<ChartPoint> {
        val series = if (values.last() != livePrice) values + livePrice else values
        val end = (series.size - 1).coerceAtLeast(1)
        return series.mapIndexed { index, price ->
            val isLast = index == series.lastIndex
            ChartPoint(
                normalizedX = index / end.toFloat(),
                price = price,
                isNowMarker = isLast,
                label = if (isLast) "Live" else null,
                timeLabel = labels.getOrNull(index) ?: if (isLast) "Now" else ""
            )
        }
    }

    private fun sample(
        candles: List<HistoricalMarketRepository.Candle>,
        maxPoints: Int
    ): List<HistoricalMarketRepository.Candle> {
        if (candles.size <= maxPoints) return candles
        val step = candles.size / maxPoints
        val out = candles.filterIndexed { index, _ -> index % step == 0 }.toMutableList()
        if (out.last() != candles.last()) out += candles.last()
        return out
    }

    private fun dayLabels(count: Int): List<String> =
        List(count) { index -> if (index == count - 1) "Now" else "T$index" }

    private fun dayLabel(timeMs: Long): String =
        SimpleDateFormat("dd MMM", Locale.US).format(Date(timeMs))
}
