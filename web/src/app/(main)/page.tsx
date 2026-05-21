"use client";

import { STATS, CLASS_ICONS } from "@/lib/types";
import type { User, StatKey } from "@/lib/types";
import "./home.css";

// ── Demo data (will be replaced with Supabase) ──
const DEMO_USER: User = {
  id: "1",
  nickname: "Shadow Monarch",
  avatar_path: null,
  class_name: "Alchemist",
  level: 15,
  current_exp: 8450,
  exp_to_next_level: 10000,
  gold: 2350,
  gems: 15,
  equipped_title_id: "t1",
  str: 67,
  int_stat: 83,
  agi: 42,
  vit: 58,
  wis: 71,
  cha: 49,
  debt_points: 0,
  streak_days: 12,
  total_quests_completed: 89,
  created_at: "",
  updated_at: "",
};

const STAT_CHANGES: Record<StatKey, number> = {
  str: 3,
  int_stat: 5,
  agi: 1,
  vit: 2,
  wis: 4,
  cha: 0,
};

export default function HomePage() {
  const user = DEMO_USER;
  const expPercent = (user.current_exp / user.exp_to_next_level) * 100;
  const questsDone = 3;
  const questsTotal = 8;
  const questPercent = (questsDone / questsTotal) * 100;

  return (
    <div className="home">
      {/* ── Page Header ── */}
      <div className="home__header animate-fadeIn">
        <h1 className="home__title font-heading">
          <span className="home__title-icon">⚔️</span>
          The System — Status
          <span className="home__title-icon">⚔️</span>
        </h1>
      </div>

      {/* ── Character Card ── */}
      <section className="home__character game-card animate-slideInUp" style={{ animationDelay: '0.1s' }}>
        <div className="home__character-left">
          <div className="home__avatar">
            <div className="home__avatar-glow" />
            <div className="home__avatar-ring" />
            <span className="home__avatar-emoji">
              {CLASS_ICONS[user.class_name]}
            </span>
          </div>
        </div>
        <div className="home__character-right">
          <h2 className="home__name font-heading">{user.nickname}</h2>
          <div className="home__class">
            Class: {CLASS_ICONS[user.class_name]} {user.class_name}
          </div>
          <div className="home__level">
            <span className="home__level-badge glow-text--gold">
              Lv. {user.level}
            </span>
          </div>
          <div className="home__title-equipped font-fantasy">
            &ldquo;Kẻ Nỗ Lực Không Ngừng&rdquo;
          </div>
        </div>
      </section>

      {/* ── Stats ── */}
      <section className="home__stats game-card animate-slideInUp" style={{ animationDelay: '0.2s' }}>
        <h3 className="home__section-title font-heading">
          ═══ Attributes ═══
        </h3>
        <div className="home__stats-list">
          {STATS.map((stat) => {
            const value = user[stat.key];
            const change = STAT_CHANGES[stat.key];
            return (
              <div key={stat.key} className="stat-bar">
                <span
                  className="stat-bar__label"
                  style={{ color: stat.color }}
                >
                  {stat.shortLabel}
                </span>
                <div className="stat-bar__track">
                  <div
                    className={`stat-bar__fill progress-bar--${stat.cssClass} progress-bar__fill`}
                    style={{ width: `${value}%` }}
                  />
                </div>
                <span className="stat-bar__value font-mono">{value}</span>
                <span
                  className={`stat-bar__change ${
                    change > 0
                      ? "stat-bar__change--up"
                      : change < 0
                      ? "stat-bar__change--down"
                      : "stat-bar__change--none"
                  }`}
                >
                  {change > 0 ? `+${change} ↑` : change < 0 ? `${change} ↓` : "─"}
                </span>
              </div>
            );
          })}
        </div>
      </section>

      {/* ── EXP Bar ── */}
      <section className="home__exp game-card animate-slideInUp" style={{ animationDelay: '0.3s' }}>
        <div className="home__exp-header">
          <span className="home__exp-label font-heading">EXP</span>
          <span className="home__exp-value font-mono">
            {user.current_exp.toLocaleString()} / {user.exp_to_next_level.toLocaleString()}
          </span>
        </div>
        <div className="progress-bar progress-bar--exp">
          <div
            className="progress-bar__fill"
            style={{ width: `${expPercent}%` }}
          />
        </div>
      </section>

      {/* ── Quick Stats Row ── */}
      <div className="home__quick-stats animate-slideInUp" style={{ animationDelay: '0.4s' }}>
        <div className="home__quick-stat game-card game-card--gold">
          <span className="home__quick-stat-icon">💰</span>
          <span className="home__quick-stat-value font-mono">
            {user.gold.toLocaleString()}
          </span>
          <span className="home__quick-stat-label">Gold</span>
        </div>
        <div className="home__quick-stat game-card game-card--purple">
          <span className="home__quick-stat-icon">💎</span>
          <span className="home__quick-stat-value font-mono">{user.gems}</span>
          <span className="home__quick-stat-label">Gems</span>
        </div>
        <div className="home__quick-stat game-card">
          <span className="home__quick-stat-icon">🔥</span>
          <span className="home__quick-stat-value font-mono">
            {user.streak_days}
          </span>
          <span className="home__quick-stat-label">Streak</span>
        </div>
        <div
          className={`home__quick-stat game-card ${
            user.debt_points > 0 ? "game-card--red" : ""
          }`}
        >
          <span className="home__quick-stat-icon">⚠️</span>
          <span className="home__quick-stat-value font-mono">
            {user.debt_points}
          </span>
          <span className="home__quick-stat-label">Debt</span>
        </div>
      </div>

      {/* ── Today's Quests ── */}
      <section className="home__quests game-card animate-slideInUp" style={{ animationDelay: '0.5s' }}>
        <div className="home__quests-header">
          <span className="home__quests-icon">📋</span>
          <span className="home__quests-title font-heading">
            Today&apos;s Quests: {questsDone}/{questsTotal} Done
          </span>
        </div>
        <div className="progress-bar">
          <div
            className="progress-bar__fill"
            style={{
              width: `${questPercent}%`,
              background: `linear-gradient(90deg, var(--system-green), var(--system-cyan))`,
            }}
          />
        </div>
        <div className="home__quests-percent font-mono">
          {questPercent.toFixed(1)}%
        </div>
      </section>

      {/* ── Aura NPC Greeting ── */}
      <section className="home__aura game-card animate-slideInUp" style={{ animationDelay: '0.6s' }}>
        <div className="home__aura-character">
          <div className="home__aura-glow" />
          <span className="home__aura-emoji">🌟</span>
        </div>
        <div className="home__aura-bubble">
          <div className="home__aura-name font-heading">Aura</div>
          <p className="home__aura-text">
            Chào buổi chiều, <strong>{user.nickname}</strong>! 
            Bạn còn {questsTotal - questsDone} nhiệm vụ cần hoàn thành hôm nay. 
            Chuỗi combo {user.streak_days} ngày — hãy giữ vững nhé! 🔥
          </p>
        </div>
      </section>
    </div>
  );
}
