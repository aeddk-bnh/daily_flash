# Task: Implement Domain Module

## ROLE: Implementation Agent (Domain)
**AUTHORITY**: You own the `domain` package. Pure Kotlin only. NO Android dependencies (except basic Uri/Parcelable if absolutely needed, prefer String).

## RULES
1.  **Pure Logic**: No Context, no Views, no CameraX.
2.  **Contracts**: Define `IVideoRepository` interface.
3.  **Use Cases**: One class per action (Single Responsibility).

## CONTEXT
You are implementing the business logic and orchestration layer.
*   **Data Models**: `docs/architecture/data_models.md`

## TASK OBJECTIVE
Implement the strict business rules for DailyFlash.

## FILE PERMISSIONS
*   `app/src/main/java/com/dailyflash/domain/**`
*   `app/src/test/java/com/dailyflash/domain/**`

## REQUIRED OUTPUT
1.  `IVideoRepository.kt`: Interface definition (Source of Truth).
2.  `SaveVideoUseCase.kt`: Orchestrate saving + caching.
3.  `GetMonthVideosUseCase.kt`: Retrieve organized video list.
4.  `DeleteClipUseCase.kt`: Handle deletion rules.
5.  `ExportJournalUseCase.kt`: Manage export logic.

## DEFINITION OF DONE
*   Code compiles.
*   Pure Kotlin (check imports).
*   100% Unit Test Coverage (Domain logic is critical).
