package com.dailyflash.presentation.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyflash.domain.DeleteClipUseCase
import com.dailyflash.domain.GetAllVideosUseCase
import com.dailyflash.domain.VideoEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GalleryUiState {
    data object Loading : GalleryUiState
    data class Success(val videos: List<VideoEntity>) : GalleryUiState
    data class Error(val message: String) : GalleryUiState
}

class GalleryViewModel(
    private val getAllVideosUseCase: GetAllVideosUseCase,
    private val deleteClipUseCase: DeleteClipUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.Loading)
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    private fun loadVideos() {
        viewModelScope.launch {
            try {
                getAllVideosUseCase().collect { videos ->
                    _uiState.value = GalleryUiState.Success(videos)
                }
            } catch (e: Exception) {
                _uiState.value = GalleryUiState.Error(e.message ?: "Failed to load videos")
            }
        }
    }
    
    fun deleteVideo(video: VideoEntity) {
        viewModelScope.launch {
            try {
                deleteClipUseCase(video.id)
            } catch (e: Exception) {
                // error handling
            }
        }
    }
}
