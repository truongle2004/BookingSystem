## 4. Planned use cases: identity and owner onboarding `[M1]`

Do not implement in M0.

### UC-01: Sign in and lazy user creation (Customer)

- **Precondition:** valid Clerk token.
- **Main flow:** system reads `sub` from the token. If no local `users` row exists, it creates one with role `CUSTOMER` and minimal profile data. Identity always comes from the token, never from a request field or header.
- **Alternate:** user status is `BLOCKED`. System returns 403 `USER_BLOCKED`.

```http
GET /v1/me HTTP/1.1
Authorization: Bearer <token>
```

```json
{
  "id": "0f8b6c5e-3d7a-4f21-9a0c-6e1d2b3c4a5f",
  "username": "an.nguyen",
  "email": "an@example.com",
  "roles": ["CUSTOMER"],
  "status": "ACTIVE"
}
```

`GET /v1/me` is not in the original endpoint list. It is a proposed addition for M1. Remove it if not wanted.

### UC-02: Request hotel owner role (Customer)

- **Precondition:** user has no pending or approved owner request.
- **Main flow:** user submits a request. System stores it as `PENDING`. An admin reviews it.
- **Alternate:** an active request exists. System returns 409.

```http
POST /v1/owner-requests HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{ "reason": "I run a 12-room guesthouse in Da Lat." }
```

```http
HTTP/1.1 201 Created
Location: /v1/owner-requests/9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33

{
  "id": "9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33",
  "status": "PENDING",
  "reason": "I run a 12-room guesthouse in Da Lat.",
  "created_at": "2026-10-05T08:30:00Z"
}
```

Check status:

```http
GET /v1/owner-requests/9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33
```

Only the requester or an admin may read it. Anyone else gets 404.

### UC-03: Review an owner request (Admin)

The original endpoint list has no admin endpoint for this. Proposed: `POST /v1/admin/owner-requests/{id}/approve` and `/reject`. Approval grants `HOTEL_OWNER` and writes an audit log entry.

```http
POST /v1/admin/owner-requests/9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33/approve
Authorization: Bearer <admin token>
Content-Type: application/json

{ "reason": "Documents verified." }
```

```json
{
  "id": "9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33",
  "status": "APPROVED",
  "reviewed_at": "2026-10-05T09:10:00Z"
}
```

A non-admin receives 403 `FORBIDDEN`.

---
