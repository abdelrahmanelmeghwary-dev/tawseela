-- Delivery profile extension for authenticated users (1:1 with users).
CREATE TABLE profiles (
    id          UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    full_name   VARCHAR(200),
    phone       VARCHAR(64)  NOT NULL,
    fcm_token   TEXT,
    avatar_url  VARCHAR(512),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_profiles_phone ON profiles (phone);

-- Backfill profiles for existing users from auth data.
INSERT INTO profiles (id, full_name, phone, created_at, updated_at)
SELECT
    u.id,
    TRIM(CONCAT(u.first_name, ' ', u.last_name)),
    u.mobile_number,
    u.created_at,
    u.updated_at
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM profiles p WHERE p.id = u.id);
