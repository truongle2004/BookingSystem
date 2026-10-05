## 7. Planned use cases: payment and lifecycle `[M4]`

### 7.1 Booking state machine

| From | To | Condition |
|---|---|---|
| (new) | `HELD` | Room active, dates and capacity valid, no overlap |
| `HELD` | `CONFIRMED` | Valid successful payment and `expires_at > db now` |
| `HELD` | `EXPIRED` | `expires_at <= db now` |
| `HELD` | `CANCELLED` | Owner of booking or admin |
| `CONFIRMED` | `CANCELLED` | Before local check-in date |
| `CONFIRMED` | `CHECKED_IN` | Hotel owner or admin, local date within stay |
| `CHECKED_IN` | `COMPLETED` | Hotel owner or admin, on or after local check-out date |

`EXPIRED`, `CANCELLED`, `COMPLETED` are terminal.

### UC-15: Start a payment (Customer)

- **Precondition:** booking is owned by caller, `HELD`, not expired.
- **Main flow:** amount and currency are read from the server record, never from the client. Multiple attempts per booking are allowed. `Idempotency-Key` is required.

```http
POST /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08/payments
Authorization: Bearer <customer token>
Idempotency-Key: 2a9c4e6b-1d3f-4a85-b7c0-9e8d5f2a1b34
```

```http
HTTP/1.1 201 Created

{
  "id": "d2e4f6a8-0b1c-4d3e-8f5a-7b9c1d3e5f70",
  "booking_id": "b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08",
  "amount_minor": 1800000,
  "currency": "VND",
  "provider": "fake",
  "provider_transaction_id": "fake_txn_000123",
  "status": "PENDING",
  "created_at": "2026-10-05T10:02:00Z"
}
```

Expired hold: 409 `HOLD_EXPIRED`.

### UC-16: Receive payment callback (Fake provider)

- **Authenticity:** HMAC-SHA256 of the raw body using a configured development secret, sent in a signature header. Header name `X-Fake-Signature` is a proposed default. The endpoint is not an open public endpoint.
- **Deduplication:** unique `provider + provider_event_id`. Dedup record and local changes commit in one transaction.

```http
POST /v1/webhooks/payments/fake HTTP/1.1
X-Fake-Signature: sha256=3b1f...e9a0
Content-Type: application/json

{
  "event_id": "evt_0001",
  "event_type": "payment.succeeded",
  "provider_transaction_id": "fake_txn_000123",
  "amount_minor": 1800000,
  "currency": "VND"
}
```

```http
HTTP/1.1 200 OK

{ "status": "PROCESSED" }
```

Outcomes:

| Situation | Result |
|---|---|
| Valid, hold unexpired | Payment `SUCCEEDED`, booking `CONFIRMED` |
| Same event delivered again | 200, no repeated side effects |
| Bad signature or malformed body | 401 or 400, nothing recorded |
| Amount or currency mismatch | Rejected, nothing confirmed |
| Success at or after `expires_at`, or booking cancelled | Payment recorded `SUCCEEDED`, booking not resurrected, refund obligation recorded |
| Second successful attempt for a confirmed booking | Payment recorded, refund obligation recorded |
| Processing transaction fails | Non-2xx so the provider retries. Retry must succeed. |

### UC-17: Check in and complete (Hotel owner, Admin)

```http
POST /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08/check-in
Authorization: Bearer <owner token>
```

```json
{ "id": "b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08", "status": "CHECKED_IN" }
```

```http
POST /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08/complete
Authorization: Bearer <owner token>
```

```json
{ "id": "b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08", "status": "COMPLETED" }
```

Check-in outside the stay dates (hotel local date) or complete before local check-out date returns 409 `INVALID_STATE_TRANSITION`. An owner of a different hotel gets 404.

### UC-18: Execute refund (System)

Refund obligations are rows created atomically with cancellation or late success. A separate retryable job executes them against the fake provider. Failures must be visible (logged and left pending), never silent.

---
