package com.dailyflash.data.notification

import com.dailyflash.domain.notification.INotificationManager
import com.dailyflash.domain.reminders.ReminderScheduler
import java.time.LocalTime

class NotificationManagerImpl constructor(
    private val reminderScheduler: ReminderScheduler
) : INotificationManager {

    override suspend fun scheduleDailyReminder(time: LocalTime) {
        reminderScheduler.schedule(time)
    }

    override suspend fun cancelDailyReminder() {
        reminderScheduler.cancel()
    }
}

