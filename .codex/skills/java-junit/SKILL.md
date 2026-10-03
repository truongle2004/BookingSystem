---
name: java-junit
description: 'Get best practices for JUnit 5 unit testing, including data-driven tests'
---

# JUnit 5+ Best Practices

Your goal is to help me write effective unit tests with JUnit 5, covering both standard and data-driven testing approaches.

## Project Setup

- Use a standard Maven or Gradle project structure.
- Place test source code in `src/test/java`.
- Include dependencies for `junit-jupiter-api`, `junit-jupiter-engine`, and `junit-jupiter-params` for parameterized tests.
- Use build tool commands to run tests: `mvn test` or `gradle test`.

## Test Structure

- Test classes should have a `Test` suffix, e.g., `CalculatorTest` for a `Calculator` class.
- Use `@Test` for test methods.
- Follow the Arrange-Act-Assert (AAA) pattern.
- Name tests using a descriptive convention, like `methodName_should_expectedBehavior_when_scenario`.
- Use `@BeforeEach` and `@AfterEach` for per-test setup and teardown.
- Use `@BeforeAll` and `@AfterAll` for per-class setup and teardown (must be static methods).
- Use `@DisplayName` to provide a human-readable name for test classes and methods.

## Standard Tests

- Keep tests focused on a single behavior.
- Avoid testing multiple conditions in one test method.
- Make tests independent and idempotent (can run in any order).
- Avoid test interdependencies.

## Data-Driven (Parameterized) Tests

- Use `@ParameterizedTest` to mark a method as a parameterized test.
- Use `@ValueSource` for simple literal values (strings, ints, etc.).
- Use `@MethodSource` to refer to a factory method that provides test arguments as a `Stream`, `Collection`, etc.
- Use `@CsvSource` for inline comma-separated values.
- Use `@CsvFileSource` to use a CSV file from the classpath.
- Use `@EnumSource` to use enum constants.

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

## Testing

- Unit tests: JUnit 5 + Mockito.
- **Use `@MockitoBean` and `@MockitoSpyBean`.** `@MockBean` and `@SpyBean`
  are removed in Boot 4 and will not compile.
- Slice tests (`@WebMvcTest`, `@DataJpaTest`) need the matching test starter
  dependency. If a test annotation or `TestRestTemplate` is unresolved, add
  the test starter for that technology.
- `@SpringBootTest` for integration tests. Consider Testcontainers.

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

- Use the static methods from `org.junit.jupiter.api.Assertions` (e.g., `assertEquals`, `assertTrue`, `assertNotNull`).
- For more fluent and readable assertions, consider using a library like AssertJ (`assertThat(...).is...`).
- Use `assertThrows` or `assertDoesNotThrow` to test for exceptions.
- Group related assertions with `assertAll` to ensure all assertions are checked before the test fails.
- Use descriptive messages in assertions to provide clarity on failure.

## Mocking and Isolation

- Use a mocking framework like Mockito to create mock objects for dependencies.
- Use `@Mock` and `@InjectMocks` annotations from Mockito to simplify mock creation and injection.
- Use interfaces to facilitate mocking.

## Test Organization

- Group tests by feature or component using packages.
- Use `@Tag` to categorize tests (e.g., `@Tag("fast")`, `@Tag("integration")`).
- Use `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` and `@Order` to control test execution order when strictly necessary.
- Use `@Disabled` to temporarily skip a test method or class, providing a reason.
- Use `@Nested` to group tests in a nested inner class for better organization and structure.
Beta
0 / 0
used queries
1
