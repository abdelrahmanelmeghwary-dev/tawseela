-- =============================================================================
-- Tawseela — full database schema (manual run)
-- =============================================================================
-- Run once against your DB (e.g. psql -h localhost -p 55432 -U admin -d mydb -f schema.sql).
-- Idempotent: uses IF NOT EXISTS / CREATE OR REPLACE where possible.
-- Spring Boot: profiles matches com.tawseela.domain.Profile (no Supabase auth.users FK).
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ----------------------------------------------------------------------------- profiles
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY,
    phone VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(255),
    full_name VARCHAR(255),
    role VARCHAR(16) NOT NULL CHECK (role IN ('customer', 'driver', 'admin')),
    fcm_token VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_profiles_phone ON profiles (phone);

-- Email (nullable; PostgreSQL UNIQUE allows multiple NULLs). Idempotent for older DBs.
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS email VARCHAR(255);
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'profiles_email_key'
    ) THEN
        ALTER TABLE profiles ADD CONSTRAINT profiles_email_key UNIQUE (email);
    END IF;
END $$;

-- ----------------------------------------------------------------------------- otp_codes
CREATE TABLE IF NOT EXISTS otp_codes (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone       VARCHAR(64) NOT NULL,
    code        VARCHAR(6)  NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otp_codes_phone ON otp_codes (phone);
CREATE INDEX IF NOT EXISTS idx_otp_codes_expires_at ON otp_codes (expires_at);

-- ----------------------------------------------------------------------------- drivers
CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY REFERENCES profiles (id) ON DELETE CASCADE,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    last_seen TIMESTAMPTZ,
    current_lat DECIMAL(9, 6),
    current_lng DECIMAL(9, 6),
    total_deliveries INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------- orders
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES profiles (id),
    driver_id UUID REFERENCES drivers (id),
    description TEXT NOT NULL,
    delivery_lat DECIMAL(9, 6) NOT NULL,
    delivery_lng DECIMAL(9, 6) NOT NULL,
    delivery_address TEXT,
    status TEXT NOT NULL DEFAULT 'CREATED' CHECK (status IN (
        'CREATED', 'ASSIGNED', 'ACCEPTED',
        'PURCHASING', 'ON_THE_WAY', 'DELIVERED',
        'COMPLETED', 'CANCELLED'
    )),
    assigned_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------- order_status_history
CREATE TABLE IF NOT EXISTS order_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    status TEXT NOT NULL,
    actor_id UUID REFERENCES profiles (id),
    note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------- notifications
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    data JSONB NOT NULL DEFAULT '{}',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ----------------------------------------------------------------------------- indexes
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders (customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_driver_id ON orders (driver_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON order_status_history (order_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications (user_id);

-- ----------------------------------------------------------------------------- updated_at triggers
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS orders_updated_at ON orders;
CREATE TRIGGER orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

DROP TRIGGER IF EXISTS profiles_updated_at ON profiles;
CREATE TRIGGER profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at();

-- ----------------------------------------------------------------------------- metrics RPC
CREATE OR REPLACE FUNCTION get_daily_metrics(target_date DATE DEFAULT CURRENT_DATE)
RETURNS TABLE (
    total_orders BIGINT,
    completed_orders BIGINT,
    cancelled_orders BIGINT,
    active_drivers BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        COUNT(*) FILTER (WHERE DATE(o.created_at) = target_date),
        COUNT(*) FILTER (WHERE o.status = 'COMPLETED' AND DATE(o.created_at) = target_date),
        COUNT(*) FILTER (WHERE o.status = 'CANCELLED' AND DATE(o.created_at) = target_date),
        (SELECT COUNT(*)::BIGINT FROM drivers WHERE is_online = TRUE)
    FROM orders o;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
