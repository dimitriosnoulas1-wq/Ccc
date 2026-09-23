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
}

