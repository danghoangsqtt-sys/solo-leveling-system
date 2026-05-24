# Brainstorm Session: Phân tích kết quả Audit Tier 3 (Round 3)

**Ngày:** 2026-05-23
**Chủ đề:** Rà soát và đề xuất phương án khắc phục các lỗ hổng kỹ thuật (High, Medium, Low) phát hiện trong đợt quét Tier 3.

## 1. Bối cảnh
Hệ thống vừa trải qua đợt quét **vp-audit** (Round 3) tập trung vào Stack Android/Kotlin (Jetpack Compose, Room, Ktor, Gemini). Tổng cộng quét 14 file (core + feature modules), phát hiện ra 9 issues (2 High, 3 Medium, 4 Low) có ảnh hưởng đến độ ổn định, hiệu năng và trải nghiệm người dùng của ứng dụng **Solo Leveling System**.

---

## 2. Phân tích chi tiết và Giải pháp (Architecture Decisions)

### 🔴 HIGH - Rủi ro nghiêm trọng (Cần xử lý ngay)

#### **H-1: Lỗi Silent API Failure trong GeminiApiService**
* **Vấn đề:** Hàm `generateContent()` parse nội dung ngay cả khi HTTP status báo lỗi (401, 429), trả về chuỗi rỗng `""`. Service `AiQuestGeneratorService` nhận chuỗi rỗng sẽ âm thầm rớt xuống fallback hardcoded quests mà user không hay biết.
* **Giải pháp:** Cần ném `Exception` hoặc log error rõ ràng nếu `status.value !in 200..299` (giống như cách `generateImageBase64` đang làm). Catch lỗi ở Repository/ViewModel để thông báo Toast/SnackBar cho user (ví dụ: "Hệ thống AI đang quá tải, sử dụng nhiệm vụ cơ bản").

#### **H-2: AppwriteSyncService.syncCourses() thiếu phân trang (Pagination)**
* **Vấn đề:** Query có `limit=100`. Nếu số lượng khóa học/tài liệu (module dhebook) > 100, dữ liệu đằng sau sẽ bị mất mà không báo lỗi.
* **Giải pháp:** Cài đặt cơ chế phân trang (while loop với `cursor` hoặc `offset`) cho đến khi `response.documents.isEmpty()`. Đảm bảo toàn bộ tài liệu được tải và đồng bộ hoàn chỉnh.

---

### 🟡 MEDIUM - Lỗi hiệu năng và đồng bộ trạng thái

#### **M-1: Lỗi O(n²) trong LazyColumn tại CourseDetailScreen**
* **Vấn đề:** Dùng `lessons.indexOf(lesson)` trong closure của `items(lessons)` làm cho độ phức tạp render list biến thành O(n²), gây lag nặng nếu có hơn 50 bài học.
* **Giải pháp:** Sử dụng `itemsIndexed(lessons, key = { _, lesson -> lesson.id })` do Jetpack Compose cung cấp. Biến `idx` sẽ được cung cấp sẵn với chi phí O(1).

#### **M-2: Race Condition và Logic đổi chỗ trong CourseDetailViewModel**
* **Vấn đề:** 
  1. Thao tác `moveLessonUp/Down` đọc từ `lessons.value` bên trong một coroutine, nếu user tap liên tục sẽ sinh ra "Lost Update" (hai thao tác đọc cùng 1 snapshot).
  2. Không check trường hợp nhiều bài học cùng `orderIndex = 0` (lỗi hiển thị vô hiệu khi swap).
* **Giải pháp:** Dùng Mutex để khóa luồng khi thay đổi thứ tự (hoặc tận dụng `.update {}` của StateFlow nếu state xử lý trên bộ nhớ trước). Cập nhật lại logic swap thứ tự đảm bảo tất cả `orderIndex` phải là duy nhất.

#### **M-3: Tràn Context Window trong AuraService.chat()**
* **Vấn đề:** Tích lũy toàn bộ history không giới hạn, dẫn tới tốn token Gemini API (cost cao), dễ bị quá tải Context Window và tăng độ trễ (latency).
* **Giải pháp:** Cắt bớt lịch sử, chỉ giữ lại ví dụ 20 tin nhắn gần nhất: `history.takeLast(20)`. Hoặc thông minh hơn, sử dụng một "Summarizer" nếu cần ghi nhớ ngữ cảnh dài.

---

### 🔵 LOW - Các vấn đề nhỏ, Code Smell và UX

#### **L-1: Bất đồng nhất version Gemini API**
* **Vấn đề:** `GeminiApiService` dùng `gemini-2.0-flash` trong khi `AuraService` dùng `gemini-2.5-flash`.
* **Giải pháp:** Gom cấu hình Model ID về một file `AiConfig.kt` hoặc `BuildConfig` chung. Đề xuất thống nhất nâng lên `gemini-2.5-flash` toàn diện để tối ưu thông minh.

#### **L-2: Dùng System Icon cho Notifications**
* **Vấn đề:** `NotificationHelper` dùng icon mặc định của Android (`android.R.drawable.ic_dialog_alert`), làm mất đi chất liệu thẩm mỹ RPG (Solo Leveling theme).
* **Giải pháp:** Thêm vector drawable riêng biệt, thiết kế theo hướng monochrome (phù hợp chuẩn Notification) như thanh kiếm, cửa sổ thông báo hệ thống.

#### **L-3: Icon Hardcode trong LibraryScreen**
* **Vấn đề:** Mọi file đều có icon "📄" thay vì phản ánh định dạng thật (PDF, VIDEO).
* **Giải pháp:** Viết một extension function `.getIcon()` cho ContentType hoặc truyền trực tiếp vào component dựa trên enum.

#### **L-4: N+1 Query trong AppwriteSyncService.importNode**
* **Vấn đề:** Mỗi vòng lặp thêm lesson lại chọc xuống database để đếm `getTotalCountSync`.
* **Giải pháp:** Query count 1 lần đầu tiên gán cho biến `var currentLessonCount`, trong vòng lặp dùng `currentLessonCount++` để gán `orderIndex`.

---

## 3. Tổng Hợp Tác Vụ Cần Xử Lý

### Cần quyết định (Mở)
* **Q1:** Đối với API error (H-1), ứng dụng sẽ tự động ẩn UI báo lỗi và im lặng chạy fallback cục bộ, hay bung một Popup báo "Kết nối đến Hầm Ngục AI bị gián đoạn"?
* **Q2:** Việc thống nhất Gemini model (L-1), có cần thay đổi cả version API trong `libs.versions.toml` không?

### Action Items
- [ ] Mở `/vp-auto` thực thi ngay lập tức cụm High (H-1, H-2) và M-1 (vì dễ làm nhưng ảnh hưởng lớn).
- [ ] Gom cụm M-2, M-3, L-4 xử lý chung trong một ticket tối ưu hiệu năng.
- [ ] Giao L-2, L-3 cho ticket dọn dẹp UI/UX.

---

## Phases (Phân loại cập nhật)
- **Phase 9 (Current):** Cần đưa ngay H-1, H-2 và M-1 vào xử lý trong phase hiện tại vì nó ảnh hưởng đến core logic và API calls.
- **Phase 10 (Next):** Sẽ gom các task Medium và Low còn lại như tối ưu Mutex, giới hạn lịch sử chat, thay notification icons và tối ưu N+1 Query.

---

## Project meta intake (FEAT-009)
*Đã ràng buộc cấu hình profile. Bỏ qua.*
