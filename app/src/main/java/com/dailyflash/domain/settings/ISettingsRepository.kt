package com.dailyflash.domain.settings

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

interface ISettingsRepository {
    val userSettings: Flow<UserSettings>
    suspend fun updateReminderTime(time: LocalTime)
    suspend fun toggleReminder(isEnabled: Boolean)
    suspend fun toggleAutoCleanup(isEnabled: Boolean)
    suspend fun updateStreak(today: LocalDate)
}
