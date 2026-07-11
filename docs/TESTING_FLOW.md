# Tawseela API — testing flow

All authenticated calls use `Authorization: Bearer {{accessToken}}` unless noted.

## ID cheat sheet (important)

| Concept | Table | ID to use in API |
|--------|--------|------------------|
| Auth / login user | `users` | `userId` |
| Driver registration (KYC) | `driver_profiles` | `driverProfileId` — admin approve/reject only |
| Delivery profile (name, phone, FCM) | `profiles` | same as `userId` |
| Runtime driver (online, location) | `drivers` | same as `userId` |

**Admin** `PUT /api/admin/drivers/{id}/approve` → `{id}` = **driverProfileId**  
**Delivery** `GET /api/v1/drivers/{id}` → `{id}` = **userId**

---

## 1. Customer (happy path)

1. `POST /api/auth/register` — role `CUSTOMER`, body with mobile/password/name  
2. `POST /api/auth/register/verify` — OTP → returns tokens  
3. `GET /api/auth/me` — confirm role CUSTOMER  
4. `POST /api/v1/orders` — create order (CUSTOMER)  
5. `GET /api/v1/orders` — list own orders  

---

## 2. Driver (happy path)

1. `POST /api/auth/register` — role `DRIVER` + vehicle fields  
2. `POST /api/auth/register/verify` — OTP → message pending approval (no tokens)  
3. Admin: `POST /api/auth/login` — ADMIN  
4. `GET /api/admin/drivers` — note `driverProfileId` and `userId`  
5. `PUT /api/admin/drivers/{driverProfileId}/approve`  
6. Driver: `POST /api/auth/login`  
7. `GET /api/auth/me` — vehicle fields, `driverApproved: true`  
8. `GET /api/v1/drivers/{userId}` — runtime driver (auto-created on approve)  
9. `PATCH /api/v1/drivers/{userId}` — set `online`, lat/lng  

---

## 3. Admin

- `GET /api/admin/users`  
- `GET /api/admin/drivers`  
- `PUT /api/admin/drivers/{driverProfileId}/approve`  
- `PUT /api/admin/drivers/{driverProfileId}/reject`  
- `GET /api/v1/profiles?role=DRIVER` — delivery profiles (admin)  

---

## 4. Three “profile” layers (by design)

1. **driver_profiles** — registration KYC (vehicle, license, `approved`)  
2. **profiles** — delivery app profile (`fullName`, `phone`, `fcm_token`); created on register verify  
3. **drivers** — runtime state (`online`, location); created on admin approve (or first GET driver)  

They are not duplicates; each table has one row per user when the flow completes.

---

## Postman

Import `postman/tawseela-production.postman_collection.json` — request names include role suffix (`— public`, `— ADMIN`, etc.).
