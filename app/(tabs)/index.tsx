// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — HOME SCREEN v3 (Status Panel — Live Data)
// Solo Leveling: Arise inspired layout — Character center + stats
// Light theme, compact font, dynamic & cool
// Connected to GameContext for real user data
// ═══════════════════════════════════════════════════════════════

import React, { useEffect, useRef, useMemo } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  Animated,
  Dimensions,
  TouchableOpacity,
  Image,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Spacing, Radius } from '@/constants/Theme';
import { StatColors, SystemColors, Gradients } from '@/constants/Colors';
import type { StatKey } from '@/constants/Types';
import { useGame } from '@/constants/GameContext';
import CharacterPreview from '@/components/character/CharacterPreview';

const { width: SW } = Dimensions.get('window');
const CHARACTER_SIZE = Math.min(SW * 0.45, 220);

// ═══ ANIMATED STAT ROW (compact) ═══
function StatRow({ stat, value, change }: { stat: StatKey; value: number; change: number }) {
  const { theme } = useTheme();
  const anim = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.timing(anim, { toValue: value / 100, duration: 1000, useNativeDriver: false }).start();
  }, [value]);

  const w = anim.interpolate({ inputRange: [0, 1], outputRange: ['0%', '100%'] });
  const barColor = value < 30 ? '#EF4444' : value < 50 ? '#F97316' : value < 70 ? '#F59E0B' : value < 85 ? '#10B981' : '#3B82F6';

  return (
    <View style={s.statRow}>
      <View style={[s.statDot, { backgroundColor: StatColors[stat] }]} />
      <Text style={[s.statLabel, { color: StatColors[stat] }]}>{stat}</Text>
      <View style={[s.statBarBg, { backgroundColor: theme.colors.bgSurface }]}>
        <Animated.View style={[s.statBarFill, { width: w, backgroundColor: barColor }]} />
      </View>
      <Text style={[s.statVal, { color: theme.colors.text }]}>{value}</Text>
      {change !== 0 && (
        <Text style={[s.statChg, { color: change > 0 ? SystemColors.green : SystemColors.red }]}>
          {change > 0 ? `+${change}` : `${change}`}
        </Text>
      )}
    </View>
  );
}

// ═══ MINI STAT CARD ═══
function MiniStat({ icon, label, value, color }: { icon: string; label: string; value: string; color: string }) {
  const { theme } = useTheme();
  return (
    <View style={[s.miniStat, { backgroundColor: theme.colors.bgCard, borderColor: `${color}20` }]}>
      <Text style={s.miniIcon}>{icon}</Text>
      <Text style={[s.miniVal, { color }]}>{value}</Text>
      <Text style={[s.miniLabel, { color: theme.colors.textMuted }]}>{label}</Text>
    </View>
  );
}

// ═══ POWER STAT ═══
function PowerStat({ icon, label, value, color }: { icon: string; label: string; value: number; color: string }) {
  const { theme } = useTheme();
  return (
    <View style={s.powerRow}>
      <Text style={[s.powerIcon, { color }]}>{icon}</Text>
      <Text style={[s.powerLabel, { color: theme.colors.textSecondary }]}>{label}</Text>
      <Text style={[s.powerVal, { color }]}>{value.toLocaleString()}</Text>
    </View>
  );
}

// ═══ CLASS ICON MAP ═══
const CLASS_ICONS: Record<string, string> = {
  warrior: '🗡️', mage: '🧙', ranger: '🏹', guardian: '🛡️',
  bard: '🎭', alchemist: '⚗️', healer: '🌿', scholar: '📜',
};

// ═══ HOME SCREEN ═══
export default function HomeScreen() {
  const { theme, isDark } = useTheme();
  const { state } = useGame();
  const user = state.user;

  // Computed values from user
  const u = useMemo(() => {
    if (!user) return null;
    const stats = user.stats;
    const totalPower = Object.values(stats).reduce((a, b) => a + b, 0) * 50;
    const hp = Math.round(stats.VIT * 100 + stats.STR * 5);
    const attack = Math.round(stats.STR * 50 + stats.AGI * 20 + stats.INT * 14);
    const defense = Math.round(stats.VIT * 30 + stats.STR * 10 + stats.WIS * 7);

    // Find equipped title
    const equippedTitle = state.titles.find(t => t.id === user.equippedTitleId);

    // Today's quest progress
    const todayQuests = state.quests;
    const completed = todayQuests.filter(q => q.status === 'completed').length;

    return {
      ...user,
      classIcon: CLASS_ICONS[user.className] || '⚔️',
      totalPower,
      hp,
      attack,
      defense,
      equippedTitle: equippedTitle?.name || null,
      todayQuests: { completed, total: todayQuests.length || 1 },
      statChanges: { STR: 0, INT: 0, AGI: 0, VIT: 0, WIS: 0, CHA: 0 } as Record<StatKey, number>,
    };
  }, [user, state.quests, state.titles]);

  const expPct = u ? u.currentExp / u.expToNextLevel : 0;
  const questPct = u ? u.todayQuests.completed / u.todayQuests.total : 0;
  const expAnim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.timing(expAnim, { toValue: expPct, duration: 1500, useNativeDriver: false }).start();
  }, [expPct]);

  const expW = expAnim.interpolate({ inputRange: [0, 1], outputRange: ['0%', '100%'] });

  if (!u) {
    return (
      <View style={[s.container, { backgroundColor: theme.colors.bg, justifyContent: 'center', alignItems: 'center' }]}>
        <Text style={[s.miniVal, { color: theme.colors.textMuted }]}>Loading...</Text>
      </View>
    );
  }

  return (
    <View style={[s.container, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={s.scroll} showsVerticalScrollIndicator={false}>

        {/* ══ HERO SECTION — Character + Info ══ */}
        <LinearGradient
          colors={isDark ? ['#0F172A', '#1E293B'] : ['#DBEAFE', '#EDE9FE', '#E0E7FF']}
          style={s.hero}>

          {/* Top bar */}
          <View style={s.topBar}>
            <View>
              <Text style={[s.heroName, { color: theme.colors.text }]}>{u.nickname}</Text>
              <Text style={[s.heroClass, { color: SystemColors.blue }]}>
                {u.classIcon} {u.className.charAt(0).toUpperCase() + u.className.slice(1)}
              </Text>
            </View>
            <View style={s.topRight}>
              <Text style={[s.heroLevel, { color: theme.colors.text }]}>
                Lv.{u.level}
              </Text>
              <Text style={[s.heroExpText, { color: theme.colors.textMuted }]}>
                {u.currentExp.toLocaleString()} / {u.expToNextLevel.toLocaleString()}
              </Text>
            </View>
          </View>

          {/* EXP Bar under top */}
          <View style={[s.expBarBg, { backgroundColor: isDark ? '#1E293B' : 'rgba(0,0,0,0.06)' }]}>
            <Animated.View style={[s.expBarFill, { width: expW }]}>
              <LinearGradient colors={['#3B82F6', '#8B5CF6', '#EC4899']} start={{ x: 0, y: 0 }} end={{ x: 1, y: 0 }} style={StyleSheet.absoluteFill} />
            </Animated.View>
          </View>

          {/* Character + Stats Layout */}
          <View style={s.heroBody}>
            {/* LEFT: Stats */}
            <View style={s.statsLeft}>
              <Text style={[s.statsTitle, { color: theme.colors.textSecondary }]}>Stats</Text>
              {(['STR', 'VIT', 'AGI'] as StatKey[]).map(st => (
                <View key={st} style={s.hexStat}>
                  <Text style={[s.hexStatLabel, { color: StatColors[st] }]}>{st}</Text>
                  <Text style={[s.hexStatVal, { color: theme.colors.text }]}>{u.stats[st]}</Text>
                </View>
              ))}
            </View>

            {/* CENTER: Character */}
            <View style={s.characterWrap}>
              <View style={s.previewCharContainer}>
                <CharacterPreview
                  stats={{
                    heightCm: u.heightCm || 170,
                    weightKg: u.weightKg || 65,
                    STR: u.stats.STR,
                    VIT: u.stats.VIT,
                    AGI: u.stats.AGI,
                    level: u.level,
                  }}
                />
              </View>
              {/* Title under character */}
              {u.equippedTitle && (
                <View style={[s.titleBadge, { backgroundColor: `${SystemColors.purple}18`, borderColor: `${SystemColors.purple}30` }]}>
                  <Text style={[s.titleText, { color: SystemColors.purple }]}>「{u.equippedTitle}」</Text>
                </View>
              )}
            </View>

            {/* RIGHT: Power stats */}
            <View style={s.statsRight}>
              <View style={[s.totalPower, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.gold}25` }]}>
                <Text style={[s.tpLabel, { color: theme.colors.textMuted }]}>Total Power</Text>
                <Text style={[s.tpVal, { color: SystemColors.gold }]}>{u.totalPower.toLocaleString()}</Text>
              </View>
              <PowerStat icon="❤️" label="HP" value={u.hp} color={SystemColors.red} />
              <PowerStat icon="⚔️" label="Attack" value={u.attack} color={SystemColors.orange} />
              <PowerStat icon="🛡️" label="Defense" value={u.defense} color={SystemColors.blue} />
            </View>
          </View>

          {/* RIGHT side remaining stats */}
          <View style={s.statsBottomRow}>
            {(['INT', 'WIS', 'CHA'] as StatKey[]).map(st => (
              <View key={st} style={s.hexStat}>
                <Text style={[s.hexStatLabel, { color: StatColors[st] }]}>{st}</Text>
                <Text style={[s.hexStatVal, { color: theme.colors.text }]}>{u.stats[st]}</Text>
              </View>
            ))}
          </View>
        </LinearGradient>

        {/* ══ ATTRIBUTES DETAIL ══ */}
        <View style={[s.card, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
          <Text style={[s.cardTitle, { color: theme.colors.text }]}>📊 Attributes</Text>
          {(Object.keys(u.stats) as StatKey[]).map(st => (
            <StatRow key={st} stat={st} value={u.stats[st]} change={u.statChanges[st]} />
          ))}
        </View>

        {/* ══ QUICK STATS ROW ══ */}
        <View style={s.miniRow}>
          <MiniStat icon="💰" label="Gold" value={u.gold.toLocaleString()} color={SystemColors.gold} />
          <MiniStat icon="💎" label="Gem" value={String(u.gems)} color={SystemColors.cyan} />
          <MiniStat icon="🔥" label="Streak" value={`${u.streakDays}d`} color={SystemColors.orange} />
          <MiniStat icon="⚠️" label="Debt" value={String(u.debtPoints)} color={u.debtPoints > 0 ? SystemColors.red : SystemColors.green} />
        </View>

        {/* ══ TODAY'S QUESTS ══ */}
        <TouchableOpacity style={[s.card, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]} activeOpacity={0.7}>
          <View style={s.questHeader}>
            <Text style={[s.cardTitle, { color: theme.colors.text }]}>📋 Today's Quests</Text>
            <Text style={[s.questCount, { color: SystemColors.blue }]}>{u.todayQuests.completed}/{u.todayQuests.total}</Text>
          </View>
          <View style={[s.questBarBg, { backgroundColor: theme.colors.bgSurface }]}>
            <View style={[s.questBarFill, { width: `${questPct * 100}%`, backgroundColor: questPct === 1 ? SystemColors.green : SystemColors.blue }]} />
          </View>
          <Text style={[s.questPct, { color: theme.colors.textMuted }]}>{Math.round(questPct * 100)}% completed</Text>
        </TouchableOpacity>

        <View style={{ height: 20 }} />
      </ScrollView>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1 },
  scroll: { paddingBottom: 16 },

  // Hero
  hero: { paddingTop: 48, paddingBottom: 16, paddingHorizontal: 16 },
  topBar: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
  heroName: { ...Typography.h1 },
  heroClass: { ...Typography.caption, marginTop: 2 },
  topRight: { alignItems: 'flex-end' },
  heroLevel: { ...Typography.h2 },
  heroExpText: { ...Typography.monoSmall, marginTop: 2 },

  // EXP bar
  expBarBg: { height: 6, borderRadius: 3, overflow: 'hidden', marginBottom: 12 },
  expBarFill: { height: '100%', borderRadius: 3, overflow: 'hidden' },

  // Hero body — 3 columns
  heroBody: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', minHeight: 200 },

  // Left stats
  statsLeft: { width: 70, alignItems: 'center' },
  statsTitle: { ...Typography.label, marginBottom: 8, letterSpacing: 1 },
  hexStat: { alignItems: 'center', marginBottom: 8 },
  hexStatLabel: { ...Typography.label, letterSpacing: 1 },
  hexStatVal: { ...Typography.stat, marginTop: 1 },

  // Character center
  characterWrap: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  previewCharContainer: {
    width: CHARACTER_SIZE * 1.1,
    height: CHARACTER_SIZE * 1.3,
    alignItems: 'center',
    justifyContent: 'center',
  },
  titleBadge: { marginTop: 6, paddingHorizontal: 10, paddingVertical: 3, borderRadius: Radius.sm, borderWidth: 1 },
  titleText: { ...Typography.label },

  // Right stats
  statsRight: { width: 100, alignItems: 'flex-end' },
  totalPower: { padding: 8, borderRadius: Radius.sm, borderWidth: 1, alignItems: 'center', marginBottom: 8, width: '100%' },
  tpLabel: { ...Typography.label },
  tpVal: { ...Typography.stat },
  powerRow: { flexDirection: 'row', alignItems: 'center', gap: 4, marginBottom: 4, width: '100%', justifyContent: 'flex-end' },
  powerIcon: { fontSize: 12 },
  powerLabel: { ...Typography.label },
  powerVal: { ...Typography.mono, minWidth: 40, textAlign: 'right' },

  // Bottom stats row
  statsBottomRow: { flexDirection: 'row', justifyContent: 'space-around', marginTop: 4, paddingHorizontal: 40 },

  // Card
  card: { marginHorizontal: 12, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1 },
  cardTitle: { ...Typography.h3, marginBottom: 8 },

  // Stat bars
  statRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 5 },
  statDot: { width: 6, height: 6, borderRadius: 3, marginRight: 4 },
  statLabel: { width: 28, ...Typography.captionBold },
  statBarBg: { flex: 1, height: 10, borderRadius: 5, marginHorizontal: 6, overflow: 'hidden' },
  statBarFill: { height: '100%', borderRadius: 5 },
  statVal: { width: 24, ...Typography.monoSmall, textAlign: 'right' },
  statChg: { width: 28, ...Typography.label, textAlign: 'right' },

  // Mini stats
  miniRow: { flexDirection: 'row', marginHorizontal: 10, marginTop: 10, gap: 6 },
  miniStat: { flex: 1, alignItems: 'center', paddingVertical: 8, borderRadius: Radius.md, borderWidth: 1 },
  miniIcon: { fontSize: 16, marginBottom: 2 },
  miniVal: { ...Typography.mono },
  miniLabel: { ...Typography.label, marginTop: 1 },

  // Quests
  questHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  questCount: { ...Typography.mono },
  questBarBg: { height: 8, borderRadius: 4, overflow: 'hidden' },
  questBarFill: { height: '100%', borderRadius: 4 },
  questPct: { ...Typography.caption, textAlign: 'center', marginTop: 4 },
});
