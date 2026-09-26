package com.example.data.network

import com.example.data.model.FuturesTrade

/**
 * Tape "large moves" across major USDT-M perps.
 * The selected-symbol socket still drives the futures terminal (mark, book, ticker, OI).
 * Extra @aggTrade streams are only for the mixed large-print list — no invented fills.
 */
object TapeLargePrints {
    const val MIN_USD = 50_000.0
    const val LIST_LIMIT = 30

    val MAJOR_BASES = listOf(
        "BTC", "ETH", "SOL", "XRP", "BNB", "DOGE", "ADA", "SUI",
        "AVAX", "LINK", "NEAR", "INJ", "RENDER", "TAO", "PEPE", "HYPE"
    )

    val FALLBACK_PAIRS = listOf(
        "BTCUSDT", "ETHUSDT", "SOLUSDT", "XRPUSDT", "BNBUSDT", "DOGEUSDT", "ADAUSDT"
    )

    fun isLarge(trade: FuturesTrade, minUsd: Double = MIN_USD): Boolean =
        trade.price > 0.0 && trade.qty > 0.0 && trade.valueUsd >= minUsd

    fun merge(
        existing: List<FuturesTrade>,
        incoming: FuturesTrade,
        limit: Int = LIST_LIMIT,
        minUsd: Double = MIN_USD
    ): List<FuturesTrade> {
        if (!isLarge(incoming, minUsd)) return existing
        return (listOf(incoming) + existing)
            .distinctBy { "${it.symbol}-${it.id}-${it.timeMs}" }
            .sortedByDescending { it.timeMs }
            .take(limit)
    }

    fun aggTradePairs(activePair: String, listedFutures: Set<String>): List<String> {
        val active = activePair.trim().uppercase().ifBlank { "BTCUSDT" }
        if (listedFutures.isEmpty()) {
            return (FALLBACK_PAIRS + active).distinct()
        }
        return (MAJOR_BASES.map { SymbolMath.futuresPair(it, listedFutures) } + active)
            .distinct()
            .filter { it == active || it in listedFutures }
    }

    fun combinedStreamUrl(activePair: String, listedFutures: Set<String>): String {
        val active = activePair.trim().uppercase().ifBlank { "BTCUSDT" }
        val sym = active.lowercase()
        val extras = aggTradePairs(active, listedFutures)
            .map { it.lowercase() }
            .filter { it != sym }
            .distinct()
            .joinToString("/") { "$it@aggTrade" }
        val core = "${sym}@markPrice@1s/${sym}@aggTrade/${sym}@ticker/${sym}@bookTicker/!forceOrder@arr"
        return "wss://fstream.binance.com/stream?streams=" +
            if (extras.isEmpty()) core else "$core/$extras"
    }

    fun sameContract(left: String, right: String): Boolean {
        val a = SymbolMath.canonical(left).first
        val b = SymbolMath.canonical(right).first
        return a.isNotEmpty() && a.equals(b, ignoreCase = true)
    }
}
