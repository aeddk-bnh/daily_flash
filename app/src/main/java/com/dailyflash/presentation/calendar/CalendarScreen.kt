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

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedVideoUri by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null) }

    if (selectedVideoUri != null) {
        com.dailyflash.presentation.components.VideoPlayerDialog(
            videoUri = selectedVideoUri!!,
            onDismiss = { selectedVideoUri = null }
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
                    
                    CalendarGrid(
                        yearMonth = state.yearMonth,
                        days = state.days,
                        onDayClick = { video ->
                            if (video != null) {
                                selectedVideoUri = video.uri
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
    onDayClick: (VideoEntity?) -> Unit
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
                hasVideo = video != null,
                onClick = { onDayClick(video) }
            )
        }
    }
}

@Composable
fun DayCell(
    date: LocalDate,
    hasVideo: Boolean,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                if (isToday) AppColors.SurfaceVariant else Color.Transparent
            )
            .border(
                width = if (isToday) 1.dp else 0.dp,
                color = if (isToday) AppColors.Primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasVideo) AppColors.Primary else AppColors.OnSurface
            )
            
            if (hasVideo) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AppColors.Secondary)
                )
            }
        }
    }
}
