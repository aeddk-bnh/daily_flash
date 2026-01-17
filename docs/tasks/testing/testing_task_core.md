# Task: Test Core Module

## ROLE: QA/Test Agent (Core)
**AUTHORITY**: You own `app/src/test/java/com/dailyflash/core/**` and `app/src/androidTest/java/com/dailyflash/core/**`.

## RULES
1.  **Isolation**: Use Mockito to mock Android/System dependencies in Unit Tests.
2.  **Integration**: Use Instrumented tests for actual File/Camera checks.

## CONTEXT
Validate the low-level infrastructure wrappers.

## TASK OBJECTIVE
Ensure Camera, Storage, and MediaProcessor behave correctly under real and mock conditions.

## REQUIRED OUTPUT
1.  `CameraServiceTest.kt`: Verify lifecycle binding and state transitions.
2.  `StorageManagerTest.kt`: Verify file creation, deletion, and path generation (Robolectric).
3.  `MediaProcessorTest.kt`: Verify transformation configuration (Mock Transformer).

## DEFINITION OF DONE
*   All tests pass.
*   > 80% coverage on Core classes.
