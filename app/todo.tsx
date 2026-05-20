// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — TODO LIST SCREEN
// CRUD + priority + deadline + "Convert to Quest" option
// ═══════════════════════════════════════════════════════════════

import React, { useState } from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, TextInput, Platform } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors } from '@/constants/Colors';
import type { TodoItem } from '@/constants/Types';
import { useGame } from '@/constants/GameContext';

const PRIORITY_CONFIG = {
  urgent: { icon: '🔴', label: 'Urgent', color: '#EF4444' },
  high: { icon: '🟠', label: 'High', color: '#F97316' },
  medium: { icon: '🟡', label: 'Medium', color: '#F59E0B' },
  low: { icon: '🟢', label: 'Low', color: '#10B981' },
} as const;

function TodoCard({ todo, onToggle, onDelete }: { todo: TodoItem; onToggle: () => void; onDelete: () => void }) {
  const { theme } = useTheme();
  const p = PRIORITY_CONFIG[todo.priority];

  return (
    <View
      style={[
        st.todo,
        {
          backgroundColor: theme.colors.bgCard,
          borderColor: todo.isCompleted ? `${SystemColors.green}30` : `${p.color}20`,
          opacity: todo.isCompleted ? 0.5 : 1,
        },
      ]}>
      <TouchableOpacity
        style={[
          st.checkbox,
          {
            borderColor: todo.isCompleted ? SystemColors.green : p.color,
            backgroundColor: todo.isCompleted ? SystemColors.green : 'transparent',
          },
        ]}
        onPress={onToggle}>
        {todo.isCompleted && <Text style={st.checkmark}>✓</Text>}
      </TouchableOpacity>

      <View style={st.todoContent}>
        <View style={st.todoRow}>
          <Text style={{ fontSize: 10 }}>{p.icon}</Text>
          <Text
            style={[
              st.todoTitle,
              {
                color: theme.colors.text,
                textDecorationLine: todo.isCompleted ? 'line-through' : 'none',
              },
            ]}
            numberOfLines={2}>
            {todo.title}
          </Text>
        </View>
        {todo.description && (
          <Text style={[st.todoDesc, { color: theme.colors.textMuted }]} numberOfLines={1}>
            {todo.description}
          </Text>
        )}
        <View style={st.todoMeta}>
          <View style={[st.prioTag, { backgroundColor: `${p.color}10`, borderColor: `${p.color}30` }]}>
            <Text style={[st.prioText, { color: p.color }]}>{p.label}</Text>
          </View>
          <Text style={[st.todoDate, { color: theme.colors.textMuted }]}>
            {new Date(todo.createdAt).toLocaleDateString('vi-VN')}
          </Text>
        </View>
      </View>

      <TouchableOpacity style={st.deleteBtn} onPress={onDelete}>
        <Text style={[st.deleteBtnText, { color: theme.colors.textMuted }]}>✕</Text>
      </TouchableOpacity>
    </View>
  );
}

export default function TodoScreen() {
  const { theme, isDark } = useTheme();
  const { state, actions } = useGame();
  const [isAdding, setIsAdding] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newPriority, setNewPriority] = useState<TodoItem['priority']>('medium');
  const [filter, setFilter] = useState<'all' | 'active' | 'done'>('all');

  const todos = state.todos;
  const filtered = todos.filter(t => {
    if (filter === 'active') return !t.isCompleted;
    if (filter === 'done') return t.isCompleted;
    return true;
  });

  const activeCount = todos.filter(t => !t.isCompleted).length;
  const doneCount = todos.filter(t => t.isCompleted).length;

  const handleAdd = () => {
    if (!newTitle.trim()) return;
    actions.addTodo(newTitle.trim(), newPriority);
    setNewTitle('');
    setIsAdding(false);
  };

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#D1FAE5', '#DBEAFE']} style={st.hdr}>
          <View style={st.hdrRow}>
            <View>
              <Text style={[st.hdrT, { color: theme.colors.text }]}>✅ To-Do List</Text>
              <Text style={[st.hdrS, { color: theme.colors.textMuted }]}>{activeCount} việc cần làm • {doneCount} đã xong</Text>
            </View>
            <TouchableOpacity
              style={[st.addBtn, { backgroundColor: SystemColors.green }]}
              onPress={() => setIsAdding(!isAdding)}>
              <Text style={st.addBtnText}>{isAdding ? '✕' : '+'}</Text>
            </TouchableOpacity>
          </View>
        </LinearGradient>

        {/* Filter tabs */}
        <View style={st.filters}>
          {(['all', 'active', 'done'] as const).map(f => (
            <TouchableOpacity
              key={f}
              style={[
                st.filterBtn,
                {
                  backgroundColor: filter === f ? `${SystemColors.blue}12` : theme.colors.bgCard,
                  borderColor: filter === f ? SystemColors.blue : theme.colors.border,
                },
              ]}
              onPress={() => setFilter(f)}>
              <Text style={[st.filterText, { color: filter === f ? SystemColors.blue : theme.colors.textMuted }]}>
                {f === 'all' ? `Tất cả (${todos.length})` : f === 'active' ? `Đang làm (${activeCount})` : `Xong (${doneCount})`}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Add form */}
        {isAdding && (
          <View style={[st.form, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.green}30` }]}>
            <TextInput
              style={[st.input, { color: theme.colors.text, borderColor: theme.colors.border, backgroundColor: theme.colors.bgSurface }]}
              placeholder="Việc cần làm..."
              placeholderTextColor={theme.colors.textMuted}
              value={newTitle}
              onChangeText={setNewTitle}
              autoFocus
            />
            <View style={st.prioRow}>
              {(Object.keys(PRIORITY_CONFIG) as TodoItem['priority'][]).map(p => (
                <TouchableOpacity
                  key={p}
                  style={[
                    st.prioBtn,
                    {
                      backgroundColor: newPriority === p ? `${PRIORITY_CONFIG[p].color}15` : theme.colors.bgSurface,
                      borderColor: newPriority === p ? PRIORITY_CONFIG[p].color : theme.colors.border,
                    },
                  ]}
                  onPress={() => setNewPriority(p)}>
                  <Text style={{ fontSize: 10 }}>{PRIORITY_CONFIG[p].icon}</Text>
                  <Text style={[st.prioBtnText, { color: newPriority === p ? PRIORITY_CONFIG[p].color : theme.colors.textMuted }]}>
                    {PRIORITY_CONFIG[p].label}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
            <TouchableOpacity
              style={[st.submitBtn, { backgroundColor: newTitle.trim() ? SystemColors.green : theme.colors.bgSurface }]}
              onPress={handleAdd}
              disabled={!newTitle.trim()}>
              <Text style={st.submitBtnText}>✅ Thêm</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* Todo list */}
        {filtered.length === 0 ? (
          <View style={[st.empty, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
            <Text style={st.emptyIcon}>✅</Text>
            <Text style={[st.emptyText, { color: theme.colors.textMuted }]}>
              {filter === 'done' ? 'Chưa hoàn thành việc nào.' : 'Không có việc cần làm.\nBấm + để thêm!'}
            </Text>
          </View>
        ) : (
          filtered.map(todo => (
            <TodoCard
              key={todo.id}
              todo={todo}
              onToggle={() => actions.toggleTodo(todo.id)}
              onDelete={() => actions.deleteTodo(todo.id)}
            />
          ))
        )}

        <View style={{ height: 20 }} />
      </ScrollView>
    </View>
  );
}

const st = StyleSheet.create({
  c: { flex: 1 }, sc: { paddingBottom: 16 },
  hdr: { paddingTop: 48, paddingBottom: 12, paddingHorizontal: 16 },
  hdrRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  hdrT: { ...Typography.h1 },
  hdrS: { ...Typography.caption, marginTop: 2 },
  addBtn: { width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center' },
  addBtnText: { color: '#FFF', fontSize: 20, fontWeight: '700' },

  // Filters
  filters: { flexDirection: 'row', marginHorizontal: 10, marginTop: 6, gap: 6 },
  filterBtn: { flex: 1, paddingVertical: 6, borderRadius: Radius.sm, borderWidth: 1, alignItems: 'center' },
  filterText: { ...Typography.captionBold },

  // Form
  form: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1.5 },
  input: { borderRadius: Radius.md, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 10, ...Typography.body, marginBottom: 8 },
  prioRow: { flexDirection: 'row', gap: 6, marginBottom: 8 },
  prioBtn: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 4, paddingVertical: 6, borderRadius: Radius.sm, borderWidth: 1 },
  prioBtnText: { ...Typography.label },
  submitBtn: { paddingVertical: 10, borderRadius: Radius.md, alignItems: 'center' },
  submitBtnText: { color: '#FFF', ...Typography.bodyBold },

  // Todo card
  todo: { flexDirection: 'row', alignItems: 'flex-start', marginHorizontal: 10, marginTop: 6, borderRadius: Radius.md, padding: 10, borderWidth: 1 },
  checkbox: { width: 22, height: 22, borderRadius: 6, borderWidth: 2, alignItems: 'center', justifyContent: 'center', marginTop: 1, marginRight: 8 },
  checkmark: { color: '#FFF', fontSize: 12, fontWeight: '800' },
  todoContent: { flex: 1 },
  todoRow: { flexDirection: 'row', alignItems: 'center', gap: 4 },
  todoTitle: { ...Typography.bodyBold, flex: 1 },
  todoDesc: { ...Typography.caption, marginTop: 2 },
  todoMeta: { flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 4 },
  prioTag: { paddingHorizontal: 6, paddingVertical: 1, borderRadius: 4, borderWidth: 1 },
  prioText: { fontSize: 8, fontWeight: '700' },
  todoDate: { ...Typography.label },
  deleteBtn: { padding: 4 },
  deleteBtnText: { fontSize: 14, fontWeight: '600' },

  empty: { marginHorizontal: 10, marginTop: 20, borderRadius: Radius.lg, padding: 24, borderWidth: 1, alignItems: 'center' },
  emptyIcon: { fontSize: 40, marginBottom: 8 },
  emptyText: { ...Typography.body, textAlign: 'center', lineHeight: 20 },
});
