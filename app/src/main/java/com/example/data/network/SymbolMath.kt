package com.example.data.network

/**
 * Binance quotes a few assets as bundles (1000PEPE) or under a rebranded ticker (RAYSOL).
 * Everything that turns an exchange symbol into a catalog coin goes through here
 * so the spot list and the futures terminal cannot drift apart.
 */
object SymbolMath {

    data class Listing(val pair: String, val divisor: Double, val spot: Boolean)

    fun stripQuote(raw: String): String = raw.trim().uppercase()
        .removeSuffix("USDT")
        .removeSuffix("BUSD")
        .removeSuffix("USDC")
        .removeSuffix("PERP")

    /** Catalog symbol plus the multiplier that converts the exchange price into a per-token price. */
    fun canonical(raw: String): Pair<String, Double> {
        val clean = stripQuote(raw)
        val (body, multiplier) = when {
            clean.startsWith("1000000") && clean.length > 7 -> clean.removePrefix("1000000") to 1_000_000.0
            clean.startsWith("1000") && clean.length > 4 -> clean.removePrefix("1000") to 1000.0
            else -> clean to 1.0
        }
        val base = when (body) {
            "RAYSOL" -> "RAY"
            else -> body
        }
        return base to multiplier
    }

    fun aliases(symbol: String): List<String> {
        val sym = stripQuote(symbol)
        return when (sym) {
            "RAY" -> listOf("RAY", "RAYSOL")
            else -> listOf(sym)
        }
    }

    /**
     * Spot pair when Binance trades one, otherwise the live perpetual.
     * Bundle contracts are only used when no 1:1 USDT market exists, and their price is divided
     * back to a single token.
     */
    fun spotFirstListing(symbol: String, spot: Set<String>, futures: Set<String>): Listing? {
        val aliases = aliases(symbol)
        for (base in aliases) {
            val pair = "${base}USDT"
            if (pair in spot) return Listing(pair, 1.0, spot = true)
        }
        for (base in aliases) {
            val pair = "${base}USDT"
            if (pair in futures) return Listing(pair, 1.0, spot = false)
        }
        for (base in aliases) {
            val pair = "1000${base}USDT"
            when {
                pair in spot -> return Listing(pair, 1000.0, spot = true)
                pair in futures -> return Listing(pair, 1000.0, spot = false)
            }
        }
        for (base in aliases) {
            val pair = "1000000${base}USDT"
            when {
                pair in futures -> return Listing(pair, 1_000_000.0, spot = false)
                pair in spot -> return Listing(pair, 1_000_000.0, spot = true)
            }
        }
        return null
    }

    /** Perpetual used by the futures terminal. Falls back to the known bundle when info is not loaded yet. */
    fun futuresPair(symbol: String, futures: Set<String> = emptySet()): String {
        val aliases = aliases(symbol)
        if (futures.isNotEmpty()) {
            for (base in aliases) {
                val pair = "${base}USDT"
                if (pair in futures) return pair
            }
            for (base in aliases) {
                val pair = "1000${base}USDT"
                if (pair in futures) return pair
            }
            for (base in aliases) {
                val pair = "1000000${base}USDT"
                if (pair in futures) return pair
            }
        }
        val base = stripQuote(symbol)
        return when (base) {
            "RAY" -> "RAYSOLUSDT"
            "PEPE", "SHIB", "BONK", "FLOKI", "LUNC", "SATS", "RATS", "CHEEMS", "CAT" -> "1000${base}USDT"
            else -> "${base}USDT"
        }
    }
}
