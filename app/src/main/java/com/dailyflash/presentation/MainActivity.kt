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
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    
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
    
    // Permission state
    private var permissionsGranted by mutableStateOf(false)
    private var showPermissionDenied by mutableStateOf(false)
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            permissionsGranted = true
            showPermissionDenied = false
        } else {
            showPermissionDenied = true
            Toast.makeText(
                this,
                "Camera and microphone permissions are required",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        storageManager = StorageManager(this)
        cameraService = CameraService(this, storageManager)
        videoRepository = VideoRepositoryImpl(storageManager, contentResolver)
        mediaProcessor = MediaProcessor(this, storageManager)
        
        captureVideoUseCase = CaptureVideoUseCase(videoRepository)
        getCalendarDataUseCase = GetCalendarDataUseCase(videoRepository)
        exportJournalUseCase = ExportJournalUseCase(videoRepository, mediaProcessor, storageManager)
        deleteClipUseCase = com.dailyflash.domain.DeleteClipUseCase(videoRepository)
        getAllVideosUseCase = com.dailyflash.domain.GetAllVideosUseCase(videoRepository)
        
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
                            getAllVideosUseCase = getAllVideosUseCase
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
