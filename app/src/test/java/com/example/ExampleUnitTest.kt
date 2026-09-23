package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.LiveMarketContextSnapshot
import com.example.data.network.GeminiAiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testGeminiAiServiceDirectQuery() = runBlocking {
    val service = GeminiAiService(apiKeyOverride = "")
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

    // Test conversational "are you live" query
    val liveCheckResponse = service.analyzeMarketQuery(
      prompt = "are you livee",
      snapshot = snapshot,
      language = AppLanguage.ENGLISH
    )
    assertNotNull(liveCheckResponse)
    assertTrue("Response should acknowledge being live", liveCheckResponse.contains("live", ignoreCase = true) || liveCheckResponse.contains("online", ignoreCase = true))
  }

  @Test
  fun prefersCheapestGeminiModelsFirst() {
    assertEquals(
      listOf("gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.6-flash"),
      GeminiAiService.MODELS
    )
  }
}

