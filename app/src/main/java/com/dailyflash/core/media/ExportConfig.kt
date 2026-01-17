package com.dailyflash.core.media

import androidx.media3.common.MimeTypes

/**
 * Export configuration constants for video processing.
 * These settings ensure optimal quality and performance for video journal exports.
 */
object ExportConfig {
    // Video codec settings
    const val OUTPUT_VIDEO_MIME_TYPE = MimeTypes.VIDEO_H264
    const val OUTPUT_BITRATE = 8_000_000 // 8 Mbps - good quality for 1080p
    const val OUTPUT_FRAME_RATE = 30
    const val OUTPUT_RESOLUTION_WIDTH = 1920
    const val OUTPUT_RESOLUTION_HEIGHT = 1080
    
    // Audio settings
    const val OUTPUT_AUDIO_MIME_TYPE = MimeTypes.AUDIO_AAC
    const val AUDIO_BITRATE = 128_000 // 128 kbps
    const val AUDIO_SAMPLE_RATE = 44100
    const val AUDIO_CHANNEL_COUNT = 2
    
    // Audio mixing modes
    enum class AudioMixMode {
        MUTE_ORIGINAL,      // Mute original clip audio, use only background track
        DUCK_ORIGINAL,      // Lower original audio volume when background plays
        MIX_EQUAL           // Mix both at equal volume
    }
    
    // Default audio mixing mode
    val DEFAULT_AUDIO_MIX_MODE = AudioMixMode.MUTE_ORIGINAL
    
    // Background audio volume (0.0 to 1.0)
    const val BACKGROUND_AUDIO_VOLUME = 0.8f
    const val DUCKED_ORIGINAL_VOLUME = 0.3f
    
    // Progress polling interval
    const val PROGRESS_POLL_INTERVAL_MS = 100L
    
    // Export file settings
    const val EXPORT_FILE_EXTENSION = ".mp4"
    const val EXPORT_FOLDER_NAME = "exports"
    
    // Performance settings
    const val MAX_CONCURRENT_EXPORTS = 1
    const val THERMAL_THROTTLE_CHECK_INTERVAL_MS = 5000L
}
