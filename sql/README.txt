Flyway (versioned migrations under src/main/resources/db/migration)
=====================================================================

- Do not change migration files after they have been applied to an environment
  (Flyway checksum validation will fail, and you rewrite history).
- Need a schema fix? Add a new file: V3__description.sql, V4__..., never by editing
  an already-executed V1__ / V2__ / ...
- Mistaken edit: restore the exact previous file from git, or flyway repair (only
  when the file truly matches what ran), or add forward-fix migrations.
- Obsolete note: an older git revision used a different V1 (profiles/orders);
  do not "restore" that into this repo if your databases and Java code expect the
  current auth schema — use new versions instead.


Manual database setup
=======================

Run this single file against your PostgreSQL database:

  sql/schema.sql

Example (host):

  psql -h localhost -p 55432 -U admin -d mydb -f "d:\Tawseela\supabase\tawseela-api\sql\schema.sql"


Full reset (drop public schema, then Flyway from app)
=====================================================

Option A — Spring Boot (recommended): start the API once with profile schema-reset
so Flyway clean() + migrate() runs, then remove that profile from the command line.

  java -jar tawseela-api.jar --spring.profiles.active=schema-reset

Option B — SQL + Maven Flyway: run sql/reset_public.sql, then migrate.

  psql ... -f sql/reset_public.sql
  .\mvnw.cmd flyway:migrate "-Dflyway.url=..." "-Dflyway.user=..." "-Dflyway.password=..."

Option C — Maven Flyway only (drops Flyway-managed objects, then migrate):

  .\mvnw.cmd flyway:clean flyway:migrate "-Dflyway.url=..." "-Dflyway.user=..." "-Dflyway.password=..."


Production: "relation profiles already exists" (Flyway at V3)
=============================================================

Happens when delivery tables were created outside Flyway (legacy Supabase/manual SQL)
while flyway_schema_history is still at V3.

Fix: deploy the latest app (V4–V8 migrations are idempotent: CREATE IF NOT EXISTS).

If a migration still fails after deploy, inspect:

  SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;

Then either repair checksums (only if file content matches what ran):

  flyway repair

or baseline past existing objects (last resort; coordinate with ops):

  flyway baseline -baselineVersion=8
