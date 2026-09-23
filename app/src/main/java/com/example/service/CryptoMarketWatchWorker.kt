package com.example.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.model.AlertCategory
import com.example.data.network.MarketDataClient
import com.example.util.AlertHistoryManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CryptoMarketWatchWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            AlertHistoryManager.initialize(context)

            // 1. Fetch Binance Futures Mark Price & Funding Rate
            val jsonString = MarketDataClient.getText(
                "https://www.binance.com/fapi/v1/premiumIndex?symbol=BTCUSDT",
                attempts = 2
            )
            if (!jsonString.isNullOrBlank()) {
                val json = JSONObject(jsonString)
                val markPrice = json.optDouble("markPrice", 67000.0)
                val lastFundingRate = json.optDouble("lastFundingRate", 0.0001)
                val fundingPercent = lastFundingRate * 100.0

                // Trigger 1: Extreme Positive Funding (Overheated Longs)
                if (fundingPercent >= 0.05) {
                    val title = "🚨 BINANCE BTC FUNDING SPIKE (> +0.05%)"
                    val msg = "Ακραία υπερθέρμανση Longs (${String.format("%.3f", fundingPercent)}%). Κίνδυνος Flush τις επόμενες ώρες."
                    val reason = "Το κόστος διακράτησης Long θέσεων εκτοξεύτηκε. Οι εκκαθαρίσεις (Long Squeeze) είναι στατιστικά πιθανές."
                    
                    NotificationHelper.sendFundingAlertNotification(
                        context = context,
                        title = title,
                        message = msg,
                        details = reason
                    )
                    AlertHistoryManager.addAlert(
                        category = AlertCategory.FUNDING_SPIKE,
                        title = title,
                        message = msg,
                        detailedReason = reason,
                        btcPrice = markPrice,
                        navTargetTab = "DERIVATIVES",
                        isUrgent = true
                    )
                } 
                // Trigger 2: Extreme Negative Funding (Short Squeeze Potential)
                else if (fundingPercent <= -0.04) {
                    val title = "🚀 NEGATIVE FUNDING SHORT SQUEEZE ALERT"
                    val msg = "Αρνητικό Funding (${String.format("%.3f", fundingPercent)}%). Οι Shorts πληρώνουν τους Longs — Πιθανό Squeeze προς τα πάνω."
                    val reason = "Ακραία απαισιοδοξία στα παράγωγα που συχνά οδηγεί σε βίαιη ανοδική εκτίναξη (Short Squeeze)."
                    
                    NotificationHelper.sendFundingAlertNotification(
                        context = context,
                        title = title,
                        message = msg,
                        details = reason
                    )
                    AlertHistoryManager.addAlert(
                        category = AlertCategory.FUNDING_SPIKE,
                        title = title,
                        message = msg,
                        detailedReason = reason,
                        btcPrice = markPrice,
                        navTargetTab = "DERIVATIVES",
                        isUrgent = true
                    )
                }
            }

            try {
                com.example.util.CycleDayAlertDispatcher.refresh(context)
            } catch (_: Exception) {
            }

            Result.success()
        } catch (_: Exception) {
            Result.success() // Keep worker resilient
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "crypto_market_watch_periodic_worker"

        fun schedulePeriodic(context: Context) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val workRequest = PeriodicWorkRequestBuilder<CryptoMarketWatchWorker>(
                        15, TimeUnit.MINUTES,
                        5, TimeUnit.MINUTES // Flex interval
                    )
                        .setConstraints(constraints)
                        .build()

                    WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                        UNIQUE_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                    )
                } catch (_: Exception) {}
            }
        }

        fun triggerImmediateCheck(context: Context) {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val oneTimeRequest = OneTimeWorkRequestBuilder<CryptoMarketWatchWorker>()
                        .setConstraints(constraints)
                        .build()

                    WorkManager.getInstance(context.applicationContext).enqueue(oneTimeRequest)
                } catch (_: Exception) {}
            }
        }
    }
}
