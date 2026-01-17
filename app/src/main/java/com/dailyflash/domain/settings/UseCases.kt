package com.dailyflash.domain.settings

import com.dailyflash.domain.notification.INotificationManager
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime

class GetUserSettingsUseCase(private val repository: ISettingsRepository) {
    operator fun invoke(): Flow<UserSettings> = repository.settings
}

class UpdateReminderUseCase(
    private val repository: ISettingsRepository,
    private val notificationManager: INotificationManager
) {
    suspend operator fun invoke(enabled: Boolean, time: LocalTime) {
        repository.updateReminder(enabled, time)
        if (enabled) {
            notificationManager.scheduleDailyReminder(time)
        } else {
            notificationManager.cancelDailyReminder()
        }
    }
}

class UpdateAutoCleanupUseCase(private val repository: ISettingsRepository) {
    suspend operator fun invoke(enabled: Boolean, days: Int) {
        repository.updateAutoCleanup(enabled, days)
    }
}
