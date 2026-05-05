# Applies sql/schema.sql (full schema in one file).
# Prereq: from tawseela-api folder run `docker compose up -d`
# Usage: powershell -ExecutionPolicy Bypass -File scripts\init-db.ps1

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$sqlPath = Join-Path $root "sql\schema.sql"
$compose = Join-Path $root "docker-compose.yml"

Write-Host "Applying $sqlPath ..."
Get-Content $sqlPath -Raw | docker compose -f $compose exec -T postgres psql -U admin -d mydb
Write-Host "Done."
