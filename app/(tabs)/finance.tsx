// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — FINANCE SCREEN v2
// Light theme, compact, gamified finance dashboard
// ═══════════════════════════════════════════════════════════════

import React from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors, FinanceColors } from '@/constants/Colors';

const DATA = {
  month: '05/2026', net: 2350000, inc: 8500000, exp: 6150000,
  saveRate: 32, saveChg: 5, avgDay: 150000, topCat: 'Ăn uống', topPct: 35,
  txs: [
    { id: '1', t: 'expense' as const, name: 'Ăn phở', amt: 50000, cat: '🍜', d: 'Hôm nay' },
    { id: '2', t: 'expense' as const, name: 'Grab đi học', amt: 30000, cat: '🚗', d: 'Hôm nay' },
    { id: '3', t: 'income' as const, name: 'Freelance project', amt: 2000000, cat: '💼', d: 'Hôm qua' },
    { id: '4', t: 'expense' as const, name: 'Cafe', amt: 35000, cat: '☕', d: 'Hôm qua' },
    { id: '5', t: 'expense' as const, name: 'Sách IELTS', amt: 250000, cat: '📚', d: '18/05' },
  ],
};

const fmt = (n: number) => n >= 1e6 ? `${(n / 1e6).toFixed(1)}M` : n >= 1e3 ? `${(n / 1e3).toFixed(0)}k` : `${n}`;

function Chip({ icon, label, value, detail, color }: { icon: string; label: string; value: string; detail: string; color: string }) {
  const { theme } = useTheme();
  return (
    <View style={[st.chip, { backgroundColor: theme.colors.bgCard, borderColor: `${color}20` }]}>
      <Text style={{ fontSize: 18 }}>{icon}</Text>
      <Text style={[st.chipVal, { color }]}>{value}</Text>
      <Text style={[st.chipLbl, { color: theme.colors.textMuted }]}>{label}</Text>
      <Text style={[st.chipDet, { color }]}>{detail}</Text>
    </View>
  );
}

export default function FinanceScreen() {
  const { theme, isDark } = useTheme();
  const d = DATA;
  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#FEF3C7', '#DBEAFE']} style={st.hdr}>
          <Text style={[st.hdrT, { color: theme.colors.text }]}>💰 Finance — {d.month}</Text>
        </LinearGradient>

        {/* Insight chips */}
        <View style={st.chips}>
          <Chip icon="💹" label="Tiết kiệm" value={`${d.saveRate}%`} detail={`↑${d.saveChg}%`} color={FinanceColors.income} />
          <Chip icon="📊" label="Chi TB" value={fmt(d.avgDay)} detail="↑2%" color={SystemColors.orange} />
          <Chip icon="🏷️" label="Top" value={d.topCat} detail={`${d.topPct}%`} color={SystemColors.pink} />
        </View>

        {/* Net cashflow */}
        <View style={[st.hero, { backgroundColor: theme.colors.bgCard, borderColor: d.net >= 0 ? `${FinanceColors.income}30` : `${FinanceColors.expense}30` }]}>
          <Text style={[st.heroLbl, { color: theme.colors.textMuted }]}>NET CASHFLOW</Text>
          <Text style={[st.heroVal, { color: d.net >= 0 ? FinanceColors.income : FinanceColors.expense }]}>
            {d.net >= 0 ? '+' : ''}{d.net.toLocaleString('vi-VN')}đ
          </Text>
          <View style={st.heroSubs}>
            <Text style={[st.heroSub, { color: FinanceColors.income }]}>Thu: {fmt(d.inc)}</Text>
            <Text style={[st.heroSub, { color: FinanceColors.expense }]}>Chi: {fmt(d.exp)}</Text>
          </View>
        </View>

        {/* Actions */}
        <View style={st.acts}>
          {[
            { i: '➕', l: 'Thêm GD', c: SystemColors.blue },
            { i: '📋', l: 'Tất cả', c: SystemColors.purple },
            { i: '🎯', l: 'Mục tiêu', c: SystemColors.green },
            { i: '📒', l: 'Sổ nợ', c: SystemColors.orange },
            { i: '🤖', l: 'AI', c: SystemColors.cyan },
          ].map(a => (
            <TouchableOpacity key={a.l} style={[st.act, { backgroundColor: theme.colors.bgCard, borderColor: `${a.c}20` }]} activeOpacity={0.7}>
              <Text style={{ fontSize: 18 }}>{a.i}</Text>
              <Text style={[st.actL, { color: theme.colors.textSecondary }]}>{a.l}</Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Transactions */}
        <View style={[st.card, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
          <Text style={[st.cardT, { color: theme.colors.text }]}>📋 Giao dịch gần đây</Text>
          {d.txs.map(tx => (
            <View key={tx.id} style={[st.tx, { borderBottomColor: theme.colors.borderLight }]}>
              <Text style={{ fontSize: 20 }}>{tx.cat}</Text>
              <View style={st.txInfo}>
                <Text style={[st.txName, { color: theme.colors.text }]}>{tx.name}</Text>
                <Text style={[st.txDate, { color: theme.colors.textMuted }]}>{tx.d}</Text>
              </View>
              <Text style={[st.txAmt, { color: tx.t === 'income' ? FinanceColors.income : FinanceColors.expense }]}>
                {tx.t === 'income' ? '+' : '-'}{tx.amt.toLocaleString('vi-VN')}đ
              </Text>
            </View>
          ))}
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

  chips: { flexDirection: 'row', marginHorizontal: 10, gap: 6 },
  chip: { flex: 1, alignItems: 'center', padding: 10, borderRadius: Radius.md, borderWidth: 1 },
  chipVal: { ...Typography.h2, marginTop: 2 },
  chipLbl: { ...Typography.label, marginTop: 1 },
  chipDet: { ...Typography.label, marginTop: 1 },

  hero: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 16, borderWidth: 1, alignItems: 'center' },
  heroLbl: { ...Typography.label, letterSpacing: 1 },
  heroVal: { fontSize: 24, fontWeight: '900', marginTop: 2, fontFamily: 'SpaceMono' },
  heroSubs: { flexDirection: 'row', gap: 20, marginTop: 6 },
  heroSub: { ...Typography.bodyBold },

  acts: { flexDirection: 'row', marginHorizontal: 10, marginTop: 10, gap: 6 },
  act: { flex: 1, alignItems: 'center', paddingVertical: 10, borderRadius: Radius.md, borderWidth: 1 },
  actL: { ...Typography.label, marginTop: 3 },

  card: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1 },
  cardT: { ...Typography.h3, marginBottom: 8 },

  tx: { flexDirection: 'row', alignItems: 'center', paddingVertical: 8, borderBottomWidth: 0.5 },
  txInfo: { flex: 1, marginLeft: 8 },
  txName: { ...Typography.bodyBold },
  txDate: { ...Typography.label, marginTop: 1 },
  txAmt: { ...Typography.mono },
});
