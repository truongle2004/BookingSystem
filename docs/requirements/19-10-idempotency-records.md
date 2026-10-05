### 19.10 Idempotency records

Booking/reservation creation and payment creation require idempotency. Do not rely only on an in-memory cache.

Recommended generic table:

#### `idempotency_records`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `scope` | varchar(80) | NOT NULL, e.g. `CREATE_RESERVATION`, `CREATE_PAYMENT` |
| `actor_key` | varchar(160) | NOT NULL; normally authenticated user id or reservation id |
| `idempotency_key` | varchar(128) | NOT NULL |
| `request_hash` | varchar(128) | NOT NULL |
| `resource_type` | varchar(80) | NOT NULL |
| `resource_id` | uuid | nullable until operation completes if implementation requires |
| `response_status` | integer | nullable |
| `response_json` | jsonb | nullable |
| `created_at` | timestamptz | NOT NULL |
| `expires_at` | timestamptz | nullable |

`UNIQUE(scope, actor_key, idempotency_key)`.

A repeated key with a different request hash returns `IDEMPOTENCY_CONFLICT`.
