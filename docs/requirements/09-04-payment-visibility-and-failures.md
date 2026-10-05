### 9.4 Payment visibility and failures `[M4]`

#### UC-43: View payment attempts for a booking (Customer, Admin)

```http
GET /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08/payments
```

```json
{
  "items": [
    { "id": "d2e4f6a8-0b1c-4d3e-8f5a-7b9c1d3e5f70", "amount_minor": 1800000, "currency": "VND",
      "status": "SUCCEEDED", "created_at": "2026-10-05T10:02:00Z" }
  ]
}
```

Owners do not see payment records.

#### UC-44: Payment fails or is abandoned (Customer, Fake provider)

```http
POST /v1/webhooks/payments/fake
X-Fake-Signature: sha256=...

{ "event_id": "evt_0002", "event_type": "payment.failed", "provider_transaction_id": "fake_txn_000124",
  "amount_minor": 1800000, "currency": "VND" }
```

- The payment becomes `FAILED`. The booking stays `HELD` until expiry, so the customer may start another attempt (UC-15).
- No refund obligation (nothing was charged).
- An attempt that never receives any callback stays `PENDING`. When the booking expires or is cancelled these attempts are closed as `CANCELLED` (proposed) and any late success follows UC-16 rules.

Payment statuses: `PENDING`, `SUCCEEDED`, `FAILED`, `CANCELLED`.

#### UC-45: Check refund status (Customer, Admin)

```http
GET /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08/refunds
```

```json
{
  "items": [
    { "id": "77aa88bb-99cc-4dd0-8eef-001122334455", "payment_id": "d2e4f6a8-0b1c-4d3e-8f5a-7b9c1d3e5f70",
      "amount_minor": 1800000, "currency": "VND", "reason": "BOOKING_CANCELLED",
      "status": "PENDING", "attempts": 0 }
  ]
}
```

Refund statuses: `PENDING`, `COMPLETED`, `FAILED`. Reasons: `BOOKING_CANCELLED`, `LATE_PAYMENT`, `DUPLICATE_PAYMENT`.

#### UC-46: Retry a failed refund (Admin, System)

The system retries automatically with bounded attempts. After the limit the refund becomes `FAILED` and appears in an admin queue. A refund must never disappear silently.

```http
POST /v1/admin/refunds/77aa88bb-99cc-4dd0-8eef-001122334455/retry
```

```json
{ "id": "77aa88bb-99cc-4dd0-8eef-001122334455", "status": "PENDING" }
```
