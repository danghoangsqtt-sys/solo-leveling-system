"use client";

import { useState } from "react";
import type { Skill, SkillLevel } from "@/lib/types";
import { SKILL_LEVEL_NAMES, SKILL_LEVEL_SP } from "@/lib/types";
import "./skills.css";

// ── Demo Skill Tree Data ──
const DEMO_SKILLS: Skill[] = [
  {
    id: "s1",
    goal_id: "g1",
    name: "Web Development",
    description: "Khả năng xây dựng các ứng dụng web hiện đại và tối ưu.",
    icon_name: "🌐",
    current_sp: 1250,
    current_level: 4,
    level_name: SKILL_LEVEL_NAMES[4].vi,
    parent_skill_id: null,
    is_unlocked: true,
    order_index: 0,
  },
  {
    id: "s2",
    goal_id: "g1",
    name: "Frontend Mastery",
    description: "Khai phá sức mạnh của React và Next.js. Trở thành phù thủy UI.",
    icon_name: "🎨",
    current_sp: 450,
    current_level: 2,
    level_name: SKILL_LEVEL_NAMES[2].vi,
    parent_skill_id: "s1",
    is_unlocked: true,
    order_index: 0,
  },
  {
    id: "s3",
    goal_id: "g1",
    name: "Backend Architecture",
    description: "Kiến trúc hệ thống mạnh mẽ, bảo mật và có khả năng mở rộng.",
    icon_name: "⚙️",
    current_sp: 650,
    current_level: 3,
    level_name: SKILL_LEVEL_NAMES[3].vi,
    parent_skill_id: "s1",
    is_unlocked: true,
    order_index: 1,
  },
  {
    id: "s4",
    goal_id: "g1",
    name: "UI/UX Animation",
    description: "Thổi hồn vào giao diện với những chuyển động mượt mà.",
    icon_name: "✨",
    current_sp: 0,
    current_level: 1,
    level_name: SKILL_LEVEL_NAMES[1].vi,
    parent_skill_id: "s2",
    is_unlocked: false,
    order_index: 0,
  },
  {
    id: "s5",
    goal_id: "g1",
    name: "Database Design",
    description: "Thiết kế cơ sở dữ liệu tối ưu cho hàng triệu người dùng.",
    icon_name: "🗄️",
    current_sp: 120,
    current_level: 1,
    level_name: SKILL_LEVEL_NAMES[1].vi,
    parent_skill_id: "s3",
    is_unlocked: true,
    order_index: 0,
  },
];

export default function SkillsPage() {
  const [selectedSkill, setSelectedSkill] = useState<Skill | null>(DEMO_SKILLS[0]);

  // Recursively render skill tree nodes
  const renderSkillNode = (skill: Skill, depth = 0) => {
    const children = DEMO_SKILLS.filter((s) => s.parent_skill_id === skill.id)
      .sort((a, b) => a.order_index - b.order_index);
    
    const isSelected = selectedSkill?.id === skill.id;
    const progress = skill.current_level < 7 
      ? (skill.current_sp / SKILL_LEVEL_SP[skill.current_level]) * 100 
      : 100;

    return (
      <div key={skill.id} className="skill-tree__node-wrapper">
        <div 
          className={`skill-tree__node game-card ${isSelected ? 'skill-tree__node--selected' : ''} ${!skill.is_unlocked ? 'skill-tree__node--locked' : ''}`}
          onClick={() => setSelectedSkill(skill)}
        >
          <div className="skill-tree__node-icon">{skill.icon_name}</div>
          <div className="skill-tree__node-level font-mono">Lv.{skill.current_level}</div>
          
          {skill.is_unlocked && (
            <div className="skill-tree__node-progress">
              <div 
                className="skill-tree__node-progress-fill"
                style={{ width: `${progress}%` }}
              />
            </div>
          )}
          
          {!skill.is_unlocked && (
            <div className="skill-tree__node-lock">🔒</div>
          )}
        </div>
        
        {children.length > 0 && (
          <div className="skill-tree__children">
            {children.map(child => renderSkillNode(child, depth + 1))}
          </div>
        )}
      </div>
    );
  };

  const rootSkills = DEMO_SKILLS.filter((s) => s.parent_skill_id === null);

  return (
    <div className="skills-page">
      <div className="skills-page__header animate-fadeIn">
        <h1 className="font-heading" style={{ fontSize: 'var(--text-2xl)', color: 'var(--system-blue)' }}>
          🌳 SKILL TREE
        </h1>
        <p style={{ color: 'var(--text-secondary)' }}>Tiến hóa thông qua học tập và rèn luyện</p>
      </div>

      <div className="skills-page__content">
        {/* ── Skill Graph Area ── */}
        <div className="skills-page__graph game-card animate-scaleIn">
          <div className="skill-tree">
            {rootSkills.map(root => renderSkillNode(root))}
          </div>
        </div>

        {/* ── Skill Detail Panel ── */}
        <div className="skills-page__detail animate-slideInRight">
          {selectedSkill ? (
            <div className={`game-card ${selectedSkill.is_unlocked ? 'game-card--blue' : ''}`} style={{ height: '100%' }}>
              <div className="skill-detail__header">
                <div className="skill-detail__icon">{selectedSkill.icon_name}</div>
                <div>
                  <h2 className="font-heading" style={{ fontSize: 'var(--text-xl)', color: 'var(--text-primary)' }}>
                    {selectedSkill.name}
                  </h2>
                  <div className="skill-detail__level glow-text--gold font-mono">
                    Level {selectedSkill.current_level} — {selectedSkill.level_name}
                  </div>
                </div>
              </div>

              <p className="skill-detail__desc">
                {selectedSkill.description}
              </p>

              <div className="skill-detail__progress-section">
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 'var(--space-2)' }}>
                  <span className="font-heading" style={{ color: 'var(--text-secondary)' }}>SKILL POINTS</span>
                  <span className="font-mono" style={{ color: 'var(--system-blue)' }}>
                    {selectedSkill.current_sp} / {SKILL_LEVEL_SP[selectedSkill.current_level] || 'MAX'}
                  </span>
                </div>
                
                <div className="progress-bar">
                  <div 
                    className="progress-bar__fill"
                    style={{ 
                      width: `${selectedSkill.current_level < 7 ? (selectedSkill.current_sp / SKILL_LEVEL_SP[selectedSkill.current_level]) * 100 : 100}%`,
                      background: 'linear-gradient(90deg, var(--system-blue), var(--system-cyan))'
                    }}
                  />
                </div>
              </div>

              {!selectedSkill.is_unlocked && (
                <div className="skill-detail__locked-overlay">
                  <span style={{ fontSize: '2rem' }}>🔒</span>
                  <p style={{ marginTop: 'var(--space-2)' }}>Kỹ năng chưa mở khóa</p>
                  <p style={{ fontSize: 'var(--text-xs)', color: 'var(--text-muted)' }}>
                    Yêu cầu kỹ năng tiền quyết đạt Level {selectedSkill.current_level}
                  </p>
                </div>
              )}
            </div>
          ) : (
            <div className="game-card" style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
              Chọn một kỹ năng để xem chi tiết
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
