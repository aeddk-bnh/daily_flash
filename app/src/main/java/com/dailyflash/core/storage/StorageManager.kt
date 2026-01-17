package com.dailyflash.core.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.dailyflash.core.logging.FlowLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

/**
 * Implementation of storage operations.
 * Uses app-specific external storage with date-based folder structure.
 */
class StorageManager(
    private val context: Context
) : IStorageManager {
    
    /**
     * Get the base directory for video storage.
     * Uses app-specific external storage (no permission required on Android 10+).
     */
    private fun getBaseDirectory(): File {
        val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?: throw IllegalStateException("External storage not available")
        val baseDir = File(moviesDir, StorageConfig.BASE_FOLDER_NAME)
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }
        return baseDir
    }
    
    /**
     * Save video data to storage organized by date.
     * Creates directory structure: Movies/DailyFlash/YYYY/MM/DD/dailyflash_{timestamp}.mp4
     */
    override suspend fun saveVideo(data: ByteArray, date: LocalDate): Uri = withContext(Dispatchers.IO) {
        FlowLogger.flow("StorageSaveStart", "date=$date, size=${data.size} bytes")
        val saveStartTime = System.currentTimeMillis()
        
        val baseDir = getBaseDirectory()
        val dateDir = DateOrganizer.getPathForDate(baseDir, date)
        
        // Create directories if needed
        if (!dateDir.exists()) {
            dateDir.mkdirs()
        }
        
        // Generate unique filename and create file
        val filename = DateOrganizer.generateFilename(date)
        val videoFile = File(dateDir, filename)
        
        FlowLogger.resource("ALLOC", "VideoFile", "path=${videoFile.path}")
        
        // Write bytes to file
        FileOutputStream(videoFile).use { outputStream ->
            outputStream.write(data)
        }
        
        val saveDuration = System.currentTimeMillis() - saveStartTime
        FlowLogger.timing("SaveVideo", saveDuration, "size=${data.size}, file=${filename}")
        FlowLogger.flow("StorageSaved", "path=${videoFile.name}")
        
        videoFile.toUri()
    }
    
    /**
     * Get all videos for a specific date.
     * Scans the date-specific directory for video files.
     */
    override suspend fun getVideosByDate(date: LocalDate): List<VideoFile> = withContext(Dispatchers.IO) {
        FlowLogger.flow("StorageRetrieve", "date=$date")
        
        val baseDir = getBaseDirectory()
        val dateDir = DateOrganizer.getPathForDate(baseDir, date)
        
        if (!dateDir.exists() || !dateDir.isDirectory) {
            FlowLogger.flow("StorageRetrieveEmpty", "date=$date, noDirectory=true")
            return@withContext emptyList()
        }
        
        val videos = dateDir.listFiles { file -> 
            file.isFile && file.extension == "mp4" 
        }?.map { file ->
            VideoFile.fromFile(file, date)
        }?.sortedBy { it.createdAt } ?: emptyList()
        
        FlowLogger.flow("StorageRetrieveComplete", "date=$date, count=${videos.size}")
        videos
    }
    
    /**
     * Get videos within a date range (inclusive).
     * Iterates through each date in the range and collects all videos.
     */
    override suspend fun getVideosByRange(start: LocalDate, end: LocalDate): List<VideoFile> = withContext(Dispatchers.IO) {
        require(!start.isAfter(end)) { "Start date must be before or equal to end date" }
        
        val videos = mutableListOf<VideoFile>()
        var currentDate = start
        
        while (!currentDate.isAfter(end)) {
            videos.addAll(getVideosByDate(currentDate))
            currentDate = currentDate.plusDays(1)
        }
        
        videos.sortedBy { it.createdAt }
    }
    
    /**
     * Delete a video by URI.
     * Returns true if the file was deleted successfully.
     */
    override suspend fun deleteVideo(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        FlowLogger.flow("StorageDelete", "uri=$uri")
        
        try {
            val file = File(uri.path ?: return@withContext false)
            if (file.exists()) {
                val deleted = file.delete()
                
                if (deleted) {
                    FlowLogger.resource("RELEASE", "VideoFile", "path=${file.path}")
                    // Clean up empty parent directories
                    cleanupEmptyDirectories(file.parentFile)
                    FlowLogger.flow("StorageDeleted", "file=${file.name}")
                } else {
                    FlowLogger.error("StorageDelete", "Failed to delete file")
                }
                
                deleted
            } else {
                FlowLogger.error("StorageDelete", "File does not exist")
                false
            }
        } catch (e: Exception) {
            FlowLogger.error("StorageDelete", e, "uri=$uri")
            false
        }
    }
    
    /**
     * Recursively clean up empty parent directories.
     * Stops at the base DailyFlash directory.
     */
    private fun cleanupEmptyDirectories(directory: File?) {
        if (directory == null) return
        if (directory.name == StorageConfig.BASE_FOLDER_NAME) return
        
        val files = directory.listFiles()
        if (files != null && files.isEmpty()) {
            directory.delete()
            cleanupEmptyDirectories(directory.parentFile)
        }
    }
    
    /**
     * Get total storage size used by DailyFlash.
     * @return Total bytes used
     */
    suspend fun getTotalStorageUsed(): Long = withContext(Dispatchers.IO) {
        getBaseDirectory().walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() }
    }
    
    /**
     * Get count of all videos stored.
     * @return Total number of video files
     */
    suspend fun getVideoCount(): Int = withContext(Dispatchers.IO) {
        getBaseDirectory().walkTopDown()
            .filter { it.isFile && it.extension == "mp4" }
            .count()
    }

    override suspend fun getAllVideos(): List<VideoFile> = withContext(Dispatchers.IO) {
        val baseDir = getBaseDirectory()
        baseDir.walkTopDown()
            .filter { it.isFile && it.extension == "mp4" }
            .mapNotNull { file ->
                val date = DateOrganizer.parseDateFromPath(file.parentFile?.path ?: "")
                    ?: return@mapNotNull null
                VideoFile.fromFile(file, date)
            }
            .sortedByDescending { it.createdAt }
            .toList()
    }

    /**
     * Export a video file to the public Gallery (MediaStore).
     * Handles version-specific logic for scoped storage.
     */
    override suspend fun exportVideoToGallery(videoFile: File): Uri = withContext(Dispatchers.IO) {
        val filename = "DailyFlash_${LocalDate.now()}_${System.currentTimeMillis()}.mp4"
        
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/DailyFlash")
                put(android.provider.MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.provider.MediaStore.Video.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, contentValues)
            ?: throw IllegalStateException("Failed to create MediaStore entry")
            
        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                java.io.FileInputStream(videoFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            // Clean up if copy fails
            resolver.delete(uri, null, null)
            throw e
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(android.provider.MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }
        
        uri
    }

    override fun createTempFile(prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, context.cacheDir)
    }
}
