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
import kotlin.math.ln

/**
 * Halving-to-halving closes for the cycle chart.
 * Bitcoin uses the full daily history. Other coins use their Binance daily market.
 * Each line is that cycle's real closes, rebased to its own halving. Nothing is drawn after today.
 */
object HistoricalMarketRepository {

    data class Candle(
        val timeMs: Long,
        val close: Double,
        val high: Double = 0.0,
        val low: Double = 0.0
    )

    private val cache = ConcurrentHashMap<String, Pair<Long, CycleFractalData>>()
    private val candleCache = ConcurrentHashMap<String, Pair<Long, List<Candle>>>()
    private const val TTL_MS = 10 * 60 * 1000L

    private const val DAY_MS = 86_400_000L
    private const val HALVING_2012 = 1354116278000L
    private const val HALVING_2016 = 1468082773000L
    private const val HALVING_2020 = 1589217823000L
    private const val ANCHOR_MS = 14 * DAY_MS

    @Suppress("UNUSED_PARAMETER")
    suspend fun load(coinId: String, symbol: String, zoom: String): CycleFractalData? = withContext(Dispatchers.IO) {
        val key = "${coinId.lowercase()}|${symbol.uppercase()}|HALVING"
        val now = System.currentTimeMillis()
        cache[key]?.let { (at, data) ->
            val sameDay = !data.usesHalving || data.currentDay == HalvingCycleUtils.getDaysSince4thHalving().coerceAtMost(data.axisDays)
            if (now - at < TTL_MS && sameDay) return@withContext data
        }
        val candles = fetchDailyHistory(symbol) ?: return@withContext null
        if (candles.size < 30) return@withContext null
        val data = buildHalvingOverlay(symbol, candles, now) ?: return@withContext null
        cache[key] = now to data
        data
    }

    suspend fun loadRecentDaily(symbol: String, days: Int): List<Candle>? = withContext(Dispatchers.IO) {
        val key = symbol.uppercase()
        val now = System.currentTimeMillis()
        val all = candleCache[key]?.let { (at, data) ->
            if (now - at < TTL_MS) data else null
        } ?: run {
            val fetched = fetchDailyHistory(symbol) ?: return@withContext null
            candleCache[key] = now to fetched
            fetched
        }
        if (days <= 0) return@withContext all
        val cutoff = now - days.toLong() * DAY_MS
        all.filter { it.timeMs >= cutoff }.ifEmpty { all.takeLast(days.coerceAtLeast(2)) }
    }

    /**
     * Daily OHLC for models that need real highs/lows (QuantForecast ATR).
     * Prefers Binance klines even for BTC so high/low are exchange prints, not close-only.
     */
    suspend fun loadRecentDailyOhlc(symbol: String, days: Int): List<Candle>? = withContext(Dispatchers.IO) {
        val binance = fetchBinanceDaily(symbol)
        val source = if (!binance.isNullOrEmpty() && binance.count { it.high > 0.0 && it.low > 0.0 } >= 30) {
            binance
        } else {
            loadRecentDaily(symbol, days) ?: return@withContext null
        }
        if (days <= 0) return@withContext source
        val now = System.currentTimeMillis()
        val cutoff = now - days.toLong() * DAY_MS
        source.filter { it.timeMs >= cutoff }.ifEmpty { source.takeLast(days.coerceAtLeast(2)) }
    }

    private fun fetchDailyHistory(symbol: String): List<Candle>? {
        if (symbol.equals("BTC", ignoreCase = true)) {
            val btc = fetchBlockchainBtc()
            if (!btc.isNullOrEmpty()) return btc
        }
        return fetchBinanceDaily(symbol)
    }

    private fun fetchBlockchainBtc(): List<Candle>? {
        val body = MarketDataClient.getText(
            "https://api.blockchain.info/charts/market-price?timespan=all&format=json&sampled=false&cors=true"
        ) ?: return null
        return try {
            val values = JSONObject(body).optJSONArray("values") ?: return null
            buildList {
                for (i in 0 until values.length()) {
                    val row = values.optJSONObject(i) ?: continue
                    val time = row.optLong("x") * 1000L
                    val close = row.optDouble("y", 0.0)
                    if (time > 0L && close > 0.0) add(Candle(time, close))
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchBinanceDaily(symbol: String): List<Candle>? {
        ExchangeDirectory.ensureLoaded()
        val listing = SymbolMath.spotFirstListing(symbol, ExchangeDirectory.spotPairs(), ExchangeDirectory.futuresPairs())
            ?: return null
        val out = mutableListOf<Candle>()
        var start = HALVING_2016
        val now = System.currentTimeMillis()
        var guard = 0
        while (guard < 6 && start < now) {
            val urls = if (listing.spot) {
                listOf(
                    "https://data-api.binance.vision/api/v3/klines?symbol=${listing.pair}&interval=1d&limit=1000&startTime=$start",
                    "https://api.binance.com/api/v3/klines?symbol=${listing.pair}&interval=1d&limit=1000&startTime=$start"
                )
            } else {
                listOf(
                    "https://www.binance.com/fapi/v1/klines?symbol=${listing.pair}&interval=1d&limit=1000&startTime=$start",
                    "https://fapi.binance.com/fapi/v1/klines?symbol=${listing.pair}&interval=1d&limit=1000&startTime=$start"
                )
            }
            val batch = MarketDataClient.getText(urls)?.let { parseKlines(it, listing.divisor) }.orEmpty()
            if (batch.isEmpty()) break
            out += batch
            val next = batch.last().timeMs + DAY_MS
            if (batch.size < 900 || next <= start) break
            start = next
            guard++
        }
        return out.distinctBy { it.timeMs / DAY_MS }.sortedBy { it.timeMs }.takeIf { it.isNotEmpty() }
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
                    val high = row.optString(2).toDoubleOrNull() ?: 0.0
                    val low = row.optString(3).toDoubleOrNull() ?: 0.0
                    val close = row.optString(4).toDoubleOrNull() ?: continue
                    if (time > 0L && close > 0.0) {
                        add(
                            Candle(
                                timeMs = time,
                                close = close / divisor,
                                high = if (high > 0.0) high / divisor else 0.0,
                                low = if (low > 0.0) low / divisor else 0.0
                            )
                        )
                    }
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

    internal fun buildHalvingOverlay(symbol: String, candles: List<Candle>, nowMs: Long): CycleFractalData? {
        val sorted = candles.filter { it.close > 0.0 }.sortedBy { it.timeMs }
        if (sorted.size < 30) return null
        val axisDays = ((HalvingCycleUtils.HALVING_5TH_TIMESTAMP - HalvingCycleUtils.HALVING_4TH_TIMESTAMP) / DAY_MS)
            .toInt()
            .coerceAtLeast(1)
        val now = nowMs.coerceAtMost(HalvingCycleUtils.HALVING_5TH_TIMESTAMP)
        val cycle2012 = cycleDrafts(sorted, HALVING_2012, HALVING_2016, axisDays)
        val cycle2016 = cycleDrafts(sorted, HALVING_2016, HALVING_2020, axisDays)
        val cycle2020 = cycleDrafts(sorted, HALVING_2020, HalvingCycleUtils.HALVING_4TH_TIMESTAMP, axisDays)
        val cycleNow = cycleDrafts(sorted, HalvingCycleUtils.HALVING_4TH_TIMESTAMP, now, axisDays)
        if (cycleNow.size < 2 && cycle2020.size < 2) return null
        val logs = (cycle2016 + cycle2020 + cycleNow).map { ln(it.multiple.coerceAtLeast(0.05)) }
        val minLog = logs.minOrNull() ?: 0.0
        val maxLog = logs.maxOrNull() ?: minLog
        val span = (maxLog - minLog).takeIf { abs(it) > 1e-6 } ?: 1.0

        fun draw(drafts: List<CycleDraft>): List<FractalPoint> = drafts.map { draft ->
            val logM = ln(draft.multiple.coerceAtLeast(0.05))
            val norm = ((logM - minLog) / span).toFloat().coerceIn(0.04f, 0.96f)
            FractalPoint(draft.day, norm, draft.price, draft.label)
        }

        val currentDay = if (nowMs <= HalvingCycleUtils.HALVING_4TH_TIMESTAMP) {
            0
        } else {
            ((nowMs - HalvingCycleUtils.HALVING_4TH_TIMESTAMP) / DAY_MS).toInt().coerceIn(0, axisDays)
        }
        val change = if (cycleNow.size >= 2) (cycleNow.last().multiple - 1.0) * 100.0 else 0.0
        return CycleFractalData(
            currentPoints = draw(cycleNow),
            projectedPoints = emptyList(),
            projectedBandUpper = emptyList(),
            projectedBandLower = emptyList(),
            points2020 = draw(cycle2020),
            points2016 = draw(cycle2016),
            correlationScore2020 = change,
            correlationScore2016 = 0.0,
            currentDay = currentDay,
            peakDay = cycleNow.maxByOrNull { it.price }?.day ?: 0,
            floorDay = 0,
            pastCycleLabel = "2020",
            earlierCycleLabel = "2016",
            usesHalving = symbol.equals("BTC", ignoreCase = true) || cycle2016.isNotEmpty() || cycle2020.isNotEmpty(),
            eventDays = listOf(0 to "Halving"),
            windowLabel = "Day $currentDay / $axisDays",
            axisDays = axisDays,
            multipleNow = multipleAt(cycleNow, currentDay),
            multiple2012 = multipleAt(cycle2012, currentDay),
            multiple2016 = multipleAt(cycle2016, currentDay),
            multiple2020 = multipleAt(cycle2020, currentDay),
            close2012 = priceAt(cycle2012, currentDay),
            close2016 = priceAt(cycle2016, currentDay),
            close2020 = priceAt(cycle2020, currentDay)
        )
    }

    private fun multipleAt(drafts: List<CycleDraft>, day: Int): Double? = hitAt(drafts, day)?.multiple

    private fun priceAt(drafts: List<CycleDraft>, day: Int): Double? = hitAt(drafts, day)?.price

    private fun hitAt(drafts: List<CycleDraft>, day: Int): CycleDraft? {
        val hit = drafts.minByOrNull { abs(it.day - day) } ?: return null
        if (abs(hit.day - day) > 14) return null
        return hit
    }

    private data class CycleDraft(val day: Int, val multiple: Double, val price: Double, val label: String)

    private fun cycleDrafts(candles: List<Candle>, startMs: Long, endMs: Long, axisDays: Int): List<CycleDraft> {
        if (endMs <= startMs) return emptyList()
        val anchor = candles.minByOrNull { abs(it.timeMs - startMs) } ?: return emptyList()
        if (abs(anchor.timeMs - startMs) > ANCHOR_MS || anchor.close <= 0.0) return emptyList()
        val series = candles.filter { it.timeMs in startMs..endMs }
        if (series.size < 8) return emptyList()
        val sampled = series.filterIndexed { index, _ -> index % 7 == 0 }.toMutableList()
        if (sampled.last().timeMs != series.last().timeMs) sampled += series.last()
        if (sampled.first().timeMs != anchor.timeMs && anchor.timeMs <= endMs) {
            sampled.add(0, anchor)
        }
        return sampled.map { candle ->
            val day = ((candle.timeMs - startMs) / DAY_MS).toInt().coerceIn(0, axisDays)
            CycleDraft(
                day = day,
                multiple = candle.close / anchor.close,
                price = candle.close,
                label = formatDay(candle.timeMs)
            )
        }.distinctBy { it.day }
    }

    private fun formatDay(timeMs: Long): String =
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(timeMs))
}
