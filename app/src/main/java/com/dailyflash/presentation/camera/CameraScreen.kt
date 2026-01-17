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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material3.CircularProgressIndicator
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
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val torchEnabled by viewModel.torchEnabled.collectAsState()
    val recordingProgress by viewModel.recordingProgress.collectAsState()

    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToLifecycle(lifecycleOwner)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Camera Preview
        AndroidView(
            factory = { viewModel.getPreviewView() },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Controls (Top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Torch Toggle
            IconButton(
                onClick = { viewModel.toggleTorch() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Torch",
                    tint = if (torchEnabled) Color.Yellow else Color.White
                )
            }

            // Settings
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

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
                        .size(56.dp)
                        .background(color = Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = Color.White
                    )
                }

                // Shutter Button (Center)
                Box(modifier = Modifier.align(Alignment.Center)) {
                    ShutterButton(
                        onClick = { viewModel.startRecording() },
                        isRecording = uiState is CameraUiState.Recording,
                        progress = recordingProgress
                    )
                }

                // Gallery Button (Right)
                IconButton(
                    onClick = onNavigateToGallery,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(56.dp)
                        .background(color = Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = "Gallery",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ShutterButton(
    onClick: () -> Unit,
    isRecording: Boolean,
    progress: Float
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clickable(onClick = onClick, enabled = !isRecording),
        contentAlignment = Alignment.Center
    ) {
        // Progress Ring
        if (isRecording) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                color = AppColors.RecordRed,
                strokeWidth = 4.dp,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        } else {
            // Static outer ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(4.dp, Color.White, CircleShape)
            )
        }

        // Inner Red Circle
        Box(
            modifier = Modifier
                .size(if (isRecording) 40.dp else 60.dp)
                .clip(if (isRecording) RoundedCornerShape(4.dp) else CircleShape)
                .background(AppColors.RecordRed)
        )
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
