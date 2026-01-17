package com.dailyflash.presentation.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.dailyflash.presentation.components.DailyFlashScaffold
import com.dailyflash.presentation.components.DailyFlashTopBar

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current

    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                viewModel.onReminderTimeChange(LocalTime.of(hour, minute))
            },
            settings.reminderTime.hour,
            settings.reminderTime.minute,
            true // 24h format
        )
    }

    DailyFlashScaffold(
        topBar = {
            DailyFlashTopBar(title = "Settings", onBackClick = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Reminder Section
            Text("Notifications", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Daily Reminder")
                Switch(
                    checked = settings.dailyReminderEnabled,
                    onCheckedChange = { viewModel.onReminderToggle(it) }
                )
            }
            
            if (settings.dailyReminderEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Reminder Time")
                    TextButton(onClick = { 
                        timePickerDialog.updateTime(settings.reminderTime.hour, settings.reminderTime.minute)
                        timePickerDialog.show() 
                    }) {
                        Text(settings.reminderTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Storage Section
            Text("Storage", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Auto Cleanup Old Videos")
                Switch(
                    checked = settings.autoCleanupEnabled,
                    onCheckedChange = { viewModel.onAutoCleanupToggle(it) }
                )
            }
            
            if (settings.autoCleanupEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Keep for (days)")
                    // Simple dropdown or text for now, could be improved
                    // Using a simple Button to cycle for simplicity or just display
                    // Requirement says "Keep videos for N days". 
                    // Implementing a simple cycle 30 -> 60 -> 90 -> 30
                    TextButton(onClick = {
                        val nextDays = when (settings.keepDays) {
                            30 -> 60
                            60 -> 90
                            else -> 30
                        }
                        viewModel.onKeepDaysChange(nextDays)
                    }) {
                        Text("${settings.keepDays} Days")
                    }
                }
            }
        }
    }
}
