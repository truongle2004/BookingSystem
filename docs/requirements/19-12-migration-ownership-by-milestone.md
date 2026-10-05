### 19.12 Migration ownership by milestone

| Milestone | Database objects expected |
|---|---|
| M0 | `btree_gist` extension only |
| M1 | users, roles, owner requests, hotels, room types, rooms, amenities, property images, audit foundation |
| M2 | saved hotels and search-supporting indexes; no duplicated search-result tables required initially |
| M3 | rate plans/daily rates if pricing ships here; reservations, reservation items, guests, special requests, overlap constraint, idempotency |
| M4 | payments, payment events, refunds, finalized price/cancellation snapshots |
| M5 | reviews, reactions, questions, answers |
| M6 | conversations, messages, notification outbox |
| M7 | usually no mandatory new financial tables for receipt/calendar reads; add only if a concrete UC requires them |
| M8 | promotions/loyalty schema remains intentionally **not locked** until stacking and eligibility rules are approved |
