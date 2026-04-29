-- Phase 9 - RLS policies (MVP)
alter table public.users enable row level security;
alter table public.workouts enable row level security;
alter table public.sets enable row level security;

create policy users_owner_rw on public.users
for all to authenticated
using (id = auth.uid()::text)
with check (id = auth.uid()::text);

create policy workouts_owner_rw on public.workouts
for all to authenticated
using (user_id = auth.uid()::text)
with check (user_id = auth.uid()::text);

create policy sets_owner_rw on public.sets
for all to authenticated
using (user_id = auth.uid()::text)
with check (user_id = auth.uid()::text);
