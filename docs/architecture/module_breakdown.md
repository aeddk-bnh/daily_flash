# Module Breakdown & Responsibilities

## 1. Module: Core (`core`)
*   **Objective:** Provide strict, low-level implementation of hardware and system services.
*   **Authority:** Owns all `Android.*` dependencies related to Camera, FileSystem, and Media3.
*   **Files:**
    *   `core/camera/CameraService.kt`
    *   `core/storage/StorageManager.kt`
    *   `core/storage/DateOrganizer.kt`
    *   `core/media/MediaProcessor.kt`

## 2. Module: Domain (`domain`)
*   **Objective:** Pure business logic and orchestration.
*   **Authority:** Defines the 'What', not the 'How'. Zero Android UI usage.
*   **Files:**
    *   `domain/IVideoRepository.kt` (Interface)
    *   `domain/DeleteClipUseCase.kt`
    *   `domain/ExportJournalUseCase.kt`
    *   `domain/SaveVideoUseCase.kt`
    *   `domain/GetMonthVideosUseCase.kt`

## 3. Module: Data (`data`)
*   **Objective:** Connect Domain abstraction to Core implementation.
*   **Authority:** Implements Repository interfaces using Core services.
*   **Files:**
    *   `data/VideoRepositoryImpl.kt`

## 4. Module: Presentation (`presentation`)
*   **Objective:** Render UI and handle user input.
*   **Authority:** Jetpack Compose screens, ViewModels, and State management.
*   **Sub-Modules:**
    *   `camera/` (CameraScreen, CameraViewModel)
    *   `calendar/` (CalendarScreen, CalendarViewModel)
    *   `export/` (ExportScreen, ExportViewModel)
    *   `components/` (Shared UI: DayCell, RecordButton)
