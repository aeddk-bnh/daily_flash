package com.dailyflash.domain.settings

import java.time.LocalTime

data class UserSettings(
    val dailyReminderEnabled: Boolean = true,
    val reminderTime: LocalTime = LocalTime.of(20, 0),
    val autoCleanupEnabled: Boolean = false,
    val keepDays: Int = 30,
    val currentStreak: Int = 0,
    val lastRecordingDate: String? = null // Store as ISO-8601 string
)
