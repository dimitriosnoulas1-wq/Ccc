package com.example.ui.rainbow

import kotlin.math.abs

/**
 * Ανάλυση "ίδια μέρα σε κάθε κύκλο". Όλοι οι αριθμοί βγαίνουν από τα ημερήσια δεδομένα.
 */
object CycleInsights {

    /** Πόσες μέρες γύρω από την ίδια μέρα θεωρούμε "κοντά". */
    const val NEAR_DAYS = 45L
    /** Παράθυρο για τη μεταβολή πριν/μετά. */
    const val WINDOW_DAYS = 30L

    data class Extreme(val point: PricePoint, val isHigh: Boolean, val final: Boolean)

    /** Κάτι που έγινε κοντά στην ίδια μέρα: [offset] > 0 σημαίνει "μετά". */
    data class Nearby(val day: Long, val offset: Long, val el: String, val en: String, val kind: RainbowEvents.Kind?) {
        fun text(greek: Boolean) = if (greek) el else en
    }

    data class Insight(
        val halving: Halving,
        val isCurrent: Boolean,
        val day: Long,
        val n: Long,
        val price: Double?,
        val band: Int?,
        /** τιμή / τιμή πριν 30 μέρες */
        val change30Before: Double?,
        /** τιμή σε 30 μέρες / τιμή (μόνο για παλιούς κύκλους) */
        val change30After: Double?,
        /** Υψηλότερη τιμή του κύκλου ΜΕΧΡΙ εκείνη τη μέρα */
        val highSoFar: PricePoint?,
        val high: Extreme?,
        val low: Extreme?,
        val nearby: List<Nearby>,
    ) {
        /** τιμή / υψηλό-ως-τότε */
        val fromHigh: Double? get() = if (price != null && highSoFar != null) price / highSoFar.price else null
    }

    // ── Κορυφές / πάτοι από τα δεδομένα ──
    /** Υψηλό του κύκλου (και χαμηλό μετά από αυτό), με δεδομένα έως [today]. */
    fun extremes(series: List<PricePoint>, h: Halving, today: Long): Pair<Extreme?, Extreme?> {
        val end = RainbowModel.nextHalving(h)?.day ?: Long.MAX_VALUE
        val finished = end <= today
        val from = series.lowerBound(h.day)
        val to = series.lowerBound(minOf(end, today + 1))
        if (from >= to) return null to null
        var hi = series[from]
        for (i in from until to) if (series[i].price > hi.price) hi = series[i]
        val high = Extreme(hi, isHigh = true, final = finished)
        val afterHigh = series.lowerBound(hi.day + 1)
        if (afterHigh >= to) return high to null
        var lo = series[afterHigh]
        for (i in afterHigh until to) if (series[i].price < lo.price) lo = series[i]
        return high to Extreme(lo, isHigh = false, final = finished)
    }

    private fun highUntil(series: List<PricePoint>, fromDay: Long, untilDay: Long): PricePoint? {
        val a = series.lowerBound(fromDay); val b = series.lowerBound(untilDay + 1)
        if (a >= b) return null
        var hi = series[a]
        for (i in a until b) if (series[i].price > hi.price) hi = series[i]
        return hi
    }

    fun compute(series: List<PricePoint>, today: Long): List<Insight> {
        if (series.isEmpty()) return emptyList()
        val n = RainbowModel.daysSinceHalving(today)
        val lastDay = series.last().day
        return RainbowModel.HALVINGS.filter { !it.estimated && it.day <= today }.map { h ->
            val d = h.day + n
            val isCurrent = h == RainbowModel.currentHalving(today)
            val price = series.priceNear(d)
            val before = series.priceNear(d - WINDOW_DAYS)
            val after = if (!isCurrent && d + WINDOW_DAYS <= lastDay) series.priceNear(d + WINDOW_DAYS) else null
            val (high, low) = extremes(series, h, today)

            val nearby = buildList {
                RainbowEvents.ALL.forEach { ev ->
                    val off = ev.day - d
                    if (abs(off) <= NEAR_DAYS && ev.day <= today) add(Nearby(ev.day, off, ev.el, ev.en, ev.kind))
                }
                listOfNotNull(high, low).forEach { x ->
                    val off = x.point.day - d
                    if (abs(off) > NEAR_DAYS) return@forEach
                    val (el, en) = extremeLabel(x, h)
                    add(Nearby(x.point.day, off, el, en, null))
                }
            }.sortedBy { abs(it.offset) }

            Insight(
                halving = h, isCurrent = isCurrent, day = d, n = n,
                price = price, band = price?.let { RainbowModel.bandIndex(d, it) },
                change30Before = if (price != null && before != null) price / before else null,
                change30After = if (price != null && after != null) after / price else null,
                highSoFar = highUntil(series, h.day, d),
                high = high, low = low, nearby = nearby,
            )
        }
    }

    fun extremeLabel(x: Extreme, h: Halving): Pair<String, String> {
        val p = usd(x.point.price)
        return when {
            x.isHigh && x.final -> "Κορυφή του κύκλου ${h.year} ($p)" to "Cycle ${h.year} top ($p)"
            x.isHigh -> "Υψηλότερη τιμή ως τώρα στον κύκλο ${h.year} ($p)" to "Highest price so far in the ${h.year} cycle ($p)"
            x.final -> "Πάτος του κύκλου ${h.year} ($p)" to "Cycle ${h.year} bottom ($p)"
            else -> "Χαμηλότερη τιμή μετά την κορυφή, ως τώρα ($p)" to "Lowest price after the high, so far ($p)"
        }
    }

    /** "12 μέρες πριν" / "σε 20 μέρες" / "την ίδια μέρα". */
    fun offsetText(off: Long, greek: Boolean): String = when {
        off == 0L -> if (greek) "Την ίδια μέρα" else "Same day"
        off < 0 -> if (greek) "${-off} ${days(-off, true)} πριν" else "${-off} ${days(-off, false)} before"
        else -> if (greek) "${off} ${days(off, true)} μετά" else "${off} ${days(off, false)} after"
    }

    private fun days(n: Long, greek: Boolean) =
        if (greek) (if (n == 1L) "μέρα" else "μέρες") else (if (n == 1L) "day" else "days")

    /** Σύντομη περίληψη κύκλου σε προτάσεις. */
    fun summary(i: Insight, greek: Boolean): List<String> = buildList {
        i.change30Before?.let {
            add(if (greek) "Τις 30 προηγούμενες μέρες: ${pct(it)}" else "Previous 30 days: ${pct(it)}")
        }
        i.change30After?.let {
            add(if (greek) "Τις 30 επόμενες μέρες: ${pct(it)}" else "Next 30 days: ${pct(it)}")
        }
        val hs = i.highSoFar
        val fh = i.fromHigh
        if (hs != null && fh != null) {
            val ago = i.day - hs.day
            add(
                when {
                    ago <= 1 && greek -> "Σε νέο υψηλό του κύκλου"
                    ago <= 1 -> "At a new cycle high"
                    greek -> "${pct(fh)} από το υψηλό του κύκλου ως τότε (${DateUtil.format(hs.day, true)}, πριν $ago μέρες)"
                    else -> "${pct(fh)} from the cycle high so far (${DateUtil.format(hs.day)}, $ago days earlier)"
                }
            )
        }
    }
}

/**
 * Κανόνας ενημέρωσης του σημερινού σημείου στο rainbow: μία φορά τη μέρα,
 * εκτός αν γίνει μεγάλη κίνηση (±[BIG_MOVE]) ή αλλάξει ζώνη.
 */
object LiveGate {
    const val BIG_MOVE = 0.07

    enum class Reason { NEW_DAY, BIG_MOVE, BAND_CHANGE }

    fun reason(shown: PricePoint?, live: Double, today: Long): Reason? {
        if (live <= 0 || live.isNaN()) return null
        if (shown == null || shown.day != today) return Reason.NEW_DAY
        if (abs(live / shown.price - 1) >= BIG_MOVE) return Reason.BIG_MOVE
        if (RainbowModel.bandIndex(today, live) != RainbowModel.bandIndex(shown.day, shown.price)) return Reason.BAND_CHANGE
        return null
    }
}
