package com.example.util

import com.example.data.network.MarketDataClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class CoinNewsItem(
    val id: String,
    val title: String,
    val publisher: String,
    val timeAgo: String,
    val url: String,
    val ticker: String,
    val deltaPct: Double
)

enum class NewsLoadState {
    READY,
    EMPTY,
    UNAVAILABLE
}

object CoinNewsManager {
    private const val CACHE_TTL_MS = 10 * 60 * 1000L

    private val feedCache = ConcurrentHashMap<String, Pair<Long, List<CoinNewsItem>>>()

    private val feeds = listOf(
        "https://cointelegraph.com/rss" to "Cointelegraph",
        "https://www.coindesk.com/arc/outboundfeeds/rss/" to "CoinDesk",
        "https://decrypt.co/feed" to "Decrypt"
    )

    suspend fun getNewsForCoin(symbol: String, coinName: String, coinDelta: Double): List<CoinNewsItem> =
        load(symbol, coinName, coinDelta).second

    /**
     * EMPTY means the feeds responded and none of the current articles mention this coin.
     * UNAVAILABLE means every feed failed, which is not the same as "no news".
     */
    suspend fun load(symbol: String, coinName: String, coinDelta: Double): Pair<NewsLoadState, List<CoinNewsItem>> =
        withContext(Dispatchers.IO) {
            val upper = symbol.uppercase(Locale.US)
            val now = System.currentTimeMillis()
            val cached = feedCache[upper]
            if (cached != null && now - cached.first < CACHE_TTL_MS) {
                val state = if (cached.second.isEmpty()) NewsLoadState.EMPTY else NewsLoadState.READY
                return@withContext state to cached.second
            }

            val articles = mutableListOf<CoinNewsItem>()
            var anyFeed = false
            for ((url, publisher) in feeds) {
                val xml = MarketDataClient.getText(url, attempts = 2)
                if (xml.isNullOrBlank()) continue
                anyFeed = true
                articles += parseRss(xml, publisher, upper, coinDelta)
            }
            val announcements = MarketDataClient.getText(
                "https://www.binance.com/bapi/composite/v1/public/cms/article/catalog/list/query?catalogId=48&pageNo=1&pageSize=30",
                attempts = 2
            )
            if (!announcements.isNullOrBlank()) {
                anyFeed = true
                articles += parseBinanceAnnouncements(announcements, upper, coinDelta)
            }

            if (!anyFeed) {
                return@withContext NewsLoadState.UNAVAILABLE to emptyList()
            }

            val matched = articles
                .filter { mentionsCoin(it.title, upper, coinName) }
                .distinctBy { it.title.lowercase(Locale.US) }
                .take(12)
            feedCache[upper] = now to matched
            val state = if (matched.isEmpty()) NewsLoadState.EMPTY else NewsLoadState.READY
            state to matched
        }

    private fun mentionsCoin(title: String, symbol: String, name: String): Boolean {
        val text = title.lowercase(Locale.US)
        val cleanedName = name.lowercase(Locale.US).trim()
        if (cleanedName.length >= 4 && text.contains(cleanedName)) return true
        val sym = symbol.lowercase(Locale.US)
        if (sym.isEmpty()) return false
        return if (sym.length <= 3) {
            Regex("\\b${Regex.escape(sym)}\\b").containsMatchIn(text)
        } else {
            text.contains(sym)
        }
    }

    private fun parseRss(xml: String, publisher: String, symbol: String, coinDelta: Double): List<CoinNewsItem> {
        val items = Regex("(?s)<item>(.*?)</item>").findAll(xml)
        val out = mutableListOf<CoinNewsItem>()
        for ((index, match) in items.withIndex()) {
            val block = match.groupValues[1]
            val title = unescape(tag(block, "title"))
            val link = unescape(tag(block, "link")).ifBlank { tag(block, "guid") }
            if (title.isBlank() || !link.startsWith("http")) continue
            val published = tag(block, "pubDate")
            out += CoinNewsItem(
                id = "$publisher-$index-${title.hashCode()}",
                title = title,
                publisher = publisher,
                timeAgo = timeAgo(parseRfc1123(published)),
                url = link,
                ticker = symbol,
                deltaPct = coinDelta
            )
            if (out.size >= 40) break
        }
        return out
    }

    private fun parseBinanceAnnouncements(body: String, symbol: String, coinDelta: Double): List<CoinNewsItem> {
        return try {
            val articles = JSONObject(body).optJSONObject("data")?.optJSONArray("articles") ?: return emptyList()
            buildList {
                for (i in 0 until articles.length()) {
                    val item = articles.optJSONObject(i) ?: continue
                    val title = item.optString("title")
                    val code = item.optString("code")
                    if (title.isBlank() || code.isBlank()) continue
                    add(
                        CoinNewsItem(
                            id = "binance-$code",
                            title = title,
                            publisher = "Binance",
                            timeAgo = "Binance",
                            url = "https://www.binance.com/en/support/announcement/$code",
                            ticker = symbol,
                            deltaPct = coinDelta
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun tag(block: String, name: String): String {
        val cdata = Regex("(?s)<$name[^>]*>\\s*<!\\[CDATA\\[(.*?)]]>\\s*</$name>").find(block)
        if (cdata != null) return cdata.groupValues[1].trim()
        val plain = Regex("(?s)<$name[^>]*>(.*?)</$name>").find(block)
        return plain?.groupValues?.get(1)?.trim().orEmpty()
    }

    private fun unescape(value: String): String = value
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("<[^>]+>"), "")
        .trim()

    private fun parseRfc1123(value: String): Long {
        if (value.isBlank()) return 0L
        val patterns = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss z"
        )
        for (pattern in patterns) {
            try {
                val parsed = SimpleDateFormat(pattern, Locale.US).parse(value)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }
        return 0L
    }

    private fun timeAgo(publishedMs: Long): String {
        if (publishedMs <= 0L) return "Recent"
        val hours = ((System.currentTimeMillis() - publishedMs) / 3_600_000L).coerceAtLeast(0L)
        return when {
            hours < 1L -> "Just now"
            hours < 24L -> "${hours}h ago"
            else -> "${hours / 24}d ago"
        }
    }
}
