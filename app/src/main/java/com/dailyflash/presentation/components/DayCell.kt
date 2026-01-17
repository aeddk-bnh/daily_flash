package com.dailyflash.presentation.components

import com.dailyflash.presentation.theme.AppColors
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dailyflash.domain.VideoEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Calendar day cell component.
 * Shows date number and video thumbnail if available.
 *
 * @param date The date to display
 * @param video Video entity if exists for this date
 * @param isSelected Whether this date is currently selected
 * @param isToday Whether this date is today
 * @param isCurrentMonth Whether this date is in the current displayed month
 * @param onClick Callback when cell is clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayCell(
    date: LocalDate,
    video: VideoEntity?,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean = true,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    // ... (lines 57-102 remain similar, just skipped for brevity unless I use multi_replace, but replace_file needs contiguous block. 
    // I will replace the whole function signature and the modifier block to be safe and clean)
    
    val hasVideo = video != null
    
    // Background color animation
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> AppColors.SelectedDay
            hasVideo -> AppColors.SurfaceVariant
            else -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "bgColor"
    )
    
    // Text color animation
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            !isCurrentMonth -> AppColors.OnSurfaceVariant.copy(alpha = 0.4f)
            isToday -> AppColors.Primary
            else -> AppColors.OnSurface
        },
        animationSpec = tween(200),
        label = "textColor"
    )
    
    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    val semanticDescription = buildString {
        append(date.format(dateFormatter))
        if (isToday) append(", Today")
        if (hasVideo) append(", Has video")
        if (isSelected) append(", Selected")
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(2.dp, AppColors.TodayIndicator, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                enabled = isCurrentMonth,
                onClick = onClick,
                onLongClick = if (hasVideo) onLongClick else null
            )
            .semantics { contentDescription = semanticDescription },
        contentAlignment = Alignment.Center
    ) {
        // Video thumbnail as background
        if (hasVideo && video?.thumbnailUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(video.thumbnailUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Video thumbnail for ${date.dayOfMonth}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            // Dark overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Scrim.copy(alpha = 0.5f))
            )
        }
        
        // Date number
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (hasVideo && !isSelected) Color.White else textColor,
            textAlign = TextAlign.Center
        )
        
        // Video indicator dot (when has video but no thumbnail)
        if (hasVideo && video?.thumbnailUri == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(AppColors.HasVideoIndicator)
            )
        }
    }
}
