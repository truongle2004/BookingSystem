---
name: java-springboot
description: 'Best practices for developing applications with Spring Boot 4.x (Spring Framework 7, Jackson 3, Java 17+). Use when writing or modifying Spring Boot code in a project whose parent/BOM is 4.x.'
---

# Spring Boot 4 Best Practices

Target: Spring Boot 4.x, Spring Framework 7, Spring Security 7, Jakarta EE 11,
Java 17+. Before applying anything, confirm the Boot version in `pom.xml`.
If the project is on 3.x, do not use the Boot 4 patterns below.

## Project Setup & Structure

- **Build Tool:** Maven (`pom.xml`). Use `./mvnw` if present.
- **Modular starters:** Boot 4 splits auto-configuration into focused modules.
  Declare the starter for each technology you use, and do not rely on
  transitive dependencies.
  - Spring MVC: `spring-boot-starter-webmvc` (NOT `spring-boot-starter-web`)
  - Flyway/Liquibase: `spring-boot-starter-flyway` / `spring-boot-starter-liquibase`
  - Add the companion **test starter** for each technology you test.
- **Classic starters** (`spring-boot-starter-classic`) are only a temporary,
  deprecated migration bridge. Do not add them to new code.
- **Package Structure:** Organize by feature/domain (`com.example.app.order`),
  not by layer.
- **Missing bean/auto-config after a change?** The cause is usually a missing
  modular starter. Check the starters before debugging anything else.

## Dependency Injection & Components

- Use constructor injection with `private final` fields.
- Use `@Component`, `@Service`, `@Repository`, `@RestController` appropriately.
- Use JSpecify annotations (`@Nullable`, `@NullMarked`) to document nullability.

## Configuration

- Use `application.yml`, `@ConfigurationProperties` for type-safe binding,
  and profiles (`application-dev.yml`, `application-prod.yml`).
- Never hardcode secrets. Use environment variables or a secret manager.
- Jackson properties moved: e.g. `spring.jackson.read.*` is now
  `spring.jackson.json.read.*`. Check property names against the 4.x docs.
- Use `spring-boot-properties-migrator` temporarily when upgrading, then remove it.

## Web Layer

- Design consistent RESTful endpoints. Use Boot 4's first-class **API versioning**
  support for versioned APIs rather than hand-rolled URL parsing.
- Use DTOs. Never expose JPA entities directly.
- Validate with Bean Validation (`@Valid`, `@NotNull`, `@Size`) on DTOs.
- Use a global `@ControllerAdvice` with `@ExceptionHandler`.
- For HTTP clients, prefer declarative `@HttpExchange` interfaces registered
  via `@ImportHttpServices`.
- Behind a proxy, `ForwardedHeaderFilter` must be registered explicitly.

## JSON (Jackson 3)

- Jackson 3 is the default. Imports use `tools.jackson.*`
  (e.g. `tools.jackson.databind.JsonNode`, `tools.jackson.databind.ObjectMapper`).
  Exception: `jackson-annotations` keeps `com.fasterxml.jackson.annotation`.
- Use `@JacksonComponent` / `@JacksonMixin` (not `@JsonComponent` / `@JsonMixin`).
- `JacksonException` is **unchecked** (extends `RuntimeException`). Do not write
  `catch (IOException)` expecting it to catch Jackson errors.
- Jackson auto-registers all modules on the classpath. Disable with
  `spring.jackson.find-and-add-modules=false` if needed.
- Format-specific mappers (`JsonMapper`, `XmlMapper`) are available as beans.
- Do not mix `com.fasterxml.jackson.databind` and `tools.jackson.databind`
  in new code. Use Jackson 2 only where a third-party library requires it.

## Service Layer

- Put business logic in stateless `@Service` classes.
- Use `@Transactional` at the most granular level necessary.
- Use Spring Framework 7's built-in retry support instead of the separate
  Spring Retry library for new code.

## Data Layer

- Use Spring Data JPA (`JpaRepository`), `@Query` or Criteria API for complex
  queries, and DTO projections to fetch only needed data.

## Logging

- Use SLF4J with `private static final Logger logger = LoggerFactory.getLogger(X.class);`
- Use parameterized messages: `logger.info("Processing user {}", userId);`

## Security (Spring Security 7)

- Use Spring Security. Encode passwords with BCrypt (or `DelegatingPasswordEncoder`).
- Spring Security 7 changed some defaults. Do not assume Boot 3-era
  configuration works unchanged. Prefer the lambda DSL and check the Security 7
  migration guide when a config fails.
- Prevent SQL injection with Spring Data or parameterized queries. Encode output
  to prevent XSS.

## Upgrade Notes (only when migrating from 3.x)

- Move to the latest 3.5.x first and remove deprecated API usage, then go to 4.x.
- Search for leftovers: `com.fasterxml.jackson`, `@MockBean`, `@SpyBean`,
  `spring-boot-starter-web`, `spring-boot-starter-undertow`.

## Verification

After any change, run `./mvnw -B clean verify` (or `mvn -B clean verify`)
and fix failures. Never use `-DskipTests` to get a green build.