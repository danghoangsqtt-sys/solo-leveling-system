// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — Theme Context v2
// Light-first theme with Solo Leveling: Arise aesthetic
// ═══════════════════════════════════════════════════════════════

import React, { createContext, useContext, useState, useMemo } from 'react';
import { DarkTheme, LightTheme, SystemColors, Gradients } from './Colors';

export type ThemeMode = 'dark' | 'light';

type ThemeColors = {
  bg: string;
  bgCard: string;
  bgElevated: string;
  bgSurface: string;
  bgOverlay: string;
  bgGlass: string;
  text: string;
  textSecondary: string;
  textMuted: string;
  border: string;
  borderLight: string;
  shadow: string;
  shadowStrong: string;
};

export interface Theme {
  mode: ThemeMode;
  colors: ThemeColors;
  system: typeof SystemColors;
  gradients: typeof Gradients;
}

const lightTheme: Theme = {
  mode: 'light',
  colors: LightTheme,
  system: SystemColors,
  gradients: Gradients,
};

const darkTheme: Theme = {
  mode: 'dark',
  colors: DarkTheme,
  system: SystemColors,
  gradients: Gradients,
};

interface ThemeContextType {
  theme: Theme;
  toggleTheme: () => void;
  isDark: boolean;
}

const ThemeContext = createContext<ThemeContextType>({
  theme: lightTheme,
  toggleTheme: () => {},
  isDark: false,
});

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [mode, setMode] = useState<ThemeMode>('light'); // Light default

  const value = useMemo(() => ({
    theme: mode === 'dark' ? darkTheme : lightTheme,
    toggleTheme: () => setMode(m => m === 'dark' ? 'light' : 'dark'),
    isDark: mode === 'dark',
  }), [mode]);

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}

// ═══ TYPOGRAPHY SCALE (compact, font ~13-15) ═══
export const Typography = {
  h1: { fontSize: 18, fontWeight: '800' as const, letterSpacing: 0.5 },
  h2: { fontSize: 15, fontWeight: '700' as const, letterSpacing: 0.3 },
  h3: { fontSize: 13, fontWeight: '700' as const },
  body: { fontSize: 13, fontWeight: '400' as const },
  bodyBold: { fontSize: 13, fontWeight: '600' as const },
  caption: { fontSize: 11, fontWeight: '500' as const },
  captionBold: { fontSize: 11, fontWeight: '700' as const },
  label: { fontSize: 10, fontWeight: '600' as const },
  mono: { fontSize: 12, fontWeight: '600' as const, fontFamily: 'SpaceMono' },
  monoSmall: { fontSize: 10, fontWeight: '600' as const, fontFamily: 'SpaceMono' },
  stat: { fontSize: 15, fontWeight: '800' as const, fontFamily: 'SpaceMono' },
} as const;

// ═══ SPACING ═══
export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  xxl: 28,
} as const;

// ═══ BORDER RADIUS ═══
export const Radius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  full: 999,
} as const;
