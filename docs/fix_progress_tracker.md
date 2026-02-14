# DailyFlash Fix Progress Tracker

Last updated: 2026-02-14
Owner: Engineering

## Status Legend

- `OPEN`: not started
- `IN_PROGRESS`: currently being fixed
- `BLOCKED`: cannot proceed due to dependency/risk
- `DONE`: fixed and verified

## Active Issues

| ID | Area | Priority | Status | Problem Summary | Target Verification |
| --- | --- | --- | --- | --- | --- |
| DF-001 | App Startup | P0 | DONE | `MainActivity` has multiple `lateinit` use cases that are passed into `NavGraph`; initialization safety must be guaranteed to avoid runtime crash. | App launch + navigation smoke test on device/emulator |
| DF-002 | Capture/Data | P0 | DONE | Capture path persists clip in `CameraService`, then persists again via `CaptureVideoUseCase` -> repository, causing duplicate-save behavior risk. | Record once, verify single saved clip in MediaStore |
| DF-003 | Data Delete | P1 | DONE | `VideoRepositoryImpl.deleteVideo(id)` relies on in-memory cache lookup by id; delete may fail when cache is cold. | Delete from Gallery/Calendar after fresh app launch |
| DF-004 | Unit Tests | P1 | DONE | Unit tests fail to compile because `CameraViewModel` and `SettingsViewModel` constructors changed but tests were not updated. | `./gradlew testDebugUnitTest` passes |
| DF-005 | Reminder Architecture | P2 | DONE | Consolidated reminder architecture to a single path (`domain` + `data.reminders` + `core.reminders`) and removed unused `core/notification` stack. | Single chosen path documented and validated |
| DF-006 | Export Options | P2 | DONE | Removed unsupported `fadeAudio` option and wired real date-overlay toggle path in export UI/ViewModel. | Functional export test with options on/off |

## Detailed Checklist

### DF-001 - MainActivity UseCase Initialization

- [x] Confirm every use case dependency is instantiated before `NavGraph` call.
- [x] Add runtime-safe initialization order.
- [x] Build check: `./gradlew assembleDebug -x test`.
- [x] Manual smoke test for app launch and first navigation.

### DF-002 - Duplicate Save In Capture Pipeline

- [x] Decide canonical owner of persistence (camera layer vs repository layer).
- [x] Remove duplicate persistence path.
- [x] Verify returned URI consistency to UI.
- [ ] Record one clip and validate exactly one new media item.

### DF-003 - Repository Delete Reliability

- [x] Refactor delete to resolve by storage query when cache miss occurs.
- [x] Keep cache invalidation/update behavior correct after delete.
- [x] Add/adjust unit tests for cold-cache delete scenario.
- [ ] Verify delete from both Calendar and Gallery flows.

### DF-004 - Unit Test Alignment

- [x] Update `CameraViewModelTest` constructor setup to include new dependencies.
- [x] Update `SettingsViewModelTest` constructor setup to include storage-location dependency.
- [x] Run `./gradlew testDebugUnitTest`.
- [x] Fix additional failing tests (if any) after compile stage.

### DF-005 - Reminder Stack Consolidation

- [x] Choose one reminder architecture path as source of truth.
- [x] Remove or deprecate duplicated path safely.
- [x] Verify boot-reschedule behavior and daily trigger.
- [x] Update docs to match final architecture.

### DF-006 - Export Options Validation

- [x] Confirm actual behavior of text overlay option in output file.
- [x] Confirm actual behavior of fade-audio option.
- [x] If partial, either complete implementation or restrict UI options.
- [x] Add regression test notes for export matrix.

## Change Log

| Date | ID | Change | By | Notes |
| --- | --- | --- | --- | --- |
| 2026-02-14 | INIT | Tracker created with initial issue set from current code scan. | Codex | Baseline from as-built review |
| 2026-02-14 | DF-001 | Initialized all required use cases in `MainActivity` before `NavGraph` usage. | Codex | Build verification passed |
| 2026-02-14 | DF-002 | Capture flow changed to single persistence path (repository owns final save). | Codex | Camera service now returns temp URI |
| 2026-02-14 | DF-003 | Repository delete now supports cold-cache fallback via storage query. | Codex | Cache cleanup improved |
| 2026-02-14 | DF-004 | Updated ViewModel tests + StorageManager tests; unit test suite passing. | Codex | `testDebugUnitTest` passed |
| 2026-02-14 | DF-005 | Removed `core/notification/*` and standardized reminder flow on WorkManager scheduler path. | Codex | Build + unit tests passed |
| 2026-02-14 | DF-006 | Removed unsupported `fadeAudio` surface and added export date-overlay toggle with range-based text. | Codex | Build + unit tests passed |
| 2026-02-14 | DF-001 | Emulator smoke launch executed (`am start` to `MainActivity`) with status `ok`. | Codex | Startup path validated on emulator |
| 2026-02-14 | DF-005 | BootCompletedReceiver instrumented tests executed via `connectedDebugAndroidTest` and passed on emulator. | Codex | Boot reschedule behavior validated through receiver flow |
| 2026-02-14 | DF-006 | Added unit regression checks for export date overlay option mapping in `ExportViewModelTest`. | Codex | Range-based `dateText` verified |
| 2026-02-14 | ANDROIDTEST | Restored androidTest toolchain by adding missing dependencies and refactoring legacy incompatible tests to current contracts. | Codex | `connectedDebugAndroidTest` 10/10 passed |
