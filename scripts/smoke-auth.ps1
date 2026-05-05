# Call every auth endpoint once. Run after `mvn spring-boot:run` (or jar) with DB ready.
# Usage: powershell -ExecutionPolicy Bypass -File scripts\smoke-auth.ps1
# Optional: -BaseUrl http://127.0.0.1:8080 -Phone +213555000099

param(
    [string] $BaseUrl = "http://localhost:8080",
    [string] $Phone = "+213555000099"
)

$ErrorActionPreference = "Stop"
$headers = @{ "Content-Type" = "application/json" }

Write-Host "=== 1) OTP request ===" -ForegroundColor Cyan
$r1 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/otp/request" -Method Post -Headers $headers -Body (@{ phone = $Phone } | ConvertTo-Json)
$r1 | ConvertTo-Json

Write-Host "`n=== 2) OTP verify (register or login) ===" -ForegroundColor Cyan
$r2 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/otp/verify" -Method Post -Headers $headers -Body (@{ phone = $Phone; otp = "1234" } | ConvertTo-Json)
$r2 | ConvertTo-Json
$access = $r2.accessToken
$refresh = $r2.refreshToken

if (-not $access) { throw "No accessToken in response" }

Write-Host "`n=== 3) GET /auth/me ===" -ForegroundColor Cyan
$auth = @{ Authorization = "Bearer $access" }
Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/me" -Method Get -Headers $auth | ConvertTo-Json

Write-Host "`n=== 4) Token refresh ===" -ForegroundColor Cyan
$r4 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/token/refresh" -Method Post -Headers $headers -Body (@{ refreshToken = $refresh } | ConvertTo-Json)
$r4 | ConvertTo-Json
$access2 = $r4.accessToken

Write-Host "`n=== 5) Logout (204) ===" -ForegroundColor Cyan
$logout = Invoke-WebRequest -Uri "$BaseUrl/api/v1/auth/logout" -Method Post -Headers @{ Authorization = "Bearer $access2" } -UseBasicParsing
Write-Host $logout.StatusCode

Write-Host "`nAll auth steps finished." -ForegroundColor Green
