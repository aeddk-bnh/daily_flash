package com.dailyflash.core.notification

import java.time.LocalTime

interface INotificationManager {
    fun scheduleDailyReminder(time: LocalTime)
    fun cancelReminder()
}
