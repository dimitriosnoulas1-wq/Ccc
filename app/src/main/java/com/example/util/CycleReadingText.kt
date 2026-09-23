package com.example.util

import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object CycleReadingText {
    const val DISCLAIMER_EN = "Educational history of past cycles. Not a forecast and not advice."
    const val DISCLAIMER_EL = "Εκπαιδευτικό ιστορικό παρελθόντων κύκλων. Δεν είναι πρόβλεψη και δεν είναι συμβουλή."

    fun disclaimer(greek: Boolean): String = if (greek) DISCLAIMER_EL else DISCLAIMER_EN

    fun formatMultiple(value: Double?): String {
        if (value == null || value <= 0.0) return "—"
        return String.format(Locale.US, "%.2fx", value)
    }

    fun band(multiple: Double?, multiple2016: Double?, multiple2020: Double?, greek: Boolean): String {
        if (multiple == null) {
            return if (greek) "Το πολλαπλάσιο αυτού του κύκλου λείπει." else "This cycle's multiple is missing."
        }
        if (multiple2016 == null && multiple2020 == null) {
            return if (greek) {
                "Δεν υπάρχει κλείσιμο του 2016 ή του 2020 για αυτή την ημέρα."
            } else {
                "No 2016 or 2020 close for this day."
            }
        }
        if (multiple2016 != null && multiple2020 != null) {
            val high = max(multiple2016, multiple2020)
            val low = min(multiple2016, multiple2020)
            return when {
                multiple > high -> if (greek) "Πάνω και από τους δύο παλιούς κύκλους." else "Above both past cycles."
                multiple < low -> if (greek) "Κάτω και από τους δύο παλιούς κύκλους." else "Below both past cycles."
                else -> if (greek) "Ανάμεσα στον κύκλο του 2016 και του 2020." else "Between the 2016 and 2020 cycles."
            }
        }
        val past = multiple2016 ?: multiple2020 ?: return ""
        val year = if (multiple2016 != null) "2016" else "2020"
        return if (multiple >= past) {
            if (greek) "Πάνω από τον κύκλο του $year. Ο άλλος κύκλος λείπει." else "Above the $year cycle. The other cycle is missing."
        } else {
            if (greek) "Κάτω από τον κύκλο του $year. Ο άλλος κύκλος λείπει." else "Below the $year cycle. The other cycle is missing."
        }
    }

    fun alertLine(day: Int, multiple2016: Double?, multiple2020: Double?, greek: Boolean): String {
        val past2016 = formatMultiple(multiple2016).takeIf { it != "—" }
        val past2020 = formatMultiple(multiple2020).takeIf { it != "—" }
        return if (greek) {
            when {
                past2016 != null && past2020 != null ->
                    "Ημέρα $day. Το 2016 αυτή η μέρα ήταν $past2016, το 2020 ήταν $past2020."
                past2016 != null ->
                    "Ημέρα $day. Το 2016 αυτή η μέρα ήταν $past2016. Το κλείσιμο του 2020 λείπει."
                past2020 != null ->
                    "Ημέρα $day. Το 2020 αυτή η μέρα ήταν $past2020. Το κλείσιμο του 2016 λείπει."
                else ->
                    "Ημέρα $day. Τα κλεισίματα των παλιών κύκλων για αυτή την ημέρα λείπουν."
            }
        } else {
            when {
                past2016 != null && past2020 != null ->
                    "Day $day. In 2016 this day-count was $past2016, in 2020 it was $past2020."
                past2016 != null ->
                    "Day $day. In 2016 this day-count was $past2016. The 2020 close is missing."
                past2020 != null ->
                    "Day $day. In 2020 this day-count was $past2020. The 2016 close is missing."
                else ->
                    "Day $day. Past-cycle closes for this day are missing."
            }
        }
    }
}
