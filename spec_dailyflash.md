# SPECIFICATION: DailyFlash (As-Built)

## 1. Document Control
*   **Project Name:** DailyFlash
*   **Version:** 1.0 (Release Candidate)
*   **Status:** ✅ **AS-BUILT** (Verified Implementation)
*   **Date:** 2026-01-17

---

## 2. Purpose & Scope
### 2.1 Goal
Provide a minimal Android application for capturing 1-second daily video moments and stitching them into a cohesive video journal, emphasizing privacy, speed, and simplicity.

### 2.2 In-Scope
*   **Video Capture:** 1-second fixed duration recording.
*   **Calendar Organization:** Visual grid of daily memories.
*   **Journal Export:** On-device stitching of clips into a single MP4.
*   **Audio Overlay:** Option to add background music during export.
*   **Management:** Review and deletion of specific daily clips.

### 2.3 Out-of-Scope
*   Cloud Backup / Sync.
*   Social Sharing integration (beyond system Share Sheet).
*   Complex Video Editing (trimming, filters, transitions).

---

## 3. Functional Requirements

### 3.1 Video Capture Module
*   **REQ-CAP-01:** System **MUST** utilize `CameraX` for video capture.
*   **REQ-CAP-02:** Recording duration **MUST** be strictly limited to ~1000ms (1 second).
*   **REQ-CAP-03:** Capture interface **MUST** require single-tap interaction.
*   **REQ-CAP-04:** System **MUST** support both Hardware Encoders (Real Devices) and Software Encoders (Emulators via `Quality.LOWEST` fallback).

### 3.2 Media Organization (Calendar)
*   **REQ-ORG-01:** System **MUST** display a monthly calendar view.
*   **REQ-ORG-02:** Days with recorded content **MUST** show a thumbnail indicator.
*   **REQ-ORG-03:** System **MUST** allow playback of the daily clip upon tapping a date.
*   **REQ-ORG-04:** System **MUST** allow deletion of a clip via long-press interaction.

### 3.3 Export Engine
*   **REQ-EXP-01:** System **MUST** use `Media3 Transformer` for video stitching.
*   **REQ-EXP-02:** Export process **MUST** run locally without internet access.
*   **REQ-EXP-03:** User **MAY** select a custom audio file (MP3/AAC) to overlay on the final video.
*   **REQ-EXP-04:** Final video **MUST** be saved to the public Gallery (`MediaStore`) for external access.

---

### 3.4 Gallery & Interaction
*   **REQ-GAL-01:** System **MUST** provide a grid view of all recorded videos.
*   **REQ-GAL-02:** System **MUST** support "Swipe Navigation" in detail view to move between daily clips (Left/Right).
*   **REQ-GAL-03:** System **MUST** allow deletion of clips directly from the detail view.

### 3.5 Engagement & Notifications
*   **REQ-ENG-01:** System **SHOULD** send a local push notification at a user-defined time (default 8:00 PM) to remind them to record.
*   **REQ-ENG-02:** System **MUST** track and display "Streaks" (consecutive days recorded) on the main Camera screen.

### 3.6 Advanced Features
*   **REQ-ADV-01 (Preview):** System **MUST** show an "Instant Preview" (PIP or Overlay) immediately after recording to validate the shot.
*   **REQ-ADV-02 (Export):** Export process **SHOULD** support text overlays (Date) on individual clips.
*   **REQ-ADV-03 (Export):** Export process **SHOULD** apply fade-in/out effects to background audio.
*   **REQ-ADV-04 (Storage):** System **SHOULD** offer an option to "Auto-Cleanup" raw clips after successful export of a compilation.

---

## 4. Data & Workflows

### 4.1 Data Entities
*   **VideoFile:**
    *   `id`: String (Unique)
    *   `uri`: Uri (App-Specific Storage)
    *   `date`: LocalDate
    *   `sizeBytes`: Long
*   **UserSettings:**
    *   `dailyReminderTime`: LocalTime
    *   `autoCleanupEnabled`: Boolean
    *   `currentStreak`: Int

### 4.2 Core Workflows
1.  **Capture Flow:** User Taps Record -> CameraService records 1s -> Saves to public MediaStore -> **Shows Instant Preview**.
2.  **Export Flow:** User Selects Range -> Configures Options (Overlay/Audio) -> Transformer stitches clips -> Saves to `Movies/DailyFlash/Exports` -> **(Optional) Auto-deletes source clips**.

---

## 5. Technical Constraints & Decisions

### 5.1 Platform
*   **OS:** Android 7.0 (API 24) minimum.
*   **Language:** Kotlin.
*   **UI:** Jetpack Compose (Material 3).

### 5.2 Storage & Privacy
*   **Public Storage (MediaStore):** Used for clips to ensure user accessibility and visibility in standard Gallery apps.
*   **Local Only:** No `INTERNET` permission in Manifest.
*   **Scoped Storage:** Compliant with Android Q+ requirements using `RELATIVE_PATH` in `Movies/DailyFlash`.

### 5.3 Compatibility
*   **Emulator Support:** Explicit logic detects emulators and downgrades recording quality to 480p to bypass encoder hardware limitations.

---

## 6. Traceability Appendix*   **Capture Logic:** `CameraService.kt`
*   **Storage Logic:** `StorageManager.kt`
*   **Export Logic:** `MediaProcessor.kt`, `ExportJournalUseCase.kt`
*   **UI Layer:** `CameraScreen.kt`, `CalendarScreen.kt`, `ExportScreen.kt`
