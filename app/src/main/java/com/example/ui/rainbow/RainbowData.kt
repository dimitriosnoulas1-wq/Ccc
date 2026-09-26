package com.example.ui.rainbow

import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

data class PricePoint(val day: Long, val price: Double)
data class Band(val name: String, val color: Long)
data class Halving(val year: Int, val day: Long, val estimated: Boolean, val color: Long)
data class CycleCompare(val halving: Halving, val day: Long, val price: Double?, val band: Int?)

object DateUtil {
    private val MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val MONTHS_EL = arrayOf("Ιαν", "Φεβ", "Μαρ", "Απρ", "Μαΐ", "Ιουν", "Ιουλ", "Αυγ", "Σεπ", "Οκτ", "Νοε", "Δεκ")

    fun epochDay(y: Int, m: Int, d: Int): Long {
        val yy = if (m <= 2) y - 1 else y
        val era = (if (yy >= 0) yy else yy - 399) / 400
        val yoe = yy - era * 400
        val doy = (153 * ((m + 9) % 12) + 2) / 5 + d - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097L + doe - 719468L
    }

    fun civil(day: Long): Triple<Int, Int, Int> {
        val z = day + 719468
        val era = (if (z >= 0) z else z - 146096) / 146097
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
        val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
        val y = (yoe + era * 400 + if (m <= 2) 1 else 0).toInt()
        return Triple(y, m, d)
    }

    fun today(): Long = System.currentTimeMillis() / 86_400_000L

    fun format(day: Long, greek: Boolean = false): String {
        val (y, m, d) = civil(day)
        return "$d ${(if (greek) MONTHS_EL else MONTHS)[m - 1]} $y"
    }

}

fun usd(p: Double): String =
    if (p >= 1) String.format(Locale.US, "$%,.0f", p) else String.format(Locale.US, "$%.2f", p)

fun pct(ratio: Double): String {
    val v = (ratio - 1) * 100
    val sign = if (v >= 0) "+" else "−"
    return sign + String.format(Locale.US, "%.1f%%", abs(v))
}

object RainbowModel {
    /*
     * ΡΥΘΜΙΣΕΙΣ ΜΟΝΤΕΛΟΥ — σταθερός κλασικός τύπος, όχι dynamic fit.
     * log10(τιμή) = A · ln(μέρες από ORIGIN) + B
     */
    private val ORIGIN = DateUtil.epochDay(2009, 1, 9)
    private const val A = 2.66167155005961
    private const val B = -17.9183761889864

    const val FORMULA_NOTE_EL = "Κλασικός τύπος Rainbow"
    const val FORMULA_NOTE_EN = "Classic Rainbow formula"

    val EDGES = doubleArrayOf(-0.62, -0.46, -0.30, -0.14, 0.02, 0.18, 0.34, 0.50, 0.66, 0.82)

    val BANDS = listOf(
        Band("Lowest Band", 0xFF4A6FD0),
        Band("Historically deep floor", 0xFF4FB37A),
        Band("Accumulate", 0xFF8FCB84),
        Band("Still Cheap", 0xFFC4DF8E),
        Band("HODL", 0xFFF5DE75),
        Band("Is this a bubble?", 0xFFF6B55B),
        Band("FOMO intensifies", 0xFFEF8A3C),
        Band("Historically overheated", 0xFFE0532D),
        Band("Maximum Bubble", 0xFFB8141C),
    )

    val HALVINGS = listOf(
        Halving(2012, DateUtil.epochDay(2012, 11, 28), false, 0xFF8B5CF6),
        Halving(2016, DateUtil.epochDay(2016, 7, 9), false, 0xFF0891B2),
        Halving(2020, DateUtil.epochDay(2020, 5, 11), false, 0xFFEA580C),
        Halving(2024, DateUtil.epochDay(2024, 4, 20), false, 0xFF111827),
        Halving(2028, DateUtil.epochDay(2028, 4, 15), true, 0xFFDC2626),
        Halving(2032, DateUtil.epochDay(2032, 4, 10), true, 0xFFDC2626),
    )

    fun baseLog10(day: Double): Double = A * ln((day - ORIGIN).coerceAtLeast(1.0)) + B
    fun edgePrice(day: Double, edge: Int): Double = 10.0.pow(baseLog10(day) + EDGES[edge])

    fun bandIndex(day: Long, price: Double): Int {
        val off = log10(price) - baseLog10(day.toDouble())
        for (i in 0 until BANDS.size) if (off < EDGES[i + 1]) return i
        return BANDS.lastIndex
    }

    fun currentHalving(today: Long): Halving = HALVINGS.last { !it.estimated && it.day <= today }
    fun daysSinceHalving(today: Long): Long = today - currentHalving(today).day
    fun cycleOf(day: Long): Halving? = HALVINGS.lastOrNull { !it.estimated && it.day <= day }
    fun nextHalving(h: Halving): Halving? = HALVINGS.firstOrNull { it.day > h.day }

    fun compareCycles(series: List<PricePoint>, today: Long): List<CycleCompare> {
        val n = daysSinceHalving(today)
        return HALVINGS.filter { !it.estimated }.map { h ->
            val d = h.day + n
            val p = series.priceNear(d)
            CycleCompare(h, d, p, p?.let { bandIndex(d, it) })
        }
    }
}

fun List<PricePoint>.nearest(day: Long): PricePoint? {
    if (isEmpty()) return null
    var lo = 0
    var hi = size - 1
    while (lo < hi) {
        val mid = (lo + hi) / 2
        if (this[mid].day < day) lo = mid + 1 else hi = mid
    }
    val a = this[lo]
    return if (lo > 0 && abs(this[lo - 1].day - day) < abs(a.day - day)) this[lo - 1] else a
}

fun List<PricePoint>.priceNear(day: Long, tolerance: Long = 3): Double? =
    nearest(day)?.takeIf { abs(it.day - day) <= tolerance }?.price

fun List<PricePoint>.lowerBound(day: Long): Int {
    var lo = 0
    var hi = size
    while (lo < hi) {
        val mid = (lo + hi) / 2
        if (this[mid].day < day) lo = mid + 1 else hi = mid
    }
    return lo
}

fun buildSeries(history: List<PricePoint>, live: Double?, today: Long): List<PricePoint> {
    val shown = live?.takeIf { it > 0.0 }?.let { PricePoint(today, it) }
    return buildSeries(history, shown, today)
}

fun buildSeries(history: List<PricePoint>, shown: PricePoint?, today: Long): List<PricePoint> {
    if (history.isEmpty()) return emptyList()
    val past = history.filter { it.day < today }
    return when {
        shown != null && shown.day == today -> past + shown
        past.isEmpty() -> history
        else -> past
    }
}
