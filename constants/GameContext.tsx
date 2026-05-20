// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — Global Game Context
// React Context + useReducer for all game state management
// Persists to AsyncStorage on every state change
// ═══════════════════════════════════════════════════════════════

import React, { createContext, useContext, useReducer, useEffect, useCallback, useRef } from 'react';
import type {
  UserProfile, Quest, InventoryItem, Title, Goal, Skill,
  Transaction, JournalEntry, CalendarEvent, Habit, TodoItem,
  PenaltyLog, QuestStatus, CharacterStats, StatKey,
} from './Types';
import * as Storage from './Storage';
import { getExpToNextLevel } from './GameData';

// ═══ STATE SHAPE ═══
export interface GameState {
  isLoading: boolean;
  isOnboarded: boolean;
  user: UserProfile | null;
  quests: Quest[];
  inventory: InventoryItem[];
  titles: Title[];
  goals: Goal[];
  skills: Skill[];
  transactions: Transaction[];
  journal: JournalEntry[];
  calendar: CalendarEvent[];
  habits: Habit[];
  todos: TodoItem[];
  penalties: PenaltyLog[];
  settings: Storage.AppSettings;
}

const initialState: GameState = {
  isLoading: true,
  isOnboarded: false,
  user: null,
  quests: [],
  inventory: [],
  titles: [],
  goals: [],
  skills: [],
  transactions: [],
  journal: [],
  calendar: [],
  habits: [],
  todos: [],
  penalties: [],
  settings: Storage.DEFAULT_SETTINGS,
};

// ═══ ACTIONS ═══
type GameAction =
  | { type: 'INIT'; payload: Partial<GameState> }
  | { type: 'SET_USER'; payload: UserProfile }
  | { type: 'SET_ONBOARDED'; payload: boolean }
  | { type: 'UPDATE_STATS'; payload: Partial<CharacterStats> }
  | { type: 'ADD_EXP'; payload: number }
  | { type: 'ADD_GOLD'; payload: number }
  | { type: 'ADD_GEMS'; payload: number }
  | { type: 'ADD_DEBT'; payload: number }
  | { type: 'SET_QUESTS'; payload: Quest[] }
  | { type: 'UPDATE_QUEST'; payload: { id: string; updates: Partial<Quest> } }
  | { type: 'COMPLETE_QUEST'; payload: string }
  | { type: 'FAIL_QUEST'; payload: string }
  | { type: 'SET_INVENTORY'; payload: InventoryItem[] }
  | { type: 'ADD_ITEM'; payload: InventoryItem }
  | { type: 'SET_TITLES'; payload: Title[] }
  | { type: 'UNLOCK_TITLE'; payload: string }
  | { type: 'EQUIP_TITLE'; payload: string }
  | { type: 'SET_GOALS'; payload: Goal[] }
  | { type: 'SET_SKILLS'; payload: Skill[] }
  | { type: 'SET_TRANSACTIONS'; payload: Transaction[] }
  | { type: 'ADD_TRANSACTION'; payload: Transaction }
  | { type: 'SET_JOURNAL'; payload: JournalEntry[] }
  | { type: 'ADD_JOURNAL'; payload: JournalEntry }
  | { type: 'SET_CALENDAR'; payload: CalendarEvent[] }
  | { type: 'SET_HABITS'; payload: Habit[] }
  | { type: 'SET_TODOS'; payload: TodoItem[] }
  | { type: 'ADD_TODO'; payload: TodoItem }
  | { type: 'UPDATE_TODO'; payload: { id: string; updates: Partial<TodoItem> } }
  | { type: 'DELETE_TODO'; payload: string }
  | { type: 'SET_PENALTIES'; payload: PenaltyLog[] }
  | { type: 'SET_SETTINGS'; payload: Storage.AppSettings }
  | { type: 'RESET' };

// ═══ LEVEL UP HELPER ═══
function processExp(user: UserProfile, addedExp: number): UserProfile {
  let { level, currentExp, expToNextLevel } = user;
  currentExp += addedExp;

  while (currentExp >= expToNextLevel) {
    currentExp -= expToNextLevel;
    level++;
    expToNextLevel = getExpToNextLevel(level);
  }

  return { ...user, level, currentExp, expToNextLevel, updatedAt: Date.now() };
}

// ═══ REDUCER ═══
function gameReducer(state: GameState, action: GameAction): GameState {
  switch (action.type) {
    case 'INIT':
      return { ...state, ...action.payload, isLoading: false };

    case 'SET_USER':
      return { ...state, user: action.payload };

    case 'SET_ONBOARDED':
      return { ...state, isOnboarded: action.payload };

    case 'UPDATE_STATS':
      if (!state.user) return state;
      return {
        ...state,
        user: {
          ...state.user,
          stats: { ...state.user.stats, ...action.payload },
          updatedAt: Date.now(),
        },
      };

    case 'ADD_EXP':
      if (!state.user) return state;
      return { ...state, user: processExp(state.user, action.payload) };

    case 'ADD_GOLD':
      if (!state.user) return state;
      return {
        ...state,
        user: {
          ...state.user,
          gold: state.user.gold + action.payload,
          updatedAt: Date.now(),
        },
      };

    case 'ADD_GEMS':
      if (!state.user) return state;
      return {
        ...state,
        user: {
          ...state.user,
          gems: state.user.gems + action.payload,
          updatedAt: Date.now(),
        },
      };

    case 'ADD_DEBT':
      if (!state.user) return state;
      return {
        ...state,
        user: {
          ...state.user,
          debtPoints: Math.max(0, state.user.debtPoints + action.payload),
          updatedAt: Date.now(),
        },
      };

    case 'SET_QUESTS':
      return { ...state, quests: action.payload };

    case 'UPDATE_QUEST':
      return {
        ...state,
        quests: state.quests.map(q =>
          q.id === action.payload.id ? { ...q, ...action.payload.updates } : q
        ),
      };

    case 'COMPLETE_QUEST': {
      const quest = state.quests.find(q => q.id === action.payload);
      if (!quest || !state.user) return state;

      const updatedQuests = state.quests.map(q =>
        q.id === action.payload
          ? { ...q, status: 'completed' as QuestStatus, completedAt: Date.now() }
          : q
      );

      let updatedUser = processExp(state.user, quest.rewards.exp);
      updatedUser = {
        ...updatedUser,
        gold: updatedUser.gold + quest.rewards.gold,
        totalQuestsCompleted: updatedUser.totalQuestsCompleted + 1,
      };

      return { ...state, quests: updatedQuests, user: updatedUser };
    }

    case 'FAIL_QUEST': {
      const quest = state.quests.find(q => q.id === action.payload);
      if (!quest || !state.user) return state;

      const updatedQuests = state.quests.map(q =>
        q.id === action.payload
          ? { ...q, status: 'failed' as QuestStatus }
          : q
      );

      return {
        ...state,
        quests: updatedQuests,
        user: {
          ...state.user,
          debtPoints: state.user.debtPoints + quest.penaltyOnFail.debtPoints,
          updatedAt: Date.now(),
        },
      };
    }

    case 'SET_INVENTORY':
      return { ...state, inventory: action.payload };

    case 'ADD_ITEM':
      return { ...state, inventory: [...state.inventory, action.payload] };

    case 'SET_TITLES':
      return { ...state, titles: action.payload };

    case 'UNLOCK_TITLE':
      return {
        ...state,
        titles: state.titles.map(t =>
          t.id === action.payload
            ? { ...t, isUnlocked: true, unlockedAt: Date.now() }
            : t
        ),
      };

    case 'EQUIP_TITLE':
      if (!state.user) return state;
      return {
        ...state,
        user: { ...state.user, equippedTitleId: action.payload, updatedAt: Date.now() },
      };

    case 'SET_GOALS':
      return { ...state, goals: action.payload };

    case 'SET_SKILLS':
      return { ...state, skills: action.payload };

    case 'SET_TRANSACTIONS':
      return { ...state, transactions: action.payload };

    case 'ADD_TRANSACTION':
      return { ...state, transactions: [action.payload, ...state.transactions] };

    case 'SET_JOURNAL':
      return { ...state, journal: action.payload };

    case 'ADD_JOURNAL':
      return { ...state, journal: [action.payload, ...state.journal] };

    case 'SET_CALENDAR':
      return { ...state, calendar: action.payload };

    case 'SET_HABITS':
      return { ...state, habits: action.payload };

    case 'SET_TODOS':
      return { ...state, todos: action.payload };

    case 'ADD_TODO':
      return { ...state, todos: [action.payload, ...state.todos] };

    case 'UPDATE_TODO':
      return {
        ...state,
        todos: state.todos.map(t =>
          t.id === action.payload.id ? { ...t, ...action.payload.updates } : t
        ),
      };

    case 'DELETE_TODO':
      return {
        ...state,
        todos: state.todos.filter(t => t.id !== action.payload),
      };

    case 'SET_PENALTIES':
      return { ...state, penalties: action.payload };

    case 'SET_SETTINGS':
      return { ...state, settings: action.payload };

    case 'RESET':
      return { ...initialState, isLoading: false };

    default:
      return state;
  }
}

// ═══ CONTEXT ═══
interface GameContextType {
  state: GameState;
  dispatch: React.Dispatch<GameAction>;
  actions: {
    completeQuest: (id: string) => void;
    failQuest: (id: string) => void;
    addTodo: (title: string, priority?: TodoItem['priority']) => void;
    toggleTodo: (id: string) => void;
    deleteTodo: (id: string) => void;
    addJournalEntry: (content: string, mood?: JournalEntry['mood']) => void;
    addTransaction: (tx: Omit<Transaction, 'id' | 'createdAt'>) => void;
    equipTitle: (id: string) => void;
    resetData: () => Promise<void>;
  };
}

const GameContext = createContext<GameContextType | null>(null);

// ═══ PROVIDER ═══
export function GameProvider({ children }: { children: React.ReactNode }) {
  const [state, dispatch] = useReducer(gameReducer, initialState);
  const isInitialized = useRef(false);

  // Load all data from storage on mount
  useEffect(() => {
    (async () => {
      try {
        const [
          isOnboarded, user, quests, inventory, titles,
          goals, skills, transactions, journal, calendar,
          habits, todos, penalties, settings,
        ] = await Promise.all([
          Storage.isOnboardingDone(),
          Storage.loadUserProfile(),
          Storage.loadQuests(),
          Storage.loadInventory(),
          Storage.loadTitles(),
          Storage.loadGoals(),
          Storage.loadSkills(),
          Storage.loadTransactions(),
          Storage.loadJournal(),
          Storage.loadCalendarEvents(),
          Storage.loadHabits(),
          Storage.loadTodos(),
          Storage.loadPenalties(),
          Storage.loadSettings(),
        ]);

        dispatch({
          type: 'INIT',
          payload: {
            isOnboarded,
            user,
            quests,
            inventory,
            titles,
            goals,
            skills,
            transactions,
            journal,
            calendar,
            habits,
            todos,
            penalties,
            settings,
          },
        });
        isInitialized.current = true;
      } catch (e) {
        console.error('[GameContext] Init failed:', e);
        dispatch({ type: 'INIT', payload: {} });
      }
    })();
  }, []);

  // Persist state changes to AsyncStorage
  useEffect(() => {
    if (!isInitialized.current) return;

    const persist = async () => {
      const promises: Promise<void>[] = [];
      if (state.user) promises.push(Storage.saveUserProfile(state.user));
      promises.push(Storage.setOnboardingDone(state.isOnboarded));
      if (state.quests.length) promises.push(Storage.saveQuests(state.quests));
      if (state.inventory.length) promises.push(Storage.saveInventory(state.inventory));
      if (state.titles.length) promises.push(Storage.saveTitles(state.titles));
      if (state.goals.length) promises.push(Storage.saveGoals(state.goals));
      if (state.skills.length) promises.push(Storage.saveSkills(state.skills));
      if (state.transactions.length) promises.push(Storage.saveTransactions(state.transactions));
      if (state.journal.length) promises.push(Storage.saveJournal(state.journal));
      if (state.calendar.length) promises.push(Storage.saveCalendarEvents(state.calendar));
      if (state.habits.length) promises.push(Storage.saveHabits(state.habits));
      if (state.todos.length) promises.push(Storage.saveTodos(state.todos));
      if (state.penalties.length) promises.push(Storage.savePenalties(state.penalties));
      promises.push(Storage.saveSettings(state.settings));
      await Promise.all(promises);
    };

    persist();
  }, [state]);

  // ═══ CONVENIENCE ACTIONS ═══
  const actions = {
    completeQuest: useCallback((id: string) => {
      dispatch({ type: 'COMPLETE_QUEST', payload: id });
    }, []),

    failQuest: useCallback((id: string) => {
      dispatch({ type: 'FAIL_QUEST', payload: id });
    }, []),

    addTodo: useCallback((title: string, priority: TodoItem['priority'] = 'medium') => {
      const todo: TodoItem = {
        id: Storage.generateId(),
        title,
        description: null,
        priority,
        deadline: null,
        isCompleted: false,
        completedAt: null,
        createdAt: Date.now(),
      };
      dispatch({ type: 'ADD_TODO', payload: todo });
    }, []),

    toggleTodo: useCallback((id: string) => {
      dispatch({
        type: 'UPDATE_TODO',
        payload: {
          id,
          updates: { isCompleted: true, completedAt: Date.now() },
        },
      });
    }, []),

    deleteTodo: useCallback((id: string) => {
      dispatch({ type: 'DELETE_TODO', payload: id });
    }, []),

    addJournalEntry: useCallback((content: string, mood: JournalEntry['mood'] = null) => {
      const entry: JournalEntry = {
        id: Storage.generateId(),
        date: Date.now(),
        content,
        mood,
        tags: [],
        createdAt: Date.now(),
        updatedAt: Date.now(),
      };
      dispatch({ type: 'ADD_JOURNAL', payload: entry });
    }, []),

    addTransaction: useCallback((tx: Omit<Transaction, 'id' | 'createdAt'>) => {
      const full: Transaction = {
        ...tx,
        id: Storage.generateId(),
        createdAt: Date.now(),
      };
      dispatch({ type: 'ADD_TRANSACTION', payload: full });
    }, []),

    equipTitle: useCallback((id: string) => {
      dispatch({ type: 'EQUIP_TITLE', payload: id });
    }, []),

    resetData: useCallback(async () => {
      await Storage.resetAllData();
      dispatch({ type: 'RESET' });
    }, []),
  };

  return (
    <GameContext.Provider value={{ state, dispatch, actions }}>
      {children}
    </GameContext.Provider>
  );
}

// ═══ HOOK ═══
export function useGame() {
  const ctx = useContext(GameContext);
  if (!ctx) throw new Error('useGame must be used within GameProvider');
  return ctx;
}
