package com.dailyflash.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dailyflash.R
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "daily_flash_reminder"
        const val WORK_TAG = "daily_reminder_work"
    }

    override suspend fun doWork(): Result {
        showNotification()
        rescheduleNextDay()
        return Result.success()
    }

    private fun showNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Daily Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to capture your daily flash"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open MainActivity (or launcher)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera) // Replace with app icon if available
            .setContentTitle("Time for your Daily Flash!")
            .setContentText("Capture your 1-second video now.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    private fun rescheduleNextDay() {
        // Logic to reschedule next day logic is simplistic here, 
        // relying on the Manager to schedule properly. 
        // Typically a daily worker should just be Periodic, but if we want exact timing we utilize OneTime with delay.
        // However, self-rescheduling needs reference to the DESIRED time.
        // For simplicity in this iteration, we rely on Periodic Work if possible or re-read settings.
        // Since we don't have easy access to SettingsRepository here without DI, 
        // we'll assume the manager handles rescheduling OR we accept that this OneTime worker ends.
        
        // CORRECTION: Architecture plan chose WorkManager. 
        // The robust way is sending the target time as input data.
        val targetHour = inputData.getInt("HOUR", 20)
        val targetMinute = inputData.getInt("MINUTE", 0)
        
        DailyNotificationManager(context).scheduleNextInstance(LocalTime.of(targetHour, targetMinute))
    }
}
