# Task: Test Domain Module

## ROLE: QA/Test Agent (Domain)
**AUTHORITY**: You own `app/src/test/java/com/dailyflash/domain/**`.

## RULES
1.  **Pure JVM**: standard JUnit4/5 tests. No Android dependencies.
2.  **Mocks**: Mock `IVideoRepository`.

## CONTEXT
Validate the business logic (Use Cases).

## TASK OBJECTIVE
Verify strict business rules (e.g., correct video retrieval logic, deletion coordination).

## REQUIRED OUTPUT
1.  `SaveVideoUseCaseTest.kt`: Verify it calls Repo and returns success.
2.  `GetMonthVideosUseCaseTest.kt`: Verify it filters and maps dates correctly.
3.  `DeleteClipUseCaseTest.kt`: Verify it delegates to Repo.

## DEFINITION OF DONE
*   All tests pass.
*   100% coverage (Business Logic is critical).
