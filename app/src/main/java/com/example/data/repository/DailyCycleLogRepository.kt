package com.example.data.repository

import android.content.Context
import com.example.data.model.DailyCycleLogEntry
import com.example.util.CycleFractalData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DailyCycleLogRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _logs = MutableStateFlow(load())
    val logs: StateFlow<List<DailyCycleLogEntry>> = _logs.asStateFlow()

    fun record(reading: CycleFractalData, priceUsd: Double, nowMs: Long = System.currentTimeMillis()) {
        if (priceUsd <= 0.0) return
        val entry = DailyCycleLogEntry(
            dateKey = dayKey(nowMs),
            displayDate = DISPLAY.format(Date(nowMs)),
            cycleDay = reading.currentDay,
            axisDays = reading.axisDays,
            multiple = reading.multipleNow,
            multiple2016 = reading.multiple2016,
            multiple2020 = reading.multiple2020,
            priceUsd = priceUsd
        )
        val next = upsertDailyLog(_logs.value, entry)
        _logs.value = next
        persist(next)
    }

    private fun load(): List<DailyCycleLogEntry> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        DailyCycleLogEntry(
                            dateKey = o.getString("dateKey"),
                            displayDate = o.optString("displayDate"),
                            cycleDay = o.optInt("cycleDay"),
                            axisDays = o.optInt("axisDays"),
                            multiple = o.optionalDouble("multiple"),
                            multiple2016 = o.optionalDouble("multiple2016"),
                            multiple2020 = o.optionalDouble("multiple2020"),
                            priceUsd = o.optDouble("priceUsd")
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun persist(entries: List<DailyCycleLogEntry>) {
        val arr = JSONArray()
        entries.forEach { entry ->
            arr.put(
                JSONObject().apply {
                    put("dateKey", entry.dateKey)
                    put("displayDate", entry.displayDate)
                    put("cycleDay", entry.cycleDay)
                    put("axisDays", entry.axisDays)
                    if (entry.multiple != null) put("multiple", entry.multiple)
                    if (entry.multiple2016 != null) put("multiple2016", entry.multiple2016)
                    if (entry.multiple2020 != null) put("multiple2020", entry.multiple2020)
                    put("priceUsd", entry.priceUsd)
                }
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    companion object {
        private const val PREFS = "crypto_cycles_daily_log"
        private const val KEY = "days"
        private val DISPLAY = SimpleDateFormat("dd MMM yyyy", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun dayKey(nowMs: Long): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            return format.format(Date(nowMs))
        }
    }
}

internal fun upsertDailyLog(
    existing: List<DailyCycleLogEntry>,
    incoming: DailyCycleLogEntry,
    limit: Int = 400
): List<DailyCycleLogEntry> {
    val rest = existing.filterNot { it.dateKey == incoming.dateKey }
    return (listOf(incoming) + rest).take(limit)
}

private fun JSONObject.optionalDouble(key: String): Double? {
    if (!has(key) || isNull(key)) return null
    val value = optDouble(key, Double.NaN)
    return value.takeIf { !it.isNaN() }
}
