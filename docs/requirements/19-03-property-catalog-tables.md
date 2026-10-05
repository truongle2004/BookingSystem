### 19.3 Property catalog tables `[M1]`

#### `hotels`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `owner_id` | uuid | FK -> `users.id`, NOT NULL |
| `name` | varchar(200) | NOT NULL |
| `address` | varchar(500) | NOT NULL |
| `city` | varchar(120) | NOT NULL |
| `country_code` | char(2) | NOT NULL, default/product-configured `VN` initially |
| `timezone` | varchar(80) | NOT NULL, e.g. `Asia/Ho_Chi_Minh` |
| `hotel_type` | varchar(40) | NOT NULL |
| `location_type` | varchar(40) | nullable |
| `phone_number` | varchar(40) | nullable |
| `star_rating` | smallint | nullable, CHECK 1..5 |
| `description` | text | nullable |
| `breakfast_description` | text | nullable |
| `publication_status` | varchar(30) | NOT NULL |
| `latest_rejection_reason` | text | nullable |
| `submitted_at` | timestamptz | nullable |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

`publication_status` values:

- `DRAFT`;
- `PENDING_REVIEW`;
- `PUBLISHED`;
- `REJECTED`;
- `SUSPENDED`.

Indexes should support at least:

- owner dashboard: `(owner_id, publication_status, id)`;
- public search: `(publication_status, city, id)`;
- optional `hotel_type` / `star_rating` indexes added only after query plans justify them.

#### `room_types`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `hotel_id` | uuid | FK -> `hotels.id`, NOT NULL |
| `name` | varchar(160) | NOT NULL |
| `description` | text | nullable |
| `max_guests` | smallint | NOT NULL, CHECK > 0 |
| `bed_type` | varchar(40) | nullable |
| `bed_quantity` | smallint | nullable, CHECK > 0 when present |
| `room_size` | numeric(8,2) | nullable, CHECK > 0 when present |
| `base_price_minor` | bigint | temporary/simple-rate compatibility field, CHECK >= 0 |
| `currency` | char(3) | NOT NULL |
| `status` | varchar(20) | `ACTIVE` / `INACTIVE` |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Recommended unique constraint: `(hotel_id, name)` unless product later allows duplicate display names.

#### `rooms`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `hotel_id` | uuid | FK -> `hotels.id`, NOT NULL |
| `room_type_id` | uuid | NOT NULL |
| `room_number` | varchar(40) | NOT NULL |
| `floor` | integer | nullable |
| `operational_status` | varchar(30) | `ACTIVE` / `OUT_OF_SERVICE` |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Constraints:

- `UNIQUE(hotel_id, room_number)`;
- room and room type must belong to the same hotel.

Recommended relational enforcement:

```sql
ALTER TABLE room_types
    ADD CONSTRAINT uq_room_types_hotel_id_id UNIQUE (hotel_id, id);

ALTER TABLE rooms
    ADD CONSTRAINT fk_rooms_room_type_same_hotel
    FOREIGN KEY (hotel_id, room_type_id)
    REFERENCES room_types(hotel_id, id);
```

This composite FK prevents cross-hotel room-type linkage at the database layer.

#### `amenities`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `code` | varchar(80) | NOT NULL, UNIQUE |
| `name` | varchar(120) | NOT NULL |
| `category` | varchar(60) | NOT NULL |
| `active` | boolean | NOT NULL default true |

Join tables:

- `hotel_amenities(hotel_id, amenity_id)` PK `(hotel_id, amenity_id)`;
- `room_type_amenities(room_type_id, amenity_id)` PK `(room_type_id, amenity_id)`.

#### `hotel_images`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `hotel_id` | uuid | FK -> `hotels.id`, NOT NULL |
| `object_key` | varchar(500) | NOT NULL |
| `caption` | varchar(300) | nullable |
| `sort_order` | integer | NOT NULL default 0 |
| `is_cover` | boolean | NOT NULL default false |
| `created_at` | timestamptz | NOT NULL |

Recommended partial unique index to ensure one cover per hotel:

```sql
CREATE UNIQUE INDEX uq_hotel_images_one_cover
    ON hotel_images(hotel_id)
    WHERE is_cover = true;
```

#### `room_type_images`

Same structure as `hotel_images`, replacing `hotel_id` with `room_type_id`. Apply the same one-cover-per-room-type rule.
