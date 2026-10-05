### 19.9 Messaging, notification and audit tables `[M6+]`

#### `conversations`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `reservation_id` | uuid | FK -> `reservations.id`, NOT NULL, UNIQUE |
| `created_at` | timestamptz | NOT NULL |

One private conversation thread per reservation in the first implementation.

#### `messages`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `conversation_id` | uuid | FK -> `conversations.id`, NOT NULL |
| `sender_user_id` | uuid | FK -> `users.id`, NOT NULL |
| `message` | text | NOT NULL |
| `created_at` | timestamptz | NOT NULL |

Index: `(conversation_id, created_at, id)`.

#### `notification_outbox`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `event_type` | varchar(100) | NOT NULL |
| `aggregate_type` | varchar(80) | NOT NULL |
| `aggregate_id` | uuid | NOT NULL |
| `payload_json` | jsonb | NOT NULL |
| `status` | varchar(20) | `PENDING`, `PROCESSING`, `SENT`, `FAILED` |
| `attempts` | integer | NOT NULL default 0 |
| `next_attempt_at` | timestamptz | nullable |
| `created_at` | timestamptz | NOT NULL |
| `processed_at` | timestamptz | nullable |

Indexes should support polling pending rows by `(status, next_attempt_at, created_at)`.

#### `audit_logs`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `actor_user_id` | uuid | nullable FK -> `users.id`; null allowed for system actor |
| `action` | varchar(100) | NOT NULL |
| `entity_type` | varchar(80) | NOT NULL |
| `entity_id` | uuid | NOT NULL |
| `previous_state` | varchar(80) | nullable |
| `next_state` | varchar(80) | nullable |
| `reason` | text | nullable |
| `metadata_json` | jsonb | nullable |
| `created_at` | timestamptz | NOT NULL |

Append-only. Index `(entity_type, entity_id, created_at, id)`.
