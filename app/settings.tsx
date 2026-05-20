// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — SETTINGS SCREEN
// Profile, Theme, AI Config, Language, Backup, Reset
// ═══════════════════════════════════════════════════════════════

import React, { useState } from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, TextInput, Alert, Switch, Platform } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors } from '@/constants/Colors';
import { useGame } from '@/constants/GameContext';
import type { AppSettings } from '@/constants/Storage';

function SettingRow({ icon, label, value, onPress, color }: { icon: string; label: string; value?: string; onPress?: () => void; color?: string }) {
  const { theme } = useTheme();
  return (
    <TouchableOpacity
      style={[st.row, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}
      onPress={onPress}
      activeOpacity={onPress ? 0.7 : 1}
      disabled={!onPress}>
      <Text style={{ fontSize: 18 }}>{icon}</Text>
      <Text style={[st.rowLabel, { color: theme.colors.text }]}>{label}</Text>
      {value && <Text style={[st.rowVal, { color: color || theme.colors.textMuted }]}>{value}</Text>}
      {onPress && <Text style={[st.rowArrow, { color: theme.colors.textMuted }]}>›</Text>}
    </TouchableOpacity>
  );
}

function SettingToggle({ icon, label, value, onChange }: { icon: string; label: string; value: boolean; onChange: (v: boolean) => void }) {
  const { theme } = useTheme();
  return (
    <View style={[st.row, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
      <Text style={{ fontSize: 18 }}>{icon}</Text>
      <Text style={[st.rowLabel, { color: theme.colors.text }]}>{label}</Text>
      <Switch
        value={value}
        onValueChange={onChange}
        trackColor={{ false: theme.colors.bgSurface, true: `${SystemColors.blue}60` }}
        thumbColor={value ? SystemColors.blue : theme.colors.textMuted}
      />
    </View>
  );
}

export default function SettingsScreen() {
  const { theme, isDark, toggleTheme } = useTheme();
  const { state, dispatch, actions } = useGame();
  const user = state.user;
  const settings = state.settings;

  const handleReset = () => {
    if (Platform.OS === 'web') {
      if (confirm('Bạn có chắc muốn xóa toàn bộ dữ liệu? Hành động này không thể hoàn tác!')) {
        actions.resetData();
      }
    } else {
      Alert.alert(
        '⚠️ Reset Data',
        'Bạn có chắc muốn xóa toàn bộ dữ liệu?\nHành động này không thể hoàn tác!',
        [
          { text: 'Hủy', style: 'cancel' },
          {
            text: 'Xóa tất cả',
            style: 'destructive',
            onPress: () => actions.resetData(),
          },
        ],
      );
    }
  };

  const updateSetting = (key: keyof AppSettings, value: any) => {
    dispatch({
      type: 'SET_SETTINGS',
      payload: { ...settings, [key]: value },
    });
  };

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#E5E7EB', '#DBEAFE']} style={st.hdr}>
          <Text style={[st.hdrT, { color: theme.colors.text }]}>⚙️ Settings</Text>
        </LinearGradient>

        {/* Profile section */}
        <View style={st.section}>
          <Text style={[st.sectionTitle, { color: theme.colors.textMuted }]}>PROFILE</Text>
          <SettingRow icon="👤" label="Tên nhân vật" value={user?.nickname || 'N/A'} />
          <SettingRow
            icon="⚔️"
            label="Class"
            value={user?.className ? user.className.charAt(0).toUpperCase() + user.className.slice(1) : 'N/A'}
          />
          <SettingRow icon="📊" label="Level" value={`Lv.${user?.level || 1}`} color={SystemColors.blue} />
          <SettingRow
            icon="📈"
            label="Total Quests"
            value={String(user?.totalQuestsCompleted || 0)}
            color={SystemColors.green}
          />
        </View>

        {/* Display section */}
        <View style={st.section}>
          <Text style={[st.sectionTitle, { color: theme.colors.textMuted }]}>DISPLAY</Text>
          <SettingToggle
            icon="🌙"
            label="Dark Mode"
            value={isDark}
            onChange={toggleTheme}
          />
          <SettingRow
            icon="🌐"
            label="Ngôn ngữ"
            value={settings.language === 'vi' ? 'Tiếng Việt' : 'English'}
            onPress={() => updateSetting('language', settings.language === 'vi' ? 'en' : 'vi')}
          />
        </View>

        {/* Quest config */}
        <View style={st.section}>
          <Text style={[st.sectionTitle, { color: theme.colors.textMuted }]}>QUEST CONFIG</Text>
          <SettingRow
            icon="📋"
            label="Quests/ngày"
            value={`${settings.questsPerDay} quests`}
            onPress={() => {
              const next = settings.questsPerDay >= 12 ? 4 : settings.questsPerDay + 2;
              updateSetting('questsPerDay', next);
            }}
          />
          <SettingToggle
            icon="⚠️"
            label="Hệ thống phạt"
            value={settings.penaltyEnabled}
            onChange={(v) => updateSetting('penaltyEnabled', v)}
          />
          <SettingToggle
            icon="🔔"
            label="Thông báo"
            value={settings.notifications}
            onChange={(v) => updateSetting('notifications', v)}
          />
        </View>

        {/* AI section */}
        <View style={st.section}>
          <Text style={[st.sectionTitle, { color: theme.colors.textMuted }]}>AI INTEGRATION</Text>
          <SettingRow
            icon="🤖"
            label="AI Provider"
            value={settings.aiProvider ? settings.aiProvider.charAt(0).toUpperCase() + settings.aiProvider.slice(1) : 'Chưa cấu hình'}
            onPress={() => {
              const next = settings.aiProvider === 'claude' ? 'gemini' : settings.aiProvider === 'gemini' ? null : 'claude';
              updateSetting('aiProvider', next);
            }}
          />
          <SettingRow
            icon="🔑"
            label="API Key"
            value={settings.aiApiKey ? '••••••••' : 'Chưa nhập'}
            color={settings.aiApiKey ? SystemColors.green : SystemColors.orange}
          />
        </View>

        {/* Data section */}
        <View style={st.section}>
          <Text style={[st.sectionTitle, { color: theme.colors.textMuted }]}>DATA</Text>
          <SettingRow
            icon="💾"
            label="Backup & Restore"
            onPress={() => {}}
          />
          <TouchableOpacity
            style={[st.dangerBtn, { backgroundColor: `${SystemColors.red}08`, borderColor: `${SystemColors.red}30` }]}
            onPress={handleReset}>
            <Text style={[st.dangerBtnText, { color: SystemColors.red }]}>🗑️ Reset toàn bộ dữ liệu</Text>
          </TouchableOpacity>
        </View>

        {/* App info */}
        <View style={[st.info, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
          <Text style={[st.infoName, { color: SystemColors.blue }]}>⚔️ System Leveling</Text>
          <Text style={[st.infoVer, { color: theme.colors.textMuted }]}>v1.0.0 — Expo SDK 54</Text>
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

  section: { marginTop: 12 },
  sectionTitle: { ...Typography.label, letterSpacing: 1, marginHorizontal: 14, marginBottom: 6 },

  row: { flexDirection: 'row', alignItems: 'center', gap: 10, marginHorizontal: 10, marginBottom: 2, borderRadius: Radius.md, padding: 12, borderWidth: 1 },
  rowLabel: { ...Typography.bodyBold, flex: 1 },
  rowVal: { ...Typography.mono },
  rowArrow: { fontSize: 18, fontWeight: '300' },

  dangerBtn: { marginHorizontal: 10, marginTop: 4, borderRadius: Radius.md, padding: 14, borderWidth: 1, alignItems: 'center' },
  dangerBtnText: { ...Typography.bodyBold },

  info: { marginHorizontal: 10, marginTop: 20, borderRadius: Radius.lg, padding: 16, borderWidth: 1, alignItems: 'center' },
  infoName: { ...Typography.h3 },
  infoVer: { ...Typography.label, marginTop: 2 },
  infoQ: { ...Typography.caption, fontStyle: 'italic', marginTop: 6 },
});
