// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — Storage Layer (AsyncStorage)
// Offline-first data persistence for all game entities
// ═══════════════════════════════════════════════════════════════

import AsyncStorage from '@react-native-async-storage/async-storage';
import type {
  UserProfile, Quest, InventoryItem, Title,
  Transaction, BudgetCategory, JournalEntry,
  CalendarEvent, Habit, TodoItem, PenaltyLog, Goal, Skill,
} from './Types';

// ═══ STORAGE KEYS ═══
const KEYS = {
  USER: '@sl_user_profile',
  QUESTS: '@sl_quests',
  INVENTORY: '@sl_inventory',
  TITLES: '@sl_titles',
  GOALS: '@sl_goals',
  SKILLS: '@sl_skills',
  TRANSACTIONS: '@sl_transactions',
  BUDGETS: '@sl_budgets',
  JOURNAL: '@sl_journal',
  CALENDAR: '@sl_calendar',
  HABITS: '@sl_habits',
  TODOS: '@sl_todos',
  PENALTIES: '@sl_penalties',
  ONBOARDING_DONE: '@sl_onboarding_done',
  SETTINGS: '@sl_settings',
} as const;

// ═══ GENERIC HELPERS ═══
async function saveJSON<T>(key: string, data: T): Promise<void> {
  try {
    await AsyncStorage.setItem(key, JSON.stringify(data));
  } catch (e) {
    console.error(`[Storage] Failed to save ${key}:`, e);
  }
}

async function loadJSON<T>(key: string): Promise<T | null> {
  try {
    const raw = await AsyncStorage.getItem(key);
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    console.error(`[Storage] Failed to load ${key}:`, e);
    return null;
  }
}

// ═══ USER PROFILE ═══
export async function saveUserProfile(user: UserProfile): Promise<void> {
  await saveJSON(KEYS.USER, { ...user, updatedAt: Date.now() });
}

export async function loadUserProfile(): Promise<UserProfile | null> {
  return loadJSON<UserProfile>(KEYS.USER);
}

// ═══ ONBOARDING STATE ═══
export async function setOnboardingDone(done: boolean): Promise<void> {
  await AsyncStorage.setItem(KEYS.ONBOARDING_DONE, JSON.stringify(done));
}

export async function isOnboardingDone(): Promise<boolean> {
  const val = await AsyncStorage.getItem(KEYS.ONBOARDING_DONE);
  return val === 'true';
}

// ═══ QUESTS ═══
export async function saveQuests(quests: Quest[]): Promise<void> {
  await saveJSON(KEYS.QUESTS, quests);
}

export async function loadQuests(): Promise<Quest[]> {
  return (await loadJSON<Quest[]>(KEYS.QUESTS)) ?? [];
}

// ═══ INVENTORY ═══
export async function saveInventory(items: InventoryItem[]): Promise<void> {
  await saveJSON(KEYS.INVENTORY, items);
}

export async function loadInventory(): Promise<InventoryItem[]> {
  return (await loadJSON<InventoryItem[]>(KEYS.INVENTORY)) ?? [];
}

// ═══ TITLES ═══
export async function saveTitles(titles: Title[]): Promise<void> {
  await saveJSON(KEYS.TITLES, titles);
}

export async function loadTitles(): Promise<Title[]> {
  return (await loadJSON<Title[]>(KEYS.TITLES)) ?? [];
}

// ═══ GOALS ═══
export async function saveGoals(goals: Goal[]): Promise<void> {
  await saveJSON(KEYS.GOALS, goals);
}

export async function loadGoals(): Promise<Goal[]> {
  return (await loadJSON<Goal[]>(KEYS.GOALS)) ?? [];
}

// ═══ SKILLS ═══
export async function saveSkills(skills: Skill[]): Promise<void> {
  await saveJSON(KEYS.SKILLS, skills);
}

export async function loadSkills(): Promise<Skill[]> {
  return (await loadJSON<Skill[]>(KEYS.SKILLS)) ?? [];
}

// ═══ TRANSACTIONS ═══
export async function saveTransactions(txs: Transaction[]): Promise<void> {
  await saveJSON(KEYS.TRANSACTIONS, txs);
}

export async function loadTransactions(): Promise<Transaction[]> {
  return (await loadJSON<Transaction[]>(KEYS.TRANSACTIONS)) ?? [];
}

// ═══ JOURNAL ═══
export async function saveJournal(entries: JournalEntry[]): Promise<void> {
  await saveJSON(KEYS.JOURNAL, entries);
}

export async function loadJournal(): Promise<JournalEntry[]> {
  return (await loadJSON<JournalEntry[]>(KEYS.JOURNAL)) ?? [];
}

// ═══ CALENDAR ═══
export async function saveCalendarEvents(events: CalendarEvent[]): Promise<void> {
  await saveJSON(KEYS.CALENDAR, events);
}

export async function loadCalendarEvents(): Promise<CalendarEvent[]> {
  return (await loadJSON<CalendarEvent[]>(KEYS.CALENDAR)) ?? [];
}

// ═══ HABITS ═══
export async function saveHabits(habits: Habit[]): Promise<void> {
  await saveJSON(KEYS.HABITS, habits);
}

export async function loadHabits(): Promise<Habit[]> {
  return (await loadJSON<Habit[]>(KEYS.HABITS)) ?? [];
}

// ═══ TODOS ═══
export async function saveTodos(todos: TodoItem[]): Promise<void> {
  await saveJSON(KEYS.TODOS, todos);
}

export async function loadTodos(): Promise<TodoItem[]> {
  return (await loadJSON<TodoItem[]>(KEYS.TODOS)) ?? [];
}

// ═══ PENALTIES ═══
export async function savePenalties(logs: PenaltyLog[]): Promise<void> {
  await saveJSON(KEYS.PENALTIES, logs);
}

export async function loadPenalties(): Promise<PenaltyLog[]> {
  return (await loadJSON<PenaltyLog[]>(KEYS.PENALTIES)) ?? [];
}

// ═══ SETTINGS ═══
export interface AppSettings {
  theme: 'light' | 'dark' | 'auto';
  language: 'vi' | 'en';
  notifications: boolean;
  aiProvider: 'claude' | 'gemini' | null;
  aiApiKey: string | null;
  penaltyEnabled: boolean;
  questsPerDay: number;
}

export const DEFAULT_SETTINGS: AppSettings = {
  theme: 'light',
  language: 'vi',
  notifications: true,
  aiProvider: null,
  aiApiKey: null,
  penaltyEnabled: true,
  questsPerDay: 8,
};

export async function saveSettings(settings: AppSettings): Promise<void> {
  await saveJSON(KEYS.SETTINGS, settings);
}

export async function loadSettings(): Promise<AppSettings> {
  return (await loadJSON<AppSettings>(KEYS.SETTINGS)) ?? DEFAULT_SETTINGS;
}

// ═══ RESET ALL DATA ═══
export async function resetAllData(): Promise<void> {
  const keys = Object.values(KEYS);
  await AsyncStorage.multiRemove(keys);
}

// ═══ GENERATE ID ═══
export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;
}
