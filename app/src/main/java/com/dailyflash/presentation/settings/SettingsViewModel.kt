package com.dailyflash.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyflash.domain.settings.GetUserSettingsUseCase
import com.dailyflash.domain.settings.UpdateAutoCleanupUseCase
import com.dailyflash.domain.settings.UpdateReminderUseCase
import com.dailyflash.domain.settings.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

class SettingsViewModel(
    getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateReminderUseCase: UpdateReminderUseCase,
    private val updateAutoCleanupUseCase: UpdateAutoCleanupUseCase
) : ViewModel() {

    val settings: StateFlow<UserSettings> = getUserSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    fun onReminderToggle(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            updateReminderUseCase(enabled, current.reminderTime)
        }
    }

    fun onReminderTimeChange(time: LocalTime) {
        viewModelScope.launch {
            val current = settings.value
            updateReminderUseCase(current.dailyReminderEnabled, time)
        }
    }

    fun onAutoCleanupToggle(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            updateAutoCleanupUseCase(enabled, current.keepDays)
        }
    }

    fun onKeepDaysChange(days: Int) {
        viewModelScope.launch {
            val current = settings.value
            // Helper logic to ensure days is valid if needed
            updateAutoCleanupUseCase(current.autoCleanupEnabled, days)
        }
    }
}
