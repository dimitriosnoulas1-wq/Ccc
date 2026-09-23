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
    private const val KEY_ALERTS_JSON = "alerts_json_v1"

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
            // Seed initial realistic 30-day historical institutional alerts
            val defaultHistory = getSeedHistory()
            _alerts.value = defaultHistory
            saveAlerts(defaultHistory)
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
                if (list.isEmpty()) {
                    val seed = getSeedHistory()
                    _alerts.value = seed
                    saveAlerts(seed)
                } else {
                    _alerts.value = list
                }
            } catch (_: Exception) {
                val seed = getSeedHistory()
                _alerts.value = seed
                saveAlerts(seed)
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

    fun clearAll() {
        val seed = getSeedHistory()
        _alerts.value = seed
        saveAlerts(seed)
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

    private fun getSeedHistory(): List<AlertHistoryItem> {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        return listOf(
            AlertHistoryItem(
                id = "seed-1",
                timestamp = now - 3600000L * 3,
                displayDate = "Σήμερα, 04:15",
                category = AlertCategory.FUNDING_SPIKE,
                title = "⚡ BINANCE FUNDING SPIKE WARNING",
                message = "Binance BTC Funding Rate έφτασε το +0.048% με αυξανόμενο Open Interest.",
                detailedReason = "Ακραία υπερθέρμανση Long μοχλεύσεων. Αυξημένος κίνδυνος Long Squeeze / Local Flush τις επόμενες ώρες.",
                btcPriceAtTrigger = 67420.0,
                navTargetTab = "DERIVATIVES",
                isUrgent = true,
                isRead = false
            ),
            AlertHistoryItem(
                id = "seed-2",
                timestamp = now - dayMs * 1,
                displayDate = "Χθες, 19:40",
                category = AlertCategory.ETF_EXTREME,
                title = "🏛️ MASSIVE SPOT ETF INFLOW (+640M)",
                message = "Θεσμική καθαρή εισροή +$640.2M στα US Spot Bitcoin ETFs (IBIT & FBTC).",
                detailedReason = "Συνεχιζόμενη απορρόφηση προσφοράς από Spot ETFs. Ισχυρή υποστήριξη τιμής και θετική απόκλιση ρευστότητας.",
                btcPriceAtTrigger = 66800.0,
                navTargetTab = "MACRO",
                isUrgent = false,
                isRead = true
            ),
            AlertHistoryItem(
                id = "seed-3",
                timestamp = now - dayMs * 3,
                displayDate = "09 Σεπ, 11:20",
                category = AlertCategory.WHALE_FLOW,
                title = "🚨 WHALE DEPOSIT TO SPOT EXCHANGE",
                message = "Μεταφορά 4,850 BTC (~$325M) από άγνωστο πορτοφόλι προς Binance Spot.",
                detailedReason = "Εντοπίστηκε αυξημένη πιθανότητα spot πώλησης ή ανακατανομής θέσεων από μεγάλο κάτοχο.",
                btcPriceAtTrigger = 66100.0,
                navTargetTab = "WHALES",
                isUrgent = true,
                isRead = true
            ),
            AlertHistoryItem(
                id = "seed-4",
                timestamp = now - dayMs * 7,
                displayDate = "05 Σεπ, 16:00",
                category = AlertCategory.CYCLE_SHIFT,
                title = "🎯 CYCLE REGIME TRANSITION: EXPANSION",
                message = "Ο συνθετικός δείκτης κύκλου επιβεβαίωσε είσοδο στη Ζώνη 2 (Cycle Expansion).",
                detailedReason = "Συνδυασμός θετικών ροών ETF, διεύρυνσης Stablecoins και διατήρησης του BTC πάνω από το 200W SMA.",
                btcPriceAtTrigger = 64900.0,
                navTargetTab = "MACRO",
                isUrgent = false,
                isRead = true
            ),
            AlertHistoryItem(
                id = "seed-5",
                timestamp = now - dayMs * 14,
                displayDate = "29 Αυγ, 08:30",
                category = AlertCategory.LIQUIDATION_CASCADE,
                title = "💥 LIQUIDATION CASCADE FLUSH ($180M)",
                message = "Εκκαθαρίσεις $180M σε Long θέσεις εντός 1 ώρας. Το Funding Rate μηδένισε.",
                detailedReason = "Υγιής εκτόνωση υπερβολικής μόχλευσης (Leverage Reset). Η αγορά δημιούργησε τοπικό πυθμένα υποστήριξης.",
                btcPriceAtTrigger = 62150.0,
                navTargetTab = "DERIVATIVES",
                isUrgent = true,
                isRead = true
            )
        )
    }
}
