package com.example.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs

/**
 * Dated facts placed on a halving-cycle day. The line always says what already
 * happened in that past cycle. It never says the same thing will happen next.
 */
object CycleDayLessons {
    private const val DAY_MS = 86_400_000L

    data class Lesson(
        val cycle: Int,
        val day: Int,
        val textEn: String,
        val textEl: String
    )

    private val events: List<Lesson> = listOf(
        lesson(2012, 2013, 12, 4, "4 Dec 2013 close was the cycle high. The coin market then was mostly Bitcoin.", "Το κλείσιμο της 4ης Δεκ 2013 ήταν το υψηλό του κύκλου. Η αγορά τότε ήταν σχεδόν μόνο Bitcoin."),
        lesson(2012, 2015, 1, 14, "14 Jan 2015 close was the later cycle low. The months after that low were a long quiet grind, not a new high.", "Το κλείσιμο της 14ης Ιαν 2015 ήταν το μεταγενέστερο χαμηλό. Οι μήνες μετά ήταν μακρά ήσυχη τριβή, όχι νέο υψηλό."),
        lesson(2016, 2017, 12, 16, "16 Dec 2017 close was Bitcoin's cycle high. ETH's high of that cycle came later, in January 2018.", "Το κλείσιμο της 16ης Δεκ 2017 ήταν το υψηλό του Bitcoin. Το υψηλό του ETH σε εκείνον τον κύκλο ήρθε μετά, τον Ιανουάριο του 2018."),
        lesson(2016, 2018, 12, 15, "15 Dec 2018 close was the later cycle low. Alts fell with Bitcoin. There was no separate rescue for them.", "Το κλείσιμο της 15ης Δεκ 2018 ήταν το μεταγενέστερο χαμηλό. Τα alts έπεσαν μαζί με το Bitcoin. Δεν υπήρξε ξεχωριστή διάσωση."),
        lesson(2020, 2021, 5, 19, "19 May 2021 was the policy and leverage unwind. DOGE had already peaked on 8 May 2021, before that drop.", "Η 19η Μαΐ 2021 ήταν το ξετύλιγμα πολιτικής και μόχλευσης. Το DOGE είχε ήδη κορυφωθεί στις 8 Μαΐ 2021, πριν από εκείνη την πτώση."),
        lesson(2020, 2021, 11, 8, "8 Nov 2021 close was Bitcoin's cycle high. ETH printed its high in the same days, and SOL's high of that cycle was a few days earlier.", "Το κλείσιμο της 8ης Νοε 2021 ήταν το υψηλό του Bitcoin. Το ETH τύπωσε το δικό του υψηλό τις ίδιες μέρες, και το υψηλό του SOL σε εκείνον τον κύκλο ήταν λίγες μέρες νωρίτερα."),
        lesson(2020, 2022, 11, 9, "9 Nov 2022 close was the FTX-week low. SOL, tied to that venue, fell hard in the same weeks.", "Το κλείσιμο της 9ης Νοε 2022 ήταν το χαμηλό της εβδομάδας FTX. Το SOL, δεμένο με εκείνο το ανταλλακτήριο, έπεσε απότομα τις ίδιες εβδομάδες."),
        lesson(2020, 2024, 1, 10, "10 Jan 2024 was the spot ETF approval. Bitcoin did not surge that week. The information was already in the price.", "Η 10η Ιαν 2024 ήταν η έγκριση του spot ETF. Το Bitcoin δεν εκτοξεύτηκε εκείνη τη βδομάδα. Η πληροφορία ήταν ήδη στην τιμή."),
        lesson(2024, 2025, 10, 6, "6 Oct 2025 is the highest close so far in this open cycle. Not a finished top.", "Η 6η Οκτ 2025 είναι το υψηλότερο κλείσιμο ως τώρα σε αυτόν τον ανοιχτό κύκλο. Όχι τελειωμένη κορυφή."),
        lesson(2024, 2025, 10, 10, "10 Oct 2025 was a leverage unwind on the futures tape.", "Η 10η Οκτ 2025 ήταν ξετύλιγμα μόχλευσης στην ταινία futures.")
    )

    fun paragraphs(day: Int, greek: Boolean): List<String> {
        if (day < 0) return emptyList()
        val intro = if (greek) {
            "Ημέρα $day αυτού του halving. Τα παρακάτω έγιναν ήδη στην ίδια μέτρηση παλιότερων κύκλων. Αύριο η μέτρηση αλλάζει, και μπορεί να αλλάξει και η πιο κοντινή σημείωση."
        } else {
            "Day $day of this halving. The notes below already happened at this same count in earlier cycles. Tomorrow the count moves, and the nearest note can change."
        }
        val picked = nearest(day, limit = 3)
        val lines = picked.map { line(it, day, greek) }
        val finished = events.filter { it.cycle != 2024 }
        val quiet = finished.groupBy { it.cycle }.values.all { group ->
            group.minOf { abs(it.day - day) } > 45
        }
        val quietLine = if (!quiet) {
            null
        } else if (greek) {
            "Σε αυτή την ημέρα και οι τρεις τελειωμένοι κύκλοι είναι ήσυχοι στον χάρτη: κανένα καταγεγραμμένο γεγονός δεν πέφτει μέσα σε 45 μέρες. Η πιο κοντινή σημείωση μένει από κάτω, για να φαίνεται πόσο μακριά είναι."
        } else {
            "On this day-count all three finished cycles are quiet on this map: no dated fact falls within 45 days. The nearest note stays below, so you can see how far away it sits."
        }
        return listOf(intro) + listOfNotNull(quietLine) + lines
    }

    fun nearest(day: Int, limit: Int = 3): List<Lesson> {
        if (day < 0) return emptyList()
        return events.sortedBy { abs(it.day - day) }.take(limit)
    }

    fun line(lesson: Lesson, today: Int, greek: Boolean): String {
        val delta = lesson.day - today
        val fact = if (greek) lesson.textEl else lesson.textEn
        val whenText = when {
            delta == 0 -> if (greek) {
                "Την ίδια ημέρα ${lesson.cycle}: $fact"
            } else {
                "On this day-count in ${lesson.cycle}: $fact"
            }
            delta > 0 -> if (greek) {
                "Στον κύκλο ${lesson.cycle}, $delta μέρες μετά από αυτή την ημέρα: $fact"
            } else {
                "In the ${lesson.cycle} cycle, $delta days after this day-count: $fact"
            }
            else -> if (greek) {
                "Στον κύκλο ${lesson.cycle}, ${-delta} μέρες πριν από αυτή την ημέρα: $fact"
            } else {
                "In the ${lesson.cycle} cycle, ${-delta} days before this day-count: $fact"
            }
        }
        return whenText
    }

    private fun lesson(cycle: Int, year: Int, month: Int, day: Int, en: String, el: String): Lesson {
        val halving = when (cycle) {
            2012 -> 1354116278000L
            2016 -> 1468082773000L
            2020 -> 1589217823000L
            else -> HalvingCycleUtils.HALVING_4TH_TIMESTAMP
        }
        val eventMs = utc(year, month, day)
        val cycleDay = ((eventMs - halving) / DAY_MS).toInt()
        return Lesson(
            cycle = cycle,
            day = cycleDay,
            textEn = en,
            textEl = el
        )
    }

    private fun utc(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(year, month - 1, day, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
