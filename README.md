# System Leveling

System Leveling là một ứng dụng Android Native giúp biến cuộc sống thực thành một game RPG cá nhân, lấy cảm hứng từ truyện Solo Leveling. Dự án được thiết kế theo chuẩn ViePilot Workflow với Clean Architecture, 100% Kotlin và Jetpack Compose.

## Bắt đầu (Getting Started)

1. Mở dự án trong **Android Studio**.
2. Đồng bộ Gradle (Sync Project with Gradle Files).
3. Biên dịch và chạy ứng dụng trên Emulator hoặc thiết bị thực.

## Cấu trúc Dự Án (Architecture)

Dự án sử dụng **Multi-module Clean Architecture**:
- `:app`: Điểm khởi chạy của ứng dụng, cấu hình Hilt và Navigation tổng thể.
- `:core`: Chứa cơ sở dữ liệu (Room), kết nối mạng (Ktor), Domain Models và các repository chung.
- `:feature:*`: Các module tính năng riêng biệt (home, skills, quests, finance, library, v.v.).

## Công nghệ sử dụng
- **UI:** Jetpack Compose (Material 3).
- **DI:** Dagger Hilt.
- **Local Database:** Room Database.
- **Network:** Ktor Client / Retrofit.
- **Navigation:** Jetpack Navigation Compose.
- **Architecture:** MVVM, Offline-first.
