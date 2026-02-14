package com.dailyflash.core.reminders

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dailyflash.core.settings.SettingsDataStore
import com.dailyflash.data.reminders.WorkManagerReminderScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalTime

@RunWith(AndroidJUnit4::class)
class BootCompletedReceiverInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val workManager = WorkManager.getInstance(context)
    private val settingsDataStore = SettingsDataStore(context)

    @Before
    fun setup() = runBlocking {
        workManager.cancelAllWorkByTag(WorkManagerReminderScheduler.REMINDER_WORK_TAG)
        delay(300)
    }

    @Test
    fun onBoot_schedulesReminderWork_whenEnabled() = runBlocking {
        settingsDataStore.setReminderEnabled(true)
        settingsDataStore.setReminderTime(LocalTime.of(20, 0).toString().substring(0, 5))

        val receiver = BootCompletedReceiver()
        receiver.onReceive(context, Intent("android.intent.action.BOOT_COMPLETED"))

        val hasWork = waitForReminderWork()
        assertTrue(hasWork)
    }

    @Test
    fun onBoot_doesNotScheduleReminderWork_whenDisabled() = runBlocking {
        settingsDataStore.setReminderEnabled(false)

        val receiver = BootCompletedReceiver()
        receiver.onReceive(context, Intent("android.intent.action.BOOT_COMPLETED"))

        delay(1000)
        val workInfos = workManager.getWorkInfosByTag(WorkManagerReminderScheduler.REMINDER_WORK_TAG).get()
        val hasActiveWork = workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        assertFalse(hasActiveWork)
    }

    private suspend fun waitForReminderWork(timeoutMs: Long = 5000): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val workInfos = workManager.getWorkInfosByTag(WorkManagerReminderScheduler.REMINDER_WORK_TAG).get()
            val hasActiveWork = workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
            if (hasActiveWork) {
                return true
            }
            delay(250)
        }
        return false
    }
}
