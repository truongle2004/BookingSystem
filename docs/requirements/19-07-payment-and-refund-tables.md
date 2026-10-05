### 19.7 Payment and refund tables `[M4]`

#### `payments`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `reservation_id` | uuid | FK -> `reservations.id`, NOT NULL |
| `provider` | varchar(40) | NOT NULL |
| `provider_transaction_id` | varchar(200) | nullable |
| `amount_minor` | bigint | NOT NULL, CHECK >= 0 |
| `currency` | char(3) | NOT NULL |
| `status` | varchar(30) | `PENDING`, `SUCCEEDED`, `FAILED`, `CANCELLED` |
| `idempotency_key` | varchar(128) | NOT NULL |
| `request_hash` | varchar(128) | NOT NULL; used to detect key reuse with different body |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Recommended uniqueness:

- `UNIQUE(user/reservation scope as chosen, idempotency_key)`; simplest initial rule: `UNIQUE(reservation_id, idempotency_key)`;
- `(provider, provider_transaction_id)` unique when provider transaction id is non-null.

#### `payment_events`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `provider` | varchar(40) | NOT NULL |
| `provider_event_id` | varchar(200) | NOT NULL |
| `payment_id` | uuid | nullable FK -> `payments.id` |
| `event_type` | varchar(80) | NOT NULL |
| `payload_json` | jsonb | NOT NULL |
| `processed_at` | timestamptz | nullable |
| `created_at` | timestamptz | NOT NULL |

`UNIQUE(provider, provider_event_id)` guarantees webhook deduplication.

#### `refunds`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `reservation_id` | uuid | FK -> `reservations.id`, NOT NULL |
| `payment_id` | uuid | FK -> `payments.id`, NOT NULL |
| `amount_minor` | bigint | NOT NULL, CHECK > 0 |
| `currency` | char(3) | NOT NULL |
| `reason` | varchar(40) | NOT NULL |
| `status` | varchar(30) | `PENDING`, `COMPLETED`, `FAILED` |
| `attempts` | integer | NOT NULL default 0, CHECK >= 0 |
| `last_error` | text | nullable |
| `next_retry_at` | timestamptz | nullable |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Initial reason values:

- `RESERVATION_CANCELLED`;
- `LATE_PAYMENT`;
- `DUPLICATE_PAYMENT`;
- `MODIFICATION_PRICE_DIFFERENCE`.
