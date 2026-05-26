# Architecture — Solo Leveling System

> Kiến trúc Android Native · Kotlin · Jetpack Compose · Room · Hilt DI · WorkManager

---

## Module Structure

```mermaid
graph TD
    subgraph App["app/"]
        A[SystemLevelingApp<br/>Application + WorkManager]
        N[AppNavGraph<br/>Navigation Host]
    end

    subgraph Feature["feature/"]
        F_HOME[home]
        F_QUEST[quests]
        F_SKILL[skills]
        F_INV[inventory]
        F_TITLE[titles]
        F_FIN[finance]
        F_CAL[calendar]
        F_JOUR[journal]
        F_LIB[library]
        F_ONBOARD[onboarding]
    end

    subgraph Core["core/"]
        C_DB[database<br/>Room + DAOs]
        C_MODEL[model<br/>Enums, Data Classes]
        C_ENGINE[engine<br/>Reward, Penalty, Loot]
        C_NET[network<br/>Gemini API, QuestGen]
        C_WORKER[worker<br/>DailyQuest, EndOfDay]
        C_DI[di<br/>Hilt Modules]
        C_DS[designsystem<br/>GlassCard, Theme]
        C_NOTIF[notification<br/>NotificationHelper]
        C_SYNC[sync<br/>CloudSyncManager]
        C_SETTINGS[settings<br/>SettingsManager]
        C_AI[ai<br/>AuraRepository]
    end

    A --> N
    N --> Feature
    Feature --> Core
    C_ENGINE --> C_DB
    C_NET --> C_DB
    C_WORKER --> C_NET
    C_WORKER --> C_ENGINE
    C_SYNC --> C_DB
```

---

## Data Flow: Quest Lifecycle

```mermaid
sequenceDiagram
    participant User
    participant VM as QuestViewModel
    participant Gen as AiQuestGeneratorService
    participant DB as Room Database
    participant RE as RewardEngine
    participant PE as PenaltyEngine

    Note over VM: App opened → init{}
    VM->>DB: getQuestCountByDate(today)
    alt No quests for today
        VM->>Gen: generateDailyQuests(apiKey, dayStart)
        Gen->>DB: insertQuests(quests + health reminders)
    end
    DB-->>VM: Flow<List<QuestEntity>> (today only)
    VM-->>User: Display quest timeline

    User->>VM: completeQuest(quest)
    VM->>RE: processQuestCompletion(quest)
    RE->>DB: withTransaction { update user, stats, skills, items }
    RE-->>VM: RewardResult
    VM-->>User: Show reward dialog

    Note over VM: Timer expires
    VM->>PE: processQuestFailure(quest)
    PE->>DB: Update quest status → FAILED
    PE-->>VM: PenaltyEvent
    VM-->>User: Show penalty banner
```

---

## Background Workers

```mermaid
graph LR
    WM[WorkManager<br/>Periodic 24h] --> DQW[DailyQuestWorker<br/>Midnight: Generate quests]
    WM --> EODW[EndOfDayWorker<br/>22:00: Process penalties]
    DQW --> AQG[AiQuestGeneratorService]
    EODW --> PE[PenaltyEngine]
```

---

## Key Design Decisions

| Decision | Rationale |
|---|---|
| **Room + Flow** | Reactive UI updates từ database, tự động refresh khi data thay đổi |
| **withTransaction** | Atomic reward processing — tránh corrupt state khi app crash giữa chừng |
| **WorkManager periodic** | Background quest generation/penalty xử lý ngay cả khi app đóng |
| **Hilt DI** | Constructor injection cho tất cả Engine, Service, ViewModel |
| **MutableStateFlow + flatMapLatest** | Quest filtering theo ngày tự động refresh khi ngày thay đổi |
| **Component extraction** | UI files tách nhỏ để dễ maintain và review |

---

## Module Dependency Rules

1. `feature/*` → phụ thuộc vào `core/` (one-way)
2. `core/` modules **không** phụ thuộc vào `feature/`
3. `app/` → phụ thuộc vào cả `feature/` và `core/`
4. Tránh circular dependency giữa các feature modules
