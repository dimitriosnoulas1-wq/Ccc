package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class FiredAlertRecord(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val type: String
)

object NotificationHelper {
    const val CHANNEL_WHALES = "whale_alerts_channel"
    const val CHANNEL_CYCLE = "cycle_alerts_channel"
    const val CHANNEL_FUNDING = "funding_alerts_channel"

    private val notificationScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var channelsCreated = false

    private val firedAlertIds = mutableSetOf<String>()
    private val _history = mutableListOf<FiredAlertRecord>()
    val history: List<FiredAlertRecord> get() = synchronized(_history) { _history.toList() }

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationScope.launch {
                createChannelsInternal(context.applicationContext)
            }
        }
    }

    private fun createChannelsInternal(appContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (notificationManager != null && !channelsCreated) {
                    val whaleChannel = NotificationChannel(
                        CHANNEL_WHALES,
                        "Crypto Whale Alerts",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Real-time alerts for mega on-chain whale transactions and market-moving flows."
                        enableLights(true)
                        enableVibration(true)
                    }

                    val cycleChannel = NotificationChannel(
                        CHANNEL_CYCLE,
                        "Cycle & Indicator Alerts",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Macro zone shifts, Pi cycle crossovers, and valuation band updates."
                        enableLights(true)
                        enableVibration(true)
                    }

                    val fundingChannel = NotificationChannel(
                        CHANNEL_FUNDING,
                        "Futures & Funding Alerts",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Extreme funding rates and liquidation spikes."
                        enableLights(true)
                        enableVibration(true)
                    }

                    notificationManager.createNotificationChannels(listOf(whaleChannel, cycleChannel, fundingChannel))
                    channelsCreated = true
                }
            } catch (_: Throwable) {
                // Safely handle channel creation issues
            }
        }
    }

    fun canSendNotifications(context: Context): Boolean {
        return try {
            val appContext = context.applicationContext
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                NotificationManagerCompat.from(appContext).areNotificationsEnabled()
            }
        } catch (_: Throwable) {
            false
        }
    }

    fun recordAndNotify(
        context: Context,
        eventId: String,
        channelId: String,
        title: String,
        body: String,
        details: String,
        navTab: String,
        type: String,
        bypassPermission: Boolean = false
    ) {
        val appContext = context.applicationContext

        // 1. Thread-safe in-memory deduplication and recording
        synchronized(this) {
            if (firedAlertIds.contains(eventId)) return
            firedAlertIds.add(eventId)
            if (firedAlertIds.size > 200) {
                val first = firedAlertIds.firstOrNull()
                if (first != null) firedAlertIds.remove(first)
            }

            val record = FiredAlertRecord(
                id = eventId,
                title = title,
                body = body,
                timestamp = System.currentTimeMillis(),
                type = type
            )
            synchronized(_history) {
                _history.add(0, record)
                if (_history.size > 20) {
                    _history.removeAt(_history.lastIndex)
                }
            }
        }

        // 2. Perform persistence and system notification strictly on Dispatchers.IO
        notificationScope.launch {
            try {
                AlertHistoryManager.initialize(appContext)
                val cat = when (type) {
                    "WHALE", "WHALE_TEST" -> com.example.data.model.AlertCategory.WHALE_FLOW
                    "FUNDING" -> com.example.data.model.AlertCategory.FUNDING_SPIKE
                    "CYCLE" -> com.example.data.model.AlertCategory.CYCLE_SHIFT
                    else -> com.example.data.model.AlertCategory.ALL
                }
                AlertHistoryManager.addAlert(
                    category = cat,
                    title = title,
                    message = body,
                    detailedReason = details,
                    btcPrice = 67200.0,
                    navTargetTab = navTab,
                    isUrgent = true
                )
            } catch (_: Throwable) {
                // Non-blocking history record failure
            }

            try {
                createChannelsInternal(appContext)

                // If notification permission is denied or revoked, gracefully abort system dispatch
                if (!bypassPermission && !canSendNotifications(appContext)) {
                    return@launch
                }

                if (!bypassPermission && !NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
                    return@launch
                }

                val intent = Intent(appContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("EXTRA_NAV_TAB", navTab)
                }

                val pendingIntent = PendingIntent.getActivity(
                    appContext,
                    eventId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(appContext, channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\n$details"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVibrate(longArrayOf(0, 200, 100, 200))
                    .build()

                NotificationManagerCompat.from(appContext).notify(eventId.hashCode(), notification)
            } catch (_: SecurityException) {
                // Gracefully handle denied/revoked permission without throwing
            } catch (_: Throwable) {
                // Prevent any notification-related exceptions from crashing the app or ViewModel
            }
        }
    }

    fun sendFundingAlertNotification(
        context: Context,
        title: String,
        message: String,
        details: String
    ) {
        val eventId = "funding-${title.hashCode()}-${System.currentTimeMillis() / 60_000}"
        recordAndNotify(
            context = context,
            eventId = eventId,
            channelId = CHANNEL_FUNDING,
            title = title,
            body = message,
            details = details,
            navTab = "DERIVATIVES",
            type = "FUNDING",
            bypassPermission = BuildConfig.DEBUG
        )
    }
}
