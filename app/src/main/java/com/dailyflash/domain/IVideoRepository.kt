package com.dailyflash.domain

import android.net.Uri
import com.dailyflash.core.storage.VideoFile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

/**
 * Interface for video data operations.
 * Coordinates between domain and core services.
 */

interface IVideoRepository {
    /**
     * Get videos organized by month for calendar display.
     * @param yearMonth Target month
     * @return Flow of date-to-video mapping
     */
    fun getVideosForMonth(yearMonth: YearMonth): Flow<Map<LocalDate, VideoFile?>>
    
    /**
     * Get videos in a date range for export.
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return List of video files
     */
    suspend fun getVideosInRange(start: LocalDate, end: LocalDate): List<VideoFile>
    
    /**
     * Save a newly captured video.
     * @param uri Video URI from camera
     * @param date Date of capture
     * @return Result with VideoFile or error
     */
    suspend fun saveVideo(uri: Uri, date: LocalDate): Result<VideoFile>
    
    /**
     * Delete a video by ID.
     * @param id Video identifier
     * @return Result indicating success or error
     */
    suspend fun deleteVideo(id: String): Result<Unit>

    /**
     * Get all videos for the gallery view.
     * @return Flow of video list
     */
    fun getAllVideos(): Flow<List<VideoFile>>
}
