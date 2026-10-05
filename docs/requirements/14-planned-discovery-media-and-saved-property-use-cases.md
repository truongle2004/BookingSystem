## 14. Planned discovery, media and saved-property use cases `[M1 to M2]`

### UC-54: Manage hotel and room images (Hotel owner) `[M1]`

Owners upload or register images for hotels and room types. Images have a stable ID, URL/object key, caption, sort order and optional cover flag.

```http
POST /v1/hotels/{hotel_id}/images
Authorization: Bearer <owner token>
Content-Type: application/json

{
  "storage_key": "hotels/{hotel_id}/lobby-01.webp",
  "caption": "Lobby",
  "is_cover": true
}
```

Rules:

- owner must own the hotel;
- only supported content types and bounded sizes are accepted by the upload flow;
- deleting the cover image chooses another image or leaves the hotel without a cover;
- room-type images use `/v1/hotels/{hotel_id}/room-types/{room_type_id}/images`;
- public hotel detail returns ordered image metadata.

### UC-55: Save or unsave a hotel (Customer) `[M2]`

```http
PUT /v1/me/saved-hotels/{hotel_id}
DELETE /v1/me/saved-hotels/{hotel_id}
GET /v1/me/saved-hotels?limit=20
```

The relation is unique by `(user_id, hotel_id)`. Saving the same hotel twice is idempotent. A saved hotel that later becomes unpublished remains in the user's saved list but is marked unavailable/private rather than exposing hidden catalog details.

### UC-56: Search with filters and sort (Anonymous, Customer) `[M2]`

UC-08 defines the primary endpoint and supported filter/sort families. This UC captures validation and ranking rules:

- invalid enum/filter combinations return 422;
- price filtering uses the best currently bookable quote for the supplied dates, not only the room type's static base price;
- sort is stable with hotel ID as tie-breaker;
- cursor tokens encode the sort position and must not expose raw SQL offsets;
- search must not promise availability after the response; hold/create always revalidates.

### UC-57: View hotel detail with bookable offers (Anonymous, Customer) `[M2]`

```http
GET /v1/hotels/{hotel_id}?check_in=2026-11-10&check_out=2026-11-12&adults=2&children=0&rooms=1
```

The response combines public hotel information with room-type offers:

```json
{
  "id": "...",
  "name": "Pine Hill Guesthouse",
  "images": [],
  "amenities": [],
  "review_score": 8.6,
  "offers": [
    {
      "room_type_id": "...",
      "rate_plan_id": "...",
      "rate_plan_name": "Flexible",
      "available_rooms": 2,
      "max_guests": 2,
      "breakfast_included": true,
      "free_cancellation_until": "2026-11-08T17:00:00Z",
      "price": {
        "room_subtotal_minor": 1700000,
        "tax_minor": 100000,
        "fee_minor": 0,
        "discount_minor": 0,
        "total_minor": 1800000,
        "currency": "VND"
      }
    }
  ]
}
```

---
