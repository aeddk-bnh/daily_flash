package com.dailyflash.domain

import android.net.Uri

/**
 * Sealed class representing export progress states.
 * Used to communicate export status from use case to presentation layer.
 */
sealed class ExportProgress {
    /**
     * Initial idle state before export starts.
     */
    data object Idle : ExportProgress()

    /**
     * Preparing state - clips are being gathered.
     * @property clipCount Number of clips to be processed
     */
    data class Preparing(val clipCount: Int) : ExportProgress()

    /**
     * Processing state - video stitching in progress.
     * @property progress Progress value from 0.0 to 1.0
     */
    data class Processing(val progress: Float) : ExportProgress()

    /**
     * Export completed successfully.
     * @property outputUri URI of the exported video file
     */
    data class Completed(val outputUri: Uri) : ExportProgress()

    /**
     * Export failed with an error.
     * @property error The throwable that caused the failure
     */
    data class Failed(val error: Throwable) : ExportProgress()
}
