package com.example.data.derivatives

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DerivativesJsonTest {

    @Test
    fun parseHubKeepsNullLiquidations() {
        val body = JSONObject()
            .put("ok", true)
            .put("symbol", "BTC")
            .put("asOfMs", 1_700_000_000_000L)
            .put("freshness", "DEGRADED")
            .put("sourceLabel", "Binance & OKX")
            .put(
                "venues",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("venue", "binance")
                            .put("contract", "BTCUSDT")
                            .put("ok", true)
                            .put("fundingRate", 0.0001)
                            .put("openInterestUsd", 5_000.0)
                            .put("missing", JSONArray().put("liquidations"))
                    )
                    .put(
                        JSONObject()
                            .put("venue", "bybit")
                            .put("contract", "BTCUSDT")
                            .put("ok", false)
                            .put("error", "geo_blocked")
                    )
            )
            .toString()
        val parsed = DerivativesJson.parseHub(body)
        requireNotNull(parsed)
        assertEquals("BTC", parsed.symbol)
        assertEquals(1, parsed.venuesUp.size)
        assertNull(parsed.aggregated.longLiqUsd)
        assertTrue(parsed.score.redistributed)
    }

    @Test
    fun parseBinanceDoesNotInventLiquidations() {
        val premium = JSONObject()
            .put("markPrice", "85000.0")
            .put("lastFundingRate", "0.0001")
            .put("time", 1_700_000_000_000L)
        val ticker = JSONObject()
            .put("lastPrice", "85010.0")
            .put("priceChangePercent", "1.25")
        val oi = JSONObject()
            .put("openInterest", "10")
            .put("time", 1_700_000_000_000L)
        val parsed = DerivativesJson.parseBinanceBundle(
            contract = "BTCUSDT",
            premium = premium,
            ticker = ticker,
            openInterest = oi,
            oiHist = null,
            taker = null,
            longShort = null,
            fundingHist = null
        )
        assertTrue(parsed.ok)
        assertEquals(850_000.0, parsed.openInterestUsd!!, 0.001)
        assertNull(parsed.longLiqUsd)
        assertTrue(parsed.missing.contains("liquidations"))
        assertTrue(parsed.missing.contains("taker"))
    }
}
