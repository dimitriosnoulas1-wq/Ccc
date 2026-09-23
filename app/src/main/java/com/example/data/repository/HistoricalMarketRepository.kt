package com.example.data.repository

import com.example.data.network.ExchangeDirectory
import com.example.data.network.MarketDataClient
import com.example.data.network.SymbolMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.util.CycleFractalData
import com.example.util.FractalPoint
import com.example.util.HalvingCycleUtils
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Real OHLC closes for the analytics cycle chart.
 * Binance klines when the asset is listed, otherwise CoinGecko market_chart.
 * The dashed extension replays prior-window returns and is labelled as a statistical projection.
 */
object HistoricalMarketRepository {

    data class Candle(val timeMs: Long, val close: Double)

    private val cache = ConcurrentHashMap<String, Pair<Long, CycleFractalData>>()
    private const val TTL_MS = 10 * 60 * 1000L

    private val btcHalvings = listOf(
        1354116278000L to "2012 halving",
        1468082773000L to "2016 halving",
        1589217823000L to "2020 halving",
        HalvingCycleUtils.HALVING_4TH_TIMESTAMP to "2024 halving"
    )

    suspend fun load(coinId: String, symbol: String, zoom: String): CycleFractalData? = withContext(Dispatchers.IO) {
        val key = "${coinId.lowercase()}|${symbol.uppercase()}|${zoom.uppercase()}"
        val now = System.currentTimeMillis()
        cache[key]?.let { (at, data) ->
            if (now - at < TTL_MS) return@withContext data
        }
        val spec = zoomSpec(zoom)
        val candles = fetchCloses(coinId, symbol, spec.interval, spec.fetchBars) ?: return@withContext null
        if (candles.size < 4) return@withContext null
        val data = build(symbol, candles, spec.barsPerWindow, spec.fetchBars)
        cache[key] = now to data
        data
    }

    private data class ZoomSpec(val interval: String, val barsPerWindow: Int, val fetchBars: Int)

    private fun zoomSpec(zoom: String): ZoomSpec = when (zoom.uppercase()) {
        "1W" -> ZoomSpec("1h", 168, 520)
        "1M" -> ZoomSpec("4h", 180, 560)
        "1Y" -> ZoomSpec("1d", 365, 1000)
        else -> ZoomSpec("15m", 96, 300)
    }

    private fun fetchCloses(coinId: String, symbol: String, interval: String, limit: Int): List<Candle>? {
        ExchangeDirectory.ensureLoaded()
        val listing = SymbolMath.spotFirstListing(symbol, ExchangeDirectory.spotPairs(), ExchangeDirectory.futuresPairs())
        if (listing != null) {
            val urls = if (listing.spot) {
                listOf(
                    "https://data-api.binance.vision/api/v3/klines?symbol=${listing.pair}&interval=$interval&limit=$limit",
                    "https://api.binance.com/api/v3/klines?symbol=${listing.pair}&interval=$interval&limit=$limit"
                )
            } else {
                listOf(
                    "https://www.binance.com/fapi/v1/klines?symbol=${listing.pair}&interval=$interval&limit=$limit",
                    "https://fapi.binance.com/fapi/v1/klines?symbol=${listing.pair}&interval=$interval&limit=$limit"
                )
            }
            val body = MarketDataClient.getText(urls)
            val parsed = body?.let { parseKlines(it, listing.divisor) }
            if (!parsed.isNullOrEmpty()) return parsed
        }
        if (coinId.isBlank()) return null
        val days = when (interval) {
            "15m" -> "7"
            "1h" -> "30"
            "4h" -> "90"
            else -> "max"
        }
        val cg = MarketDataClient.getText(
            "https://api.coingecko.com/api/v3/coins/$coinId/market_chart?vs_currency=usd&days=$days"
        ) ?: return null
        return parseCoinGecko(cg, interval)
    }

    private fun parseKlines(body: String, divisor: Double): List<Candle> {
        return try {
            val arr = JSONArray(body)
            buildList {
                for (i in 0 until arr.length()) {
                    val row = arr.optJSONArray(i) ?: continue
                    val time = row.optLong(0)
                    val close = row.optString(4).toDoubleOrNull() ?: continue
                    if (time > 0L && close > 0.0) add(Candle(time, close / divisor))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseCoinGecko(body: String, interval: String): List<Candle> {
        return try {
            val prices = JSONObject(body).optJSONArray("prices") ?: return emptyList()
            val raw = buildList {
                for (i in 0 until prices.length()) {
                    val row = prices.optJSONArray(i) ?: continue
                    val time = row.optLong(0)
                    val close = row.optDouble(1, 0.0)
                    if (time > 0L && close > 0.0) add(Candle(time, close))
                }
            }
            downsample(raw, interval)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun downsample(raw: List<Candle>, interval: String): List<Candle> {
        if (raw.size < 4) return raw
        val bucketMs = when (interval) {
            "15m" -> 15 * 60 * 1000L
            "1h" -> 60 * 60 * 1000L
            "4h" -> 4 * 60 * 60 * 1000L
            else -> 24 * 60 * 60 * 1000L
        }
        val buckets = linkedMapOf<Long, Candle>()
        for (candle in raw) {
            val bucket = candle.timeMs / bucketMs
            buckets[bucket] = candle
        }
        return buckets.values.toList()
    }

    private fun build(symbol: String, candles: List<Candle>, window: Int, fetchedCap: Int): CycleFractalData {
        val current = candles.takeLast(window.coerceAtMost(candles.size))
        val before = candles.dropLast(current.size)
        val past = before.takeLast(current.size)
        val earlier = before.dropLast(past.size).takeLast(current.size)
        val projection = emptyList<Candle>()
        val currentBase = current.first().close
        val allReturns = returns(current) + returns(past) + returns(earlier) + projection.map { candle ->
            if (currentBase > 0.0) candle.close / currentBase - 1.0 else 0.0
        }
        val minRet = allReturns.minOrNull() ?: -0.05
        val maxRet = allReturns.maxOrNull() ?: 0.05
        val span = (maxRet - minRet).takeIf { abs(it) > 1e-9 } ?: 1.0

        fun points(series: List<Candle>, xEnd: Int): List<FractalPoint> {
            if (series.size < 2) return emptyList()
            val base = series.first().close
            return series.mapIndexed { index, candle ->
                val x = if (series.size == 1) 0 else (index * xEnd) / (series.size - 1)
                val ret = if (base > 0) candle.close / base - 1.0 else 0.0
                val norm = ((ret - minRet) / span).toFloat().coerceIn(0.04f, 0.96f)
                FractalPoint(
                    day = x,
                    normalizedValue = norm,
                    price = candle.close,
                    phaseTag = formatDay(candle.timeMs)
                )
            }
        }

        val currentPts = points(current, 640)
        val pastPts = points(past, 640)
        val earlierPts = points(earlier, 640)
        val projected = if (projection.isEmpty() || currentPts.isEmpty()) {
            emptyList()
        } else {
            val base = current.first().close
            val startNorm = currentPts.last().normalizedValue
            listOf(currentPts.last()) + projection.mapIndexed { index, candle ->
                val x = 640 + ((index + 1) * 160) / projection.size
                val ret = if (base > 0) candle.close / base - 1.0 else 0.0
                val norm = ((ret - minRet) / span).toFloat().coerceIn(0.04f, 0.96f)
                FractalPoint(x, if (index == 0) startNorm else norm, candle.close, "Projected ${formatDay(candle.timeMs)}")
            }
        }

        val windowStart = current.first().timeMs
        val windowEnd = current.last().timeMs
        val events = mutableListOf<Pair<Int, String>>()
        val coversFullHistory = candles.size < fetchedCap - 2
        val high = current.maxByOrNull { it.close }
        val low = current.minByOrNull { it.close }
        if (high != null) {
            val label = if (coversFullHistory && candles.maxOf { it.close } == high.close) "ATH" else "High"
            dayIndex(high.timeMs, windowStart, windowEnd)?.let { events += it to label }
        }
        if (low != null) {
            val label = if (coversFullHistory && candles.minOf { it.close } == low.close) "ATL" else "Low"
            dayIndex(low.timeMs, windowStart, windowEnd)?.let { events += it to label }
        }
        if (coversFullHistory) {
            events += 0 to "Listing"
        }
        val usesHalving = symbol.equals("BTC", ignoreCase = true)
        if (usesHalving) {
            for ((time, label) in btcHalvings) {
                dayIndex(time, windowStart, windowEnd)?.let { events += it to label }
            }
        }

        val change = if (current.first().close > 0) {
            ((current.last().close - current.first().close) / current.first().close) * 100.0
        } else 0.0
        val pastLabel = past.firstOrNull()?.let { "Past ${formatDay(it.timeMs)}" } ?: "Past cycle"
        val earlierLabel = earlier.firstOrNull()?.let { "Earlier ${formatDay(it.timeMs)}" } ?: "Earlier cycle"

        return CycleFractalData(
            currentPoints = currentPts,
            projectedPoints = projected,
            projectedBandUpper = emptyList(),
            projectedBandLower = emptyList(),
            points2020 = pastPts,
            points2016 = earlierPts,
            correlationScore2020 = change,
            correlationScore2016 = 0.0,
            currentDay = currentPts.lastOrNull()?.day ?: 0,
            peakDay = events.firstOrNull { it.second == "ATH" || it.second == "High" }?.first ?: 0,
            floorDay = events.firstOrNull { it.second == "ATL" || it.second == "Low" }?.first ?: 0,
            pastCycleLabel = pastLabel,
            earlierCycleLabel = earlierLabel,
            usesHalving = usesHalving,
            eventDays = events.distinctBy { it.second },
            windowLabel = "${formatDay(windowStart)} – ${formatDay(windowEnd)}"
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun project(current: List<Candle>, past: List<Candle>): List<Candle> {
        return emptyList()
    }

    private fun returns(series: List<Candle>): List<Double> {
        if (series.size < 2 || series.first().close <= 0.0) return emptyList()
        val base = series.first().close
        return series.map { it.close / base - 1.0 }
    }

    private fun dayIndex(timeMs: Long, start: Long, end: Long): Int? {
        if (end <= start || timeMs < start || timeMs > end) return null
        return (((timeMs - start).toDouble() / (end - start).toDouble()) * 640.0).toInt().coerceIn(0, 640)
    }

    private fun formatDay(timeMs: Long): String =
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(timeMs))
}
