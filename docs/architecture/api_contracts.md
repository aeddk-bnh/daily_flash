# API Contracts (Module Interfaces)

## 1. Capture Module (`camera`)
**Responsibility:** Handle CameraX lifecycle and video recording.

### `ICameraService`
```kotlin
interface ICameraService {
    suspend fun initialize(lifecycleOwner: LifecycleOwner)
    suspend fun startRecording(outputFile: File): Result<Uri>
    suspend fun stopRecording()
    fun isRecording(): Boolean
}
```

## 2. Data Module (`data` / `storage`)
**Responsibility:** Persistence and retrieval of video files.

### `IStorageManager`
```kotlin
interface IStorageManager {
    suspend fun saveVideo(data: ByteArray, date: LocalDate): Uri
    suspend fun getVideosByDate(date: LocalDate): List<VideoFile>
    suspend fun getVideosByRange(start: LocalDate, end: LocalDate): List<VideoFile>
    suspend fun deleteVideo(uri: Uri): Boolean
    suspend fun exportVideoToGallery(videoFile: File): Uri
    fun createTempFile(prefix: String, suffix: String): File
    suspend fun getTotalStorageUsed(): Long
    suspend fun getVideoCount(): Int
}
```

### `IVideoRepository`
**Responsibility:** Domain-level abstraction over Storage.
```kotlin
interface IVideoRepository {
    fun getVideosForMonth(yearMonth: YearMonth): Flow<Map<LocalDate, VideoFile?>>
    suspend fun getVideosInRange(start: LocalDate, end: LocalDate): List<VideoFile>
    suspend fun saveVideo(uri: Uri, date: LocalDate): Result<VideoFile>
    suspend fun deleteVideo(id: String): Result<Unit>
}
```

## 3. Export Module (`media`)
**Responsibility:** Stitching and audio overlay.

### `IMediaProcessor`
```kotlin
interface IMediaProcessor {
    suspend fun stitchVideos(
        videoFiles: List<File>,
        outputFile: File,
        audioTrack: Uri? = null,
        onProgress: (Float) -> Unit
    ): Result<File>
    
    fun cancelProcessing()
}
```
