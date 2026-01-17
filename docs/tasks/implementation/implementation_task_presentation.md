# Task: Implement Presentation Module

## ROLE: Implementation Agent (Presentation)
**AUTHORITY**: You own the `presentation` package. Jetpack Compose UI.

## RULES
1.  **MVVM**: strict ViewModel <-> Screen separation.
2.  **State**: ViewModels expose `StateFlow<UiState>`.
3.  **No Logic**: Do not perform business logic in ViewModels. Call UseCases.

## CONTEXT
You are implementing the user interface for the users to interact with the system.
*   **Data Models**: `docs/architecture/data_models.md` (UiStates)

## TASK OBJECTIVE
Implement the screens: Camera, Calendar, Export.

## FILE PERMISSIONS
*   `app/src/main/java/com/dailyflash/presentation/**`
*   `app/src/test/java/com/dailyflash/presentation/**`

## REQUIRED OUTPUT
1.  `CameraScreen` / `CameraViewModel`: Real-time capture UI.
2.  `CalendarScreen` / `CalendarViewModel`: Monthly grid view.
3.  `ExportScreen` / `ExportViewModel`: Date selection and export progress.

## DEFINITION OF DONE
*   Code compiles.
*   Preview annotations working.
*   ViewModels correctly call UseCases.
