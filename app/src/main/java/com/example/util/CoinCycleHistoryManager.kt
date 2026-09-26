package com.example.util

import com.example.data.repository.HistoricalMarketRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CycleHistoricalBar(
    val label: String,
    val days: Int,
    val progress: Float
)

data class CycleHistoricalRise(
    val label: String,
    val days: Int
)

data class CoinCycleProfile(
    val typicalCorrectionDays: Int,
    val pastCorrectionRows: List<CycleHistoricalBar>,
    val pastRiseRows: List<CycleHistoricalRise>
)

/**
 * Computes past-cycle correction/rise day-counts from this coin's own real daily closes.
 * A "correction" here is the run from a past all-time high down to its lowest close
 * before the price went on to make a new all-time high. A "rise" is the run from that
 * low back up to the next new all-time high. Both are real, already-finished history,
 * computed from the same daily candles the cycle chart fetches — nothing hand-typed
 * and nothing generated from the coin's name. A coin without enough real daily history
 * (recent listings, thin data) returns null instead of an invented number.
 */
object CoinCycleHistoryManager {

    private const val DAY_MS = 86_400_000L
    private const val MIN_DEPTH = 0.15 // ignore small wobbles, keep real corrections only
    private val yearFormat = SimpleDateFormat("yyyy", Locale.US)

    private data class Group(val peakIdx: Int, val troughIdx: Int, val nextPeakIdx: Int?)

    suspend fun computeProfile(symbol: String): CoinCycleProfile? {
        val candles = HistoricalMarketRepository.loadRecentDaily(symbol, days = 0)
            ?.filter { it.close > 0.0 }
            ?.sortedBy { it.timeMs }
            ?: return null
        if (candles.size < 60) return null

        val groups = mutableListOf<Group>()
        var peakIdx = 0
        var peakPrice = candles[0].close
        var i = 1
        while (i < candles.size) {
            if (candles[i].close > peakPrice) {
                peakPrice = candles[i].close
                peakIdx = i
                i++
                continue
            }
            var troughIdx = i
            var troughPrice = candles[i].close
            var j = i
            while (j < candles.size && candles[j].close <= peakPrice) {
                if (candles[j].close < troughPrice) {
                    troughPrice = candles[j].close
                    troughIdx = j
                }
                j++
            }
            val reachedNewHigh = j < candles.size
            groups += Group(peakIdx, troughIdx, if (reachedNewHigh) j else null)
            if (reachedNewHigh) {
                peakPrice = candles[j].close
                peakIdx = j
                i = j + 1
            } else {
                i = j
            }
        }

        val completed = groups
            .filter { it.nextPeakIdx != null && it.troughIdx > it.peakIdx }
            .map { g ->
                val depth = 1.0 - candles[g.troughIdx].close / candles[g.peakIdx].close
                val days = ((candles[g.troughIdx].timeMs - candles[g.peakIdx].timeMs) / DAY_MS).toInt()
                Triple(g, depth, days)
            }
            .filter { (_, depth, days) -> depth > MIN_DEPTH && days > 0 }
        if (completed.isEmpty()) return null

        val topByDepth = completed.sortedByDescending { it.second }.take(3)
        val maxDays = topByDepth.maxOf { it.third }.coerceAtLeast(1)

        val correctionRows = topByDepth.sortedByDescending { it.first.peakIdx }.map { (g, _, days) ->
            val year = yearFormat.format(Date(candles[g.peakIdx].timeMs))
            CycleHistoricalBar("$year high", days, (days.toFloat() / maxDays).coerceIn(0.1f, 1f))
        }

        val riseRows = topByDepth.sortedByDescending { it.first.peakIdx }.mapNotNull { (g, _, _) ->
            val nextPeak = g.nextPeakIdx ?: return@mapNotNull null
            val days = ((candles[nextPeak].timeMs - candles[g.troughIdx].timeMs) / DAY_MS).toInt()
            if (days <= 0) return@mapNotNull null
            val troughYear = yearFormat.format(Date(candles[g.troughIdx].timeMs))
            val peakYear = yearFormat.format(Date(candles[nextPeak].timeMs))
            val label = if (troughYear == peakYear) "$troughYear low → high" else "$troughYear low → $peakYear high"
            CycleHistoricalRise(label, days)
        }

        val typicalDays = topByDepth.map { it.third }.average().toInt().coerceAtLeast(1)

        return CoinCycleProfile(
            typicalCorrectionDays = typicalDays,
            pastCorrectionRows = correctionRows,
            pastRiseRows = riseRows
        )
    }
}
