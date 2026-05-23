-- Service / cron roles for internal APIs (push, scheduled jobs).
INSERT INTO roles (name)
SELECT 'SERVICE'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'SERVICE');

INSERT INTO roles (name)
SELECT 'CRON'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'CRON');
