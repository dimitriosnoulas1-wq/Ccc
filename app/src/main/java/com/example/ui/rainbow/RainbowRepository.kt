package com.example.ui.rainbow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/** Parsing χωρίς org.json, ώστε να ελέγχεται με απλά JVM tests. */
object RainbowParse {
    private val POINT = Regex("\"x\"\\s*:\\s*(\\d+)\\s*,\\s*\"y\"\\s*:\\s*([0-9.eE+-]+)")
    private val AMOUNT = Regex("\"amount\"\\s*:\\s*\"([0-9.]+)\"")

    /** blockchain.info market-price → ημερήσιες τιμές, ταξινομημένες, μία ανά μέρα. */
    fun history(json: String): List<PricePoint> {
        val byDay = sortedMapOf<Long, Double>()
        POINT.findAll(json).forEach { m ->
            val p = m.groupValues[2].toDoubleOrNull() ?: return@forEach
            if (p > 0 && p.isFinite()) byDay[m.groupValues[1].toLong() / 86_400L] = p
        }
        return byDay.map { (d, p) -> PricePoint(d, p) }
    }

    /** Coinbase spot → τιμή. */
    fun spot(json: String): Double? =
        AMOUNT.find(json)?.groupValues?.get(1)?.toDoubleOrNull()?.takeIf { it > 0 }

    fun encodeHistory(points: List<PricePoint>): String =
        points.joinToString("\n") { "${it.day},${String.format(Locale.US, "%.8f", it.price)}" }

    fun decodeHistory(text: String): List<PricePoint> = text.lineSequence().mapNotNull { line ->
        val parts = line.split(',')
        if (parts.size != 2) return@mapNotNull null
        val d = parts[0].toLongOrNull() ?: return@mapNotNull null
        val p = parts[1].toDoubleOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
        PricePoint(d, p)
    }.toList()
}

/** Τι έχει αποθηκευτεί: πότε κατέβηκε το ιστορικό και ποιο σημείο φαίνεται σήμερα. */
data class RainbowCacheState(val fetchedDay: Long, val shown: PricePoint?) {
    fun encode(): String = listOf(
        fetchedDay.toString(),
        shown?.day?.toString() ?: "",
        shown?.price?.let { String.format(Locale.US, "%.8f", it) } ?: ""
    ).joinToString(",")

    companion object {
        fun decode(text: String): RainbowCacheState? {
            val p = text.trim().split(',')
            if (p.size != 3) return null
            val fetched = p[0].toLongOrNull() ?: return null
            val d = p[1].toLongOrNull(); val price = p[2].toDoubleOrNull()
            return RainbowCacheState(fetched, if (d != null && price != null && price > 0) PricePoint(d, price) else null)
        }
    }
}

class RainbowRepository(private val dir: File) {
    private val historyFile get() = File(dir, "rainbow_history.csv")
    private val stateFile get() = File(dir, "rainbow_state.txt")

    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000; c.readTimeout = 30_000
        c.setRequestProperty("User-Agent", "CryptoCycles/1.0")
        return try {
            if (c.responseCode !in 200..299) error("HTTP ${c.responseCode}")
            c.inputStream.bufferedReader().use { it.readText() }
        } finally { c.disconnect() }
    }

    suspend fun cachedHistory(): List<PricePoint> = withContext(Dispatchers.IO) {
        runCatching { RainbowParse.decodeHistory(historyFile.readText()) }.getOrDefault(emptyList())
    }

    suspend fun cachedState(): RainbowCacheState? = withContext(Dispatchers.IO) {
        runCatching { RainbowCacheState.decode(stateFile.readText()) }.getOrNull()
    }

    suspend fun saveState(state: RainbowCacheState) = withContext(Dispatchers.IO) {
        runCatching { stateFile.writeText(state.encode()) }
    }

    /** Όλο το ιστορικό ημερήσιων τιμών. Αποθηκεύεται για χρήση χωρίς σύνδεση. */
    suspend fun fetchHistory(): List<PricePoint> = withContext(Dispatchers.IO) {
        val h = RainbowParse.history(
            get("https://api.blockchain.info/charts/market-price?timespan=all&sampled=false&format=json")
        )
        check(h.size > 1000) { "Incomplete history (${h.size} points)" }
        runCatching { historyFile.writeText(RainbowParse.encodeHistory(h)) }
        h
    }

    suspend fun spot(): Double = withContext(Dispatchers.IO) {
        RainbowParse.spot(get("https://api.coinbase.com/v2/prices/BTC-USD/spot")) ?: error("Bad spot response")
    }
}
