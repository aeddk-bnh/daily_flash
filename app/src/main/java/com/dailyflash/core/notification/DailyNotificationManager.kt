package com.dailyflash.core.notification

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dailyflash.domain.notification.INotificationManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class DailyNotificationManager(private val context: Context) : INotificationManager {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun scheduleDailyReminder(time: LocalTime) {
        scheduleNextInstance(time)
    }

    override suspend fun cancelDailyReminder() {
        workManager.cancelAllWorkByTag(NotificationWorker.WORK_TAG)
    }
    
    fun scheduleNextInstance(time: LocalTime) {
        val now = LocalDateTime.now()
        var target = now.with(time) // Today at HH:mm
        
        if (target.isBefore(now) || target.isEqual(now)) {
            target = target.plusDays(1) // Tomorrow
        }
        
        val initialDelay = Duration.between(now, target).toMillis()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .addTag(NotificationWorker.WORK_TAG)
            .setInputData(workDataOf(
                "HOUR" to time.hour,
                "MINUTE" to time.minute
            ))
            .build()

        // Replace existing to ensure only one chain active
        workManager.enqueueUniqueWork(
            "daily_flash_schedule",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
