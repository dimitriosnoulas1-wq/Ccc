package com.example.data.repository

import com.example.data.network.ExchangeDirectory
import com.example.data.network.MarketDataClient
import com.example.data.network.SymbolMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

enum class VolumeProfileRange(val interval: String, val limit: Int, val labelEn: String, val labelEl: String) {
    DAY("15m", 96, "24h", "24ω"),
    WEEK("1h", 168, "7D", "7ημ"),
    MONTH("4h", 180, "30D", "30ημ")
}

data class VolumeCandle(
    val high: Double,
    val low: Double,
    val close: Double,
    val quoteVolume: Double,
    val takerBuyQuote: Double
)

data class VolumeBin(val low: Double, val high: Double, val volume: Double, val buyVolume: Double) {
    val mid: Double get() = (low + high) / 2.0
}

data class VolumeProfile(
    val bins: List<VolumeBin>,
    val pocIndex: Int,
    val valueAreaLow: Double,
    val valueAreaHigh: Double,
    val totalVolume: Double,
    val lastPrice: Double
) {
    val poc: Double get() = bins[pocIndex].mid
    val vaLowIndex: Int get() = bins.indexOfFirst { it.high > valueAreaLow }.coerceAtLeast(0)
    val vaHighIndex: Int get() = bins.indexOfLast { it.low < valueAreaHigh }.coerceAtLeast(0)
}

object VolumeProfileMath {
    const val VALUE_AREA_SHARE = 0.70

    /** Spreads each candle's quote volume evenly across the price range it traded. */
    fun build(candles: List<VolumeCandle>, binCount: Int = 40): VolumeProfile? {
        val usable = candles.filter { it.high > 0.0 && it.low > 0.0 && it.high >= it.low && it.quoteVolume > 0.0 }
        if (usable.size < 2 || binCount < 3) return null
        val top = usable.maxOf { it.high }
        val bottom = usable.minOf { it.low }
        if (top <= bottom) return null
        val step = (top - bottom) / binCount
        val volume = DoubleArray(binCount)
        val buy = DoubleArray(binCount)

        for (c in usable) {
            val buyShare = (c.takerBuyQuote / c.quoteVolume).coerceIn(0.0, 1.0)
            val first = ((c.low - bottom) / step).toInt().coerceIn(0, binCount - 1)
            val last = ((c.high - bottom) / step).toInt().coerceIn(0, binCount - 1)
            val range = c.high - c.low
            for (i in first..last) {
                val share = if (range <= 0.0) {
                    1.0
                } else {
                    val lo = bottom + i * step
                    val overlap = minOf(c.high, lo + step) - maxOf(c.low, lo)
                    (overlap / range).coerceAtLeast(0.0)
                }
                volume[i] += c.quoteVolume * share
                buy[i] += c.quoteVolume * share * buyShare
            }
        }

        val total = volume.sum()
        if (total <= 0.0) return null
        val bins = List(binCount) { i ->
            VolumeBin(bottom + i * step, bottom + (i + 1) * step, volume[i], buy[i])
        }
        val poc = volume.indices.maxBy { volume[it] }

        var lo = poc
        var hi = poc
        var inside = volume[poc]
        while (inside < total * VALUE_AREA_SHARE && (lo > 0 || hi < binCount - 1)) {
            val below = if (lo > 0) volume[lo - 1] else -1.0
            val above = if (hi < binCount - 1) volume[hi + 1] else -1.0
            if (above >= below) inside += volume[++hi] else inside += volume[--lo]
        }

        return VolumeProfile(
            bins = bins,
            pocIndex = poc,
            valueAreaLow = bins[lo].low,
            valueAreaHigh = bins[hi].high,
            totalVolume = total,
            lastPrice = usable.last().close
        )
    }

    /** Binance kline row: [openTime, open, high, low, close, volume, closeTime, quoteVolume, trades, takerBuyBase, takerBuyQuote, …]. */
    fun parseKlines(body: String, divisor: Double): List<VolumeCandle> = try {
        val arr = JSONArray(body)
        buildList {
            for (i in 0 until arr.length()) {
                val row = arr.optJSONArray(i) ?: continue
                val high = row.optString(2).toDoubleOrNull() ?: continue
                val low = row.optString(3).toDoubleOrNull() ?: continue
                val close = row.optString(4).toDoubleOrNull() ?: continue
                val quote = row.optString(7).toDoubleOrNull() ?: continue
                val takerQuote = row.optString(10).toDoubleOrNull() ?: 0.0
                add(VolumeCandle(high / divisor, low / divisor, close / divisor, quote, takerQuote))
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

object VolumeProfileRepository {
    private const val TTL_MS = 2 * 60 * 1000L
    private val cache = ConcurrentHashMap<String, Pair<Long, VolumeProfile>>()

    suspend fun load(symbol: String, range: VolumeProfileRange): VolumeProfile? = withContext(Dispatchers.IO) {
        val key = "${symbol.uppercase()}|${range.name}"
        val now = System.currentTimeMillis()
        cache[key]?.let { (at, profile) -> if (now - at < TTL_MS) return@withContext profile }

        ExchangeDirectory.ensureLoaded()
        val listing = SymbolMath.spotFirstListing(symbol, ExchangeDirectory.spotPairs(), ExchangeDirectory.futuresPairs())
            ?: return@withContext null
        val query = "symbol=${listing.pair}&interval=${range.interval}&limit=${range.limit}"
        val urls = if (listing.spot) {
            listOf(
                "https://data-api.binance.vision/api/v3/klines?$query",
                "https://api.binance.com/api/v3/klines?$query"
            )
        } else {
            listOf(
                "https://fapi.binance.com/fapi/v1/klines?$query",
                "https://www.binance.com/fapi/v1/klines?$query"
            )
        }
        val body = MarketDataClient.getText(urls) ?: return@withContext null
        val profile = VolumeProfileMath.build(VolumeProfileMath.parseKlines(body, listing.divisor))
            ?: return@withContext null
        cache[key] = now to profile
        profile
    }
}
