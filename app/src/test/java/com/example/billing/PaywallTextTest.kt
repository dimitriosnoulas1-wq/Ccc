package com.example.billing

import com.example.data.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PaywallTextTest {

    @Test fun isoPeriods() {
        assertEquals(7, PaywallText.isoPeriodDays("P7D"))
        assertEquals(7, PaywallText.isoPeriodDays("P1W"))
        assertEquals(14, PaywallText.isoPeriodDays("P2W"))
        assertEquals(30, PaywallText.isoPeriodDays("P1M"))
        assertEquals(365, PaywallText.isoPeriodDays("P1Y"))
        assertNull(PaywallText.isoPeriodDays("P"))
        assertNull(PaywallText.isoPeriodDays("7 days"))
    }

    @Test fun trialIsOnlyShownWhenPlayHasOne() {
        AppLanguage.entries.forEach { lang ->
            val none = PaywallText.monthly(lang, "3,59 €", null)
            assertNull(none.badge)
            assertTrue(none.sub.contains("3,59 €"))
            assertFalse(none.button.contains("7"))
            val trial = PaywallText.monthly(lang, "3,59 €", 7)
            assertTrue(trial.badge!!.contains("7"))
            assertTrue(trial.sub.contains("3,59 €") && trial.sub.contains("7"))
        }
    }

    @Test fun textsUseThePlayPriceNeverAHardcodedOne() {
        AppLanguage.entries.forEach { lang ->
            val texts = listOf(
                PaywallText.monthly(lang, "X1", 3).sub,
                PaywallText.monthly(lang, "X1", null).sub,
                PaywallText.yearlySub(lang, "Y2"),
                PaywallText.priceLine(lang, "X1", "Y2", 3)
            )
            texts.forEach { t ->
                assertFalse(t, t.contains("2.99") || t.contains("24.99") || t.contains("2.08"))
            }
            assertTrue(PaywallText.yearlySub(lang, "Y2").contains("Y2"))
        }
    }

    @Test fun priceLine() {
        assertEquals("3,59 € / month (7-day free trial) · 29,99 € / year",
            PaywallText.priceLine(AppLanguage.ENGLISH, "3,59 €", "29,99 €", 7))
        assertEquals("3,59 € / μήνα · 29,99 € / έτος",
            PaywallText.priceLine(AppLanguage.GREEK, "3,59 €", "29,99 €", null))
        assertEquals("Prices in Google Play", PaywallText.priceLine(AppLanguage.ENGLISH, "", "29,99 €", 7))
    }
}
