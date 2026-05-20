// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — QUESTS SCREEN v3 (Interactive)
// Complete/fail quests with rewards animation
// Connected to GameContext for live data
// ═══════════════════════════════════════════════════════════════

import React, { useState, useRef, useCallback } from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, Animated, Modal } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors, QuestRankColors } from '@/constants/Colors';
import type { Quest, QuestRank, QuestStatus } from '@/constants/Types';
import { useGame } from '@/constants/GameContext';

// ═══ REWARD POPUP ═══
function RewardPopup({
  visible,
  quest,
  onClose,
}: {
  visible: boolean;
  quest: Quest | null;
  onClose: () => void;
}) {
  const { theme } = useTheme();
  const scaleAnim = useRef(new Animated.Value(0.5)).current;
  const opacityAnim = useRef(new Animated.Value(0)).current;

  React.useEffect(() => {
    if (visible) {
      Animated.parallel([
        Animated.spring(scaleAnim, { toValue: 1, friction: 6, tension: 80, useNativeDriver: true }),
        Animated.timing(opacityAnim, { toValue: 1, duration: 200, useNativeDriver: true }),
      ]).start();
    } else {
      scaleAnim.setValue(0.5);
      opacityAnim.setValue(0);
    }
  }, [visible]);

  if (!quest) return null;

  return (
    <Modal visible={visible} transparent animationType="none" onRequestClose={onClose}>
      <Animated.View style={[st.popupOverlay, { opacity: opacityAnim }]}>
        <Animated.View style={[st.popupCard, { transform: [{ scale: scaleAnim }], backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.gold}40` }]}>
          <Text style={st.popupEmoji}>🎉</Text>
          <Text style={[st.popupTitle, { color: SystemColors.gold }]}>QUEST COMPLETE!</Text>
          <Text style={[st.popupQuest, { color: theme.colors.text }]}>{quest.title}</Text>

          <View style={st.popupRewards}>
            <View style={st.popupRewardRow}>
              <Text style={st.popupRewardIcon}>⭐</Text>
              <Text style={[st.popupRewardVal, { color: SystemColors.blue }]}>+{quest.rewards.exp} EXP</Text>
            </View>
            <View style={st.popupRewardRow}>
              <Text style={st.popupRewardIcon}>💰</Text>
              <Text style={[st.popupRewardVal, { color: SystemColors.gold }]}>+{quest.rewards.gold} Gold</Text>
            </View>
            {quest.rewards.skillPoints.length > 0 && (
              <View style={st.popupRewardRow}>
                <Text style={st.popupRewardIcon}>📈</Text>
                <Text style={[st.popupRewardVal, { color: SystemColors.green }]}>+SP</Text>
              </View>
            )}
          </View>

          <TouchableOpacity style={[st.popupBtn, { backgroundColor: SystemColors.blue }]} onPress={onClose}>
            <Text style={st.popupBtnText}>✨ Tiếp tục</Text>
          </TouchableOpacity>
        </Animated.View>
      </Animated.View>
    </Modal>
  );
}

// ═══ QUEST ITEM ═══
function QuestItem({
  q,
  isLast,
  onComplete,
  onFail,
}: {
  q: Quest;
  isLast: boolean;
  onComplete: () => void;
  onFail: () => void;
}) {
  const { theme } = useTheme();
  const rc = QuestRankColors[q.difficulty];
  const done = q.status === 'completed';
  const failed = q.status === 'failed';
  const active = q.status === 'in_progress' || q.status === 'pending';
  const si = done ? '✅' : failed ? '❌' : '○';
  const scaleAnim = useRef(new Animated.Value(1)).current;

  const handleComplete = () => {
    Animated.sequence([
      Animated.timing(scaleAnim, { toValue: 0.95, duration: 80, useNativeDriver: true }),
      Animated.timing(scaleAnim, { toValue: 1, duration: 120, useNativeDriver: true }),
    ]).start();
    onComplete();
  };

  const timeStr = q.timeWindow?.start || '';

  return (
    <Animated.View style={{ transform: [{ scale: scaleAnim }] }}>
      <View
        style={[
          st.qi,
          {
            backgroundColor: theme.colors.bgCard,
            borderColor: done ? `${SystemColors.green}30` : failed ? `${SystemColors.red}30` : theme.colors.border,
            opacity: done || failed ? 0.55 : 1,
          },
        ]}>
        {/* Timeline */}
        <View style={st.tl}>
          <Text style={[st.tlTime, { color: theme.colors.textMuted }]}>{timeStr}</Text>
          <View style={[st.tlDot, { backgroundColor: rc, shadowColor: rc, shadowOpacity: 0.4, shadowRadius: 4, elevation: 3 }]} />
          {!isLast && <View style={[st.tlLine, { backgroundColor: theme.colors.borderLight }]} />}
        </View>
        {/* Content */}
        <View style={st.qc}>
          <View style={st.qh}>
            <View style={[st.rk, { backgroundColor: `${rc}15`, borderColor: `${rc}40` }]}>
              <Text style={[st.rkT, { color: rc }]}>{q.difficulty}</Text>
            </View>
            <Text style={[st.qTitle, { color: theme.colors.text, textDecorationLine: done ? 'line-through' : 'none' }]} numberOfLines={1}>
              {q.title}
            </Text>
            <Text style={{ fontSize: 13 }}>{si}</Text>
          </View>
          <Text style={[st.qDesc, { color: theme.colors.textMuted }]} numberOfLines={1}>→ {q.description}</Text>
          <Text style={[st.qRew, { color: SystemColors.gold }]}>+{q.rewards.exp} EXP +{q.rewards.gold} Gold</Text>

          {/* Action buttons */}
          {active && (
            <View style={st.qActions}>
              <TouchableOpacity
                style={[st.qActionBtn, { backgroundColor: `${SystemColors.green}12`, borderColor: `${SystemColors.green}40` }]}
                onPress={handleComplete}
                activeOpacity={0.7}>
                <Text style={[st.qActionText, { color: SystemColors.green }]}>✅ Done</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[st.qActionBtn, { backgroundColor: `${SystemColors.red}08`, borderColor: `${SystemColors.red}25` }]}
                onPress={onFail}
                activeOpacity={0.7}>
                <Text style={[st.qActionText, { color: SystemColors.red }]}>✖ Fail</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>
      </View>
    </Animated.View>
  );
}

export default function QuestsScreen() {
  const { theme, isDark } = useTheme();
  const { state, actions } = useGame();
  const [rewardQuest, setRewardQuest] = useState<Quest | null>(null);
  const [showReward, setShowReward] = useState(false);

  const quests = state.quests;
  const done = quests.filter(q => q.status === 'completed').length;
  const pct = quests.length > 0 ? Math.round((done / quests.length) * 100) : 0;
  const streak = state.user?.streakDays || 0;

  const handleComplete = useCallback((quest: Quest) => {
    setRewardQuest(quest);
    setShowReward(true);
    actions.completeQuest(quest.id);
  }, [actions]);

  const handleFail = useCallback((questId: string) => {
    actions.failQuest(questId);
  }, [actions]);

  const today = new Date();
  const dayNames = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
  const dateStr = `${dayNames[today.getDay()]} — ${today.getDate().toString().padStart(2, '0')}/${(today.getMonth() + 1).toString().padStart(2, '0')}/${today.getFullYear()}`;

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        {/* Header */}
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#DBEAFE', '#E0E7FF']} style={st.hdr}>
          <View style={st.hdrRow}>
            <View>
              <Text style={[st.hdrTitle, { color: theme.colors.text }]}>📋 Quest Board</Text>
              <Text style={[st.hdrDate, { color: theme.colors.textMuted }]}>{dateStr}</Text>
            </View>
            <View style={st.hdrRight}>
              <Text style={[st.hdrStreak, { color: SystemColors.orange }]}>🔥 {streak} days</Text>
              <Text style={[st.hdrProg, { color: SystemColors.blue }]}>{done}/{quests.length} ({pct}%)</Text>
            </View>
          </View>
          {/* Progress bar */}
          <View style={[st.pBg, { backgroundColor: isDark ? '#1E293B' : 'rgba(0,0,0,0.06)' }]}>
            <View style={[st.pFill, { width: `${pct}%`, backgroundColor: pct === 100 ? SystemColors.green : SystemColors.blue }]} />
          </View>
        </LinearGradient>

        {/* Quest List */}
        {quests.length === 0 ? (
          <View style={[st.empty, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
            <Text style={st.emptyIcon}>📭</Text>
            <Text style={[st.emptyText, { color: theme.colors.textMuted }]}>
              Chưa có quest nào hôm nay.{'\n'}Hoàn thành onboarding để nhận quests!
            </Text>
          </View>
        ) : (
          quests.map((q, i) => (
            <QuestItem
              key={q.id}
              q={q}
              isLast={i === quests.length - 1}
              onComplete={() => handleComplete(q)}
              onFail={() => handleFail(q.id)}
            />
          ))
        )}

        <View style={{ height: 20 }} />
      </ScrollView>

      {/* Reward Popup */}
      <RewardPopup
        visible={showReward}
        quest={rewardQuest}
        onClose={() => setShowReward(false)}
      />
    </View>
  );
}

const st = StyleSheet.create({
  c: { flex: 1 }, sc: { paddingBottom: 16 },
  hdr: { paddingTop: 48, paddingBottom: 12, paddingHorizontal: 16 },
  hdrRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 },
  hdrTitle: { ...Typography.h1 },
  hdrDate: { ...Typography.caption, marginTop: 2 },
  hdrRight: { alignItems: 'flex-end' },
  hdrStreak: { ...Typography.captionBold },
  hdrProg: { ...Typography.monoSmall, marginTop: 2 },
  pBg: { height: 4, borderRadius: 2, overflow: 'hidden' },
  pFill: { height: '100%', borderRadius: 2 },

  qi: { flexDirection: 'row', marginHorizontal: 10, marginBottom: 3, borderRadius: Radius.md, paddingVertical: 8, paddingHorizontal: 10, borderWidth: 1 },
  tl: { width: 44, alignItems: 'center' },
  tlTime: { ...Typography.monoSmall },
  tlDot: { width: 8, height: 8, borderRadius: 4, marginTop: 4 },
  tlLine: { width: 1.5, flex: 1, marginTop: 3 },
  qc: { flex: 1, marginLeft: 6 },
  qh: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  rk: { width: 22, height: 22, borderRadius: 5, borderWidth: 1, alignItems: 'center', justifyContent: 'center' },
  rkT: { fontSize: 10, fontWeight: '900' },
  qTitle: { flex: 1, ...Typography.bodyBold },
  qDesc: { ...Typography.caption, marginTop: 2, marginLeft: 28 },
  qRew: { ...Typography.label, marginTop: 1, marginLeft: 28 },

  // Action buttons
  qActions: { flexDirection: 'row', gap: 6, marginTop: 6, marginLeft: 28 },
  qActionBtn: { paddingHorizontal: 12, paddingVertical: 5, borderRadius: Radius.sm, borderWidth: 1 },
  qActionText: { ...Typography.captionBold },

  // Empty state
  empty: { marginHorizontal: 10, marginTop: 20, borderRadius: Radius.lg, padding: 24, borderWidth: 1, alignItems: 'center' },
  emptyIcon: { fontSize: 40, marginBottom: 8 },
  emptyText: { ...Typography.body, textAlign: 'center', lineHeight: 20 },

  // Reward popup
  popupOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'center', alignItems: 'center' },
  popupCard: { width: '80%', maxWidth: 320, borderRadius: Radius.xl, padding: 24, alignItems: 'center', borderWidth: 2 },
  popupEmoji: { fontSize: 48, marginBottom: 8 },
  popupTitle: { fontSize: 18, fontWeight: '900', letterSpacing: 1 },
  popupQuest: { ...Typography.bodyBold, marginTop: 8, textAlign: 'center' },
  popupRewards: { marginTop: 16, gap: 6, width: '100%' },
  popupRewardRow: { flexDirection: 'row', alignItems: 'center', gap: 8, justifyContent: 'center' },
  popupRewardIcon: { fontSize: 18 },
  popupRewardVal: { ...Typography.h2 },
  popupBtn: { marginTop: 20, paddingHorizontal: 32, paddingVertical: 10, borderRadius: Radius.md },
  popupBtnText: { color: '#FFF', ...Typography.bodyBold },
});
