package com.dailyflash.presentation.export

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailyflash.presentation.components.DailyFlashButton
import com.dailyflash.presentation.components.DailyFlashScaffold
import com.dailyflash.presentation.components.DailyFlashSecondaryButton
import com.dailyflash.presentation.components.DailyFlashTopBar
import com.dailyflash.presentation.theme.AppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var exportedVideoUri by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null) }

    if (exportedVideoUri != null) {
        com.dailyflash.presentation.components.VideoPlayerDialog(
            videoUri = exportedVideoUri!!,
            onDismiss = { exportedVideoUri = null }
        )
    }

    DailyFlashScaffold(
        topBar = {
            DailyFlashTopBar(
                title = "Export Journal",
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            when (val state = uiState) {
                is ExportUiState.Idle -> {
                    ExportConfigSection(
                        startDate = state.startDate,
                        endDate = state.endDate,
                        audioTrack = state.audioTrack,
                        onStartDateChanged = { viewModel.updateDateRange(it, state.endDate) },
                        onEndDateChanged = { viewModel.updateDateRange(state.startDate, it) },
                        onAudioTrackSelected = { viewModel.setAudioTrack(it) },
                        onExportClick = { viewModel.startExport() }
                    )
                }
                is ExportUiState.Processing -> {
                    ProcessingSection(progress = state.progress)
                }
                is ExportUiState.Success -> {
                    SuccessSection(
                        onBack = onBack,
                        onPlay = { exportedVideoUri = state.uri }
                    )
                }
                is ExportUiState.Error -> {
                    ErrorSection(message = state.message, onRetry = { viewModel.resetState() })
                }
            }
        }
    }
}

@Composable
fun ColumnScope.ExportConfigSection(
    startDate: LocalDate,
    endDate: LocalDate,
    audioTrack: android.net.Uri?,
    onStartDateChanged: (LocalDate) -> Unit,
    onEndDateChanged: (LocalDate) -> Unit,
    onAudioTrackSelected: (android.net.Uri?) -> Unit,
    onExportClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Date Range",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.OnSurface
            )
            
            DateSelector(label = "From", date = startDate, onDateChanged = onStartDateChanged)
            DateSelector(label = "To", date = endDate, onDateChanged = onEndDateChanged)
            
            AudioSelector(
                selectedUri = audioTrack,
                onUriSelected = onAudioTrackSelected
            )
        }
    }
    
    Spacer(modifier = Modifier.weight(1f))
    
    DailyFlashButton(
        onClick = onExportClick,
        text = "Start Export",
        modifier = Modifier.fillMaxWidth().height(56.dp)
    )
}

@Composable
fun DateSelector(
    label: String,
    date: LocalDate,
    onDateChanged: (LocalDate) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnSurfaceVariant,
            modifier = Modifier.width(60.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Background, MaterialTheme.shapes.small)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onDateChanged(date.minusDays(1)) }) {
                Icon(Icons.Default.KeyboardArrowLeft, null, tint = AppColors.OnSurface)
            }
            
            Text(
                text = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.OnSurface,
                fontWeight = FontWeight.Medium
            )
            
            IconButton(onClick = { onDateChanged(date.plusDays(1)) }) {
                Icon(Icons.Default.KeyboardArrowRight, null, tint = AppColors.OnSurface)
            }
        }
    }
}

@Composable
fun AudioSelector(
    selectedUri: android.net.Uri?,
    onUriSelected: (android.net.Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        onUriSelected(uri)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Background Audio (Optional)",
            style = MaterialTheme.typography.titleMedium,
            color = AppColors.OnSurface
        )

        if (selectedUri != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.SurfaceVariant, MaterialTheme.shapes.small)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle, // Reusing existing icon or could use MusicNote if available
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Audio Selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.OnSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = { onUriSelected(null) }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Audio",
                        tint = AppColors.OnSurfaceVariant
                    )
                }
            }
        } else {
            DailyFlashSecondaryButton(
                onClick = { launcher.launch("audio/*") },
                text = "Select Audio File",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProcessingSection(progress: Float) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Exporting your memories...",
            style = MaterialTheme.typography.headlineSmall,
            color = AppColors.OnBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = AppColors.Primary,
            trackColor = AppColors.SurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.OnSurfaceVariant
        )
    }
}

@Composable
fun SuccessSection(onBack: () -> Unit, onPlay: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = AppColors.Success,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Export Complete!",
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.OnBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        DailyFlashButton(
            onClick = onPlay,
            text = "Play Video",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        DailyFlashSecondaryButton(
            onClick = onBack,
            text = "Back to Calendar",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = AppColors.Error,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Export Failed",
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.Error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.OnSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        DailyFlashSecondaryButton(
            onClick = onRetry,
            text = "Try Again",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
