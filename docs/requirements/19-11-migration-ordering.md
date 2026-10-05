### 19.11 Migration ordering

Flyway migrations should be small enough to review but grouped by coherent domain dependency. Recommended baseline:

```text
V1__enable_extensions.sql

V2__create_users_and_roles.sql
V3__create_owner_requests.sql

V4__create_hotels.sql
V5__create_room_types_and_rooms.sql
V6__create_amenities.sql
V7__create_property_images.sql

V8__create_rate_plans.sql
V9__create_daily_rates.sql
V10__create_saved_hotels.sql

V11__create_reservations.sql
V12__create_reservation_items.sql
V13__add_room_overlap_constraint.sql
V14__create_reservation_guests_and_special_requests.sql
V15__create_idempotency_records.sql

V16__create_payments_and_events.sql
V17__create_refunds.sql

V18__create_reviews_and_reactions.sql
V19__create_questions_and_answers.sql

V20__create_conversations_and_messages.sql
V21__create_notification_outbox.sql
V22__create_audit_logs.sql
```

Exact version numbers may change if development introduces migrations earlier. **Never rename or modify a migration that has already been applied to a shared/prod-like database.** Add a new forward migration instead.
