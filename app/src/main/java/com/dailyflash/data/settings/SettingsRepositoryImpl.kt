package com.dailyflash.data.settings

import com.dailyflash.core.settings.SettingsDataStore
import com.dailyflash.domain.settings.ISettingsRepository
import com.dailyflash.domain.settings.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SettingsRepositoryImpl(
    private val settingsDataStore: SettingsDataStore
) : ISettingsRepository {

    override val settings: Flow<UserSettings> = combine(
        settingsDataStore.reminderEnabled,
        settingsDataStore.reminderTime,
        settingsDataStore.autoCleanup,
        settingsDataStore.keepDays
    ) { enabled, timeStr, cleanup, days ->
        UserSettings(
            dailyReminderEnabled = enabled,
            reminderTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")),
            autoCleanupEnabled = cleanup,
            keepDays = days
        )
    }

    override suspend fun updateReminder(enabled: Boolean, time: LocalTime) {
        settingsDataStore.setReminderEnabled(enabled)
        settingsDataStore.setReminderTime(time.format(DateTimeFormatter.ofPattern("HH:mm")))
    }

    override suspend fun updateAutoCleanup(enabled: Boolean, days: Int) {
        settingsDataStore.setAutoCleanup(enabled)
        settingsDataStore.setKeepDays(days)
    }
}
