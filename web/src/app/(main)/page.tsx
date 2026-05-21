"use client";

import type { User } from "@/lib/types";
import "./home.css";

// ── Demo data (will be replaced with Supabase) ──
const DEMO_USER: User = {
  id: "1",
  nickname: "Vua Bóng Tối",
  avatar_path: null,
  class_name: "Alchemist",
  level: 42,
  current_exp: 8450,
  exp_to_next_level: 10000,
  gold: 2350,
  gems: 15,
  equipped_title_id: "t1",
  str: 142,
  int_stat: 205,
  agi: 98,
  vit: 160,
  wis: 185,
  cha: 65,
  debt_points: 0,
  streak_days: 12,
  total_quests_completed: 89,
  created_at: "",
  updated_at: "",
};

const STATS = [
  { key: "str", label: "SỨC MẠNH (STR)", icon: "💪", baseColor: "#FF4757" },
  { key: "int_stat", label: "TRÍ LỰC (INT)", icon: "🧠", baseColor: "#4A9EFF" },
  { key: "agi", label: "NHANH NHẸN (AGI)", icon: "⚡", baseColor: "#2ED573" },
  { key: "vit", label: "THỂ LỰC (VIT)", icon: "❤️", baseColor: "#FF9F43" },
  { key: "wis", label: "THÔNG THÁI (WIS)", icon: "📖", baseColor: "#B266FF" },
  { key: "cha", label: "MỊ LỰC (CHA)", icon: "✨", baseColor: "#FF6B81" },
] as const;

const STAT_CHANGES: Record<string, number> = {
  str: 12,
  int_stat: 45,
  agi: 5,
  vit: 20,
  wis: 30,
  cha: 0,
};

export default function HomePage() {
  const user = DEMO_USER;
  const expPercent = (user.current_exp / user.exp_to_next_level) * 100;
  
  const questsDone = 8;
  const questsTotal = 10;
  const questPercent = (questsDone / questsTotal) * 100;

  return (
    <div className="home">
      {/* ── LEFT PANE (Character & Core Info) ── */}
      <section className="home__left-pane">
        {/* Player Nameplate */}
        <div className="game-card home__character-card animate-slideInUp">
          <span className="home__title-badge shimmer-wrapper">
            ⭐ Chiến Binh Nhàn Hạ
          </span>
          <h2 className="home__name">{user.nickname}</h2>
          <p className="home__class">Class: {user.class_name}</p>
        </div>

        {/* Central NPC Display */}
        <div className="home__npc-display animate-fadeIn" style={{ animationDelay: '0.2s' }}>
          <div className="home__npc-orbit-1" />
          <div className="home__npc-orbit-2" />
          <div className="home__npc-glow" />
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img 
            className="home__npc-image" 
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuChmP0ou5ba51c6SQUPAKv2TAAnBTmFQjJdb2ttEQtcGrL34sufFHFYNCQWeBYfj5AQK0HN0QyVWpb_Yqe0sC45RVLjEWimBB-MAY3QOvzrzZbk6Ajp7_BSTBAkJPY8h6fMXTE8LVNKRDR_EgvJgFVIV_gvjqjcLz2EPTrIPWorxf41J9-o26hUomkUAOe1acEL9zBRXSaTagixM6Ytn_GmAiIwFzD3bVBKTl5LDP9r8wdL_WCGvZKZ_1htWhDFukzCc4QI6XVgPQ" 
            alt="Aura NPC" 
          />
        </div>

        {/* EXP & Currency Block */}
        <div className="game-card home__status-block animate-slideInUp" style={{ animationDelay: '0.4s' }}>
          <div>
            <div className="home__exp-header">
              <span className="home__exp-label">KINH NGHIỆM</span>
              <span className="home__exp-value">
                {user.current_exp.toLocaleString()} / {user.exp_to_next_level.toLocaleString()}
              </span>
            </div>
            <div className="stat-bar-track shimmer-wrapper">
              <div 
                className="stat-bar-fill exp-bar-fill" 
                style={{ width: `${expPercent}%` }} 
              />
            </div>
          </div>
          <div className="home__currency-row">
            <div className="currency-card">
              <span className="currency-card__icon currency-card__icon--gold">💰</span>
              <div className="currency-card__info">
                <span className="currency-card__label">VÀNG</span>
                <span className="currency-card__value">{user.gold.toLocaleString()}</span>
              </div>
            </div>
            <div className="currency-card">
              <span className="currency-card__icon currency-card__icon--gem">💎</span>
              <div className="currency-card__info">
                <span className="currency-card__label">NGỌC</span>
                <span className="currency-card__value">{user.gems}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* ── RIGHT PANE (Attributes & Quests) ── */}
      <section className="home__right-pane">
        {/* Attributes Panel */}
        <div className="game-card home__attributes-card animate-slideInUp" style={{ animationDelay: '0.3s' }}>
          <div className="home__attributes-header">
            <span className="home__attributes-icon">📊</span>
            <h3 className="home__attributes-title">Chỉ Số Cơ Bản</h3>
          </div>
          <div className="home__attributes-grid">
            {STATS.map((stat) => {
              const value = user[stat.key as keyof typeof user] as number;
              const bonus = STAT_CHANGES[stat.key] || 0;
              const percent = Math.min((value / 300) * 100, 100); // 300 as arbitrary max for bar

              return (
                <div key={stat.key} className="stat-item">
                  <div className="stat-item__header">
                    <span className="stat-item__label">
                      {stat.icon} {stat.label}
                    </span>
                    <div className="stat-item__values">
                      <span className="stat-item__value">{value}</span>
                      <span className="stat-item__bonus">+{bonus}</span>
                    </div>
                  </div>
                  <div className="stat-bar-track">
                    <div 
                      className="stat-bar-fill" 
                      style={{ width: `${percent}%` }} 
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Today's Quest Mini-Card */}
        <div className="game-card home__quest-mini animate-slideInUp" style={{ animationDelay: '0.5s' }}>
          <div className="home__quest-header">
            <div>
              <span className="home__quest-label">NHIỆM VỤ HÀNG NGÀY</span>
              <h4 className="home__quest-title">Thu thập tinh thể Mana</h4>
            </div>
            <div className="home__quest-icon">📋</div>
          </div>
          <div className="home__quest-progress">
            <div className="home__quest-progress-header">
              <span>Tiến độ</span>
              <span>{questsDone} / {questsTotal}</span>
            </div>
            <div className="stat-bar-track">
              <div 
                className="home__quest-progress-fill" 
                style={{ width: `${questPercent}%` }} 
              />
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
