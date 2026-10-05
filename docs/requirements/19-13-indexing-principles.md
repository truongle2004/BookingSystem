### 19.13 Indexing principles

Do not index every column preemptively.

Create indexes for:

1. primary/unique constraints;
2. foreign keys that participate in joins/deletes;
3. known list/search access paths from approved UCs;
4. expiration/outbox/refund workers;
5. exclusion integrity required for availability.

Initial important access paths:

```text
hotels(publication_status, city, id)
hotels(owner_id, publication_status, id)
rooms(hotel_id, room_type_id)
rate_plans(room_type_id, status)
daily_rates(rate_plan_id, stay_date)
reservations(user_id, status, created_at, id)
reservations(hotel_id, status, created_at, id)
reservation_items(reservation_id)
reservation_items(room_id)
payments(reservation_id, created_at)
refunds(status, next_retry_at)
reviews(hotel_id, created_at, id)
messages(conversation_id, created_at, id)
notification_outbox(status, next_attempt_at, created_at)
audit_logs(entity_type, entity_id, created_at, id)
```

Advanced search indexes (price, stars, amenities, review score, text/location ranking) should be added after the actual SQL search shape is implemented and inspected with `EXPLAIN (ANALYZE, BUFFERS)`. Do not add speculative indexes solely because a field is filterable.
