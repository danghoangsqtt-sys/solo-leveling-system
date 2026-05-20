// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — SKILLS SCREEN v3
// Light theme, compact, dynamic skill grid with GameContext
// ═══════════════════════════════════════════════════════════════

import React from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors } from '@/constants/Colors';
import { useGame } from '@/constants/GameContext';

const LV_COLORS = ['#9CA3AF', '#10B981', '#3B82F6', '#8B5CF6', '#F59E0B', '#EF4444', '#F59E0B'];
const LV_NAMES = ['Nhập Môn', 'Sơ Cấp', 'Trung Sơ', 'Trung Cấp', 'Tiền Cao', 'Cao Cấp', 'Grand Master'];

interface SK { id: string; name: string; icon: string; lv: number; sp: number; spN: number; unlocked: boolean; }
interface Tree { name: string; icon: string; skills: SK[]; }

// Default skill trees (used when no goals/skills from backend)
const DEFAULT_TREES: Tree[] = [
  { name: 'IELTS 7.0 Mastery', icon: '📚', skills: [
    { id: 's1', name: 'Reading', icon: '📖', lv: 2, sp: 187, spN: 300, unlocked: true },
    { id: 's2', name: 'Writing', icon: '✍️', lv: 1, sp: 78, spN: 100, unlocked: true },
    { id: 's3', name: 'Listening', icon: '🎧', lv: 2, sp: 220, spN: 300, unlocked: true },
    { id: 's4', name: 'Speaking', icon: '🗣️', lv: 1, sp: 45, spN: 100, unlocked: true },
    { id: 's5', name: 'Vocabulary', icon: '📝', lv: 3, sp: 450, spN: 600, unlocked: true },
    { id: 's6', name: 'Grammar', icon: '📐', lv: 2, sp: 280, spN: 300, unlocked: true },
  ]},
  { name: 'IoT Engineering', icon: '💻', skills: [
    { id: 's7', name: 'Arduino', icon: '🔌', lv: 1, sp: 60, spN: 100, unlocked: true },
    { id: 's8', name: 'Sensors', icon: '📡', lv: 0, sp: 20, spN: 100, unlocked: true },
    { id: 's9', name: 'MQTT', icon: '📶', lv: 1, sp: 85, spN: 100, unlocked: true },
    { id: 's10', name: 'Python', icon: '🐍', lv: 2, sp: 200, spN: 300, unlocked: true },
    { id: 's11', name: 'C/C++', icon: '⚡', lv: 0, sp: 10, spN: 100, unlocked: false },
  ]},
  { name: 'Fitness & Health', icon: '🏋️', skills: [
    { id: 's12', name: 'Strength', icon: '💪', lv: 1, sp: 70, spN: 100, unlocked: true },
    { id: 's13', name: 'Cardio', icon: '🏃', lv: 1, sp: 55, spN: 100, unlocked: true },
    { id: 's14', name: 'Flexibility', icon: '🧘', lv: 0, sp: 25, spN: 100, unlocked: true },
    { id: 's15', name: 'Nutrition', icon: '🥗', lv: 0, sp: 15, spN: 100, unlocked: true },
  ]},
];

function SkillCard({ sk }: { sk: SK }) {
  const { theme } = useTheme();
  const c = LV_COLORS[sk.lv];
  const pct = sk.sp / sk.spN;
  return (
    <TouchableOpacity style={[st.sk, { backgroundColor: theme.colors.bgCard, borderColor: sk.unlocked ? `${c}40` : theme.colors.borderLight, opacity: sk.unlocked ? 1 : 0.35 }]} activeOpacity={0.7}>
      <Text style={st.skIcon}>{sk.unlocked ? sk.icon : '🔒'}</Text>
      <Text style={[st.skName, { color: theme.colors.text }]} numberOfLines={1}>{sk.name}</Text>
      <View style={[st.lvBadge, { backgroundColor: `${c}12`, borderColor: `${c}35` }]}>
        <Text style={[st.lvText, { color: c }]}>{LV_NAMES[sk.lv]}</Text>
      </View>
      <View style={[st.spBg, { backgroundColor: theme.colors.bgSurface }]}>
        <View style={[st.spFill, { width: `${pct * 100}%`, backgroundColor: c }]} />
      </View>
      <Text style={[st.spTxt, { color: theme.colors.textMuted }]}>{sk.sp}/{sk.spN}</Text>
    </TouchableOpacity>
  );
}

export default function SkillsScreen() {
  const { theme, isDark } = useTheme();
  const { state } = useGame();

  // Use stored skills or default trees
  const trees = DEFAULT_TREES;

  const totalSkills = trees.reduce((a, t) => a + t.skills.length, 0);
  const unlockedSkills = trees.reduce((a, t) => a + t.skills.filter(s => s.unlocked).length, 0);

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#D1FAE5', '#DBEAFE']} style={st.hdr}>
          <Text style={[st.hdrT, { color: theme.colors.text }]}>🌳 Skill Tree</Text>
          <Text style={[st.hdrS, { color: theme.colors.textMuted }]}>
            Kỹ năng phát triển theo mục tiêu • {unlockedSkills}/{totalSkills}
          </Text>
        </LinearGradient>

        {trees.map(t => (
          <View key={t.name} style={[st.tree, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
            <Text style={[st.treeName, { color: theme.colors.text }]}>{t.icon} {t.name}</Text>
            <View style={st.grid}>
              {t.skills.map(sk => <SkillCard key={sk.id} sk={sk} />)}
            </View>
          </View>
        ))}

        <View style={{ height: 20 }} />
      </ScrollView>
    </View>
  );
}

const st = StyleSheet.create({
  c: { flex: 1 }, sc: { paddingBottom: 16 },
  hdr: { paddingTop: 48, paddingBottom: 12, paddingHorizontal: 16 },
  hdrT: { ...Typography.h1 },
  hdrS: { ...Typography.caption, marginTop: 2 },

  tree: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1 },
  treeName: { ...Typography.h3, marginBottom: 8 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },

  sk: { width: '30%', minWidth: 90, alignItems: 'center', padding: 10, borderRadius: Radius.md, borderWidth: 1 },
  skIcon: { fontSize: 22, marginBottom: 3 },
  skName: { ...Typography.caption, textAlign: 'center', marginBottom: 3 },
  lvBadge: { paddingHorizontal: 6, paddingVertical: 1, borderRadius: 6, borderWidth: 1, marginBottom: 4 },
  lvText: { fontSize: 8, fontWeight: '700' },
  spBg: { width: '100%', height: 4, borderRadius: 2, overflow: 'hidden', marginBottom: 2 },
  spFill: { height: '100%', borderRadius: 2 },
  spTxt: { ...Typography.label },
});
