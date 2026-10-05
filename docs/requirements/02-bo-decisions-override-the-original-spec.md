## 2. BO decisions (override the original spec)

| # | Topic | Decision |
|---|---|---|
| 1 | Clerk in M0 vs M1 | M0 delivers JWT validation and the auth ADR. M1 adds local `users`/roles and lazy user sync. |
| 2 | Flyway | `spring.flyway.enabled=false` for the API. Migrate only with `./mvnw flyway:migrate`. Testcontainers tests may run Flyway programmatically. |
| 3 | Go-isms | "Context" means explicit timeouts. "Middleware" means servlet filter. Shutdown is `server.shutdown=graceful` with a bounded phase timeout. |
| 4 | `.env` loading | `local` profile uses `spring.config.import=optional:file:.env[.properties]`. README documents bash and PowerShell fallbacks. |
| 5 | `DATABASE_URL` | JDBC URL without credentials. Separate `DATABASE_USER` and `DATABASE_PASSWORD`. Password never logged. |
| 6 | Health | No Actuator. Two small controllers. Ready runs `SELECT 1` with a 2 s timeout. |
| 7 | Worker | None in M0. Arrives in M3. |
| 8 | Module layout | `domain/`, `application/`, `infrastructure/`, `web/` per module, created only when real code needs them. M0 creates only `config/` and `platform/`. |
| 9 | First migration | `V1__enable_extensions.sql` creating `btree_gist`. No placeholder table. |
| 10 | Integration test | One Testcontainers smoke test (`*IT`, run by Failsafe in `verify`). `test` stays Docker-free. |
| 11 | Versions | Agent checks official sources and records choices in ADR-0001. If no Spring Boot 3.x line is in OSS support, use the current supported line. |
| 12 | Clerk decoder | Use `NimbusJwtDecoder.withJwkSetUri(...)` (lazy), not `JwtDecoders.fromIssuerLocation`. Derive JWKS URI from the issuer if unset, after verifying against Clerk docs. |

### 2.1 Business rules for later milestones

| Topic | Decision |
|---|---|
| Hotel publication | See state table in Section 6.3. |
| Material edits (published hotel returns to `PENDING_REVIEW`) | Name, address, city, timezone, hotel type, location type, deactivating the last room type or room. |
| Minor edits (stay published) | Price, description, amenities, phone number. |
| Owner approval | Manual, by an admin reviewing an owner request. Role grant is audited. |
| First admin | Dev seed in local/test. Production: documented one-off SQL. No claim-based bootstrap. |
| User status | `ACTIVE` or `BLOCKED`. Blocked users are rejected after authentication. |
| Email | Not unique-constrained (Clerk emails change). Plain index. |
| Owner booking own hotel | Disallowed. |
| Reservation aggregate | A customer-facing reservation may contain one or more reservation items. Each item maps to a physical room for a date range. Early milestones may expose a one-room compatibility flow, but the target model must not hard-code one reservation = one room. |
| Rate plan | A room type can expose multiple rate plans (for example flexible, non-refundable, breakfast included). Price and cancellation policy are snapshotted when a reservation item is held/confirmed. |
| Price calculation | Final quoted price is server-calculated from nightly rates, discounts, taxes and fees. Client-supplied totals are rejected. |
| Cancellation policy | Cancellation/refund follows the snapshotted rate-plan policy instead of a global full-refund rule once M4 pricing policy is enabled. |
| Cancel `HELD` | Allowed, no refund. |
| Cancel `CONFIRMED` | Before M4 policy support: full refund before local check-in date. From M4 onward: apply the snapshotted cancellation policy and create the required refund/fee obligation atomically. |

---
