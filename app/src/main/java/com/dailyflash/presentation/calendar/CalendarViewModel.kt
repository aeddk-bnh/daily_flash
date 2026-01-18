package com.dailyflash.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyflash.domain.GetCalendarDataUseCase
import com.dailyflash.domain.VideoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

sealed interface CalendarUiState {
    data class Loading(val yearMonth: YearMonth) : CalendarUiState
    data class Success(
        val yearMonth: YearMonth,
        val days: Map<LocalDate, VideoEntity?>
    ) : CalendarUiState
    data class Error(val message: String) : CalendarUiState
}

class CalendarViewModel(
    private val getCalendarDataUseCase: GetCalendarDataUseCase,
    private val deleteClipUseCase: com.dailyflash.domain.DeleteClipUseCase
) : ViewModel() {

    fun deleteVideo(video: VideoEntity) {
        // Optimistically update UI or just reload?
        // Since getCalendarDataUseCase is a Flow, we might need to trigger a reload or if it observes DB, it auto-updates.
        // Assuming UseCase handles DB deletion and Flow emits new data.
        viewModelScope.launch {
            try {
                deleteClipUseCase(video.id)
                // Reload current month to refresh data
                val current = (uiState.value as? CalendarUiState.Success)?.yearMonth 
                    ?: (uiState.value as? CalendarUiState.Loading)?.yearMonth 
                    ?: YearMonth.now()
                loadMonth(current)
            } catch (e: Exception) {
                // Handle error (maybe show snackbar, but for now log/ignore)
            }
        }
    }

    private val _uiState = MutableStateFlow<CalendarUiState>(
        CalendarUiState.Loading(YearMonth.now())
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    fun loadMonth(yearMonth: YearMonth) {
        _uiState.update { CalendarUiState.Loading(yearMonth) }

        getCalendarDataUseCase(yearMonth)
            .onEach { dateMap ->
                _uiState.update { 
                    CalendarUiState.Success(yearMonth, dateMap) 
                }
            }
            .catch { error ->
                _uiState.update { 
                    CalendarUiState.Error(error.message ?: "Failed to load calendar data") 
                }
            }
            .launchIn(viewModelScope)
    }

    fun previousMonth() {
        val current = (uiState.value as? CalendarUiState.Success)?.yearMonth 
            ?: (uiState.value as? CalendarUiState.Loading)?.yearMonth 
            ?: YearMonth.now()
        loadMonth(current.minusMonths(1))
    }

    fun nextMonth() {
        val current = (uiState.value as? CalendarUiState.Success)?.yearMonth 
            ?: (uiState.value as? CalendarUiState.Loading)?.yearMonth 
            ?: YearMonth.now()
        loadMonth(current.plusMonths(1))
    }
}
