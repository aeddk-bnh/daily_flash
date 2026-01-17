package com.dailyflash.domain.settings

import kotlinx.coroutines.flow.Flow

interface ISettingsRepository {
    val settings: Flow<UserSettings>
    
    suspend fun updateReminder(enabled: Boolean, time: java.time.LocalTime)
    suspend fun updateAutoCleanup(enabled: Boolean, days: Int)
}
