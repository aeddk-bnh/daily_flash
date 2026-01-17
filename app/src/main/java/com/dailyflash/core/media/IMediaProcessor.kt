package com.dailyflash.core.media

import android.net.Uri

/**
 * Interface for video processing operations.
 * Handles video concatenation and audio mixing.
 */

interface IMediaProcessor {
    /**
     * Stitch multiple video clips into a single output.
     * @param clips List of video URIs to concatenate
     * @param outputUri Destination URI for output
     * @param audioTrack Optional background audio track
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return Result with output URI or error
     */
    suspend fun stitchVideos(
        clips: List<Uri>,
        outputUri: Uri,
        audioTrack: Uri? = null,
        enableFade: Boolean = false,
        textOverlay: (index: Int) -> String? = { null },
        onProgress: (Float) -> Unit = {}
    ): Result<Uri>
    
    /**
     * Cancel ongoing processing.
     */
    fun cancel()
}
