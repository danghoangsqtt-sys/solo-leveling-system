# Nguồn Chân Lý Dự Án (Project Brain)

Đây là tài liệu gốc định hướng hệ thống dành cho AI Agent (Vibe Coding).

## 1. Ngữ cảnh (Context)
- **Tên dự án:** System Leveling
- **Mục tiêu:** Ứng dụng Gamification phong cách RPG giúp theo dõi thói quen, quản lý tài chính và học tập.
- **Kiến trúc:** Multi-module Clean Architecture (Android). Ngôn ngữ 100% Kotlin.

## 2. Các Module Chính
1. `:app`: Giao diện chính, AppNavGraph.
2. `:core`: Mạng lưới, Database (Room), DI (Hilt), Model dùng chung.
3. `:feature:onboarding`: Đăng ký, thiết lập chỉ số.
4. `:feature:home`: Bảng trạng thái (Status Panel), Radar Chart.
5. `:feature:skills`: Cây kỹ năng (Skill Tree).
6. `:feature:quests`: Nhiệm vụ (Daily/System/Penalty Quests).
7. `:feature:finance`: Quản lý tài chính (Budget, Net Cashflow).
8. `:feature:library`: Trình đọc tài liệu học tập, Zen mode.

## 3. Luồng Giao Tiếp
- Giao diện người dùng sử dụng **Jetpack Compose**. Các màn hình nằm trong gói `ui` của từng `:feature`.
- Lấy dữ liệu thông qua **ViewModel** (Hilt) và **Repository** (ở `:core`).
- Database ưu tiên local **Room Database** (Offline-first). Đồng bộ qua **Google Drive API** (Dự kiến).
- Các luồng tương tác AI sẽ gọi thông qua `:core:network` bằng **Ktor Client**.

## 4. Thiết kế Giao Diện (Design System)
- Sử dụng Holographic Glassmorphism. Các thông số, màu sắc được quy định chi tiết trong thư mục `themes/design_theme.json` và `themes/design_system.md`.
