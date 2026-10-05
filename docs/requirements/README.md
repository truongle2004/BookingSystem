# Booking System Requirements v3 — split index

This directory contains the project requirements as review-oriented Markdown files. Read `00-document-overview.md` first for scope, precedence, and milestone status, then use the paths below to load only the material relevant to the current task.

## Fast review paths

- Current Milestone 0: `00-document-overview.md`, `01-scope-and-conventions.md`, `02-bo-decisions-override-the-original-spec.md`, and `03-milestone-0-requirements-current-scope.md`.
- Planned behavior: sections 04–18. Section 09 is further split by domain because it is large.
- Persistence and migrations: section 19 files.

## Overview

- [Booking System: Requirements, Use Cases and API Examples](00-document-overview.md)

## Requirements and use cases

- [1. Scope and conventions](01-scope-and-conventions.md)
- [2. BO decisions (override the original spec)](02-bo-decisions-override-the-original-spec.md)
- [3. Milestone 0 requirements (current scope)](03-milestone-0-requirements-current-scope.md)
- [4. Planned use cases: identity and owner onboarding `[M1]`](04-planned-use-cases-identity-and-owner-onboarding.md)
- [5. Planned use cases: catalog and publication `[M1]`](05-planned-use-cases-catalog-and-publication.md)
- [6. Planned use cases: booking `[M2]`](06-planned-use-cases-booking.md)
- [7. Planned use cases: payment and lifecycle `[M4]`](07-planned-use-cases-payment-and-lifecycle.md)
- [8. Planned use cases: feedback `[M5]`](08-planned-use-cases-feedback.md)
- [9. Additional use cases (gaps not covered by the original spec)](09-00-additional-use-cases-overview.md)
- [9.1 Identity and account `[M1]`](09-01-identity-and-account.md)
- [9.2 Catalog management `[M1]`](09-02-catalog-management.md)
- [9.3 Booking visibility and edge cases `[M2 to M4]`](09-03-booking-visibility-and-edge-cases.md)
- [9.4 Payment visibility and failures `[M4]`](09-04-payment-visibility-and-failures.md)
- [9.5 Feedback visibility `[M5]`](09-05-feedback-visibility.md)
- [9.6 Operations `[M0 to M3]`](09-06-operations.md)
- [9.7 Explicitly deferred from the MVP](09-07-explicitly-deferred-from-the-mvp.md)
- [10. Cross-cutting authorization rules](10-cross-cutting-authorization-rules.md)
- [11. Validation scenarios (from the original spec)](11-validation-scenarios-from-the-original-spec.md)
- [12. Open items for the product owner](12-open-items-for-the-product-owner.md)
- [13. Product expansion roadmap `[M2 to M8]`](13-product-expansion-roadmap.md)
- [14. Planned discovery, media and saved-property use cases `[M1 to M2]`](14-planned-discovery-media-and-saved-property-use-cases.md)
- [15. Planned pricing and reservation use cases `[M3 to M5]`](15-planned-pricing-and-reservation-use-cases.md)
- [16. Planned communication and operations use cases `[M6 to M8]`](16-planned-communication-and-operations-use-cases.md)
- [17. Additional validation scenarios for the expanded model](17-additional-validation-scenarios-for-the-expanded-model.md)
- [18. Revised product-owner decisions](18-revised-product-owner-decisions.md)

## Database design

- [19. Database design and migration contract](19-00-database-design-overview.md)
- [19.1 Database conventions](19-01-database-conventions.md)
- [19.2 Identity and authorization tables `[M1]`](19-02-identity-and-authorization-tables.md)
- [19.3 Property catalog tables `[M1]`](19-03-property-catalog-tables.md)
- [19.4 Rate plan and pricing tables `[M3/M4]`](19-04-rate-plan-and-pricing-tables.md)
- [19.5 Saved-property table `[M2]`](19-05-saved-property-table.md)
- [19.6 Reservation aggregate `[M3/M4]`](19-06-reservation-aggregate.md)
- [19.7 Payment and refund tables `[M4]`](19-07-payment-and-refund-tables.md)
- [19.8 Feedback tables `[M5]`](19-08-feedback-tables.md)
- [19.9 Messaging, notification and audit tables `[M6+]`](19-09-messaging-notification-and-audit-tables.md)
- [19.10 Idempotency records](19-10-idempotency-records.md)
- [19.11 Migration ordering](19-11-migration-ordering.md)
- [19.12 Migration ownership by milestone](19-12-migration-ownership-by-milestone.md)
- [19.13 Indexing principles](19-13-indexing-principles.md)
- [19.14 Schema rules that must not be weakened by application code](19-14-schema-rules-that-must-not-be-weakened-by-application-code.md)
- [19.15 Open schema decisions](19-15-open-schema-decisions.md)
- [19.16 Migration readiness checklist](19-16-migration-readiness-checklist.md)
