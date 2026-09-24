package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.ui.components.BitcoinRainbowChart
import com.example.ui.components.RainbowModelEngine
import com.example.util.CycleDayLessons
import com.example.util.CycleFractalData
import com.example.util.RainbowCalculator

@Composable
fun CycleRainbowHome(
    reading: CycleFractalData?,
    priceUsd: Double,
    priceIsLive: Boolean,
    isProUnlocked: Boolean,
    greek: Boolean,
    onOpenProModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val day = reading?.currentDay
    val markers = remember(reading) {
        buildCycleMarkers(reading)
    }
    val lessons = if (day == null) emptyList() else CycleDayLessons.paragraphs(day, greek)
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BitcoinRainbowChart(
            btcPriceUsd = if (priceIsLive) priceUsd else 0.0,
            currency = Currency.USD,
            isProUnlocked = isProUnlocked,
            onOpenProModal = onOpenProModal,
            cycleMarkers = markers
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarkerKey(Color(0xFF05050F), if (greek) "Σήμερα" else "Today")
            MarkerKey(Color(0xFF7C5CFF), "2012")
            MarkerKey(Color(0xFF0891B2), "2016")
            MarkerKey(Color(0xFFEA580C), "2020")
        }
        Text(
            text = if (greek) {
                "Η κουκκίδα στο τέλος της μαύρης γραμμής είναι η σημερινή τιμή. Οι χρωματιστές είναι η ίδια ημέρα κύκλου το 2012, το 2016 και το 2020."
            } else {
                "The dot at the end of the black line is today’s price. The colored dots are the same cycle day in 2012, 2016 and 2020."
            },
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        lessons.forEach { paragraph ->
            Text(
                text = paragraph,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color(0xFF14161A),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

private fun buildCycleMarkers(
    reading: CycleFractalData?
): List<RainbowModelEngine.CycleMarker> {
    val day = reading?.currentDay ?: return emptyList()
    val markers = mutableListOf<RainbowModelEngine.CycleMarker>()
    marker(1354116278000L, day, reading.close2012, Color(0xFF7C5CFF))?.let { markers += it }
    marker(1468082773000L, day, reading.close2016, Color(0xFF0891B2))?.let { markers += it }
    marker(1589217823000L, day, reading.close2020, Color(0xFFEA580C))?.let { markers += it }
    return markers
}

@Composable
private fun MarkerKey(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, fontSize = 12.sp, color = Color(0xFF14161A))
    }
}

private fun marker(halvingMs: Long, day: Int, close: Double?, color: Color): RainbowModelEngine.CycleMarker? {
    if (close == null || close <= 0.0) return null
    val whenMs = halvingMs + day * 86_400_000L
    if (whenMs > System.currentTimeMillis()) return null
    return RainbowModelEngine.CycleMarker(
        x = RainbowCalculator.getCurrentFractionalYear(whenMs),
        y = close,
        color = color
    )
}
