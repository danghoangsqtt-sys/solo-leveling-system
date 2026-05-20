// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — MORE SCREEN v3
// Grid menu with navigation to sub-screens
// ═══════════════════════════════════════════════════════════════

import React from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors } from '@/constants/Colors';
import { useGame } from '@/constants/GameContext';

type MenuItem = {
  icon: string;
  label: string;
  vi: string;
  color: string;
  route: string;
  badge?: number;
};

export default function MoreScreen() {
  const { theme, isDark } = useTheme();
  const { state } = useGame();

  const activeTodos = state.todos.filter(t => !t.isCompleted).length;
  const newItems = state.inventory.length;

  const ITEMS: MenuItem[] = [
    { icon: '🎒', label: 'Inventory', vi: 'Kho đồ', color: SystemColors.orange, route: '/inventory', badge: newItems > 0 ? newItems : undefined },
    { icon: '🏆', label: 'Titles', vi: 'Danh hiệu', color: SystemColors.gold, route: '/titles' },
    { icon: '📅', label: 'Calendar', vi: 'Lịch', color: SystemColors.cyan, route: '/calendar' },
    { icon: '📓', label: 'Journal', vi: 'Nhật ký', color: SystemColors.purple, route: '/journal' },
    { icon: '✅', label: 'To-Do', vi: 'Việc cần làm', color: SystemColors.green, route: '/todo', badge: activeTodos > 0 ? activeTodos : undefined },
    { icon: '⚠️', label: 'Penalty', vi: 'Hệ thống phạt', color: SystemColors.red, route: '/inventory' },
    { icon: '📚', label: 'Library', vi: 'Tài liệu', color: SystemColors.blue, route: '/inventory' },
    { icon: '⚙️', label: 'Settings', vi: 'Cài đặt', color: '#94A3B8', route: '/settings' },
  ];

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#EDE9FE', '#E0E7FF']} style={st.hdr}>
          <Text style={[st.hdrT, { color: theme.colors.text }]}>📦 More Features</Text>
          <Text style={[st.hdrS, { color: theme.colors.textMuted }]}>Truy cập các tính năng khác</Text>
        </LinearGradient>

        <View style={st.grid}>
          {ITEMS.map(it => (
            <TouchableOpacity
              key={it.label}
              style={[st.item, { backgroundColor: theme.colors.bgCard, borderColor: `${it.color}20` }]}
              activeOpacity={0.7}
              onPress={() => router.push(it.route as any)}>
              {it.badge !== undefined && it.badge > 0 && (
                <View style={[st.badge, { backgroundColor: SystemColors.red }]}>
                  <Text style={st.badgeT}>{it.badge}</Text>
                </View>
              )}
              <View style={[st.iconWrap, { backgroundColor: `${it.color}10` }]}>
                <Text style={{ fontSize: 24 }}>{it.icon}</Text>
              </View>
              <Text style={[st.itemLbl, { color: theme.colors.text }]}>{it.label}</Text>
              <Text style={[st.itemVi, { color: theme.colors.textMuted }]}>{it.vi}</Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Quick stats */}
        <View style={[st.quickStats, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
          <View style={st.qsRow}>
            <Text style={[st.qsLabel, { color: theme.colors.textMuted }]}>📋 Quests hoàn thành:</Text>
            <Text style={[st.qsVal, { color: SystemColors.blue }]}>{state.user?.totalQuestsCompleted || 0}</Text>
          </View>
          <View style={st.qsRow}>
            <Text style={[st.qsLabel, { color: theme.colors.textMuted }]}>🎒 Vật phẩm:</Text>
            <Text style={[st.qsVal, { color: SystemColors.orange }]}>{state.inventory.length}</Text>
          </View>
          <View style={st.qsRow}>
            <Text style={[st.qsLabel, { color: theme.colors.textMuted }]}>🏆 Danh hiệu:</Text>
            <Text style={[st.qsVal, { color: SystemColors.gold }]}>
              {state.titles.filter(t => t.isUnlocked).length}/{state.titles.length}
            </Text>
          </View>
          <View style={st.qsRow}>
            <Text style={[st.qsLabel, { color: theme.colors.textMuted }]}>📓 Nhật ký:</Text>
            <Text style={[st.qsVal, { color: SystemColors.purple }]}>{state.journal.length}</Text>
          </View>
        </View>

        <View style={[st.info, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
          <Text style={[st.infoName, { color: SystemColors.blue }]}>⚔️ System Leveling</Text>
          <Text style={[st.infoVer, { color: theme.colors.textMuted }]}>v1.0.0 — Phase 1</Text>
          <Text style={[st.infoQ, { color: theme.colors.textMuted }]}>"Arise." — The System</Text>
        </View>
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

  grid: { flexDirection: 'row', flexWrap: 'wrap', paddingHorizontal: 10, gap: 8, marginTop: 4 },
  item: { width: '47%', alignItems: 'center', paddingVertical: 16, borderRadius: Radius.lg, borderWidth: 1, position: 'relative' },
  badge: { position: 'absolute', top: 6, right: 6, minWidth: 18, height: 18, borderRadius: 9, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 4 },
  badgeT: { color: '#FFF', fontSize: 9, fontWeight: '800' },
  iconWrap: { width: 48, height: 48, borderRadius: 24, alignItems: 'center', justifyContent: 'center', marginBottom: 8 },
  itemLbl: { ...Typography.bodyBold },
  itemVi: { ...Typography.label, marginTop: 2 },

  quickStats: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1 },
  qsRow: { flexDirection: 'row', justifyContent: 'space-between', paddingVertical: 4 },
  qsLabel: { ...Typography.body },
  qsVal: { ...Typography.mono },

  info: { marginHorizontal: 10, marginTop: 16, borderRadius: Radius.lg, padding: 16, borderWidth: 1, alignItems: 'center' },
  infoName: { ...Typography.h3 },
  infoVer: { ...Typography.label, marginTop: 2 },
  infoQ: { ...Typography.caption, fontStyle: 'italic', marginTop: 6 },
});
