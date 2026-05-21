"use client";

import { useState } from "react";
import type { InventoryItem } from "@/lib/types";
import { RARITY_INFO } from "@/lib/types";
import "./inventory.css";

// ── Demo Inventory Data ──
const DEMO_INVENTORY: InventoryItem[] = [
  {
    id: "i1",
    name: "Vé Xóa Phạt (Cấp Thấp)",
    description: "Sử dụng để xóa 1 Điểm Phạt (Debt Point) hệ thống. Chỉ áp dụng cho các nhiệm vụ Rank D trở xuống.",
    icon_name: "🎟️",
    rarity: "uncommon",
    category: "consumable",
    quantity: 3,
    obtained_at: "2026-05-20T10:00:00Z",
    quest_id: "Q-001",
  },
  {
    id: "i2",
    name: "Thuốc Hồi Phục Tập Trung",
    description: "Tăng 50% lượng EXP nhận được từ các nhiệm vụ học tập trong vòng 2 giờ.",
    icon_name: "🧪",
    rarity: "rare",
    category: "potion",
    quantity: 1,
    obtained_at: "2026-05-19T14:30:00Z",
    quest_id: "Q-005",
  },
  {
    id: "i3",
    name: "Sách Cổ Kỹ Năng",
    description: "Cấp ngay 500 Skill Points cho bất kỳ kỹ năng nào chưa đạt cấp tối đa.",
    icon_name: "📖",
    rarity: "epic",
    category: "book",
    quantity: 2,
    obtained_at: "2026-05-15T08:00:00Z",
    quest_id: "Q-BOSS",
  },
  {
    id: "i4",
    name: "Mảnh Vỡ Ngôi Sao",
    description: "Nguyên liệu rèn luyện bí ẩn. Người ta đồn rằng nếu thu thập đủ 10 mảnh có thể triệu hồi một phép màu.",
    icon_name: "⭐",
    rarity: "legendary",
    category: "material",
    quantity: 7,
    obtained_at: "2026-05-10T12:00:00Z",
    quest_id: null,
  },
  {
    id: "i5",
    name: "Nước Mắt Ác Ma",
    description: "Cực kỳ quý hiếm. Sử dụng để reset toàn bộ Skill Tree hoặc thay đổi Class mà không mất cấp độ.",
    icon_name: "💧",
    rarity: "mythic",
    category: "special",
    quantity: 1,
    obtained_at: "2026-04-01T00:00:00Z",
    quest_id: "Q-EPIC",
  },
  {
    id: "i6",
    name: "Ghi Chú Rách",
    description: "Một mảnh giấy vô dụng. Có thể bán lấy 5 Gold.",
    icon_name: "📜",
    rarity: "common",
    category: "junk",
    quantity: 12,
    obtained_at: "2026-05-21T09:00:00Z",
    quest_id: null,
  },
];

const CATEGORIES = ["all", "consumable", "potion", "book", "material", "special", "junk"];

export default function InventoryPage() {
  const [selectedItem, setSelectedItem] = useState<InventoryItem | null>(null);
  const [filter, setFilter] = useState("all");

  const filteredItems = filter === "all" 
    ? DEMO_INVENTORY 
    : DEMO_INVENTORY.filter(item => item.category === filter);

  // Fill empty slots to make the grid look like an RPG inventory
  const totalSlots = 35; // 7x5 grid
  const emptySlots = Math.max(0, totalSlots - filteredItems.length);

  return (
    <div className="inventory-page">
      <div className="inventory-page__header animate-fadeIn">
        <h1 className="font-heading" style={{ fontSize: 'var(--text-2xl)', color: 'var(--system-blue)' }}>
          🎒 INVENTORY
        </h1>
        
        <div className="inventory__filters">
          {CATEGORIES.map(cat => (
            <button
              key={cat}
              className={`inventory__filter-btn ${filter === cat ? 'inventory__filter-btn--active' : ''}`}
              onClick={() => setFilter(cat)}
            >
              {cat.toUpperCase()}
            </button>
          ))}
        </div>
      </div>

      <div className="inventory-page__content">
        {/* ── Grid ── */}
        <div className="inventory__grid game-card animate-slideInUp">
          {filteredItems.map((item, index) => (
            <div 
              key={item.id}
              className={`inventory__slot ${RARITY_INFO[item.rarity].cssClass} ${selectedItem?.id === item.id ? 'inventory__slot--selected' : ''}`}
              onClick={() => setSelectedItem(item)}
              style={{ animationDelay: `${index * 0.05}s` }}
            >
              <span className="inventory__slot-icon">{item.icon_name}</span>
              {item.quantity > 1 && (
                <span className="inventory__slot-qty font-mono">{item.quantity}</span>
              )}
            </div>
          ))}
          
          {/* Empty Slots */}
          {[...Array(emptySlots)].map((_, i) => (
            <div key={`empty-${i}`} className="inventory__slot inventory__slot--empty" />
          ))}
        </div>

        {/* ── Detail Panel ── */}
        <div className="inventory__detail animate-slideInRight">
          {selectedItem ? (
            <div className={`game-card game-card--${selectedItem.rarity === 'mythic' ? 'red' : selectedItem.rarity === 'legendary' ? 'gold' : selectedItem.rarity === 'epic' ? 'purple' : 'blue'}`} style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
              
              <div className="inventory-detail__header">
                <div className={`inventory-detail__icon ${RARITY_INFO[selectedItem.rarity].cssClass}`}>
                  {selectedItem.icon_name}
                </div>
                <div>
                  <h2 className="font-heading" style={{ fontSize: 'var(--text-lg)', color: 'var(--text-primary)', marginBottom: '4px' }}>
                    {selectedItem.name}
                  </h2>
                  <span 
                    className="inventory-detail__rarity font-heading"
                    style={{ color: RARITY_INFO[selectedItem.rarity].color }}
                  >
                    {RARITY_INFO[selectedItem.rarity].label}
                  </span>
                </div>
              </div>

              <div className="inventory-detail__meta font-mono">
                <span>Số lượng: {selectedItem.quantity}</span>
                <span>Loại: {selectedItem.category}</span>
              </div>

              <div className="inventory-detail__desc">
                {selectedItem.description}
              </div>

              <div className="inventory-detail__actions">
                <button className="btn btn-primary" style={{ width: '100%' }}>
                  SỬ DỤNG
                </button>
                <button className="btn btn-secondary" style={{ width: '100%' }}>
                  BỎ ĐI
                </button>
              </div>
            </div>
          ) : (
            <div className="game-card" style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
              Chọn một vật phẩm
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
