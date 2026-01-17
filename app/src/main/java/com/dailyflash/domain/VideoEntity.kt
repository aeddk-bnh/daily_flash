package com.dailyflash.domain

import android.net.Uri
import java.time.LocalDate

/**
 * Domain entity representing a video.
 * This is the domain layer representation, independent of storage or UI concerns.
 *
 * @property id Unique identifier for the video
 * @property uri Content URI pointing to the video file
 * @property date Date when the video was captured
 * @property durationMs Duration of the video in milliseconds
 * @property thumbnailUri Optional URI for the video thumbnail (generated lazily)
 */
data class VideoEntity(
    val id: String,
    val uri: Uri,
    val date: LocalDate,
    val durationMs: Long,
    val thumbnailUri: Uri? = null
)
