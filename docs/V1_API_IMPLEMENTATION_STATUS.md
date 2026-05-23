# `/api/v1` implementation status

**Status: complete** (delivery domain; auth remains on `/api/auth`).

## Implemented endpoints

| Module | Endpoints |
|--------|-----------|
| Auth | `GET /api/v1/auth/me` |
| Profiles | `GET`, `GET /{id}`, `POST`, `PATCH /{id}` |
| Drivers | `GET`, `GET /{id}`, `POST`, `PATCH /{id}` |
| Orders | `GET`, `GET /{id}`, `POST`, `POST /{id}/status`, `DELETE /{id}`, `GET /{id}/history` |
| Notifications | `GET`, `POST`, `PATCH /{id}` |
| Push | `POST /push/send` |
| Metrics | `GET /metrics/daily` |
| System | `POST /system/expire-stale-assignments` |

## Database (Flyway)

- `V3__delivery_roles.sql` — SERVICE, CRON
- `V4__profiles.sql`
- `V5__drivers.sql`
- `V6__orders.sql`
- `V7__notifications.sql`

## Auth (not duplicated)

Use existing:

- `POST /api/auth/login`, `/register`, `/otp/*`, `/refresh-token`, `/logout`, forgot-password

## Internal headers

| Header | Role | Paths |
|--------|------|-------|
| `X-Cron-Secret` | CRON | `/api/v1/system/**` |
| `X-Service-Secret` | SERVICE | `/api/v1/push/**` |

## Order lifecycle

`CREATED` → `ASSIGNED` → `ACCEPTED` → `PURCHASING` → `ON_THE_WAY` → `DELIVERED` → `COMPLETED` (or `CANCELLED`)

Push notifications + stale assignment cron (30s / 60s) match Supabase behavior.
