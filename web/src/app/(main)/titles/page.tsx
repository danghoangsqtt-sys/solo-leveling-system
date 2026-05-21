"use client";

import type { Title } from "@/lib/types";
import { RARITY_INFO } from "@/lib/types";
import "./titles.css";

// ── Demo Titles Data ──
const DEMO_TITLES: Title[] = [
  {
    id: "t1",
    name: "Kẻ Nỗ Lực Không Ngừng",
    name_en: "The Relentless Worker",
    description: "Hoàn thành 30 nhiệm vụ hàng ngày liên tiếp không bỏ sót.",
    condition: "Hoàn thành 30/30 quests",
    rarity: "epic",
    icon_emoji: "🔥",
    is_unlocked: true,
    unlocked_at: "2026-05-15T00:00:00Z",
    category: "grind",
  },
  {
    id: "t2",
    name: "Sói Độc Hành",
    name_en: "Lone Wolf",
    description: "Tự mình vượt qua 5 nhiệm vụ Boss (Hạng B trở lên) trong tháng.",
    condition: "Đã diệt 5/5 Bosses",
    rarity: "legendary",
    icon_emoji: "🐺",
    is_unlocked: true,
    unlocked_at: "2026-05-01T00:00:00Z",
    category: "combat",
  },
  {
    id: "t3",
    name: "Chúa Tể Bóng Tối",
    name_en: "Shadow Monarch",
    description: "Mở khóa toàn bộ các kỹ năng thuộc nhánh Sát Thủ.",
    condition: "Mở khóa 8/8 kỹ năng",
    rarity: "mythic",
    icon_emoji: "👑",
    is_unlocked: false,
    unlocked_at: null,
    category: "skill",
  },
  {
    id: "t4",
    name: "Kẻ Gom Góp",
    name_en: "The Hoarder",
    description: "Tích lũy được 100,000 Gold trong tài khoản.",
    condition: "Tiến độ: 2,350 / 100,000",
    rarity: "rare",
    icon_emoji: "💰",
    is_unlocked: false,
    unlocked_at: null,
    category: "finance",
  },
  {
    id: "t5",
    name: "Con Sâu Sách",
    name_en: "Bookworm",
    description: "Hoàn thành đọc 5 cuốn sách PDF trong Library.",
    condition: "Tiến độ: 1 / 5",
    rarity: "uncommon",
    icon_emoji: "📚",
    is_unlocked: false,
    unlocked_at: null,
    category: "study",
  },
];

export default function TitlesPage() {
  const equippedTitleId = "t1"; // Hardcoded for demo

  return (
    <div className="titles-page">
      <div className="titles-page__header animate-fadeIn">
        <h1 className="font-heading" style={{ fontSize: 'var(--text-2xl)', color: 'var(--system-blue)' }}>
          🏆 DANH HIỆU
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>Thu thập danh hiệu để chứng tỏ sức mạnh</p>
      </div>

      <div className="titles__list">
        {DEMO_TITLES.map((title, index) => {
          const isEquipped = title.id === equippedTitleId;
          const rarityCss = RARITY_INFO[title.rarity].cssClass;
          
          return (
            <div 
              key={title.id} 
              className={`title-card game-card ${title.is_unlocked ? rarityCss : 'title-card--locked'} animate-slideInUp`}
              style={{ animationDelay: `${index * 0.1}s` }}
            >
              <div className="title-card__left">
                <div className={`title-card__icon ${title.is_unlocked ? '' : 'grayscale'}`}>
                  {title.icon_emoji}
                </div>
              </div>

              <div className="title-card__body">
                <div className="title-card__header">
                  <h3 className="title-card__name font-fantasy">
                    {title.name}
                  </h3>
                  {title.is_unlocked && (
                    <span 
                      className="title-card__rarity font-heading"
                      style={{ color: RARITY_INFO[title.rarity].color }}
                    >
                      {RARITY_INFO[title.rarity].label}
                    </span>
                  )}
                </div>

                {title.name_en && (
                  <div className="title-card__en font-heading">{title.name_en}</div>
                )}

                <p className="title-card__desc">{title.description}</p>
                
                <div className="title-card__condition font-mono">
                  {title.is_unlocked ? (
                    <span className="text-success">✓ Đã mở khóa ({new Date(title.unlocked_at!).toLocaleDateString('vi-VN')})</span>
                  ) : (
                    <span className="text-muted">Đang tiến hành: {title.condition}</span>
                  )}
                </div>
              </div>

              <div className="title-card__actions">
                {title.is_unlocked ? (
                  isEquipped ? (
                    <button className="btn btn-secondary btn-sm" disabled>
                      ĐANG TRANG BỊ
                    </button>
                  ) : (
                    <button className="btn btn-primary btn-sm">
                      TRANG BỊ
                    </button>
                  )
                ) : (
                  <div className="title-card__lock">🔒</div>
                )}
              </div>

              {/* Shimmer effect for high rarity */}
              {(title.rarity === 'legendary' || title.rarity === 'mythic') && title.is_unlocked && (
                <div className="title-card__shimmer" />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
