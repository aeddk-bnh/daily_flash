package com.dailyflash.core.storage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dailyflash.core.logging.FlowLogger
import com.dailyflash.core.settings.SettingsDataStore
import com.dailyflash.domain.settings.ISettingsRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Worker responsible for automatically cleaning up old video clips
 * based on the user's "Auto-Cleanup" settings.
 */
class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val storageManager: IStorageManager = StorageManager(context)
    private val settingsDataStore: SettingsDataStore = SettingsDataStore(context)
    private val settingsRepository: ISettingsRepository = com.dailyflash.data.settings.SettingsRepositoryImpl(settingsDataStore)

    override suspend fun doWork(): Result {
        FlowLogger.flow("CleanupWorkerStart", "Checking for old clips")
        
        try {
            val settings = settingsRepository.settings.first()
            
            if (!settings.autoCleanupEnabled) {
                FlowLogger.flow("CleanupWorkerSkipped", "Auto-cleanup is disabled")
                return Result.success()
            }
            
            val cutoffDate = LocalDate.now().minusDays(settings.keepDays.toLong())
            FlowLogger.flow("CleanupWorkerRunning", "Cutoff date: $cutoffDate")
            
            val deletedCount = storageManager.deleteVideosOlderThan(cutoffDate)
            
            FlowLogger.flow("CleanupWorkerFinished", "Deleted $deletedCount clips")
            return Result.success()
        } catch (e: Exception) {
            FlowLogger.error("CleanupWorkerError", e)
            return Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "auto_cleanup_work"

        fun schedule(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiresStorageNotLow(true)
                .build()

            val request = androidx.work.PeriodicWorkRequestBuilder<CleanupWorker>(
                1, java.util.concurrent.TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .addTag(WORK_NAME)
                .build()

            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
