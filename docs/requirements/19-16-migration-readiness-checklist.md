### 19.16 Migration readiness checklist

Before implementing a milestone migration, verify:

- [ ] All tables required by that milestone are listed in Section 19.
- [ ] Column nullability/defaults are explicit.
- [ ] FK direction and delete behavior are understood.
- [ ] Unique/check/exclusion constraints are represented in SQL.
- [ ] Indexes map to approved query/worker access paths.
- [ ] Enum-like values used by code and DB are documented together.
- [ ] Money uses integer minor units.
- [ ] Dates vs timestamps follow the hotel-local/UTC rule.
- [ ] Historical financial/reservation rows are not physically cascaded away.
- [ ] Migration is forward-only and tested against an empty PostgreSQL database.
- [ ] `./mvnw flyway:migrate` succeeds from a clean clone.
- [ ] Testcontainers integration tests verify the critical constraints for that milestone.

**Schema readiness conclusion:** M0 is fully locked. M1-M6 now have a migration-ready baseline contract. M8 promotion/loyalty storage intentionally remains deferred until its business rules are approved.
