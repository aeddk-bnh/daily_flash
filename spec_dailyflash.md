# SPECIFICATION: DailyFlash (As-Built)

## 1. Document Control
*   **Project Name:** DailyFlash
*   **Version:** 1.0 (Release Candidate)
*   **Status:** ✅ **AS-BUILT** (Verified Implementation)
*   **Date:** 2026-01-17

---

## 2. Purpose & Scope
### 2.1 Goal
Provide a minimal Android application for capturing 1-second daily video moments and stitching them into a cohesive video journal, emphasizing privacy, speed, and simplicity.

### 2.2 In-Scope
*   **Video Capture:** 1-second fixed duration recording.
*   **Calendar Organization:** Visual grid of daily memories.
*   **Journal Export:** On-device stitching of clips into a single MP4.
*   **Audio Overlay:** Option to add background music during export.
*   **Management:** Review and deletion of specific daily clips.

### 2.3 Out-of-Scope
*   Cloud Backup / Sync.
*   Social Sharing integration (beyond system Share Sheet).
*   Complex Video Editing (trimming, filters, transitions).

---

## 3. Functional Requirements

### 3.1 Video Capture Module
*   **REQ-CAP-01:** System **MUST** utilize `CameraX` for video capture.
*   **REQ-CAP-02:** Recording duration **MUST** be strictly limited to ~1000ms (1 second).
*   **REQ-CAP-03:** Capture interface **MUST** require single-tap interaction.
*   **REQ-CAP-04:** System **MUST** support both Hardware Encoders (Real Devices) and Software Encoders (Emulators via `Quality.LOWEST` fallback).

### 3.2 Media Organization (Calendar)
*   **REQ-ORG-01:** System **MUST** display a monthly calendar view.
*   **REQ-ORG-02:** Days with recorded content **MUST** show a thumbnail indicator.
*   **REQ-ORG-03:** System **MUST** allow playback of the daily clip upon tapping a date.
*   **REQ-ORG-04:** System **MUST** allow deletion of a clip via long-press interaction.

### 3.3 Export Engine
*   **REQ-EXP-01:** System **MUST** use `Media3 Transformer` for video stitching.
*   **REQ-EXP-02:** Export process **MUST** run locally without internet access.
*   **REQ-EXP-03:** User **MAY** select a custom audio file (MP3/AAC) to overlay on the final video.
*   **REQ-EXP-04:** Final video **MUST** be saved to the public Gallery (`MediaStore`) for external access.

---

## 4. Data & Workflows

### 4.1 Data Entities
*   **VideoFile:**
    *   `id`: String (Unique)
    *   `uri`: Uri (App-Specific Storage)
    *   `date`: LocalDate
    *   `sizeBytes`: Long

### 4.2 Core Workflows
1.  **Capture Flow:** User Taps Record -> CameraService records 1s -> Saves to public MediaStore in `Movies/DailyFlash/`. Filename: `dailyflash_YYYYMMDD_timestamp.mp4`.
2.  **Export Flow:** User Selects Range -> User Selects Audio (Optional) -> Transformer stitches clips -> Saves to `Movies/DailyFlash/Exports` -> Share Sheet opens.

---

## 5. Technical Constraints & Decisions

### 5.1 Platform
*   **OS:** Android 7.0 (API 24) minimum.
*   **Language:** Kotlin.
*   **UI:** Jetpack Compose (Material 3).

### 5.2 Storage & Privacy
*   **Public Storage (MediaStore):** Used for clips to ensure user accessibility and visibility in standard Gallery apps.
*   **Local Only:** No `INTERNET` permission in Manifest.
*   **Scoped Storage:** Compliant with Android Q+ requirements using `RELATIVE_PATH` in `Movies/DailyFlash`.

### 5.3 Compatibility
*   **Emulator Support:** Explicit logic detects emulators and downgrades recording quality to 480p to bypass encoder hardware limitations.

---

## 6. 📝 MENU TINH CHỈNH (Yêu cầu Quyết định)

Dưới đây là các đề xuất cải tiến để nâng cấp ứng dụng. Vui lòng chọn các phương án bạn muốn đưa vào tài liệu đặc tả chính thức:

### 🛑 VẤN ĐỀ [01]: Trải nghiệm sau khi quay (Post-Capture Experience)
*Bối cảnh: Hiện tại ứng dụng chỉ hiển thị thông báo "Video saved". Cần cải thiện tính tương tác.*

**Vui lòng chọn một phương án:**
1.  **Preview tức thời (Instant Preview):** Hiển thị ngay đoạn video vừa quay trong một cửa sổ nhỏ hoặc hiệu ứng chuyển cảnh để người dùng kiểm tra kết quả ngay lập tức.
    *   *Ưu điểm:* Tăng tính xác thực, người dùng biết ngay video có đạt yêu cầu không.
2.  **Giữ nguyên hiện tại:** Chỉ hiển thị Toast thông báo.

### 🛑 VẤN ĐỀ [02]: Điều hướng trong Gallery (Gallery Navigation)
*Bối cảnh: Việc quay lại danh sách để xem từng video có thể gây ngắt quãng.*

**Vui lòng chọn một phương án:**
1.  **Thao tác vuốt (Swipe to Navigate):** Khi đang xem một video trong Gallery, cho phép vuốt Trái/Phải để chuyển sang clip của ngày tiếp theo hoặc trước đó.
    *   *Ưu điểm:* Trải nghiệm mượt mà giống như các ứng dụng mạng xã hội hiện đại (TikTok/Reels).
2.  **Giữ nguyên hiện tại:** Quay lại lưới (Grid) để chọn video khác.

### 🛑 VẤN ĐỀ [03]: Tính năng Nhắc nhở & Giữ chân người dùng (Engagement)
*Bối cảnh: Người dùng dễ quên quay phim hàng ngày, làm đứt quãng hành trình.*

**Vui lòng chọn một phương án:**
1.  **Nhắc nhở & Streaks:** Gửi thông báo đẩy (Push Notification) vào giờ cố định và hiển thị số ngày quay liên tiếp (Streaks) để tạo động lực.
    *   *Ưu điểm:* Tăng tỷ lệ giữ chân người dùng (Retention).
2.  **Không nhắc nhở:** Giữ ứng dụng ở mức tối giản nhất.

### 🛑 VẤN ĐỀ [04]: Nâng cấp Bộ máy Export (Advanced Export)
*Bối cảnh: Video xuất ra hiện tại khá đơn giản.*

**Vui lòng chọn một phương án:**
1.  **Export nâng cao:** Cho phép chèn Text Overlay (ngày tháng) lên từng clip, tùy chỉnh thứ tự video, và thêm hiệu ứng âm thanh (fade-in/out) cho nhạc nền.
    *   *Ưu điểm:* Sản phẩm cuối cùng chuyên nghiệp hơn để chia sẻ.
2.  **Giữ nguyên hiện tại:** Ghép nối đơn giản các clip theo thời gian.

### 🛑 VẤN ĐỀ [05]: Tự động hóa Quản lý Dung lượng (Storage Management)
*Bối cảnh: Các clip thô (1s) có thể tích tụ làm đầy bộ nhớ.*

**Vui lòng chọn một phương án:**
1.  **Tự động dọn dẹp:** Tùy chọn tự động xóa các clip thô sau khi người dùng đã Export thành công video tổng hợp của tháng hoặc năm.
    *   *Ưu điểm:* Tiết kiệm dung lượng lưu trữ cho người dùng.
2.  **Giữ nguyên hiện tại:** Người dùng tự quản lý hoặc xóa thủ công.

---

## 7. Traceability Appendix
*   **Capture Logic:** `CameraService.kt`
*   **Storage Logic:** `StorageManager.kt`
*   **Export Logic:** `MediaProcessor.kt`, `ExportJournalUseCase.kt`
*   **UI Layer:** `CameraScreen.kt`, `CalendarScreen.kt`, `ExportScreen.kt`
