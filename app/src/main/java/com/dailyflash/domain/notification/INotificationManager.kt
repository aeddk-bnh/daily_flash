package com.dailyflash.domain.notification

import java.time.LocalTime

interface INotificationManager {
    suspend fun scheduleDailyReminder(time: LocalTime)
    suspend fun cancelDailyReminder()
}
