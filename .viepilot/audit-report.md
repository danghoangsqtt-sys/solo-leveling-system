# ViePilot Project Audit Report

**Date:** 2026-05-26  
**Target:** Solo Leveling System (Android Native Project) - Focus: Quest System  
**Status:** ⚠️ Audit Action Required (Drifts & Missing Docs Detected)

---

## 📊 Summary of Tiers

| Tier | Area | Status | Findings |
|---|---|---|---|
| **Tier 1** | State Consistency | ⚠️ Drift Detected | Missing TRACKER.md and ROADMAP.md |
| **Tier 2** | Documentation Drift | ⚠️ Gaps Detected | Missing CHANGELOG.md and ARCHITECTURE.md |
| **Tier 3** | Stack Best Practices | ⚠️ Compliance Issue | Code Smells found in Quest Module |
| **Tier 4** | Framework Integrity | ➖ Skipped | Non-framework repository |

---

## 🧱 Tier 1 — ViePilot State Consistency

### Findings
- **`HANDOFF.json`**: Present.
- **`TRACKER.md`**: ❌ Missing. Không có file theo dõi task trung tâm.
- **`ROADMAP.md`**: ❌ Missing. Không có milestone lộ trình.
- **Git Tags**: ❌ Không phát hiện git tags (ví dụ: `vp-p9-complete`).

### Action Items
- Cần chạy `/vp-crystallize` hoặc tự tạo `.viepilot/TRACKER.md` và `.viepilot/ROADMAP.md` để theo dõi dự án chuẩn theo quy trình.

---

## 📄 Tier 2 — Project Documentation Drift

### Findings
- **`README.md`**: ✅ Đã có và khá đầy đủ chi tiết.
- **`CHANGELOG.md`**: ❌ Missing.
- **`ARCHITECTURE.md`**: ❌ Missing.

### Action Items
- Tạo `CHANGELOG.md` để lưu lại quá trình phát triển tính năng.
- Tạo `ARCHITECTURE.md` để vẽ sơ đồ module và kiến trúc hệ thống (kết hợp với thư mục `docs/`).

---

## ⚡ Tier 3 — Stack Best Practices & Code Quality (Focus: Quest System)

### 1. `AiQuestGeneratorService.kt` (⚠️ Vi Phạm Nguyên tắc Đơn Trách Nhiệm - SRP)
- **Tình trạng**: File dài hơn 600 dòng. Class đang ôm đồm quá nhiều việc: gọi API (Network), xây dựng prompt rất dài (Logic AI), tính toán chia khung giờ (Time slots logic), phân tích cú pháp chuỗi JSON (Parsing), và chứa cả dữ liệu cứng (Fallback Quests).
- **Đề xuất fix**: Tách nhỏ ra thành các class: `QuestPromptBuilder`, `QuestTimeSlotCalculator`, và `FallbackQuestProvider`.

### 2. `QuestEntity.kt` & Room Database (⚠️ Anti-Pattern)
- **Tình trạng**: Các trường như `subtasks`, `statPointRewards`, `skillPointRewards` đang được lưu dưới dạng `String` (chứa mảng hoặc object JSON) rồi mã hóa/giải mã thủ công bằng kotlinx.serialization ở mọi nơi.
- **Đề xuất fix**: Tạo `Room TypeConverter` để tự động map `List<String>` hoặc `Map<String, Int>` sang `String` lúc lưu vào database, giữ Entities ở dạng object (POJO) sạch sẽ.

### 3. `QuestListScreen.kt` (⚠️ Maintainability)
- **Tình trạng**: File UI Jetpack Compose siêu dài (gần 900 dòng).
- **Đề xuất fix**: Tách các Compose Unit như `QuestTimelineItem`, `QuestTimer`, `QuestDetailSheet` ra thành các tệp tin `ui/components/` riêng biệt.

### 4. `RewardEngine.kt` (✅ Tốt)
- **Tình trạng**: Lỗi Transaction được ghi nhận ở kỳ kiểm tra trước đã được fix. Phương thức `processQuestCompletion` hiện đang được bọc an toàn trong `database.withTransaction { ... }`.

---

## 🛠️ Lựa Chọn Tự Động Sửa Lỗi (Auto-Fix)

Bạn có thể yêu cầu tôi thực hiện:
1. **Sửa Tier 1 & 2**: Tự động tạo `TRACKER.md`, `ROADMAP.md`, `CHANGELOG.md`, `ARCHITECTURE.md`.
2. **Sửa Tier 3 (Refactor)**: Viết lại `QuestEntity.kt` (Thêm TypeConverters) hoặc chẻ nhỏ `QuestListScreen.kt` và `AiQuestGeneratorService.kt`.
