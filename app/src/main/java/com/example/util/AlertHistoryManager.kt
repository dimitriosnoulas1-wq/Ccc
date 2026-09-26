package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AlertCategory
import com.example.data.model.AlertHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlertHistoryManager {
    private const val PREFS_NAME = "crypto_alert_history_prefs"
    private const val KEY_ALERTS_JSON = "alerts_json_v2_live_only"

    private val _alerts = MutableStateFlow<List<AlertHistoryItem>>(emptyList())
    val alerts: StateFlow<List<AlertHistoryItem>> = _alerts.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadAlerts()
        }
    }

    private fun loadAlerts() {
        val jsonString = prefs?.getString(KEY_ALERTS_JSON, null)
        if (jsonString.isNullOrEmpty()) {
            _alerts.value = emptyList()
        } else {
            try {
                val array = JSONArray(jsonString)
                val list = mutableListOf<AlertHistoryItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        AlertHistoryItem(
                            id = obj.optString("id", "alert-$i"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            displayDate = obj.optString("displayDate", "Πρόσφατα"),
                            category = try {
                                AlertCategory.valueOf(obj.optString("category", AlertCategory.ALL.name))
                            } catch (_: Exception) {
                                AlertCategory.ALL
                            },
                            title = obj.optString("title", ""),
                            message = obj.optString("message", ""),
                            detailedReason = obj.optString("detailedReason", ""),
                            btcPriceAtTrigger = obj.optDouble("btcPriceAtTrigger", 0.0),
                            navTargetTab = obj.optString("navTargetTab", "MACRO"),
                            isUrgent = obj.optBoolean("isUrgent", true),
                            isRead = obj.optBoolean("isRead", false)
                        )
                    )
                }
                _alerts.value = list.filterNot { it.id.startsWith("seed-") }
            } catch (_: Exception) {
                _alerts.value = emptyList()
            }
        }
    }

    @Synchronized
    fun addAlert(
        category: AlertCategory,
        title: String,
        message: String,
        detailedReason: String,
        btcPrice: Double,
        navTargetTab: String = "MACRO",
        isUrgent: Boolean = true
    ) {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val displayDate = sdf.format(Date(now))

        val newItem = AlertHistoryItem(
            id = "alert-${System.currentTimeMillis()}",
            timestamp = now,
            displayDate = displayDate,
            category = category,
            title = title,
            message = message,
            detailedReason = detailedReason,
            btcPriceAtTrigger = btcPrice,
            navTargetTab = navTargetTab,
            isUrgent = isUrgent,
            isRead = false
        )

        val updated = mutableListOf(newItem)
        updated.addAll(_alerts.value.filter { it.id != newItem.id }.take(49)) // Keep last 50 alerts (30 days)
        _alerts.value = updated
        saveAlerts(updated)
    }

    fun markAllAsRead() {
        val updated = _alerts.value.map { it.copy(isRead = true) }
        _alerts.value = updated
        saveAlerts(updated)
    }

    private fun saveAlerts(list: List<AlertHistoryItem>) {
        try {
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("timestamp", item.timestamp)
                    put("displayDate", item.displayDate)
                    put("category", item.category.name)
                    put("title", item.title)
                    put("message", item.message)
                    put("detailedReason", item.detailedReason)
                    put("btcPriceAtTrigger", item.btcPriceAtTrigger)
                    put("navTargetTab", item.navTargetTab)
                    put("isUrgent", item.isUrgent)
                    put("isRead", item.isRead)
                }
                array.put(obj)
            }
            prefs?.edit()?.putString(KEY_ALERTS_JSON, array.toString())?.apply()
        } catch (_: Exception) {}
    }

}
