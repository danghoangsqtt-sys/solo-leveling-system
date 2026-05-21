/* ═══════════════════════════════════════════════════════════════
   SOLO LEVELING SYSTEM — Core TypeScript Types
   Mapped from PROMPT_System_Leveling_App_V2.md §17 Database Schema
   ═══════════════════════════════════════════════════════════════ */

// ═══ USER ═══
export interface User {
  id: string;
  nickname: string;
  avatar_path: string | null;
  class_name: ClassName;
  level: number;
  current_exp: number;
  exp_to_next_level: number;
  gold: number;
  gems: number;
  equipped_title_id: string | null;
  str: number;
  int_stat: number;
  agi: number;
  vit: number;
  wis: number;
  cha: number;
  debt_points: number;
  streak_days: number;
  total_quests_completed: number;
  created_at: string;
  updated_at: string;
}

export const DEMO_USER: User = {
  id: "demo-id",
  nickname: "Jin-Woo",
  avatar_path: null,
  class_name: "Warrior",
  level: 42,
  current_exp: 1500,
  exp_to_next_level: 3000,
  gold: 12450,
  gems: 340,
  equipped_title_id: null,
  str: 150,
  int_stat: 85,
  agi: 105,
  vit: 95,
  wis: 60,
  cha: 50,
  debt_points: 0,
  streak_days: 12,
  total_quests_completed: 145,
  created_at: new Date().toISOString(),
  updated_at: new Date().toISOString(),
};


export type ClassName =
  | "Warrior"
  | "Mage"
  | "Ranger"
  | "Guardian"
  | "Bard"
  | "Alchemist"
  | "Healer"
  | "Scholar";

export const CLASS_ICONS: Record<ClassName, string> = {
  Warrior: "🗡️",
  Mage: "🧙",
  Ranger: "🏹",
  Guardian: "🛡️",
  Bard: "🎭",
  Alchemist: "⚗️",
  Healer: "🌿",
  Scholar: "📜",
};

// ═══ STATS ═══
export type StatKey = "str" | "int_stat" | "agi" | "vit" | "wis" | "cha";

export interface StatInfo {
  key: StatKey;
  label: string;
  shortLabel: string;
  color: string;
  cssClass: string;
}

export const STATS: StatInfo[] = [
  { key: "str", label: "Strength", shortLabel: "STR", color: "#FF4757", cssClass: "str" },
  { key: "int_stat", label: "Intelligence", shortLabel: "INT", color: "#4A9EFF", cssClass: "int" },
  { key: "agi", label: "Agility", shortLabel: "AGI", color: "#2ED573", cssClass: "agi" },
  { key: "vit", label: "Vitality", shortLabel: "VIT", color: "#FF9F43", cssClass: "vit" },
  { key: "wis", label: "Wisdom", shortLabel: "WIS", color: "#B266FF", cssClass: "wis" },
  { key: "cha", label: "Charisma", shortLabel: "CHA", color: "#FF6B81", cssClass: "cha" },
];

// ═══ QUESTS ═══
export type QuestType = "daily" | "weekly" | "boss" | "penalty" | "event" | "side";
export type QuestRank = "E" | "D" | "C" | "B" | "A" | "S";
export type QuestStatus = "pending" | "in_progress" | "completed" | "failed" | "expired";

export interface Quest {
  id: string;
  title: string;
  description: string;
  type: QuestType;
  difficulty: QuestRank;
  category: string;
  date: string;
  start_time: string | null;
  end_time: string | null;
  duration_minutes: number | null;
  subtasks: string[];
  rewards: QuestRewards;
  penalty_on_fail: QuestPenalty;
  status: QuestStatus;
  related_goal_id: string | null;
  related_skill_ids: string[];
  completed_at: string | null;
  created_at: string;
}

export interface QuestRewards {
  exp: number;
  gold: number;
  skill_points: { skill_id: string; amount: number }[];
  item_drop: { item_template: string; rarity: Rarity; chance: number } | null;
}

export interface QuestPenalty {
  exp_loss: number;
  debt_points: number;
}

// ═══ SKILLS ═══
export type SkillLevel = 1 | 2 | 3 | 4 | 5 | 6 | 7;

export const SKILL_LEVEL_NAMES: Record<SkillLevel, { vi: string; en: string }> = {
  1: { vi: "Nhập Môn", en: "Novice" },
  2: { vi: "Sơ Cấp", en: "Apprentice" },
  3: { vi: "Trung Sơ Cấp", en: "Intermediate" },
  4: { vi: "Trung Cấp", en: "Advanced" },
  5: { vi: "Tiền Cao Cấp", en: "Expert" },
  6: { vi: "Cao Cấp", en: "Master" },
  7: { vi: "Grand Master", en: "Grand Master" },
};

export const SKILL_LEVEL_SP: Record<SkillLevel, number> = {
  1: 100, 2: 300, 3: 600, 4: 1000, 5: 1800, 6: 3000, 7: Infinity,
};

export interface Skill {
  id: string;
  goal_id: string;
  name: string;
  description: string;
  icon_name: string;
  current_sp: number;
  current_level: SkillLevel;
  level_name: string;
  parent_skill_id: string | null;
  is_unlocked: boolean;
  order_index: number;
}

// ═══ INVENTORY ═══
export type Rarity = "common" | "uncommon" | "rare" | "epic" | "legendary" | "mythic";

export const RARITY_INFO: Record<Rarity, { label: string; color: string; cssClass: string }> = {
  common: { label: "Thường", color: "#B0B0B0", cssClass: "rarity-common" },
  uncommon: { label: "Hiếm", color: "#2ED573", cssClass: "rarity-uncommon" },
  rare: { label: "Quý Hiếm", color: "#4A9EFF", cssClass: "rarity-rare" },
  epic: { label: "Sử Thi", color: "#B266FF", cssClass: "rarity-epic" },
  legendary: { label: "Huyền Thoại", color: "#FF9F43", cssClass: "rarity-legendary" },
  mythic: { label: "Thần Thoại", color: "#FF4757", cssClass: "rarity-mythic" },
};

export interface InventoryItem {
  id: string;
  name: string;
  description: string;
  icon_name: string;
  rarity: Rarity;
  category: string;
  quantity: number;
  obtained_at: string;
  quest_id: string | null;
}

// ═══ TITLES ═══
export interface Title {
  id: string;
  name: string;
  name_en: string | null;
  description: string;
  condition: string;
  rarity: Rarity;
  icon_emoji: string;
  is_unlocked: boolean;
  unlocked_at: string | null;
  category: string;
}

// ═══ GOALS ═══
export interface Goal {
  id: string;
  name: string;
  description: string;
  category: string;
  deadline: string | null;
  is_active: boolean;
  created_at: string;
}

// ═══ FINANCE ═══
export type TransactionType = "income" | "expense";

export interface Transaction {
  id: string;
  type: TransactionType;
  amount: number;
  category_id: string;
  category_name: string;
  category_icon: string;
  note: string | null;
  date: string;
  is_recurring: boolean;
  created_at: string;
}

export interface BudgetCategory {
  id: string;
  name: string;
  icon: string;
  monthly_budget: number;
  color: string;
  rollover_enabled: boolean;
  rollover_amount: number;
}

export interface FinancialGoal {
  id: string;
  name: string;
  target_amount: number;
  current_amount: number;
  deadline: string | null;
  icon: string;
  is_completed: boolean;
  created_at: string;
}

export interface Debt {
  id: string;
  type: "receivable" | "payable";
  person_name: string;
  amount: number;
  remaining_amount: number;
  note: string | null;
  deadline: string | null;
  is_settled: boolean;
  created_at: string;
}

// ═══ LIBRARY ═══
export type NodeType =
  | "folder" | "pdf" | "pptx" | "video_youtube"
  | "video_local" | "gdrive" | "note" | "web_link";

export interface LibraryNode {
  id: string;
  name: string;
  type: NodeType;
  parent_id: string | null;
  file_path: string | null;
  url: string | null;
  mime_type: string | null;
  file_size: number | null;
  thumbnail_path: string | null;
  notes: string | null;
  is_pinned: boolean;
  tags: string[];
  progress: number;
  last_opened_at: string | null;
  related_goal_id: string | null;
  related_skill_ids: string[];
  order_index: number;
  created_at: string;
}

// ═══ JOURNAL ═══
export type Mood = "😄" | "😊" | "😐" | "😟" | "😢";

export interface JournalEntry {
  id: string;
  date: string;
  content: string;
  mood: Mood | null;
  tags: string[];
  created_at: string;
  updated_at: string;
}

// ═══ CALENDAR ═══
export interface CalendarEvent {
  id: string;
  title: string;
  description: string | null;
  start_time: string;
  end_time: string;
  is_all_day: boolean;
  color: string | null;
  reminder_minutes: number | null;
  is_quest_related: boolean;
  quest_id: string | null;
}

// ═══ HABITS ═══
export interface Habit {
  id: string;
  name: string;
  icon: string;
  frequency: "daily" | "weekly" | "custom";
  target_per_day: number;
  color: string;
  is_active: boolean;
  created_at: string;
}

// ═══ TODO ═══
export type Priority = "urgent" | "high" | "medium" | "low";

export interface Todo {
  id: string;
  title: string;
  description: string | null;
  priority: Priority;
  deadline: string | null;
  is_completed: boolean;
  completed_at: string | null;
  created_at: string;
}

// ═══ AURA CONVERSATIONS ═══
export interface AuraMessage {
  id: string;
  role: "user" | "assistant";
  content: string;
  created_at: string;
}

// ═══ NAV ITEMS ═══
export interface NavItem {
  path: string;
  label: string;
  icon: string;
  badge?: number;
}

export const NAV_ITEMS: NavItem[] = [
  { path: "/", label: "Home", icon: "🏠" },
  { path: "/quests", label: "Quests", icon: "📋" },
  { path: "/skills", label: "Skills", icon: "🌳" },
  { path: "/finance", label: "Finance", icon: "💰" },
  { path: "/library", label: "Library", icon: "📚" },
  { path: "/inventory", label: "Items", icon: "🎒" },
  { path: "/titles", label: "Titles", icon: "🏆" },
  { path: "/calendar", label: "Calendar", icon: "📅" },
  { path: "/journal", label: "Journal", icon: "📓" },
  { path: "/todos", label: "Todos", icon: "✅" },
  { path: "/settings", label: "Settings", icon: "⚙️" },
];
