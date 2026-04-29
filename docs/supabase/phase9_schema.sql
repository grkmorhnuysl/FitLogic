-- Phase 9 - Supabase schema (MVP)
create table if not exists public.users (
  id text primary key,
  email text,
  display_name text not null default '',
  updated_at bigint not null,
  sync_status text not null default 'SYNCED'
);

create table if not exists public.workouts (
  id text primary key,
  user_id text not null,
  title text not null,
  status text not null,
  total_volume real not null,
  updated_at bigint not null,
  sync_status text not null default 'SYNCED'
);

create table if not exists public.sets (
  id text primary key,
  workout_id text not null,
  user_id text not null,
  weight_kg real not null,
  reps int not null,
  volume real not null,
  is_pr boolean not null default false,
  updated_at bigint not null,
  sync_status text not null default 'SYNCED'
);
