## 15. Planned pricing and reservation use cases `[M3 to M5]`

### UC-58: Manage rate plans (Hotel owner) `[M4]`

A room type may have multiple sellable plans, for example:

- `FLEXIBLE` - higher price, free cancellation until a deadline;
- `NON_REFUNDABLE` - lower price, no refund after confirmation;
- `BREAKFAST_INCLUDED` - includes breakfast benefit and may have its own price.

```http
POST /v1/hotels/{hotel_id}/room-types/{room_type_id}/rate-plans
Content-Type: application/json

{
  "name": "Flexible with breakfast",
  "code": "FLEX_BREAKFAST",
  "breakfast_included": true,
  "cancellation_policy": {
    "type": "FREE_UNTIL_DEADLINE",
    "hours_before_check_in": 48,
    "late_fee_percent": 100
  }
}
```

Rate-plan edits affect future quotes only. Existing holds/confirmed reservations use snapshots.

### UC-59: Manage nightly prices (Hotel owner) `[M4]`

```http
PUT /v1/hotels/{hotel_id}/room-types/{room_type_id}/rate-plans/{rate_plan_id}/prices
Content-Type: application/json

{
  "dates": [
    { "date": "2026-12-24", "price_minor": 1500000 },
    { "date": "2026-12-25", "price_minor": 1700000 }
  ],
  "currency": "VND"
}
```

The quote service falls back to the plan/base price only when no date override exists. Past reservation snapshots never change.

### UC-60: Quote a reservation with price breakdown (Anonymous, Customer) `[M3/M4]`

```http
POST /v1/reservation-quotes
Content-Type: application/json

{
  "hotel_id": "...",
  "check_in": "2026-11-10",
  "check_out": "2026-11-12",
  "rooms": [
    { "room_type_id": "...", "rate_plan_id": "...", "adults": 2, "children": 0 }
  ]
}
```

```json
{
  "quote_id": "...",
  "expires_at": "2026-10-05T10:10:00Z",
  "items": [
    {
      "room_type_id": "...",
      "rate_plan_id": "...",
      "nightly_prices": [900000, 900000],
      "subtotal_minor": 1800000
    }
  ],
  "discount_minor": 0,
  "tax_minor": 100000,
  "fee_minor": 0,
  "total_minor": 1900000,
  "currency": "VND"
}
```

The quote is advisory until held, but its server-calculated breakdown is the only valid basis for reservation creation.

### UC-61: Provide guest and contact details (Customer) `[M3]`

The booking account and staying guests are distinct concepts.

```json
{
  "contact": {
    "full_name": "Thomas Le",
    "email": "thomas@example.com",
    "phone": "+84901234567"
  },
  "guests": [
    { "first_name": "Van A", "last_name": "Nguyen", "type": "ADULT" },
    { "first_name": "Van B", "last_name": "Nguyen", "type": "ADULT" }
  ]
}
```

Identity/ownership still comes from the authenticated user. Guest names are reservation data and never grant access.

### UC-62: Hold a multi-room reservation (Customer) `[M3]`

```http
POST /v1/reservations
Authorization: Bearer <customer token>
Idempotency-Key: <key>
Content-Type: application/json

{
  "quote_id": "...",
  "guest_details": { "...": "..." },
  "items": [
    { "room_type_id": "rt-1", "rate_plan_id": "rp-1", "adults": 2, "children": 0 },
    { "room_type_id": "rt-2", "rate_plan_id": "rp-3", "adults": 1, "children": 1 }
  ]
}
```

The transaction must either hold every requested item or none of them. Lock ordering must be deterministic to prevent deadlocks. The existing one-room UC-11 becomes the single-item compatibility case.

### UC-63: Add special requests (Customer) `[M3]`

```http
POST /v1/reservations/{reservation_id}/special-requests

{ "type": "LATE_ARRIVAL", "message": "Expected arrival around 23:00." }
```

Special requests are requests, not guarantees. Owners may mark them `ACKNOWLEDGED`, `ACCEPTED` or `DECLINED`. They do not change price unless a future paid add-on feature explicitly says so.

### UC-64: Modify a confirmed reservation (Customer, Admin) `[M5]`

Supported changes in the first version:

- guest/contact details;
- check-in/check-out dates;
- room type / rate plan where inventory exists;
- add/remove a reservation item before the modification cutoff.

Modification flow:

```text
load reservation
  -> validate modification policy
  -> lock affected inventory
  -> re-check availability
  -> re-price using current offers
  -> calculate price difference
  -> collect extra payment OR create refund obligation
  -> atomically replace reservation items/snapshots
```

A modification never silently changes an existing confirmed price without recording the before/after amount.

### UC-65: Apply cancellation policy (Customer, Admin) `[M4/M5]`

Cancellation uses the rate-plan snapshot for each reservation item.

Supported policy types:

- `FREE_UNTIL_DEADLINE`;
- `NON_REFUNDABLE`;
- `PERCENTAGE_FEE`;
- `FIRST_NIGHT_FEE` (optional later within M5).

For multi-room reservations, the user may cancel the entire reservation or selected items if product rules allow it. Refund obligations are created atomically with the cancellation transition.

### UC-66: Mark no-show (Hotel owner, Admin) `[M5]`

```http
POST /v1/reservations/{id}/no-show
Authorization: Bearer <owner token>
```

Allowed only after the local check-in cutoff defined for the property. The transition is audited and may trigger a policy-defined charge/refund outcome.

---
