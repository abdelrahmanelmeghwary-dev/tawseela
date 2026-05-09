# Smoke test new auth API (password + JWT). Run with app up and DB migrated.
# Usage: powershell -ExecutionPolicy Bypass -File scripts\smoke-auth.ps1
# Optional: -BaseUrl http://127.0.0.1:8099 -Mobile admin -Password "ChangeMe1!Strong"

param(
    [string] $BaseUrl = "http://localhost:8099",
    [string] $Mobile = "admin",
    [string] $Password = "ChangeMe1!Strong"
)

$ErrorActionPreference = "Stop"
$json = @{ "Content-Type" = "application/json" }

function Unwrap-Data($response) {
    if ($response.data) { return $response.data }
    return $response
}

Write-Host "=== 1) Login ===" -ForegroundColor Cyan
$loginBody = @{ mobileNumber = $Mobile; password = $Password } | ConvertTo-Json
$login = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Headers $json -Body $loginBody
$tokens = Unwrap-Data $login
$access = $tokens.accessToken
$refresh = $tokens.refreshToken
if (-not $access) { throw "No accessToken in response (expected ApiResponse wrapper with data)" }

Write-Host "`n=== 2) Customer me (403 if not CUSTOMER) ===" -ForegroundColor Cyan
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/customer/me" -Method Get -Headers @{ Authorization = "Bearer $access" } | ConvertTo-Json
} catch {
    Write-Host "customer/me skipped or forbidden for this role" -ForegroundColor Yellow
}

Write-Host "`n=== 3) Admin users (403 if not ADMIN) ===" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$BaseUrl/api/admin/users" -Method Get -Headers @{ Authorization = "Bearer $access" } | ConvertTo-Json

Write-Host "`n=== 4) Refresh token ===" -ForegroundColor Cyan
$refBody = @{ refreshToken = $refresh } | ConvertTo-Json
$ref = Invoke-RestMethod -Uri "$BaseUrl/api/auth/refresh-token" -Method Post -Headers $json -Body $refBody
$tokens2 = Unwrap-Data $ref
$access2 = $tokens2.accessToken
$refresh2 = $tokens2.refreshToken

Write-Host "`n=== 5) Logout ===" -ForegroundColor Cyan
$logoutBody = @{ refreshToken = $refresh2 } | ConvertTo-Json
Invoke-RestMethod -Uri "$BaseUrl/api/auth/logout" -Method Post -Headers @{
    Authorization = "Bearer $access2"
    "Content-Type"  = "application/json"
} -Body $logoutBody | ConvertTo-Json

Write-Host "`nSmoke finished." -ForegroundColor Green
