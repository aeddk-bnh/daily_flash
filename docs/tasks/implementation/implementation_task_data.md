# Task: Implement Data Module

## ROLE: Implementation Agent (Data)
**AUTHORITY**: You own the `data` package. Connects Domain to Core.

## RULES
1.  **Mediation**: Do not contain business logic. Just map data.
2.  **Dependencies**: Depends on `IStorageManager` (Core).
3.  **Contracts**: Implement `IVideoRepository` (Domain).

## CONTEXT
You are implementing the Repository layer that manages data persistence and caching.
*   **Contracts**: `docs/architecture/api_contracts.md`

## TASK OBJECTIVE
Implement `VideoRepository` to bridge the gap between Domain UseCases and FileStorage.

## FILE PERMISSIONS
*   `app/src/main/java/com/dailyflash/data/**`
*   `app/src/test/java/com/dailyflash/data/**`

## REQUIRED OUTPUT
1.  `VideoRepositoryImpl.kt`: Implements `IVideoRepository`.
    *   Maintains In-Memory Cache (for performance).
    *   Delegates persistence to `StorageManager`.

## DEFINITION OF DONE
*   Code compiles.
*   Implements `IVideoRepository`.
*   Correctly maps `VideoFile` <-> `VideoEntity`.
