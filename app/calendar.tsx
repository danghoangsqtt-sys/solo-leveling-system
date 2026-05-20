// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — CALENDAR SCREEN
// Month view with quest overlay & habit tracker
// ═══════════════════════════════════════════════════════════════

import React, { useState, useMemo } from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors } from '@/constants/Colors';
import { useGame } from '@/constants/GameContext';

const DAY_NAMES = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
const MONTH_NAMES = [
  'Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
  'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12',
];

function getDaysInMonth(year: number, month: number) {
  return new Date(year, month + 1, 0).getDate();
}

function getFirstDayOfMonth(year: number, month: number) {
  return new Date(year, month, 1).getDay();
}

export default function CalendarScreen() {
  const { theme, isDark } = useTheme();
  const { state } = useGame();
  const now = new Date();
  const [currentMonth, setCurrentMonth] = useState(now.getMonth());
  const [currentYear, setCurrentYear] = useState(now.getFullYear());
  const [selectedDate, setSelectedDate] = useState<number | null>(now.getDate());

  const daysInMonth = getDaysInMonth(currentYear, currentMonth);
  const firstDay = getFirstDayOfMonth(currentYear, currentMonth);
  const todayDate = now.getDate();
  const isCurrentMonth = now.getMonth() === currentMonth && now.getFullYear() === currentYear;

  // Build calendar grid
  const calendarDays = useMemo(() => {
    const days: (number | null)[] = [];
    for (let i = 0; i < firstDay; i++) days.push(null);
    for (let d = 1; d <= daysInMonth; d++) days.push(d);
    while (days.length % 7 !== 0) days.push(null);
    return days;
  }, [currentYear, currentMonth, daysInMonth, firstDay]);

  // Quest counts per day (simple mock based on streak)
  const questCountForDay = (day: number): number => {
    if (!state.user) return 0;
    const d = new Date(currentYear, currentMonth, day);
    const today = new Date();
    if (d > today) return 0;
    if (d.toDateString() === today.toDateString()) {
      return state.quests.filter(q => q.status === 'completed').length;
    }
    // For past days, use streak to estimate
    const diff = Math.floor((today.getTime() - d.getTime()) / (1000 * 60 * 60 * 24));
    if (diff <= (state.user.streakDays || 0)) return Math.floor(Math.random() * 5) + 3;
    return 0;
  };

  const prevMonth = () => {
    if (currentMonth === 0) {
      setCurrentMonth(11);
      setCurrentYear(currentYear - 1);
    } else {
      setCurrentMonth(currentMonth - 1);
    }
    setSelectedDate(null);
  };

  const nextMonth = () => {
    if (currentMonth === 11) {
      setCurrentMonth(0);
      setCurrentYear(currentYear + 1);
    } else {
      setCurrentMonth(currentMonth + 1);
    }
    setSelectedDate(null);
  };

  // Selected day's quests
  const selectedDayQuests = selectedDate && isCurrentMonth && selectedDate === todayDate
    ? state.quests
    : [];

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#DBEAFE', '#D1FAE5']} style={st.hdr}>
          <Text style={[st.hdrT, { color: theme.colors.text }]}>📅 Calendar</Text>
        </LinearGradient>

        {/* Month navigation */}
        <View style={[st.monthNav, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
          <TouchableOpacity onPress={prevMonth} style={st.navBtn}>
            <Text style={[st.navBtnText, { color: SystemColors.blue }]}>◀</Text>
          </TouchableOpacity>
          <Text style={[st.monthTitle, { color: theme.colors.text }]}>
            {MONTH_NAMES[currentMonth]} {currentYear}
          </Text>
          <TouchableOpacity onPress={nextMonth} style={st.navBtn}>
            <Text style={[st.navBtnText, { color: SystemColors.blue }]}>▶</Text>
          </TouchableOpacity>
        </View>

        {/* Day names header */}
        <View style={st.dayNames}>
          {DAY_NAMES.map((d, i) => (
            <Text
              key={d}
              style={[
                st.dayName,
                { color: i === 0 ? SystemColors.red : theme.colors.textMuted },
              ]}>
              {d}
            </Text>
          ))}
        </View>

        {/* Calendar grid */}
        <View style={[st.grid, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
          {calendarDays.map((day, i) => {
            if (day === null) {
              return <View key={`e-${i}`} style={st.dayCell} />;
            }

            const isToday = isCurrentMonth && day === todayDate;
            const isSelected = day === selectedDate;
            const questCount = questCountForDay(day);
            const hasDot = questCount > 0;

            return (
              <TouchableOpacity
                key={day}
                style={[
                  st.dayCell,
                  isToday && { backgroundColor: `${SystemColors.blue}15`, borderRadius: 8 },
                  isSelected && { backgroundColor: `${SystemColors.blue}25`, borderRadius: 8 },
                ]}
                onPress={() => setSelectedDate(day)}>
                <Text
                  style={[
                    st.dayText,
                    { color: isToday ? SystemColors.blue : theme.colors.text },
                    isToday && { fontWeight: '800' },
                    i % 7 === 0 && { color: SystemColors.red },
                  ]}>
                  {day}
                </Text>
                {hasDot && (
                  <View style={st.dotRow}>
                    <View style={[st.dot, { backgroundColor: questCount >= 5 ? SystemColors.green : SystemColors.blue }]} />
                  </View>
                )}
              </TouchableOpacity>
            );
          })}
        </View>

        {/* Selected day details */}
        {selectedDate && (
          <View style={[st.dayDetail, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
            <Text style={[st.dayDetailTitle, { color: theme.colors.text }]}>
              📋 {selectedDate}/{currentMonth + 1}/{currentYear}
            </Text>
            {selectedDayQuests.length > 0 ? (
              selectedDayQuests.slice(0, 5).map(q => (
                <View key={q.id} style={[st.miniQuest, { borderColor: theme.colors.borderLight }]}>
                  <Text style={{ fontSize: 12 }}>
                    {q.status === 'completed' ? '✅' : q.status === 'failed' ? '❌' : '○'}
                  </Text>
                  <Text style={[st.miniQuestTitle, { color: theme.colors.text }]} numberOfLines={1}>
                    {q.title}
                  </Text>
                  <Text style={[st.miniQuestTime, { color: theme.colors.textMuted }]}>
                    {q.timeWindow?.start || ''}
                  </Text>
                </View>
              ))
            ) : (
              <Text style={[st.noQuests, { color: theme.colors.textMuted }]}>
                Không có quest vào ngày này
              </Text>
            )}
          </View>
        )}

        {/* Streak info */}
        <View style={[st.streakCard, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.orange}20` }]}>
          <Text style={{ fontSize: 24 }}>🔥</Text>
          <View>
            <Text style={[st.streakVal, { color: SystemColors.orange }]}>{state.user?.streakDays || 0} ngày liên tiếp</Text>
            <Text style={[st.streakLabel, { color: theme.colors.textMuted }]}>Streak hiện tại</Text>
          </View>
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

  monthNav: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginHorizontal: 10, marginTop: 6, borderRadius: Radius.md, padding: 10, borderWidth: 1 },
  navBtn: { padding: 6 },
  navBtnText: { fontSize: 16, fontWeight: '700' },
  monthTitle: { ...Typography.h2 },

  dayNames: { flexDirection: 'row', marginHorizontal: 10, marginTop: 8, paddingHorizontal: 4 },
  dayName: { flex: 1, textAlign: 'center', ...Typography.captionBold },

  grid: { flexDirection: 'row', flexWrap: 'wrap', marginHorizontal: 10, marginTop: 4, borderRadius: Radius.lg, padding: 4, borderWidth: 1 },
  dayCell: { width: `${100 / 7}%`, aspectRatio: 1, alignItems: 'center', justifyContent: 'center', padding: 2 },
  dayText: { ...Typography.body },
  dotRow: { flexDirection: 'row', gap: 2, marginTop: 1 },
  dot: { width: 4, height: 4, borderRadius: 2 },

  dayDetail: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1 },
  dayDetailTitle: { ...Typography.h3, marginBottom: 8 },
  miniQuest: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingVertical: 4, borderBottomWidth: 0.5 },
  miniQuestTitle: { ...Typography.body, flex: 1 },
  miniQuestTime: { ...Typography.monoSmall },
  noQuests: { ...Typography.body, textAlign: 'center', paddingVertical: 8 },

  streakCard: { flexDirection: 'row', alignItems: 'center', gap: 12, marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 14, borderWidth: 1 },
  streakVal: { ...Typography.h2 },
  streakLabel: { ...Typography.caption, marginTop: 1 },
});
