package com.dailyflash.core.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
     * Save video data to public storage (Movies/DailyFlash).
     * Uses MediaStore for better accessibility and Scoped Storage compliance.
     */
    override suspend fun saveVideo(data: ByteArray, date: LocalDate): Uri = withContext(Dispatchers.IO) {
        FlowLogger.flow("StorageSaveStart", "date=$date, size=${data.size} bytes")
        val saveStartTime = System.currentTimeMillis()
        
        val filename = DateOrganizer.generateFilename(date)
        val relativePath = "${Environment.DIRECTORY_MOVIES}/${StorageConfig.BASE_FOLDER_NAME}"
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, contentValues)
            ?: throw IllegalStateException("Failed to create MediaStore entry")

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            
            val saveDuration = System.currentTimeMillis() - saveStartTime
            FlowLogger.timing("SaveVideo", saveDuration, "size=${data.size}, file=${filename}")
            FlowLogger.flow("StorageSaved", "uri=$uri")
            
            uri
        } catch (e: Exception) {
            // Clean up if write fails
            resolver.delete(uri, null, null)
            throw e
        }
    }
    
    /**
     * Get all videos for a specific date.
     * Filters result from getAllVideos.
     */
    override suspend fun getVideosByDate(date: LocalDate): List<VideoFile> = withContext(Dispatchers.IO) {
        getAllVideos().filter { it.date == date }
    }
    
    /**
     * Get videos within a date range (inclusive).
     */
    override suspend fun getVideosByRange(start: LocalDate, end: LocalDate): List<VideoFile> = withContext(Dispatchers.IO) {
        getAllVideos().filter { 
            !it.date.isBefore(start) && !it.date.isAfter(end)
        }
    }
    
    /**
     * Delete a video by URI.
     * Returns true if the file was deleted successfully.
     */
    override suspend fun deleteVideo(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        FlowLogger.flow("StorageDelete", "uri=$uri")
        try {
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (e: Exception) {
            FlowLogger.error("StorageDelete", e, "uri=$uri")
            false
        }
    }
    
    override fun createTempFile(prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, context.cacheDir)
    }

    override suspend fun getAllVideos(): List<VideoFile> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoFile>()
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA
        )
        
        // Query for videos with display name starting with our prefix
        val selection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("${StorageConfig.FILENAME_PREFIX}%")
        
        val sortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        
        context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateTaken = cursor.getLong(dateColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                
                val uri = android.content.ContentUris.withAppendedId(collection, id)
                
                // Try to parse date from filename first (more reliable for our naming convention)
                val date = DateOrganizer.parseDateFromPath(name)
                    ?: LocalDate.ofEpochDay(dateTaken / (24 * 60 * 60 * 1000))
                
                if (date != null) {
                    videos.add(
                        VideoFile(
                            id = name.substringBeforeLast("."), 
                            uri = uri,
                            date = date,
                            durationMs = duration,
                            sizeBytes = size,
                            createdAt = dateTaken,
                            thumbnailUri = uri 
                        )
                    )
                }
            }
        }
        
        videos
    }

    override suspend fun exportVideoToGallery(videoFile: File): Uri = withContext(Dispatchers.IO) {
        val filename = "DailyFlash_Export_${System.currentTimeMillis()}.mp4"
        val relativePath = "${Environment.DIRECTORY_MOVIES}/${StorageConfig.BASE_FOLDER_NAME}/${StorageConfig.EXPORT_FOLDER_NAME}"
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, filename)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, contentValues)
            ?: throw IllegalStateException("Failed to create MediaStore entry")
            
        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                java.io.FileInputStream(videoFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    }
}
