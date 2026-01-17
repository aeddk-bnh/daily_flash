package com.dailyflash.core.storage

import android.net.Uri
import java.time.LocalDate

/**
 * Interface for storage operations.
 * Handles file save/load with date-based organization.
 */
interface IStorageManager {
    /**
     * Save video data to storage organized by date.
     * @param data Video byte array
     * @param date Date for organization
     * @return URI of saved file
     */
    suspend fun saveVideo(data: ByteArray, date: LocalDate): Uri
    
    /**
     * Get all videos for a specific date.
     * @param date Target date
     * @return List of video files for that date
     */
    suspend fun getVideosByDate(date: LocalDate): List<VideoFile>
    
    /**
     * Get videos within a date range.
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return List of video files in range
     */
    suspend fun getVideosByRange(start: LocalDate, end: LocalDate): List<VideoFile>
    
    /**
     * Delete a video by URI.
     * @param uri Video URI to delete
     * @return true if deleted successfully
     */
    suspend fun deleteVideo(uri: Uri): Boolean

    /**
     * Export a video file to the public Gallery (MediaStore).
     * @param videoFile The temporary video file to export
     * @return The URI of the saved video in MediaStore
     */
    suspend fun exportVideoToGallery(videoFile: java.io.File): Uri
    
    /**
     * Get all videos stored in DailyFlash.
     * @return List of all video files
     */
    suspend fun getAllVideos(): List<VideoFile>

    /**
     * Create a temporary file in the app's cache directory.
     * @param prefix File name prefix
     * @param suffix File name suffix
     * @return Created temporary file
     */
    fun createTempFile(prefix: String, suffix: String): java.io.File

    /**
     * Delete all videos older than the specified date.
     * @param date Cutoff date (exclusive)
     * @return Number of videos deleted
     */
    suspend fun deleteVideosOlderThan(date: LocalDate): Int
    
    /**
     * Get a user-friendly description of where videos are stored.
     * @return String description of storage path (e.g. "Movies/DailyFlash")
     */
    fun getStorageLocationDescription(): String
}
