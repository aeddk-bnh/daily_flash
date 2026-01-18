package com.dailyflash.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dailyflash.domain.VideoEntity
import com.dailyflash.presentation.components.DailyFlashScaffold
import com.dailyflash.presentation.components.DailyFlashTopBar
import com.dailyflash.presentation.theme.AppColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import coil.compose.AsyncImage
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedVideoUri by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null) }
    var videoToDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<VideoEntity?>(null) }

    if (selectedVideoUri != null) {
        com.dailyflash.presentation.components.VideoPlayerDialog(
            videoUri = selectedVideoUri!!,
            onDismiss = { selectedVideoUri = null }
        )
    }

    if (videoToDelete != null) {
        AlertDialog(
            onDismissRequest = { videoToDelete = null },
            title = { Text("Delete Video") },
            text = { Text("Are you sure you want to delete the video for ${videoToDelete?.date}?") },
            confirmButton = {
                TextButton(onClick = {
                    videoToDelete?.let { viewModel.deleteVideo(it) }
                    videoToDelete = null
                }) {
                    Text("Delete", color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { videoToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    DailyFlashScaffold(
        topBar = {
            DailyFlashTopBar(
                title = "DailyFlash",
                onBackClick = onNavigateToCamera,
                actions = {
                    IconButton(onClick = onNavigateToExport) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export",
                            tint = AppColors.OnBackground
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is CalendarUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                }
                is CalendarUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = AppColors.Error)
                    }
                }
                is CalendarUiState.Success -> {
                    MonthHeader(
                        yearMonth = state.yearMonth,
                        onPrevious = { viewModel.previousMonth() },
                        onNext = { viewModel.nextMonth() }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DaysOfWeekHeader()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val context = LocalContext.current
                    val imageLoader = remember(context) {
                        ImageLoader.Builder(context)
                            .components { add(VideoFrameDecoder.Factory()) }
                            .crossfade(true)
                            .build()
                    }

                    CalendarGrid(
                        yearMonth = state.yearMonth,
                        days = state.days,
                        imageLoader = imageLoader,
                        onDayClick = { video ->
                            if (video != null) {
                                selectedVideoUri = video.uri
                            }
                        },
                        onDayLongClick = { video ->
                            if (video != null) {
                                videoToDelete = video
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.KeyboardArrowLeft, "Previous Month", tint = AppColors.OnBackground)
        }
        
        Text(
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.OnBackground,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNext) {
            Icon(Icons.Default.KeyboardArrowRight, "Next Month", tint = AppColors.OnBackground)
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        DayOfWeek.values().forEach { dayOfWeek ->
            Text(
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    days: Map<LocalDate, VideoEntity?>,
    imageLoader: ImageLoader,
    onDayClick: (VideoEntity?) -> Unit,
    onDayLongClick: (VideoEntity?) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startOffset = firstDayOfMonth.dayOfWeek.value - 1 // Monday = 1, we want 0-indexed for grid
    
    // Total cells needed: offset + days
    val totalCells = startOffset + daysInMonth
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Empty cells for offset
        items(startOffset) {
            Box(modifier = Modifier.aspectRatio(1f))
        }
        
        // Day cells
        items(daysInMonth) { dayIndex ->
            val date = yearMonth.atDay(dayIndex + 1)
            val video = days[date]
            
            DayCell(
                date = date,
                video = video,
                imageLoader = imageLoader,
                onClick = { onDayClick(video) },
                onLongClick = { onDayLongClick(video) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayCell(
    date: LocalDate,
    video: VideoEntity?,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    val hasVideo = video != null
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        label = "day_cell_scale"
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(CircleShape)
            .background(
                if (isToday) AppColors.SurfaceVariant else Color.Transparent
            )
            .border(
                width = if (isToday) 1.dp else 0.dp,
                color = if (isToday) AppColors.Primary else Color.Transparent,
                shape = CircleShape
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (video != null) {
            AsyncImage(
                model = video.uri,
                contentDescription = null,
                imageLoader = imageLoader,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.6f
            )
        }

        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (hasVideo) AppColors.OnPrimary else AppColors.OnSurface,
            fontWeight = if (hasVideo || isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}
