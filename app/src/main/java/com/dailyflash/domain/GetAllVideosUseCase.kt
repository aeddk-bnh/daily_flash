package com.dailyflash.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to retrieve all recorded videos for gallery view.
 */
class GetAllVideosUseCase(
    private val videoRepository: IVideoRepository
) {
    operator fun invoke(): Flow<List<VideoEntity>> {
        return videoRepository.getAllVideos().map { videoFiles ->
            videoFiles.map { file ->
                VideoEntity(
                    id = file.id,
                    uri = file.uri,
                    date = file.date,
                    durationMs = file.durationMs,
                    thumbnailUri = file.thumbnailUri
                )
            }
        }
    }
}
