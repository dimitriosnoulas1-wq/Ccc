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
        val band = com.example.ui.rainbow.RainbowModel.bandIndex(com.example.ui.rainbow.DateUtil.today(), 95_000.0)
        assertEquals(com.example.ui.rainbow.RainbowModel.BANDS[band].name, state.rainbowBandName)
    }

    @Test
    fun rainbowBandsHaveNoBuySellOrders() {
        RainbowBand.entries.forEach { band ->
            assertFalse(band.nameEn.contains("BUY!", ignoreCase = false))
            assertFalse(band.nameEn.contains("SELL!", ignoreCase = false))
            assertFalse(band.descriptionEn.contains("Favorable risk/reward", ignoreCase = true))
            assertFalse(band.descriptionEn.contains("scaling out", ignoreCase = true))
            assertFalse(band.nameEn.contains("ΑΓΟΡΑ"))
        }
        com.example.ui.rainbow.RainbowModel.BANDS.forEach { band ->
            assertFalse(band.name.contains("Buy", ignoreCase = true))
            assertFalse(band.name.contains("Sell", ignoreCase = true))
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
        val service = GeminiAiService(hubBaseUrl = "")
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

    @Test
    fun whyCardDashesWhenTapeIsMissingOrMismatched() {
        val empty = com.example.data.model.CoinMovementDriverMapper.rows(
            symbol = "BTC",
            change24h = 2.4,
            hasLiveChange24h = false,
            report = com.example.data.model.MarketIntelligenceReport()
        )
        assertTrue(empty.all { it.intensity == "—" && it.bars == 0 })

        val waiting = MarketIntelligenceEngine.analyze(
            symbol = "BTCUSDT",
            ticker = null,
            bookTicker = null,
            markFunding = null,
            openInterest = null,
            recentTrades = emptyList(),
            recentLiquidations = emptyList(),
            macroSentiment = MacroMarketSentiment()
        )
        val waitingRows = com.example.data.model.CoinMovementDriverMapper.rows(
            symbol = "BTC",
            change24h = 1.2,
            hasLiveChange24h = true,
            report = waiting
        )
        assertEquals("24h change", waitingRows.first { it.key == "spot" }.labelEn)
        assertTrue(waitingRows.first { it.key == "spot" }.isLive)
        assertEquals("—", waitingRows.first { it.key == "oi" }.intensity)
        assertEquals("—", waitingRows.first { it.key == "funding" }.intensity)
        assertEquals("—", waitingRows.first { it.key == "liq" }.intensity)
        assertEquals(0, waitingRows.first { it.key == "oi" }.bars)

        val liveFunding = MarketIntelligenceEngine.analyze(
            symbol = "BTCUSDT",
            ticker = null,
            bookTicker = null,
            markFunding = FuturesMarkFunding(
                symbol = "BTCUSDT",
                markPrice = 90_000.0,
                indexPrice = 90_000.0,
                basis = 0.0,
                basisPercent = 0.0,
                fundingRate = 0.00012,
                nextFundingTimeMs = 0L,
                approxApr = 0.0,
                eventTimeMs = 1L,
                receivedTimeMs = 1L,
                fromExchange = true
            ),
            openInterest = com.example.data.model.FuturesOpenInterest(
                symbol = "BTCUSDT",
                openInterest = 12_000.0,
                openInterestUsd = 1_200_000_000.0,
                lastRefreshTimeMs = 1L,
                isAvailable = true
            ),
            recentTrades = emptyList(),
            recentLiquidations = emptyList(),
            macroSentiment = MacroMarketSentiment(fearAndGreedValue = 62, fearAndGreedClassification = "Greed")
        )
        val liveRows = com.example.data.model.CoinMovementDriverMapper.rows(
            symbol = "BTC",
            change24h = 3.1,
            hasLiveChange24h = true,
            report = liveFunding,
            etf = BitcoinEtfFlowData(oneDayNetFlowMillionUsd = 180.0, isLive = true)
        )
        assertEquals("0.0120%", liveRows.first { it.key == "funding" }.intensity)
        assertTrue(liveRows.first { it.key == "oi" }.intensity.contains("B"))
        assertEquals("62", liveRows.first { it.key == "macro" }.intensity)
        assertEquals("+180.0M", liveRows.first { it.key == "etf" }.intensity)

        val ethOnBtcTape = com.example.data.model.CoinMovementDriverMapper.rows(
            symbol = "ETH",
            change24h = 1.0,
            hasLiveChange24h = true,
            report = liveFunding,
            etf = BitcoinEtfFlowData(oneDayNetFlowMillionUsd = 180.0, isLive = true)
        )
        assertEquals("—", ethOnBtcTape.first { it.key == "funding" }.intensity)
        assertTrue(ethOnBtcTape.none { it.key == "etf" })
    }

    @Test
    fun leftoverChromeIsNotATradeCallOrOnChainLie() {
        val packs = listOf(
            AppStrings(),
            GreekAppStrings(),
            com.example.util.FrenchAppStrings(),
            com.example.util.GermanAppStrings(),
            com.example.util.SpanishAppStrings(),
            com.example.util.ItalianAppStrings()
        )
        packs.forEach { strings ->
            assertFalse(strings.signalBullish.contains("BULLISH", ignoreCase = true))
            assertFalse(strings.signalBearish.contains("BEARISH", ignoreCase = true))
            assertFalse(strings.whaleRadarTitle.contains("ON-CHAIN", ignoreCase = true))
            assertFalse(strings.whaleRadarSub.contains("10M"))
            assertFalse(strings.derivativesRiskGuardrail.contains("1-2%"))
            assertFalse(strings.derivativesRiskGuardrail.contains("1-2"))
            assertTrue(strings.whaleRadarTitle.contains("USDT-M"))
            assertTrue(strings.derivativesRiskGuardrail.contains("tape") || strings.derivativesRiskGuardrail.contains("ταινία") || strings.derivativesRiskGuardrail.contains("nastro") || strings.derivativesRiskGuardrail.contains("cinta") || strings.derivativesRiskGuardrail.contains("Tape") || strings.derivativesRiskGuardrail.contains("bande"))
            assertFalse(strings.navSignals.equals("Signals", ignoreCase = true))
            assertFalse(strings.navSignals.equals("Σήματα", ignoreCase = true))
            assertTrue(strings.navCoins.isNotBlank())
        }
    }
}
