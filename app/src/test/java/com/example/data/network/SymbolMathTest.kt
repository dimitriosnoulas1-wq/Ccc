package com.example.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SymbolMathTest {

    private val spot = setOf(
        "BTCUSDT", "ETHUSDT", "RAYUSDT", "PEPEUSDT", "SHIBUSDT", "BONKUSDT",
        "FLOKIUSDT", "ENSUSDT", "HYPEUSDT"
    )
    private val futures = setOf(
        "BTCUSDT", "ETHUSDT", "RAYSOLUSDT", "1000PEPEUSDT", "1000SHIBUSDT",
        "1000BONKUSDT", "1000FLOKIUSDT", "ENSUSDT", "HYPEUSDT", "KASUSDT",
        "XMRUSDT", "LITUSDT", "SPXUSDT", "AKTUSDT"
    )

    @Test
    fun `canonical strips quote and 1000 bundles`() {
        assertEquals("BTC" to 1.0, SymbolMath.canonical("btcusdt"))
        assertEquals("PEPE" to 1000.0, SymbolMath.canonical("1000PEPEUSDT"))
        assertEquals("RAY" to 1.0, SymbolMath.canonical("RAYSOLUSDT"))
    }

    @Test
    fun `spot-first listing prefers the 1-to-1 USDT market`() {
        val ray = SymbolMath.spotFirstListing("RAY", spot, futures)!!
        assertEquals("RAYUSDT", ray.pair)
        assertEquals(true, ray.spot)
        assertEquals(1.0, ray.divisor, 0.0)

        val pepe = SymbolMath.spotFirstListing("PEPE", spot, futures)!!
        assertEquals("PEPEUSDT", pepe.pair)
        assertEquals(true, pepe.spot)
    }

    @Test
    fun `futures pair uses RAYSOL and 1000 bundles when that is the live perp`() {
        assertEquals("RAYSOLUSDT", SymbolMath.futuresPair("RAY", futures))
        assertEquals("1000PEPEUSDT", SymbolMath.futuresPair("PEPE", futures))
        assertEquals("1000SHIBUSDT", SymbolMath.futuresPair("SHIB", futures))
        assertEquals("HYPEUSDT", SymbolMath.futuresPair("HYPE", futures))
        assertEquals("LITUSDT", SymbolMath.futuresPair("LIT", futures))
        assertEquals("ENSUSDT", SymbolMath.futuresPair("ENS", futures))
        assertEquals("KASUSDT", SymbolMath.futuresPair("KAS", futures))
    }

    @Test
    fun `futures-only coins still resolve without a spot pair`() {
        val kas = SymbolMath.spotFirstListing("KAS", emptySet(), futures)!!
        assertEquals("KASUSDT", kas.pair)
        assertEquals(false, kas.spot)
        assertNull(SymbolMath.spotFirstListing("XMN", spot, futures))
        assertNull(SymbolMath.spotFirstListing("TON", spot, futures))
        assertNull(SymbolMath.spotFirstListing("FRAX", spot, futures))
    }
}
