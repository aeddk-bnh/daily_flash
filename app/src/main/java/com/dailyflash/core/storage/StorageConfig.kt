package com.dailyflash.core.storage

/**
 * Storage configuration constants.
 */
object StorageConfig {
    const val VIDEO_EXTENSION = ".mp4"
    const val VIDEO_MIME_TYPE = "video/mp4"
    const val BASE_FOLDER_NAME = "DailyFlash"
    const val EXPORT_FOLDER_NAME = "Exports"
    
    /** Prefix for generated video filenames */
    const val FILENAME_PREFIX = "dailyflash_"
    
    /** Date format pattern for folder structure */
    const val DATE_PATH_PATTERN = "yyyy/MM/dd"
    
    /** FileProvider authority (must match AndroidManifest.xml) */
    const val FILE_PROVIDER_AUTHORITY = "com.dailyflash.fileprovider"
}
