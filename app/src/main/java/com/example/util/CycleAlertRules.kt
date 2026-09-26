package com.example.util

import java.util.Locale

/**
 * Pure rules for the cycle push alerts (no Android), so they can be unit tested.
 * Every alert fires on a change of state, never on a level that merely persists,
 * and the text states what happened, not what will happen.
 */
object CycleAlertRules {

    const val PI_NEAR_GAP = 0.10
    const val SMA_NEAR_RATIO = 1.10
    const val SMA_STRETCHED_RATIO = 2.5
    const val FUNDING_HIGH_PCT = 0.05
    const val FUNDING_LOW_PCT = -0.04

    enum class PiState { FAR, NEAR, CROSSED }
    enum class SmaState { BELOW, NEAR, ABOVE, STRETCHED }
    enum class FundingState { NORMAL, HIGH, LOW }

    data class Message(val title: String, val body: String)

    fun sma(values: List<Double>, period: Int): Double? =
        if (period <= 0 || values.size < period) null else values.takeLast(period).average()

    /** Gap between the 111-day SMA and 2 × the 350-day SMA, as a share of the latter. */
    fun piGap(dma111: Double, dma350x2: Double): Double = (dma350x2 - dma111) / dma350x2

    fun piState(dma111: Double, dma350x2: Double): PiState {
        val gap = piGap(dma111, dma350x2)
        return when {
            gap <= 0.0 -> PiState.CROSSED
            gap < PI_NEAR_GAP -> PiState.NEAR
            else -> PiState.FAR
        }
    }

    fun smaState(price: Double, ma200w: Double): SmaState {
        val ratio = price / ma200w
        return when {
            ratio < 1.0 -> SmaState.BELOW
            ratio < SMA_NEAR_RATIO -> SmaState.NEAR
            ratio < SMA_STRETCHED_RATIO -> SmaState.ABOVE
            else -> SmaState.STRETCHED
        }
    }

    fun fundingState(ratePct: Double): FundingState = when {
        ratePct >= FUNDING_HIGH_PCT -> FundingState.HIGH
        ratePct <= FUNDING_LOW_PCT -> FundingState.LOW
        else -> FundingState.NORMAL
    }

    /** The first reading only sets the baseline; after that, any change of state is news. */
    fun changed(previous: String?, current: String): Boolean = previous != null && previous != current

    fun rainbowMessage(fromBand: String, toBand: String, price: Double, greek: Boolean): Message =
        if (greek) {
            Message("Rainbow: νέα ζώνη", "Το Bitcoin (${usd(price)}) πέρασε από «$fromBand» σε «$toBand» στο μοντέλο Rainbow.")
        } else {
            Message("Rainbow: new band", "Bitcoin (${usd(price)}) moved from \"$fromBand\" to \"$toBand\" on the Rainbow model.")
        }

    fun zoneMessage(fromZone: String, toZone: String, greek: Boolean): Message =
        if (greek) {
            Message("Κλίμα αγοράς: νέα ζώνη", "Από «$fromZone» σε «$toZone». Βάση: Fear & Greed και funding.")
        } else {
            Message("Market mood: new zone", "From \"$fromZone\" to \"$toZone\". Based on Fear & Greed and funding.")
        }

    fun piMessage(state: PiState, dma111: Double, dma350x2: Double, greek: Boolean): Message {
        val gap = pct(piGap(dma111, dma350x2) * 100.0)
        return when (state) {
            PiState.CROSSED -> if (greek) {
                Message("Pi Cycle: διασταύρωση", "Ο 111DMA (${usd(dma111)}) πέρασε πάνω από τον 350DMA × 2 (${usd(dma350x2)}).")
            } else {
                Message("Pi Cycle: cross", "The 111DMA (${usd(dma111)}) moved above the 350DMA × 2 (${usd(dma350x2)}).")
            }
            PiState.NEAR -> if (greek) {
                Message("Pi Cycle: η απόσταση μίκρυνε", "Ο 111DMA είναι $gap κάτω από τον 350DMA × 2 (κάτω από ${pct(PI_NEAR_GAP * 100.0)}).")
            } else {
                Message("Pi Cycle: gap narrowed", "The 111DMA is $gap below the 350DMA × 2 (under ${pct(PI_NEAR_GAP * 100.0)}).")
            }
            PiState.FAR -> if (greek) {
                Message("Pi Cycle: η απόσταση μεγάλωσε", "Ο 111DMA είναι $gap κάτω από τον 350DMA × 2.")
            } else {
                Message("Pi Cycle: gap widened", "The 111DMA is $gap below the 350DMA × 2.")
            }
        }
    }

    fun smaMessage(state: SmaState, price: Double, ma200w: Double, greek: Boolean): Message {
        val ratio = String.format(Locale.US, "%.2f×", price / ma200w)
        val line = if (greek) {
            "Τιμή ${usd(price)}, 200W SMA ${usd(ma200w)} ($ratio)."
        } else {
            "Price ${usd(price)}, 200W SMA ${usd(ma200w)} ($ratio)."
        }
        val title = when (state) {
            SmaState.BELOW -> if (greek) "200W SMA: η τιμή πέρασε κάτω" else "200W SMA: price moved below"
            SmaState.NEAR -> if (greek) "200W SMA: η τιμή πλησίασε" else "200W SMA: price is within 10%"
            SmaState.ABOVE -> if (greek) "200W SMA: κανονική απόσταση" else "200W SMA: back to a normal distance"
            SmaState.STRETCHED -> if (greek) "200W SMA: 2.5× ή περισσότερο πάνω" else "200W SMA: 2.5× or more above"
        }
        return Message(title, line)
    }

    fun fundingMessage(state: FundingState, ratePct: Double, greek: Boolean): Message {
        val rate = String.format(Locale.US, "%+.4f%%", ratePct)
        return when (state) {
            FundingState.HIGH -> if (greek) {
                Message("Funding BTC υψηλό", "Το funding του BTCUSDT στη Binance είναι $rate (όριο ${pctRate(FUNDING_HIGH_PCT)}). Οι long πληρώνουν τους short.")
            } else {
                Message("BTC funding high", "Binance BTCUSDT funding is $rate (threshold ${pctRate(FUNDING_HIGH_PCT)}). Longs are paying shorts.")
            }
            FundingState.LOW -> if (greek) {
                Message("Funding BTC αρνητικό", "Το funding του BTCUSDT στη Binance είναι $rate (όριο ${pctRate(FUNDING_LOW_PCT)}). Οι short πληρώνουν τους long.")
            } else {
                Message("BTC funding negative", "Binance BTCUSDT funding is $rate (threshold ${pctRate(FUNDING_LOW_PCT)}). Shorts are paying longs.")
            }
            FundingState.NORMAL -> if (greek) {
                Message("Funding BTC κανονικό", "Το funding του BTCUSDT στη Binance επέστρεψε στο $rate.")
            } else {
                Message("BTC funding back to normal", "Binance BTCUSDT funding is back to $rate.")
            }
        }
    }

    fun whaleMessage(symbol: String, isSell: Boolean, valueUsd: Double, greek: Boolean): Message =
        if (greek) {
            Message(
                if (isSell) "Μεγάλη πώληση futures" else "Μεγάλη αγορά futures",
                "${if (isSell) "Πώληση" else "Αγορά"} $symbol ${usd(valueUsd)} στα Binance USDT-M futures."
            )
        } else {
            Message(
                if (isSell) "Large futures sell" else "Large futures buy",
                "A ${usd(valueUsd)} $symbol ${if (isSell) "sell" else "buy"} on Binance USDT-M futures."
            )
        }

    private fun usd(v: Double): String = String.format(Locale.US, "$%,.0f", v)
    private fun pct(v: Double): String = String.format(Locale.US, "%.1f%%", v)
    private fun pctRate(v: Double): String = String.format(Locale.US, "%+.2f%%", v)
}
