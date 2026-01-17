# Data Models & Shared Types

## 1. Domain Entities (Core Logic)
These entities represent the core business objects, independent of framework.

### `VideoEntity`
Represents a single daily capture.
```kotlin
data class VideoEntity(
    val id: String,           // Unique Identifier
    val date: LocalDate,      // The day this video belongs to
    val durationMs: Long,     // Duration (Approx 1000ms)
    val uri: String,          // Persistence URI (String format)
    val sizeBytes: Long       // File size
)
```

## 2. Data Transfer Objects (DTOs)
Used for storage and external communication.

### `VideoFileDTO` (Legacy: `VideoFile`)
Maps to the File System structure.
```kotlin
data class VideoFile(
    val id: String,
    val uri: Uri,             // Android Uri
    val date: LocalDate,
    val durationMs: Long,     // Duration
    val sizeBytes: Long,      // File Size
    val createdAt: Long,      // Timestamp
    val thumbnailUri: Uri?    // Cached Thumbnail
)
```

## 3. UI States (Presentation)
State representations for Jetpack Compose.

### `CalendarDayState`
```kotlin
data class CalendarDayState(
    val date: LocalDate,
    val hasVideo: Boolean,
    val isSelected: Boolean,
    val videoThumbnail: String? // Uri string
)
```

### `ExportState`
```kotlin
sealed class ExportState {
    object Idle : ExportState()
    data class Processing(val progress: Float) : ExportState()
    data class Success(val outputUri: Uri) : ExportState()
    data class Error(val message: String) : ExportState()
}
```

## 4. User Preferences
For engagement and settings features.

### `UserSettings`
```kotlin
data class UserSettings(
    val dailyReminderTime: LocalTime? = LocalTime.of(20, 0), // Default 8 PM
    val isReminderEnabled: Boolean = false,
    val autoCleanupEnabled: Boolean = false,
    val currentStreak: Int = 0,
    val lastRecordingDate: LocalDate? = null
)
```
