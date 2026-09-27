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
    val pastRiseRows: List<CycleHistoricalRise>,
    /** Highest daily print in this coin's own history, and its day ("dd MMM yyyy"). */
    val athUsd: Double = 0.0,
    val athDate: String = ""
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
    private const val MAJOR_DEPTH = 0.5 // a cycle-level bear market
    private val yearFormat = SimpleDateFormat("yyyy", Locale.US)
    private val dayFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

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

        // The three most recent cycle-level bear markets (a 50%+ fall); if the coin never had
        // one, its three most recent real corrections. Recent cycles, not the deepest ever,
        // so a 2011-style crash does not drag today's comparison.
        val majors = completed.filter { it.second >= MAJOR_DEPTH }
        val chronological = majors.ifEmpty { completed }.sortedBy { it.first.peakIdx }
        val recent = chronological.takeLast(3).reversed() // newest first
        val maxDays = recent.maxOf { it.third }.coerceAtLeast(1)

        val correctionRows = recent.map { (g, _, days) ->
            val year = yearFormat.format(Date(candles[g.peakIdx].timeMs))
            CycleHistoricalBar("$year high", days, (days.toFloat() / maxDays).coerceIn(0.1f, 1f))
        }

        val athCandleIdx = candles.indices.maxByOrNull { idx ->
            candles[idx].let { if (it.high > 0.0) it.high else it.close }
        } ?: 0

        // A rise runs from a past low to the next cycle's top: the peak that started the next
        // bear market, or for the latest one, the all-time high since.
        val riseRows = recent.mapNotNull { (g, _, _) ->
            val nextMajor = chronological.firstOrNull { it.first.peakIdx > g.troughIdx }
            val topIdx = nextMajor?.first?.peakIdx ?: athCandleIdx.takeIf { it > g.troughIdx } ?: return@mapNotNull null
            val days = ((candles[topIdx].timeMs - candles[g.troughIdx].timeMs) / DAY_MS).toInt()
            if (days <= 0) return@mapNotNull null
            val lowYear = yearFormat.format(Date(candles[g.troughIdx].timeMs))
            val topYear = yearFormat.format(Date(candles[topIdx].timeMs))
            val label = if (lowYear == topYear) "$lowYear low → high" else "$lowYear low → $topYear high"
            CycleHistoricalRise(label, days)
        }

        val typicalDays = recent.map { it.third }.average().toInt().coerceAtLeast(1)

        val athCandle = candles[athCandleIdx]
        val athPrice = if (athCandle.high > 0.0) athCandle.high else athCandle.close

        return CoinCycleProfile(
            typicalCorrectionDays = typicalDays,
            pastCorrectionRows = correctionRows,
            pastRiseRows = riseRows,
            athUsd = athPrice,
            athDate = dayFormat.format(Date(athCandle.timeMs))
        )
    }
}
