## 5. Planned use cases: catalog and publication `[M1]`

### UC-04: Create a hotel draft (Hotel owner)

- **Precondition:** user holds `HOTEL_OWNER`.
- **Main flow:** system creates the hotel with `owner_id` taken from the authenticated user and status `DRAFT`.
- **Rule:** a client-supplied `owner_id` is a rejected unknown field.

```http
POST /v1/hotels HTTP/1.1
Authorization: Bearer <owner token>
Content-Type: application/json

{
  "name": "Pine Hill Guesthouse",
  "address": "12 Tran Hung Dao",
  "city": "Da Lat",
  "timezone": "Asia/Ho_Chi_Minh",
  "location_type": "MOUNTAIN",
  "hotel_type": "HOTEL",
  "phone_number": "+84 263 555 0100",
  "star_rating": 3,
  "description": "Quiet guesthouse near the lake.",
  "breakfast_description": "Vietnamese breakfast included."
}
```

```http
HTTP/1.1 201 Created
Location: /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61

{
  "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61",
  "name": "Pine Hill Guesthouse",
  "city": "Da Lat",
  "timezone": "Asia/Ho_Chi_Minh",
  "publication_status": "DRAFT",
  "created_at": "2026-10-05T09:30:00Z"
}
```

Rejected client-supplied owner:

```http
HTTP/1.1 400 Bad Request

{
  "error": {
    "code": "UNKNOWN_FIELD",
    "message": "Unknown field: owner_id",
    "request_id": "..."
  }
}
```

### UC-05: Add room type and physical rooms (Hotel owner)

- **Rule:** a room must belong to a room type of the same hotel. The hotel is taken from the path, never from the body.

```http
POST /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/room-types
Authorization: Bearer <owner token>
Content-Type: application/json

{
  "name": "Deluxe Double",
  "description": "Garden view.",
  "max_guests": 2,
  "bed_type": "DOUBLE",
  "bed_quantity": 1,
  "room_size": 24,
  "base_price_minor": 900000,
  "currency": "VND"
}
```

```json
{
  "id": "e7a2b9c4-3d1f-4e65-a8b0-1c9d4f6a2e77",
  "hotel_id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61",
  "name": "Deluxe Double",
  "max_guests": 2,
  "base_price_minor": 900000,
  "currency": "VND"
}
```

```http
POST /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/rooms
Authorization: Bearer <owner token>
Content-Type: application/json

{ "room_type_id": "e7a2b9c4-3d1f-4e65-a8b0-1c9d4f6a2e77", "room_number": "101", "floor": 1 }
```

```json
{
  "id": "4b6d8f0a-2c1e-4a73-9d5b-8e7f1a3c5d90",
  "hotel_id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61",
  "room_type_id": "e7a2b9c4-3d1f-4e65-a8b0-1c9d4f6a2e77",
  "room_number": "101",
  "floor": 1,
  "operational_status": "ACTIVE"
}
```

Linking a room to another hotel's room type returns 422 (and the database rejects it too via composite foreign key). A duplicate `room_number` in the same hotel returns 409.

### UC-06: Submit hotel for review (Hotel owner)

- **Precondition:** hotel is `DRAFT` (or `REJECTED` after returning to draft), has required catalog data, at least one active room type, and at least one active room.

```http
POST /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/submit
Authorization: Bearer <owner token>
```

```json
{ "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61", "publication_status": "PENDING_REVIEW" }
```

Incomplete hotel:

```http
HTTP/1.1 422 Unprocessable Entity

{
  "error": {
    "code": "HOTEL_INCOMPLETE",
    "message": "At least one active room type and one active room are required.",
    "request_id": "..."
  }
}
```

Another owner's hotel returns 404 (or 403). Pick one and apply it consistently.

### UC-07: Approve, reject, suspend a hotel (Admin)

```http
POST /v1/admin/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/approve
Authorization: Bearer <admin token>
```

```json
{ "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61", "publication_status": "PUBLISHED" }
```

```http
POST /v1/admin/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/reject
Content-Type: application/json

{ "reason": "Address could not be verified." }
```

```json
{ "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61", "publication_status": "REJECTED" }
```

`reject` and `suspend` require a non-empty `reason`. Every transition writes an audit log row (actor, previous status, next status, reason, timestamp). Owners cannot call these endpoints (403).

### UC-08: Browse and search hotels (Anonymous, Customer) `[M2]`

- **Rule:** only `PUBLISHED` hotels are returned. Draft, pending, rejected and suspended hotels are invisible.
- Search is availability-aware when dates are supplied. A hotel with no matching inventory is excluded unless the client explicitly requests unavailable properties for discovery-only UI.
- The canonical search inputs are destination, check-in, check-out, adults, children and requested room count.
- Filters and sort options are server-defined enums/parameters, not arbitrary SQL fields.

Basic discovery (dates omitted):

```http
GET /v1/hotels?destination=Da%20Lat&limit=20 HTTP/1.1
```

Availability-aware search:

```http
GET /v1/hotels/search?destination=Da%20Lat&check_in=2026-11-10&check_out=2026-11-12&adults=2&children=0&rooms=1&stars=3,4,5&amenities=WIFI,PARKING&free_cancellation=true&sort=PRICE_ASC&limit=20
```

```json
{
  "query": {
    "destination": "Da Lat",
    "check_in": "2026-11-10",
    "check_out": "2026-11-12",
    "adults": 2,
    "children": 0,
    "rooms": 1
  },
  "items": [
    {
      "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61",
      "name": "Pine Hill Guesthouse",
      "city": "Da Lat",
      "hotel_type": "HOTEL",
      "star_rating": 3,
      "review_score": 8.6,
      "from_price_minor": 900000,
      "currency": "VND",
      "available_room_types": 2,
      "free_cancellation_available": true,
      "cover_image_url": "https://example.invalid/hotels/pine-hill/cover.jpg"
    }
  ],
  "next_cursor": null
}
```

Supported filter families planned for M2:

- price range;
- star rating;
- review score;
- hotel/property type;
- amenities;
- breakfast included;
- free cancellation;
- bed type;
- room capacity;
- optional distance filter once geospatial coordinates are introduced.

Supported sort values planned for M2: `RECOMMENDED`, `PRICE_ASC`, `PRICE_DESC`, `REVIEW_SCORE_DESC`, `STAR_RATING_DESC`.

Search results are advisory. Reservation creation revalidates inventory and price under lock.

### UC-09: Edit a hotel (Hotel owner)

```http
PATCH /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61
Content-Type: application/json

{ "city": "Dalat" }
```

- On `DRAFT`: applied.
- On `PUBLISHED`, material field (see 2.1): applied and status becomes `PENDING_REVIEW` (hotel hidden, existing bookings kept).
- On `PUBLISHED`, minor field: applied, status unchanged.

---
