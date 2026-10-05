### 9.1 Identity and account `[M1]`

#### UC-23: Update own profile (Customer, Owner)

Only optional profile fields can be changed. `clerk_user_id`, roles and status cannot.

```http
PATCH /v1/me
Authorization: Bearer <token>
Content-Type: application/json

{ "username": "an.nguyen", "address": "5 Le Loi, Ho Chi Minh City", "description": "Frequent traveller" }
```

```json
{ "id": "0f8b6c5e-3d7a-4f21-9a0c-6e1d2b3c4a5f", "username": "an.nguyen", "roles": ["CUSTOMER"], "status": "ACTIVE" }
```

Sending `roles` or `status` returns 400 `UNKNOWN_FIELD`.

#### UC-24: Block and unblock a user (Admin)

- **Effect:** a `BLOCKED` user's valid Clerk token is rejected with 403 `USER_BLOCKED` on the next request. Existing bookings are not cancelled automatically.
- **Audit:** actor, previous and next status, reason.

```http
POST /v1/admin/users/0f8b6c5e-3d7a-4f21-9a0c-6e1d2b3c4a5f/block
Content-Type: application/json

{ "reason": "Repeated fraudulent chargebacks." }
```

```json
{ "id": "0f8b6c5e-3d7a-4f21-9a0c-6e1d2b3c4a5f", "status": "BLOCKED" }
```

`POST .../unblock` reverses it, also with a reason. An admin cannot block themselves (422).

#### UC-25: Withdraw an owner request (Customer)

```http
POST /v1/owner-requests/9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33/withdraw
```

```json
{ "id": "9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33", "status": "WITHDRAWN" }
```

Only while `PENDING`. This frees the "one active request per user" slot. After `REJECTED` or `WITHDRAWN` the user may file a new request. The status list becomes `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`.

#### UC-26: Review queue for owner requests (Admin)

```http
GET /v1/admin/owner-requests?status=PENDING&limit=20
```

```json
{
  "items": [
    { "id": "9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33", "user_id": "0f8b6c5e-3d7a-4f21-9a0c-6e1d2b3c4a5f",
      "status": "PENDING", "reason": "I run a 12-room guesthouse in Da Lat.", "created_at": "2026-10-05T08:30:00Z" }
  ],
  "next_cursor": null
}
```

#### UC-27: Reject an owner request, or revoke the owner role (Admin)

```http
POST /v1/admin/owner-requests/9d3a7e52-1c4b-4a8f-b6e0-5f2c8d1a7b33/reject
Content-Type: application/json

{ "reason": "Business registration could not be verified." }
```

```http
POST /v1/admin/users/0f8b6c5e-3d7a-4f21-9a0c-6e1d2b3c4a5f/revoke-owner
Content-Type: application/json

{ "reason": "Policy violation." }
```

Revoking the role must not leave hotels ownerless and bookable. **Proposed rule:** all of that user's `PUBLISHED` hotels move to `SUSPENDED` in the same transaction (existing bookings preserved). The reason is audited.
