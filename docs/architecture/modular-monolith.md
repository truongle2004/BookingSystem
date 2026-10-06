# Modular monolith boundaries

The application is a single deployable Spring Boot process, but its Java packages are
organized so business modules can become independently owned without a later package
migration.

## Package rules

- `com.bookingsystem.<module>` is the boundary for a business capability such as identity,
  catalog, booking, payment, or feedback.
- A module may expose application services and DTOs; its domain rules stay inside the module.
- Cross-cutting runtime concerns belong under `com.bookingsystem.platform`, not inside a
  business module. The current platform packages are `health` and `web`.
- `com.bookingsystem.config` contains only application wiring and external-provider
  configuration. It must not contain business rules.
- Controllers are transport adapters. Database access, validation policy, and business
  decisions belong in services or infrastructure components.
- Controllers belong in the module's `controller` package and are transport adapters.
- Service contracts belong in the module's `service` package. Spring implementations belong
  in `service.impl` and are the only classes annotated with `@Service`.
- Modules must not import another module's persistence implementation. Cross-module behavior
  uses an explicit service or event contract.

Milestone 0 has no business capability yet, so `config` and `platform` are the only production
packages. Future milestone work should add feature packages rather than creating global
`controller`, `service`, or `repository` layers.
