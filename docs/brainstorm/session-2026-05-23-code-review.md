# 🧠 Brainstorm Session — 2026-05-23 Code Review

## Chủ đề: Đánh Giá Mã Nguồn & Phân Tích Luồng Hoạt Động

> **Trạng thái**: ✅ Scope Locked
> **Ngày**: 2026-05-23
> **Người yêu cầu**: @danghoangsqtt-sys
> **Phạm vi**: Các file đã thay đổi trong git — core/settings, core/sync, core/database, feature/home, feature/onboarding, feature/quests

---

## 📐 Kiến Trúc Tổng Quan

```
App (entry point)
├── core/
│   ├── database/       — Room DB v8 (10 entities, 9 DAOs)
│   ├── settings/       — DataStore<Preferences> qua SettingsManager
│   ├── sync/           — CloudSyncManager (Supabase REST via Ktor)
│   ├── ai/             — AuraRepository (Gemini API)
│   ├── engine/         — RewardEngine (EXP/Gold/Item drop)
│   └── network/        — AiQuestGeneratorService
└── feature/
    ├── home/           — Dashboard + sync controls
    ├── onboarding/     — AI-powered character creation
    ├── quests/         — Daily quest list + auto-gen
    ├── inventory/      — Item grid + compendium
    └── ...
```

**Stack:** Kotlin · Jetpack Compose · Hilt · Room v8 · DataStore · Ktor · Supabase REST · Gemini AI

---

## 🔄 Phân Tích Luồng Hoạt Động Chính

### Luồng 1 — App Startup
```
App khởi động
  → check DataStore["isOnboarded"] (key trong OnboardingViewModel)
  → false  → OnboardingScreen
  → true   → HomeScreen
               → HomeViewModel.init{}
                   → cloudSyncManager.restoreIfEmpty() [chạy ngầm, có guard]
                   → SyncState: Idle → Restoring → Restored/Idle
```

### Luồng 2 — Onboarding (Tạo Nhân Vật)
```
User nhập: nickname + goal + survey data + API key
  → OnboardingViewModel.generateRoadmapAndComplete()
      → settingsManager.setGeminiApiKey()           [lưu key]
      → settingsManager.setSupabaseConfig()         [lưu Supabase]
      → auraRepository.generateCompleteOnboarding() [gọi Gemini]
          → parse JSON → AiCompleteOnboardingResponse
          → UiState.Result(data)
  → User chọn class → acceptAndComplete()
      → userDao.insertUser()      [UserEntity]
      → userDao.insertStats()     [StatEntity — 6 chỉ số RPG]
      → skillDao.deleteAllSkills() + insertSkills() [cây kỹ năng AI gen]
      → DataStore["isOnboarded"] = true
      → UiState.Success → navigate to HomeScreen
```

### Luồng 3 — Quest Hàng Ngày
```
QuestScreen attach
  → QuestViewModel.init{}
      → ensureTodayQuests() [IO thread]
          → questDao.getQuestCountByDate(today) == 0?
              → true  → aiQuestGenerator.generateDailyQuests(apiKey, todayStart)
              → false → skip (quests đã tồn tại)

User complete quest
  → completeQuest(quest)
      → rewardEngine.processQuestCompletion(quest)  [IO thread]
          → emit RewardResult (SharedFlow — one-time event)
      → cloudSyncManager.push()  ← push toàn bộ snapshot sau MỖI quest
```

### Luồng 4 — Cloud Sync (Supabase)
```
Push:
  settingsManager → supabaseUrl + anonKey + deviceId
  → collect: user + stats + skills + items (4 entities)
  → encode → PlayerSnapshot JSON
  → POST /rest/v1/game_state
      header: Prefer: resolution=merge-duplicates  [upsert by device_id]

Restore (chỉ khi DB trống):
  → GET /rest/v1/game_state?device_id=eq.{uuid}&select=player_json&limit=1
  → decode → insert user + stats + skills + items
  ⚠️ Quests và Journal KHÔNG được sync
```

### Luồng 5 — Settings
```
DataStore<Preferences>
  → SettingsManager (Singleton)
      → expose as Flow<T>   (reactive — collect trong ViewModel)
      → expose as suspend   (one-shot read — first())
  → ViewModel.stateIn()
      → UI collect StateFlow
```

---

## 🐛 Issues Phát Hiện

### Issue #1 — `isOnboarded` key không trong SettingsManager [MEDIUM]
- **Vị trí:** [OnboardingViewModel.kt:171](feature/onboarding/src/main/java/com/systemleveling/feature/onboarding/ui/OnboardingViewModel.kt#L171)
- `booleanPreferencesKey("isOnboarded")` được define trực tiếp trong ViewModel
- `DataStore` cũng được inject trực tiếp vào ViewModel — vi phạm abstraction
- **Rủi ro:** Nếu component khác cần check `isOnboarded`, phải duplicate key → có thể typo
- **Fix:** Chuyển key và suspend fun về SettingsManager; ViewModel chỉ gọi settingsManager

### Issue #2 — `restoreIfEmpty()` gọi mỗi lần HomeViewModel tạo [LOW]
- **Vị trí:** [HomeViewModel.kt:48-52](feature/home/src/main/java/com/systemleveling/feature/home/ui/HomeViewModel.kt#L48)
- Mỗi lần navigate về Home → ViewModel.init{} chạy → network coroutine launch
- Guard `if (localUser != null) return false` bảo vệ nhưng vẫn tốn 1 DB query + coroutine overhead
- **Fix:** Chỉ restore 1 lần sau install, hoặc cache flag trong memory/DataStore

### Issue #3 — Quests không được sync lên cloud [MEDIUM]
- **Vị trí:** [CloudSyncManager.kt:57-63](core/src/main/java/com/systemleveling/core/sync/CloudSyncManager.kt#L57)
- `PlayerSnapshot` chỉ gồm: user, stats, skills, items — thiếu quests và journal
- Khi restore thiết bị mới, toàn bộ lịch sử quest/journal bị mất
- **Quyết định cần làm:** Quests có ephemeral (daily reset) hay cần lưu lịch sử?

### Issue #4 — cloudSyncManager.push() sau MỖI quest complete [MEDIUM]
- **Vị trí:** [QuestViewModel.kt:81](feature/quests/src/main/java/com/systemleveling/feature/quests/ui/QuestViewModel.kt#L81)
- User complete 5 quests liên tiếp → 5 lần push toàn bộ snapshot lên Supabase
- **Fix:** Debounce push (ví dụ 30s), hoặc chỉ push khi app vào background/onStop

### Issue #5 — SettingsManager: pattern add*PlanItem lặp 3 lần [LOW]
- **Vị trí:** [SettingsManager.kt:80-98, 115-122, 148-154](core/src/main/java/com/systemleveling/core/settings/SettingsManager.kt)
- Work/Weekly/Monthly dùng cùng pattern: read-decode-append-encode-write
- **Fix:** Extract generic `<T> addItemToList(key, item, serializer)` helper

### Issue #6 — AppDatabase exportSchema = false [LOW]
- **Vị trí:** [AppDatabase.kt:29](core/src/main/java/com/systemleveling/core/database/AppDatabase.kt#L29)
- Không export schema JSON → CI không thể validate migration consistency
- **Fix:** `exportSchema = true` + thêm `room.schemaLocation` vào build.gradle.kts

### Issue #7 — Hard-coded user IDs trong SQL [INFO]
- **Vị trí:** [UserDao.kt:13,16,22,25](core/src/main/java/com/systemleveling/core/database/dao/UserDao.kt)
- `WHERE id = 'local_user'`, `WHERE id = 'local_stats'` — single-user assumption
- Hiện tại chấp nhận được, nhưng cần document nếu tương lai hỗ trợ multi-profile

---

## ✅ Điểm Tốt Trong Codebase

| Điểm mạnh | Chi tiết |
|---|---|
| Reactive data | Toàn bộ UI dùng `Flow` + `stateIn(WhileSubscribed(5000))` |
| IO threading | Quest/sync operations dùng `Dispatchers.IO` đúng cách |
| One-time events | `SharedFlow` cho `rewardResult` — không bị re-emit |
| Database migration | MIGRATION_7_8 explicit thay vì fallbackToDestructiveMigration |
| Sync guard | `restoreIfEmpty()` chỉ chạy khi DB trống |
| Lenient JSON | `ignoreUnknownKeys = true` + `isLenient = true` cho AI response |
| Upsert | Supabase `resolution=merge-duplicates` — idempotent push |
| Skill hierarchy | AI roadmap nodes → parent/child skill tree (đúng RPG pattern) |

---

## 🏗️ Phases

### Phase 1 — Quick Wins (ưu tiên cao, low risk)
- [ ] **P1.1** Chuyển `isOnboarded` key + DataStore access vào SettingsManager
- [ ] **P1.2** Xóa `DataStore` inject trực tiếp khỏi OnboardingViewModel
- [ ] **P1.3** Debounce `cloudSyncManager.push()` sau quest complete (30s delay hoặc queue)
- [ ] **P1.4** Bật `exportSchema = true` trong AppDatabase

### Phase 2 — Cải Tiến Cấu Trúc
- [ ] **P2.1** Extract generic `addItemToList()` helper trong SettingsManager
- [ ] **P2.2** Quyết định sync quests lên cloud (ephemeral vs persistent)
- [ ] **P2.3** Tối ưu `restoreIfEmpty()` — chỉ chạy 1 lần sau install (thêm flag DataStore)

### Phase 3 — Tech Debt / Future
- [ ] **P3.1** Document single-user assumption rõ ràng hoặc refactor cho multi-profile
- [ ] **P3.2** Rate limiting / retry cho Supabase push failures

---

## ❓ Open Questions

1. **Quests có cần sync không?** — Daily quests ephemeral hay muốn lưu lịch sử streak/completion?
2. **Push frequency** — Mỗi quest hay push theo batch? Batch size bao nhiêu?
3. **Onboarding re-entry** — Có cho phép user re-onboard (reset character) không?

---

## 📝 Ghi Chú Kỹ Thuật

- **DB version:** 8 (MIGRATION_7_8: thêm `isStored` column cho items)
- **Sync scope:** user + stats + skills + items (quests/journal excluded)
- **AI provider:** Google Gemini `gemini-2.0-flash` via AuraRepository
- **Supabase table:** `game_state` với column `device_id` (UUID) + `player_json` (TEXT)
- **Plan storage:** WorkPlanItems + WeeklyPlanItems + MonthlyPlanItems trong DataStore (JSON string)

---

## 🔗 Files Liên Quan

- [SettingsManager.kt](core/src/main/java/com/systemleveling/core/settings/SettingsManager.kt)
- [CloudSyncManager.kt](core/src/main/java/com/systemleveling/core/sync/CloudSyncManager.kt)
- [HomeViewModel.kt](feature/home/src/main/java/com/systemleveling/feature/home/ui/HomeViewModel.kt)
- [OnboardingViewModel.kt](feature/onboarding/src/main/java/com/systemleveling/feature/onboarding/ui/OnboardingViewModel.kt)
- [QuestViewModel.kt](feature/quests/src/main/java/com/systemleveling/feature/quests/ui/QuestViewModel.kt)
- [AppDatabase.kt](core/src/main/java/com/systemleveling/core/database/AppDatabase.kt)
- [UserDao.kt](core/src/main/java/com/systemleveling/core/database/dao/UserDao.kt)
- [ItemDao.kt](core/src/main/java/com/systemleveling/core/database/dao/ItemDao.kt)

---

## 🔜 Bước Tiếp Theo

```
/vp-crystallize   → chuyển Phase 1 issues thành action items cụ thể
/vp-auto          → tự động implement fixes Phase 1
```

---
*Session completed — 2026-05-23 · vp-brainstorm v1.1.0*
