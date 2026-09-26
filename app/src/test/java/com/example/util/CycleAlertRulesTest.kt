package com.example.util

import com.example.util.CycleAlertRules.FundingState
import com.example.util.CycleAlertRules.PiState
import com.example.util.CycleAlertRules.SmaState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleAlertRulesTest {

    @Test fun smaNeedsEnoughValues() {
        assertNull(CycleAlertRules.sma(listOf(1.0, 2.0), 3))
        assertEquals(3.0, CycleAlertRules.sma(listOf(10.0, 2.0, 3.0, 4.0), 3)!!, 1e-12)
    }

    @Test fun piStates() {
        assertEquals(PiState.FAR, CycleAlertRules.piState(80_000.0, 100_000.0))
        assertEquals(PiState.NEAR, CycleAlertRules.piState(91_000.0, 100_000.0))
        assertEquals(PiState.CROSSED, CycleAlertRules.piState(100_000.0, 100_000.0))
        assertEquals(PiState.CROSSED, CycleAlertRules.piState(105_000.0, 100_000.0))
    }

    @Test fun smaStates() {
        assertEquals(SmaState.BELOW, CycleAlertRules.smaState(49_000.0, 50_000.0))
        assertEquals(SmaState.NEAR, CycleAlertRules.smaState(54_000.0, 50_000.0))
        assertEquals(SmaState.ABOVE, CycleAlertRules.smaState(84_000.0, 50_000.0))
        assertEquals(SmaState.STRETCHED, CycleAlertRules.smaState(125_000.0, 50_000.0))
    }

    @Test fun fundingStates() {
        assertEquals(FundingState.NORMAL, CycleAlertRules.fundingState(0.01))
        assertEquals(FundingState.HIGH, CycleAlertRules.fundingState(0.05))
        assertEquals(FundingState.LOW, CycleAlertRules.fundingState(-0.04))
    }

    @Test fun firstReadingIsABaselineNotAnAlert() {
        assertFalse(CycleAlertRules.changed(null, "HIGH"))
        assertFalse(CycleAlertRules.changed("HIGH", "HIGH"))
        assertTrue(CycleAlertRules.changed("NORMAL", "HIGH"))
    }

    @Test fun messagesStateFactsNotForecasts() {
        val words = listOf("will", "likely", "probable", "squeeze", "flush", "buy now", "sell now", "πιθαν", "κίνδυνος")
        val all = buildList {
            for (greek in listOf(false, true)) {
                add(CycleAlertRules.rainbowMessage("Lowest Band", "Accumulate", 84_000.0, greek))
                add(CycleAlertRules.zoneMessage("Zone 2: Neutral mood", "Zone 1: Fear", greek))
                PiState.entries.forEach { add(CycleAlertRules.piMessage(it, 90_000.0, 100_000.0, greek)) }
                SmaState.entries.forEach { add(CycleAlertRules.smaMessage(it, 84_000.0, 50_000.0, greek)) }
                FundingState.entries.forEach { add(CycleAlertRules.fundingMessage(it, 0.06, greek)) }
                add(CycleAlertRules.whaleMessage("BTC", true, 2_000_000.0, greek))
            }
        }
        all.forEach { m ->
            val text = (m.title + " " + m.body).lowercase()
            assertTrue(m.title.isNotBlank() && m.body.isNotBlank())
            words.forEach { assertFalse("'$it' in: $text", text.contains(it)) }
        }
    }

    @Test fun messagesCarryTheNumbers() {
        val pi = CycleAlertRules.piMessage(PiState.NEAR, 92_000.0, 100_000.0, false)
        assertTrue(pi.body.contains("8.0%"))
        val sma = CycleAlertRules.smaMessage(SmaState.NEAR, 54_000.0, 50_000.0, false)
        assertTrue(sma.body.contains("$54,000") && sma.body.contains("1.08×"))
        val funding = CycleAlertRules.fundingMessage(FundingState.HIGH, 0.0612, false)
        assertTrue(funding.body.contains("+0.0612%"))
    }
}
