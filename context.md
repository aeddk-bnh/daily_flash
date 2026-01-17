# Project Context: DailyFlash

## Overview
DailyFlash is an Android application for capturing 1-second daily video moments and stitching them into a cohesive video journal.

## Current Status
- **Development Phase:** Implementation & UX Refinement.
- **Last Milestone:** Seperated Calendar and Gallery features, migrated storage to public MediaStore.
- **GitHub Repo:** `https://github.com/aeddk-bnh/daily_flash.git`

## Recent Changes (2026-01-17)
1.  **Gallery Implementation:**
    *   Created `GalleryScreen` and `GalleryViewModel` for viewing all videos in a grid.
    *   Added `GetAllVideosUseCase` to the domain layer.
2.  **UI/UX Updates:**
    *   Updated `CameraScreen` with a dual-navigation layout: **Calendar** (Left) and **Gallery** (Right).
    *   Integrated `VideoPlayerDialog` for direct playback in the Gallery.
3.  **Storage Refactor (Scoped Storage & MediaStore):**
    *   Migrated storage from app-private directory to public `Movies/DailyFlash`.
    *   Flattened directory structure using date-encoded filenames (`dailyflash_YYYYMMDD_timestamp.mp4`).
    *   Updated `StorageManager` to query/write via `MediaStore` API.
    *   Updated `DateOrganizer` to parse dates from filenames.
4.  **Build & Deployment:**
    *   Added `androidx.compose.material:material-icons-extended` dependency.
    *   Verified build successful and app running on emulator/device.
    *   Pushed latest changes to GitHub.

## Architecture
- **Presentation:** Jetpack Compose, MVVM pattern.
- **Domain:** Use Cases (`CaptureVideo`, `GetCalendarData`, `ExportJournal`, `DeleteClip`, `GetAllVideos`).
- **Data:** `VideoRepositoryImpl` with local caching.
- **Core/Infrastructure:**
    *   `ICameraService` (CameraX).
    *   `IMediaProcessor` (Media3 Transformer).
    *   `IStorageManager` (MediaStore/File System).

## Technical Stack
- **Languages:** Kotlin
- **Persistence:** Local Storage (Movies directory)
- **UI:** Jetpack Compose (Material 3)
- **Frameworks:** CameraX, Media3, Coil (Thumbnails).

## Next Steps
- Verify video stitching (Export) with the new storage structure.
- Consider adding thumbnail generation for the Gallery view if MediaStore thumbnails are insufficient.
- Finalize UX polishing for the success/error states in Export.
