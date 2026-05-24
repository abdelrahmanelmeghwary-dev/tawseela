-- Backfill runtime drivers rows for approved driver_profiles (id = users.id).
-- Only rows whose user_id exists in users (skip legacy orphan driver_profiles).
INSERT INTO drivers (id, is_online, total_deliveries, created_at, updated_at)
SELECT dp.user_id, FALSE, 0, NOW(), NOW()
FROM driver_profiles dp
INNER JOIN users u ON u.id = dp.user_id
WHERE dp.approved = TRUE
  AND NOT EXISTS (SELECT 1 FROM drivers d WHERE d.id = dp.user_id);
