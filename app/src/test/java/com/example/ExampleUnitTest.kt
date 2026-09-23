package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.LiveMarketContextSnapshot
import com.example.data.network.GeminiAiService
import com.example.util.GreekAppStrings
import com.example.util.AppStrings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testGeminiAiServiceDirectQuery() = runBlocking {
    val service = GeminiAiService(apiKeyOverride = "", openAiKeyOverride = "")
    val snapshot = LiveMarketContextSnapshot(
      btcPrice = 85200.0,
      btc24hChange = 2.4,
      ethPrice = 2750.0,
      solPrice = 180.0,
      fundingRatePct = 0.01,
      fearAndGreedScore = 65,
      fearAndGreedSentiment = "Greed",
      btcDominancePct = 58.5,
      altcoinSeasonIndex = 42,
      daysSinceHalving = 500
    )

    // Test Greek query
    val greekResponse = service.analyzeMarketQuery(
      prompt = "Πότε είναι το halving και ποια είναι η τρέχουσα τιμή;",
      snapshot = snapshot,
      language = AppLanguage.GREEK
    )
    assertNotNull(greekResponse)
    assertTrue("Response should not be empty", greekResponse.isNotBlank())

    val liveCheckResponse = service.analyzeMarketQuery(
      prompt = "are you livee",
      snapshot = snapshot,
      language = AppLanguage.ENGLISH
    )
    assertEquals("I'm here. What would you like to see?", liveCheckResponse)

    val greekHello = service.analyzeMarketQuery(
      prompt = "γεια σου",
      snapshot = snapshot,
      language = AppLanguage.GREEK
    )
    assertEquals("Είμαι εδώ. Τι θα θέλατε να δούμε;", greekHello)

    assertFalse(service.isGreetingOnly("τιμή BTC τώρα"))
    assertTrue(service.isGreetingOnly("hello"))
  }

  @Test
  fun aiGreetingIsACleanPresenceLine() {
    assertEquals("I'm here. What would you like to see?", AppStrings().aiGreeting)
    assertEquals("Είμαι εδώ. Τι θα θέλατε να δούμε;", GreekAppStrings().aiGreeting)
  }

  @Test
  fun prefersCheapestGeminiModelsFirst() {
    assertEquals(
      listOf("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.6-flash"),
      GeminiAiService.MODELS
    )
  }

  @Test
  fun prefersCheapestOpenAiBackupModelsFirst() {
    assertEquals(
      listOf("gpt-5-nano", "gpt-4.1-nano", "gpt-4o-mini"),
      GeminiAiService.OPENAI_MODELS
    )
  }

  @Test
  fun acceptsGptSecretNameInAnyCaseAsOpenAiBackup() {
    assertTrue(GeminiAiService.OPENAI_SECRET_ALIASES.any { it.equals("gpt", ignoreCase = true) })
    assertEquals(
      "sk-test-backup",
      GeminiAiService.firstNamedValue(
        mapOf("Gpt" to "sk-test-backup"),
        *GeminiAiService.OPENAI_SECRET_ALIASES
      )
    )
    assertEquals(
      "sk-test-backup",
      GeminiAiService.firstNamedValue(
        mapOf("GPT" to "sk-test-backup"),
        *GeminiAiService.OPENAI_SECRET_ALIASES
      )
    )
    assertEquals(
      "sk-from-openai",
      GeminiAiService.firstNamedValue(
        mapOf("OPENAI_API_KEY" to "sk-from-openai", "Gpt" to "sk-other"),
        *GeminiAiService.OPENAI_SECRET_ALIASES
      )
    )
    assertEquals(
      "",
      GeminiAiService.firstNamedValue(
        mapOf("Gemini" to "unused"),
        *GeminiAiService.OPENAI_SECRET_ALIASES
      )
    )
  }

  @Test
  fun liveTelemetryDefaultsAreEmptyNotDemo() {
    val etf = com.example.data.model.BitcoinEtfFlowData()
    assertFalse(etf.isLive)
    assertEquals(0.0, etf.oneDayNetFlowMillionUsd, 0.0)
    val stables = com.example.data.model.StablecoinLiquidityData()
    assertFalse(stables.isLive)
    assertEquals(0.0, stables.totalCirculatingUsd, 0.0)
    val av = com.example.data.model.LiveMovingAverages(dma350 = 50_000.0, isLive = true)
    assertEquals(100_000.0, av.dma350x2)
    val snapshot = com.example.data.model.LiveMarketContextSnapshot()
    assertNull(snapshot.whaleNet24h)
    assertNull(snapshot.cyclePhase)
  }

  @Test
  fun catalogSeedDropsInventedProjections() {
    val coin = com.example.data.repository.CoinDataRegistry.getAllCoins().first()
    assertEquals("—", coin.projectedNextMove1w)
    assertEquals("—", coin.projectedNextMove2w)
    assertEquals("—", coin.projectedNextMove4w)
    assertEquals(0.0, coin.analog.projectedCyclePeak, 0.0)
    assertEquals(0.0, coin.analog.projectedCycleBottom, 0.0)
    val moves = com.example.util.CoinLocalization.liveRealizedMoves(coin)
    assertEquals("—", moves.first)
    assertEquals("—", com.example.util.CoinLocalization.getProjected1w(coin, AppLanguage.ENGLISH))
    val (p1, p2, p3) = com.example.util.CoinLocalization.getProbabilitiesForCoin(coin)
    assertEquals(0, p1)
    assertEquals(0, p2)
    assertEquals(0, p3)
  }

  @Test
  fun liveMovesUseRealPrintsNotInventedTargets() {
    val seed = com.example.data.repository.CoinDataRegistry.getAllCoins().first()
    val live = seed.copy(
      priceUsd = 100.0,
      change24h = 2.5,
      athUsd = 200.0,
      sparkline = listOf(90.0, 95.0, 100.0),
      priceUpdatedAtMs = System.currentTimeMillis(),
      quoteState = com.example.data.model.QuoteState.LIVE
    )
    val moves = com.example.util.CoinLocalization.liveRealizedMoves(live)
    assertEquals("+2.5%", moves.first)
    assertEquals("+11.1%", moves.second)
    assertEquals("-50.0%", moves.third)
    assertFalse(com.example.util.AppStrings().backtestBadgeText.contains("72.8"))
    assertFalse(com.example.util.AppStrings().nextMoveHeader.contains("PREDICTED"))
  }
}

