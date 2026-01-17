package com.dailyflash.domain



/**
 * Use case for deleting a video clip.
 * Delegates deletion to the repository layer.
 */
class DeleteClipUseCase(
    private val videoRepository: IVideoRepository
) {
    /**
     * Delete a video clip by ID.
     * @param videoId Video identifier
     * @return Result indicating success or error
     */
    suspend operator fun invoke(videoId: String): Result<Unit> {
        return videoRepository.deleteVideo(videoId)
    }
}
