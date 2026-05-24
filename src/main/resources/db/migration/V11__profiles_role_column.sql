-- Optional legacy role column (Supabase); nullable on new installs.
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS role VARCHAR(32);
