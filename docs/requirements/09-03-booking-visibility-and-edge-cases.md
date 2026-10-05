### 9.3 Booking visibility and edge cases `[M2 to M4]`

#### UC-37: List my bookings (Customer)

```http
GET /v1/bookings?status=CONFIRMED&limit=20
Authorization: Bearer <customer token>
```

```json
{
  "items": [
    { "id": "b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08", "hotel_name": "Pine Hill Guesthouse",
      "check_in": "2026-11-10", "check_out": "2026-11-12", "status": "CONFIRMED",
      "total_price_minor": 1800000, "currency": "VND" }
  ],
  "next_cursor": null
}
```

Callers only ever see their own bookings. A `user_id` filter is rejected.

#### UC-38: List bookings of my hotel (Hotel owner, Admin)

```http
GET /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/bookings?from=2026-11-01&to=2026-11-30&status=CONFIRMED
Authorization: Bearer <owner token>
```

Owners see operational data only (dates, room, guest count, status, guest name). Not payment details. Used for check-in lists and room planning. Another owner's hotel returns 404.

#### UC-39: Hold has expired while the customer is paying (Customer)

- The customer opens a payment page after the 10-minute hold.
- `POST .../payments` returns 409 `HOLD_EXPIRED`. `GET /v1/bookings/{id}` shows `EXPIRED` (or `HELD` with a past `expires_at` if the worker has not yet run, which clients must treat as expired).
- The customer starts over with a new hold (UC-11). The old booking is never reused.
- Another customer may have taken the room meanwhile. That is a normal `ROOM_UNAVAILABLE`.

#### UC-40: Same customer holds overlapping stays (Customer)

Open question. Should one customer be allowed several simultaneous holds, on different rooms or even the same dates, and how many? **Proposed:** at most 3 active `HELD` bookings per user, to limit inventory hoarding. Exceeding returns 422 `HOLD_LIMIT_REACHED`. The limit is a configuration value.

#### UC-41: Hotel-local date edge (Customer, Owner)

Dates use the hotel's timezone, not the caller's or the server's.

- At 2026-10-05T17:30:00Z it is already 2026-10-06 00:30 in `Asia/Ho_Chi_Minh` (UTC+7).
- Booking `check_in=2026-10-05` is therefore rejected with 422 `INVALID_DATE_RANGE` (past local date), even though the UTC date is still the 5th.
- Cancellation cutoff, check-in and completion all use the same hotel-local date logic.

```http
HTTP/1.1 422 Unprocessable Entity

{ "error": { "code": "INVALID_DATE_RANGE", "message": "check_in is before the hotel's current local date.", "request_id": "..." } }
```

#### UC-42: Admin cancels on behalf (Admin)

Covers hotel closure, fraud or support cases. Uses the same endpoint as UC-13 with an admin token and a required reason. For a `CONFIRMED` booking a full refund obligation is created. The action is audited.

```http
POST /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08/cancel
Authorization: Bearer <admin token>
Content-Type: application/json

{ "reason": "Hotel closed due to flooding." }
```
