package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RainbowBand
import com.example.ui.theme.SpaceGroteskFont
import com.example.util.CycleFractalData
import com.example.util.CycleDayLessons
import com.example.util.RainbowCalculator
import kotlin.math.ln
import kotlin.math.max

@Composable
fun CycleRainbowHome(
    reading: CycleFractalData?,
    priceUsd: Double,
    priceIsLive: Boolean,
    greek: Boolean,
    modifier: Modifier = Modifier
) {
    val day = reading?.currentDay
    val nowMs = System.currentTimeMillis()
    val yearNow = RainbowCalculator.getCurrentFractionalYear(nowMs)
    val dots = remember(reading, priceUsd, priceIsLive, yearNow) {
        buildDots(reading, priceUsd, priceIsLive, yearNow)
    }
        val lessons = if (day == null) emptyList() else CycleDayLessons.paragraphs(day, greek)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (greek) "Rainbow · πού κάθεται η μέρα" else "Rainbow · where this day sits",
            fontFamily = SpaceGroteskFont,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF14161A)
        )
        Text(
            text = if (greek) {
                "Οι ζώνες είναι ιστορικό μοντέλο, όχι στόχος. Οι κουκκίδες είναι κλεισίματα."
            } else {
                "The bands are a historical fit, not a target. The dots are closes."
            },
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )
        Canvas(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            val left = 8f
            val right = size.width - 8f
            val top = 12f
            val bottom = size.height - 16f
            val x0 = 2012.0
            val x1 = max(yearNow, 2012.2)
            val samples = 48
            val bandLows = mutableListOf<Double>()
            val bandHighs = mutableListOf<Double>()
            for (i in 0..samples) {
                val year = x0 + (x1 - x0) * i / samples
                val days = ((year - 2009.0) * 365.25).toInt().coerceAtLeast(1)
                bandLows += RainbowCalculator.calculateBandPrice(days, RainbowBand.FIRE_SALE)
                bandHighs += RainbowCalculator.calculateBandPrice(days, RainbowBand.MAXIMUM_BUBBLE)
            }
            val prices = bandLows + bandHighs + dots.map { it.price }
            val yMin = prices.minOrNull()?.coerceAtLeast(1.0) ?: 1.0
            val yMax = prices.maxOrNull()?.coerceAtLeast(yMin * 1.1) ?: 10.0
            fun xOf(year: Double) = left + ((year - x0) / (x1 - x0)).toFloat() * (right - left)
            fun yOf(price: Double): Float {
                val t = (ln(price) - ln(yMin)) / (ln(yMax) - ln(yMin))
                return bottom - t.toFloat() * (bottom - top)
            }
            val bands = RainbowBand.entries.sortedBy { it.id }
            for (bandIndex in 0 until bands.lastIndex) {
                val path = Path()
                for (i in 0..samples) {
                    val year = x0 + (x1 - x0) * i / samples
                    val days = ((year - 2009.0) * 365.25).toInt().coerceAtLeast(1)
                    val price = RainbowCalculator.calculateBandPrice(days, bands[bandIndex + 1])
                    val x = xOf(year)
                    val y = yOf(price)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                for (i in samples downTo 0) {
                    val year = x0 + (x1 - x0) * i / samples
                    val days = ((year - 2009.0) * 365.25).toInt().coerceAtLeast(1)
                    val price = RainbowCalculator.calculateBandPrice(days, bands[bandIndex])
                    path.lineTo(xOf(year), yOf(price))
                }
                path.close()
                drawPath(path, bands[bandIndex].color.copy(alpha = 0.85f))
            }
            dots.forEach { dot ->
                drawCircle(dot.color, radius = if (dot.now) 7f else 5.5f, center = Offset(xOf(dot.year), yOf(dot.price)))
                drawCircle(Color.White, radius = if (dot.now) 3f else 2.2f, center = Offset(xOf(dot.year), yOf(dot.price)))
            }
        }
        DotKey(greek)
        if (day == null || !priceIsLive) {
            Text(
                text = if (greek) "Η σημερινή κουκκίδα μένει κενή μέχρι να έρθει ζωντανή τιμή." else "Today's dot stays empty until a live price arrives.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
        lessons.forEach { paragraph ->
            Text(
                text = paragraph,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color(0xFF14161A)
            )
        }
    }
}

@Composable
private fun DotKey(greek: Boolean) {
    Text(
        text = if (greek) {
            "Άσπρο κέντρο: σήμερα. Μικρές κουκκίδες: η ίδια ημέρα κύκλου το 2012, το 2016 και το 2020."
        } else {
            "White center: today. Smaller dots: the same cycle day in 2012, 2016 and 2020."
        },
        fontSize = 11.sp,
        color = Color(0xFF64748B)
    )
}

private data class YearDot(val year: Double, val price: Double, val color: Color, val now: Boolean)

private fun buildDots(
    reading: CycleFractalData?,
    priceUsd: Double,
    priceIsLive: Boolean,
    yearNow: Double
): List<YearDot> {
    val dots = mutableListOf<YearDot>()
    val day = reading?.currentDay
    if (day != null) {
        marker(2012, 1354116278000L, day, reading.close2012)?.let { dots += it }
        marker(2016, 1468082773000L, day, reading.close2016)?.let { dots += it }
        marker(2020, 1589217823000L, day, reading.close2020)?.let { dots += it }
    }
    if (priceIsLive && priceUsd > 0.0) {
        dots += YearDot(yearNow, priceUsd, Color(0xFF05050F), now = true)
    }
    return dots.filter { it.year <= yearNow + 0.01 && it.price > 0.0 }
}

private fun marker(cycle: Int, halvingMs: Long, day: Int, close: Double?): YearDot? {
    if (close == null || close <= 0.0) return null
    val whenMs = halvingMs + day * 86_400_000L
    if (whenMs > System.currentTimeMillis()) return null
    val year = RainbowCalculator.getCurrentFractionalYear(whenMs)
    val color = when (cycle) {
        2012 -> Color(0xFF7C5CFF)
        2016 -> Color(0xFF0891B2)
        else -> Color(0xFFEA580C)
    }
    return YearDot(year, close, color, now = false)
}
