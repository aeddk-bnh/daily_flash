package com.dailyflash.core.storage

import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.time.LocalDate

/**
 * Data class representing a video file in storage.
 */
data class VideoFile(
    val id: String,
    val uri: Uri,
    val date: LocalDate,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAt: Long,
    val thumbnailUri: Uri? = null
) {
    companion object {
        /**
         * Create a VideoFile from a File object.
         * @param file The file on disk
         * @param date The date this video belongs to
         * @param durationMs Duration in milliseconds (default 1000 for 1-second clips)
         */
        fun fromFile(file: File, date: LocalDate, durationMs: Long = 1000L): VideoFile {
            return VideoFile(
                id = file.nameWithoutExtension,
                uri = file.toUri(),
                date = date,
                durationMs = durationMs,
                sizeBytes = file.length(),
                createdAt = file.lastModified()
            )
        }
    }
}
