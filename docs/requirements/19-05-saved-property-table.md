### 19.5 Saved-property table `[M2]`

#### `saved_hotels`

| Column | Type | Rules |
|---|---|---|
| `user_id` | uuid | FK -> `users.id`, NOT NULL |
| `hotel_id` | uuid | FK -> `hotels.id`, NOT NULL |
| `created_at` | timestamptz | NOT NULL |

Primary key: `(user_id, hotel_id)`.

Suspending a hotel does not delete saved relations.
