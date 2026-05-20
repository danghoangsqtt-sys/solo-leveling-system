// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — Design System Colors v2
// Light theme default + Solo Leveling: Arise inspired palette
// ═══════════════════════════════════════════════════════════════

// ═══ PRIMARY SYSTEM COLORS ═══
export const SystemColors = {
  blue: '#3B82F6',
  gold: '#F59E0B',
  purple: '#8B5CF6',
  red: '#EF4444',
  green: '#10B981',
  orange: '#F97316',
  cyan: '#06B6D4',
  pink: '#EC4899',
  indigo: '#6366F1',
} as const;

// ═══ LIGHT THEME (Default) ═══
export const LightTheme = {
  bg: '#F0F4FF',
  bgCard: '#FFFFFF',
  bgElevated: '#F8FAFF',
  bgSurface: '#EDF2FF',
  bgOverlay: 'rgba(240, 244, 255, 0.92)',
  bgGlass: 'rgba(255, 255, 255, 0.75)',
  text: '#1E293B',
  textSecondary: '#475569',
  textMuted: '#94A3B8',
  border: 'rgba(100, 116, 162, 0.15)',
  borderLight: 'rgba(100, 116, 162, 0.08)',
  shadow: 'rgba(55, 65, 120, 0.08)',
  shadowStrong: 'rgba(55, 65, 120, 0.16)',
} as const;

// ═══ DARK THEME ═══
export const DarkTheme = {
  bg: '#0B0E1A',
  bgCard: '#151929',
  bgElevated: '#1E2338',
  bgSurface: '#121627',
  bgOverlay: 'rgba(11, 14, 26, 0.9)',
  bgGlass: 'rgba(21, 25, 41, 0.8)',
  text: '#E8ECF4',
  textSecondary: '#94A3CC',
  textMuted: '#5B6688',
  border: 'rgba(74, 158, 255, 0.15)',
  borderLight: 'rgba(255, 255, 255, 0.06)',
  shadow: 'rgba(0, 0, 0, 0.3)',
  shadowStrong: 'rgba(0, 0, 0, 0.5)',
} as const;

// ═══ RARITY COLORS ═══
export const RarityColors = {
  common: '#9CA3AF',
  uncommon: '#10B981',
  rare: '#3B82F6',
  epic: '#8B5CF6',
  legendary: '#F59E0B',
  mythic: '#EF4444',
} as const;

export type RarityTier = keyof typeof RarityColors;

// ═══ STAT COLORS ═══
export const StatColors = {
  STR: '#EF4444',
  INT: '#3B82F6',
  AGI: '#10B981',
  VIT: '#F59E0B',
  WIS: '#8B5CF6',
  CHA: '#EC4899',
} as const;

export type StatKey = keyof typeof StatColors;

// ═══ QUEST RANK COLORS ═══
export const QuestRankColors = {
  E: '#9CA3AF',
  D: '#10B981',
  C: '#3B82F6',
  B: '#8B5CF6',
  A: '#F59E0B',
  S: '#EF4444',
} as const;

export type QuestRank = keyof typeof QuestRankColors;

// ═══ QUEST TYPE COLORS ═══
export const QuestTypeColors = {
  daily: '#10B981',
  weekly: '#3B82F6',
  boss: '#8B5CF6',
  penalty: '#EF4444',
  event: '#F59E0B',
  side: '#9CA3AF',
} as const;

// ═══ FINANCE COLORS ═══
export const FinanceColors = {
  income: '#10B981',
  expense: '#EF4444',
  saving: '#3B82F6',
  neutral: '#9CA3AF',
} as const;

// ═══ GRADIENTS ═══
export const Gradients = {
  systemBlue: ['#3B82F6', '#1D4ED8'] as const,
  systemGold: ['#F59E0B', '#D97706'] as const,
  systemPurple: ['#8B5CF6', '#6D28D9'] as const,
  systemRed: ['#EF4444', '#DC2626'] as const,
  systemGreen: ['#10B981', '#059669'] as const,
  systemCyan: ['#06B6D4', '#0891B2'] as const,
  expBar: ['#3B82F6', '#8B5CF6', '#EC4899'] as const,
  heroCard: ['#EDF2FF', '#DBEAFE'] as const,
  heroDark: ['#151929', '#1E2338'] as const,
  goldShimmer: ['#F59E0B', '#FBBF24', '#F59E0B'] as const,
  characterGlow: ['rgba(59,130,246,0)', 'rgba(59,130,246,0.15)', 'rgba(59,130,246,0)'] as const,
  statusHeader: ['#1E3A8A', '#3B82F6', '#6366F1'] as const,
  statusHeaderLight: ['#DBEAFE', '#EDE9FE', '#E0E7FF'] as const,
} as const;

// ═══ CLASS COLORS ═══
export const ClassColors: Record<string, string> = {
  warrior: '#EF4444',
  mage: '#3B82F6',
  ranger: '#10B981',
  guardian: '#F59E0B',
  bard: '#EC4899',
  alchemist: '#06B6D4',
  healer: '#8B5CF6',
  scholar: '#F59E0B',
};

// ═══ LEVEL BACKGROUND THEMES ═══
export const LevelThemes = {
  forest: { range: [1, 10], gradient: ['#D1FAE5', '#A7F3D0', '#6EE7B7'] },
  desert: { range: [11, 20], gradient: ['#FEF3C7', '#FDE68A', '#FCD34D'] },
  ocean: { range: [21, 30], gradient: ['#DBEAFE', '#BFDBFE', '#93C5FD'] },
  mountain: { range: [31, 40], gradient: ['#E5E7EB', '#D1D5DB', '#9CA3AF'] },
  castle: { range: [41, 50], gradient: ['#EDE9FE', '#DDD6FE', '#C4B5FD'] },
  galaxy: { range: [51, 999], gradient: ['#1E1B4B', '#312E81', '#4338CA'] },
} as const;
