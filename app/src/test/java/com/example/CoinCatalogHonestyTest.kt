package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.Currency
import com.example.data.repository.CoinDatabaseFull
import com.example.util.CoinLocalization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The catalog is identity only. Market facts (rank, ATH/ATL with dates, supply) load live,
 * and no coin gets a made-up profile.
 */
class CoinCatalogHonestyTest {

    private val coins = CoinDatabaseFull.get100Coins()

    @Test
    fun catalogCarriesNoMarketFacts() {
        coins.forEach { c ->
            assertEquals(c.symbol, 0.0, c.athUsd, 0.0)
            assertEquals(c.symbol, "", c.athDate)
            assertEquals(c.symbol, 0.0, c.atlUsd, 0.0)
            assertEquals(c.symbol, "", c.atlDate)
            assertEquals(c.symbol, 0.0, c.circulatingSupply, 0.0)
            assertEquals(c.symbol, 0L, c.metaUpdatedAtMs)
        }
    }

    @Test
    fun unloadedFactsReadAsDashNotZero() {
        val c = coins.first { it.symbol == "BTC" }
        assertEquals("—", c.displayRank)
        assertEquals("—", c.formattedAth(Currency.USD))
        assertEquals("—", c.formattedAtl(Currency.USD))
        assertEquals("—", c.athDateWithDays)
        assertEquals("—", c.formattedSupply(c.circulatingSupply))
        assertEquals(0f, c.circulatingPercentage, 0f)
    }

    @Test
    fun liveMetaShowsRankAndAthDate() {
        val c = coins.first { it.symbol == "BTC" }.copy(
            rank = 1, athUsd = 100_000.0, athDate = "06 Oct 2025", metaUpdatedAtMs = 1L
        )
        assertEquals("#1", c.displayRank)
        assertTrue(c.athDateWithDays.startsWith("06 Oct 2025 ("))
    }

    @Test
    fun noTemplateProfiles() {
        coins.forEach { c ->
            assertTrue(c.symbol, !c.founderOrCreator.contains("Core Team & DAO"))
            assertTrue(c.symbol, c.consensusMechanism != "Proof of Stake / Smart Contract Engine")
            assertTrue(c.symbol, c.genesisDate != "15 Oct 2020")
        }
    }

    @Test
    fun coinsWithoutAWrittenProfileGetNoGenericText() {
        val bare = coins.first { it.symbol == "XMR" }
        AppLanguage.values().forEach { lang ->
            assertEquals(lang.name, "", CoinLocalization.getTechnologyDetails(bare, lang))
            assertEquals(lang.name, "", CoinLocalization.getTokenomicsDetails(bare, lang))
            assertEquals(lang.name, "", CoinLocalization.getWhitepaperSummary(bare, lang))
            assertTrue(lang.name, CoinLocalization.getUseCases(bare, lang).isEmpty())
        }
    }
}
