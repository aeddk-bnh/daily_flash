package com.dailyflash.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.dailyflash.domain.VideoEntity
import com.dailyflash.presentation.components.DailyFlashScaffold
import com.dailyflash.presentation.components.DailyFlashTopBar
import com.dailyflash.presentation.theme.AppColors
import java.time.format.DateTimeFormatter

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    DailyFlashScaffold(
        topBar = {
            DailyFlashTopBar(
                title = "Gallery",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is GalleryUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AppColors.Primary
                    )
                }
                is GalleryUiState.Error -> {
                    Text(
                        text = state.message,
                        color = AppColors.Error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is GalleryUiState.Success -> {
                    if (state.videos.isEmpty()) {
                        EmptyGallery(modifier = Modifier.align(Alignment.Center))
                    } else {
                        val context = LocalContext.current
                        val imageLoader = remember(context) {
                            ImageLoader.Builder(context)
                                .components {
                                    add(VideoFrameDecoder.Factory())
                                }
                                .crossfade(true)
                                .build()
                        }
                        
                        VideoGrid(
                            videos = state.videos,
                            imageLoader = imageLoader,
                            onVideoClick = { video -> 
                                val index = state.videos.indexOf(video)
                                if (index != -1) onNavigateToDetail(index)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoGrid(
    videos: List<VideoEntity>,
    imageLoader: ImageLoader,
    onVideoClick: (VideoEntity) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(videos) { video ->
            VideoItem(
                video = video,
                imageLoader = imageLoader,
                onClick = { onVideoClick(video) }
            )
        }
    }
}

@Composable
fun VideoItem(
    video: VideoEntity,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (video.thumbnailUri != null) {
                AsyncImage(
                    model = video.thumbnailUri,
                    imageLoader = imageLoader,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder if no thumbnail
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = AppColors.OnSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = video.date.format(DateTimeFormatter.ofPattern("MMM dd")),
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.OnSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun EmptyGallery(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No videos yet",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.OnSurfaceVariant
        )
    }
}
