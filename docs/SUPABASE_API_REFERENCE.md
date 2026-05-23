# Tawseela — Supabase API Reference

> **Source:** `D:\Tawseela\supabase` (migrations, Edge Functions, RLS policies)  
> **Purpose:** Catalog every backend capability from Supabase so the Spring Boot app (`tawseela`) can implement the same behavior.  
> **Target base URL (Spring):** `http://localhost:8080/api/v1`

---

## Overview

The Supabase backend has three layers:

| Layer | What it provides |
|-------|------------------|
| **PostgREST** | CRUD on `profiles`, `drivers`, `orders`, `order_status_history`, `notifications` (RLS-enforced) |
| **PostgreSQL RPC** | `get_daily_metrics(target_date)` |
| **Edge Functions** | `update-order-status`, `send-push-notification`, `expire-stale-assignments`, `test-phone-login` |
| **Supabase Auth** | Phone OTP (production); dev uses `test-phone-login` with OTP `1234` |

---

## Supabase → Spring mapping (quick reference)

| Supabase (today) | Spring Boot (target) |
|------------------|----------------------|
| `POST /auth/v1/otp` (phone) | `POST /api/v1/auth/otp/request` |
| `test-phone-login` edge function | `POST /api/v1/auth/otp/verify` (OTP `1234` in dev) |
| `GET /rest/v1/profiles` | `GET /api/v1/profiles`, `GET /api/v1/profiles/{id}` |
| `POST/PATCH /rest/v1/profiles` | `POST/PATCH /api/v1/profiles` |
| `GET/POST/PATCH /rest/v1/drivers` | `GET/POST/PATCH /api/v1/drivers` |
| `GET/POST /rest/v1/orders` | `GET/POST /api/v1/orders` |
| `update-order-status` function | `POST /api/v1/orders/{orderId}/status` |
| `GET /rest/v1/order_status_history` | `GET /api/v1/orders/{orderId}/history` |
| `GET/PATCH /rest/v1/notifications` | `GET/PATCH /api/v1/notifications` |
| `send-push-notification` function | `POST /api/v1/push/send` (internal/admin) |
| `POST /rest/v1/rpc/get_daily_metrics` | `GET /api/v1/metrics/daily` |
| `expire-stale-assignments` function | `POST /api/v1/system/expire-stale-assignments` |

---

## Edge Functions (Supabase)

Base URL: `{SUPABASE_URL}/functions/v1/{name}`

### 1. `update-order-status`

**Supabase:** `POST /functions/v1/update-order-status`  
**Spring:** `POST /api/v1/orders/{orderId}/status`

**Auth:** `Authorization: Bearer <JWT>` (validated via Auth API `/auth/v1/user`)

**Request body:**
```json
{
  "orderId": "uuid",
  "newStatus": "ASSIGNED",
  "note": "optional",
  "driverId": "uuid"
}
```

**Rules (from source):**
- `driverId` required when `newStatus` is `ASSIGNED`
- State machine transitions (see [Order status transitions](#order-status-transitions))
- `canTransition` also tries role `system` for every request (same as authenticated user triggering system transitions)
- On `ASSIGNED`: sets `driver_id`, `assigned_at`
- On `CREATED` from `ASSIGNED` (or `note` = `auto_expired` / `driver_rejected`): clears `driver_id`, `assigned_at`
- Inserts `order_status_history`; `actor_id` is `null` when `note === "auto_expired"`

**Push side-effects:**

| Transition | Recipient | Title | Body |
|------------|-----------|-------|------|
| `ASSIGNED` | Driver | 📦 New Delivery | You have a new order request |
| `ACCEPTED` | Customer | ✅ Order Accepted | Your driver is heading to the store |
| `ON_THE_WAY` | Customer | 🛵 On The Way | Your order is coming |
| `DELIVERED` | Customer | 🎉 Delivered | Your order has been delivered |
| `CREATED` + `note=auto_expired` | All admins | ⏰ No Response | Order #{shortId} was not accepted, back in queue |

**Response `200`:**
```json
{ "success": true, "order": { /* order row */ } }
```

**Errors:** `401` Unauthorized, `400` missing fields, `404` order not found, `403` transition not allowed, `500` update failed

---

### 2. `send-push-notification`

**Supabase:** `POST /functions/v1/send-push-notification`  
**Spring:** `POST /api/v1/push/send` (service/admin or internal only)

**Auth:** Service role key (internal calls from other functions)

**Request body:**
```json
{
  "userId": "uuid",
  "title": "string",
  "body": "string",
  "data": { "orderId": "uuid" }
}
```

**Behavior:**
- Loads `profiles.fcm_token` for `userId`
- Prefers FCM HTTP v1 (`FCM_SERVICE_ACCOUNT_JSON`); fallback legacy `FCM_SERVER_KEY`
- Does **not** insert into `notifications` table

**Response:**
```json
{ "success": true }
```
or
```json
{ "success": false, "reason": "no_fcm_token" }
```

**`reason` values:** `no_fcm_token`, `no_fcm_config`, `json_parse_error`, `missing_fields`, `token_error`, `fcm_not_configured`

---

### 3. `test-phone-login` (dev only)

**Supabase:** `POST /functions/v1/test-phone-login`  
**Spring:** Dev behavior on `POST /api/v1/auth/otp/verify` when `OTP_TEST_MODE=true`

**Request body:**
```json
{
  "phone": "+213XXXXXXXXX",
  "otp": "1234"
}
```

**Behavior:**
- Only accepts OTP `1234`
- Normalizes phone (`+` + digits)
- Finds or creates auth user; returns `token_hash` for client session (Supabase-specific)
- **Spring equivalent:** return JWT access/refresh tokens directly (see Auth section)

---

### 4. `expire-stale-assignments` (cron)

**Supabase:** `POST /functions/v1/expire-stale-assignments`  
**Spring:** `POST /api/v1/system/expire-stale-assignments` + `@Scheduled` every 60s

**Auth:** Cron secret header `X-Cron-Secret` (Spring); Supabase invokes via scheduled job

**Logic:**
- Finds orders `status = ASSIGNED` and `assigned_at < NOW() - 30 seconds`
- Sets `status = CREATED`, clears `driver_id`, `assigned_at`
- Inserts history: `status=CREATED`, `actor_id=null`, `note=auto_expired`
- Push-notifies all admins (same as `update-order-status` auto-expire)

**Response `200`:**
```json
{ "success": true, "expired": 3 }
```

---

## PostgreSQL RPC

### `get_daily_metrics`

**Supabase:** `POST /rest/v1/rpc/get_daily_metrics`  
**Body:** `{ "target_date": "2026-04-26" }` (optional, defaults to today)

**Spring:** `GET /api/v1/metrics/daily?date=2026-04-26` (admin only)

**Returns:**

| Field | Description |
|-------|-------------|
| `total_orders` | Orders created on `target_date` |
| `completed_orders` | `status = COMPLETED` on that date |
| `cancelled_orders` | `status = CANCELLED` on that date |
| `active_drivers` | Count of `drivers` where `is_online = true` |

**Spring response shape:**
```json
{
  "date": "2026-04-26",
  "totalOrders": 120,
  "completedOrders": 95,
  "cancelledOrders": 8,
  "activeDrivers": 14
}
```

---

## PostgREST tables (RLS → REST semantics)

Base: `{SUPABASE_URL}/rest/v1/{table}` with `apikey` + `Authorization: Bearer <JWT>`

PostgREST uses query params: `select`, `eq`, `filter`, etc. Spring exposes resource-oriented REST below.

### `profiles`

| Operation | RLS rule | Spring endpoint |
|-----------|----------|-----------------|
| SELECT own | `auth.uid() = id` | `GET /api/v1/profiles/{id}` |
| SELECT all | `role = admin` | `GET /api/v1/profiles` |
| INSERT own | `auth.uid() = id` | `POST /api/v1/profiles` |
| UPDATE own | `auth.uid() = id` | `PATCH /api/v1/profiles/{id}` |

**Columns:** `id`, `phone`, `full_name`, `role`, `fcm_token`, `created_at`, `updated_at`  
**Roles:** `customer`, `driver`, `admin`

---

### `drivers`

| Operation | RLS rule | Spring endpoint |
|-----------|----------|-----------------|
| SELECT | admin, customer, driver | `GET /api/v1/drivers`, `GET /api/v1/drivers/{id}` |
| INSERT own | `auth.uid() = id` | `POST /api/v1/drivers` |
| UPDATE own | `auth.uid() = id` | `PATCH /api/v1/drivers/{id}` |

**Columns:** `id`, `is_online`, `last_seen`, `current_lat`, `current_lng`, `total_deliveries`, `created_at`

> `total_deliveries` has no DB trigger — increment in Spring when order reaches `COMPLETED`.

---

### `orders`

| Operation | RLS rule | Spring endpoint |
|-----------|----------|-----------------|
| INSERT | customer, `customer_id = auth.uid()` | `POST /api/v1/orders` |
| SELECT | customer owns order | `GET /api/v1/orders` (filtered) |
| SELECT | driver is `driver_id` | `GET /api/v1/orders` (filtered) |
| ALL | admin | Full access + `DELETE /api/v1/orders/{id}` |

**Status writes:** Only via `update-order-status` (Edge Function / Spring status endpoint), not direct PATCH on `status` in client apps.

**Columns:** `id`, `customer_id`, `driver_id`, `description`, `delivery_lat`, `delivery_lng`, `delivery_address`, `status`, `assigned_at`, `created_at`, `updated_at`

---

### `order_status_history`

| Operation | RLS rule | Spring endpoint |
|-----------|----------|-----------------|
| SELECT | customer/driver on order, or admin | `GET /api/v1/orders/{orderId}/history` |
| INSERT | service role (`WITH CHECK (true)`) | Internal (status update service) |

**Columns:** `id`, `order_id`, `status`, `actor_id`, `note`, `created_at`

---

### `notifications`

| Operation | RLS rule | Spring endpoint |
|-----------|----------|-----------------|
| SELECT own | `auth.uid() = user_id` | `GET /api/v1/notifications` |
| UPDATE own | `auth.uid() = user_id` | `PATCH /api/v1/notifications/{id}` |
| INSERT | `WITH CHECK (true)` | Optional; Edge Functions do not insert today |

---

## Order status transitions

Allowed transitions (from `update-order-status/index.ts`):

| From | To | Roles |
|------|-----|-------|
| `CREATED` | `ASSIGNED` | `admin` (+ `driverId` required) |
| `ASSIGNED` | `ACCEPTED`, `CREATED` | `driver`, `system` |
| `ACCEPTED` | `PURCHASING` | `driver` |
| `PURCHASING` | `ON_THE_WAY` | `driver` |
| `ON_THE_WAY` | `DELIVERED` | `driver` |
| `DELIVERED` | `COMPLETED` | `driver`, `system` |
| `*` (non-terminal) | `CANCELLED` | `admin` only |

**Driver rule:** If role is `driver`, `order.driver_id` must equal `actorId`.

**Enum values:** `CREATED`, `ASSIGNED`, `ACCEPTED`, `PURCHASING`, `ON_THE_WAY`, `DELIVERED`, `COMPLETED`, `CANCELLED`

---

## Spring Boot REST API (full spec)

Auth uses JWT Bearer. All paths under `/api/v1`.

### 1. Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/otp/request` | No | Request OTP for phone |
| POST | `/auth/otp/verify` | No | Verify OTP → JWT (+ user) |
| POST | `/auth/token/refresh` | No | Refresh tokens |
| POST | `/auth/logout` | Yes | Logout |
| GET | `/auth/me` | Yes | Current user + profile fields |

**OTP request:**
```json
{ "phone": "+213XXXXXXXXX" }
```

**OTP verify:**
```json
{ "phone": "+213XXXXXXXXX", "otp": "1234" }
```

**Verify response:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "uuid",
    "phone": "+213XXXXXXXXX",
    "role": "customer | driver | admin"
  }
}
```

---

### 2. Profiles

| Method | Path | Access |
|--------|------|--------|
| GET | `/profiles/{id}` | Own or admin |
| POST | `/profiles` | Own (`id` = auth user) |
| PATCH | `/profiles/{id}` | Own (`fullName`, `fcmToken`) |
| GET | `/profiles` | Admin only (`role`, `page`, `size`) |

---

### 3. Drivers

| Method | Path | Access |
|--------|------|--------|
| GET | `/drivers/{id}` | Authenticated |
| POST | `/drivers` | Driver role, `id` = own profile |
| PATCH | `/drivers/{id}` | Own (`isOnline`, `lastSeen`, `currentLat`, `currentLng`) |
| GET | `/drivers` | Authenticated (`isOnline`, pagination) |

---

### 4. Orders

| Method | Path | Access |
|--------|------|--------|
| POST | `/orders` | Customer — creates with `status=CREATED` |
| GET | `/orders/{id}` | Customer (own), driver (assigned), admin (any) |
| GET | `/orders` | Role-filtered list (`status`, `page`, `size`, `sort`) |
| POST | `/orders/{orderId}/status` | State machine (see above) |
| DELETE | `/orders/{id}` | Admin only |

**Create order body:**
```json
{
  "description": "Buy medicine from pharmacy",
  "deliveryLat": 36.737232,
  "deliveryLng": 3.086472,
  "deliveryAddress": "12 Rue Didouche Mourad, Alger"
}
```

**Update status body:**
```json
{
  "newStatus": "ASSIGNED",
  "note": "optional",
  "driverId": "uuid"
}
```

---

### 5. Order status history

| Method | Path | Access |
|--------|------|--------|
| GET | `/orders/{orderId}/history` | Same as reading the order |

---

### 6. Notifications

| Method | Path | Access |
|--------|------|--------|
| GET | `/notifications` | Own (`isRead`, pagination) |
| PATCH | `/notifications/{id}` | Own — `{ "isRead": true }` |

---

### 7. Push

| Method | Path | Access |
|--------|------|--------|
| POST | `/push/send` | Admin / internal service |

---

### 8. Metrics

| Method | Path | Access |
|--------|------|--------|
| GET | `/metrics/daily?date=YYYY-MM-DD` | Admin only |

---

### 9. System / cron

| Method | Path | Access |
|--------|------|--------|
| POST | `/system/expire-stale-assignments` | `X-Cron-Secret` header |

**Schedule:** every 60 seconds; stale threshold 30 seconds.

---

## Data models (database)

### `profiles`
- `id` UUID PK → auth user
- `phone` TEXT UNIQUE NOT NULL
- `full_name`, `role`, `fcm_token`
- `created_at`, `updated_at` (trigger on update)

### `drivers`
- `id` UUID PK → `profiles(id)`
- `is_online`, `last_seen`, `current_lat`, `current_lng`, `total_deliveries`

### `orders`
- `customer_id` → profiles, `driver_id` → drivers (nullable)
- `status` with CHECK constraint
- `assigned_at` set on assign, cleared on reject/expire

### `order_status_history`
- Append-only audit; FK cascade delete with order

### `notifications`
- `user_id`, `title`, `body`, `data` JSONB, `is_read`

---

## Error responses (Spring)

```json
{
  "error": "Human-readable message",
  "code": "MACHINE_READABLE_CODE",
  "status": 400
}
```

| HTTP | When |
|------|------|
| 400 | Validation, missing fields |
| 401 | Missing/invalid JWT |
| 403 | Wrong role or ownership |
| 404 | Not found |
| 409 | Duplicate (e.g. phone) |
| 422 | Invalid state transition |
| 500 | Server error |

---

## Environment variables

| Variable | Purpose |
|----------|---------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL |
| `JWT_SECRET`, `JWT_EXPIRATION_MS`, `JWT_REFRESH_EXPIRATION_MS` | Auth tokens |
| `FCM_SERVICE_ACCOUNT_JSON`, `FCM_PROJECT_ID` | Push (FCM v1) |
| `FCM_SERVER_KEY` | Push legacy fallback |
| `CRON_SECRET` | Cron endpoint auth |
| `STALE_ASSIGNMENT_SECONDS` | Default `30` |
| `OTP_TEST_MODE` | Allow OTP `1234` in non-prod |
| `SERVER_PORT` | Default `8080` |

---

## Source files (Supabase repo)

| Path | Contents |
|------|----------|
| `migrations/20250221000001_core_tables.sql` | Schema |
| `migrations/20250221000002_indexes.sql` | Indexes |
| `migrations/20250221000003_updated_at_trigger.sql` | `updated_at` trigger |
| `migrations/20250221000004_rls.sql` | RLS policies |
| `migrations/20250221000005_get_daily_metrics_rpc.sql` | Metrics RPC |
| `functions/update-order-status/index.ts` | Order state machine |
| `functions/send-push-notification/index.ts` | FCM push |
| `functions/expire-stale-assignments/index.ts` | Stale assignment cron |
| `functions/test-phone-login/index.ts` | Dev OTP `1234` |
| `config.toml` | Edge function JWT settings |
| `SPRING_BOOT_API_SPEC.md` | Extended Spring implementation notes |

---

## Notes for Spring implementation

1. **Status updates** must go through a single service (mirrors `update-order-status`); do not expose raw `PATCH` on order `status` to clients.
2. **FCM token** is stored on `profiles.fcm_token` via `PATCH /profiles/{id}` — no separate device-token API.
3. **Push on status change** is server-driven; clients do not call push after status updates.
4. **`total_deliveries`** increment when transitioning to `COMPLETED` (not in Supabase DB logic).
5. **Existing tawseela controllers** (`/api/auth`, `/api/admin`, etc.) differ from this spec — align or migrate clients when implementing Supabase parity.
