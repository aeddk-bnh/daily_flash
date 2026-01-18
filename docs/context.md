# Project Context: DailyFlash

## Overview
DailyFlash is an Android application for capturing 1-second daily video moments and stitching them into a cohesive video journal.

## Current Status
- **Development Phase:** Implementation & UX Refinement.
- **Last Milestone:** Seperated Calendar and Gallery features, migrated storage to public MediaStore.
- **GitHub Repo:** `https://github.com/aeddk-bnh/daily_flash.git`

## Recent Changes (2026-01-18)
1.  **V6: Onion Skin & Gamification:**
    *   Implemented **Onion Skin (Ghost Overlay)**: Semi-transparent overlay of the last recorded frame for better alignment.
    *   Implemented **Streak Tracking**: Tracks consecutive days of recording.
    *   Updated `CameraScreen` with Streak display and Onion Skin toggle.
    *   Added `UpdateStreakUseCase` and updated `SettingsRepository`.
    *   Build passed and deployed to emulator (2026-01-18).
2.  **V4: Storage Location Display:**
    *   Added display of storage path (`Movies/DailyFlash`) in Settings.
    *   Implemented `GetStorageLocationUseCase`.
2.  **V5: Camera Lens Switching:**
    *   Added ability to toggle between Front/Back cameras.
    *   Updated `CameraService` to manage lens state and rebind lifecycle.
    *   Added Switch Camera button to `CameraScreen`.
3.  **Gallery Improvements (Fix):**
    *   Implemented Video Thumbnails using `coil-video` with `VideoFrameDecoder`.
    *   Fixed `Application` class conflict by using a local `ImageLoader` in `GalleryScreen`.
4.  **Previous Updates:**
    *   Gallery Grid Implementation.
    *   Storage Migration to public MediaStore.

## Architecture
- **Presentation:** Jetpack Compose, MVVM pattern.
- **Domain:** Use Cases (`CaptureVideo`, `GetCalendarData`, `ExportJournal`, `DeleteClip`, `GetAllVideos`, `GetStorageLocation`).
- **Data:** `VideoRepositoryImpl` with local caching.
- **Core/Infrastructure:**
    *   `ICameraService` (CameraX).
    *   `IMediaProcessor` (Media3 Transformer).
    *   `IStorageManager` (MediaStore/File System).

## Technical Stack
- **Languages:** Kotlin
- **Persistence:** Local Storage (Movies directory)
- **UI:** Jetpack Compose (Material 3)
- **Frameworks:** CameraX, Media3, Coil (including `coil-video` for thumbnails).

## Next Steps
- Monitor user feedback on storage location visibility.
- Potential future feature: Cloud backup integration.
