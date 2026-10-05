## 18. Revised product-owner decisions

| # | Decision | Proposed default |
|---|---|---|
| 13 | Reservation model | `Reservation` aggregate with 1..N `ReservationItem`; keep one-room compatibility endpoint initially |
| 14 | Physical room assignment | Hold a concrete room in the first implementation to preserve current concurrency model; consider room-type inventory pools only as a later redesign |
| 15 | Rate plans | Required before advanced pricing/payment milestone |
| 16 | Cancellation | Snapshotted per rate plan; no global full-refund assumption after M4 |
| 17 | Taxes/fees | Server-calculated components in quote/price snapshot; exact Vietnam tax/business rules are product configuration, not hard-coded domain assumptions |
| 18 | Search endpoint | `/v1/hotels/search` for availability-aware search; keep `/v1/hotels` for simple catalog browsing if useful |
| 19 | Images | Store object key/metadata in DB; binary storage is external object storage |
| 20 | Notifications | Transactional outbox + async worker in M6 |
| 21 | Messaging | Private reservation-scoped thread, separate from public hotel Q&A |
| 22 | Loyalty/promotions | Optional M8; must not complicate M0-M5 core delivery |

All example IDs, tokens and timestamps are illustrative.

---
