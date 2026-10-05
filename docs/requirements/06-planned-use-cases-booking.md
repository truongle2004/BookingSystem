## 6. Planned use cases: booking `[M2]`

### 6.1 Rules recap

- Stay is `[check_in, check_out)` in the hotel's local calendar dates. Checkout day equals next check-in day is allowed.
- `check_in < check_out`. `check_in` not before the hotel's current local date.
- `1 <= guest_count <= room_type.max_guests`.
- Price is `base_price_minor × nights`, snapshotted on the booking.
- Hold lasts 10 minutes (`HOLD_DURATION`). `expires_at` is an absolute UTC instant from the database clock.
- Blocking statuses: `HELD`, `CONFIRMED`, `CHECKED_IN`, `COMPLETED`.

### UC-10: Check availability (Anonymous, Customer)

Advisory only. Booking creation revalidates.

```http
GET /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/availability?check_in=2026-11-10&check_out=2026-11-12&guests=2
```

```json
{
  "hotel_id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61",
  "check_in": "2026-11-10",
  "check_out": "2026-11-12",
  "nights": 2,
  "room_types": [
    {
      "room_type_id": "e7a2b9c4-3d1f-4e65-a8b0-1c9d4f6a2e77",
      "name": "Deluxe Double",
      "available_rooms": 2,
      "total_price_minor": 1800000,
      "currency": "VND"
    }
  ]
}
```

Invalid range:

```http
HTTP/1.1 422 Unprocessable Entity

{ "error": { "code": "INVALID_DATE_RANGE", "message": "check_out must be after check_in.", "request_id": "..." } }
```

### UC-11: Hold a room (Customer)

- **Precondition:** hotel `PUBLISHED`, room `ACTIVE`, customer is not the hotel's owner.
- **Main flow:** system locks the room row, expires stale holds for that room, checks overlap, inserts a `HELD` booking with price snapshot and expiry. User identity comes from the token.
- **Idempotency:** `Idempotency-Key` is required. Same key and same body returns the original result. Same key with a different body returns 409 `IDEMPOTENCY_CONFLICT`.

```http
POST /v1/bookings HTTP/1.1
Authorization: Bearer <customer token>
Idempotency-Key: 6e0f2b4a-8c1d-4e7f-9a3b-5d2c1e0f8a46
Content-Type: application/json

{
  "room_id": "4b6d8f0a-2c1e-4a73-9d5b-8e7f1a3c5d90",
  "check_in": "2026-11-10",
  "check_out": "2026-11-12",
  "guest_count": 2
}
```

```http
HTTP/1.1 201 Created
Location: /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08

{
  "id": "b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08",
  "hotel_id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61",
  "room_id": "4b6d8f0a-2c1e-4a73-9d5b-8e7f1a3c5d90",
  "check_in": "2026-11-10",
  "check_out": "2026-11-12",
  "guest_count": 2,
  "status": "HELD",
  "total_price_minor": 1800000,
  "currency": "VND",
  "expires_at": "2026-10-05T10:10:00Z",
  "created_at": "2026-10-05T10:00:00Z"
}
```

A client-supplied `hotel_id`, `user_id` or `total_price_minor` is rejected as an unknown field.

Failures:

| Situation | Response |
|---|---|
| Overlapping blocking booking exists | 409 `ROOM_UNAVAILABLE` |
| Room `OUT_OF_SERVICE` | 409 `ROOM_UNAVAILABLE` |
| Guests exceed `max_guests` | 422 `CAPACITY_EXCEEDED` |
| Hotel not `PUBLISHED` | 422 `HOTEL_NOT_BOOKABLE` |
| Same key, different body | 409 `IDEMPOTENCY_CONFLICT` |
| Missing `Idempotency-Key` | 400 `MALFORMED_REQUEST` |

```http
HTTP/1.1 409 Conflict

{
  "error": {
    "code": "ROOM_UNAVAILABLE",
    "message": "The room is not available for the requested dates.",
    "request_id": "..."
  }
}
```

Concurrency expectation: 100 simultaneous requests for one room and the same dates produce exactly one `HELD` booking and no persisted overlap.

### UC-12: View a booking (Customer, Hotel owner, Admin)

```http
GET /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08
```

- Customer: only own bookings.
- Hotel owner: only bookings of own hotels.
- Admin: any.
- Anyone else: 404.

Response has the same shape as UC-11.

### UC-13: Cancel a booking (Customer, Admin)

```http
POST /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08/cancel
Authorization: Bearer <customer token>
```

```json
{ "id": "b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08", "status": "CANCELLED" }
```

- `HELD`: cancelled, no refund.
- `CONFIRMED` before local check-in date: cancelled and a full refund obligation is recorded in the same transaction `[M4]`.
- Otherwise: 409 `INVALID_STATE_TRANSITION`.

### UC-14: Hold expires (System) `[M2 cleanup, M3 worker]`

- On any hold request for a room, stale `HELD` rows for that room are moved to `EXPIRED` under the room lock, so a stopped worker never blocks a legitimate booking.
- From M3 a bounded polling worker does the same in batches, using the same room-first lock order.

---
