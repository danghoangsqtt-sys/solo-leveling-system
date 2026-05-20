// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — JOURNAL SCREEN
// Rich editor with mood selector, RPG notebook style
// ═══════════════════════════════════════════════════════════════

import React, { useState } from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, TextInput, KeyboardAvoidingView, Platform } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors } from '@/constants/Colors';
import type { JournalEntry, MoodType } from '@/constants/Types';
import { useGame } from '@/constants/GameContext';

const MOODS: { type: MoodType; icon: string; label: string; color: string }[] = [
  { type: 'great', icon: '😄', label: 'Tuyệt vời', color: '#10B981' },
  { type: 'good', icon: '🙂', label: 'Tốt', color: '#3B82F6' },
  { type: 'neutral', icon: '😐', label: 'Bình thường', color: '#F59E0B' },
  { type: 'bad', icon: '😔', label: 'Buồn', color: '#F97316' },
  { type: 'terrible', icon: '😢', label: 'Rất tệ', color: '#EF4444' },
];

function JournalCard({ entry }: { entry: JournalEntry }) {
  const { theme } = useTheme();
  const mood = MOODS.find(m => m.type === entry.mood);
  const date = new Date(entry.date);
  const dateStr = date.toLocaleDateString('vi-VN', { weekday: 'short', day: '2-digit', month: '2-digit' });
  const timeStr = date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });

  return (
    <View style={[st.entry, { backgroundColor: theme.colors.bgCard, borderColor: mood ? `${mood.color}25` : theme.colors.border }]}>
      <View style={st.entryHeader}>
        <View>
          <Text style={[st.entryDate, { color: theme.colors.textMuted }]}>{dateStr} • {timeStr}</Text>
        </View>
        {mood && (
          <View style={[st.moodTag, { backgroundColor: `${mood.color}12`, borderColor: `${mood.color}30` }]}>
            <Text style={{ fontSize: 14 }}>{mood.icon}</Text>
            <Text style={[st.moodTagText, { color: mood.color }]}>{mood.label}</Text>
          </View>
        )}
      </View>
      <Text style={[st.entryContent, { color: theme.colors.text }]} numberOfLines={4}>
        {entry.content}
      </Text>
      {entry.tags.length > 0 && (
        <View style={st.tags}>
          {entry.tags.map((tag, i) => (
            <View key={i} style={[st.tag, { backgroundColor: `${SystemColors.blue}08`, borderColor: `${SystemColors.blue}20` }]}>
              <Text style={[st.tagText, { color: SystemColors.blue }]}>#{tag}</Text>
            </View>
          ))}
        </View>
      )}
    </View>
  );
}

export default function JournalScreen() {
  const { theme, isDark } = useTheme();
  const { state, actions } = useGame();
  const [isWriting, setIsWriting] = useState(false);
  const [content, setContent] = useState('');
  const [selectedMood, setSelectedMood] = useState<MoodType | null>(null);

  const entries = state.journal;

  const handleSave = () => {
    if (!content.trim()) return;
    actions.addJournalEntry(content.trim(), selectedMood);
    setContent('');
    setSelectedMood(null);
    setIsWriting(false);
  };

  return (
    <KeyboardAvoidingView
      style={[st.c, { backgroundColor: theme.colors.bg }]}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#EDE9FE', '#DBEAFE']} style={st.hdr}>
          <View style={st.hdrRow}>
            <View>
              <Text style={[st.hdrT, { color: theme.colors.text }]}>📓 Journal</Text>
              <Text style={[st.hdrS, { color: theme.colors.textMuted }]}>{entries.length} entries</Text>
            </View>
            <TouchableOpacity
              style={[st.writeBtn, { backgroundColor: SystemColors.blue }]}
              onPress={() => setIsWriting(!isWriting)}>
              <Text style={st.writeBtnText}>{isWriting ? '✕' : '✏️'} {isWriting ? 'Hủy' : 'Viết'}</Text>
            </TouchableOpacity>
          </View>
        </LinearGradient>

        {/* ═══ NEW ENTRY FORM ═══ */}
        {isWriting && (
          <View style={[st.form, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.blue}30` }]}>
            {/* Mood selector */}
            <Text style={[st.formLabel, { color: theme.colors.textSecondary }]}>Tâm trạng hôm nay?</Text>
            <View style={st.moods}>
              {MOODS.map(m => (
                <TouchableOpacity
                  key={m.type}
                  style={[
                    st.moodBtn,
                    {
                      backgroundColor: selectedMood === m.type ? `${m.color}15` : theme.colors.bgSurface,
                      borderColor: selectedMood === m.type ? m.color : theme.colors.border,
                      borderWidth: selectedMood === m.type ? 1.5 : 1,
                    },
                  ]}
                  onPress={() => setSelectedMood(selectedMood === m.type ? null : m.type)}>
                  <Text style={{ fontSize: 20 }}>{m.icon}</Text>
                  <Text style={[st.moodLabel, { color: selectedMood === m.type ? m.color : theme.colors.textMuted }]}>
                    {m.label}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            {/* Content */}
            <TextInput
              style={[st.textInput, { color: theme.colors.text, backgroundColor: theme.colors.bgSurface, borderColor: theme.colors.border }]}
              placeholder="Hôm nay bạn đã làm gì? Cảm nhận thế nào?..."
              placeholderTextColor={theme.colors.textMuted}
              multiline
              numberOfLines={6}
              textAlignVertical="top"
              value={content}
              onChangeText={setContent}
            />

            <TouchableOpacity
              style={[st.saveBtn, { backgroundColor: content.trim() ? SystemColors.green : theme.colors.bgSurface }]}
              onPress={handleSave}
              disabled={!content.trim()}>
              <Text style={st.saveBtnText}>💾 Lưu Nhật Ký</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* ═══ ENTRIES LIST ═══ */}
        {entries.length === 0 && !isWriting ? (
          <View style={[st.empty, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
            <Text style={st.emptyIcon}>📓</Text>
            <Text style={[st.emptyText, { color: theme.colors.textMuted }]}>
              Chưa có nhật ký nào.{'\n'}Bấm ✏️ Viết để bắt đầu!
            </Text>
          </View>
        ) : (
          entries.map(entry => <JournalCard key={entry.id} entry={entry} />)
        )}

        <View style={{ height: 20 }} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const st = StyleSheet.create({
  c: { flex: 1 }, sc: { paddingBottom: 16 },
  hdr: { paddingTop: 48, paddingBottom: 12, paddingHorizontal: 16 },
  hdrRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  hdrT: { ...Typography.h1 },
  hdrS: { ...Typography.caption, marginTop: 2 },
  writeBtn: { paddingHorizontal: 14, paddingVertical: 7, borderRadius: Radius.md },
  writeBtnText: { color: '#FFF', ...Typography.captionBold },

  // Form
  form: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 14, borderWidth: 1.5 },
  formLabel: { ...Typography.bodyBold, marginBottom: 8 },
  moods: { flexDirection: 'row', gap: 6, marginBottom: 12 },
  moodBtn: { flex: 1, alignItems: 'center', paddingVertical: 8, borderRadius: Radius.md },
  moodLabel: { ...Typography.label, marginTop: 2 },
  textInput: { borderRadius: Radius.md, borderWidth: 1, padding: 12, minHeight: 120, ...Typography.body, lineHeight: 20, marginBottom: 10 },
  saveBtn: { paddingVertical: 10, borderRadius: Radius.md, alignItems: 'center' },
  saveBtnText: { color: '#FFF', ...Typography.bodyBold },

  // Entries
  entry: { marginHorizontal: 10, marginTop: 8, borderRadius: Radius.lg, padding: 14, borderWidth: 1 },
  entryHeader: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 },
  entryDate: { ...Typography.monoSmall },
  moodTag: { flexDirection: 'row', alignItems: 'center', gap: 4, paddingHorizontal: 8, paddingVertical: 3, borderRadius: 8, borderWidth: 1 },
  moodTagText: { ...Typography.label },
  entryContent: { ...Typography.body, lineHeight: 20 },
  tags: { flexDirection: 'row', flexWrap: 'wrap', gap: 4, marginTop: 8 },
  tag: { paddingHorizontal: 8, paddingVertical: 2, borderRadius: 6, borderWidth: 1 },
  tagText: { ...Typography.label },

  empty: { marginHorizontal: 10, marginTop: 20, borderRadius: Radius.lg, padding: 24, borderWidth: 1, alignItems: 'center' },
  emptyIcon: { fontSize: 40, marginBottom: 8 },
  emptyText: { ...Typography.body, textAlign: 'center', lineHeight: 20 },
});
