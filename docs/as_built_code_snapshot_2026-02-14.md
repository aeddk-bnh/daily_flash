# DailyFlash As-Built Code Snapshot

Date: 2026-02-14
Scope: Source code under `app/src/main` and current Gradle setup.

## 1. Project Summary

DailyFlash is an Android app for capturing short daily videos, browsing them in calendar/gallery views, and exporting a stitched journal video.  
Architecture follows a layered structure inside one app module:

- `presentation`: Compose UI + ViewModels
- `domain`: use cases + repository interfaces
- `data`: repository implementations and adapters
- `core`: CameraX, Media3, MediaStore, DataStore, Workers

## 2. Build And Runtime Baseline

- Module: single module `:app`
- Android Gradle Plugin: `8.4.0`
- Kotlin Android plugin: `1.9.23`
- Compile SDK: `34`
- Min SDK: `24`
- Target SDK: `34`
- UI: Jetpack Compose Material 3
- Navigation: Navigation Compose
- Camera: CameraX
- Export: Media3 Transformer
- Storage: MediaStore (`Movies/DailyFlash*`)
- Settings: DataStore Preferences
- Background jobs: WorkManager

## 3. App Entry And Wiring

Entry activity:

- `app/src/main/java/com/dailyflash/presentation/MainActivity.kt`

Main responsibilities:

- requests runtime permissions (camera, audio, notifications for API 33+)
- creates service/repository/use case instances via manual DI
- schedules cleanup worker
- renders `NavGraph` after permission grant

Navigation graph:

- `app/src/main/java/com/dailyflash/presentation/navigation/NavGraph.kt`
- routes: `camera`, `settings`, `gallery`, `gallery_detail/{initialIndex}`, `calendar`, `export`

## 4. Feature Flows (Current Code)

### 4.1 Capture Flow

Primary files:

- `presentation/camera/CameraScreen.kt`
- `presentation/camera/CameraViewModel.kt`
- `core/camera/CameraService.kt`
- `core/storage/StorageManager.kt`
- `domain/CaptureVideoUseCase.kt`
- `data/VideoRepositoryImpl.kt`

Current flow in code:

1. UI triggers `CameraViewModel.startRecording()`
2. ViewModel calls `cameraService.recordClip(1000)`
3. `CameraService` records with CameraX to a temporary file and returns temp URI
4. ViewModel calls `CaptureVideoUseCase(uri)` and repository persists to MediaStore via `StorageManager.saveVideo(...)`
5. ViewModel updates streak via `UpdateStreakUseCase`

Camera extras implemented:

- torch toggle
- front/back switch camera
- onion skin overlay using latest clip thumbnail
- streak display on camera screen

### 4.2 Calendar Flow

Primary files:

- `presentation/calendar/CalendarScreen.kt`
- `presentation/calendar/CalendarViewModel.kt`
- `domain/GetCalendarDataUseCase.kt`

Behavior:

- monthly grid with video thumbnails on days that have clips
- tap day to play clip
- long press day to delete clip
- month navigation prev/next

### 4.3 Gallery Flow

Primary files:

- `presentation/gallery/GalleryScreen.kt`
- `presentation/gallery/GalleryDetailScreen.kt`
- `presentation/gallery/GalleryViewModel.kt`

Behavior:

- grid of all videos
- open detail view by index
- horizontal swipe pager in detail screen
- delete action from detail top bar

### 4.4 Export Flow

Primary files:

- `presentation/export/ExportScreen.kt`
- `presentation/export/ExportViewModel.kt`
- `domain/ExportJournalUseCase.kt`
- `core/media/MediaProcessor.kt`

Behavior:

1. user picks date range and optional audio
2. use case loads clips from repository
3. media processor stitches clips via Media3
4. result is copied to gallery via `StorageManager.exportVideoToGallery(...)`
5. UI shows progress and success/failure states

Export options currently wired:

- optional background audio track
- optional text overlay toggle (`Show Date Overlay`) with range-based text label

### 4.5 Settings, Reminder, Cleanup

Primary files:

- `presentation/settings/SettingsScreen.kt`
- `presentation/settings/SettingsViewModel.kt`
- `data/settings/SettingsRepositoryImpl.kt`
- `core/settings/SettingsDataStore.kt`
- `data/reminders/WorkManagerReminderScheduler.kt`
- `core/storage/CleanupWorker.kt`

Behavior:

- reminder enable/disable + time picker
- auto-cleanup toggle + keep-days value
- storage location display (`Movies/DailyFlash`)
- streak fields persisted in DataStore
- reminder schedule restored on boot (`BootCompletedReceiver`)
- cleanup worker runs daily and deletes old videos when enabled

## 5. Data And Storage Model

Core video model:

- `core/storage/VideoFile.kt`

Fields:

- `id`, `uri`, `date`, `durationMs`, `sizeBytes`, `createdAt`, `thumbnailUri`

Domain video model:

- `domain/VideoEntity.kt`

Storage strategy:

- videos are stored through MediaStore
- query filters by filename prefix `dailyflash_`
- date is derived from filename pattern where possible

## 6. Notification/Reminder Components Present

Single reminder path in active code:

- `domain.notification.INotificationManager`
- `data.notification.NotificationManagerImpl`
- `data.reminders.WorkManagerReminderScheduler`
- `core.reminders.ReminderWorker`
- `core.reminders.BootCompletedReceiver`

## 7. Current Verification Snapshot

Command result snapshot from this repo state:

- `assembleDebug -x test`: success
- `testDebugUnitTest`: success
- emulator app launch (`adb am start`): success
- `connectedDebugAndroidTest`: success (10/10 tests on emulator)

This means app compile, unit tests, and connected Android tests are healthy at snapshot time.

## 8. Immediate Technical Risks Observed

- device-level validation still recommended for reminder trigger timing after reboot
- device-level validation still recommended for exported video overlay rendering across vendors

For actionable tracking and fix progress, use:

- `docs/fix_progress_tracker.md`
