## 8. Planned use cases: feedback `[M5]`

### UC-19: Review a completed stay (Customer)

- **Preconditions:** booking is owned by the reviewer, belongs to the hotel, status `COMPLETED`, no review yet (one per booking).
- **Scores:** staff, amenities, cleanliness, comfort, value, location, wifi. Range 1 to 10. At least one score required. Missing wifi is `null`, not zero. Overall is the average of supplied scores.

```http
POST /v1/bookings/b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08/review
Authorization: Bearer <customer token>
Content-Type: application/json

{
  "scores": { "staff": 9, "cleanliness": 8, "comfort": 8, "value": 7, "wifi": null },
  "title": "Lovely quiet stay",
  "description": "Friendly staff and a very clean room."
}
```

```http
HTTP/1.1 201 Created

{
  "id": "a1b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
  "booking_id": "b8f3d1a6-4e2c-4b90-a7d5-3c6e9f1b2a08",
  "overall_score": 8.0,
  "title": "Lovely quiet stay",
  "created_at": "2026-11-13T07:00:00Z"
}
```

Review of a `HELD` or `CONFIRMED` booking returns 409 `INVALID_STATE_TRANSITION` (or 422). Second review returns 409. A score of 11 returns 422.

### UC-20: Read reviews (Anonymous)

```http
GET /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/reviews?limit=20
```

```json
{
  "items": [
    {
      "id": "a1b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
      "overall_score": 8.0,
      "title": "Lovely quiet stay",
      "likes": 3,
      "dislikes": 0,
      "created_at": "2026-11-13T07:00:00Z"
    }
  ],
  "next_cursor": null
}
```

Likes and dislikes are derived counts.

### UC-21: React to a review (Customer)

```http
PUT /v1/reviews/a1b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d/reaction
Content-Type: application/json

{ "reaction": "LIKE" }
```

```json
{ "review_id": "a1b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d", "reaction": "LIKE" }
```

One reaction per user per review. A second `PUT` replaces the first.

### UC-22: Ask and answer questions (Customer, Hotel owner)

```http
POST /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/questions
Content-Type: application/json

{ "question": "Is there parking for motorbikes?" }
```

```json
{ "id": "f0e1d2c3-b4a5-4968-8776-5a4b3c2d1e0f", "question": "Is there parking for motorbikes?" }
```

```http
POST /v1/questions/f0e1d2c3-b4a5-4968-8776-5a4b3c2d1e0f/answers
Authorization: Bearer <owner token>
Content-Type: application/json

{ "answer": "Yes, free parking behind the building." }
```

The answering user is read from the token, and the owner must own the hotel the question belongs to.

---
