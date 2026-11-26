package com.example.data.remote

object SupabaseSqlScript {
  val SQL =
    """
-- ============================================================================
-- SUIVICOACH — Complete Supabase PostgreSQL Schema & Row Level Security (RLS)
-- Paste this entire script into the Supabase SQL Editor and click "Run".
-- ============================================================================

-- 1. Enable UUID extension
create extension if not exists "pgcrypto";

-- 2. Coach Profiles Table
create table if not exists public.coach_profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  full_name text not null default '',
  business_name text not null default 'SUIVICOACH Studio',
  phone text not null default '',
  email text not null default '',
  avatar_url text not null default '',
  created_at timestamptz not null default now()
);

-- Migration: Add avatar_url column if it doesn't exist yet
alter table public.coach_profiles add column if not exists avatar_url text not null default '';

-- Auto-create coach profile when a coach signs up
create or replace function public.handle_new_coach_user()
returns trigger
language plpgsql
security definer set search_path = public
as ${'$'}${'$'}
begin
  insert into public.coach_profiles (id, full_name, email)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'full_name', ''),
    coalesce(new.email, '')
  )
  on conflict (id) do nothing;
  return new;
end;
${'$'}${'$'};

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute procedure public.handle_new_coach_user();

-- 3. Clients Table
create table if not exists public.clients (
  id uuid primary key default gen_random_uuid(),
  coach_id uuid not null references auth.users(id) on delete cascade,
  full_name text not null,
  phone text not null default '',
  email text not null default '',
  goal text not null default '',
  notes text not null default '',
  is_archived boolean not null default false,
  created_at timestamptz not null default now()
);

create index if not exists idx_clients_coach_id on public.clients(coach_id);

-- 4. Subscriptions Table
create table if not exists public.subscriptions (
  id uuid primary key default gen_random_uuid(),
  coach_id uuid not null references auth.users(id) on delete cascade,
  client_id uuid not null references public.clients(id) on delete cascade,
  plan_name text not null,
  price_da integer not null default 0,
  duration_days integer not null default 30,
  start_date date not null,
  end_date date not null,
  sessions_total integer null,
  sessions_used integer not null default 0,
  amount_paid_da integer not null default 0,
  notes text not null default '',
  created_at timestamptz not null default now()
);

create index if not exists idx_subscriptions_coach_id on public.subscriptions(coach_id);
create index if not exists idx_subscriptions_client_id on public.subscriptions(client_id);
create index if not exists idx_subscriptions_end_date on public.subscriptions(end_date);

-- 5. Payments Ledger Table
create table if not exists public.payments (
  id uuid primary key default gen_random_uuid(),
  coach_id uuid not null references auth.users(id) on delete cascade,
  client_id uuid not null references public.clients(id) on delete cascade,
  subscription_id uuid null references public.subscriptions(id) on delete set null,
  amount_da integer not null check (amount_da > 0),
  paid_at timestamptz not null default now(),
  method text not null default 'cash' check (method in ('cash', 'baridimob', 'ccp', 'transfer')),
  note text not null default ''
);

create index if not exists idx_payments_coach_id on public.payments(coach_id);
create index if not exists idx_payments_client_id on public.payments(client_id);

-- 6. Row Level Security (RLS)
alter table public.coach_profiles enable row level security;
alter table public.clients enable row level security;
alter table public.subscriptions enable row level security;
alter table public.payments enable row level security;

-- Coach Profiles Policies
drop policy if exists "coach_profiles_select_own" on public.coach_profiles;
create policy "coach_profiles_select_own" on public.coach_profiles
  for select using (auth.uid() = id);

drop policy if exists "coach_profiles_insert_own" on public.coach_profiles;
create policy "coach_profiles_insert_own" on public.coach_profiles
  for insert with check (auth.uid() = id);

drop policy if exists "coach_profiles_update_own" on public.coach_profiles;
create policy "coach_profiles_update_own" on public.coach_profiles
  for update using (auth.uid() = id) with check (auth.uid() = id);

-- Clients Policies
drop policy if exists "clients_all_own" on public.clients;
create policy "clients_all_own" on public.clients
  for all using (auth.uid() = coach_id) with check (auth.uid() = coach_id);

-- Subscriptions Policies
drop policy if exists "subscriptions_all_own" on public.subscriptions;
create policy "subscriptions_all_own" on public.subscriptions
  for all using (auth.uid() = coach_id) with check (auth.uid() = coach_id);

-- Payments Policies
drop policy if exists "payments_all_own" on public.payments;
create policy "payments_all_own" on public.payments
  for all using (auth.uid() = coach_id) with check (auth.uid() = coach_id);
"""
      .trimIndent()
}
