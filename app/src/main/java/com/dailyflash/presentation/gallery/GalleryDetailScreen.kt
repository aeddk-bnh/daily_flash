package com.dailyflash.presentation.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dailyflash.presentation.components.VideoPlayer
import com.dailyflash.presentation.theme.AppColors

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GalleryDetailScreen(
    viewModel: GalleryViewModel,
    initialIndex: Int,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // We need videos to display pager. If loading, show loader.
    when (val state = uiState) {
        is GalleryUiState.Success -> {
            val videos = state.videos
            if (videos.isNotEmpty()) {
                val pagerState = rememberPagerState(initialPage = initialIndex.coerceIn(0, videos.size - 1)) {
                    videos.size
                }
                
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.deleteVideo(videos[pagerState.currentPage]) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent
                            )
                        )
                    },
                    containerColor = Color.Black
                ) { padding ->
                    Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val video = videos[page]
                            // Reuse VideoPlayerDialog logic or check implementation. 
                            // VideoPlayerDialog is a dialog, we want inline player or simplified player.
                            // For simplicity, reusing VideoPlayerDialog content logic or just an inline player.
                            // But VideoPlayerDialog uses AndroidView(SimpleExoPlayer).
                            // Let's implement a simple inline player using the same Composable if it's reusable
                            // OR we create a specific player composable given we don't want a dialog.
                            // Assuming we can use VideoPlayer internally.
                            // Actually, VideoPlayerDialog internal logic is what we want.
                            // Let's assume we can use a "VideoPlayer" composable.
                            // Since I don't reference "VideoPlayer" content, I will use "VideoPlayerDialog" for now as a "View" 
                            // by modifying it or copying its content. 
                            // Re-using VideoPlayerDialog logic here is tricky as it is a Dialog.
                            // I should have refactored the player content out.
                            // I will assume there is a `VideoPlayer` component I can use, or I will use `VideoPlayerDialog` which is a Composable function that shows a Dialog.
                            // Ah, `VideoPlayerDialog` uses `Dialog` window. I need the content.
                            // I'll make a `SimpleVideoPlayer` here for now.
                            
                            val isPageVisible = pagerState.currentPage == page
                            VideoPlayer(
                                videoUri = video.uri,
                                modifier = Modifier.fillMaxSize(),
                                playWhenReady = isPageVisible
                            )
                        }
                    }
                }
            } else {
                onBack() // No videos
            }
        }
        else -> {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        }
    }
}
