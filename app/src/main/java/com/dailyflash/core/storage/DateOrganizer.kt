package com.dailyflash.core.storage

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Utility for generating date-organized paths.
 */
object DateOrganizer {
    
    private val dateFormatter = DateTimeFormatter.ofPattern(StorageConfig.DATE_PATH_PATTERN)
    
    /**
     * Generate a file path for a given date.
     * Creates folder structure: baseDir/YYYY/MM/DD/
     * 
     * @param baseDir The base storage directory
     * @param date The date to generate path for
     * @return File object pointing to the date-specific directory
     */
    fun getPathForDate(baseDir: File, date: LocalDate): File {
        val datePath = date.format(dateFormatter)
        return File(baseDir, datePath)
    }
    
    /**
     * Generate a unique filename for a video captured at a given date.
     * Format: dailyflash_{timestamp}.mp4
     * 
     * @param date The date of capture (used for context, timestamp provides uniqueness)
     * @return Unique filename string
     */
    fun generateFilename(date: LocalDate): String {
        val timestamp = System.currentTimeMillis()
        return "${StorageConfig.FILENAME_PREFIX}${timestamp}${StorageConfig.VIDEO_EXTENSION}"
    }
    
    /**
     * Parse date from a date-organized path.
     * 
     * @param path Path in format YYYY/MM/DD
     * @return LocalDate if parsing succeeds, null otherwise
     */
    fun parseDateFromPath(path: String): LocalDate? {
        return try {
            // Regex to match YYYY/MM/DD logic (platform agnostic separator handled by looking for digits)
            val regex = Regex(".*(\\d{4})[\\\\/](\\d{2})[\\\\/](\\d{2}).*")
            val match = regex.find(path) ?: return null
            val (year, month, day) = match.destructured
            LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        } catch (e: Exception) {
            null
        }
    }
}
