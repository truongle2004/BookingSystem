### 19.8 Feedback tables `[M5]`

#### `reviews`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `reservation_id` | uuid | FK -> `reservations.id`, NOT NULL, UNIQUE |
| `user_id` | uuid | FK -> `users.id`, NOT NULL |
| `hotel_id` | uuid | FK -> `hotels.id`, NOT NULL |
| `staff_score` | smallint | nullable, CHECK 1..10 |
| `amenities_score` | smallint | nullable, CHECK 1..10 |
| `cleanliness_score` | smallint | nullable, CHECK 1..10 |
| `comfort_score` | smallint | nullable, CHECK 1..10 |
| `value_score` | smallint | nullable, CHECK 1..10 |
| `location_score` | smallint | nullable, CHECK 1..10 |
| `wifi_score` | smallint | nullable, CHECK 1..10 |
| `overall_score` | numeric(4,2) | NOT NULL, CHECK 1..10 |
| `title` | varchar(200) | nullable |
| `description` | text | nullable |
| `created_at` | timestamptz | NOT NULL |

At least one component score must be non-null. `overall_score` is calculated server-side from supplied component scores and persisted for stable display/querying.

#### `review_reactions`

| Column | Type | Rules |
|---|---|---|
| `review_id` | uuid | FK -> `reviews.id`, NOT NULL |
| `user_id` | uuid | FK -> `users.id`, NOT NULL |
| `reaction` | varchar(10) | `LIKE` / `DISLIKE` |
| `updated_at` | timestamptz | NOT NULL |

Primary key: `(review_id, user_id)`.

#### `questions`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `hotel_id` | uuid | FK -> `hotels.id`, NOT NULL |
| `user_id` | uuid | FK -> `users.id`, NOT NULL |
| `question` | text | NOT NULL |
| `created_at` | timestamptz | NOT NULL |

#### `answers`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `question_id` | uuid | FK -> `questions.id`, NOT NULL |
| `user_id` | uuid | FK -> `users.id`, NOT NULL |
| `answer` | text | NOT NULL |
| `created_at` | timestamptz | NOT NULL |

Authorization ensures an answering hotel owner owns the questioned hotel.
