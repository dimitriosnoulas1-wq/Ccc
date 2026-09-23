package com.example.data.network

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * GET with host fallback and backoff. When [MarketHub] is configured, phones
 * read the shared cache first so 5,000 users do not each open CoinGecko/Binance.
 * If the hub is down, the original exchange hosts are used.
 */
object MarketDataClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun getText(url: String, attempts: Int = 3): String? = getText(alternateUrls(url), attempts)

    fun getText(urls: List<String>, attempts: Int = 3): String? {
        if (urls.isEmpty()) return null
        if (MarketHub.isEnabled()) {
            for (url in urls) {
                val viaHub = getOnce(MarketHub.proxyUrl(url))
                if (!viaHub.isNullOrBlank()) return viaHub
            }
        }
        var pauseMs = 350L
        repeat(attempts) {
            for (url in urls) {
                val body = getOnce(url)
                if (!body.isNullOrBlank()) return body
            }
            try {
                Thread.sleep(pauseMs)
            } catch (_: InterruptedException) {
                return null
            }
            pauseMs = (pauseMs * 2).coerceAtMost(4_000L)
        }
        return null
    }

    private fun getOnce(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CryptoCycles/1.132")
                .header("Accept", "application/json, application/rss+xml, application/xml, text/xml, */*")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.takeIf { it.isNotBlank() }
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun alternateUrls(url: String): List<String> {
        val urls = linkedSetOf(url)
        if (url.startsWith("https://api.binance.com")) {
            urls.add(url.replace("https://api.binance.com", "https://data-api.binance.vision"))
            urls.add(url.replace("https://api.binance.com", "https://www.binance.com"))
        }
        if (url.startsWith("https://data-api.binance.vision")) {
            urls.add(url.replace("https://data-api.binance.vision", "https://www.binance.com"))
            urls.add(url.replace("https://data-api.binance.vision", "https://api.binance.com"))
        }
        if (url.startsWith("https://fapi.binance.com")) {
            urls.add(url.replace("https://fapi.binance.com", "https://www.binance.com"))
        }
        if (url.startsWith("https://www.binance.com/fapi/")) {
            urls.add(url.replace("https://www.binance.com", "https://fapi.binance.com"))
        }
        if (url.startsWith("https://www.binance.com/api/")) {
            urls.add(url.replace("https://www.binance.com", "https://data-api.binance.vision"))
        }
        return urls.toList()
    }
}
