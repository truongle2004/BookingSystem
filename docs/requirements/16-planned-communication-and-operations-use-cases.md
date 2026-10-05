## 16. Planned communication and operations use cases `[M6 to M8]`

### UC-67: Customer-property messaging (Customer, Hotel owner) `[M6]`

A private conversation is scoped to a reservation. This is separate from public Q&A.

```http
POST /v1/reservations/{reservation_id}/messages

{ "message": "Can I check in after 22:00?" }
```

Only the reservation owner, the owning property's authorized staff/owner, and admin/support roles may read the thread.

### UC-68: Send reservation notifications (System) `[M6]`

Notification events include:

- reservation held;
- payment succeeded/failed;
- reservation confirmed;
- reservation modified;
- reservation cancelled;
- refund completed/failed;
- upcoming-stay reminder;
- property replied to a message/special request.

Use a transactional outbox so the database state change and notification intent cannot diverge. Delivery may be email first; push is optional later.

### UC-69: Generate invoice/receipt data (Customer, Admin) `[M7]`

```http
GET /v1/reservations/{reservation_id}/receipt
```

Returns immutable financial facts already stored by the reservation/payment snapshots. PDF generation is a presentation concern and may be added later.

### UC-70: Owner reservation calendar / occupancy view (Hotel owner) `[M7]`

Owners can query room occupancy for operational planning without seeing customer payment details.

```http
GET /v1/hotels/{hotel_id}/calendar?from=2026-11-01&to=2026-11-30
```

The response exposes room/date occupancy states and reservation references needed for operations.

### UC-71: Create a promotion (Hotel owner) `[M8 optional]`

Initial promotion types may include:

- member discount;
- early-bird discount;
- last-minute discount;
- long-stay discount.

Promotions are evaluated by the quote service and snapshotted into the reservation price breakdown. Stacking rules must be explicit.

### UC-72: Lightweight loyalty level (Customer, System) `[M8 optional]`

A small member program may grant percentage discounts after configured completed-stay thresholds. It is intentionally simpler than Booking.com's full Genius program and must not be required for the core MVP.

---
