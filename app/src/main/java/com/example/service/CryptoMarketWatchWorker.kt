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
import com.example.util.AlertHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CryptoMarketWatchWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            AlertHistoryManager.initialize(context)

            try {
                com.example.util.CycleAlertDispatcher.run(context)
            } catch (_: Exception) {
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
