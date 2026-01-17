# Technical Stack

## 1. Core Platform
*   **Language:** Kotlin 1.9+
*   **Minimum SDK:** API 24 (Android 7.0)
*   **Target SDK:** API 34 (Android 14)
*   **Build System:** Gradle (Kotlin DSL)

## 2. User Interface
*   **Framework:** Jetpack Compose (Material 3)
*   **Navigation:** Navigation Compose
*   **Async Image Loading:** Coil
*   **Icons:** Material Icons Extended

## 3. Core Architecture
*   **Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture (Layers: App -> Presentation -> Domain -> Data -> Core)
*   **Dependency Injection:** Context Injection (Manual) / HILT (Optional - sticking to Manual for simplicity per current codebase)
*   **Concurrency:** Kotlin Coroutines + Flow

## 4. Key Libraries (Modules)
### 4.1 Camera (Capture Module)
*   **Library:** CameraX
*   **Components:** `camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-video`, `camera-view`
*   **Rationale:** Vendor-agnostic, lifecycle-aware, simplifies video capture complexity.

### 4.2 Media Processing (Export Module)
*   **Library:** Jetpack Media3
*   **Components:** `media3-transformer`, `media3-effect`, `media3-common`
*   **Rationale:** Modern replacement for ffmpeg/MediaMuxer, supports hardware acceleration, concatenation, and audio mixing.

### 4.3 Storage & Persistence (Data Module)
*   **Video Storage:** MediaStore (Public `Movies/DailyFlash`)
*   **Preferences:** Jetpack DataStore (Proto/Preferences) for Settings & Streaks.
*   **Cache:** In-Memory Caching

### 4.4 Background Jobs
*   **Notifications:** AlarmManager
*   **Cleanup:** WorkManager (Periodic jobs)

## 5. Testing
*   **Unit Tests:** JUnit 4, Mockito-Kotlin, Robolectric
*   **Coroutines:** `kotlinx-coroutines-test`
*   **Integration:** Instrumented Tests (Espresso - Future)
