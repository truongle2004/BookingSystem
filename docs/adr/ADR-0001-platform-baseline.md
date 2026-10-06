# ADR-0001: Platform baseline

- Status: Accepted
- Date: 2026-10-06

## Context

Milestone 0 needs a reproducible Java and Spring platform before product use cases are implemented. The repository already uses Spring Boot 4 modular starters and the `com.bookingsystem` package, but its Java configuration was still set to Java 17 and the platform choices were not recorded.

## Decision

- Use Java 21 as the source, target, CI, build-image, and runtime-image version.
- Use Spring Boot 4.1.1 through `spring-boot-starter-parent`.
- Use Maven Wrapper 3.9.16 so local and CI builds use the same Maven distribution.
- Retain `com.bookingsystem` as the application base package.
- Keep Spring Boot 4 modular starters and declare required capabilities explicitly.

## Rationale

Java 21 is the milestone requirement and a long-term-support Java release. Spring Boot 4.1.1 is already pinned by the project and its modular dependency model is established in the current build. Keeping `com.bookingsystem` avoids a broad package migration that would not add product value and makes the codebase consistent with its existing artifact and service names.

## Consequences

- Developers and CI need a Java 21 or newer JDK to compile the project with release 21 compatibility.
- Container builds compile and run the service on Eclipse Temurin 21 images.
- Future platform-version changes require a new ADR that supersedes this decision.
- New application code belongs under `com.bookingsystem` and should use feature-oriented packages as product modules are introduced.
