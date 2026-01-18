package com.dailyflash.data.settings

import com.dailyflash.core.settings.SettingsDataStore
import com.dailyflash.domain.settings.ISettingsRepository
import com.dailyflash.domain.settings.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SettingsRepositoryImpl(
    private val settingsDataStore: SettingsDataStore
) : ISettingsRepository {

    override val settings: Flow<UserSettings> = combine(
        settingsDataStore.reminderEnabled,
        settingsDataStore.reminderTime,
        settingsDataStore.autoCleanup,
        settingsDataStore.keepDays,
        settingsDataStore.currentStreak,
        settingsDataStore.lastRecordingDate
    ) { values ->
        val enabled = values[0] as Boolean
        val timeStr = values[1] as String
        val cleanup = values[2] as Boolean
        val days = values[3] as Int
        val streak = values[4] as Int
        val lastDate = values[5] as String?
        UserSettings(
            dailyReminderEnabled = enabled,
            reminderTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")),
            autoCleanupEnabled = cleanup,
            keepDays = days,
            currentStreak = streak,
            lastRecordingDate = lastDate
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

    override suspend fun updateStreak(today: java.time.LocalDate) {
        val lastDateStr = settingsDataStore.lastRecordingDate.first()
        val currentStreak = settingsDataStore.currentStreak.first()
        
        if (lastDateStr == null) {
            // First time recording
            settingsDataStore.setStreak(1)
        } else {
            val lastDate = java.time.LocalDate.parse(lastDateStr)
            when (today) {
                lastDate -> { /* Already recorded today, streak stays the same */ }
                lastDate.plusDays(1) -> {
                    // Recorded yesterday, increment streak
                    settingsDataStore.setStreak(currentStreak + 1)
                }
                else -> {
                    // Streak broken
                    settingsDataStore.setStreak(1)
                }
            }
        }
        settingsDataStore.setLastRecordingDate(today.toString())
    }
}
