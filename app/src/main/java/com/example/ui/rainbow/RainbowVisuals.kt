package com.example.ui.rainbow

import kotlin.math.log10

/**
 * Καθαροί υπολογισμοί για τα γραφικά του chart (χωρίς Compose), ώστε να ελέγχονται με JVM tests.
 */
object RainbowVisuals {

    /** Θέση της τιμής μέσα στο rainbow: 0 = κάτω όριο κάτω ζώνης, 1 = πάνω όριο πάνω ζώνης. */
    fun position(day: Long, price: Double): Double {
        val e = RainbowModel.EDGES
        val off = log10(price) - RainbowModel.baseLog10(day.toDouble())
        return ((off - e.first()) / (e.last() - e.first())).coerceIn(0.0, 1.0)
    }

    /**
     * Χωρίζει τα σημεία σε συνεχόμενα κομμάτια ίδιας ζώνης, για γραμμή τιμής που αλλάζει
     * χρώμα ανά ζώνη. Κάθε κομμάτι ξεκινά από το τελευταίο σημείο του προηγούμενου,
     * ώστε η γραμμή να μην έχει κενά.
     */
    fun bandRuns(points: List<PricePoint>): List<Pair<Int, List<PricePoint>>> {
        if (points.isEmpty()) return emptyList()
        val out = mutableListOf<Pair<Int, List<PricePoint>>>()
        var band = RainbowModel.bandIndex(points[0].day, points[0].price)
        var cur = mutableListOf(points[0])
        for (i in 1 until points.size) {
            val pt = points[i]
            val b = RainbowModel.bandIndex(pt.day, pt.price)
            if (b != band) {
                cur.add(pt)
                out += band to cur
                band = b
                cur = mutableListOf(pt)
            } else {
                cur.add(pt)
            }
        }
        out += band to cur
        return out
    }

    /**
     * "Ghost" ενός προηγούμενου κύκλου πάνω στον τρέχοντα: η πορεία του κύκλου [h] από το halving του
     * μέχρι τη μέρα [n], μετατοπισμένη ώστε το halving του να πέφτει πάνω στο τρέχον halving, και
     * κλιμακωμένη ώστε η τιμή του halving να ταυτίζεται. Δείχνει ΜΟΝΟ ό,τι έχει ήδη συμβεί ως
     * σήμερα (μέρες 0..n)· δεν είναι πρόβλεψη.
     */
    fun ghost(series: List<PricePoint>, h: Halving, current: Halving, n: Long): List<PricePoint> {
        if (h == current || n < 0) return emptyList()
        val base = series.priceNear(h.day) ?: return emptyList()
        val curBase = series.priceNear(current.day) ?: return emptyList()
        val scale = curBase / base
        val from = series.lowerBound(h.day)
        val to = series.lowerBound(h.day + n + 1)
        val shift = current.day - h.day
        return (from until to).map { i -> PricePoint(series[i].day + shift, series[i].price * scale) }
    }

    /** Γραμμική παρεμβολή για ομαλή αλλαγή εύρους. */
    fun lerp(a: Double, b: Double, t: Float): Double = a + (b - a) * t
}
