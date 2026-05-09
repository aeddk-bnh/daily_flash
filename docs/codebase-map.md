# Codebase Map: DailyFlash

## Top Level
- `app/` — Android application module
- `docs/` — product, architecture, and status documentation
- `build.gradle.kts`, `settings.gradle.kts` — Gradle configuration

## App Module Structure
- `app/src/main/java/com/dailyflash/presentation/` — Compose screens, navigation, and ViewModels
- `app/src/main/java/com/dailyflash/domain/` — use cases, entities, and interfaces
- `app/src/main/java/com/dailyflash/data/` — repository and adapter implementations
- `app/src/main/java/com/dailyflash/core/` — Android-facing infrastructure (camera, storage, media, reminders, settings, logging)
- `app/src/main/res/` — Android resources
- `app/src/test/java/` — unit and Robolectric tests
- `app/src/androidTest/java/` — instrumented tests

## Key Entry Points
- `app/src/main/java/com/dailyflash/DailyFlashApplication.kt` — application startup and `AppContainer` initialization
- `app/src/main/java/com/dailyflash/presentation/MainActivity.kt` — permission gating, activity-scoped camera service, screen ViewModel ownership, app startup
- `app/src/main/java/com/dailyflash/presentation/navigation/NavGraph.kt` — app navigation graph and screen composition
- `app/src/main/AndroidManifest.xml` — permissions, application class, boot receiver, FileProvider, launcher activity

## Feature Map
### Capture
- Screen: `presentation/camera/CameraScreen.kt`
- ViewModel: `presentation/camera/CameraViewModel.kt`
- Service: `core/camera/CameraService.kt`
- Use case: `domain/CaptureVideoUseCase.kt`

### Calendar
- Screen: `presentation/calendar/CalendarScreen.kt`
- ViewModel: `presentation/calendar/CalendarViewModel.kt`
- Use case: `domain/GetCalendarDataUseCase.kt`

### Gallery
- Screen: `presentation/gallery/GalleryScreen.kt`
- Detail: `presentation/gallery/GalleryDetailScreen.kt`
- ViewModel: `presentation/gallery/GalleryViewModel.kt`
- Use case: `domain/GetAllVideosUseCase.kt`

### Export
- Screen: `presentation/export/ExportScreen.kt`
- ViewModel: `presentation/export/ExportViewModel.kt`
- Use case: `domain/ExportJournalUseCase.kt`
- Media processing: `core/media/MediaProcessor.kt`

### Settings / Reminders
- Screen: `presentation/settings/SettingsScreen.kt`
- ViewModel: `presentation/settings/SettingsViewModel.kt`
- DataStore: `core/settings/SettingsDataStore.kt`
- Reminder scheduler: `data/reminders/WorkManagerReminderScheduler.kt`
- Reminder worker: `core/reminders/ReminderWorker.kt`
- Boot restore: `core/reminders/BootCompletedReceiver.kt`

## Data Flow Overview
- UI events go from Screen -> ViewModel -> UseCase -> Repository -> Core service/storage layer.
- Video capture records to a temp file through the activity-scoped `CameraService`, then repository persists to MediaStore `Movies/DailyFlash`.
- Export loads clips by date range, stitches them with Media3 Transformer, then saves output to MediaStore `Movies/DailyFlash/Exports`.
- Settings are stored in DataStore and used by reminder scheduling and cleanup workers.
- Shared non-UI dependencies are provided through `AppContainer`.

## Known Gaps To Revisit
- Repo still uses manual DI via `AppContainer` rather than a framework-backed graph.
- Repository caching is still in-memory and may diverge from cold-start behavior.
- Export success UI still supports playback only; there is no share action yet.
