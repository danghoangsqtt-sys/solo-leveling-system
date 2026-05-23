# Supabase Cloud Backup — Setup Guide

This guide sets up a free Supabase project to back up your Solo Leveling System data.
The app stores all data locally in Room; Supabase is a cloud mirror only.

---

## 1. Create a Supabase project

1. Go to [supabase.com](https://supabase.com) and sign in (free tier is enough).
2. Click **New project**, choose a name (e.g. `solo-leveling`), set a database password.
3. Wait ~2 minutes for the project to provision.

---

## 2. Create the `game_state` table

In the Supabase dashboard → **SQL Editor**, run:

```sql
create table if not exists game_state (
  device_id  text primary key,
  player_json text not null,
  updated_at  timestamptz not null default now()
);

-- Auto-update updated_at on every upsert
create or replace function update_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;

create or replace trigger set_updated_at
before update on game_state
for each row execute function update_updated_at();
```

---

## 3. Create the `daily_history` table

This table stores one row per device per day — the end-of-day summary pushed at 22:00.
Used for weekly / monthly / yearly analytics aggregation.

```sql
create table if not exists daily_history (
  device_id    text not null,
  date_key     text not null,          -- "YYYY-MM-DD", e.g. "2026-05-23"
  summary_json text not null,          -- JSON blob of DailySummaryHistoryItem
  synced_at    timestamptz not null default now(),
  primary key (device_id, date_key)
);

-- Auto-update synced_at on re-push
create or replace trigger set_history_synced_at
before update on daily_history
for each row execute function update_updated_at();   -- reuses the function from game_state

-- Handy views for aggregation
create or replace view weekly_summary as
select
  device_id,
  date_trunc('week', date_key::date) as week_start,
  count(*)                            as days_logged,
  avg((summary_json::jsonb->>'completionRate')::float) as avg_completion,
  sum((summary_json::jsonb->>'expEarned')::int)        as total_exp,
  sum((summary_json::jsonb->>'goldEarned')::int)       as total_gold,
  max((summary_json::jsonb->>'currentStreak')::int)    as peak_streak
from daily_history
group by device_id, week_start;

create or replace view monthly_summary as
select
  device_id,
  date_trunc('month', date_key::date) as month_start,
  count(*)                             as days_logged,
  avg((summary_json::jsonb->>'completionRate')::float) as avg_completion,
  sum((summary_json::jsonb->>'expEarned')::int)        as total_exp,
  sum((summary_json::jsonb->>'goldEarned')::int)       as total_gold,
  max((summary_json::jsonb->>'currentStreak')::int)    as peak_streak
from daily_history
group by device_id, month_start;
```

---

## 4. Enable Row Level Security (RLS)

Since the app identifies itself by a device UUID (no login), disable RLS or use an open policy.
For a personal app this is fine — the anon key is kept on-device only.

```sql
-- Option A: disable RLS entirely (simplest for personal use)
alter table game_state disable row level security;

-- Option B: open policy via anon key (slightly more explicit)
-- alter table game_state enable row level security;
-- create policy "anon full access" on game_state
--   for all using (true) with check (true);
```

---

## 5. Get your URL and anon key

In the Supabase dashboard → **Project Settings → API**:

| Field | Where to find |
|-------|---------------|
| **Project URL** | `https://<your-project-ref>.supabase.co` |
| **anon / public key** | Under "Project API keys" → `anon public` |

---

## 6. Configure in the app

Open the app → **Settings screen** → scroll to **Cloud Backup**:

- **Supabase URL**: paste the Project URL
- **Anon Key**: paste the anon public key
- Tap **Save**

The app will immediately attempt a cloud push. On a fresh install or after clearing data,
the app will automatically pull your saved state from Supabase on first launch.

---

## 7. How sync works

| Event | Action | Table |
|-------|--------|-------|
| App first launch (empty DB) | `restoreIfEmpty()` — pulls player snapshot | `game_state` |
| Quest completed (debounced 5s) | `pushToCloud()` — pushes full snapshot | `game_state` |
| Manual trigger | `HomeViewModel.pushToCloud()` | `game_state` |
| **22:00 every day** | `EndOfDayWorker` → `pushDailyHistory()` | **`daily_history`** |
| Catch-up after offline | `pushHistoryBatch(days=7)` — pushes recent summaries | **`daily_history`** |

`game_state`: single row per device (upsert replaces). Contains user + stats + skills + items.

`daily_history`: one row per device per day (`device_id` + `date_key` primary key). Never deleted. Aggregated by Supabase views for weekly / monthly / yearly reports.

---

## 8. Migrate to a new device

1. Install the app on the new device.
2. Go to Settings → Cloud Backup → enter the same Supabase URL and anon key.
3. Force-close and reopen the app — `restoreIfEmpty()` will pull your data automatically.
