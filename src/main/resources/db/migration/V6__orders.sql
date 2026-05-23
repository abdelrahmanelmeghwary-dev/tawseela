-- Orders and status history (Supabase-aligned lifecycle).
CREATE TABLE IF NOT EXISTS orders (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id       UUID         NOT NULL,
    driver_id         UUID,
    description       TEXT         NOT NULL,
    delivery_lat      DECIMAL(9, 6) NOT NULL,
    delivery_lng      DECIMAL(9, 6) NOT NULL,
    delivery_address  TEXT,
    status            VARCHAR(32)  NOT NULL DEFAULT 'CREATED',
    assigned_at       TIMESTAMPTZ,
    version           BIGINT       NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'orders_customer_id_fkey') THEN
        ALTER TABLE orders
            ADD CONSTRAINT orders_customer_id_fkey
            FOREIGN KEY (customer_id) REFERENCES users (id);
    END IF;
EXCEPTION
    WHEN foreign_key_violation THEN
        RAISE NOTICE 'orders_customer_id_fkey not added';
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'orders_driver_id_fkey') THEN
        ALTER TABLE orders
            ADD CONSTRAINT orders_driver_id_fkey
            FOREIGN KEY (driver_id) REFERENCES drivers (id);
    END IF;
EXCEPTION
    WHEN foreign_key_violation THEN
        RAISE NOTICE 'orders_driver_id_fkey not added';
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_orders_status') THEN
        ALTER TABLE orders ADD CONSTRAINT chk_orders_status CHECK (status IN (
            'CREATED', 'ASSIGNED', 'ACCEPTED', 'PURCHASING',
            'ON_THE_WAY', 'DELIVERED', 'COMPLETED', 'CANCELLED'
        ));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders (customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_driver_id ON orders (driver_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_assigned_at ON orders (assigned_at);

CREATE TABLE IF NOT EXISTS order_status_history (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id   UUID         NOT NULL,
    status     VARCHAR(32)  NOT NULL,
    actor_id   UUID,
    note       TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'order_status_history_order_id_fkey') THEN
        ALTER TABLE order_status_history
            ADD CONSTRAINT order_status_history_order_id_fkey
            FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'order_status_history_actor_id_fkey') THEN
        ALTER TABLE order_status_history
            ADD CONSTRAINT order_status_history_actor_id_fkey
            FOREIGN KEY (actor_id) REFERENCES users (id);
    END IF;
EXCEPTION
    WHEN foreign_key_violation THEN
        RAISE NOTICE 'order_status_history_actor_id_fkey not added';
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_order_status_history_status') THEN
        ALTER TABLE order_status_history ADD CONSTRAINT chk_order_status_history_status CHECK (status IN (
            'CREATED', 'ASSIGNED', 'ACCEPTED', 'PURCHASING',
            'ON_THE_WAY', 'DELIVERED', 'COMPLETED', 'CANCELLED'
        ));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON order_status_history (order_id);
