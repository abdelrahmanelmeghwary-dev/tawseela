-- Backfill runtime drivers rows for already-approved driver_profiles (id = users.id).
INSERT INTO drivers (id, is_online, total_deliveries, created_at, updated_at)
SELECT dp.user_id, FALSE, 0, NOW(), NOW()
FROM driver_profiles dp
WHERE dp.approved = TRUE
  AND NOT EXISTS (SELECT 1 FROM drivers d WHERE d.id = dp.user_id);
