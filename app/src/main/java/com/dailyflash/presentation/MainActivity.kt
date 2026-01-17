package com.dailyflash.presentation

import com.dailyflash.presentation.theme.AppColors
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.dailyflash.core.storage.StorageManager
import com.dailyflash.core.camera.CameraService
import com.dailyflash.data.VideoRepositoryImpl
import com.dailyflash.core.media.MediaProcessor
import com.dailyflash.domain.CaptureVideoUseCase
import com.dailyflash.domain.GetCalendarDataUseCase
import com.dailyflash.domain.ExportJournalUseCase
import com.dailyflash.presentation.navigation.NavGraph
import com.dailyflash.presentation.theme.DailyFlashTheme

/**
 * Main entry point for DailyFlash.
 * Handles permission requests and dependency injection setup.
 */
class MainActivity : ComponentActivity() {
    
    // Required permissions for the app
    private val requiredPermissions = if (android.os.Build.VERSION.SDK_INT >= 33) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
    
    // Dependencies
    private lateinit var storageManager: StorageManager
    private lateinit var cameraService: CameraService
    private lateinit var videoRepository: VideoRepositoryImpl
    private lateinit var mediaProcessor: MediaProcessor
    
    // UseCases
    private lateinit var captureVideoUseCase: CaptureVideoUseCase
    private lateinit var getCalendarDataUseCase: GetCalendarDataUseCase
    private lateinit var exportJournalUseCase: ExportJournalUseCase
    private lateinit var deleteClipUseCase: com.dailyflash.domain.DeleteClipUseCase
    private lateinit var getAllVideosUseCase: com.dailyflash.domain.GetAllVideosUseCase
    
    // Settings Dependencies
    private lateinit var settingsDataStore: com.dailyflash.core.settings.SettingsDataStore
    private lateinit var settingsRepository: com.dailyflash.data.settings.SettingsRepositoryImpl
    private lateinit var notificationManager: com.dailyflash.core.notification.DailyNotificationManager
    
    private lateinit var getUserSettingsUseCase: com.dailyflash.domain.settings.GetUserSettingsUseCase
    private lateinit var updateReminderUseCase: com.dailyflash.domain.settings.UpdateReminderUseCase
    private lateinit var updateAutoCleanupUseCase: com.dailyflash.domain.settings.UpdateAutoCleanupUseCase
    
    // Permission state
    private var permissionsGranted by mutableStateOf(false)
    private var showPermissionDenied by mutableStateOf(false)
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value } // Simplified check
        if (allGranted) {
            permissionsGranted = true
            showPermissionDenied = false
        } else {
            // Check essential permissions
            val camera = permissions[Manifest.permission.CAMERA] ?: false
            val audio = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            if (!camera || !audio) {
                showPermissionDenied = true
            } else {
                permissionsGranted = true // Notification perm might be denied but app can work
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        storageManager = StorageManager(this)
        cameraService = CameraService(this, storageManager)
        videoRepository = VideoRepositoryImpl(storageManager, contentResolver)
        mediaProcessor = MediaProcessor(this, storageManager)
        
        // Settings Init
        settingsDataStore = com.dailyflash.core.settings.SettingsDataStore(this)
        settingsRepository = com.dailyflash.data.settings.SettingsRepositoryImpl(settingsDataStore)
        notificationManager = com.dailyflash.core.notification.DailyNotificationManager(this)
        
        getUserSettingsUseCase = com.dailyflash.domain.settings.GetUserSettingsUseCase(settingsRepository)
        updateReminderUseCase = com.dailyflash.domain.settings.UpdateReminderUseCase(settingsRepository, notificationManager)
        updateAutoCleanupUseCase = com.dailyflash.domain.settings.UpdateAutoCleanupUseCase(settingsRepository)
        
        captureVideoUseCase = CaptureVideoUseCase(videoRepository)
        getCalendarDataUseCase = GetCalendarDataUseCase(videoRepository)
        exportJournalUseCase = ExportJournalUseCase(videoRepository, mediaProcessor, storageManager)
        deleteClipUseCase = com.dailyflash.domain.DeleteClipUseCase(videoRepository)
        getAllVideosUseCase = com.dailyflash.domain.GetAllVideosUseCase(videoRepository)
        
        // Schedule auto-cleanup
        com.dailyflash.core.storage.CleanupWorker.schedule(this)
        
        // Check existing permissions
        checkPermissions()
        
        setContent {
            DailyFlashTheme {
                val navController = rememberNavController()
                
                when {
                    permissionsGranted -> {
                        NavGraph(
                            navController = navController,
                            cameraService = cameraService,
                            captureVideoUseCase = captureVideoUseCase,
                            getCalendarDataUseCase = getCalendarDataUseCase,
                            exportJournalUseCase = exportJournalUseCase,
                            deleteClipUseCase = deleteClipUseCase,
                            getAllVideosUseCase = getAllVideosUseCase,
                            getUserSettingsUseCase = getUserSettingsUseCase,
                            updateReminderUseCase = updateReminderUseCase,
                            updateAutoCleanupUseCase = updateAutoCleanupUseCase
                        )
                    }
                    showPermissionDenied -> {
                        // Permission denied state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Please grant camera and microphone permissions to use DailyFlash",
                                color = AppColors.OnBackground
                            )
                        }
                    }
                    else -> {
                        // Loading state while checking permissions
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AppColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Re-check permissions in case user granted them in settings
        if (!permissionsGranted) {
            checkPermissions()
        }
    }
    
    private fun checkPermissions() {
        val allGranted = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allGranted) {
            permissionsGranted = true
        } else {
            // Request permissions
            permissionLauncher.launch(requiredPermissions)
        }
    }
}
