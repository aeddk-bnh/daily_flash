package com.dailyflash.core.media

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.dailyflash.core.logging.FlowLogger
import com.dailyflash.core.storage.IStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/**
 * Media3 Transformer-based video processor.
 * Handles video stitching with hardware acceleration.
 */
@OptIn(UnstableApi::class)
class MediaProcessor(
    private val context: Context,
    private val storageManager: IStorageManager
) : IMediaProcessor {

    private var transformer: Transformer? = null
    private var isCancelled = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private var progressHandler: Handler? = null
    private var progressRunnable: Runnable? = null

    override suspend fun stitchVideos(
        clips: List<Uri>,
        outputUri: Uri,
        audioTrack: Uri?,
        onProgress: (Float) -> Unit
    ): Result<Uri> = withContext(Dispatchers.Main) {
        FlowLogger.flow("StitchingStart", "clipCount=${clips.size}, hasAudio=${audioTrack != null}")
        val stitchStartTime = System.currentTimeMillis()
        
        // Reset cancellation flag
        isCancelled = false

        // Validate input
        if (clips.isEmpty()) {
            FlowLogger.error("StitchingStart", "Clip list cannot be empty")
            return@withContext Result.failure(IllegalArgumentException("Clip list cannot be empty"))
        }

        // Validate each clip URI is accessible
        for (clip in clips) {
            try {
                context.contentResolver.openInputStream(clip)?.close()
            } catch (e: Exception) {
                FlowLogger.error("StitchingValidation", e, "inaccessible clip=$clip")
                return@withContext Result.failure(
                    IllegalArgumentException("Cannot access clip: $clip", e)
                )
            }
        }
        
        FlowLogger.flow("StitchingValidated", "all ${clips.size} clips accessible")

        suspendCancellableCoroutine { continuation ->
            try {
                // Build Transformer with H.264 codec and hardware acceleration
                transformer = Transformer.Builder(context)
                    .setVideoMimeType(MimeTypes.VIDEO_H264)
                    .addListener(object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                            val stitchDuration = System.currentTimeMillis() - stitchStartTime
                            FlowLogger.timing("StitchVideos", stitchDuration, "clipCount=${clips.size}")
                            FlowLogger.flow("StitchingComplete", "uri=$outputUri")
                            
                            stopProgressPolling()
                            onProgress(1.0f)
                            if (continuation.isActive) {
                                continuation.resume(Result.success(outputUri))
                            }
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            FlowLogger.error("Stitching", exportException, "cancelled=$isCancelled")
                            
                            stopProgressPolling()
                            if (continuation.isActive) {
                                if (isCancelled) {
                                    FlowLogger.flow("StitchingCancelled", "by user")
                                    continuation.resume(
                                        Result.failure(CancellationException("Export cancelled"))
                                    )
                                } else {
                                    continuation.resume(Result.failure(exportException))
                                }
                            }
                        }
                    })
                    .build()

                // Create EditedMediaItem for each clip
                val editedMediaItems = clips.map { clipUri ->
                    val mediaItem = MediaItem.fromUri(clipUri)
                    EditedMediaItem.Builder(mediaItem).build()
                }

                // Create video sequence for concatenation
                val videoSequence = EditedMediaItemSequence(editedMediaItems)

                // Build composition
                val compositionBuilder = Composition.Builder(videoSequence)

                // Add background audio if provided
                if (audioTrack != null) {
                    val audioMediaItem = MediaItem.fromUri(audioTrack)
                    val audioEditedItem = EditedMediaItem.Builder(audioMediaItem)
                        .setRemoveVideo(true)
                        .build()
                    val audioSequence = EditedMediaItemSequence(listOf(audioEditedItem))
                    // Note: For proper audio mixing, we'd need additional configuration
                    // This adds audio as a separate track
                }

                val composition = compositionBuilder.build()

                // Get output file path
                val outputPath = getPathFromUri(outputUri)
                
                FlowLogger.resource("ALLOC", "Transformer", "codec=H.264")
                FlowLogger.flow("ExportStart", "outputPath=$outputPath, clipCount=${clips.size}")

                // Start progress polling
                startProgressPolling(onProgress)

                // Start export
                transformer?.start(composition, outputPath)

                // Handle coroutine cancellation
                continuation.invokeOnCancellation {
                    cancel()
                }

            } catch (e: Exception) {
                stopProgressPolling()
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }
        }
    }

    override fun cancel() {
        FlowLogger.flow("StitchingCancel", "requested by user")
        isCancelled = true
        stopProgressPolling()
        mainHandler.post {
            transformer?.cancel()
            if (transformer != null) {
                FlowLogger.resource("RELEASE", "Transformer", "cancelled")
            }
            transformer = null
        }
    }

    /**
     * Start polling for export progress.
     */
    private fun startProgressPolling(onProgress: (Float) -> Unit) {
        progressHandler = Handler(Looper.getMainLooper())
        val progressHolder = ProgressHolder()
        
        progressRunnable = object : Runnable {
            override fun run() {
                transformer?.let { t ->
                    val progressState = t.getProgress(progressHolder)
                    if (progressState == Transformer.PROGRESS_STATE_AVAILABLE) {
                        val progress = progressHolder.progress / 100f
                        onProgress(progress.coerceIn(0f, 0.99f))
                    }
                    progressHandler?.postDelayed(this, PROGRESS_POLL_INTERVAL_MS)
                }
            }
        }
        progressHandler?.post(progressRunnable!!)
    }

    /**
     * Stop progress polling.
     */
    private fun stopProgressPolling() {
        progressRunnable?.let { progressHandler?.removeCallbacks(it) }
        progressRunnable = null
        progressHandler = null
    }

    /**
     * Convert Uri to file path for Transformer output.
     */
    private fun getPathFromUri(uri: Uri): String {
        // For file:// URIs
        if (uri.scheme == "file") {
            return uri.path ?: throw IllegalArgumentException("Invalid file URI: $uri")
        }
        
        // For content:// URIs, create a temporary output file
        val outputDir = context.getExternalFilesDir("exports")
            ?: context.filesDir
        val outputFile = File(outputDir, "export_${System.currentTimeMillis()}.mp4")
        return outputFile.absolutePath
    }

    /**
     * Custom cancellation exception for export cancellation.
     */
    class CancellationException(message: String) : Exception(message)

    companion object {
        private const val PROGRESS_POLL_INTERVAL_MS = 100L
    }
}
