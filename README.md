<div align="center">
  <img src="images/logo.png" width="160" height="160" alt="Solo Leveling System Logo" style="border-radius: 24px;" />
  
  <h1>⚡ SOLO LEVELING SYSTEM ⚡</h1>
  <p><b>Hệ thống thức tỉnh và vượt cấp thành thần trong đời thực</b></p>

  <p>
    <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform" /></a>
    <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Language" /></a>
    <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="UI" /></a>
    <a href="https://deepmind.google/technologies/gemini/"><img src="https://img.shields.io/badge/Gemini_AI-2.0_Flash-F68A0A?style=for-the-badge&logo=google-gemini&logoColor=white" alt="AI" /></a>
    <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Room_DB-v7-0068B7?style=for-the-badge&logo=sqlite&logoColor=white" alt="Database" /></a>
  </p>
</div>

```text
╔══════════════════════════════════════════════════════════════════════════════════════╗
║                                   [SYSTEM ALERT]                                     ║
║                                                                                      ║
║   Chào mừng Hunter! Bạn đã được chọn làm "Người Chơi" của Hệ Thống.                  ║
║   Hãy thực hiện các nhiệm vụ thực tế hàng ngày để gia tăng chỉ số, rèn luyện kỹ năng,║
║   thu thập trang bị và vượt qua các giới hạn để thức tỉnh thần cách của chính mình.  ║
║                                                                                      ║
║   ⚠️ CẢNH BÁO: Thất bại hoặc trốn tránh nhiệm vụ sẽ phải gánh chịu Hình Phạt tương ứng!║
╚══════════════════════════════════════════════════════════════════════════════════════╝
```

---

## 📖 Giới thiệu Dự án

**Solo Leveling System** là một ứng dụng Android Native được xây dựng nhằm mục đích **Gamification (Trò chơi hóa) cuộc sống thực**. Lấy cảm hứng từ bộ manhwa/anime nổi tiếng *Solo Leveling*, ứng dụng biến những mục tiêu phát triển bản thân, học tập, thói quen và tài chính hàng ngày thành các nhiệm vụ RPG kỳ thú. 

Hệ thống kết hợp trí tuệ nhân tạo **Gemini AI** để tự động hóa việc lên lịch trình, đề xuất nhiệm vụ tối ưu, viết nhật ký và đóng vai trò trợ lý đồng hành, giúp người chơi nâng cấp bản thân liên tục 24/7.

---

## 🌟 Tính Năng Cốt Lõi

### 1. Bảng Trạng Thái & Radar Chart (Status Panel)
* **Thông tin Hunter:** Quản lý Tên, Cấp độ (Level), EXP, Vàng (Gold), và Danh hiệu (Title) đang trang bị.
* **Biểu đồ Lục Giác (Radar Chart):** Trực quan hóa 6 chỉ số thuộc tính cốt lõi được đồng bộ với tiến trình thực tế:
  * 💪 **STR (Sức mạnh):** Rèn luyện thể chất, thể dục, sức chịu đựng.
  * 🧠 **INT (Trí tuệ):** Học tập, lập trình, đọc tài liệu.
  * ⚡ **AGI (Khéo léo):** Năng suất làm việc, tốc độ hoàn thành công việc.
  * 🛡️ **VIT (Sức bền):** Ăn uống lành mạnh, giấc ngủ, uống nước.
  * 🔮 **WIS (Trí khôn):** Thiền định, viết nhật ký, quản lý tài chính.
  * ✨ **CHA (Sức hút):** Kỹ năng giao tiếp, hoạt động xã hội.

### 2. Trình Tạo Nhiệm Vụ AI (AI Quest Generator)
* **Eisenhower Matrix Algorithm:** Tự động phân loại công việc từ Kế hoạch ngày/tuần/tháng để sinh ra 6-10 nhiệm vụ hàng ngày:
  * 🔥 **CRITICAL (Rank A/B):** Nhiệm vụ khẩn cấp & quan trọng, tự động chia nhỏ thành các sub-quests hành động được ngay và đặt vào giờ sáng.
  * ⚡ **HIGH (Rank B/C):** Nhiệm vụ quan trọng trong khung giờ vàng tập trung.
  * 🟢 **NORMAL/LOW (Rank D/E):** Các routine đan xen để giảm thiểu burn-out.
* **Nhiệm vụ Sức Khỏe Tự Động:** Tự động lồng ghép nhiệm vụ nhắc nhở uống nước (Hydration Check) và vận động giãn cơ mỗi 2 tiếng.

### 3. Cây Kỹ Năng Tương Tác (Interactive Skill Tree)
* **Cấu trúc Đa Tầng (Parent-Child):** Nhóm các kỹ năng theo nhánh lớn (như Sức Khỏe, Lập Trình, Ngôn Ngữ).
* **Tích lũy Điểm Kỹ Năng (SP):** Khi hoàn thành nhiệm vụ liên kết với kỹ năng con, SP sẽ được cộng trực tiếp giúp tăng cấp tinh thông (Mastery Level) của nhánh cha.
* **Hoạt ảnh Mượt Mà:** Sử dụng Jetpack Compose Motion mang đến giao diện cây kết nối trực quan, có các thanh tiến trình chuyển động sinh động.

### 4. Động Cơ Phạt & Thưởng (Reward & Penalty Engine)
* **Reward Engine:** Hoàn thành nhiệm vụ nhận EXP, Gold, SP và quay thưởng ngẫu nhiên vật phẩm dựa trên tỷ lệ rơi (Loot Table) của từng Rank nhiệm vụ (từ Rank E đến Rank S).
* **Penalty Engine:** Nhiệm vụ quá hạn không hoàn thành sẽ tích lũy điểm Nợ (Debt Points). Đạt quá giới hạn Nợ sẽ kích hoạt hình phạt giảm EXP, hạ cấp hoặc các hình phạt ảo trong hệ thống.
* **Hệ thống Cấp độ:** Công thức EXP tăng tiến theo cấp độ để đảm bảo tính thử thách lâu dài.

### 5. Trợ Lý Ảo NPC Aura (2.5D Aura Assistant)
* **Hoạt ảnh 2.5D:** Nhân vật anime Aura đồng hành sống động với hoạt ảnh thở tự nhiên (`scale` breathing animation) và phát sáng hào quang.
* **Trò chuyện Trí Tuệ Nhân Tạo:** Tích hợp Gemini AI giúp bạn giải đáp thắc mắc, trò chuyện động viên RPG-style, và đưa ra gợi ý phát triển bản thân.

### 6. Báo Cáo Ngày & Nhật Ký AI (Daily Summary & AI Journal)
* **Daily Report Card:** Xuất hiện vào lúc 22:00 hàng ngày, tổng hợp tỷ lệ hoàn thành nhiệm vụ, các chỉ số thuộc tính được gia tăng, điểm kỹ năng đạt được và vật phẩm nhặt được trong ngày.
* **AI Auto Journal:** Tự động tổng hợp dữ liệu hoạt động trong ngày thành một trang nhật ký RPG mang phong cách sử thi hào hùng.

### 7. Quản Lý Tài Chính & Kho Đồ (Finance & Inventory)
* **Finance:** Ghi chép dòng tiền (Cashflow), lập ngân sách học tập/sức khỏe và liên kết tài chính với các chỉ số thuộc tính WIS.
* **Inventory Grid:** Kho lưu trữ vật phẩm dạng lưới MMORPG, cho phép sử dụng các Consumables (như *Small Energy Potion* để tăng EXP, *Focus Crystal* tăng SP) để tối ưu hóa tiến trình phát triển.

---

## 🛠️ Kiến Trúc & Công Nghệ

Dự án áp dụng chặt chẽ **Multi-module Clean Architecture** và tuân thủ các nguyên tắc thiết kế hiện đại trên Android:

```text
┌────────────────────────────────────────────────────────┐
│                        :app                            │
│    (Application class, Hilt DI, AppNavGraph, Theme)   │
└───────────────────┬────────────────────────┬───────────┘
                    │                        │
                    ▼                        ▼
┌────────────────────────┐      ┌────────────────────────┐
│       :feature:*       │      │       :feature:*       │
│        (Home/NPC)      │      │     (Quests/Skills)    │
│    UI, Compose, VM     │      │    UI, Compose, VM     │
└───────────────────┬────┘      └────┬───────────────────┘
                    │                        │
                    └───────────┬────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────┐
│                        :core                           │
│   (Database Room v7, Network Ktor, Reward Engines,     │
│    SettingsManager, Common Models & Repositories)      │
└────────────────────────────────────────────────────────┘
```

* **Jetpack Compose & Material 3:** Toàn bộ giao diện được viết bằng khai báo Compose, kết hợp hiệu ứng kính (Glassmorphism), bảng màu HSL tối ưu cho trải nghiệm chơi game.
* **Room Database (v7):** Lưu trữ dữ liệu ngoại tuyến (Offline-first), hỗ trợ các TypeConverters phức tạp để xử lý cấu trúc JSON trong Entity.
* **Dagger Hilt:** Quản lý Dependency Injection trên toàn bộ các module.
* **Ktor Client:** Kết nối API Gemini để thực hiện các cuộc gọi AI tối ưu hóa hiệu năng và bộ nhớ.
* **WorkManager:** Đảm bảo các tác vụ nền như sinh nhiệm vụ hàng ngày và gửi thông báo nhắc nhở hoạt động chính xác kể cả khi tắt ứng dụng.

---

## 📂 Danh Sách Module Chi Tiết

| Tên Module | Mục Tiêu | Thành Phần Chính |
|---|---|---|
| `:app` | Launcher, Navigation, Hilt Setup | `MainActivity`, `SystemLevelingApp`, `AppNavGraph` |
| `:core` | Database, DI, Network, Engine | `AppDatabase`, `AiQuestGeneratorService`, `RewardEngine`, `DailySummaryService` |
| `:feature:onboarding` | Khảo sát người chơi mới, phân lớp nghề nghiệp | `OnboardingScreen`, `SurveyScreen` (Khảo sát 3 giai đoạn) |
| `:feature:home` | Màn hình chính, trợ lý ảo Aura | `HomeScreen`, `NpcChatScreen`, `HomeViewModel` |
| `:feature:quests` | Danh sách nhiệm vụ, dialog phần thưởng | `QuestListScreen`, `QuestCompleteDialog`, `WorkPlanInputSheet` |
| `:feature:skills` | Cây kỹ năng | `SkillTreeScreen`, `SkillTreeViewModel` |
| `:feature:finance` | Quản lý tài sản và ngân sách | `FinanceScreen`, `FinanceViewModel` |

---

## 🚀 Hướng Dẫn Cài Đặt

### Yêu cầu hệ thống
* Android Studio Jellyfish (hoặc phiên bản mới hơn)
* JDK 17 trở lên
* Android SDK 34 trở lên

### Các bước cài đặt
1. **Clone mã nguồn:**
   ```bash
   git clone https://github.com/danghoangsqtt-sys/solo-leveling-system.git
   ```
2. **Mở dự án:**
   Mở thư mục `solo-leveling-system` bằng Android Studio và đợi Gradle đồng bộ hoàn tất.
3. **Cấu hình API Key (Nếu sử dụng tính năng AI):**
   * Chạy ứng dụng trên Emulator hoặc thiết bị thực.
   * Trên màn hình **HomeScreen**, nhấn vào biểu tượng Cài đặt (⚙️) ở góc trên bên phải.
   * Nhập mã khóa **Gemini API Key** cá nhân của bạn (Bạn có thể lấy key miễn phí tại [Google AI Studio](https://aistudio.google.com/)).
4. **Biên dịch và chạy:**
   Nhấn nút **Run** trong Android Studio để cài đặt lên thiết bị của bạn.

---

## 🎮 Hướng Dẫn Chơi (How to Level Up)

1. **Thức tỉnh & Chọn Nghề:** Bắt đầu ứng dụng bằng bài khảo sát tính cách để định hình Nghề Nghiệp (Class) của bạn (ví dụ: *Alchemist*, *Shadow Monarch*, *Mage*...).
2. **Nhập Kế Hoạch Thực Tế:** Thêm các công việc cần làm vào phần Kế Hoạch (WorkPlan). Hệ thống AI sẽ tự động tính điểm ưu tiên và thiết lập danh sách nhiệm vụ tương ứng vào lúc 00:00 ngày hôm sau.
3. **Đánh Bại Nhiệm Vụ:** Hoàn thành các subtasks trong ngày. Bật thông báo để không bỏ lỡ các đợt " Hydration Check" tiếp VIT.
4. **Nhận Thưởng:** Nhấp hoàn thành nhiệm vụ để mở Dialog phần thưởng RPG. Sử dụng bình dược phẩm trong Kho Đồ khi cần đẩy nhanh tiến trình EXP hoặc SP.
5. **Nhìn Lại Bản Thân:** Đọc Báo cáo ngày lúc 22:00 để theo dõi tốc độ phát triển chỉ số lục giác và chiêm ngưỡng nhật ký viết bằng AI mô tả hành trình chinh phục ngày hôm nay của bạn.

---

## 📜 Giấy Phép & Đóng Góp

Dự án này được phát triển bởi **danghoangsqtt-sys**. Mọi đóng góp nhằm tối ưu hóa hiệu năng, cải thiện thuật toán tạo quest AI hoặc thêm các hiệu ứng Compose UI RPG đều được chào đón nồng nhiệt thông qua Pull Requests.

*Hệ thống đã sẵn sàng. Bạn đã sẵn sàng vượt cấp thành thần chưa?* ⚡
