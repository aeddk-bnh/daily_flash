package com.dailyflash.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyflash.domain.GetAllVideosUseCase
import com.dailyflash.domain.VideoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

sealed interface GalleryUiState {
    data object Loading : GalleryUiState
    data class Success(val videos: List<VideoEntity>) : GalleryUiState
    data class Error(val message: String) : GalleryUiState
}

class GalleryViewModel(
    private val getAllVideosUseCase: GetAllVideosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    private fun loadVideos() {
        getAllVideosUseCase()
            .onEach { videos ->
                _uiState.update { GalleryUiState.Success(videos) }
            }
            .catch { error ->
                _uiState.update { GalleryUiState.Error(error.message ?: "Unknown error") }
            }
            .launchIn(viewModelScope)
    }
}
