package com.example.util

import android.content.Context
import com.example.data.repository.DailyCycleLogRepository
import com.example.data.repository.HistoricalMarketRepository

object CycleDayAlertDispatcher {
    suspend fun refresh(
        context: Context,
        priceUsd: Double = 0.0,
        logRepository: DailyCycleLogRepository? = null
    ): CycleFractalData? {
        val data = HistoricalMarketRepository.load("bitcoin", "BTC", "HALVING") ?: return null
        val close = when {
            priceUsd > 0.0 -> priceUsd
            else -> data.currentPoints.lastOrNull()?.price ?: 0.0
        }
        if (close > 0.0) {
            (logRepository ?: DailyCycleLogRepository(context.applicationContext)).record(data, close)
        }
        maybeNotify(context.applicationContext, data)
        return data
    }

    private fun maybeNotify(context: Context, data: CycleFractalData) {
        if (!CycleDayAlertPrefs.isEnabled(context) || !CycleDayAlertPrefs.isProCached(context)) return
        val today = DailyCycleLogRepository.dayKey(System.currentTimeMillis())
        if (CycleDayAlertPrefs.lastSentDay(context) == today) return
        if (!NotificationHelper.canSendNotifications(context) && !com.example.BuildConfig.DEBUG) return
        val greek = CycleDayAlertPrefs.isGreek(context)
        val line = CycleReadingText.alertLine(data.currentDay, data.multiple2016, data.multiple2020, greek)
        NotificationHelper.recordAndNotify(
            context = context,
            eventId = "cycle-day-$today",
            channelId = NotificationHelper.CHANNEL_CYCLE,
            title = if (greek) "Ημέρα κύκλου" else "Cycle day",
            body = line,
            details = CycleReadingText.disclaimer(greek),
            navTab = "MARKETS",
            type = "CYCLE",
            bypassPermission = com.example.BuildConfig.DEBUG
        )
        CycleDayAlertPrefs.markSent(context, today)
    }
}
