### 9.2 Catalog management `[M1]`

#### UC-28: List my hotels (Hotel owner)

Owners must see their own drafts, pending and rejected hotels, which public search hides.

```http
GET /v1/owner/hotels?status=DRAFT&limit=20
Authorization: Bearer <owner token>
```

```json
{
  "items": [
    { "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61", "name": "Pine Hill Guesthouse",
      "publication_status": "DRAFT", "rejection_reason": null }
  ],
  "next_cursor": null
}
```

#### UC-29: View a hotel (Anonymous, Owner, Admin)

`GET /v1/hotels/{id}` behaves by caller:

| Caller | Hotel status | Result |
|---|---|---|
| Anyone | `PUBLISHED` | 200 with public data, amenities and room types |
| Owner of the hotel, Admin | Any | 200, includes `publication_status` and latest `rejection_reason` |
| Anyone else | Not `PUBLISHED` | 404 (never reveal that it exists) |

```http
GET /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61
```

```json
{
  "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61",
  "name": "Pine Hill Guesthouse",
  "city": "Da Lat",
  "timezone": "Asia/Ho_Chi_Minh",
  "star_rating": 3,
  "amenities": [ { "id": "a-wifi", "name": "Free Wi-Fi", "category": "CONNECTIVITY" } ],
  "room_types": [
    { "id": "e7a2b9c4-3d1f-4e65-a8b0-1c9d4f6a2e77", "name": "Deluxe Double", "max_guests": 2,
      "base_price_minor": 900000, "currency": "VND" }
  ]
}
```

#### UC-30: Edit or deactivate a room type (Hotel owner)

```http
PATCH /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/room-types/e7a2b9c4-3d1f-4e65-a8b0-1c9d4f6a2e77
Content-Type: application/json

{ "base_price_minor": 1000000 }
```

- A price change affects future bookings only. Existing booking totals never change.
- Lowering `max_guests` does not alter existing bookings.
- Deactivating the last active room type on a `PUBLISHED` hotel is a material edit, so the hotel returns to `PENDING_REVIEW` (see 2.1). The room type needs an `active` flag or similar. Proposed field: `status` `ACTIVE`/`INACTIVE`.

#### UC-31: Take a room out of service and back (Hotel owner)

```http
PATCH /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/rooms/4b6d8f0a-2c1e-4a73-9d5b-8e7f1a3c5d90
Content-Type: application/json

{ "operational_status": "OUT_OF_SERVICE" }
```

```json
{ "id": "4b6d8f0a-2c1e-4a73-9d5b-8e7f1a3c5d90", "operational_status": "OUT_OF_SERVICE",
  "affected_bookings": 2 }
```

- New holds are rejected (`ROOM_UNAVAILABLE`). Existing bookings are kept.
- The response lists how many future bookings are affected, so the owner can act. What the owner does about them is an open question (Section 12, item 9).

#### UC-32: Manage amenities (Hotel owner, Admin)

Amenities form a shared catalog. Admin maintains it. Owners attach existing amenities.

```http
GET /v1/amenities?category=ROOM
```

```http
PUT /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/amenities
Content-Type: application/json

{ "amenity_ids": ["a-wifi", "a-parking"] }
```

```json
{ "hotel_id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61", "amenity_ids": ["a-wifi", "a-parking"] }
```

`PUT` replaces the set. An unknown amenity ID returns 422. Same pattern at `/v1/hotels/{id}/room-types/{id}/amenities`. Amenity changes are minor edits.

#### UC-33: Fix a rejected listing and resubmit (Hotel owner)

`REJECTED` to `DRAFT` is a state transition in the BO table, but no endpoint exists. Proposed: editing a rejected hotel with `PATCH` moves it to `DRAFT` automatically. Then `submit` as in UC-06. The latest rejection reason is shown in UC-29 and UC-28.

```json
{ "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61", "publication_status": "DRAFT" }
```

#### UC-34: Review queue for hotels (Admin)

```http
GET /v1/admin/hotels?status=PENDING_REVIEW&limit=20
```

```json
{
  "items": [
    { "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61", "name": "Pine Hill Guesthouse", "owner_id": "0f8b6c5e-3d7a-4f21-9a0c-6e1d2b3c4a5f",
      "publication_status": "PENDING_REVIEW", "submitted_at": "2026-10-05T10:00:00Z" }
  ],
  "next_cursor": null
}
```

#### UC-35: Reinstate a suspended hotel (Admin)

```http
POST /v1/admin/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/reinstate
Content-Type: application/json

{ "reason": "Issue resolved." }
```

```json
{ "id": "c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61", "publication_status": "PUBLISHED" }
```

`SUSPENDED` to `PUBLISHED` is in the BO table but had no endpoint.

#### UC-36: View audit history (Admin; Owner for own hotel)

```http
GET /v1/admin/audit-logs?entity_type=HOTEL&entity_id=c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61
```

```json
{
  "items": [
    { "actor_user_id": "11111111-2222-4333-8444-555555555555", "action": "STATUS_CHANGE",
      "previous_state": "PENDING_REVIEW", "next_state": "PUBLISHED", "reason": null, "created_at": "2026-10-05T11:00:00Z" }
  ],
  "next_cursor": null
}
```

Audit rows are append-only. No update or delete endpoint exists.
