package com.example

import com.example.data.engine.CycleCommandEngine
import com.example.data.model.BitcoinEtfFlowData
import com.example.data.model.FearAndGreedData
import com.example.data.model.FuturesMarkFunding
import com.example.data.model.MacroMarketSentiment
import com.example.data.model.MarketIntelligenceEngine
import com.example.data.model.RainbowBand
import com.example.data.model.StablecoinLiquidityData
import com.example.data.network.GeminiAiService
import com.example.engine.forecasting.ForecastDirection
import com.example.engine.forecasting.MarketRegime
import com.example.engine.forecasting.QuantForecastEngine
import com.example.util.AppStrings
import com.example.util.GreekAppStrings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveScreensHonestyTest {

    @Test
    fun quantForecastDoesNotInventLevelsWhenTapeIsMissing() {
        val model = QuantForecastEngine.computeForecast(
            symbol = "BTC",
            currentPrice = 100_000.0,
            historicalPrices = listOf(1.0, 2.0, 3.0),
            highs = emptyList(),
            lows = emptyList(),
            fundingRate = null,
            etfInflowsUsd = null
        )
        assertEquals(MarketRegime.INSUFFICIENT_DATA, model.regime)
        assertEquals(ForecastDirection.UNKNOWN, model.direction)
        assertEquals(0.0, model.keySupport, 0.0)
        assertEquals(0.0, model.keyResistance, 0.0)
        assertEquals(0.0, model.invalidationLevel, 0.0)
        assertEquals(0, model.probabilities.bullPct)
        assertFalse(model.hasLiveTape)
        assertFalse(model.simpleExplanation.contains("verified prediction", ignoreCase = true))
        assertFalse(model.riskWarning.contains("Do not take trades", ignoreCase = true))
    }

    @Test
    fun quantForecastUsesRealHighLowExtremaNotSyntheticPercent() {
        val closes = (1..40).map { 100.0 + it }
        val highs = closes.map { it + 2.0 }
        val lows = closes.map { it - 3.0 }
        val model = QuantForecastEngine.computeForecast(
            symbol = "BTC",
            currentPrice = 140.0,
            historicalPrices = closes,
            highs = highs,
            lows = lows,
            fundingRate = 0.00005,
            etfInflowsUsd = 10_000_000.0
        )
        assertTrue(model.hasLiveTape)
        assertTrue(model.hasLiveLevels)
        assertEquals(lows.takeLast(20).minOrNull()!!, model.keySupport, 0.0001)
        assertEquals(highs.takeLast(20).maxOrNull()!!, model.keyResistance, 0.0001)
        assertFalse(model.simpleExplanation.contains("buyers in control", ignoreCase = true))
        assertTrue(model.simpleExplanation.contains("not a forecast", ignoreCase = true))
    }

    @Test
    fun cycleCommandSummaryIsFactsNotTakeProfit() {
        val state = CycleCommandEngine.computeCycleCommandState(
            btcPrice = 95_000.0,
            etfFlowData = BitcoinEtfFlowData(
                oneDayNetFlowMillionUsd = 120.0,
                fiveDayCumulativeMillionUsd = 400.0,
                isLive = true
            ),
            liquidityData = StablecoinLiquidityData(
                totalCirculatingUsd = 160_000_000_000.0,
                change7dUsd = 2_000_000_000.0,
                isLive = true
            ),
            futuresMarkFunding = FuturesMarkFunding(
                symbol = "BTCUSDT",
                markPrice = 95_000.0,
                indexPrice = 95_000.0,
                basis = 0.0,
                basisPercent = 0.0,
                fundingRate = 0.0001,
                nextFundingTimeMs = 0L,
                approxApr = 0.0,
                eventTimeMs = 1L,
                receivedTimeMs = 1L,
                fromExchange = true
            ),
            fearGreedScore = 62
        )
        assertFalse(state.keyStanceSummaryEn.contains("take profit", ignoreCase = true))
        assertFalse(state.keyStanceSummaryEn.contains("recommended", ignoreCase = true))
        assertFalse(state.keyStanceSummaryEl.contains("κατοχύρωση", ignoreCase = true))
        assertTrue(state.keyStanceSummaryEn.contains("No trade call"))
        assertTrue(state.rainbowBandName.contains("Historically", ignoreCase = true))
    }

    @Test
    fun rainbowBandsHaveNoBuySellOrders() {
        RainbowBand.entries.forEach { band ->
            assertFalse(band.nameEn.contains("BUY!", ignoreCase = false))
            assertFalse(band.nameEn.contains("SELL!", ignoreCase = false))
            assertFalse(band.descriptionEn.contains("dollar-cost", ignoreCase = true))
            assertFalse(band.descriptionEn.contains("scaling out", ignoreCase = true))
        }
    }

    @Test
    fun fearAndGreedDefaultsOffline() {
        val data = FearAndGreedData()
        assertFalse(data.isLive)
        assertEquals("—", data.sentiment)
        assertEquals(0, data.score)
    }

    @Test
    fun futuresIntelligenceIsTapeFactsWithoutInventedInvalidation() {
        val report = MarketIntelligenceEngine.analyze(
            symbol = "BTCUSDT",
            ticker = null,
            bookTicker = null,
            markFunding = FuturesMarkFunding(
                symbol = "BTCUSDT",
                markPrice = 90_000.0,
                indexPrice = 90_000.0,
                basis = 0.0,
                basisPercent = 0.0,
                fundingRate = 0.00008,
                nextFundingTimeMs = 0L,
                approxApr = 0.0,
                eventTimeMs = 1L,
                receivedTimeMs = 1L,
                fromExchange = true
            ),
            openInterest = null,
            recentTrades = emptyList(),
            recentLiquidations = emptyList(),
            macroSentiment = MacroMarketSentiment()
        )
        assertTrue(report.hasLivePrice)
        assertTrue(report.hasLiveFunding)
        assertTrue(report.interpretationEn.contains("Last"))
        assertTrue(report.interpretationEn.contains("Funding"))
        assertFalse(report.invalidationLevelEn.contains("$"))
        assertTrue(report.invalidationLevelEn.contains("Tape only"))
        assertTrue(report.cascadeRiskEn.contains("Waiting"))
        assertFalse(report.interpretationEn.contains("crowded", ignoreCase = true))
    }

    @Test
    fun aiFallbackDoesNotInventPeakDay() = runBlocking {
        val service = GeminiAiService(apiKeyOverride = "", openAiKeyOverride = "")
        val snapshot = com.example.data.model.LiveMarketContextSnapshot(
            btcPrice = 85_200.0,
            btc24hChange = 2.4,
            ethPrice = 2_750.0,
            solPrice = 180.0,
            fundingRatePct = 0.01,
            fearAndGreedScore = 0,
            fearAndGreedSentiment = "",
            btcDominancePct = 58.5,
            altcoinSeasonIndex = 0,
            daysSinceHalving = 500
        )
        val peak = service.analyzeMarketQuery("when is the peak", snapshot, com.example.data.model.AppLanguage.ENGLISH)
        assertFalse(peak.contains("500 and 550"))
        assertFalse(peak.contains("500 έως 550"))
        assertTrue(peak.contains("do not invent") || peak.contains("not a peak"))

        val greekPeak = service.analyzeMarketQuery("ποτε η κορυφη", snapshot, com.example.data.model.AppLanguage.GREEK)
        assertFalse(greekPeak.contains("500 έως 550"))
        assertTrue(greekPeak.contains("Δεν εφευρίσκω") || greekPeak.contains("όχι πρόβλεψη"))

        val missingFg = service.analyzeMarketQuery("fear and greed", snapshot, com.example.data.model.AppLanguage.ENGLISH)
        assertTrue(missingFg.contains("—"))
        assertFalse(missingFg.contains("optimal value accumulation"))
    }

    @Test
    fun leftoverChromeIsHistoryNotHoldDca() {
        assertEquals("HISTORY", AppStrings().macroRainbowBadge)
        assertEquals("ΙΣΤΟΡΙΚΟ", GreekAppStrings().macroRainbowBadge)
        assertFalse(AppStrings().piCycleTopSectionTitle.contains("SELL"))
        assertFalse(AppStrings().piCycleBottomSectionTitle.contains("BUY SIGNAL"))
        assertFalse(AppStrings().piCycleBottomStatusAlertText.contains("Floor Buy"))
        assertFalse(AppStrings().fearGreedAccumulationTip.contains("optimal"))
    }
}
