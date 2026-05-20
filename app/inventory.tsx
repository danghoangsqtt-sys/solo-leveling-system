// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — INVENTORY SCREEN
// MMORPG-style grid layout with rarity system
// ═══════════════════════════════════════════════════════════════

import React, { useState } from 'react';
import { View, Text, ScrollView, StyleSheet, TouchableOpacity, Modal, Animated } from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useTheme, Typography, Radius } from '@/constants/Theme';
import { SystemColors, RarityColors } from '@/constants/Colors';
import type { InventoryItem, RarityTier } from '@/constants/Types';
import { useGame } from '@/constants/GameContext';

const RARITY_LABELS: Record<RarityTier, string> = {
  common: 'Common', uncommon: 'Uncommon', rare: 'Rare',
  epic: 'Epic', legendary: 'Legendary', mythic: 'Mythic',
};

const RARITY_GLOW: Record<RarityTier, boolean> = {
  common: false, uncommon: false, rare: false,
  epic: false, legendary: true, mythic: true,
};

const CATEGORY_LABELS: Record<string, string> = {
  equipment: '⚔️ Trang bị',
  consumable: '🧪 Tiêu hao',
  key_item: '🔑 Vật phẩm đặc biệt',
  material: '🪨 Nguyên liệu',
  quest_item: '📜 Quest Item',
};

function ItemCard({ item, onPress }: { item: InventoryItem; onPress: () => void }) {
  const { theme } = useTheme();
  const rc = RarityColors[item.rarity];
  const glow = RARITY_GLOW[item.rarity];

  return (
    <TouchableOpacity
      style={[
        st.item,
        {
          backgroundColor: theme.colors.bgCard,
          borderColor: `${rc}50`,
          borderWidth: glow ? 1.5 : 1,
          shadowColor: glow ? rc : 'transparent',
          shadowOpacity: glow ? 0.3 : 0,
          shadowRadius: glow ? 8 : 0,
          elevation: glow ? 4 : 0,
        },
      ]}
      onPress={onPress}
      activeOpacity={0.7}>
      <Text style={st.itemIcon}>{item.iconName}</Text>
      {item.quantity > 1 && (
        <View style={[st.itemQty, { backgroundColor: `${rc}20`, borderColor: `${rc}50` }]}>
          <Text style={[st.itemQtyText, { color: rc }]}>{item.quantity}</Text>
        </View>
      )}
      <Text style={[st.itemName, { color: theme.colors.text }]} numberOfLines={1}>{item.name}</Text>
      <View style={[st.rarityBadge, { backgroundColor: `${rc}15`, borderColor: `${rc}40` }]}>
        <Text style={[st.rarityText, { color: rc }]}>{RARITY_LABELS[item.rarity]}</Text>
      </View>
    </TouchableOpacity>
  );
}

function ItemDetail({ item, visible, onClose }: { item: InventoryItem | null; visible: boolean; onClose: () => void }) {
  const { theme } = useTheme();
  if (!item) return null;
  const rc = RarityColors[item.rarity];

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <View style={st.detailOverlay}>
        <View style={[st.detailCard, { backgroundColor: theme.colors.bgCard, borderColor: `${rc}40` }]}>
          <Text style={st.detailIcon}>{item.iconName}</Text>
          <Text style={[st.detailName, { color: rc }]}>{item.name}</Text>
          <View style={[st.detailRarity, { backgroundColor: `${rc}12`, borderColor: `${rc}30` }]}>
            <Text style={[st.detailRarityText, { color: rc }]}>{RARITY_LABELS[item.rarity]}</Text>
          </View>
          <Text style={[st.detailDesc, { color: theme.colors.textSecondary }]}>{item.description}</Text>
          <View style={st.detailMeta}>
            <Text style={[st.detailMetaItem, { color: theme.colors.textMuted }]}>
              📦 Số lượng: {item.quantity}
            </Text>
            <Text style={[st.detailMetaItem, { color: theme.colors.textMuted }]}>
              🏷️ {CATEGORY_LABELS[item.category] || item.category}
            </Text>
            <Text style={[st.detailMetaItem, { color: theme.colors.textMuted }]}>
              📅 {new Date(item.obtainedAt).toLocaleDateString('vi-VN')}
            </Text>
          </View>
          <TouchableOpacity style={[st.detailBtn, { backgroundColor: SystemColors.blue }]} onPress={onClose}>
            <Text style={st.detailBtnText}>Đóng</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
}

export default function InventoryScreen() {
  const { theme, isDark } = useTheme();
  const { state } = useGame();
  const [selectedItem, setSelectedItem] = useState<InventoryItem | null>(null);
  const [showDetail, setShowDetail] = useState(false);

  const items = state.inventory;
  const grouped = items.reduce((acc, item) => {
    const cat = item.category;
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(item);
    return acc;
  }, {} as Record<string, InventoryItem[]>);

  return (
    <View style={[st.c, { backgroundColor: theme.colors.bg }]}>
      <ScrollView contentContainerStyle={st.sc} showsVerticalScrollIndicator={false}>
        <LinearGradient colors={isDark ? ['#1E293B', '#0F172A'] : ['#FEF3C7', '#FECACA', '#DBEAFE']} style={st.hdr}>
          <Text style={[st.hdrT, { color: theme.colors.text }]}>🎒 Inventory</Text>
          <Text style={[st.hdrS, { color: theme.colors.textMuted }]}>{items.length} vật phẩm</Text>
        </LinearGradient>

        {items.length === 0 ? (
          <View style={[st.empty, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
            <Text style={st.emptyIcon}>🎒</Text>
            <Text style={[st.emptyText, { color: theme.colors.textMuted }]}>Kho đồ trống.{'\n'}Hoàn thành quests để nhận items!</Text>
          </View>
        ) : (
          Object.entries(grouped).map(([cat, catItems]) => (
            <View key={cat} style={[st.section, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
              <Text style={[st.sectionTitle, { color: theme.colors.text }]}>{CATEGORY_LABELS[cat] || cat}</Text>
              <View style={st.grid}>
                {catItems.map(item => (
                  <ItemCard
                    key={item.id}
                    item={item}
                    onPress={() => { setSelectedItem(item); setShowDetail(true); }}
                  />
                ))}
              </View>
            </View>
          ))
        )}

        <View style={{ height: 20 }} />
      </ScrollView>

      <ItemDetail item={selectedItem} visible={showDetail} onClose={() => setShowDetail(false)} />
    </View>
  );
}

const st = StyleSheet.create({
  c: { flex: 1 }, sc: { paddingBottom: 16 },
  hdr: { paddingTop: 48, paddingBottom: 12, paddingHorizontal: 16 },
  hdrT: { ...Typography.h1 },
  hdrS: { ...Typography.caption, marginTop: 2 },

  section: { marginHorizontal: 10, marginTop: 10, borderRadius: Radius.lg, padding: 12, borderWidth: 1 },
  sectionTitle: { ...Typography.h3, marginBottom: 8 },
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },

  item: { width: '30%', minWidth: 95, alignItems: 'center', padding: 10, borderRadius: Radius.md, position: 'relative' },
  itemIcon: { fontSize: 28, marginBottom: 4 },
  itemQty: { position: 'absolute', top: 4, right: 4, width: 18, height: 18, borderRadius: 9, borderWidth: 1, alignItems: 'center', justifyContent: 'center' },
  itemQtyText: { fontSize: 9, fontWeight: '800' },
  itemName: { ...Typography.caption, textAlign: 'center', marginBottom: 3 },
  rarityBadge: { paddingHorizontal: 6, paddingVertical: 1, borderRadius: 6, borderWidth: 1 },
  rarityText: { fontSize: 8, fontWeight: '700' },

  empty: { marginHorizontal: 10, marginTop: 20, borderRadius: Radius.lg, padding: 24, borderWidth: 1, alignItems: 'center' },
  emptyIcon: { fontSize: 40, marginBottom: 8 },
  emptyText: { ...Typography.body, textAlign: 'center', lineHeight: 20 },

  // Detail modal
  detailOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.6)', justifyContent: 'center', alignItems: 'center' },
  detailCard: { width: '80%', maxWidth: 320, borderRadius: Radius.xl, padding: 24, alignItems: 'center', borderWidth: 2 },
  detailIcon: { fontSize: 48, marginBottom: 8 },
  detailName: { fontSize: 16, fontWeight: '800', textAlign: 'center' },
  detailRarity: { paddingHorizontal: 12, paddingVertical: 3, borderRadius: 8, borderWidth: 1, marginTop: 6 },
  detailRarityText: { ...Typography.captionBold },
  detailDesc: { ...Typography.body, textAlign: 'center', marginTop: 12, lineHeight: 18 },
  detailMeta: { marginTop: 12, gap: 4, width: '100%' },
  detailMetaItem: { ...Typography.caption },
  detailBtn: { marginTop: 16, paddingHorizontal: 32, paddingVertical: 10, borderRadius: Radius.md },
  detailBtnText: { color: '#FFF', ...Typography.bodyBold },
});
