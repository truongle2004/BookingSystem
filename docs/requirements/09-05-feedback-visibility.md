### 9.5 Feedback visibility `[M5]`

#### UC-47: List questions and answers (Anonymous)

```http
GET /v1/hotels/c41e8a07-6b2d-4d9e-8f13-7a0b5c2d9e61/questions?limit=20
```

```json
{
  "items": [
    { "id": "f0e1d2c3-b4a5-4968-8776-5a4b3c2d1e0f", "question": "Is there parking for motorbikes?",
      "answers": [ { "id": "1234abcd-0000-4000-8000-abcdef012345", "answer": "Yes, free parking behind the building.", "by_owner": true } ] }
  ],
  "next_cursor": null
}
```

Questions and reviews for a non-`PUBLISHED` hotel are hidden from public callers.

#### UC-48: Remove a review reaction (Customer)

```http
DELETE /v1/reviews/a1b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d/reaction
```

Returns 204. Deleting a non-existent reaction also returns 204 (idempotent).
