package com.example.util

import java.util.Locale
import kotlin.math.abs

/**
 * One reading of where Bitcoin is on the halving clock.
 * The live multiple uses today's price over the 20 Apr 2024 halving close.
 * Past multiples come from real same-day closes. No price target.
 */
object WhereWeAreReading {
    const val HALVING_CLOSE_2024_USD = 64_907.99

    data class Tape(
        val fundingPct: String,
        val openInterest: String,
        val liquidations: String
    )

    data class View(
        val dayText: String,
        val nowMultiple: String,
        val multiple2012: String,
        val multiple2016: String,
        val multiple2020: String,
        val tape: Tape,
        val familyEn: String = "Calm",
        val familyEl: String = "Ηρεμία"
    )

    fun liveMultiple(priceUsd: Double, priceIsLive: Boolean): Double? {
        if (!priceIsLive || priceUsd <= 0.0) return null
        return priceUsd / HALVING_CLOSE_2024_USD
    }

    fun build(
        day: Int?,
        priceUsd: Double,
        priceIsLive: Boolean,
        multiple2012: Double?,
        multiple2016: Double?,
        multiple2020: Double?,
        fundingRate: Double?,
        openInterestUsd: Double?,
        liquidationUsd: Double?
    ): View {
        return View(
            dayText = day?.takeIf { it >= 0 }?.toString() ?: "—",
            nowMultiple = CycleReadingText.formatMultiple(liveMultiple(priceUsd, priceIsLive)),
            multiple2012 = CycleReadingText.formatMultiple(multiple2012),
            multiple2016 = CycleReadingText.formatMultiple(multiple2016),
            multiple2020 = CycleReadingText.formatMultiple(multiple2020),
            tape = Tape(
                fundingPct = formatFunding(fundingRate),
                openInterest = formatUsd(openInterestUsd),
                liquidations = formatUsd(liquidationUsd)
            )
        )
    }

    private fun formatFunding(rate: Double?): String {
        if (rate == null || abs(rate) > 1.0) return "—"
        val pct = rate * 100.0
        return String.format(Locale.US, "%+.4f%%", pct)
    }

    private fun formatUsd(value: Double?): String {
        if (value == null || value <= 0.0) return "—"
        return AppNumberFormatter.formatCompactCurrency(value)
    }
}
