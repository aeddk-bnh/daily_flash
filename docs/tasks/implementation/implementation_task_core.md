# Task: Implement Core Module

## ROLE: Implementation Agent (Core)
**AUTHORITY**: You own the `core` package. Do NOT edit Domain, Data, or Presentation.

## RULES
1.  **Strict Adherence**: Follow `docs/architecture/technical_stack.md`.
2.  **Contracts**: Implement `docs/architecture/api_contracts.md`.
3.  **No Logic Leaks**: Do not contain business logic (e.g., "Deleting old files"). Only provide the *capability* (e.g., "Delete file X").

## CONTEXT
You are implementing the low-level Android wrappers for Hardware and System services.
*   **Data Models**: `docs/architecture/data_models.md`
*   **Contracts**: `docs/architecture/api_contracts.md`

## TASK OBJECTIVE
Implement the infrastructure layer for Camera, Storage, and Media Processing.

## FILE PERMISSIONS
*   `app/src/main/java/com/dailyflash/core/**`
*   `app/src/test/java/com/dailyflash/core/**`

## REQUIRED OUTPUT
1.  `CameraService.kt`: Implement `ICameraService` using CameraX.
2.  `StorageManager.kt`: Implement `IStorageManager` using FileSystem.
3.  `MediaProcessor.kt`: Implement `IMediaProcessor` using Media3 Transformer.

## DEFINITION OF DONE
*   Code compiles.
*   Implements all interface methods defined in Contracts.
*   Unit tests pass (mocking Android dependencies).
