### 19.2 Identity and authorization tables `[M1]`

#### `users`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `clerk_user_id` | varchar(128) | NOT NULL, UNIQUE |
| `username` | varchar(100) | nullable |
| `email` | varchar(320) | nullable, **not unique** |
| `address` | varchar(500) | nullable |
| `description` | text | nullable |
| `status` | varchar(20) | NOT NULL, `ACTIVE` / `BLOCKED` |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Indexes:

- `UNIQUE(clerk_user_id)`;
- non-unique index on `email`;
- optional index on `status` only if admin filtering justifies it.

#### `user_roles`

| Column | Type | Rules |
|---|---|---|
| `user_id` | uuid | FK -> `users.id`, NOT NULL |
| `role` | varchar(30) | NOT NULL; initial values `CUSTOMER`, `HOTEL_OWNER`, `ADMIN` |
| `created_at` | timestamptz | NOT NULL |

Primary/unique key: `(user_id, role)`.

A normal authenticated user receives `CUSTOMER`. Approval of an owner request adds `HOTEL_OWNER`; it does not replace `CUSTOMER`.

#### `owner_requests`

| Column | Type | Rules |
|---|---|---|
| `id` | uuid | PK |
| `user_id` | uuid | FK -> `users.id`, NOT NULL |
| `status` | varchar(20) | `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN` |
| `reason` | text | requester reason, NOT NULL |
| `review_reason` | text | nullable |
| `reviewed_by` | uuid | FK -> `users.id`, nullable |
| `reviewed_at` | timestamptz | nullable |
| `created_at` | timestamptz | NOT NULL |
| `updated_at` | timestamptz | NOT NULL |

Constraint: at most one active pending request per user.

Recommended PostgreSQL partial unique index:

```sql
CREATE UNIQUE INDEX uq_owner_requests_one_pending_per_user
    ON owner_requests(user_id)
    WHERE status = 'PENDING';
```
