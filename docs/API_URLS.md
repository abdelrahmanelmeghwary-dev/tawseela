# Tawseela — API URLs (with role)

Roles: `public` · `authenticated` · `customer` · `driver` · `admin` · `service` · `cron`

---

## Supabase Auth

| Role | Method | URL |
|------|--------|-----|
| public | POST | `{SUPABASE_URL}/auth/v1/otp` |
| public | POST | `{SUPABASE_URL}/auth/v1/verify` |
| public | POST | `{SUPABASE_URL}/auth/v1/token?grant_type=refresh_token` |
| authenticated | POST | `{SUPABASE_URL}/auth/v1/logout` |
| authenticated | GET | `{SUPABASE_URL}/auth/v1/user` |

---

## Supabase Edge Functions

| Role | Method | URL |
|------|--------|-----|
| authenticated | POST | `{SUPABASE_URL}/functions/v1/update-order-status` |
| service | POST | `{SUPABASE_URL}/functions/v1/send-push-notification` |
| cron | POST | `{SUPABASE_URL}/functions/v1/expire-stale-assignments` |
| public | POST | `{SUPABASE_URL}/functions/v1/test-phone-login` |

---

## Supabase PostgREST

| Role | Method | URL |
|------|--------|-----|
| admin | GET | `{SUPABASE_URL}/rest/v1/profiles` |
| authenticated, admin | GET | `{SUPABASE_URL}/rest/v1/profiles?id=eq.{id}` |
| authenticated | POST | `{SUPABASE_URL}/rest/v1/profiles` |
| authenticated | PATCH | `{SUPABASE_URL}/rest/v1/profiles?id=eq.{id}` |
| customer, driver, admin | GET | `{SUPABASE_URL}/rest/v1/drivers` |
| customer, driver, admin | GET | `{SUPABASE_URL}/rest/v1/drivers?id=eq.{id}` |
| driver | POST | `{SUPABASE_URL}/rest/v1/drivers` |
| driver | PATCH | `{SUPABASE_URL}/rest/v1/drivers?id=eq.{id}` |
| customer, driver, admin | GET | `{SUPABASE_URL}/rest/v1/orders` |
| customer, driver, admin | GET | `{SUPABASE_URL}/rest/v1/orders?id=eq.{id}` |
| customer | POST | `{SUPABASE_URL}/rest/v1/orders` |
| admin | PATCH | `{SUPABASE_URL}/rest/v1/orders?id=eq.{id}` |
| admin | DELETE | `{SUPABASE_URL}/rest/v1/orders?id=eq.{id}` |
| customer, driver, admin | GET | `{SUPABASE_URL}/rest/v1/order_status_history` |
| customer, driver, admin | GET | `{SUPABASE_URL}/rest/v1/order_status_history?order_id=eq.{orderId}` |
| authenticated | GET | `{SUPABASE_URL}/rest/v1/notifications` |
| authenticated | PATCH | `{SUPABASE_URL}/rest/v1/notifications?id=eq.{id}` |
| service | POST | `{SUPABASE_URL}/rest/v1/notifications` |

---

## Supabase RPC

| Role | Method | URL |
|------|--------|-----|
| admin | POST | `{SUPABASE_URL}/rest/v1/rpc/get_daily_metrics` |

---

## Spring Boot `/api/v1` (target)

| Role | Method | URL |
|------|--------|-----|
| public | POST | `http://localhost:8080/api/v1/auth/otp/request` |
| public | POST | `http://localhost:8080/api/v1/auth/otp/verify` |
| public | POST | `http://localhost:8080/api/v1/auth/token/refresh` |
| authenticated | POST | `http://localhost:8080/api/v1/auth/logout` |
| authenticated | GET | `http://localhost:8080/api/v1/auth/me` |
| admin | GET | `http://localhost:8080/api/v1/profiles` |
| authenticated, admin | GET | `http://localhost:8080/api/v1/profiles/{id}` |
| authenticated | POST | `http://localhost:8080/api/v1/profiles` |
| authenticated | PATCH | `http://localhost:8080/api/v1/profiles/{id}` |
| customer, driver, admin | GET | `http://localhost:8080/api/v1/drivers` |
| customer, driver, admin | GET | `http://localhost:8080/api/v1/drivers/{id}` |
| driver | POST | `http://localhost:8080/api/v1/drivers` |
| driver | PATCH | `http://localhost:8080/api/v1/drivers/{id}` |
| customer, driver, admin | GET | `http://localhost:8080/api/v1/orders` |
| customer, driver, admin | GET | `http://localhost:8080/api/v1/orders/{id}` |
| customer | POST | `http://localhost:8080/api/v1/orders` |
| authenticated | POST | `http://localhost:8080/api/v1/orders/{orderId}/status` |
| admin | DELETE | `http://localhost:8080/api/v1/orders/{id}` |
| customer, driver, admin | GET | `http://localhost:8080/api/v1/orders/{orderId}/history` |
| authenticated | GET | `http://localhost:8080/api/v1/notifications` |
| admin, service | POST | `http://localhost:8080/api/v1/notifications` |
| authenticated | PATCH | `http://localhost:8080/api/v1/notifications/{id}` |
| admin, service | POST | `http://localhost:8080/api/v1/push/send` |
| admin | GET | `http://localhost:8080/api/v1/metrics/daily` |
| admin | GET | `http://localhost:8080/api/v1/metrics/daily?date={YYYY-MM-DD}` |
| cron | POST | `http://localhost:8080/api/v1/system/expire-stale-assignments` |

---

## Spring Boot `/api` (current)

| Role | Method | URL |
|------|--------|-----|
| public | POST | `http://localhost:8080/api/auth/register` |
| public | POST | `http://localhost:8080/api/auth/register/verify` |
| public | POST | `http://localhost:8080/api/auth/login` |
| public | POST | `http://localhost:8080/api/auth/refresh-token` |
| authenticated | POST | `http://localhost:8080/api/auth/logout` |
| authenticated | GET | `http://localhost:8080/api/auth/me` |
| driver | PATCH | `http://localhost:8080/api/auth/me/driver-profile` |
| public | POST | `http://localhost:8080/api/auth/forgot-password/send-otp` |
| public | POST | `http://localhost:8080/api/auth/forgot-password/verify-otp` |
| public | POST | `http://localhost:8080/api/auth/forgot-password/reset` |
| admin | GET | `http://localhost:8080/api/admin/users` |
| admin | GET | `http://localhost:8080/api/admin/drivers` |
| admin | PUT | `http://localhost:8080/api/admin/drivers/{id}/approve` |
| admin | PUT | `http://localhost:8080/api/admin/drivers/{id}/reject` |
