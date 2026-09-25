package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.rainbow.RainbowCycleSection
import com.example.ui.theme.LocalAppColors
import com.example.util.CycleDayLessons
import com.example.util.CycleFractalData
import com.example.util.CycleSameDayNote

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
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RainbowCycleSection(greek = greek)
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
}
