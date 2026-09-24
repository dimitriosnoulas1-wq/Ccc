package com.example

import com.example.billing.BillingManager
import com.example.util.AppStrings
import com.example.util.FrenchAppStrings
import com.example.util.GermanAppStrings
import com.example.util.GreekAppStrings
import com.example.util.ItalianAppStrings
import com.example.util.SpanishAppStrings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProUpgradeCopyTest {

    private val packs = listOf(
        AppStrings(),
        GreekAppStrings(),
        FrenchAppStrings(),
        GermanAppStrings(),
        SpanishAppStrings(),
        ItalianAppStrings()
    )

    @Test
    fun proPitchDoesNotPromiseInventedProductsOrWrongPrice() {
        packs.forEach { strings ->
            val pitch = listOf(
                strings.proBenefit1,
                strings.proBenefit2,
                strings.proBenefit3,
                strings.proBenefit4,
                strings.proBenefit5,
                strings.proBenefit6,
                strings.proModalSubtitle,
                strings.proModalFeature1,
                strings.proModalFeature2,
                strings.proModalFeature3,
                strings.proModalFeature4,
                strings.proModalFeature5,
                strings.proModalFeature6,
                strings.signalsProLockedTitle,
                strings.signalsProLockedSubtitle,
                strings.signalsProLockedFeature2,
                strings.signalsProLockedFeature3,
                strings.signalsGateTitle,
                strings.signalsGateDesc,
                strings.macroProLockedTitle,
                strings.macroProLockedSubtitle,
                strings.macroGateTitle,
                strings.macroGateDesc,
                strings.proAnalyticsDesc,
                strings.proTapeDesc,
                strings.historicalAnalogProUnlockText,
                strings.proMembershipPrice,
                strings.proPriceTrialLine,
                strings.planMonthlyPrice,
                strings.planAnnualPrice,
                strings.planAnnualBadge,
                strings.startFreeTrialSub,
                strings.continueYearlyBtn,
                strings.continueYearlySub
            ).joinToString("\n")
            assertFalse(pitch.contains("Masterclass", ignoreCase = true))
            assertFalse(pitch.contains("4.79"))
            assertFalse(pitch.contains("19.99"))
            assertFalse(pitch.contains("29.99"))
            assertFalse(pitch.contains("29,99"))
            assertFalse(pitch.contains("3.99"))
            assertFalse(pitch.contains("3,99"))
            assertFalse(pitch.contains("34.99"))
            assertFalse(pitch.contains("34,99"))
            assertFalse(pitch.contains("40%"))
            assertFalse(pitch.contains("predictive peak", ignoreCase = true))
            assertFalse(pitch.contains("Apex target", ignoreCase = true))
            assertFalse(pitch.contains("institutional-grade", ignoreCase = true))
            assertFalse(pitch.contains("Chapters 18-22", ignoreCase = true))
            assertFalse(pitch.contains("whale", ignoreCase = true))
            assertTrue(
                strings.proPriceTrialLine.contains("2.99") || strings.proPriceTrialLine.contains("2,99")
            )
            assertTrue(
                strings.planAnnualPrice.contains("24.99") || strings.planAnnualPrice.contains("24,99")
            )
            assertTrue(
                strings.proModalSubtitle.contains("stay free", ignoreCase = true) ||
                    strings.proModalSubtitle.contains("μένουν δωρεάν") ||
                    strings.proModalSubtitle.contains("restent gratuits") ||
                    strings.proModalSubtitle.contains("bleiben frei") ||
                    strings.proModalSubtitle.contains("siguen gratis") ||
                    strings.proModalSubtitle.contains("restano gratis")
            )
        }
    }

    @Test
    fun billingDefaultsMatchTheHonestPrice() {
        assertTrue(BillingManager.DEFAULT_MONTHLY_PRICE.contains("2.99"))
        assertTrue(BillingManager.DEFAULT_YEARLY_PRICE.contains("24.99"))
    }

    @Test
    fun englishFreePitchNamesTheLiveTape() {
        val en = AppStrings()
        assertTrue(en.proBenefit4.contains("tape", ignoreCase = true))
        assertTrue(en.proBenefit4.contains("not a forecast", ignoreCase = true))
        assertTrue(en.proBenefit6.contains("lessons", ignoreCase = true))
        assertTrue(en.proModalSubtitle.contains("Not a forecast"))
        assertTrue(en.proModalSubtitle.contains("stay free"))
    }
}
