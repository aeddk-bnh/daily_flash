package com.dailyflash.domain

import android.net.Uri
import com.dailyflash.core.storage.VideoFile

import java.time.LocalDate

/**
 * Use case for capturing and saving a video.
 * Orchestrates saving video to repository and returning domain entity.
 */
class CaptureVideoUseCase(
    private val videoRepository: IVideoRepository
) {
    /**
     * Save a captured video and return domain entity.
     * @param videoUri URI of the captured video
     * @return Result with VideoEntity or error
     */
    suspend operator fun invoke(videoUri: Uri): Result<VideoEntity> {
        val date = LocalDate.now()
        return videoRepository.saveVideo(videoUri, date)
            .map { videoFile -> videoFile.toEntity() }
    }

    /**
     * Extension function to convert VideoFile to VideoEntity.
     */
    private fun VideoFile.toEntity() = VideoEntity(
        id = id,
        uri = uri,
        date = date,
        durationMs = durationMs,
        thumbnailUri = null // Generated lazily
    )
}
