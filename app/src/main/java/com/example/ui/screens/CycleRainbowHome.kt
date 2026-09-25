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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.ui.components.BitcoinRainbowChart
import com.example.ui.components.RainbowModelEngine
import com.example.ui.theme.LocalAppColors
import com.example.util.CycleDayLessons
import com.example.util.CycleFractalData
import com.example.util.CycleSameDayNote
import com.example.util.HalvingCycleUtils
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
    val palette = LocalAppColors.current
    val day = reading?.currentDay
    val markers = remember(reading) {
        buildCycleMarkers(reading)
    }
    val sameDay = remember(reading, priceUsd, priceIsLive, greek) {
        CycleSameDayNote.paragraphs(
            day = day,
            priceUsd = priceUsd,
            priceIsLive = priceIsLive,
            close2012 = reading?.close2012,
            close2016 = reading?.close2016,
            close2020 = reading?.close2020,
            multiple2012 = reading?.multiple2012,
            multiple2016 = reading?.multiple2016,
            multiple2020 = reading?.multiple2020,
            greek = greek
        )
    }
    val lessons = if (day == null) emptyList() else CycleDayLessons.paragraphs(day, greek)
    if (isProUnlocked) {
        Column(
            modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    "Από τη μαύρη γραμμή πέφτει μια διαγώνια γραμμή σε σημείο: εκεί ήμασταν την ίδια ημέρα κύκλου. Η μαύρη κουκκίδα στο τέλος της τιμής είναι σήμερα, ζωντανά."
                } else {
                    "A diagonal stem drops from the black line to a point: that is the same cycle day. The black dot at the end of the price is today, live."
                },
                fontSize = 12.sp,
                color = palette.textMuted,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = if (greek) "Πού ήμασταν την ίδια ημέρα" else "Where we were on this same day",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
            sameDay.forEach { paragraph ->
                Text(
                    text = paragraph,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = palette.textPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
            lessons.forEach { paragraph ->
                Text(
                    text = paragraph,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = palette.textSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (greek) "Rainbow: Bitcoin, πού βρισκόμαστε σήμερα και πού ήμασταν τις ίδιες ημέρες στους προηγούμενους κύκλους — ενημερώνεται καθημερινά." else "Rainbow: Bitcoin, where we are today and where we were the same days in the past cycles — updated every single day.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )
            Text(
                text = if (greek) "Αυτό το χαρακτηριστικό είναι διαθέσιμο για Pro χρήστες." else "This feature is available for Pro users.",
                fontSize = 14.sp,
                color = palette.textSecondary
            )
            androidx.compose.material3.Button(onClick = onOpenProModal, modifier = Modifier.fillMaxWidth()) {
                Text(if (greek) "Αναβάθμιση σε Pro" else "Upgrade to Pro")
            }
        }
    }
}

private fun buildCycleMarkers(
    reading: CycleFractalData?
): List<RainbowModelEngine.CycleMarker> {
    val day = reading?.currentDay ?: return emptyList()
    val markers = mutableListOf<RainbowModelEngine.CycleMarker>()
    marker(HalvingCycleUtils.HALVING_2012_TIMESTAMP, day, reading.close2012, Color(0xFF7C5CFF), "2012")?.let { markers += it }
    marker(HalvingCycleUtils.HALVING_2016_TIMESTAMP, day, reading.close2016, Color(0xFF0891B2), "2016")?.let { markers += it }
    marker(HalvingCycleUtils.HALVING_2020_TIMESTAMP, day, reading.close2020, Color(0xFFEA580C), "2020")?.let { markers += it }
    return markers
}

@Composable
private fun MarkerKey(color: Color, label: String) {
    val palette = LocalAppColors.current
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
        Text(text = label, fontSize = 12.sp, color = palette.textPrimary)
    }
}

private fun marker(
    halvingMs: Long,
    day: Int,
    close: Double?,
    color: Color,
    label: String
): RainbowModelEngine.CycleMarker? {
    if (close == null || close <= 0.0) return null
    val whenMs = HalvingCycleUtils.utcDatePlusDays(halvingMs, day)
    if (whenMs > System.currentTimeMillis()) return null
    return RainbowModelEngine.CycleMarker(
        x = RainbowCalculator.getCurrentFractionalYear(whenMs),
        y = close,
        color = color,
        label = label
    )
}
