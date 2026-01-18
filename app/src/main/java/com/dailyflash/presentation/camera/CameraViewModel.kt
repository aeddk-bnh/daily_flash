package com.dailyflash.presentation.camera

import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyflash.core.camera.ICameraService
import com.dailyflash.domain.CaptureVideoUseCase
import com.dailyflash.domain.GetAllVideosUseCase
import com.dailyflash.domain.settings.GetUserSettingsUseCase
import com.dailyflash.domain.settings.UpdateStreakUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CameraUiState {
    data object Idle : CameraUiState
    data class Recording(val durationMs: Long) : CameraUiState
    data object Saving : CameraUiState
    data class Error(val message: String) : CameraUiState
    data class Success(val videoUri: Uri) : CameraUiState
}

class CameraViewModel(
    private val cameraService: ICameraService,
    private val captureVideoUseCase: CaptureVideoUseCase,
    private val getAllVideosUseCase: GetAllVideosUseCase,
    private val getUserSettingsUseCase: GetUserSettingsUseCase,
    private val updateStreakUseCase: UpdateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private val _recordingProgress = MutableStateFlow(0f)
    val recordingProgress: StateFlow<Float> = _recordingProgress.asStateFlow()

    private val _onionSkinUri = MutableStateFlow<Uri?>(null)
    val onionSkinUri: StateFlow<Uri?> = _onionSkinUri.asStateFlow()

    private val _onionSkinEnabled = MutableStateFlow(true)
    val onionSkinEnabled: StateFlow<Boolean> = _onionSkinEnabled.asStateFlow()

    val userSettings = getUserSettingsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = com.dailyflash.domain.settings.UserSettings()
        )

    init {
        loadLastThumbnail()
    }

    private fun loadLastThumbnail() {
        viewModelScope.launch {
            getAllVideosUseCase().collect { videos ->
                if (videos.isNotEmpty()) {
                    // Sort by date descending and get the most recent one
                    val lastVideo = videos.filter { it.date != java.time.LocalDate.now() }
                                          .maxByOrNull { it.date }
                                          ?: videos.maxByOrNull { it.date }
                    _onionSkinUri.value = lastVideo?.thumbnailUri
                }
            }
        }
    }

    fun toggleOnionSkin() {
        _onionSkinEnabled.value = !_onionSkinEnabled.value
    }

    fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        cameraService.bindToLifecycle(lifecycleOwner)
    }

    fun getPreviewView(): PreviewView {
        return cameraService.getPreviewView()
    }

    fun startRecording() {
        if (_uiState.value is CameraUiState.Recording) return

        viewModelScope.launch {
            _uiState.update { CameraUiState.Recording(0) }
            _recordingProgress.value = 0f
            
            // Side job for progress animation
            val progressJob = viewModelScope.launch {
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < 1000) {
                    _recordingProgress.value = (System.currentTimeMillis() - startTime) / 1000f
                    kotlinx.coroutines.delay(16)
                }
                _recordingProgress.value = 1f
            }

            // Record for 1 second (Project constraint)
            cameraService.recordClip(1000)
                .fold(
                    onSuccess = { uri ->
                        progressJob.cancel()
                        saveVideo(uri)
                    },
                    onFailure = { error ->
                        progressJob.cancel()
                        _uiState.update { CameraUiState.Error(error.message ?: "Recording failed") }
                    }
                )
        }
    }

    fun toggleTorch() {
        val newState = !_torchEnabled.value
        _torchEnabled.value = newState
        cameraService.toggleTorch(newState)
    }

    fun switchCamera() {
        cameraService.switchCamera()
    }

    private fun saveVideo(uri: Uri) {
        _uiState.update { CameraUiState.Saving }
        
        viewModelScope.launch {
            captureVideoUseCase(uri)
                .fold(
                    onSuccess = { entity ->
                        updateStreakUseCase()
                        _uiState.update { CameraUiState.Success(entity.uri) }
                    },
                    onFailure = { error ->
                        _uiState.update { CameraUiState.Error(error.message ?: "Saving failed") }
                    }
                )
        }
    }

    fun resetState() {
        _uiState.update { CameraUiState.Idle }
    }
}
