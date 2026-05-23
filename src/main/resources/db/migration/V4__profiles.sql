-- Delivery profile extension for authenticated users (1:1 with users).
-- Idempotent: safe when legacy Supabase/manual schema already created profiles.
CREATE TABLE IF NOT EXISTS profiles (
    id          UUID PRIMARY KEY,
    full_name   VARCHAR(200),
    phone       VARCHAR(64),
    fcm_token   TEXT,
    avatar_url  VARCHAR(512),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'profiles_id_fkey') THEN
        ALTER TABLE profiles
            ADD CONSTRAINT profiles_id_fkey
            FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE;
    END IF;
EXCEPTION
    WHEN foreign_key_violation THEN
        RAISE NOTICE 'profiles_id_fkey not added: existing profile rows must match users.id first';
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS idx_profiles_phone ON profiles (phone);

-- Backfill profiles for existing users from auth data.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'profiles' AND column_name = 'role'
    ) THEN
        INSERT INTO profiles (id, full_name, phone, created_at, updated_at, role)
        SELECT
            u.id,
            TRIM(CONCAT(u.first_name, ' ', u.last_name)),
            u.mobile_number,
            u.created_at,
            u.updated_at,
            COALESCE(
                (
                    SELECT CASE r.name
                        WHEN 'CUSTOMER' THEN 'customer'
                        WHEN 'DRIVER' THEN 'driver'
                        WHEN 'ADMIN' THEN 'admin'
                        ELSE 'customer'
                    END
                    FROM user_roles ur
                    JOIN roles r ON r.id = ur.role_id
                    WHERE ur.user_id = u.id
                    ORDER BY r.name
                    LIMIT 1
                ),
                'customer')
        FROM users u
        WHERE NOT EXISTS (SELECT 1 FROM profiles p WHERE p.id = u.id);
    ELSE
        INSERT INTO profiles (id, full_name, phone, created_at, updated_at)
        SELECT
            u.id,
            TRIM(CONCAT(u.first_name, ' ', u.last_name)),
            u.mobile_number,
            u.created_at,
            u.updated_at
        FROM users u
        WHERE NOT EXISTS (SELECT 1 FROM profiles p WHERE p.id = u.id);
    END IF;
END $$;
