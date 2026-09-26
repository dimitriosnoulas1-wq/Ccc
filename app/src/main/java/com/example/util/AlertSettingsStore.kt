package com.example.util

import android.content.Context
import com.example.data.model.WhaleAlertSettings

/** Alert toggles and the last state each alert saw, shared by the app and the background worker. */
object AlertSettingsStore {
    private const val PREFS = "crypto_cycles_alert_settings"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): WhaleAlertSettings {
        val p = prefs(context)
        val d = WhaleAlertSettings()
        return WhaleAlertSettings(
            notificationsEnabled = p.getBoolean("whale_enabled", d.notificationsEnabled),
            minThresholdUsd = p.getFloat("whale_min_usd", d.minThresholdUsd.toFloat()).toDouble(),
            notifyZoneChange = p.getBoolean("zone", d.notifyZoneChange),
            notifyPiCycle = p.getBoolean("pi", d.notifyPiCycle),
            notifyRainbowBand = p.getBoolean("rainbow", d.notifyRainbowBand),
            notify200wSma = p.getBoolean("sma200w", d.notify200wSma),
            notifyFunding = p.getBoolean("funding", d.notifyFunding)
        )
    }

    fun save(context: Context, s: WhaleAlertSettings) {
        prefs(context).edit()
            .putBoolean("whale_enabled", s.notificationsEnabled)
            .putFloat("whale_min_usd", s.minThresholdUsd.toFloat())
            .putBoolean("zone", s.notifyZoneChange)
            .putBoolean("pi", s.notifyPiCycle)
            .putBoolean("rainbow", s.notifyRainbowBand)
            .putBoolean("sma200w", s.notify200wSma)
            .putBoolean("funding", s.notifyFunding)
            .apply()
    }

    fun lastState(context: Context, alert: String): String? =
        prefs(context).getString("state_$alert", null)

    fun setLastState(context: Context, alert: String, state: String?) {
        prefs(context).edit().putString("state_$alert", state).apply()
    }

    fun lastRun(context: Context, key: String): String =
        prefs(context).getString("run_$key", "").orEmpty()

    fun setLastRun(context: Context, key: String, value: String) {
        prefs(context).edit().putString("run_$key", value).apply()
    }
}
