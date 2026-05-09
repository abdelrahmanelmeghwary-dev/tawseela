-- Adds driver_profiles when an older V1 already ran without this table (Flyway does not re-run edited V1).
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS driver_profiles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID         NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    vehicle_type    VARCHAR(100) NOT NULL,
    vehicle_number  VARCHAR(100) NOT NULL,
    license_number  VARCHAR(100) NOT NULL,
    approved        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_driver_profiles_user ON driver_profiles (user_id);
CREATE INDEX IF NOT EXISTS idx_driver_profiles_approved ON driver_profiles (approved);
