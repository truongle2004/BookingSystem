## 13. Product expansion roadmap `[M2 to M8]`

This section upgrades the product from a one-room booking engine into a focused accommodation marketplace. Existing UCs remain valid where they do not conflict with the target model below.

### 13.1 Milestone map

| Milestone | Theme | Main capabilities |
|---|---|---|
| M0 | Platform foundation | Auth validation, health, config, DB, migrations, CI, logs |
| M1 | Property onboarding | Users/roles, owner approval, hotel/room catalog, amenities, publication, images |
| M2 | Discovery and inventory | Advanced search, filters/sort, availability, saved hotels, multi-room quote |
| M3 | Reservation | Reservation aggregate, guest details, reservation items, holds, special requests, expiry worker |
| M4 | Pricing and payment | Rate plans, daily prices, cancellation policy, taxes/fees, promotions, payment/refund |
| M5 | Stay lifecycle and trust | Modify/cancel reservation, no-show, check-in/complete, reviews, Q&A |
| M6 | Communication | Booking notifications, email/outbox, customer-property messaging |
| M7 | Operations | Owner calendar/inventory views, operational reporting, invoice support |
| M8 | Growth (optional) | Member discounts / lightweight loyalty; no flight/car/attraction expansion |

### 13.2 Target domain model

The target reservation model is:

```text
Hotel
  -> RoomType
      -> Room
      -> RatePlan
          -> DailyRate / pricing rules
          -> CancellationPolicy

Reservation
  -> ReservationItem (1..N)
      -> RoomType
      -> assigned Room (nullable until assignment strategy chooses one)
      -> dates
      -> guest allocation
      -> rate-plan snapshot
      -> price snapshot
  -> Guests
  -> Payments
  -> Refunds
  -> SpecialRequests
  -> Conversation
```

**Compatibility rule:** early M2/M3 endpoints may continue accepting a single-room request, but internally they should create one `Reservation` with one `ReservationItem`. Do not design a database invariant that prevents multiple reservation items later.

---
