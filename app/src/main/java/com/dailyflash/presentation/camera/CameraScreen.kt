package com.dailyflash.presentation.camera

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.dailyflash.presentation.theme.AppColors

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToGallery: () -> Unit,
    onNavigateToCalendar: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToLifecycle(lifecycleOwner)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview
        AndroidView(
            factory = { viewModel.getPreviewView() },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls
    LaunchedEffect(uiState) {
        if (uiState is CameraUiState.Success) {
            android.widget.Toast.makeText(
                context,
                "Video saved!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {

            
            // Bottom Controls
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Calendar Button (Left)
                IconButton(
                    onClick = onNavigateToCalendar,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .size(48.dp)
                        .background(color = AppColors.SurfaceVariant.copy(alpha = 0.6f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = AppColors.OnSurface
                    )
                }

                // Shutter Button (Center)
                Box(modifier = Modifier.align(Alignment.Center)) {
                    if (uiState is CameraUiState.Recording) {
                        RecordingTimer()
                    } else {
                        ShutterButton(
                            onClick = { viewModel.startRecording() }
                        )
                    }
                }

                // Gallery Button (Right)
                IconButton(
                    onClick = onNavigateToGallery,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(48.dp)
                        .background(color = AppColors.SurfaceVariant.copy(alpha = 0.6f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Gallery",
                        tint = AppColors.OnSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ShutterButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .border(4.dp, Color.White, CircleShape)
            .padding(4.dp)
            .clip(CircleShape)
            .background(AppColors.RecordRed)
    ) {
        // Inner circle
    }
}

@Composable
fun RecordingTimer() {
    val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(AppColors.RecordRed.copy(alpha = alpha))
        )
        Text(
            text = "Recording...",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}
