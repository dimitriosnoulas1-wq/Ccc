package com.example

import com.example.data.repository.CoinDatabaseFull
import com.example.ui.components.getCoinIconUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoinCatalogIconsTest {
    @Test
    fun catalogHasOneHundredUniqueCoins() {
        val coins = CoinDatabaseFull.get100Coins()
        assertEquals(100, coins.size)
        assertEquals(100, coins.map { it.symbol.uppercase() }.toSet().size)
    }

    @Test
    fun everyCatalogCoinUsesAnOfficialCoinGeckoIcon() {
        CoinDatabaseFull.get100Coins().forEach { coin ->
            val url = getCoinIconUrl(coin.symbol.lowercase())
            assertTrue(
                "${coin.symbol} should use the official CoinGecko CDN, was $url",
                url.startsWith("https://coin-images.coingecko.com/coins/images/")
            )
            assertFalse(
                "${coin.symbol} should not fall back to the generic icon pack",
                url.contains("jsdelivr")
            )
        }
    }

    @Test
    fun rebrandedCoinsUseCurrentBrandMarks() {
        assertTrue(getCoinIconUrl("s").contains("Sonic_Logo"))
        assertFalse(getCoinIconUrl("s").contains("Fantom"))
        assertTrue(getCoinIconUrl("sky").contains("sky.jpg"))
        assertFalse(getCoinIconUrl("sky").contains("Mark_Maker"))
        assertTrue(getCoinIconUrl("fet").contains("ASI.png"))
        assertTrue(getCoinIconUrl("pol").contains("pol.png"))
        assertTrue(getCoinIconUrl("shib").contains("shiba.png"))
        assertTrue(getCoinIconUrl("xlm").contains("fmpFRHHQ"))
        assertTrue(getCoinIconUrl("zk").contains("ZKTokenBlack"))
        assertTrue(getCoinIconUrl("zro").contains("ftxG9_TJ"))
        assertTrue(getCoinIconUrl("w").contains("W_Token"))
    }
}
