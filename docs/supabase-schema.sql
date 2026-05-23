-- ============================================================
--  Solo Leveling System — Supabase Backend Schema
--  Chạy toàn bộ file này trong Supabase SQL Editor
--  Thứ tự: Step 1 → 2 → 3 → 4 → 5 → 6
-- ============================================================


-- ============================================================
-- STEP 1 — Shared trigger function (dùng chung cho mọi bảng)
-- ============================================================

create or replace function update_updated_at()
returns trigger as $$
begin
  new.updated_at = now();
  return new;
end;
$$ language plpgsql;


-- ============================================================
-- STEP 2 — Bảng game_state (player snapshot)
--
-- Mỗi thiết bị có đúng 1 row, upsert bằng device_id.
-- player_json chứa PlayerSnapshot:
--   { user, stats, skills[], items[] }
-- ============================================================

create table if not exists game_state (
  device_id   text        primary key,
  player_json text        not null,
  updated_at  timestamptz not null default now()
);

-- Tự cập nhật updated_at mỗi khi upsert
drop trigger if exists set_game_state_updated_at on game_state;
create trigger set_game_state_updated_at
before update on game_state
for each row execute function update_updated_at();

-- Tắt RLS (app cá nhân — xác thực bằng device_id + anon key)
alter table game_state disable row level security;


-- ============================================================
-- STEP 3 — Bảng daily_history (lịch sử hàng ngày)
--
-- Mỗi row = 1 ngày của 1 thiết bị.
-- Khóa chính: (device_id, date_key) — idempotent upsert.
-- summary_json chứa DailySummaryHistoryItem:
--   { id, date, totalQuests, completedQuests, failedQuests,
--     completionRate, expEarned, goldEarned, itemsDropped,
--     statChanges, skillProgress, debtPointsGained,
--     currentDebtTotal, currentStreak, aiJournalContent,
--     userNotes, tomorrowPlan, generatedAt }
-- ============================================================

create table if not exists daily_history (
  device_id    text        not null,
  date_key     text        not null,   -- format: "YYYY-MM-DD"
  summary_json text        not null,
  synced_at    timestamptz not null default now(),
  primary key (device_id, date_key)
);

-- Tự cập nhật synced_at khi re-push
drop trigger if exists set_daily_history_synced_at on daily_history;
create trigger set_daily_history_synced_at
before update on daily_history
for each row execute function update_updated_at();

-- Tắt RLS
alter table daily_history disable row level security;

-- Index để query nhanh theo device + khoảng ngày
create index if not exists idx_daily_history_device_date
  on daily_history (device_id, date_key desc);


-- ============================================================
-- STEP 4 — Helper functions (trích xuất field từ JSON text)
--
-- player_json và summary_json được lưu dưới dạng TEXT chứa JSON.
-- Dùng ::jsonb để truy vấn trực tiếp.
-- ============================================================

-- Lấy level từ game_state
create or replace function get_player_level(p_device_id text)
returns int as $$
  select (player_json::jsonb -> 'user' ->> 'level')::int
  from game_state
  where device_id = p_device_id;
$$ language sql stable;

-- Lấy streak hiện tại từ game_state
create or replace function get_player_streak(p_device_id text)
returns int as $$
  select (player_json::jsonb -> 'user' ->> 'streak')::int
  from game_state
  where device_id = p_device_id;
$$ language sql stable;

-- Lấy N ngày gần nhất dưới dạng JSON objects (không phải text)
create or replace function get_recent_days(p_device_id text, p_days int default 7)
returns table (
  date_key     text,
  synced_at    timestamptz,
  summary      jsonb
) as $$
  select
    date_key,
    synced_at,
    summary_json::jsonb as summary
  from daily_history
  where device_id = p_device_id
  order by date_key desc
  limit p_days;
$$ language sql stable;


-- ============================================================
-- STEP 5 — Views tổng hợp (weekly / monthly / yearly)
--
-- Truy vấn: SELECT * FROM weekly_summary WHERE device_id = '...'
-- ============================================================

-- Tổng hợp theo tuần
create or replace view weekly_summary as
select
  device_id,
  date_trunc('week', date_key::date)::date                              as week_start,
  (date_trunc('week', date_key::date) + interval '6 days')::date        as week_end,
  count(*)                                                               as days_logged,
  round(avg((summary_json::jsonb ->> 'completionRate')::float)::numeric, 3) as avg_completion_rate,
  sum((summary_json::jsonb ->> 'expEarned')::int)                        as total_exp,
  sum((summary_json::jsonb ->> 'goldEarned')::int)                       as total_gold,
  sum((summary_json::jsonb ->> 'itemsDropped')::int)                     as total_items_dropped,
  sum((summary_json::jsonb ->> 'completedQuests')::int)                  as total_quests_completed,
  sum((summary_json::jsonb ->> 'failedQuests')::int)                     as total_quests_failed,
  sum((summary_json::jsonb ->> 'debtPointsGained')::int)                 as total_debt_gained,
  max((summary_json::jsonb ->> 'currentStreak')::int)                    as peak_streak
from daily_history
group by device_id, date_trunc('week', date_key::date);

-- Tổng hợp theo tháng
create or replace view monthly_summary as
select
  device_id,
  date_trunc('month', date_key::date)::date                             as month_start,
  to_char(date_key::date, 'YYYY-MM')                                    as month_label,
  count(*)                                                               as days_logged,
  round(avg((summary_json::jsonb ->> 'completionRate')::float)::numeric, 3) as avg_completion_rate,
  sum((summary_json::jsonb ->> 'expEarned')::int)                        as total_exp,
  sum((summary_json::jsonb ->> 'goldEarned')::int)                       as total_gold,
  sum((summary_json::jsonb ->> 'itemsDropped')::int)                     as total_items_dropped,
  sum((summary_json::jsonb ->> 'completedQuests')::int)                  as total_quests_completed,
  sum((summary_json::jsonb ->> 'failedQuests')::int)                     as total_quests_failed,
  sum((summary_json::jsonb ->> 'debtPointsGained')::int)                 as total_debt_gained,
  max((summary_json::jsonb ->> 'currentStreak')::int)                    as peak_streak
from daily_history
group by device_id, date_trunc('month', date_key::date), to_char(date_key::date, 'YYYY-MM');

-- Tổng hợp theo năm
create or replace view yearly_summary as
select
  device_id,
  extract(year from date_key::date)::int                                as year,
  count(*)                                                               as days_logged,
  round(avg((summary_json::jsonb ->> 'completionRate')::float)::numeric, 3) as avg_completion_rate,
  sum((summary_json::jsonb ->> 'expEarned')::int)                        as total_exp,
  sum((summary_json::jsonb ->> 'goldEarned')::int)                       as total_gold,
  sum((summary_json::jsonb ->> 'itemsDropped')::int)                     as total_items_dropped,
  sum((summary_json::jsonb ->> 'completedQuests')::int)                  as total_quests_completed,
  sum((summary_json::jsonb ->> 'failedQuests')::int)                     as total_quests_failed,
  max((summary_json::jsonb ->> 'currentStreak')::int)                    as peak_streak
from daily_history
group by device_id, extract(year from date_key::date);


-- ============================================================
-- STEP 6 — Verify setup
-- Chạy để kiểm tra sau khi tạo xong
-- ============================================================

-- Kiểm tra tables và views đã tồn tại
select table_name, table_type
from information_schema.tables
where table_schema = 'public'
  and table_name in ('game_state', 'daily_history',
                     'weekly_summary', 'monthly_summary', 'yearly_summary')
order by table_name;

-- Kiểm tra triggers
select event_object_table, trigger_name, event_manipulation
from information_schema.triggers
where trigger_schema = 'public'
order by event_object_table;


-- ============================================================
-- QUICK REFERENCE — Các query thường dùng
-- ============================================================

-- Xem snapshot player hiện tại
-- SELECT device_id, updated_at, player_json::jsonb FROM game_state;

-- Xem 7 ngày gần nhất của một thiết bị
-- SELECT * FROM get_recent_days('<device_id>', 7);

-- Xem tổng hợp tuần hiện tại
-- SELECT * FROM weekly_summary WHERE device_id = '<device_id>'
--   AND week_start = date_trunc('week', current_date)::date;

-- Xem tổng hợp tháng hiện tại
-- SELECT * FROM monthly_summary WHERE device_id = '<device_id>'
--   AND month_label = to_char(current_date, 'YYYY-MM');

-- Xem tổng hợp năm
-- SELECT * FROM yearly_summary WHERE device_id = '<device_id>';

-- Xem streak dài nhất trong lịch sử
-- SELECT max((summary_json::jsonb->>'currentStreak')::int) as all_time_streak
-- FROM daily_history WHERE device_id = '<device_id>';

-- Xem completion rate theo từng ngày trong 30 ngày qua
-- SELECT date_key,
--        (summary_json::jsonb->>'completionRate')::float  as completion_rate,
--        (summary_json::jsonb->>'expEarned')::int          as exp,
--        (summary_json::jsonb->>'currentStreak')::int      as streak
-- FROM daily_history
-- WHERE device_id = '<device_id>'
--   AND date_key >= to_char(current_date - interval '30 days', 'YYYY-MM-DD')
-- ORDER BY date_key;
