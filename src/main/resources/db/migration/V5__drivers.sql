-- Runtime driver state (location, online). id matches users.id (approved drivers).
CREATE TABLE drivers (
    id                UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    is_online         BOOLEAN      NOT NULL DEFAULT FALSE,
    last_seen         TIMESTAMPTZ,
    current_lat       DECIMAL(9, 6),
    current_lng       DECIMAL(9, 6),
    total_deliveries  INT          NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_drivers_is_online ON drivers (is_online);
