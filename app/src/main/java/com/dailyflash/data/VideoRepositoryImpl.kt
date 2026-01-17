package com.dailyflash.data

import com.dailyflash.domain.IVideoRepository

import android.content.ContentResolver
import android.net.Uri
import com.dailyflash.core.storage.IStorageManager
import com.dailyflash.core.storage.VideoFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth

/**
 * Implementation of video repository.
 * Handles caching and coordinates with storage.
 */
class VideoRepositoryImpl(
    private val storageManager: IStorageManager,
    private val contentResolver: ContentResolver? = null
) : IVideoRepository {
    
    // In-memory cache for quick access
    private val videoCache = mutableMapOf<LocalDate, VideoFile?>()
    
    // StateFlow to notify changes for reactive updates
    private val cacheVersion = MutableStateFlow(0L)
    
    override fun getVideosForMonth(yearMonth: YearMonth): Flow<Map<LocalDate, VideoFile?>> {
        return cacheVersion.map {
            // Calculate start and end dates for the month
            val start = yearMonth.atDay(1)
            val end = yearMonth.atEndOfMonth()
            
            // Query storage for videos in this range
            val videos = storageManager.getVideosByRange(start, end)
            
            // Build date map for all days in month
            val dateMap = mutableMapOf<LocalDate, VideoFile?>()
            var currentDate = start
            while (!currentDate.isAfter(end)) {
                // Find video for this date (take first if multiple exist)
                val videoForDate = videos.find { it.date == currentDate }
                dateMap[currentDate] = videoForDate
                
                // Update cache
                videoCache[currentDate] = videoForDate
                
                currentDate = currentDate.plusDays(1)
            }
            
            dateMap.toMap()
        }
    }
    
    override suspend fun getVideosInRange(start: LocalDate, end: LocalDate): List<VideoFile> {
        // Check cache first for performance
        val cachedVideos = mutableListOf<VideoFile>()
        var allCached = true
        var currentDate = start
        
        while (!currentDate.isAfter(end)) {
            if (videoCache.containsKey(currentDate)) {
                videoCache[currentDate]?.let { cachedVideos.add(it) }
            } else {
                allCached = false
                break
            }
            currentDate = currentDate.plusDays(1)
        }
        
        // If all dates in range are cached, return from cache
        if (allCached) {
            return cachedVideos.sortedBy { it.date }
        }
        
        // Cache miss - query storage
        val videos = storageManager.getVideosByRange(start, end)
        
        // Update cache with results
        videos.forEach { video ->
            videoCache[video.date] = video
        }
        
        // Return list ordered by date
        return videos.sortedBy { it.date }
    }
    
    override suspend fun saveVideo(uri: Uri, date: LocalDate): Result<VideoFile> {
        return try {
            // Read file data from URI
            val data = readBytesFromUri(uri)
                ?: return Result.failure(IllegalArgumentException("Cannot read video from URI"))
            
            // Save to storage via storage manager
            val savedUri = storageManager.saveVideo(data, date)
            
            // Create VideoFile from saved data
            val videoFile = VideoFile(
                id = generateVideoId(date),
                uri = savedUri,
                date = date,
                durationMs = 0L, // Duration would be extracted by storage layer
                sizeBytes = data.size.toLong(),
                createdAt = System.currentTimeMillis()
            )
            
            // Update cache with new entry
            videoCache[date] = videoFile
            
            // Notify observers of cache change
            cacheVersion.value++
            
            Result.success(videoFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteVideo(id: String): Result<Unit> {
        return try {
            // Find video in cache by ID
            val entry = videoCache.entries.find { it.value?.id == id }
            val videoFile = entry?.value
                ?: return Result.failure(NoSuchElementException("Video not found: $id"))
            
            // Delete from storage
            val deleted = storageManager.deleteVideo(videoFile.uri)
            if (!deleted) {
                return Result.failure(IllegalStateException("Failed to delete video from storage"))
            }
            
            // Remove from cache
            videoCache.remove(entry.key)
            
            // Notify observers of cache change
            cacheVersion.value++
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllVideos(): Flow<List<VideoFile>> {
        return cacheVersion.map {
            storageManager.getAllVideos()
        }
    }

    /**
     * Read bytes from a content URI.
     */
    private fun readBytesFromUri(uri: Uri): ByteArray? {
        return try {
            contentResolver?.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Generate a unique ID for a video based on date and timestamp.
     */
    private fun generateVideoId(date: LocalDate): String {
        return "video_${date}_${System.currentTimeMillis()}"
    }
    
    /**
     * Clear all cached entries. Useful for testing or memory pressure.
     */
    fun clearCache() {
        videoCache.clear()
        cacheVersion.value++
    }
    
    /**
     * Get current cache size for debugging.
     */
    fun getCacheSize(): Int = videoCache.size
}
