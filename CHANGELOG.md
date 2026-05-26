# Changelog

Tất cả các thay đổi đáng chú ý trong dự án Solo Leveling System sẽ được ghi nhận tại đây.

Định dạng dựa trên [Keep a Changelog](https://keepachangelog.com/vi/1.0.0/).

---

## [Unreleased] — 2026-05-26

### Fixed
- **Quest Reset Bug**: Sửa lỗi nhiệm vụ không reset vào ngày mới. `QuestViewModel` bây giờ sử dụng `getQuestsByDate()` thay vì `getAllQuests()` để chỉ hiển thị nhiệm vụ của ngày hôm nay.

### Changed
- **Refactor QuestListScreen.kt**: Tách file UI lớn (~891 dòng) thành các component nhỏ gọn trong `ui/components/`:
  - `QuestConstants.kt` — Shared colors, RankMeta
  - `QuestTimelineItem.kt` — Timeline card cho từng nhiệm vụ
  - `QuestTimer.kt` — Bộ đếm ngược thời gian thực
  - `QuestDetailSheet.kt` — Bottom sheet chi tiết nhiệm vụ
  - `PenaltyBanner.kt` — Banner cảnh báo khi nhiệm vụ thất bại
- **Refactor AiQuestGeneratorService.kt**: Tách logic phức tạp (~618 dòng) thành:
  - `QuestTimeSlotCalculator.kt` — Tính toán khung giờ trong ngày
  - `FallbackQuestProvider.kt` — Sinh nhiệm vụ mặc định khi không có work plan

### Added
- `CHANGELOG.md` — File ghi nhận thay đổi dự án.
- `ARCHITECTURE.md` — Tài liệu kiến trúc hệ thống.

---

## [v1.0.0] — 2026-05-01

### Added
- Hệ thống nhiệm vụ hàng ngày (Daily Quests) với AI sinh nhiệm vụ cá nhân hóa.
- Cây kỹ năng (Skill Tree) đa nhánh với hệ thống cấp độ.
- Hệ thống phần thưởng (EXP, Gold, Stat Points, Skill Points, Loot Drops).
- Hệ thống hình phạt (Penalty Engine) với Debt Points.
- Quản lý tài chính cá nhân (Finance module).
- Lịch & Sự kiện (Calendar module).
- Nhật ký cá nhân (Journal module).
- Thư viện tài liệu & khóa học (Library module).
- Kho đồ & Danh hiệu (Inventory & Titles module).
- Đồng bộ đám mây (Cloud Sync via Firebase).
- Onboarding cho người dùng mới.
