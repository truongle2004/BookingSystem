### 19.6 Reservation aggregate `[M3/M4]`

The target model is **not** `booking.room_id`. Use a reservation aggregate that owns one or more reservation items.

#### `reservations`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `user_id` | uuid | FK -> `users.id`, NOT NULL |
| `hotel_id` | uuid | FK -> `hotels.id`, NOT NULL |
| `status` | varchar(30) | NOT NULL |
| `currency` | char(3) | NOT NULL |
| `subtotal_minor` | bigint | NOT NULL, CHECK >= 0 |
| `discount_minor` | bigint | NOT NULL default 0, CHECK >= 0 |
| `tax_minor` | bigint | NOT NULL default 0, CHECK >= 0 |
| `fee_minor` | bigint | NOT NULL default 0, CHECK >= 0 |
| `total_price_minor` | bigint | NOT NULL, CHECK >= 0 |
| `price_breakdown_json` | jsonb | NOT NULL; immutable snapshot |
| `contact_email` | varchar(320) | nullable |
| `contact_phone` | varchar(40) | nullable |
| `expires_at` | timestamptz | nullable; required while held |
| `confirmed_at` | timestamptz | nullable |
| `cancelled_at` | timestamptz | nullable |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Initial reservation status values:

- `HELD`;
- `CONFIRMED`;
- `CHECKED_IN`;
- `COMPLETED`;
- `CANCELLED`;
- `EXPIRED`;
- `NO_SHOW`.

Price invariant:

```text
subtotal_minor - discount_minor + tax_minor + fee_minor = total_price_minor
```

Enforce with a CHECK constraint.

Indexes:

- `(user_id, status, created_at DESC, id)` for customer history;
- `(hotel_id, status, created_at DESC, id)` for property operations;
- index on `expires_at` filtered to `status = 'HELD'` for expiration worker.

#### `reservation_items`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `reservation_id` | uuid | FK -> `reservations.id`, NOT NULL |
| `room_id` | uuid | FK -> `rooms.id`, NOT NULL |
| `room_type_id` | uuid | FK -> `room_types.id`, NOT NULL |
| `rate_plan_id` | uuid | FK -> `rate_plans.id`, NOT NULL when rate plans are enabled |
| `check_in` | date | NOT NULL |
| `check_out` | date | NOT NULL |
| `guest_count` | smallint | NOT NULL, CHECK > 0 |
| `subtotal_minor` | bigint | NOT NULL, CHECK >= 0 |
| `total_price_minor` | bigint | NOT NULL, CHECK >= 0 |
| `rate_snapshot_json` | jsonb | NOT NULL |
| `cancellation_snapshot_json` | jsonb | NOT NULL |
| `created_at` | timestamptz | NOT NULL |

Constraints:

- `check_in < check_out`;
- each item belongs to the same `hotel_id` indirectly represented by its reservation, room, room type and rate plan. Application validation is required; migrations should additionally use composite FKs where practical;
- one reservation must contain at least one item. This aggregate invariant is enforced in application transaction logic because a simple row CHECK cannot enforce child existence.

The first implementation performs cancellation/state transition for the **whole reservation**. Selective per-item cancellation may be added later without changing the ownership relationship.

#### Physical-room overlap protection

`btree_gist` is enabled in `V1`. The database must reject overlapping active reservation items for the same physical room.

Recommended approach: copy the aggregate blocking state onto `reservation_items.booking_status` (maintained atomically with `reservations.status`) **or** maintain a dedicated room-occupancy table. For the initial implementation, use the denormalized item status because PostgreSQL exclusion predicates cannot directly join the parent table.

Add:

```text
reservation_items.booking_status varchar(30) NOT NULL
```

with the same state vocabulary as the aggregate for blocking states.

Then enforce:

```sql
ALTER TABLE reservation_items
ADD CONSTRAINT ex_reservation_items_no_room_overlap
EXCLUDE USING gist (
    room_id WITH =,
    daterange(check_in, check_out, '[)') WITH &&
)
WHERE (booking_status IN ('HELD', 'CONFIRMED', 'CHECKED_IN'));
```

`COMPLETED` does not need to block future dates because its stay range is already historical, but including it is harmless if state synchronization is simpler. `CANCELLED`, `EXPIRED` and `NO_SHOW` must not block inventory.

Application locking still remains required to provide deterministic behavior and clean error mapping. The exclusion constraint is the final integrity backstop.

#### `reservation_guests`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `reservation_item_id` | uuid | FK -> `reservation_items.id`, NOT NULL |
| `first_name` | varchar(120) | NOT NULL |
| `last_name` | varchar(120) | NOT NULL |
| `is_primary` | boolean | NOT NULL default false |
| `created_at` | timestamptz | NOT NULL |

At least one guest should be associated with each reservation item when guest details are required by the product flow. Authorization always belongs to `reservations.user_id`; guests do not gain account permissions.

#### `special_requests`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `reservation_id` | uuid | FK -> `reservations.id`, NOT NULL |
| `reservation_item_id` | uuid | nullable FK -> `reservation_items.id` |
| `request_text` | text | NOT NULL |
| `status` | varchar(30) | `PENDING`, `ACKNOWLEDGED`, `DECLINED`, `FULFILLED` |
| `owner_response` | text | nullable |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |
