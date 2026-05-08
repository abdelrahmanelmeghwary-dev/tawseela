Manual database setup
=======================

Run this single file against your PostgreSQL database:

  sql/schema.sql

Example (host):

  psql -h localhost -p 55432 -U admin -d mydb -f "d:\Tawseela\supabase\tawseela-api\sql\schema.sql"
