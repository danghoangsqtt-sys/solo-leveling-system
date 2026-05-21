# Brainstorm Session: Web App Pivot — Complete Life Management System
Date: 2026-05-21
Status: Scope Locked ✅

## 1. Goal
Chuyển đổi toàn bộ hệ thống Solo Leveling System từ Android Native sang Web App, kết hợp:
- **Aura Assistant** (https://github.com/danghoangsqtt-sys/Aura_assistant) — NPC trợ lý ảo
- **DHEbook** (https://github.com/danghoangsqtt-sys/danghoang-ebook-v2) — Learning & Finance platform
- **Solo Leveling System** — Gamified life management

Biến thành **hệ thống quản lý cuộc sống hoàn chỉnh trong thời đại số**.

## 2. Key Decisions

### Decision 1: Platform Pivot (Android → Web)
- **Rationale:** User wants cross-platform (desktop + mobile) accessibility, easier management
- **Impact:** Full rewrite from Kotlin/Compose to TypeScript/React

### Decision 2: Backend = Supabase
- **Rationale:** All-in-one BaaS (Auth, DB, Storage, Realtime, Edge Functions)
- **Solves:** Background Worker problem via pg_cron (Penalty Engine runs server-side)
- **Alternative considered:** Appwrite (DHEbook's current backend) — Supabase chosen for pg_cron + better PostgreSQL features

### Decision 3: Frontend = Next.js
- **Rationale:** SSR, API routes, file-based routing, Vercel deploy, SEO optimization
- **Alternative considered:** Vite + React SPA — Next.js chosen for better structure and server-side capabilities

### Decision 4: NPC Aura = CSS Animations + Canvas
- **Rationale:** Lightweight, performant, cross-platform. No heavy WebGL needed.
- **Approach:** Multi-layer PNG with CSS breathing/blinking animations

### Decision 5: Kế thừa Logic & UI
- **From Aura Assistant:** NPC chat interface, voice interaction, character visual
- **From DHEbook:** Course tree system, finance tracking, planner/calendar, AI integration (Gemini)
- **From Solo Leveling System:** Complete gamification (Quests, Skills, Inventory, Titles, Penalties, Stats)

## 3. Source Material Analysis

### Aura Assistant (Node.js app)
- Virtual assistant with anime character
- Voice chat + text chat
- Meeting notes automation
- Task automation
- Key assets: aura_npc character design, chat UI patterns

### DHEbook (React + TypeScript + Vite + Appwrite)
- Live at: https://dhebook.io.vn
- Stack: React 19, TypeScript, Vite, TailwindCSS, Appwrite
- Modules: Dashboard, Courses (tree explorer), English (5 skills), Vocab Library, Finance, Planner
- Key patterns: CourseNode tree, SmartMoneyInput, AI integration via Gemini

### Solo Leveling System Spec (1505 lines)
- 22 sections covering complete RPG gamification
- 18 database entities defined
- AI prompt templates for quest/skill/title generation
- Full UI/UX design spec including colors, typography, animations

## 4. Architecture Summary

```
Next.js 15 (App Router) + TypeScript
         ↓
   Supabase (PostgreSQL + Auth + Storage + Realtime)
         ↓
   Edge Functions → Gemini 2.0 Flash AI
         ↓
   pg_cron → Penalty Engine (midnight check)
```

## 5. Open Questions for User
1. Supabase project details (existing or new?)
2. Deployment domain
3. Gemini API key configuration
4. Aura character asset files
5. DHEbook data migration needs

## Phases

### Phase 1: Foundation & Auth (Week 1-2)
- Next.js project setup + Supabase configuration
- Design system (CSS variables, colors from spec §18)
- Auth (login/register) + Onboarding flow
- Database schema (18 tables)

### Phase 2: Core RPG + Aura NPC (Week 3-5)
- Home / Status Panel with animated stats
- Quest System with AI generation
- Aura NPC character with breathing animation + chat
- Real-time updates via Supabase Realtime

### Phase 3: RPG Features (Week 6-8)
- Skill Tree (interactive node graph)
- Inventory (MMORPG grid)
- Title Gallery (achievements)
- Penalty System (pg_cron)

### Phase 4: Productivity Tools (Week 9-12)
- Finance (kế thừa DHEbook Finance)
- Learning Library (kế thừa DHEbook Courses)
- Calendar + Planner (kế thừa DHEbook Planner)
- Journal + To-Do List

### Phase 5: AI Enhancement (Week 13-14)
- All Edge Functions for AI features
- Aura intelligence layer
- SmartMoneyInput NLP

### Phase 6: Polish & Deploy (Week 15-16)
- Animations, particles, glassmorphism
- PWA setup
- Vercel deployment
- Responsive testing

## Next Steps
→ `/vp-crystallize` to convert this into actionable task structure
→ Or approve the implementation plan to begin `/vp-auto` execution
