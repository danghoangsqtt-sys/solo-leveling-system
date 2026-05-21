# 🎮 SYSTEM LEVELING — ANDROID APP PROMPT V2.0
## Hệ Thống Cá Nhân Phong Cách Solo Leveling / Manhwa Xuyên Không

> **Prompt dành cho AI Coding Agent** — Bản đầy đủ, chi tiết từ kiến trúc đến từng pixel UI.
> Phiên bản: 2.0 | Ngày tạo: 2026-05-20
> Tác giả ý tưởng: Đặng Hoàng (@danghoangsqtt-sys)

---

## MỤC LỤC

1. [Tổng Quan Dự Án](#1-tổng-quan-dự-án)
2. [Kiến Trúc Kỹ Thuật](#2-kiến-trúc-kỹ-thuật)
3. [Onboarding — Khởi Tạo Nhân Vật](#3-onboarding--khởi-tạo-nhân-vật)
4. [Home — Bảng Trạng Thái](#4-home--bảng-trạng-thái-status-panel)
5. [Skill Tree — Cây Kỹ Năng](#5-skill-tree--cây-kỹ-năng)
6. [Quest System — Nhiệm Vụ Hệ Thống](#6-quest-system--nhiệm-vụ-hệ-thống)
7. [Penalty System — Hệ Thống Xử Phạt](#7-penalty-system--hệ-thống-xử-phạt)
8. [Inventory — Kho Vật Phẩm](#8-inventory--kho-vật-phẩm)
9. [Title System — Kho Danh Hiệu](#9-title-system--kho-danh-hiệu)
10. [Finance — Quản Lý Tài Chính](#10-finance--quản-lý-tài-chính-mới)
11. [Learning Library — Kho Tài Liệu Học Tập](#11-learning-library--kho-tài-liệu-học-tập-mới)
12. [Journal — Nhật Ký](#12-journal--nhật-ký)
13. [Calendar — Lịch Làm Việc](#13-calendar--lịch-làm-việc)
14. [To-Do List](#14-to-do-list)
15. [Settings](#15-settings)
16. [AI Integration](#16-ai-integration--prompt-templates)
17. [Database Schema](#17-database-schema-room)
18. [Thiết Kế UI/UX](#18-hướng-dẫn-thiết-kế-uiux)
19. [Backup & Data](#19-backup--data-management)
20. [GitHub Repos Tham Khảo](#20-github-repos-tham-khảo)
21. [Roadmap Phát Triển](#21-roadmap-phát-triển)
22. [Lưu Ý Quan Trọng](#22-lưu-ý-quan-trọng)

---

## 1. TỔNG QUAN DỰ ÁN

### Ý tưởng cốt lõi

Xây dựng ứng dụng Android Native biến cuộc sống thực của người dùng thành một **game RPG cá nhân**, lấy cảm hứng trực tiếp từ **hệ thống (The System)** trong manhwa **Solo Leveling** và các truyện xuyên không. Người dùng trở thành nhân vật chính, được AI dẫn dắt qua các nhiệm vụ hàng ngày để phát triển bản thân trong mọi khía cạnh: thể chất, trí tuệ, kỹ năng nghề nghiệp, tài chính.

### Tên ứng dụng gợi ý

`System Leveling` | `The System` | `Arise — Personal System` | `System: Awaken`

### Phong cách thiết kế tổng thể

- **Giao diện tươi sáng, rực rỡ** — nhiều màu sắc gradient như game MMORPG (MapleStory, Genshin Impact, Maplestory M)
- **Đồ họa 2.5D** — icon, avatar, UI elements có chiều sâu và bóng đổ
- **Particle effects** — confetti, glow, sparkle khi hoàn thành nhiệm vụ, level up
- **Animation mượt mà 60fps** — transitions, micro-interactions
- **Dark mode mặc định** với option Light mode — nền tối giống giao diện hệ thống Solo Leveling

### Repo tham khảo của tác giả

**`https://github.com/danghoangsqtt-sys/danghoang-ebook-v2`** — Đây là web app hiện tại của tác giả (React + TypeScript + Appwrite), có sẵn các module sau mà PHẢI tham khảo logic & data model:

| Module trong repo | Tham khảo cho |
|---|---|
| `pages/Finance.tsx` + `services/financial.ts` | → Module **Finance** trong app Android |
| `pages/Courses.tsx` + `hooks/useCourses.ts` | → Module **Learning Library** trong app Android |
| `pages/Planner.tsx` | → Module **Calendar + To-Do** trong app Android |
| `pages/Dashboard.tsx` | → Logic **gamification, streak, daily goals** |
| `pages/English.tsx` | → Tham khảo cách tích hợp **AI cho learning** |
| `services/gemini.ts` | → Pattern gọi **AI API** |
| `services/appwrite.ts` | → Hybrid storage strategy (Auth + Local) |
| `types.ts` | → TypeScript types → chuyển sang Kotlin data classes |

---

## 2. KIẾN TRÚC KỸ THUẬT

### Tech Stack

| Thành phần | Công nghệ | Ghi chú |
|---|---|---|
| Ngôn ngữ | **Kotlin** | 100% Kotlin, không Java |
| UI Framework | **Jetpack Compose** + Material 3 | Declarative UI |
| Kiến trúc | **MVVM + Clean Architecture** | Tách biệt layer rõ ràng |
| Database Local | **Room Database** (SQLite) | Offline-first |
| Dependency Injection | **Hilt** | Dagger-based |
| Navigation | **Jetpack Navigation Compose** | Type-safe navigation |
| AI Integration | **Anthropic Claude API** / **Google Gemini API** | REST via Retrofit/Ktor |
| HTTP Client | **Ktor Client** hoặc **Retrofit + OkHttp** | Cho AI API calls |
| Animation | **Lottie Compose** + **Compose Animation API** | Particle, transitions |
| Charts/Graphs | **Vico** (Compose-native) | Cho Finance module |
| Calendar | **Kizitonwose Calendar** Compose | Cho Calendar module |
| File Viewer | **AndroidPdfViewer** + **ExoPlayer** + **WebView** | Cho Learning Library |
| Backup | **Export/Import JSON** + optional **Google Drive API** | Data portability |
| Notifications | **WorkManager** + **AlarmManager** | Quest reminders |
| Image Loading | **Coil** (Compose) | Avatar, icons |
| Serialization | **Kotlinx Serialization** | JSON parsing |
| Datastore | **Preferences DataStore** | Settings, preferences |

### Cấu trúc Project (Multi-module Clean Architecture)

```
com.systemleveling.app/
│
├── :app                              # Application module
│   ├── SystemLevelingApp.kt          # Hilt Application
│   ├── MainActivity.kt               # Single Activity
│   └── navigation/
│       └── AppNavGraph.kt            # Root navigation
│
├── :core                             # Shared core module
│   ├── database/
│   │   ├── AppDatabase.kt            # Room database
│   │   ├── dao/                      # All DAOs
│   │   └── entity/                   # All Room entities
│   ├── network/
│   │   ├── AiApiService.kt           # AI API interface
│   │   └── NetworkModule.kt          # Hilt network setup
│   ├── model/                        # Domain models
│   ├── repository/                   # Repository interfaces
│   ├── util/                         # Extensions, constants
│   └── di/                           # Core DI modules
│
├── :feature:onboarding               # Onboarding & Survey
│   ├── ui/                           # Compose screens
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:home                     # Status Panel / Home
│   ├── ui/
│   │   ├── HomeScreen.kt
│   │   ├── StatusPanel.kt            # Character stats display
│   │   ├── ExpBar.kt                 # Animated EXP bar
│   │   └── StatsRadarChart.kt        # Hexagonal stats chart
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:skills                   # Skill Tree
│   ├── ui/
│   │   ├── SkillTreeScreen.kt
│   │   ├── SkillNodeGraph.kt         # Zoomable/pannable graph
│   │   ├── SkillDetailSheet.kt       # Bottom sheet detail
│   │   └── SkillProgressBar.kt
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:quests                   # Quest System
│   ├── ui/
│   │   ├── QuestListScreen.kt
│   │   ├── QuestCard.kt
│   │   ├── QuestCompleteOverlay.kt   # Reward popup
│   │   └── QuestTimeline.kt          # Daily timeline view
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:inventory                # Inventory
│   ├── ui/
│   │   ├── InventoryScreen.kt
│   │   ├── ItemGrid.kt
│   │   └── ItemDetailDialog.kt
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:titles                   # Title/Achievement System
│   ├── ui/
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:finance                  # Finance Management (MỚI)
│   ├── ui/
│   │   ├── FinanceScreen.kt          # Dashboard tổng quan
│   │   ├── TransactionListScreen.kt  # Danh sách giao dịch
│   │   ├── AddTransactionSheet.kt    # Bottom sheet thêm giao dịch
│   │   ├── BudgetTrackerScreen.kt    # Ngân sách theo danh mục
│   │   ├── FinancialGoalsScreen.kt   # Mục tiêu tài chính
│   │   ├── DebtTrackerScreen.kt      # Quản lý nợ
│   │   ├── AiAnalysisScreen.kt       # AI phân tích tài chính
│   │   ├── charts/
│   │   │   ├── CashflowBarChart.kt
│   │   │   ├── ExpensePieChart.kt
│   │   │   └── TrendLineChart.kt
│   │   └── components/
│   │       ├── InsightCard.kt        # Card thống kê nhanh
│   │       ├── NetCashflowHero.kt    # Hero card gradient
│   │       └── SmartMoneyInput.kt    # NLP-powered input
│   ├── viewmodel/
│   │   └── FinanceViewModel.kt
│   ├── repository/
│   │   └── FinanceRepository.kt
│   └── navigation/
│
├── :feature:library                  # Learning Library (MỚI)
│   ├── ui/
│   │   ├── LibraryScreen.kt          # Kho tài liệu chính
│   │   ├── FolderTreeView.kt         # Cây thư mục đệ quy
│   │   ├── DocumentViewer.kt         # Viewer tổng hợp
│   │   ├── PdfViewer.kt              # Xem PDF
│   │   ├── VideoPlayer.kt            # Xem video (YouTube/local)
│   │   ├── PptxViewer.kt             # Xem slide PPTX
│   │   ├── WebContentViewer.kt       # WebView cho Google Drive
│   │   ├── NoteOverlay.kt            # Ghi chú bên cạnh tài liệu
│   │   └── ZenModeWrapper.kt         # Chế độ tập trung
│   ├── viewmodel/
│   │   └── LibraryViewModel.kt
│   ├── repository/
│   │   └── LibraryRepository.kt
│   └── navigation/
│
├── :feature:journal                  # Journal / Nhật ký
│   ├── ui/
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:calendar                 # Calendar + Planner
│   ├── ui/
│   │   ├── CalendarScreen.kt
│   │   ├── MonthView.kt
│   │   ├── WeekView.kt
│   │   ├── DayAgenda.kt
│   │   └── HabitTracker.kt
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:todo                     # To-Do List
│   ├── ui/
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:penalty                  # Penalty System
│   ├── ui/
│   ├── viewmodel/
│   └── navigation/
│
├── :feature:settings                 # Settings
│   ├── ui/
│   ├── viewmodel/
│   └── navigation/
│
└── :designsystem                     # Shared Design System
    ├── theme/
    │   ├── Color.kt                  # Game-inspired palette
    │   ├── Typography.kt             # Custom game fonts
    │   ├── Shape.kt                  # Card shapes, borders
    │   └── Theme.kt                  # SystemLevelingTheme
    ├── components/
    │   ├── GameCard.kt               # Base card với viền glow
    │   ├── RarityBorder.kt           # Border theo rarity color
    │   ├── AnimatedProgressBar.kt    # Thanh tiến trình animated
    │   ├── ParticleEffect.kt         # Particle overlay composable
    │   ├── GlowingText.kt            # Text có hiệu ứng glow
    │   ├── RankBadge.kt              # Badge S/A/B/C/D/E
    │   └── SystemNotification.kt     # Solo Leveling-style popup
    └── animation/
        ├── LevelUpAnimation.kt       # Full-screen level up
        ├── QuestCompleteAnimation.kt  # Quest done celebration
        └── ItemDropAnimation.kt       # Item rarity reveal
```

---

## 3. ONBOARDING — KHỞI TẠO NHÂN VẬT

### Luồng Onboarding (4 bước)

**Bước 0: Cinematic Intro**
- Màn hình đen, text xuất hiện từng ký tự kiểu terminal/glitch:
  ```
  [SYSTEM INITIALIZING...]
  [SCANNING HOST...]
  [COMPATIBLE HOST DETECTED]
  "Bạn đã được chọn. Hệ thống đang khởi tạo..."
  [TAP TO AWAKEN]
  ```
- Hiệu ứng: text glow xanh dương, scan line chạy dọc màn hình, tinh thể particle
- Tap → transition mở ra bước 1

**Bước 1: Thông tin cơ bản**
```
- Tên nhân vật (nickname): TextField với placeholder "Tên chiến binh của bạn"
- Avatar: Chọn từ 20+ avatar RPG có sẵn (chibi style) HOẶC upload ảnh từ gallery
- Ngày sinh: DatePicker
- Giới tính: Male / Female / Other (icon tương ứng)
- Chiều cao (cm): NumberField
- Cân nặng (kg): NumberField
```

**Bước 2: Khảo sát năng lực (Ability Survey)**

Hệ thống đặt 18-24 câu hỏi, chia đều cho 6 chỉ số. Mỗi câu hỏi dạng slider hoặc multiple choice. Kết quả tính thang điểm 1-100 cho mỗi stat.

| Stat | Viết tắt | Ý nghĩa | Ví dụ câu hỏi khảo sát |
|---|---|---|---|
| **Strength** | STR | Sức mạnh thể chất | "Bạn có thể nâng tạ bao nhiêu kg?", "Bạn tập gym mấy buổi/tuần?" |
| **Intelligence** | INT | Trí tuệ, học thuật | "GPA/điểm trung bình?", "Bao nhiêu sách bạn đọc/tháng?" |
| **Agility** | AGI | Nhanh nhẹn, phản xạ | "Bạn chơi thể thao nào?", "Tốc độ gõ phím (WPM)?" |
| **Vitality** | VIT | Sức khỏe, sinh lực | "Bạn ngủ mấy tiếng/ngày?", "Tần suất bệnh/năm?" |
| **Wisdom** | WIS | Trải nghiệm, kỹ năng sống | "Số năm kinh nghiệm làm việc?", "Bạn tự đánh giá kỹ năng mềm?" |
| **Charisma** | CHA | Giao tiếp, sức ảnh hưởng | "Bạn có bao nhiêu bạn thân?", "Bạn tự tin thuyết trình không?" |

Sau khi hoàn thành → **Hiệu ứng xử lý dữ liệu** (loading bar kiểu scan) → Hiển thị kết quả stat giống cửa sổ Status Window trong Solo Leveling với animation từng stat bar fill lên.

**Bước 3: Chọn Class (Nghề nghiệp)**

AI gợi ý 2-3 class phù hợp nhất dựa trên khảo sát, người dùng chọn 1:

| Class | Icon | Stat trội | Ví dụ người dùng thực tế |
|---|---|---|---|
| 🗡️ **Warrior** | Kiếm/Tạ | STR, VIT | VĐV, PT, gym-goer |
| 🧙 **Mage** | Sách/Phép | INT, WIS | Nhà khoa học, researcher |
| 🏹 **Ranger** | Cung/Tốc độ | AGI, INT | Freelancer, startup founder |
| 🛡️ **Guardian** | Khiên | VIT, WIS | Quản lý, team leader |
| 🎭 **Bard** | Đàn/Mic | CHA, AGI | Content creator, artist, MC |
| ⚗️ **Alchemist** | Bình thí nghiệm | INT, AGI | Developer, kỹ sư, engineer |
| 🌿 **Healer** | Cây thuốc | WIS, CHA | Bác sĩ, y tá, counselor |
| 📜 **Scholar** | Cuộn giấy | INT, WIS | Giáo viên, nghiên cứu sinh |

Mỗi class có card lớn hiển thị: icon 2.5D, tên, mô tả ngắn phong cách epic, stat bonuses.

**Bước 4: Chọn Mục tiêu (Goals)**

Người dùng chọn 1 hoặc nhiều mục tiêu dài hạn. Mỗi mục tiêu có icon, tên, và deadline do người dùng đặt.

```
Mục tiêu mẫu (có sẵn):
🏋️ Tăng cơ giảm mỡ — Body Transformation
🧠 Nâng cao trí tuệ — Intelligence Boost  
📚 Đạt IELTS 7.0 — Language Mastery
💻 Thành thạo lập trình nhúng IoT — Tech Skill: IoT Engineering
🎨 Học vẽ digital art — Creative Skill: Digital Art
💰 Xây dựng thu nhập thụ động — Financial Freedom
🧘 Cải thiện sức khỏe tinh thần — Mind & Soul
📐 Ôn thi đại học — Academic Excellence
🎸 Học chơi guitar — Music Skill
🗣️ Nâng cao kỹ năng giao tiếp — Communication Mastery

➕ Tạo mục tiêu tùy chỉnh (Custom Goal): nhập tên + mô tả + deadline
```

Sau khi hoàn thành onboarding → **Cinematic "SYSTEM ACTIVATED!"** → chuyển tới Home Screen.

---

## 4. HOME — BẢNG TRẠNG THÁI (STATUS PANEL)

### Layout chính

```
╔══════════════════════════════════════════╗
║        ⚔️ THE SYSTEM — STATUS ⚔️         ║
╠══════════════════════════════════════════╣
║                                          ║
║   ┌──────────┐   Shadow Monarch          ║
║   │  AVATAR  │   Class: ⚗️ Alchemist     ║
║   │  (2.5D)  │   Level: 15               ║
║   │  chibi   │   Title: "Kẻ Nỗ Lực      ║
║   └──────────┘         Không Ngừng"      ║
║                                          ║
║  ═══ ATTRIBUTES ═══                      ║
║  STR ████████░░░░░░ 67   (+3 ↑)         ║
║  INT ██████████░░░░ 83   (+5 ↑)         ║
║  AGI █████░░░░░░░░░ 42   (+1 ↑)         ║
║  VIT ███████░░░░░░░ 58   (+2 ↑)         ║
║  WIS ████████░░░░░░ 71   (+4 ↑)         ║
║  CHA ██████░░░░░░░░ 49   (+0 ─)         ║
║                                          ║
║  EXP ████████░░░░░░ 8,450 / 10,000      ║
║                                          ║
║  💰 Gold: 2,350     💎 Gem: 15           ║
║  🔥 Streak: 12 days ⚠️ Debt: 0          ║
║                                          ║
║  ┌─────────────────────────────────┐     ║
║  │ 📋 Today's Quests: 3/8 Done    │     ║
║  │ ████████░░░░░░░░ 37.5%         │     ║
║  └─────────────────────────────────┘     ║
║                                          ║
╠══════════════════════════════════════════╣
║ 🏠  📋  🌳  🎒  🏆  💰  📚  📅  ⚙️    ║
╚══════════════════════════════════════════╝
```

### Chi tiết UI Home

- **Avatar**: Ảnh chibi 2.5D trong khung tròn có viền glow theo class color
- **Stats bars**: Gradient color theo giá trị (đỏ<30, cam<50, vàng<70, xanh lá<85, xanh dương≥85), animation fill khi load
- **(+N ↑)**: Hiển thị thay đổi stat so với tuần trước, màu xanh nếu tăng, đỏ nếu giảm
- **EXP bar**: Animation smooth fill, glow pulse khi gần level up
- **Today's Quests mini card**: Tap để chuyển tới Quest screen
- **Background**: Thay đổi theme theo level range (mỗi 10 levels)
  - Lv 1-10: Forest (rừng xanh)
  - Lv 11-20: Desert (sa mạc)
  - Lv 21-30: Ocean (đại dương)
  - Lv 31-40: Mountain (núi tuyết)
  - Lv 41-50: Castle (lâu đài)
  - Lv 51+: Void/Galaxy (vũ trụ)

### Bottom Navigation (9 tabs — có thể scroll horizontal hoặc dùng drawer)

| Icon | Label | Screen |
|---|---|---|
| 🏠 | Home | Status Panel |
| 📋 | Quests | Nhiệm vụ hệ thống |
| 🌳 | Skills | Cây kỹ năng |
| 🎒 | Items | Kho vật phẩm |
| 🏆 | Titles | Danh hiệu |
| 💰 | Finance | Quản lý tài chính |
| 📚 | Library | Kho tài liệu |
| 📅 | Calendar | Lịch + Todo + Journal |
| ⚙️ | Settings | Cài đặt |

**Lưu ý**: Vì có 9 tab, nên sử dụng **Bottom Sheet Navigation** hoặc **5 tab chính ở bottom bar + drawer sidebar cho phần còn lại**. Gợi ý: Bottom bar 5 tabs (Home, Quests, Skills, Finance, More) — "More" mở drawer chứa Items, Titles, Library, Calendar, Settings.

---

## 5. SKILL TREE — CÂY KỸ NĂNG

### Cơ chế

Mỗi Goal tạo ra 1 Skill Tree riêng. AI phân tích goal và sinh ra cây kỹ năng gồm 8-15 skills, chia 3-5 nhánh.

### Hệ thống cấp độ kỹ năng (7 bậc)

| Bậc | Tên | Tên EN | SP cần | Viền màu | Icon gợi ý |
|---|---|---|---|---|---|
| 1 | Nhập Môn | Novice | 0-100 | ⬜ Trắng/Xám | Mầm cây |
| 2 | Sơ Cấp | Apprentice | 100-300 | 🟢 Xanh lá | Cây non |
| 3 | Trung Sơ Cấp | Intermediate | 300-600 | 🔵 Xanh dương | Cây lớn |
| 4 | Trung Cấp | Advanced | 600-1000 | 🟣 Tím | Cây nở hoa |
| 5 | Tiền Cao Cấp | Expert | 1000-1800 | 🟠 Cam | Cây kết trái |
| 6 | Cao Cấp | Master | 1800-3000 | 🔴 Đỏ/Vàng kim | Cây thần |
| 7 | Grand Master | Grand Master | 3000+ | ✨ Holographic/Rainbow | Ngôi sao rực rỡ |

### UI Skill Tree

- **Dạng node graph zoomable/pannable** — tham khảo: Path of Exile Passive Tree, Final Fantasy X Sphere Grid
- Mỗi skill = node hình **hexagon** hoặc **tròn** với icon bên trong
- Viền node = màu theo cấp độ hiện tại
- Đường nối giữa các node = prerequisite lines (glow animation khi đã mở khóa)
- Skill chưa unlock = mờ + icon khóa
- **Tap node** → mở **Bottom Sheet** chi tiết:

```
┌────────────────────────────────────────┐
│ ✍️ Essay Structure                      │
│ ────────────────────────────────────── │
│ "Khả năng kiến tạo một bài luận với   │
│  cấu trúc chặt chẽ, logic xuyên suốt │
│  và lập luận sắc bén như thanh kiếm   │
│  của chiến binh thượng cổ."           │
│                                        │
│ Cấp độ: 🟢 Sơ Cấp (Apprentice)       │
│ SP: ████████░░░░░░░░ 187 / 300        │
│ → Tiếp theo: Trung Sơ Cấp (cần 113)  │
│                                        │
│ 📊 SP gần đây:                         │
│ +15 SP — Quest #42 "Morning Writing"  │
│ +10 SP — Daily Practice               │
│ +20 SP — Boss: Essay Challenge         │
│                                        │
│ 🔗 Yêu cầu: Grammar Basics Lv.3      │
│ 🔓 Mở khóa: Advanced Argumentation    │
└────────────────────────────────────────┘
```

### Ví dụ Skill Tree: "IELTS 7.0 Mastery"

```
                    🎯 IELTS 7.0
                   /    |    \    \
            📖 Reading  ✍ Writing  🎧 Listening  🗣 Speaking
            /    \       /    \        |    \        /    \
     Skimming  Vocab  Task1  Task2  Note  Accent  Fluency  Discussion
         |              |              |              |
    Inference      Coherence      Speed         Pronunciation
```

### Ví dụ Skill Tree: "IoT Engineering"

```
                    💻 IoT Master
                   /    |    \
          🔌 Hardware  📡 Network  💾 Software
          /    \        /    \       /    \
    Arduino  Sensors  WiFi  MQTT  Python  C/C++
       |       |       |      |      |       |
    Circuit  ADC/DAC  TCP/IP Cloud  Flask  Embedded
```

---

## 6. QUEST SYSTEM — NHIỆM VỤ HỆ THỐNG

### Đây là CORE FEATURE quan trọng nhất

Mỗi ngày, AI tạo danh sách nhiệm vụ cá nhân hóa dựa trên goals, skill levels, lịch sử, và calendar của người dùng.

### Quy trình sinh quest

1. Trigger: Mỗi ngày lúc **thời điểm user cài đặt** (mặc định 00:00), hoặc khi user mở app lần đầu trong ngày
2. AI API nhận: goals, current skills/levels, 7-day completion history, debt points, calendar events, day-of-week
3. AI output: JSON array 6-10 quests, sắp xếp theo timeline sáng→tối, tránh conflict với calendar

### Cấu trúc Quest

```kotlin
data class Quest(
    val id: String,                    // "Q-2026-05-20-001"
    val title: String,                 // "Morning Warrior Training"
    val description: String,           // Mô tả chi tiết
    val type: QuestType,               // DAILY, WEEKLY, BOSS, PENALTY, EVENT, SIDE
    val difficulty: QuestRank,         // S, A, B, C, D, E
    val category: String,              // "fitness", "language", "tech", "finance"...
    val date: LocalDate,
    val timeWindow: TimeWindow,        // start, end, durationMinutes
    val subtasks: List<String>,        // Danh sách bước cụ thể
    val rewards: QuestRewards,         // exp, gold, skillPoints, itemChance
    val penaltyOnFail: QuestPenalty,   // expLoss, debtPoints
    val relatedGoalId: String?,
    val relatedSkillIds: List<String>,
    val status: QuestStatus            // PENDING, IN_PROGRESS, COMPLETED, FAILED, EXPIRED
)
```

### Bảng loại nhiệm vụ

| Loại | Icon/Color | Tần suất | Mô tả |
|---|---|---|---|
| 📋 **Daily Quest** | Xanh lá | Hàng ngày | Quest tự động do AI gen, 6-10/ngày |
| 📅 **Weekly Quest** | Xanh dương | Hàng tuần | Khó hơn daily, thưởng x3, gen vào thứ 2 |
| 👹 **Boss Quest** | Đỏ/Tím + glow | Theo milestone | Thử thách lớn (thi thật, chạy 10km, ship project) |
| ⚠️ **Penalty Quest** | Đỏ đen | Khi có nợ | Quest trả nợ, không thưởng EXP |
| 🎉 **Event Quest** | Vàng kim | Sự kiện đặc biệt | Tết, sinh nhật, milestone cá nhân |
| 🔄 **Side Quest** | Xám | Tùy chọn | Quest phụ do user tạo hoặc AI gợi ý |

### Bảng xếp hạng quest (Rank)

| Rank | Màu | EXP range | Ví dụ |
|---|---|---|---|
| **E** | Trắng | 20-50 | Uống 2L nước, đi bộ 15 phút |
| **D** | Xanh lá | 50-100 | Đọc 30 phút, ôn 20 từ vựng |
| **C** | Xanh dương | 100-200 | Chạy 3km, viết 1 đoạn essay |
| **B** | Tím | 200-400 | Tập gym 1 giờ, làm 1 bộ listening |
| **A** | Cam | 400-800 | Hoàn thành 1 project nhỏ, full mock test |
| **S** | Đỏ + glow | 800-2000 | Boss Quest: Thi IELTS thật, deploy app |

### UI Quest Screen

**Dạng timeline dọc (giống daily planner kiểu game)**:

```
═══ THỨ BA — 20/05/2026 ═══ 🔥 Streak: 12

  06:00 ┃ 🟢 [E] Uống nước buổi sáng           ✅ Done
  06:30 ┃ 🔵 [C] Morning Warrior Training       ⏳ 06:30-07:15
        ┃     → Chạy 3km + 30 hít đất
        ┃     → +150 EXP  +10 SP(Cardio)
  08:00 ┃ 🟣 [B] Deep Focus: IELTS Reading      ○ Pending
        ┃     → 2 passages + 40 câu hỏi
        ┃     → +250 EXP  +15 SP(Reading)
  10:00 ┃ 🔵 [C] Code Practice: MQTT Protocol   ○ Pending
        ┃     → Hoàn thành tutorial section 3
        ┃     → +180 EXP  +12 SP(Network)
  12:00 ┃ 🟢 [D] Vocabulary Builder              ○ Pending
        ┃     → Học 15 từ mới + ôn 30 từ cũ
  14:00 ┃ 🟣 [B] Essay Writing Practice          ○ Pending
        ┃     → Viết Task 2: Technology topic
  16:00 ┃ 🟢 [D] Financial Check-in              ○ Pending
        ┃     → Ghi nhận chi tiêu hôm nay
  19:00 ┃ 🔵 [C] Evening Workout                 ○ Pending
  21:00 ┃ 🟢 [E] Reflection & Journal            ○ Pending

  ─── Progress: ████░░░░░░░░ 1/9 (11%) ───
```

### Popup khi hoàn thành Quest

```
╔══════════════════════════════════════╗
║                                      ║
║      ⚔️  QUEST COMPLETE!  ⚔️         ║
║      ═══════════════════             ║
║                                      ║
║   "Morning Warrior Training"         ║
║   Rank: C  ⏱️ Hoàn thành đúng hạn   ║
║                                      ║
║   ─── REWARDS ACQUIRED ───           ║
║   ✨ +150 EXP                        ║
║   💰 +50 Gold                        ║
║   🔮 +10 SP → Cardio Endurance       ║
║   🔮 +8 SP  → Core Strength          ║
║                                      ║
║   🎁 Item Drop!                      ║
║   ┌──────────────────────┐           ║
║   │ 🟢 Small Energy Potion │         ║
║   │ (Uncommon)             │         ║
║   └──────────────────────┘           ║
║                                      ║
║        [ ✨ CLAIM REWARDS ]          ║
║                                      ║
╚══════════════════════════════════════╝
```

Animation: card flip cho item reveal, particle burst, EXP bar fill animation, sound indicator icon.

---

## 7. PENALTY SYSTEM — HỆ THỐNG XỬ PHẠT

Lấy cảm hứng trực tiếp từ **Penalty Quest** trong Solo Leveling.

### Cơ chế chi tiết

| Điều kiện | Hậu quả |
|---|---|
| Không hoàn thành 1 quest đúng hạn | +1 Debt Point, -30% EXP của quest đó |
| Tích lũy ≥ 3 Debt Points | Kích hoạt **Penalty Zone** (UI cảnh báo đỏ) |
| Tích lũy ≥ 5 Debt Points | Mất 10% Gold hiện có |
| Tích lũy ≥ 10 Debt Points mà không trả | **Level Down** (-1 level) + System Warning toàn màn hình |
| Không trả nợ trong 14 ngày | Mất 1 Title đang equip (nếu có) |

### Trả nợ

1. Người dùng vào **Penalty Screen** → chọn ngày cụ thể để trả nợ
2. Hệ thống gen **Penalty Quests** cho ngày đó — khó hơn 1.5x quest bình thường
3. Hoàn thành Penalty Quest = trừ Debt Points tương ứng
4. Penalty Quest **KHÔNG có thưởng EXP/Gold** — chỉ trả nợ
5. Nếu hoàn thành tất cả penalty quests trong 1 ngày → **Bonus**: nhận lại 50% Gold đã mất

### UI Penalty

- Khi có debt > 0: **viền đỏ nhấp nháy nhẹ** quanh Status Panel ở Home
- Badge đỏ trên bottom nav icon
- Notification kiểu Solo Leveling:
  ```
  ⚠️ [SYSTEM WARNING]
  "Người chơi có 5 Debt Points chưa thanh toán.
   Chọn ngày trả nợ hoặc đối mặt với hậu quả.
   Thời hạn còn lại: 9 ngày."
  
  [CHỌN NGÀY TRẢ NỢ]  [BỎ QUA]
  ```

---

## 8. INVENTORY — KHO VẬT PHẨM

### Phân loại rarity

| Rarity | Tên VN | Viền màu | Drop rate | Ví dụ |
|---|---|---|---|---|
| **Common** | Thường | ⬜ Trắng | 50% | Small Potion, Basic Scroll |
| **Uncommon** | Hiếm | 🟢 Xanh lá | 25% | Focus Crystal, Speed Boots |
| **Rare** | Quý hiếm | 🔵 Xanh dương | 15% | Knowledge Tome, Warrior's Gauntlet |
| **Epic** | Sử thi | 🟣 Tím | 7% | Phoenix Feather, Mind Stone |
| **Legendary** | Huyền thoại | 🟠 Vàng/Cam | 2.5% | Excalibur, Crown of Wisdom |
| **Mythic** | Thần thoại | 🔴 Đỏ + animated glow | 0.5% | Shadow Monarch's Dagger |

### UI Inventory

- **Grid layout 4-5 cột** giống inventory MMORPG
- Mỗi ô vuông: icon vật phẩm + viền rarity + stack counter
- Tap → popup detail: tên, mô tả lore (AI viết phong cách RPG), rarity badge, ngày nhận, quest liên quan
- Filter: All / theo rarity / theo category
- Sort: Newest / Rarity / Name
- Item Legendary+ có **shimmer/glow animation** liên tục

---

## 9. TITLE SYSTEM — KHO DANH HIỆU

### Danh hiệu mẫu

| Điều kiện kích hoạt | Tên danh hiệu | Rarity |
|---|---|---|
| Chạy bộ 7 ngày liên tiếp | 🏃 "Kẻ Nỗ Lực Không Ngừng Bằng Đôi Chân" | Rare |
| Đọc sách 30 ngày liên tiếp | 📚 "Hiền Giả Của Tri Thức Vô Tận" | Epic |
| Hoàn thành 100 quests tổng | ⚔️ "Chiến Binh Trăm Trận" | Epic |
| 0 Debt Points trong 30 ngày | 🛡️ "Người Kỷ Luật Thép" | Legendary |
| Level đạt 50 | 🌟 "Kẻ Vượt Qua Giới Hạn" | Legendary |
| Master 1 skill | 🔮 "Bậc Thầy [Tên Skill]" | Mythic |
| Hoàn thành Boss Quest đầu tiên | 👑 "Kẻ Chinh Phục Dungeon" | Rare |
| Trả hết penalty trong 1 ngày | ⚡ "Kẻ Chuộc Lỗi" | Uncommon |
| Tiết kiệm đạt mục tiêu tài chính | 💰 "Nhà Buôn Của Vương Quốc" | Epic |
| Học 500 từ vựng | 📖 "Pháp Sư Ngôn Từ" | Rare |
| Streak 100 ngày | 🔥 "Ngọn Lửa Bất Diệt" | Mythic |

### AI sinh title động

AI tự tạo title mới khi phát hiện pattern hành vi đặc biệt — ví dụ: user code 5 tiếng liên tục → "Kẻ Đắm Chìm Trong Dòng Code Bất Tận".

### UI Title Gallery

- Card display với **holographic shimmer effect** cho title đã đạt
- Title chưa đạt: card mờ + progress bar + điều kiện
- **Equip button**: chọn 1 title hiển thị dưới tên nhân vật ở Home
- Notification popup kiểu Solo Leveling khi đạt title mới:
  ```
  🏆 [NEW TITLE ACQUIRED!]
  "Kẻ Nỗ Lực Không Ngừng Bằng Đôi Chân"
  [EQUIP]  [VIEW COLLECTION]
  ```

---

## 10. FINANCE — QUẢN LÝ TÀI CHÍNH (MỚI)

> **Tham khảo trực tiếp**: `pages/Finance.tsx`, `services/financial.ts`, `components/TransactionList.tsx`, `components/AIPlanModal.tsx`, `components/InvestmentDashboard.tsx`, `components/SmartMoneyInput.tsx` trong repo `danghoang-ebook-v2`

### Tổng quan

Module quản lý tài chính được **gamify** — tích hợp vào hệ thống RPG. Mỗi hành vi tài chính tốt (tiết kiệm, đúng budget) = nhận EXP/Gold. Hành vi xấu (overspend) = Debt Points.

### Các sub-screens

**10.1 Finance Dashboard (Trang tổng quan)**

```
╔══════════════════════════════════════╗
║  💰 FINANCE — Tháng 05/2026         ║
╠══════════════════════════════════════╣
║                                      ║
║  ┌─────────┐ ┌─────────┐ ┌────────┐ ║
║  │Tiết kiệm│ │Chi TB   │ │Top chi │ ║
║  │  32%    │ │150k/ngày│ │Ăn uống │ ║
║  │ 🟢 ↑5% │ │ 🟡 ↑2% │ │ 35%    │ ║
║  └─────────┘ └─────────┘ └────────┘ ║
║                                      ║
║  ┌──────────────────────────────┐    ║
║  │  NET CASHFLOW: +2,350,000đ  │    ║
║  │  ████████████████ gradient   │    ║
║  │  Thu: 8.5M    Chi: 6.15M    │    ║
║  └──────────────────────────────┘    ║
║                                      ║
║  📊 [Bar Chart: Thu/Chi theo ngày]   ║
║  🥧 [Pie Chart: Phân bổ chi tiêu]   ║
║                                      ║
║  [➕ Thêm giao dịch]                 ║
║  [📋 Xem tất cả giao dịch]          ║
║  [🎯 Mục tiêu tài chính]            ║
║  [📒 Sổ nợ]                          ║
║  [🤖 AI Phân tích]                   ║
║                                      ║
╚══════════════════════════════════════╝
```

**10.2 Giao dịch (Transactions)**
- List giao dịch thu/chi, grouped by ngày
- Mỗi giao dịch: icon category, tên, số tiền (xanh=thu, đỏ=chi), ghi chú
- **SmartMoneyInput**: nhập tự nhiên "ăn phở 50k" → AI parse thành: category=Ăn uống, amount=50,000, note=phở
- Swipe delete, edit
- Filter: tháng, category, thu/chi

**10.3 Ngân sách (Budget Tracker)**
- Tham khảo `BudgetCategory` type trong repo
- Card cho mỗi danh mục (Ăn uống, Đi lại, Giải trí, Học tập, ...)
- Progress bar: đã chi / budget
- Rollover: ngân sách dư từ tháng trước cộng dồn
- Cảnh báo khi vượt 80%, 100% budget

**10.4 Mục tiêu tài chính (Financial Goals)**
- Tiết kiệm mua laptop, quỹ du lịch, quỹ khẩn cấp, ...
- Progress bar + target amount + deadline
- Tích hợp với Quest System: AI có thể gen quest "Hôm nay không mua đồ uống → +50 Gold"

**10.5 Sổ nợ (Debt Tracker)**
- Nợ phải thu (người khác nợ mình)
- Nợ phải trả (mình nợ người khác)
- Reminder khi gần deadline

**10.6 AI Phân tích tài chính**
- Tham khảo `AIPlanModal.tsx` trong repo
- AI nhận data tài chính → phân tích xu hướng, gợi ý cắt giảm, lập kế hoạch
- Output: báo cáo text + action items có thể chuyển thành Quest

### Gamification tích hợp Finance

| Hành vi tài chính | Reward/Penalty |
|---|---|
| Ghi nhận chi tiêu hàng ngày đúng hạn | +30 EXP, +10 Gold |
| Hoàn thành tháng dưới budget | +500 EXP, +200 Gold, có thể nhận title |
| Đạt mục tiêu tiết kiệm | Boss Quest Complete! + item rarity Epic+ |
| Vượt budget 1 danh mục | Cảnh báo hệ thống (không phạt nặng) |
| Vượt budget 3+ danh mục trong tháng | +1 Debt Point |

---

## 11. LEARNING LIBRARY — KHO TÀI LIỆU HỌC TẬP (MỚI)

> **Tham khảo trực tiếp**: `pages/Courses.tsx`, `hooks/useCourses.ts` trong repo `danghoang-ebook-v2` — cấu trúc CourseNode (tree folder/file), multi-format viewer, Zen Mode, note overlay.

### Tổng quan

Kho tài liệu cho phép người dùng **lưu trữ và học trực tiếp** trên app. Hỗ trợ: PDF, PPTX (slide), Video YouTube (embed), Google Drive links, và ghi chú cá nhân.

### Cấu trúc dữ liệu (tham khảo CourseNode trong repo)

```kotlin
data class LibraryNode(
    val id: String,
    val name: String,
    val type: NodeType,          // FOLDER, PDF, PPTX, VIDEO, GDRIVE, NOTE, LINK
    val parentId: String?,       // null = root
    val content: LibraryContent?,
    val order: Int,
    val isPinned: Boolean = false,
    val tags: List<String> = emptyList(),
    val lastOpenedAt: Long? = null,
    val progress: Float = 0f,    // 0.0 - 1.0 (reading/watching progress)
    val relatedGoalId: String?,  // liên kết với Goal nào
    val relatedSkillIds: List<String> = emptyList()
)

data class LibraryContent(
    val filePath: String?,        // Local file path (PDF, PPTX)
    val url: String?,             // YouTube URL, Google Drive URL
    val mimeType: String?,
    val fileSize: Long?,
    val thumbnailPath: String?,
    val notes: String? = null     // Ghi chú cá nhân cho tài liệu này
)

enum class NodeType {
    FOLDER, PDF, PPTX, VIDEO_YOUTUBE, VIDEO_LOCAL, GDRIVE, NOTE, WEB_LINK
}
```

### Các sub-screens

**11.1 Library Main Screen — Cây thư mục**

```
╔══════════════════════════════════════╗
║  📚 LEARNING LIBRARY                ║
║  ┌────────────────────────────────┐  ║
║  │ 🔍 Tìm kiếm tài liệu...      │  ║
║  └────────────────────────────────┘  ║
║                                      ║
║  📌 Đang học gần đây:                ║
║  ┌──────────┐ ┌──────────┐          ║
║  │📕 IELTS  │ │📘 IoT    │          ║
║  │Cam 18    │ │Tutorial  │          ║
║  │████░░ 65%│ │██░░░░ 30%│          ║
║  └──────────┘ └──────────┘          ║
║                                      ║
║  📁 Thư mục                          ║
║  ├── 📁 IELTS Materials              ║
║  │   ├── 📁 Reading                  ║
║  │   │   ├── 📕 Cambridge 18.pdf     ║
║  │   │   └── 📕 Practice Set 1.pdf   ║
║  │   ├── 📁 Listening                ║
║  │   │   └── 🎥 Aptis Listening.yt   ║
║  │   └── 📄 Vocabulary List.note     ║
║  ├── 📁 IoT & Embedded               ║
║  │   ├── 📊 Arduino Basics.pptx      ║
║  │   ├── 📕 MQTT Protocol.pdf        ║
║  │   └── 🎥 ESP32 Tutorial.yt        ║
║  ├── 📁 Google Drive Docs             ║
║  │   └── 🔗 Project Notes (GDrive)   ║
║  └── ➕ Thêm tài liệu / thư mục     ║
║                                      ║
╚══════════════════════════════════════╝
```

**11.2 Document Viewer**

| Loại tài liệu | Viewer | Tính năng |
|---|---|---|
| **PDF** | AndroidPdfViewer hoặc PdfRenderer | Bookmark, zoom, page navigation, reading progress |
| **PPTX** | Convert → images rồi hiển thị dạng slide carousel | Slide navigation, zoom |
| **YouTube Video** | WebView embed YouTube iframe | Play/pause, timestamp bookmark |
| **Google Drive** | WebView mở Google Drive viewer | View only |
| **Note** | Rich text editor nội bộ | Markdown support, auto-save |
| **Web Link** | WebView mở URL | Bookmark, reading mode |

**11.3 Tính năng phụ trợ**

- **Zen Mode**: ẩn tất cả UI chrome, chỉ hiện nội dung tài liệu (tham khảo Zen Mode trong repo)
- **Note Overlay**: drawer kéo từ bên phải, ghi chú song song khi đọc/xem
- **Progress Tracking**: tự động lưu tiến trình đọc PDF (trang hiện tại), xem video (timestamp)
- **Tag System**: gắn tag cho tài liệu, lọc theo tag
- **Liên kết với Skill Tree**: tài liệu gắn với Goal/Skill → đọc/xem xong = +SP cho skill tương ứng

### Gamification tích hợp Library

| Hành vi học tập | Reward |
|---|---|
| Đọc PDF ≥ 30 phút liên tục | +80 EXP, +5 SP (skill liên quan) |
| Xem hết 1 video tutorial | +60 EXP, +8 SP |
| Hoàn thành đọc 1 tài liệu (100%) | +200 EXP, item drop chance |
| Ghi 5+ notes trong 1 ngày | +50 EXP |
| Hoàn thành 1 folder tài liệu | Mini Boss Quest reward |

### Thêm tài liệu

- **Import PDF/PPTX**: từ bộ nhớ thiết bị (file picker)
- **YouTube URL**: paste link → auto-fetch thumbnail + title
- **Google Drive URL**: paste sharing link
- **Tạo Note**: tạo ghi chú trực tiếp
- **AI gợi ý tài liệu**: dựa trên Goal, AI gợi ý tên sách/khóa học/video (chỉ gợi ý, user tự tìm & thêm)

---

## 12. JOURNAL — NHẬT KÝ

- Rich text editor đơn giản (Markdown hỗ trợ)
- Ghi chú hàng ngày, auto-tag theo ngày
- **Mood selector**: 5 emoji (😄 😊 😐 😟 😢)
- AI tóm tắt tuần/tháng từ nhật ký
- UI: phong cách sổ tay RPG (giấy da, vintage feel) — nhưng vẫn giữ tông sáng colorful
- Mỗi entry journal = +20 EXP (khuyến khích viết nhật ký)

---

## 13. CALENDAR — LỊCH LÀM VIỆC

> Tham khảo: `pages/Planner.tsx` trong repo — CalendarEvent, Habit Tracker, Agenda Sidebar

### Features

- **Month View**: grid tháng, chấm màu cho mỗi ngày (🟢=all quests done, 🟡=partial, 🔴=failed, ⭐=streak day)
- **Week View**: 7 cột, chia theo giờ, hiển thị quest blocks + calendar events
- **Day View / Agenda**: timeline chi tiết + checklist
- **Quest overlay**: tự động hiển thị quests trên calendar
- **Create Event**: tạo sự kiện riêng (họp, hẹn, deadline, ...)
- **Habit Tracker**: card thói quen + 7 chấm tròn lịch sử 7 ngày gần nhất (tham khảo Planner trong repo)
- **Streak counter**: hiển thị streak dài nhất và streak hiện tại

---

## 14. TO-DO LIST

Tách biệt với Quest System — để người dùng tạo task cá nhân riêng.

- Checkbox, priority tags (🔴Urgent, 🟠High, 🟡Medium, 🟢Low)
- Deadline + reminder notification
- Drag-to-reorder
- Optional: AI gợi ý chuyển to-do thành Quest để nhận EXP (button "Convert to Quest")
- Completed tasks: strikethrough + ✅

---

## 15. SETTINGS

| Mục | Chi tiết |
|---|---|
| **Profile** | Chỉnh sửa avatar, nickname, re-survey stats, change class |
| **Goals** | Thêm/sửa/xóa mục tiêu, điều chỉnh deadline |
| **Quest Config** | Thời gian gen quest, số lượng quest/ngày (6-12), độ khó mặc định |
| **Notifications** | Bật/tắt, thời gian nhắc cho quest/calendar/todo/penalty |
| **Theme** | Dark/Light, chọn background theme, accent color |
| **AI API Key** | Nhập Claude API key hoặc Gemini API key |
| **Language** | Tiếng Việt / English |
| **Penalty Config** | Mức phạt (nhẹ/vừa/nặng), grace period (1-3 ngày) |
| **Finance Config** | Đơn vị tiền tệ (VND/USD/...), ngày bắt đầu tháng tài chính |
| **Backup & Restore** | Export JSON, Import file, optional Google Drive sync |
| **About** | Version, credits, licenses |
| **Reset** | Xóa toàn bộ dữ liệu (cần xác nhận 2 lần) |

---

## 16. AI INTEGRATION — PROMPT TEMPLATES

### 16.1 Daily Quest Generation

```
Bạn là một AI hệ thống trong game RPG thực tế (phong cách Solo Leveling). 
Tạo danh sách nhiệm vụ hàng ngày cho người chơi.

THÔNG TIN NGƯỜI CHƠI:
- Tên: {nickname}, Class: {class}, Level: {level}
- Mục tiêu: {goals_list_with_deadlines}
- Skill Tree hiện tại: {skills_with_current_SP_and_levels}
- Lịch sử 7 ngày: {completion_rate_per_day, failed_quests}
- Debt Points: {debt_points}
- Hôm nay: {date}, {day_of_week}
- Calendar events hôm nay: {events_with_times}
- Sở thích/Ghi chú: {user_notes}

YÊU CẦU:
1. Tạo {quest_count} quests (6-10), trải đều sáng→tối
2. Mỗi quest phải CỤ THỂ, ĐO LƯỜNG ĐƯỢC, có thời gian rõ ràng
3. CÂN BẰNG giữa các mục tiêu khác nhau
4. KHÔNG conflict với calendar events
5. Điều chỉnh độ khó phù hợp level + lịch sử (nếu user hay fail → giảm nhẹ)
6. Tên quest phải THÚ VỊ kiểu game RPG
7. Gán rewards phù hợp (EXP, Gold, SP cho skill liên quan, item drop chance)
8. Nếu user có debt > 0, thêm 1-2 penalty quest slots

OUTPUT: JSON array theo schema:
[{
  "id": "Q-YYYY-MM-DD-NNN",
  "title": "string (RPG-style name)",
  "description": "string",
  "type": "daily|weekly|boss|side",
  "difficulty": "E|D|C|B|A|S",
  "category": "string",
  "time_start": "HH:MM",
  "time_end": "HH:MM",
  "duration_minutes": number,
  "subtasks": ["string"],
  "rewards": {
    "exp": number,
    "gold": number,
    "skill_points": [{"skill_id": "string", "amount": number}],
    "item_drop": {"item_template": "string", "rarity": "string", "chance": 0.0-1.0} | null
  },
  "penalty_on_fail": {"exp_loss": number, "debt_points": number},
  "related_goal_id": "string",
  "related_skill_ids": ["string"]
}]
```

### 16.2 Skill Tree Generation

```
Bạn là AI thiết kế Skill Tree cho game RPG giáo dục. 
Phân tích mục tiêu và tạo cây kỹ năng chi tiết.

MỤC TIÊU: {goal_name} — {goal_description}
DEADLINE: {deadline}
TRÌNH ĐỘ HIỆN TẠI: {survey_results_relevant_stats}

YÊU CẦU:
1. Tạo 8-15 skills, chia 3-5 nhánh chính
2. Mỗi skill: tên, mô tả PHONG CÁCH RPG (epic, thú vị, ngắn gọn), icon gợi ý, prerequisite
3. Skills phải THỰC TẾ và ĐO LƯỜNG được qua quest completion
4. Có progression logic (cơ bản → nâng cao)
5. Mô tả giống mô tả skill trong game
   Ví dụ: "Khai phá sức mạnh ngôn từ, biến ý tưởng thành chuỗi câu chữ sắc bén 
   như thanh kiếm của chiến binh thượng cổ"

OUTPUT: JSON array skills + tree structure
```

### 16.3 Title Generation

```
Tạo danh hiệu RPG dựa trên hành vi người chơi.

HÀNH VI: {pattern_description}
VÍ DỤ: "Hoàn thành 7 quest chạy bộ liên tiếp"

YÊU CẦU:
- Tên danh hiệu PHẢI epic, thú vị, gây cảm giác tự hào, kiểu game
- Tiếng Việt, có thể kèm tiếng Anh
- Phân loại rarity (Common → Mythic)

OUTPUT: JSON { "name": "string", "name_en": "string", "description": "string", 
              "rarity": "string", "icon_emoji": "string" }
```

### 16.4 Financial Analysis (tham khảo gemini.ts trong repo)

```
Phân tích tài chính cá nhân và đưa ra gợi ý.

DỮ LIỆU:
- Giao dịch tháng này: {transactions_summary}
- Budget vs Actual: {budget_comparison}
- Mục tiêu tài chính: {financial_goals}
- Nợ hiện tại: {debts}

OUTPUT:
1. Tóm tắt tình hình (2-3 câu)
2. Điểm mạnh (1-2)
3. Cần cải thiện (1-2)
4. Gợi ý hành động cụ thể (3-5 items, có thể chuyển thành Quest)
5. Dự báo tháng tới
```

---

## 17. DATABASE SCHEMA (Room)

```kotlin
@Database(
    entities = [
        UserEntity::class,
        GoalEntity::class,
        SkillEntity::class,
        QuestEntity::class,
        InventoryItemEntity::class,
        TitleEntity::class,
        TransactionEntity::class,        // MỚI: Finance
        BudgetCategoryEntity::class,     // MỚI: Finance
        FinancialGoalEntity::class,      // MỚI: Finance
        DebtEntity::class,              // MỚI: Finance
        LibraryNodeEntity::class,        // MỚI: Library
        LibraryNoteEntity::class,        // MỚI: Library
        JournalEntryEntity::class,
        CalendarEventEntity::class,
        HabitEntity::class,
        TodoEntity::class,
        PenaltyLogEntity::class,
        QuestTemplateEntity::class       // Offline fallback templates
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase()
```

### Entities chính (tóm tắt — triển khai đầy đủ khi code)

```kotlin
// ═══ USER ═══
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val nickname: String, val avatarPath: String?, val className: String,
    val level: Int = 1, val currentExp: Long = 0, val expToNextLevel: Long = 1000,
    val gold: Long = 0, val gems: Int = 0, val equippedTitleId: String?,
    val str: Int, val int_stat: Int, val agi: Int, val vit: Int, val wis: Int, val cha: Int,
    val debtPoints: Int = 0, val streakDays: Int = 0, val totalQuestsCompleted: Int = 0,
    val createdAt: Long, val updatedAt: Long
)

// ═══ GOALS ═══
@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String, val name: String, val description: String,
    val category: String, val deadline: Long?, val isActive: Boolean = true, val createdAt: Long
)

// ═══ SKILLS ═══
@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val id: String, val goalId: String, val name: String,
    val description: String, val iconName: String,
    val currentSP: Int = 0, val currentLevel: Int = 1, // 1-7
    val levelName: String = "Nhập Môn",
    val parentSkillId: String?, val isUnlocked: Boolean = true, val orderIndex: Int
)

// ═══ QUESTS ═══
@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String, val title: String, val description: String,
    val type: String, val difficulty: String, val category: String,
    val date: Long, val startTime: String?, val endTime: String?, val durationMinutes: Int?,
    val subtasksJson: String, val rewardsJson: String,
    val status: String = "pending", val relatedGoalId: String?,
    val relatedSkillIdsJson: String?, val completedAt: Long?, val createdAt: Long
)

// ═══ INVENTORY ═══
@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String, val name: String, val description: String,
    val iconName: String, val rarity: String, val category: String,
    val quantity: Int = 1, val obtainedAt: Long, val questId: String?
)

// ═══ TITLES ═══
@Entity(tableName = "titles")
data class TitleEntity(
    @PrimaryKey val id: String, val name: String, val nameEn: String?,
    val description: String, val condition: String, val rarity: String,
    val iconEmoji: String, val isUnlocked: Boolean = false,
    val unlockedAt: Long?, val category: String
)

// ═══ FINANCE (MỚI) ═══
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String, val type: String, // "income" | "expense"
    val amount: Long, // Lưu theo đơn vị nhỏ nhất (VND: đồng)
    val categoryId: String, val categoryName: String, val categoryIcon: String,
    val note: String?, val date: Long, val isRecurring: Boolean = false,
    val createdAt: Long
)

@Entity(tableName = "budget_categories")
data class BudgetCategoryEntity(
    @PrimaryKey val id: String, val name: String, val icon: String,
    val monthlyBudget: Long, val color: String,
    val rolloverEnabled: Boolean = false, val rolloverAmount: Long = 0
)

@Entity(tableName = "financial_goals")
data class FinancialGoalEntity(
    @PrimaryKey val id: String, val name: String, val targetAmount: Long,
    val currentAmount: Long = 0, val deadline: Long?, val icon: String,
    val isCompleted: Boolean = false, val createdAt: Long
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey val id: String, val type: String, // "receivable" | "payable"
    val personName: String, val amount: Long, val remainingAmount: Long,
    val note: String?, val deadline: Long?, val isSettled: Boolean = false, val createdAt: Long
)

// ═══ LIBRARY (MỚI) ═══
@Entity(tableName = "library_nodes")
data class LibraryNodeEntity(
    @PrimaryKey val id: String, val name: String, val type: String,
    val parentId: String?, val filePath: String?, val url: String?,
    val mimeType: String?, val fileSize: Long?, val thumbnailPath: String?,
    val notes: String?, val isPinned: Boolean = false, val tagsJson: String?,
    val progress: Float = 0f, val lastOpenedAt: Long?,
    val relatedGoalId: String?, val relatedSkillIdsJson: String?,
    val orderIndex: Int, val createdAt: Long
)

// ═══ JOURNAL ═══
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String, val date: Long, val content: String,
    val mood: String?, val tagsJson: String?, val createdAt: Long, val updatedAt: Long
)

// ═══ CALENDAR ═══
@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String, val title: String, val description: String?,
    val startTime: Long, val endTime: Long, val isAllDay: Boolean = false,
    val color: String?, val reminderMinutes: Int?,
    val isQuestRelated: Boolean = false, val questId: String?
)

// ═══ HABITS ═══
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String, val name: String, val icon: String,
    val frequency: String, // "daily", "weekly", "custom"
    val targetPerDay: Int = 1, val color: String, val isActive: Boolean = true,
    val createdAt: Long
)

// ═══ TODOS ═══
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: String, val title: String, val description: String?,
    val priority: String, val deadline: Long?,
    val isCompleted: Boolean = false, val completedAt: Long?, val createdAt: Long
)

// ═══ PENALTY ═══
@Entity(tableName = "penalty_logs")
data class PenaltyLogEntity(
    @PrimaryKey val id: String, val questId: String, val debtPointsAdded: Int,
    val reason: String, val scheduledPayDate: Long?,
    val isPaid: Boolean = false, val paidAt: Long?, val createdAt: Long
)
```

---

## 18. HƯỚNG DẪN THIẾT KẾ UI/UX

### Color Palette

```kotlin
// ═══ PRIMARY SYSTEM COLORS ═══
val SystemBlue       = Color(0xFF4A9EFF)   // Primary UI
val SystemGold       = Color(0xFFFFD700)   // Rewards, highlight
val SystemPurple     = Color(0xFFB266FF)   // Rare/Epic
val SystemRed        = Color(0xFFFF4757)   // Penalty, danger
val SystemGreen      = Color(0xFF2ED573)   // Success, complete
val SystemOrange     = Color(0xFFFF9F43)   // Legendary, warning
val SystemCyan       = Color(0xFF18DCFF)   // Accent, tech-feel
val SystemPink       = Color(0xFFFF6B81)   // Special events

// ═══ BACKGROUND ═══
val DarkBg           = Color(0xFF0F0F23)   // Main dark bg
val DarkBgCard       = Color(0xFF1A1A3E)   // Card bg
val DarkBgElevated   = Color(0xFF252550)   // Elevated surface
val LightBg          = Color(0xFFF5F5FF)   // Light mode bg
val LightBgCard      = Color(0xFFFFFFFF)   // Light card

// ═══ RARITY COLORS ═══
val RarityCommon     = Color(0xFFB0B0B0)
val RarityUncommon   = Color(0xFF2ED573)
val RarityRare       = Color(0xFF4A9EFF)
val RarityEpic       = Color(0xFFB266FF)
val RarityLegendary  = Color(0xFFFF9F43)
val RarityMythic     = Color(0xFFFF4757)

// ═══ STAT COLORS ═══
val StatSTR = Color(0xFFFF4757)   // Đỏ
val StatINT = Color(0xFF4A9EFF)   // Xanh dương
val StatAGI = Color(0xFF2ED573)   // Xanh lá
val StatVIT = Color(0xFFFF9F43)   // Cam
val StatWIS = Color(0xFFB266FF)   // Tím
val StatCHA = Color(0xFFFF6B81)   // Hồng

// ═══ FINANCE COLORS ═══
val FinanceIncome    = Color(0xFF2ED573)   // Thu nhập - xanh
val FinanceExpense   = Color(0xFFFF4757)   // Chi tiêu - đỏ
val FinanceSaving    = Color(0xFF4A9EFF)   // Tiết kiệm - xanh dương
```

### Typography

```kotlin
// Heading / System text: Rajdhani hoặc Orbitron (game-feel)
// Body text: Inter hoặc Noto Sans Vietnamese (hỗ trợ tiếng Việt đầy đủ)
// Stat numbers: JetBrains Mono hoặc Source Code Pro (monospace)
// Quest titles: Rajdhani Bold
// Item/Title names: Cinzel hoặc MedievalSharp (fantasy-feel)
```

### Animation Specs

| Event | Animation | Duration |
|---|---|---|
| Page transition | Slide horizontal + fade | 300ms |
| Quest complete | Particle burst + scale bounce + glow overlay | 800ms |
| Level up | Full-screen overlay + particle rain + screen shake + "LEVEL UP!" text zoom | 2000ms |
| Item drop | Card flip 3D + glow border reveal theo rarity | 1200ms |
| Stat change | Number counter roll (old→new) + bar fill | 500ms |
| Skill level up | Node glow pulse + connection line light-up cascade | 1000ms |
| Penalty warning | Red pulse border + vibration pattern | Repeat 3x |
| Title acquired | Holographic shimmer + gold particles | 1500ms |
| Transaction saved | Slide down + ✅ icon bounce | 400ms |

### Glassmorphism Card Style (tham khảo repo Design System)

```kotlin
@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    glowColor: Color = SystemBlue,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = glowColor.copy(alpha = 0.3f),
                spotColor = glowColor.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkBgCard.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, glowColor.copy(alpha = 0.3f))
    ) {
        content()
    }
}
```

---

## 19. BACKUP & DATA MANAGEMENT

### Offline-first Architecture

- **Tất cả dữ liệu lưu trong Room Database** (SQLite) trên thiết bị
- App hoạt động 100% offline, chỉ cần mạng khi:
  - Gọi AI API để gen quests
  - Xem YouTube videos
  - Mở Google Drive links
- Nếu không có mạng → dùng **Quest Templates** (có sẵn bộ template cho mỗi goal type)

### Backup/Restore

```kotlin
// EXPORT
fun exportBackup(): File {
    val allData = BackupPayload(
        version = BuildConfig.VERSION_NAME,
        exportedAt = Instant.now().toEpochMilli(),
        user = userDao.getUser(),
        goals = goalDao.getAll(),
        skills = skillDao.getAll(),
        quests = questDao.getAll(),
        inventory = inventoryDao.getAll(),
        titles = titleDao.getAll(),
        transactions = transactionDao.getAll(),   // Finance
        budgets = budgetDao.getAll(),              // Finance
        financialGoals = financialGoalDao.getAll(),// Finance
        debts = debtDao.getAll(),                  // Finance
        libraryNodes = libraryDao.getAll(),        // Library (metadata only, files separate)
        journal = journalDao.getAll(),
        calendar = calendarDao.getAll(),
        habits = habitDao.getAll(),
        todos = todoDao.getAll(),
        penalties = penaltyDao.getAll()
    )
    val json = Json.encodeToString(allData)
    val file = File(context.getExternalFilesDir(null), 
                    "system_leveling_backup_${LocalDate.now()}.json")
    file.writeText(json)
    return file
}

// IMPORT
fun importBackup(file: File): Result<Unit>
// Parse JSON → validate version → clear DB → insert all → rebuild indices
```

### Tương lai — Web App

- Đặt sẵn `interface RemoteSyncRepository` cho sync với backend sau
- Database schema tương thích JSON ↔ REST API
- File `BackupPayload` sẵn sàng gửi lên server

---

## 20. GITHUB REPOS THAM KHẢO

### Repo của tác giả (BẮT BUỘC tham khảo)
- **`https://github.com/danghoangsqtt-sys/danghoang-ebook-v2`** — Web app hiện tại, đặc biệt:
  - `services/financial.ts` → logic Finance
  - `pages/Courses.tsx` + `hooks/useCourses.ts` → logic Library / CourseNode tree
  - `pages/Planner.tsx` → logic Calendar + Habits
  - `services/gemini.ts` → AI integration pattern
  - `types.ts` → Data models (convert sang Kotlin)
  - `components/SmartMoneyInput.tsx` → NLP money input

### Gamification / RPG Habit Apps
- Tìm kiếm GitHub: `habitica-android`, `gamification kotlin`, `rpg todo app`
- **Habitica**: Open-source gamified habit tracker — tham khảo reward system, avatar system
- **LifeRPG**: Gamified life tracker concepts

### Game UI References
- Tìm kiếm: `maplestory mobile ui`, `mmorpg android ui kit`, `genshin impact companion app`
- Skill Tree UI: `react-skill-tree`, `node-graph-visualization android`

### Calendar & Planner
- `kizitonwose/Calendar` — Compose calendar library
- `material-components-android` — Material 3 components

### Finance
- Tìm kiếm: `expense tracker android kotlin`, `budget app compose`

---

## 21. ROADMAP PHÁT TRIỂN

### Phase 1 — Foundation & Core (6 tuần)
- [ ] Project setup: Multi-module, Hilt, Room, Navigation, Theme
- [ ] Design System: GameCard, colors, typography, animations cơ bản
- [ ] Onboarding: Survey, Class selection, Goal selection
- [ ] Home Screen: Status Panel với stats, EXP bar, level display
- [ ] Basic Quest System: AI gen + completion + reward distribution
- [ ] Settings: AI key config, basic preferences

### Phase 2 — RPG Systems (6 tuần)
- [ ] Skill Tree: Full node graph UI + AI generation + SP tracking
- [ ] Inventory: Grid UI + item detail + rarity system
- [ ] Title System: Achievement tracking + title gallery + equip
- [ ] Penalty System: Debt tracking + penalty quest generation
- [ ] Level Up animations + background theme changes
- [ ] Notification system: WorkManager quest reminders

### Phase 3 — Productivity Tools (6 tuần)
- [ ] Calendar: Month/Week/Day views + Quest overlay + Events
- [ ] To-Do List: CRUD + priority + deadline + Convert-to-Quest
- [ ] Journal: Rich editor + mood + AI summary
- [ ] Habit Tracker: Daily tracking + 7-day history

### Phase 4 — Finance & Library (6 tuần)
- [ ] Finance Dashboard: Transaction CRUD + Charts
- [ ] Budget Tracker + Rollover
- [ ] Financial Goals + Debt Tracker
- [ ] SmartMoneyInput (AI-powered)
- [ ] AI Financial Analysis
- [ ] Learning Library: Folder tree + PDF viewer + YouTube embed
- [ ] PPTX viewer + Google Drive viewer
- [ ] Note overlay + Zen Mode + Progress tracking
- [ ] Library ↔ Skill Tree integration (reading = SP)

### Phase 5 — Polish & Ship (4 tuần)
- [ ] Animation polish (Lottie, particle effects, transitions)
- [ ] Performance optimization (lazy loading, pagination, caching)
- [ ] Đa ngôn ngữ hoàn chỉnh (VI/EN)
- [ ] Backup/Restore testing + Google Drive integration
- [ ] Offline quest templates (fallback khi không có AI)
- [ ] UI testing trên nhiều thiết bị
- [ ] Prepare RemoteSyncRepository interface cho Web App tương lai
- [ ] Play Store listing preparation

---

## 22. LƯU Ý QUAN TRỌNG

1. **Offline-first**: App PHẢI hoạt động offline. AI chỉ cần kết nối khi sinh quest mới — cache quest đã sinh. Nếu không có API key hoặc mất mạng → dùng quest templates có sẵn.

2. **Performance**: Skill Tree node graph phải smooth 60fps trên thiết bị mid-range (Snapdragon 680+). Sử dụng LazyColumn/LazyGrid cho danh sách dài. Tối ưu Room queries.

3. **Tiếng Việt**: Hỗ trợ đầy đủ Unicode tiếng Việt với dấu. Font Noto Sans Vietnamese cho body text. Tất cả string resources hỗ trợ i18n (strings.xml).

4. **Modular**: Clean Architecture + multi-module giúp dễ thêm feature mới. Mỗi feature module độc lập, communicate qua domain layer.

5. **Fun Factor**: UI PHẢI vui, đẹp, tạo cảm giác "đang chơi game" — KHÔNG PHẢI "đang dùng app nhàm chán". Mỗi tương tác cần có feedback (animation, sound indicator, haptic).

6. **AI Fallback**: Nếu không có API key hoặc mất mạng:
   - Quest: dùng template quest theo goal type
   - Skill Tree: dùng template tree theo goal category
   - Title: dùng bộ title mặc định
   - Finance Analysis: skip, chỉ hiện data thô

7. **Data Safety**: Tất cả dữ liệu lưu local. Không gửi personal data lên server nào ngoài AI API (chỉ gửi stats tổng hợp, không gửi tên thật/tài chính chi tiết cho AI).

8. **Tham khảo repo tác giả**: Logic Finance và Library trong `danghoang-ebook-v2` đã được test trên web — chuyển logic tương tự sang Android/Kotlin nhưng optimize cho mobile UX.

9. **Web App tương lai**: Đặt sẵn interface + JSON schema tương thích. Khi build web app sau, chỉ cần implement RemoteSyncRepository + REST API.

10. **Không cần backend server cho MVP**: Toàn bộ local + AI API. Backend chỉ cần khi build web app sync.

---

*Kết thúc prompt. Chúc code vui vẻ! 🎮⚔️*

> "Arise." — The System
