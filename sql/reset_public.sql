-- Full reset of the public schema (PostgreSQL). Destroys all tables, extensions in schema, and data.
-- After this, start the app so Flyway runs V1, V2, ... or run: mvnw flyway:migrate (with JDBC props).
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO CURRENT_USER;
GRANT ALL ON SCHEMA public TO public;
