-- ═══════════════════════════════════════════════════════════════════════════
-- SOLO LEVELING SYSTEM V2 - SUPABASE SCHEMA (PostgreSQL)
-- This file contains the schema definitions for all 18 core entities.
-- Run this in your Supabase SQL Editor.
-- ═══════════════════════════════════════════════════════════════════════════

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. users
CREATE TABLE public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    nickname TEXT NOT NULL,
    current_class TEXT DEFAULT 'Chưa rõ',
    level INTEGER DEFAULT 1,
    current_exp INTEGER DEFAULT 0,
    total_sp INTEGER DEFAULT 0,
    gold INTEGER DEFAULT 0,
    gems INTEGER DEFAULT 0,
    debt_points INTEGER DEFAULT 0,
    streak_days INTEGER DEFAULT 0,
    last_login_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    aura_mood TEXT DEFAULT 'idle',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. stats
CREATE TABLE public.stats (
    user_id UUID PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE,
    str INTEGER DEFAULT 10,
    int INTEGER DEFAULT 10,
    agi INTEGER DEFAULT 10,
    vit INTEGER DEFAULT 10,
    sen INTEGER DEFAULT 10,
    chr INTEGER DEFAULT 10,
    available_stat_points INTEGER DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. goals
CREATE TABLE public.goals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    target_date TIMESTAMP WITH TIME ZONE,
    priority TEXT CHECK (priority IN ('low', 'medium', 'high', 'epic')) DEFAULT 'medium',
    status TEXT CHECK (status IN ('active', 'completed', 'failed', 'paused')) DEFAULT 'active',
    progress_percent INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 4. skills
CREATE TABLE public.skills (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    goal_id UUID REFERENCES public.goals(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    description TEXT,
    icon_name TEXT,
    current_sp INTEGER DEFAULT 0,
    current_level INTEGER DEFAULT 1,
    parent_skill_id UUID REFERENCES public.skills(id) ON DELETE SET NULL,
    is_unlocked BOOLEAN DEFAULT FALSE,
    order_index INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 5. quests
CREATE TABLE public.quests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    goal_id UUID REFERENCES public.goals(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    description TEXT,
    rank TEXT CHECK (rank IN ('E', 'D', 'C', 'B', 'A', 'S')) NOT NULL,
    type TEXT CHECK (type IN ('daily', 'weekly', 'epic', 'hidden')) NOT NULL,
    category TEXT CHECK (category IN ('study', 'work', 'fitness', 'finance', 'social', 'custom')),
    status TEXT CHECK (status IN ('pending', 'in_progress', 'completed', 'failed')) DEFAULT 'pending',
    exp_reward INTEGER DEFAULT 0,
    gold_reward INTEGER DEFAULT 0,
    gem_reward INTEGER DEFAULT 0,
    stat_reward_type TEXT,
    stat_reward_amount INTEGER DEFAULT 0,
    sp_reward_amount INTEGER DEFAULT 0,
    due_date TIMESTAMP WITH TIME ZONE,
    penalty_debt_points INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. items
CREATE TABLE public.items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    icon_name TEXT,
    rarity TEXT CHECK (rarity IN ('common', 'uncommon', 'rare', 'epic', 'legendary', 'mythic')) NOT NULL,
    category TEXT CHECK (category IN ('consumable', 'potion', 'material', 'equipment', 'special', 'junk')) NOT NULL,
    quantity INTEGER DEFAULT 1,
    obtained_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    quest_id UUID REFERENCES public.quests(id) ON DELETE SET NULL
);

-- 7. titles
CREATE TABLE public.titles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    name_en TEXT,
    description TEXT,
    condition TEXT,
    rarity TEXT CHECK (rarity IN ('common', 'uncommon', 'rare', 'epic', 'legendary', 'mythic')) NOT NULL,
    icon_emoji TEXT,
    category TEXT,
    is_unlocked BOOLEAN DEFAULT FALSE,
    unlocked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 8. equipped_titles
CREATE TABLE public.equipped_titles (
    user_id UUID PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE,
    title_id UUID NOT NULL REFERENCES public.titles(id) ON DELETE CASCADE,
    equipped_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 9. penalty_debts
CREATE TABLE public.penalty_debts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    quest_id UUID REFERENCES public.quests(id) ON DELETE SET NULL,
    reason TEXT NOT NULL,
    debt_amount INTEGER NOT NULL,
    is_cleared BOOLEAN DEFAULT FALSE,
    cleared_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 10. penalty_quests
CREATE TABLE public.penalty_quests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    type TEXT CHECK (type IN ('physical', 'financial', 'time_lock')) NOT NULL,
    debt_points_cleared INTEGER NOT NULL,
    status TEXT CHECK (status IN ('pending', 'completed')) DEFAULT 'pending',
    time_limit_hours INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

-- 11. aura_messages
CREATE TABLE public.aura_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    role TEXT CHECK (role IN ('user', 'assistant', 'system')) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 12. journals
CREATE TABLE public.journals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    content TEXT NOT NULL,
    mood_score INTEGER CHECK (mood_score BETWEEN 1 AND 10),
    tags TEXT[],
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 13. financial_transactions
CREATE TABLE public.financial_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    amount NUMERIC NOT NULL,
    type TEXT CHECK (type IN ('income', 'expense')) NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    transaction_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 14. budgets
CREATE TABLE public.budgets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    category TEXT NOT NULL,
    amount_limit NUMERIC NOT NULL,
    period TEXT CHECK (period IN ('daily', 'weekly', 'monthly')) DEFAULT 'monthly',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 15. courses
CREATE TABLE public.courses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    provider TEXT,
    url TEXT,
    total_modules INTEGER,
    completed_modules INTEGER DEFAULT 0,
    status TEXT CHECK (status IN ('planned', 'in_progress', 'completed', 'dropped')) DEFAULT 'planned',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 16. books
CREATE TABLE public.books (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    author TEXT,
    total_pages INTEGER,
    read_pages INTEGER DEFAULT 0,
    cover_url TEXT,
    file_path TEXT,
    status TEXT CHECK (status IN ('want_to_read', 'reading', 'completed', 'dropped')) DEFAULT 'want_to_read',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 17. pomodoro_sessions
CREATE TABLE public.pomodoro_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    task_id UUID, -- Can refer to Quest or Goal
    duration_minutes INTEGER NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ended_at TIMESTAMP WITH TIME ZONE,
    was_completed BOOLEAN DEFAULT FALSE
);

-- 18. sync_logs
CREATE TABLE public.sync_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    sync_type TEXT NOT NULL,
    status TEXT CHECK (status IN ('success', 'failed')) NOT NULL,
    error_message TEXT,
    synced_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ── Enable Row Level Security (RLS) ──
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.stats ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.goals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.skills ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.quests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.titles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.equipped_titles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.penalty_debts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.penalty_quests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.aura_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.journals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.financial_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.budgets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.courses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.books ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pomodoro_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sync_logs ENABLE ROW LEVEL SECURITY;

-- ── RLS Policies (Users can only see and modify their own data) ──

-- users
CREATE POLICY "Users can view own data" ON public.users FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users can update own data" ON public.users FOR UPDATE USING (auth.uid() = id);

-- Function to create basic RLS policies for user_id based tables
CREATE OR REPLACE FUNCTION create_user_policies(table_name text) RETURNS void AS $$
BEGIN
    EXECUTE format('CREATE POLICY "Users can view own %I" ON public.%I FOR SELECT USING (auth.uid() = user_id)', table_name, table_name);
    EXECUTE format('CREATE POLICY "Users can insert own %I" ON public.%I FOR INSERT WITH CHECK (auth.uid() = user_id)', table_name, table_name);
    EXECUTE format('CREATE POLICY "Users can update own %I" ON public.%I FOR UPDATE USING (auth.uid() = user_id)', table_name, table_name);
    EXECUTE format('CREATE POLICY "Users can delete own %I" ON public.%I FOR DELETE USING (auth.uid() = user_id)', table_name, table_name);
END;
$$ LANGUAGE plpgsql;

-- Apply to all user_id tables
SELECT create_user_policies('stats');
SELECT create_user_policies('goals');
SELECT create_user_policies('skills');
SELECT create_user_policies('quests');
SELECT create_user_policies('items');
SELECT create_user_policies('titles');
SELECT create_user_policies('equipped_titles');
SELECT create_user_policies('penalty_debts');
SELECT create_user_policies('penalty_quests');
SELECT create_user_policies('aura_messages');
SELECT create_user_policies('journals');
SELECT create_user_policies('financial_transactions');
SELECT create_user_policies('budgets');
SELECT create_user_policies('courses');
SELECT create_user_policies('books');
SELECT create_user_policies('pomodoro_sessions');
SELECT create_user_policies('sync_logs');

-- Drop helper function
DROP FUNCTION create_user_policies(text);
