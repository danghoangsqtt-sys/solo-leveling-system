"use client";

import "./quests.css";

const DEMO_QUESTS = [
  {
    id: "Q-001",
    time: "06:00",
    title: "Uống nước buổi sáng",
    rank: "E" as const,
    status: "completed" as const,
    rewards: { exp: 30, sp: null },
    category: "health",
  },
  {
    id: "Q-002",
    time: "06:30",
    title: "Morning Warrior Training",
    rank: "C" as const,
    status: "in_progress" as const,
    rewards: { exp: 150, sp: "Cardio +10" },
    category: "fitness",
    description: "Chạy 3km + 30 hít đất",
    timeEnd: "07:15",
  },
  {
    id: "Q-003",
    time: "08:00",
    title: "Deep Focus: IELTS Reading",
    rank: "B" as const,
    status: "pending" as const,
    rewards: { exp: 250, sp: "Reading +15" },
    category: "language",
    description: "2 passages + 40 câu hỏi",
  },
  {
    id: "Q-004",
    time: "10:00",
    title: "Code Practice: MQTT Protocol",
    rank: "C" as const,
    status: "pending" as const,
    rewards: { exp: 180, sp: "Network +12" },
    category: "tech",
    description: "Hoàn thành tutorial section 3",
  },
  {
    id: "Q-005",
    time: "12:00",
    title: "Vocabulary Builder",
    rank: "D" as const,
    status: "pending" as const,
    rewards: { exp: 80, sp: null },
    category: "language",
    description: "Học 15 từ mới + ôn 30 từ cũ",
  },
  {
    id: "Q-006",
    time: "14:00",
    title: "Essay Writing Practice",
    rank: "B" as const,
    status: "pending" as const,
    rewards: { exp: 250, sp: "Writing +15" },
    category: "language",
    description: "Viết Task 2: Technology topic",
  },
  {
    id: "Q-007",
    time: "16:00",
    title: "Financial Check-in",
    rank: "D" as const,
    status: "pending" as const,
    rewards: { exp: 50, sp: null },
    category: "finance",
    description: "Ghi nhận chi tiêu hôm nay",
  },
  {
    id: "Q-008",
    time: "19:00",
    title: "Evening Workout",
    rank: "C" as const,
    status: "pending" as const,
    rewards: { exp: 150, sp: "Strength +10" },
    category: "fitness",
  },
  {
    id: "Q-009",
    time: "21:00",
    title: "Reflection & Journal",
    rank: "E" as const,
    status: "pending" as const,
    rewards: { exp: 40, sp: null },
    category: "wellness",
  },
];

const RANK_COLORS: Record<string, string> = {
  E: "var(--rank-e)",
  D: "var(--rank-d)",
  C: "var(--rank-c)",
  B: "var(--rank-b)",
  A: "var(--rank-a)",
  S: "var(--rank-s)",
};

const STATUS_ICONS: Record<string, string> = {
  completed: "✅",
  in_progress: "⏳",
  pending: "○",
  failed: "❌",
};

export default function QuestsPage() {
  const today = new Date();
  const dayName = today.toLocaleDateString("vi-VN", { weekday: "long" });
  const dateStr = today.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
  const completed = DEMO_QUESTS.filter((q) => q.status === "completed").length;
  const total = DEMO_QUESTS.length;
  const percent = ((completed / total) * 100).toFixed(1);

  return (
    <div className="quests">
      <div className="quests__header animate-fadeIn">
        <h1 className="quests__date font-heading">
          ═══ {dayName.toUpperCase()} — {dateStr} ═══
        </h1>
        <div className="quests__streak">
          🔥 Streak: <span className="font-mono">12</span>
        </div>
      </div>

      <div className="quests__timeline">
        {DEMO_QUESTS.map((quest, index) => (
          <div
            key={quest.id}
            className={`quest-item quest-item--${quest.status} animate-slideInRight`}
            style={{ animationDelay: `${index * 0.06}s` }}
          >
            <div className="quest-item__time font-mono">{quest.time}</div>
            <div className="quest-item__line">
              <div
                className="quest-item__dot"
                style={{
                  borderColor: RANK_COLORS[quest.rank],
                  background:
                    quest.status === "completed"
                      ? RANK_COLORS[quest.rank]
                      : "var(--bg-card-solid)",
                }}
              />
            </div>
            <div className="quest-item__card game-card">
              <div className="quest-item__top">
                <span
                  className={`rank-badge rank-badge--${quest.rank.toLowerCase()}`}
                >
                  {quest.rank}
                </span>
                <span className="quest-item__title">{quest.title}</span>
                <span className="quest-item__status">
                  {STATUS_ICONS[quest.status]}
                </span>
              </div>
              {quest.description && (
                <p className="quest-item__desc">{quest.description}</p>
              )}
              <div className="quest-item__rewards">
                <span className="quest-item__reward">
                  ✨ +{quest.rewards.exp} EXP
                </span>
                {quest.rewards.sp && (
                  <span className="quest-item__reward">
                    🔮 {quest.rewards.sp}
                  </span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="quests__progress game-card animate-fadeIn">
        <span className="font-heading">
          Progress: {completed}/{total}
        </span>
        <div className="progress-bar" style={{ flex: 1 }}>
          <div
            className="progress-bar__fill"
            style={{
              width: `${percent}%`,
              background:
                "linear-gradient(90deg, var(--system-green), var(--system-cyan))",
            }}
          />
        </div>
        <span className="font-mono" style={{ fontSize: "var(--text-sm)" }}>
          {percent}%
        </span>
      </div>
    </div>
  );
}
