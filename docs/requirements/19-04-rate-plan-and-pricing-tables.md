### 19.4 Rate plan and pricing tables `[M3/M4]`

#### `rate_plans`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `room_type_id` | uuid | FK -> `room_types.id`, NOT NULL |
| `name` | varchar(160) | NOT NULL |
| `status` | varchar(20) | `ACTIVE` / `INACTIVE` |
| `meal_plan` | varchar(40) | e.g. `ROOM_ONLY`, `BREAKFAST_INCLUDED` |
| `cancellation_type` | varchar(40) | NOT NULL |
| `free_cancel_until_hours` | integer | nullable, CHECK >= 0 |
| `cancellation_fee_percent` | numeric(5,2) | nullable, CHECK 0..100 |
| `currency` | char(3) | NOT NULL |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Initial `cancellation_type` values:

- `FREE_UNTIL_DEADLINE`;
- `NON_REFUNDABLE`;
- `PERCENTAGE_FEE`;
- `FIRST_NIGHT_FEE` (later-compatible).

A CHECK constraint must require/forbid policy parameters consistently. Example: `FREE_UNTIL_DEADLINE` requires `free_cancel_until_hours`; `PERCENTAGE_FEE` requires `cancellation_fee_percent`.

#### `daily_rates`

| Column | Type | Rules |
|---|---|---|
| `rate_plan_id` | uuid | FK -> `rate_plans.id`, NOT NULL |
| `stay_date` | date | NOT NULL |
| `price_minor` | bigint | NOT NULL, CHECK >= 0 |
| `available` | boolean | NOT NULL default true |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Primary key: `(rate_plan_id, stay_date)`.

If a row does not exist, the application may fall back to the configured base rate only if that fallback rule remains explicitly documented. Do not silently invent prices.

Taxes/fees for the first implementation may remain configured application-side, but every reservation must persist the **calculated components** in its immutable price snapshot.
