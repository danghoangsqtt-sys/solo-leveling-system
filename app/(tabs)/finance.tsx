// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — FINANCE DASHBOARD v3 (Live Data)
// Budget tracking, transactions, spending analysis
// Connected to GameContext
// ═══════════════════════════════════════════════════════════════

import React, { useState, useMemo } from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, TextInput, Modal } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors } from '@/constants/Colors';
import { useGame } from '@/constants/GameContext';
import type { Transaction } from '@/constants/Types';

const CATEGORIES = [
  { id: 'food', icon: '🍔', label: 'Ăn uống', color: '#F97316' },
  { id: 'transport', icon: '🚌', label: 'Đi lại', color: '#3B82F6' },
  { id: 'shopping', icon: '🛍️', label: 'Mua sắm', color: '#EC4899' },
  { id: 'bills', icon: '📄', label: 'Hóa đơn', color: '#EF4444' },
  { id: 'education', icon: '📚', label: 'Học tập', color: '#8B5CF6' },
  { id: 'health', icon: '💊', label: 'Sức khỏe', color: '#10B981' },
  { id: 'entertainment', icon: '🎮', label: 'Giải trí', color: '#F59E0B' },
  { id: 'income', icon: '💰', label: 'Thu nhập', color: '#22C55E' },
  { id: 'other', icon: '📦', label: 'Khác', color: '#94A3B8' },
];

function TransactionCard({ tx }: { tx: Transaction }) {
  const { theme } = useTheme();
  const cat = CATEGORIES.find(c => c.id === tx.category) || CATEGORIES[CATEGORIES.length - 1];
  const isIncome = tx.type === 'income';
  const date = new Date(tx.createdAt);

  return (
    <View style={[st.txCard, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
      <View style={[st.txIcon, { backgroundColor: `${cat.color}12` }]}>
        <Text style={{ fontSize: 18 }}>{cat.icon}</Text>
      </View>
      <View style={st.txContent}>
        <Text style={[st.txTitle, { color: theme.colors.text }]} numberOfLines={1}>{tx.description}</Text>
        <Text style={[st.txDate, { color: theme.colors.textMuted }]}>
          {date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })} • {cat.label}
        </Text>
      </View>
      <Text style={[st.txAmount, { color: isIncome ? SystemColors.green : SystemColors.red }]}>
        {isIncome ? '+' : '-'}{tx.amount.toLocaleString()}₫
      </Text>
    </View>
  );
}

export default function FinanceScreen() {
  const { theme, isDark } = useTheme();
  const { state, actions } = useGame();
  const [isAdding, setIsAdding] = useState(false);
  const [txDesc, setTxDesc] = useState('');
  const [txAmount, setTxAmount] = useState('');
  const [txType, setTxType] = useState<'expense' | 'income'>('expense');
  const [txCat, setTxCat] = useState('food');

  const transactions = state.transactions;

  // Compute summary
  const summary = useMemo(() => {
    const now = new Date();
    const thisMonth = transactions.filter(tx => {
      const d = new Date(tx.createdAt);
      return d.getMonth() === now.getMonth() && d.getFullYear() === now.getFullYear();
    });

    const income = thisMonth.filter(t => t.type === 'income').reduce((s, t) => s + t.amount, 0);
    const expense = thisMonth.filter(t => t.type === 'expense').reduce((s, t) => s + t.amount, 0);
    const balance = income - expense;

    return { income, expense, balance, count: thisMonth.length };
  }, [transactions]);

  const handleAdd = () => {
    if (!txDesc.trim() || !txAmount.trim()) return;
    const amount = parseInt(txAmount.replace(/\D/g, ''), 10);
    if (isNaN(amount) || amount <= 0) return;

    actions.addTransaction({
      amount,
      type: txType,
      category: txCat,
      description: txDesc.trim(),
      note: null,
    });

    setTxDesc('');
    setTxAmount('');
    setIsAdding(false);
  };

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#D1FAE5', '#FEF3C7']} style={st.hdr}>
          <View style={st.hdrRow}>
            <View>
              <Text style={[st.hdrT, { color: theme.colors.text }]}>💰 Finance</Text>
              <Text style={[st.hdrS, { color: theme.colors.textMuted }]}>Quản lý chi tiêu</Text>
            </View>
            <TouchableOpacity
              style={[st.addBtn, { backgroundColor: SystemColors.green }]}
              onPress={() => setIsAdding(!isAdding)}>
              <Text style={st.addBtnText}>{isAdding ? '✕' : '+'}</Text>
            </TouchableOpacity>
          </View>
        </LinearGradient>

        {/* Summary cards */}
        <View style={st.summaryRow}>
          <View style={[st.sumCard, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.green}20` }]}>
            <Text style={[st.sumLabel, { color: theme.colors.textMuted }]}>Thu nhập</Text>
            <Text style={[st.sumVal, { color: SystemColors.green }]}>+{summary.income.toLocaleString()}₫</Text>
          </View>
          <View style={[st.sumCard, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.red}20` }]}>
            <Text style={[st.sumLabel, { color: theme.colors.textMuted }]}>Chi tiêu</Text>
            <Text style={[st.sumVal, { color: SystemColors.red }]}>-{summary.expense.toLocaleString()}₫</Text>
          </View>
          <View style={[st.sumCard, { backgroundColor: theme.colors.bgCard, borderColor: `${summary.balance >= 0 ? SystemColors.green : SystemColors.red}20` }]}>
            <Text style={[st.sumLabel, { color: theme.colors.textMuted }]}>Số dư</Text>
            <Text style={[st.sumVal, { color: summary.balance >= 0 ? SystemColors.green : SystemColors.red }]}>
              {summary.balance >= 0 ? '+' : ''}{summary.balance.toLocaleString()}₫
            </Text>
          </View>
        </View>

        {/* Add transaction form */}
        {isAdding && (
          <View style={[st.form, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.green}30` }]}>
            {/* Type toggle */}
            <View style={st.typeRow}>
              <TouchableOpacity
                style={[st.typeBtn, { backgroundColor: txType === 'expense' ? `${SystemColors.red}12` : theme.colors.bgSurface, borderColor: txType === 'expense' ? SystemColors.red : theme.colors.border }]}
                onPress={() => setTxType('expense')}>
                <Text style={[st.typeBtnText, { color: txType === 'expense' ? SystemColors.red : theme.colors.textMuted }]}>💸 Chi tiêu</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[st.typeBtn, { backgroundColor: txType === 'income' ? `${SystemColors.green}12` : theme.colors.bgSurface, borderColor: txType === 'income' ? SystemColors.green : theme.colors.border }]}
                onPress={() => setTxType('income')}>
                <Text style={[st.typeBtnText, { color: txType === 'income' ? SystemColors.green : theme.colors.textMuted }]}>💰 Thu nhập</Text>
              </TouchableOpacity>
            </View>

            {/* Amount */}
            <TextInput
              style={[st.input, { color: theme.colors.text, borderColor: theme.colors.border, backgroundColor: theme.colors.bgSurface }]}
              placeholder="Số tiền (VNĐ)..."
              placeholderTextColor={theme.colors.textMuted}
              keyboardType="numeric"
              value={txAmount}
              onChangeText={setTxAmount}
            />

            {/* Description */}
            <TextInput
              style={[st.input, { color: theme.colors.text, borderColor: theme.colors.border, backgroundColor: theme.colors.bgSurface }]}
              placeholder="Mô tả chi tiêu..."
              placeholderTextColor={theme.colors.textMuted}
              value={txDesc}
              onChangeText={setTxDesc}
            />

            {/* Category */}
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={st.catScroll}>
              {CATEGORIES.filter(c => txType === 'income' ? c.id === 'income' : c.id !== 'income').map(cat => (
                <TouchableOpacity
                  key={cat.id}
                  style={[st.catBtn, { backgroundColor: txCat === cat.id ? `${cat.color}15` : theme.colors.bgSurface, borderColor: txCat === cat.id ? cat.color : theme.colors.border }]}
                  onPress={() => setTxCat(cat.id)}>
                  <Text style={{ fontSize: 14 }}>{cat.icon}</Text>
                  <Text style={[st.catLabel, { color: txCat === cat.id ? cat.color : theme.colors.textMuted }]}>{cat.label}</Text>
                </TouchableOpacity>
              ))}
            </ScrollView>

            <TouchableOpacity
              style={[st.submitBtn, { backgroundColor: txDesc.trim() && txAmount.trim() ? SystemColors.green : theme.colors.bgSurface }]}
              onPress={handleAdd}
              disabled={!txDesc.trim() || !txAmount.trim()}>
              <Text style={st.submitBtnText}>💾 Lưu giao dịch</Text>
            </TouchableOpacity>
          </View>
        )}

        {/* Game currency */}
        <View style={[st.gameCurrency, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
          <Text style={[st.gcTitle, { color: theme.colors.text }]}>🎮 Game Currency</Text>
          <View style={st.gcRow}>
            <View style={st.gcItem}>
              <Text style={{ fontSize: 20 }}>💰</Text>
              <Text style={[st.gcVal, { color: SystemColors.gold }]}>{state.user?.gold.toLocaleString() || '0'}</Text>
              <Text style={[st.gcLabel, { color: theme.colors.textMuted }]}>Gold</Text>
            </View>
            <View style={st.gcItem}>
              <Text style={{ fontSize: 20 }}>💎</Text>
              <Text style={[st.gcVal, { color: SystemColors.cyan }]}>{state.user?.gems || 0}</Text>
              <Text style={[st.gcLabel, { color: theme.colors.textMuted }]}>Gems</Text>
            </View>
            <View style={st.gcItem}>
              <Text style={{ fontSize: 20 }}>⚠️</Text>
              <Text style={[st.gcVal, { color: SystemColors.red }]}>{state.user?.debtPoints || 0}</Text>
              <Text style={[st.gcLabel, { color: theme.colors.textMuted }]}>Debt</Text>
            </View>
          </View>
        </View>

        {/* Transaction history */}
        <View style={st.txSection}>
          <Text style={[st.txSectionTitle, { color: theme.colors.text }]}>📋 Lịch sử giao dịch</Text>
          {transactions.length === 0 ? (
            <View style={[st.empty, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
              <Text style={st.emptyIcon}>💳</Text>
              <Text style={[st.emptyText, { color: theme.colors.textMuted }]}>
                Chưa có giao dịch nào.{'\n'}Bấm + để thêm!
              </Text>
            </View>
          ) : (
            transactions.slice(0, 20).map(tx => <TransactionCard key={tx.id} tx={tx} />)
          )}
        </View>

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

  // Summary
  summaryRow: { flexDirection: 'row', marginHorizontal: 10, marginTop: 6, gap: 6 },
  sumCard: { flex: 1, padding: 10, borderRadius: Radius.md, borderWidth: 1, alignItems: 'center' },
  sumLabel: { ...Typography.label },
  sumVal: { ...Typography.mono, marginTop: 2 },

  // Form
  form: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1.5 },
  typeRow: { flexDirection: 'row', gap: 6, marginBottom: 8 },
  typeBtn: { flex: 1, paddingVertical: 8, borderRadius: Radius.md, borderWidth: 1, alignItems: 'center' },
  typeBtnText: { ...Typography.captionBold },
  input: { borderRadius: Radius.md, borderWidth: 1, paddingHorizontal: 12, paddingVertical: 10, ...Typography.body, marginBottom: 8 },
  catScroll: { marginBottom: 8 },
  catBtn: { flexDirection: 'row', alignItems: 'center', gap: 4, paddingHorizontal: 10, paddingVertical: 6, borderRadius: Radius.sm, borderWidth: 1, marginRight: 6 },
  catLabel: { ...Typography.label },
  submitBtn: { paddingVertical: 10, borderRadius: Radius.md, alignItems: 'center' },
  submitBtnText: { color: '#FFF', ...Typography.bodyBold },

  // Game currency
  gameCurrency: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1 },
  gcTitle: { ...Typography.h3, marginBottom: 8 },
  gcRow: { flexDirection: 'row', gap: 12, justifyContent: 'space-around' },
  gcItem: { alignItems: 'center' },
  gcVal: { ...Typography.stat, marginTop: 2 },
  gcLabel: { ...Typography.label, marginTop: 1 },

  // Transactions
  txSection: { marginHorizontal: 10, marginTop: 10 },
  txSectionTitle: { ...Typography.h3, marginBottom: 8 },
  txCard: { flexDirection: 'row', alignItems: 'center', padding: 10, borderRadius: Radius.md, borderWidth: 1, marginBottom: 4 },
  txIcon: { width: 36, height: 36, borderRadius: 18, alignItems: 'center', justifyContent: 'center', marginRight: 8 },
  txContent: { flex: 1 },
  txTitle: { ...Typography.bodyBold },
  txDate: { ...Typography.label, marginTop: 1 },
  txAmount: { ...Typography.mono },

  empty: { borderRadius: Radius.lg, padding: 24, borderWidth: 1, alignItems: 'center' },
  emptyIcon: { fontSize: 40, marginBottom: 8 },
  emptyText: { ...Typography.body, textAlign: 'center', lineHeight: 20 },
});
