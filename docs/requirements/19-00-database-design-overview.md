## 19. Database design and migration contract

This section is the **baseline relational schema contract** for Flyway migrations. It exists to prevent agents from inventing tables or constraints independently from the product use cases.

Rules in this section are authoritative for database structure unless a later product-owner decision explicitly overrides them.
