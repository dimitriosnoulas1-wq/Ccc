package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object HalvingCycleUtils {

    // 4th Bitcoin Halving (Block 840,000): April 20, 2024 00:09 UTC
    const val HALVING_4TH_TIMESTAMP = 1713571740000L

    // Bitcoin Cycle ATH Peak: October 6, 2025 00:00 UTC ($126,500)
    const val BTC_CYCLE_ATH_TIMESTAMP = 1759708800000L
    const val BTC_CYCLE_ATH_USD = 126500.0

    // 5th Bitcoin Halving Estimate (Block 1,050,000): ~April 17, 2028 00:00 UTC
    const val HALVING_5TH_TIMESTAMP = 1839542400000L

    data class HalvingCountdownState(
        val days: Long,
        val hours: Long,
        val minutes: Long,
        val seconds: Long,
        val totalDaysString: String,
        val hoursString: String,
        val minutesString: String,
        val secondsString: String,
        val blockProgressPercent: Float
    )

    fun getDaysSince4thHalving(): Int {
        val now = System.currentTimeMillis()
        val diff = now - HALVING_4TH_TIMESTAMP
        return (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }

    fun getLiveHalvingCountdown(): HalvingCountdownState {
        val now = System.currentTimeMillis()
        val remainingMs = max(0L, HALVING_5TH_TIMESTAMP - now)
        val totalSecs = remainingMs / 1000L
        val days = totalSecs / (24 * 3600)
        val hours = (totalSecs % (24 * 3600)) / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60

        // Progress between 4th and 5th halving (1461 days / 4 years)
        val totalCycleSpan = HALVING_5TH_TIMESTAMP - HALVING_4TH_TIMESTAMP
        val elapsed = (now - HALVING_4TH_TIMESTAMP).coerceIn(0L, totalCycleSpan)
        val progress = (elapsed.toFloat() / totalCycleSpan.toFloat()).coerceIn(0f, 1f)

        return HalvingCountdownState(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalDaysString = String.format(Locale.US, "%d", days),
            hoursString = String.format(Locale.US, "%02d", hours),
            minutesString = String.format(Locale.US, "%02d", minutes),
            secondsString = String.format(Locale.US, "%02d", seconds),
            blockProgressPercent = progress
        )
    }

    fun parseAthDaysAgo(dateString: String, fallbackDays: Int = 0): Int {
        if (dateString.isBlank()) return fallbackDays
        val formats = listOf(
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
            SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            SimpleDateFormat("d MMM yyyy", Locale.ENGLISH),
            SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH),
            SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH),
            SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH),
            SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        )

        val trimmed = dateString.trim()
        for (fmt in formats) {
            try {
                val parsed = fmt.parse(trimmed)
                if (parsed != null) {
                    val diff = System.currentTimeMillis() - parsed.time
                    val days = (diff / (1000L * 60 * 60 * 24)).toInt()
                    return days.coerceAtLeast(0)
                }
            } catch (_: Exception) {}
        }
        return fallbackDays.coerceAtLeast(0)
    }
}
