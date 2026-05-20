// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — TITLES SCREEN
// Achievement gallery with equip functionality
// ═══════════════════════════════════════════════════════════════

import React from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors, RarityColors } from '@/constants/Colors';
import type { Title, RarityTier } from '@/constants/Types';
import { useGame } from '@/constants/GameContext';

const RARITY_LABELS: Record<RarityTier, string> = {
  common: 'Common', uncommon: 'Uncommon', rare: 'Rare',
  epic: 'Epic', legendary: 'Legendary', mythic: 'Mythic',
};

function TitleCard({ title, isEquipped, onEquip }: { title: Title; isEquipped: boolean; onEquip: () => void }) {
  const { theme } = useTheme();
  const rc = RarityColors[title.rarity];
  const locked = !title.isUnlocked;

  return (
    <TouchableOpacity
      style={[
        st.card,
        {
          backgroundColor: theme.colors.bgCard,
          borderColor: isEquipped ? SystemColors.gold : locked ? theme.colors.borderLight : `${rc}30`,
          borderWidth: isEquipped ? 2 : 1,
          opacity: locked ? 0.4 : 1,
        },
      ]}
      onPress={!locked ? onEquip : undefined}
      disabled={locked}
      activeOpacity={0.7}>
      {isEquipped && (
        <View style={[st.equippedBadge, { backgroundColor: SystemColors.gold }]}>
          <Text style={st.equippedText}>EQUIPPED</Text>
        </View>
      )}
      <Text style={st.icon}>{locked ? '🔒' : title.iconEmoji}</Text>
      <Text style={[st.name, { color: rc }]}>「{title.name}」</Text>
      {title.nameEn && <Text style={[st.nameEn, { color: theme.colors.textMuted }]}>{title.nameEn}</Text>}
      <View style={[st.rarity, { backgroundColor: `${rc}12`, borderColor: `${rc}30` }]}>
        <Text style={[st.rarityText, { color: rc }]}>{RARITY_LABELS[title.rarity]}</Text>
      </View>
      <Text style={[st.desc, { color: theme.colors.textSecondary }]} numberOfLines={2}>{title.description}</Text>
      <Text style={[st.condition, { color: theme.colors.textMuted }]}>📋 {title.condition}</Text>
      {title.isUnlocked && title.unlockedAt && (
        <Text style={[st.date, { color: theme.colors.textMuted }]}>
          🏅 {new Date(title.unlockedAt).toLocaleDateString('vi-VN')}
        </Text>
      )}
    </TouchableOpacity>
  );
}

export default function TitlesScreen() {
  const { theme, isDark } = useTheme();
  const { state, actions } = useGame();

  const titles = state.titles;
  const unlocked = titles.filter(t => t.isUnlocked);
  const locked = titles.filter(t => !t.isUnlocked);

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#FEF3C7', '#EDE9FE']} style={st.hdr}>
          <Text style={[st.hdrT, { color: theme.colors.text }]}>🏆 Titles</Text>
          <Text style={[st.hdrS, { color: theme.colors.textMuted }]}>
            {unlocked.length}/{titles.length} đã mở khóa
          </Text>
        </LinearGradient>

        {unlocked.length > 0 && (
          <View style={st.section}>
            <Text style={[st.sectionTitle, { color: theme.colors.text }]}>✨ Đã mở khóa</Text>
            {unlocked.map(t => (
              <TitleCard
                key={t.id}
                title={t}
                isEquipped={state.user?.equippedTitleId === t.id}
                onEquip={() => actions.equipTitle(t.id)}
              />
            ))}
          </View>
        )}

        {locked.length > 0 && (
          <View style={st.section}>
            <Text style={[st.sectionTitle, { color: theme.colors.textMuted }]}>🔒 Chưa mở khóa</Text>
            {locked.map(t => (
              <TitleCard
                key={t.id}
                title={t}
                isEquipped={false}
                onEquip={() => {}}
              />
            ))}
          </View>
        )}

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

  section: { marginHorizontal: 10, marginTop: 10 },
  sectionTitle: { ...Typography.h3, marginBottom: 8 },

  card: { borderRadius: Radius.lg, padding: 14, marginBottom: 8, position: 'relative' },
  equippedBadge: { position: 'absolute', top: 8, right: 8, paddingHorizontal: 8, paddingVertical: 2, borderRadius: 6 },
  equippedText: { color: '#FFF', fontSize: 8, fontWeight: '900', letterSpacing: 0.5 },
  icon: { fontSize: 28, marginBottom: 4 },
  name: { fontSize: 15, fontWeight: '800' },
  nameEn: { ...Typography.caption, marginTop: 1 },
  rarity: { alignSelf: 'flex-start', paddingHorizontal: 8, paddingVertical: 2, borderRadius: 6, borderWidth: 1, marginTop: 4 },
  rarityText: { fontSize: 9, fontWeight: '700' },
  desc: { ...Typography.body, marginTop: 6, lineHeight: 18 },
  condition: { ...Typography.label, marginTop: 4 },
  date: { ...Typography.label, marginTop: 2 },
});
