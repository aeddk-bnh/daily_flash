package com.dailyflash.domain.reminders

import java.time.LocalTime

interface ReminderScheduler {
    fun schedule(time: LocalTime)
    fun cancel()
}
