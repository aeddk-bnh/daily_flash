# Task: Test Presentation Module

## ROLE: QA/Test Agent (Presentation)
**AUTHORITY**: You own `app/src/test/java/com/dailyflash/presentation/**`.

## RULES
1.  **ViewModel Tests**: Test StateFlow updates.
2.  **Mocks**: Mock UseCases.
3.  **UI Tests (Optional)**: Compose rules.

## CONTEXT
Validate the UI State logic.

## TASK OBJECTIVE
Ensure ViewModels emit correct states (Loading -> Success/Error).

## REQUIRED OUTPUT
1.  `CameraViewModelTest.kt`: Capture flow state transitions.
2.  `CalendarViewModelTest.kt`: Date selection and list loading.
3.  `ExportViewModelTest.kt`: Progress updates and audio selection.

## DEFINITION OF DONE
*   All tests pass.
*   Verify error handling in ViewModels.
