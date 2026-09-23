package com.example.util

import android.content.Context

object CycleDayAlertPrefs {
    private const val PREFS = "crypto_cycles_cycle_day_alert"
    private const val ENABLED = "enabled"
    private const val GREEK = "greek"
    private const val LAST_SENT = "last_sent_day"
    private const val BILLING_PREFS = "cryptocycles_billing_prefs"
    private const val PRO_KEY = "key_is_pro_active"

    fun isEnabled(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(ENABLED, false)

    fun isGreek(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(GREEK, false)

    fun setEnabled(context: Context, enabled: Boolean, greek: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(ENABLED, enabled)
            .putBoolean(GREEK, greek)
            .apply()
    }

    fun lastSentDay(context: Context): String =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(LAST_SENT, "").orEmpty()

    fun markSent(context: Context, dayKey: String) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(LAST_SENT, dayKey)
            .apply()
    }

    fun isProCached(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(BILLING_PREFS, Context.MODE_PRIVATE)
            .getBoolean(PRO_KEY, false)
}
