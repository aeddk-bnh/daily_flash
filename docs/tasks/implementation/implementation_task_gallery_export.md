# Task: Gallery Refinement & Advanced Export

## Role
Implementation Agent

## Authority
You own the code within `com.dailyflash.presentation.gallery`, `com.dailyflash.presentation.export`, and media processing updates.

## Context
We are upgrading the Gallery with Swipe Navigation and creating a Pro-level Export engine.
- **Interfaces:** `IMediaProcessor` (Updated with overlays/fade).

## Task Objective
1.  **Gallery - Swipe View:** 
    - Create `GalleryDetailScreen` with a `HorizontalPager` to swipe between videos.
    - Connect data from `GalleryViewModel`.
    - Allow deletion from this view.
2.  **Advanced Export:**
    - Update `MediaProcessor` implementation to handle `textOverlay` (ffmpeg `drawtext` or Transformer equivalent) and `audioFade` (Volume effect).
    - Update `ExportScreen` UI to include toggles for "Show Date" and "Fade Audio".
3.  **Core Updates:**
    - Implement Auto-Cleanup mechanism in `StorageManager` (Optional feature).

## File Permissions
- `app/src/main/java/com/dailyflash/presentation/gallery/*`
- `app/src/main/java/com/dailyflash/presentation/export/*`
- `app/src/main/java/com/dailyflash/core/media/MediaProcessor.kt`
- `app/src/main/java/com/dailyflash/presentation/navigation/NavGraph.kt`

## Dependencies
- **Accompanist Pager:** `com.google.accompanist:accompanist-pager` (or Compose Foundation Pager).
- **Media3 Effect:** Ensure `androidx.media3:media3-effect` is available.

## TODO Map
- [ ] Implement `GalleryDetailScreen`.
- [ ] Update `stitchVideos` in `MediaProcessor.kt` to support Overlay/Fade.
- [ ] Add options to `ExportScreen`.

## Definition of Done
- User can swipe left/right in Gallery to see clips.
- Exported video has Date Text (if requested).
- Exported video has faded audio (if requested).
