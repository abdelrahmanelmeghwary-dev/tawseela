-- Runtime driver state (location, online). id matches users.id (approved drivers).
CREATE TABLE IF NOT EXISTS drivers (
    id                UUID PRIMARY KEY,
    is_online         BOOLEAN      NOT NULL DEFAULT FALSE,
    last_seen         TIMESTAMPTZ,
    current_lat       DECIMAL(9, 6),
    current_lng       DECIMAL(9, 6),
    total_deliveries  INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'drivers_id_fkey') THEN
        ALTER TABLE drivers
            ADD CONSTRAINT drivers_id_fkey
            FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE;
    END IF;
EXCEPTION
    WHEN foreign_key_violation THEN
        RAISE NOTICE 'drivers_id_fkey not added: existing driver rows must match users.id first';
END $$;

CREATE INDEX IF NOT EXISTS idx_drivers_is_online ON drivers (is_online);
