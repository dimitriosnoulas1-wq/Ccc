package com.example.util

/**
 * Live same-cycle-day reading: today's clock and price, then the real
 * close on that same day-count in 2012, 2016 and 2020. Not a forecast.
 */
object CycleSameDayNote {
    fun paragraphs(
        day: Int?,
        priceUsd: Double,
        priceIsLive: Boolean,
        close2012: Double?,
        close2016: Double?,
        close2020: Double?,
        multiple2012: Double?,
        multiple2016: Double?,
        multiple2020: Double?,
        greek: Boolean
    ): List<String> {
        val livePrice = if (priceIsLive && priceUsd > 0.0) priceUsd else null
        val nowMultiple = WhereWeAreReading.liveMultiple(priceUsd, priceIsLive)
        val head = if (greek) {
            "Σήμερα, ημέρα ${dayText(day)} από το halving του 2024: ${money(livePrice)} · ${CycleReadingText.formatMultiple(nowMultiple)} πάνω από εκείνο το κλείσιμο. Οι αριθμοί αλλάζουν με τη ζωντανή τιμή και με την ημέρα."
        } else {
            "Today, day ${dayText(day)} since the 2024 halving: ${money(livePrice)} · ${CycleReadingText.formatMultiple(nowMultiple)} above that close. The numbers move with the live price and with the day."
        }
        val past = listOf(
            pastLine(2012, close2012, multiple2012, greek),
            pastLine(2016, close2016, multiple2016, greek),
            pastLine(2020, close2020, multiple2020, greek)
        )
        return listOf(head) + past
    }

    private fun pastLine(year: Int, close: Double?, multiple: Double?, greek: Boolean): String {
        val closeText = money(close)
        val multipleText = CycleReadingText.formatMultiple(multiple)
        return if (greek) {
            "Ίδια ημέρα κύκλου το $year: κλείσιμο $closeText · $multipleText από το halving εκείνου του κύκλου."
        } else {
            "Same cycle day in $year: close $closeText · $multipleText from that cycle's halving."
        }
    }

    private fun dayText(day: Int?): String = day?.takeIf { it >= 0 }?.toString() ?: "—"

    private fun money(value: Double?): String {
        if (value == null || value <= 0.0) return "—"
        return "$" + AppNumberFormatter.formatRawPrice(value, decimals = 0)
    }
}
