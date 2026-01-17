package com.dailyflash.domain.settings

import java.time.LocalDate
import java.time.LocalTime

data class UserSettings(
    val dailyReminderTime: LocalTime? = LocalTime.of(20, 0), // Default 8 PM
    val isReminderEnabled: Boolean = false,
    val autoCleanupEnabled: Boolean = false,
    val currentStreak: Int = 0,
    val lastRecordingDate: LocalDate? = null
)
