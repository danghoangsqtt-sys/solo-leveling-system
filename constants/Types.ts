// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — Core Type Definitions
// All game data models — mirrors the Room Database schema
// ═══════════════════════════════════════════════════════════════

// ═══ USER / CHARACTER ═══
export interface UserProfile {
  id: string;
  nickname: string;
  avatarUri: string | null;
  className: ClassName;
  level: number;
  currentExp: number;
  expToNextLevel: number;
  gold: number;
  gems: number;
  equippedTitleId: string | null;
  stats: CharacterStats;
  debtPoints: number;
  streakDays: number;
  totalQuestsCompleted: number;
  dateOfBirth: string | null;
  gender: 'male' | 'female' | 'other' | null;
  heightCm: number | null;
  weightKg: number | null;
  createdAt: number;
  updatedAt: number;
}

export interface CharacterStats {
  STR: number; // 1-100
  INT: number;
  AGI: number;
  VIT: number;
  WIS: number;
  CHA: number;
}

export type StatKey = keyof CharacterStats;

export type ClassName =
  | 'warrior'
  | 'mage'
  | 'ranger'
  | 'guardian'
  | 'bard'
  | 'alchemist'
  | 'healer'
  | 'scholar';

export interface ClassInfo {
  id: ClassName;
  name: string;
  nameVi: string;
  icon: string;
  description: string;
  primaryStats: [StatKey, StatKey];
  color: string;
}

// ═══ GOALS ═══
export interface Goal {
  id: string;
  name: string;
  description: string;
  category: string;
  icon: string;
  deadline: number | null;
  isActive: boolean;
  createdAt: number;
}

// ═══ SKILLS ═══
export interface Skill {
  id: string;
  goalId: string;
  name: string;
  description: string;
  iconName: string;
  currentSP: number;
  currentLevel: SkillLevel;
  levelName: string;
  parentSkillId: string | null;
  childSkillIds: string[];
  isUnlocked: boolean;
  orderIndex: number;
}

export type SkillLevel = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export const SkillLevelNames: Record<SkillLevel, { vi: string; en: string; spRequired: number }> = {
  1: { vi: 'Nhập Môn', en: 'Novice', spRequired: 0 },
  2: { vi: 'Sơ Cấp', en: 'Apprentice', spRequired: 100 },
  3: { vi: 'Trung Sơ Cấp', en: 'Intermediate', spRequired: 300 },
  4: { vi: 'Trung Cấp', en: 'Advanced', spRequired: 600 },
  5: { vi: 'Tiền Cao Cấp', en: 'Expert', spRequired: 1000 },
  6: { vi: 'Cao Cấp', en: 'Master', spRequired: 1800 },
  7: { vi: 'Grand Master', en: 'Grand Master', spRequired: 3000 },
};

// ═══ QUESTS ═══
export interface Quest {
  id: string;
  title: string;
  description: string;
  type: QuestType;
  difficulty: QuestRank;
  category: string;
  date: number;
  timeWindow: TimeWindow | null;
  subtasks: string[];
  rewards: QuestRewards;
  penaltyOnFail: QuestPenalty;
  relatedGoalId: string | null;
  relatedSkillIds: string[];
  status: QuestStatus;
  completedAt: number | null;
  createdAt: number;
}

export type QuestType = 'daily' | 'weekly' | 'boss' | 'penalty' | 'event' | 'side';
export type QuestRank = 'E' | 'D' | 'C' | 'B' | 'A' | 'S';
export type QuestStatus = 'pending' | 'in_progress' | 'completed' | 'failed' | 'expired';

export interface TimeWindow {
  start: string; // HH:MM
  end: string;
  durationMinutes: number;
}

export interface QuestRewards {
  exp: number;
  gold: number;
  skillPoints: Array<{ skillId: string; amount: number }>;
  itemDrop: ItemDrop | null;
}

export interface QuestPenalty {
  expLoss: number;
  debtPoints: number;
}

export interface ItemDrop {
  itemTemplate: string;
  rarity: RarityTier;
  chance: number; // 0.0-1.0
}

// ═══ INVENTORY ═══
export interface InventoryItem {
  id: string;
  name: string;
  description: string;
  iconName: string;
  rarity: RarityTier;
  category: string;
  quantity: number;
  obtainedAt: number;
  questId: string | null;
}

export type RarityTier = 'common' | 'uncommon' | 'rare' | 'epic' | 'legendary' | 'mythic';

export const RarityOrder: Record<RarityTier, number> = {
  common: 0,
  uncommon: 1,
  rare: 2,
  epic: 3,
  legendary: 4,
  mythic: 5,
};

// ═══ TITLES ═══
export interface Title {
  id: string;
  name: string;
  nameEn: string | null;
  description: string;
  condition: string;
  rarity: RarityTier;
  iconEmoji: string;
  isUnlocked: boolean;
  unlockedAt: number | null;
  category: string;
}

// ═══ FINANCE ═══
export interface Transaction {
  id: string;
  type: 'income' | 'expense';
  amount: number;
  categoryId: string;
  categoryName: string;
  categoryIcon: string;
  note: string | null;
  date: number;
  isRecurring: boolean;
  createdAt: number;
}

export interface BudgetCategory {
  id: string;
  name: string;
  icon: string;
  monthlyBudget: number;
  color: string;
  rolloverEnabled: boolean;
  rolloverAmount: number;
}

export interface FinancialGoal {
  id: string;
  name: string;
  targetAmount: number;
  currentAmount: number;
  deadline: number | null;
  icon: string;
  isCompleted: boolean;
  createdAt: number;
}

export interface Debt {
  id: string;
  type: 'receivable' | 'payable';
  personName: string;
  amount: number;
  remainingAmount: number;
  note: string | null;
  deadline: number | null;
  isSettled: boolean;
  createdAt: number;
}

// ═══ LIBRARY ═══
export interface LibraryNode {
  id: string;
  name: string;
  type: NodeType;
  parentId: string | null;
  content: LibraryContent | null;
  isPinned: boolean;
  tags: string[];
  progress: number; // 0.0-1.0
  lastOpenedAt: number | null;
  relatedGoalId: string | null;
  relatedSkillIds: string[];
  orderIndex: number;
  createdAt: number;
}

export type NodeType =
  | 'folder'
  | 'pdf'
  | 'pptx'
  | 'video_youtube'
  | 'video_local'
  | 'gdrive'
  | 'note'
  | 'web_link';

export interface LibraryContent {
  filePath: string | null;
  url: string | null;
  mimeType: string | null;
  fileSize: number | null;
  thumbnailPath: string | null;
  notes: string | null;
}

// ═══ JOURNAL ═══
export interface JournalEntry {
  id: string;
  date: number;
  content: string;
  mood: MoodType | null;
  tags: string[];
  createdAt: number;
  updatedAt: number;
}

export type MoodType = 'great' | 'good' | 'neutral' | 'bad' | 'terrible';

// ═══ CALENDAR ═══
export interface CalendarEvent {
  id: string;
  title: string;
  description: string | null;
  startTime: number;
  endTime: number;
  isAllDay: boolean;
  color: string | null;
  reminderMinutes: number | null;
  isQuestRelated: boolean;
  questId: string | null;
}

// ═══ HABITS ═══
export interface Habit {
  id: string;
  name: string;
  icon: string;
  frequency: 'daily' | 'weekly' | 'custom';
  targetPerDay: number;
  color: string;
  isActive: boolean;
  completions: Record<string, number>; // date string -> count
  createdAt: number;
}

// ═══ TODOS ═══
export interface TodoItem {
  id: string;
  title: string;
  description: string | null;
  priority: 'urgent' | 'high' | 'medium' | 'low';
  deadline: number | null;
  isCompleted: boolean;
  completedAt: number | null;
  createdAt: number;
}

// ═══ PENALTY ═══
export interface PenaltyLog {
  id: string;
  questId: string;
  debtPointsAdded: number;
  reason: string;
  scheduledPayDate: number | null;
  isPaid: boolean;
  paidAt: number | null;
  createdAt: number;
}

// ═══ ONBOARDING ═══
export interface SurveyQuestion {
  id: string;
  stat: StatKey;
  question: string;
  type: 'slider' | 'multiple_choice';
  options?: string[];
  min?: number;
  max?: number;
}
