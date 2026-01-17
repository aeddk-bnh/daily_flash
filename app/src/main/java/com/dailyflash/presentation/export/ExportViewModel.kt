package com.dailyflash.presentation.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyflash.domain.ExportJournalUseCase
import com.dailyflash.domain.ExportProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate

sealed interface ExportUiState {
    data class Idle(
        val startDate: LocalDate = LocalDate.now().minusDays(30),
        val endDate: LocalDate = LocalDate.now(),
        val audioTrack: Uri? = null
    ) : ExportUiState
    
    data class Processing(val progress: Float) : ExportUiState
    data class Success(val uri: Uri) : ExportUiState
    data class Error(val message: String) : ExportUiState
}

class ExportViewModel(
    private val exportJournalUseCase: ExportJournalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun updateDateRange(start: LocalDate, end: LocalDate) {
        val currentState = _uiState.value
        if (currentState is ExportUiState.Idle) {
            _uiState.update { 
                currentState.copy(startDate = start, endDate = end) 
            }
        }
    }

    fun setAudioTrack(uri: Uri?) {
        val currentState = _uiState.value
        if (currentState is ExportUiState.Idle) {
            _uiState.update { 
                currentState.copy(audioTrack = uri) 
            }
        }
    }

    fun startExport() {
        val state = _uiState.value as? ExportUiState.Idle ?: return
        
        // Validate range
        if (state.startDate.isAfter(state.endDate)) {
            _uiState.update { ExportUiState.Error("Invalid date range") }
            return
        }

        exportJournalUseCase(
            dateRange = state.startDate..state.endDate,
            audioTrack = state.audioTrack
        ).onEach { progress ->
            when (progress) {
                is ExportProgress.Idle -> {} // No-op
                is ExportProgress.Preparing -> {
                    _uiState.update { ExportUiState.Processing(0f) }
                }
                is ExportProgress.Processing -> {
                    _uiState.update { ExportUiState.Processing(progress.progress) }
                }
                is ExportProgress.Completed -> {
                    _uiState.update { ExportUiState.Success(progress.outputUri) }
                }
                is ExportProgress.Failed -> {
                    _uiState.update { ExportUiState.Error(progress.error.message ?: "Export failed") }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun resetState() {
        _uiState.update { ExportUiState.Idle() }
    }
}
