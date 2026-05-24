-- One driver_profiles row per user (keep newest by created_at; legacy DBs may lack UNIQUE).
DELETE FROM driver_profiles dp
WHERE dp.ctid NOT IN (
    SELECT DISTINCT ON (d2.user_id) d2.ctid
    FROM driver_profiles d2
    ORDER BY d2.user_id, d2.created_at DESC NULLS LAST, d2.id DESC
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'driver_profiles_user_id_key'
           OR conname = 'driver_profiles_user_id_unique'
    ) AND NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE tablename = 'driver_profiles'
          AND indexdef LIKE '%UNIQUE%'
          AND indexdef LIKE '%user_id%'
    ) THEN
        ALTER TABLE driver_profiles ADD CONSTRAINT driver_profiles_user_id_key UNIQUE (user_id);
    END IF;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- One runtime drivers row per user (id = user_id). Remove duplicates if any exist.
DELETE FROM drivers d
WHERE d.ctid NOT IN (
    SELECT DISTINCT ON (d2.id) d2.ctid
    FROM drivers d2
    ORDER BY d2.id, d2.created_at DESC NULLS LAST
);

-- Sync runtime rows for all approved drivers with a valid user (idempotent).
INSERT INTO drivers (id, is_online, total_deliveries, created_at, updated_at)
SELECT dp.user_id, FALSE, 0, NOW(), NOW()
FROM driver_profiles dp
INNER JOIN users u ON u.id = dp.user_id
WHERE dp.approved = TRUE
  AND NOT EXISTS (SELECT 1 FROM drivers dr WHERE dr.id = dp.user_id);
