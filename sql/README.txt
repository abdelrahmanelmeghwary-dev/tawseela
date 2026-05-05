Manual database setup
=======================

Run this single file against your PostgreSQL database:

  sql/schema.sql

Example (host):

  psql -h localhost -p 55432 -U admin -d mydb -f "d:\Tawseela\supabase\tawseela-api\sql\schema.sql"

From the tawseela-api folder with Docker Compose:

  Get-Content .\sql\schema.sql -Raw | docker compose exec -T postgres psql -U admin -d mydb

Or use:  powershell -ExecutionPolicy Bypass -File .\scripts\init-db.ps1
