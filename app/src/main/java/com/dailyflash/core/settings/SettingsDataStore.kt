package com.dailyflash.core.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val AUTO_CLEANUP = booleanPreferencesKey("auto_cleanup")
        val KEEP_DAYS = intPreferencesKey("keep_days")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_RECORDING_DATE = stringPreferencesKey("last_recording_date")
    }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[REMINDER_ENABLED] ?: true }
    val reminderTime: Flow<String> = context.dataStore.data.map { it[REMINDER_TIME] ?: "20:00" }
    val autoCleanup: Flow<Boolean> = context.dataStore.data.map { it[AUTO_CLEANUP] ?: false }
    val keepDays: Flow<Int> = context.dataStore.data.map { it[KEEP_DAYS] ?: 30 }
    val currentStreak: Flow<Int> = context.dataStore.data.map { it[CURRENT_STREAK] ?: 0 }
    val lastRecordingDate: Flow<String?> = context.dataStore.data.map { it[LAST_RECORDING_DATE] }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REMINDER_ENABLED] = enabled }
    }

    suspend fun setReminderTime(time: String) {
        context.dataStore.edit { it[REMINDER_TIME] = time }
    }

    suspend fun setAutoCleanup(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_CLEANUP] = enabled }
    }

    suspend fun setKeepDays(days: Int) {
        context.dataStore.edit { it[KEEP_DAYS] = days }
    }

    suspend fun setStreak(streak: Int) {
        context.dataStore.edit { it[CURRENT_STREAK] = streak }
    }

    suspend fun setLastRecordingDate(date: String) {
        context.dataStore.edit { it[LAST_RECORDING_DATE] = date }
    }
}
