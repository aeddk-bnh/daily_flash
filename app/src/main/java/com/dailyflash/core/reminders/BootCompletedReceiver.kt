package com.dailyflash.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dailyflash.data.notification.NotificationManagerImpl
import com.dailyflash.data.reminders.WorkManagerReminderScheduler
import com.dailyflash.data.settings.SettingsRepositoryImpl
import com.dailyflash.core.settings.SettingsDataStore
import com.dailyflash.domain.settings.GetUserSettingsUseCase
import com.dailyflash.domain.settings.UpdateReminderUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.intent.action.BOOT_COMPLETED") return

        // Manual DI for BroadcastReceiver
        val appContext = context.applicationContext
        val settingsDataStore = SettingsDataStore(appContext)
        val settingsRepository = SettingsRepositoryImpl(settingsDataStore)
        val getUserSettingsUseCase = GetUserSettingsUseCase(settingsRepository)
        
        val reminderScheduler = WorkManagerReminderScheduler(appContext)
        
        CoroutineScope(Dispatchers.IO).launch {
            val settings = getUserSettingsUseCase().first()
            if (settings.dailyReminderEnabled) {
                reminderScheduler.schedule(settings.reminderTime)
            }
        }
    }
}
