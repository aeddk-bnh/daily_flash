package com.dailyflash.data.reminders

import android.content.Context
import androidx.work.*
import com.dailyflash.core.reminders.ReminderWorker
import com.dailyflash.domain.reminders.ReminderScheduler
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class WorkManagerReminderScheduler(
    private val context: Context
) : ReminderScheduler {

    companion object {
        const val REMINDER_WORK_TAG = "daily_reminder_work"
    }

    private val workManager = WorkManager.getInstance(context)

    override fun schedule(time: LocalTime) {
        val now = ZonedDateTime.now()
        var scheduledTime = now.with(time)

        // If the scheduled time is in the past, schedule it for the next day
        if (scheduledTime.isBefore(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, scheduledTime)

        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay)
            .addTag(REMINDER_WORK_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            reminderWorkRequest
        )
    }

    override fun cancel() {
        workManager.cancelAllWorkByTag(REMINDER_WORK_TAG)
    }
}
