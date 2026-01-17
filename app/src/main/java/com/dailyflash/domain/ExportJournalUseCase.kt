package com.dailyflash.domain

import android.net.Uri
import com.dailyflash.core.media.IMediaProcessor

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.util.UUID

/**
 * Use case for exporting video journal.
 * Orchestrates fetching clips and stitching them into a single video.
 */
class ExportJournalUseCase(
    private val videoRepository: IVideoRepository,
    private val mediaProcessor: IMediaProcessor,
    private val storageManager: com.dailyflash.core.storage.IStorageManager
) {
    /**
     * Export video journal for a date range.
     * @param dateRange Date range to export
     * @param audioTrack Optional background audio track
     * @return Flow emitting export progress states
     */
    operator fun invoke(
        dateRange: ClosedRange<LocalDate>,
        audioTrack: Uri? = null,
        options: ExportOptions = ExportOptions()
    ): Flow<ExportProgress> = channelFlow {
        send(ExportProgress.Idle)

        // 1. Fetch clips in range
        val clips = videoRepository.getVideosInRange(dateRange.start, dateRange.endInclusive)
        if (clips.isEmpty()) {
            send(ExportProgress.Failed(IllegalStateException("No clips in range")))
            return@channelFlow
        }

        send(ExportProgress.Preparing(clips.size))

        // 2. Create temp file for processing
        val tempFile = createTempExportFile()
        val tempUri = Uri.fromFile(tempFile)

        // 3. Stitch videos with progress tracking
        mediaProcessor.stitchVideos(
            clips = clips.map { it.uri },
            outputUri = tempUri,
            audioTrack = audioTrack,
            options = options,
            onProgress = { progress ->
                trySend(ExportProgress.Processing(progress))
            }
        ).fold(
            onSuccess = { _ -> 
                // 4. Move to public Gallery (MediaStore) via Storage Manager
                try {
                    val publicUri = storageManager.exportVideoToGallery(tempFile)
                    send(ExportProgress.Completed(publicUri))
                    tempFile.delete() // Cleanup temp
                } catch (e: Exception) {
                    send(ExportProgress.Failed(e))
                }
            },
            onFailure = { error -> send(ExportProgress.Failed(error)) }
        )
    }

    private fun createTempExportFile(): java.io.File {
        return storageManager.createTempFile("export_temp_", ".mp4")
    }
}
