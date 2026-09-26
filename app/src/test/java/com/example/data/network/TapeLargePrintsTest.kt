package com.example.data.network

import com.example.data.model.FuturesTrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TapeLargePrintsTest {

    private fun trade(
        symbol: String,
        usd: Double,
        id: Long = 1L,
        timeMs: Long = 1_000L
    ): FuturesTrade {
        val qty = usd / 100.0
        return FuturesTrade(
            id = id,
            symbol = symbol,
            price = 100.0,
            qty = qty,
            isBuyerMaker = false,
            timeMs = timeMs
        )
    }

    @Test
    fun smallPrintsAreNotListed() {
        assertFalse(TapeLargePrints.isLarge(trade("XRPUSDT", 5.0)))
        assertFalse(TapeLargePrints.isLarge(trade("XRPUSDT", 49_999.0)))
        assertTrue(TapeLargePrints.isLarge(trade("BTCUSDT", 50_000.0)))
    }

    @Test
    fun mergeKeepsLargePrintsFromEveryMajor() {
        var list = emptyList<FuturesTrade>()
        list = TapeLargePrints.merge(list, trade("BTCUSDT", 120_000.0, id = 1, timeMs = 30))
        list = TapeLargePrints.merge(list, trade("ETHUSDT", 80_000.0, id = 2, timeMs = 20))
        list = TapeLargePrints.merge(list, trade("XRPUSDT", 5.0, id = 3, timeMs = 40))
        list = TapeLargePrints.merge(list, trade("SOLUSDT", 60_000.0, id = 4, timeMs = 10))
        assertEquals(listOf("BTCUSDT", "ETHUSDT", "SOLUSDT"), list.map { it.symbol })
        assertEquals(3, list.size)
    }

    @Test
    fun emptyExchangeListUsesFallbackMajorsNotGuessedPairs() {
        val pairs = TapeLargePrints.aggTradePairs("XRPUSDT", emptySet())
        assertTrue(pairs.containsAll(listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "XRPUSDT")))
        assertFalse(pairs.contains("KASUSDT"))
        assertFalse(pairs.contains("1000PEPEUSDT"))
    }

    @Test
    fun listedFuturesDropUnknownPerps() {
        val listed = setOf("BTCUSDT", "ETHUSDT", "XRPUSDT")
        val pairs = TapeLargePrints.aggTradePairs("BTCUSDT", listed)
        assertEquals(setOf("BTCUSDT", "ETHUSDT", "XRPUSDT"), pairs.toSet())
    }

    @Test
    fun combinedUrlKeepsTerminalStreamsAndAddsOtherAggTrades() {
        val url = TapeLargePrints.combinedStreamUrl("XRPUSDT", emptySet())
        assertTrue(url.contains("xrpusdt@markPrice@1s"))
        assertTrue(url.contains("xrpusdt@aggTrade"))
        assertTrue(url.contains("xrpusdt@ticker"))
        assertTrue(url.contains("xrpusdt@bookTicker"))
        assertTrue(url.contains("!forceOrder@arr"))
        assertTrue(url.contains("btcusdt@aggTrade"))
        assertTrue(url.contains("ethusdt@aggTrade"))
        assertTrue(url.contains("solusdt@aggTrade"))
        assertEquals(1, Regex("xrpusdt@aggTrade").findAll(url).count())
        assertFalse(url.contains("xrpusdt@markPrice@1s/".repeat(2)))
    }

    @Test
    fun sameContractTreatsUsdtAndBaseAsOne() {
        assertTrue(TapeLargePrints.sameContract("XRPUSDT", "xrp"))
        assertTrue(TapeLargePrints.sameContract("1000PEPEUSDT", "PEPE"))
        assertFalse(TapeLargePrints.sameContract("BTCUSDT", "ETHUSDT"))
    }
}
