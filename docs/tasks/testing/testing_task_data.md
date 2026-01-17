# Task: Test Data Module

## ROLE: QA/Test Agent (Data)
**AUTHORITY**: You own `app/src/test/java/com/dailyflash/data/**`.

## RULES
1.  **Repository Pattern**: Test `VideoRepositoryImpl`.
2.  **Mocks**: Mock `IStorageManager`.

## CONTEXT
Validate the persistence and caching layer.

## TASK OBJECTIVE
Ensure the Repository correctly caches data and delegates I/O to StorageManager.

## REQUIRED OUTPUT
1.  `VideoRepositoryTest.kt`:
    *   Test `saveVideo`: Updates cache + calls Storage.
    *   Test `getVideosForMonth`: Returns cached data if available.

## DEFINITION OF DONE
*   All tests pass.
*   Verify Cache Invalidation logic.
