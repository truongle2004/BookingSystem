## 3. Milestone 0 requirements (current scope)

### 3.1 Functional

| ID | Requirement |
|---|---|
| M0-F1 | `GET /health/live` returns 200 while the process runs. It never touches the database. |
| M0-F2 | `GET /health/ready` returns 200 when the DB answers `SELECT 1` within 2 s, otherwise 503. |
| M0-F3 | Any `/v1/**` path without a valid token returns 401 in the standard envelope. |
| M0-F4 | Tokens with wrong issuer, expired, not-yet-valid, bad signature, or wrong audience (when configured) are rejected with 401. |
| M0-F5 | Request ID filter on every request (see 1.2). |
| M0-F6 | Standard error envelope for 400, 401, 403, 404, 405, 500. |

### 3.2 Non-functional

| ID | Requirement |
|---|---|
| M0-N1 | Java 21, Spring Boot, Maven Wrapper, Spring MVC, Spring JDBC, Flyway SQL migrations, PostgreSQL. Versions pinned and recorded in ADR-0001. |
| M0-N2 | Environment configuration validated once at startup. Failures print a useful message with secrets redacted. |
| M0-N3 | `APP_ENV=production` refuses to start if the issuer is not HTTPS or any test-auth profile is active. |
| M0-N4 | Structured JSON logs to stdout. Request ID in MDC. Tokens and passwords never logged. |
| M0-N5 | Timeouts: HTTP connection 5 s, Hikari connect 3 s, query 5 s, readiness 2 s, shutdown phase 20 s. |
| M0-N6 | Graceful shutdown on SIGTERM. Connection pool closed. |
| M0-N7 | Compose runs PostgreSQL only, bound to `127.0.0.1`, named volume, `pg_isready` healthcheck. No routine `down -v`. |
| M0-N8 | Dockerfile: multi-stage, JRE 21 runtime, non-root user. |
| M0-N9 | CI: Spotless, Checkstyle, compile, unit tests, `verify` (including smoke IT), package. |
| M0-N10 | Migrations never run automatically at API startup. |

### 3.3 Environment variables

| Variable | Required | Default / format |
|---|---|---|
| `APP_ENV` | Yes | No default; allowed: `local`, `test`, `production` |
| `HTTP_ADDRESS` | No | `0.0.0.0` |
| `HTTP_PORT` | No | `8080` |
| `DATABASE_URL` | Yes | No default; must start with `jdbc:postgresql://` |
| `DATABASE_USER` | Yes | No default |
| `DATABASE_PASSWORD` | Yes | No default; never logged |
| `LOG_LEVEL` | No | `INFO` |
| `HTTP_READ_TIMEOUT` | No | `5s` |
| `DB_CONNECT_TIMEOUT` | No | `3s` |
| `DB_QUERY_TIMEOUT` | No | `5s` |
| `DB_POOL_MAX` | No | `10` |
| `CLERK_ISSUER_URI` | Yes | HTTPS in production |
| `CLERK_JWK_SET_URI` | No | `<issuer>/.well-known/jwks.json` (verify) |
| `CLERK_AUDIENCE` | No | |
| `CLERK_AUTHORIZED_PARTIES` | No | Checks the `azp` claim |
| `CLERK_CLOCK_SKEW` | No | `30s` |
| `HOLD_DURATION` | No | Reserved, commented out in `.env.example`. Unused until M2. Default 10 minutes. |

### 3.4 Milestone 0 use cases and examples

#### UC-00-1: Check liveness (Operator, Orchestrator)

- **Precondition:** process is running. DB may be down.
- **Main flow:** caller requests `/health/live`. System answers 200.
- **Alternate:** none. This endpoint must not depend on the DB.

```http
GET /health/live HTTP/1.1
Host: localhost:8080
```

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Request-Id: 3f1c2a9e-5d0b-4e8a-9b1f-0c2d7e6a4b10

{ "status": "UP" }
```

#### UC-00-2: Check readiness (Operator, Orchestrator)

- **Precondition:** process is running.
- **Main flow:** system runs `SELECT 1` with a 2 s timeout. Returns 200 when it succeeds.
- **Alternate A:** DB stopped or slow. System returns 503.

```http
GET /health/ready HTTP/1.1
Host: localhost:8080
```

```http
HTTP/1.1 200 OK
Content-Type: application/json

{ "status": "UP" }
```

DB unavailable:

```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/json
X-Request-Id: 8b2d5c1a-0e9f-4c37-a6d4-2f1e9b7c3a55

{
  "error": {
    "code": "SERVICE_UNAVAILABLE",
    "message": "Database is not reachable.",
    "request_id": "8b2d5c1a-0e9f-4c37-a6d4-2f1e9b7c3a55"
  }
}
```

The 503 response always uses the standard error envelope shown above. This is published in the OpenAPI document
and must remain stable.

#### UC-00-3: Reject unauthenticated call to a protected path (Anonymous)

```http
GET /v1/anything HTTP/1.1
Host: localhost:8080
```

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json
X-Request-Id: 5a7e1d90-2b4c-4f6e-8d3a-9c0b1e2f7a64

{
  "error": {
    "code": "UNAUTHENTICATED",
    "message": "Authentication is required.",
    "request_id": "5a7e1d90-2b4c-4f6e-8d3a-9c0b1e2f7a64"
  }
}
```

Same response for an expired token, wrong issuer, bad signature, or malformed bearer value. Do not reveal which check failed.

```http
GET /v1/anything HTTP/1.1
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...expired
```

#### UC-00-4: Accept a valid token (test only)

A valid signed test JWT must pass the security filter. This is proven by a **test-only** controller under `src/test`. There is no production endpoint for it.

#### UC-00-5: Caller supplies their own request ID

```http
GET /health/live HTTP/1.1
X-Request-Id: web-checkout_42
```

```http
HTTP/1.1 200 OK
X-Request-Id: web-checkout_42
```

An invalid value (for example 100 characters, or containing spaces) is replaced with a generated UUID. It is never echoed back.

#### UC-00-6: Start with bad configuration (Developer)

- **Main flow:** a required variable is missing or invalid. The app exits non-zero with a clear message. Secrets are redacted.

```text
Configuration error: DATABASE_URL must start with "jdbc:postgresql://" (got: <redacted>)
Configuration error: CLERK_ISSUER_URI is required
Configuration error: CLERK_ISSUER_URI must use https when APP_ENV=production
```

#### UC-00-7: Graceful shutdown (Operator)

- **Main flow:** SIGTERM arrives. System stops accepting new requests, completes in-flight requests within 20 s, closes the connection pool, exits.
- **Evidence:** log lines showing shutdown start and pool closed.

#### UC-00-8: Apply migrations (Developer)

```bash
cp .env.example .env
docker compose up -d
./mvnw flyway:migrate
./mvnw spring-boot:run
```

Windows uses `mvnw.cmd`. Migrations are never run by `spring-boot:run`.

### 3.5 Milestone 0 acceptance checklist

| # | Criterion |
|---|---|
| 1 | The four commands in UC-00-8 work from a clean clone with no other steps. |
| 2 | `/health/live` returns 200 with the DB stopped. |
| 3 | `/health/ready` returns 200, then 503 after `docker compose stop db`. |
| 4 | Bad or missing env vars fail fast with redacted messages. |
| 5 | `/v1/**` returns 401 without a token. Valid signed test JWT passes. Wrong-issuer and expired tokens are rejected. |
| 6 | `./mvnw verify` passes (format, static checks, unit tests, smoke IT). |
| 7 | Docker image builds, or the report states why it could not be run. |
| 8 | SIGTERM produces graceful shutdown logs and a closed pool. |
| 9 | README separates Implemented from Planned. Completion report uses real results only. |

---
