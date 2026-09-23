package com.example.data.network

import com.example.BuildConfig
import java.net.URLEncoder

/**
 * Shared hub: every phone reads cached market data from one process
 * instead of each account hitting Binance/CoinGecko on its own.
 */
object MarketHub {
    @Volatile
    var baseUrl: String = BuildConfig.MARKET_HUB_URL.trim()

    fun isEnabled(): Boolean = baseUrl.isNotBlank()

    fun proxyUrl(originUrl: String): String {
        val root = baseUrl.trimEnd('/')
        return "$root/v1/proxy?u=" + URLEncoder.encode(originUrl, "UTF-8")
    }
}
