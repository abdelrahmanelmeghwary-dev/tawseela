-- Orders and status history (Supabase-aligned lifecycle).
CREATE TABLE orders (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id       UUID         NOT NULL REFERENCES users (id),
    driver_id         UUID         REFERENCES drivers (id),
    description       TEXT         NOT NULL,
    delivery_lat      DECIMAL(9, 6) NOT NULL,
    delivery_lng      DECIMAL(9, 6) NOT NULL,
    delivery_address  TEXT,
    status            VARCHAR(32)  NOT NULL DEFAULT 'CREATED',
    assigned_at       TIMESTAMPTZ,
    version           BIGINT       NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_orders_status CHECK (status IN (
        'CREATED', 'ASSIGNED', 'ACCEPTED', 'PURCHASING',
        'ON_THE_WAY', 'DELIVERED', 'COMPLETED', 'CANCELLED'
    ))
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_driver_id ON orders (driver_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at DESC);
CREATE INDEX idx_orders_assigned_at ON orders (assigned_at);

CREATE TABLE order_status_history (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id   UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    status     VARCHAR(32)  NOT NULL,
    actor_id   UUID         REFERENCES users (id),
    note       TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_order_status_history_status CHECK (status IN (
        'CREATED', 'ASSIGNED', 'ACCEPTED', 'PURCHASING',
        'ON_THE_WAY', 'DELIVERED', 'COMPLETED', 'CANCELLED'
    ))
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history (order_id);
