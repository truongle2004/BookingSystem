## 1. Scope and conventions

### 1.1 Actors

| Actor | Description |
|---|---|
| Anonymous | Unauthenticated visitor. Can browse published hotels and published feedback. |
| Customer | Authenticated user (default role). Searches, holds, pays, cancels, reviews. |
| Hotel owner | Customer with an approved `HOTEL_OWNER` role. Manages owned listings only. |
| Admin | Approves or rejects owner requests and listings, suspends hotels. All actions audited. |
| Payment provider (fake) | Sends signed callbacks. Not a user. |
| System | Expiration worker, refund job. Acts without a user. |

### 1.2 API conventions

| Topic | Rule |
|---|---|
| Prefix | `/v1` for all product endpoints. Health endpoints have no prefix. |
| Auth | `Authorization: Bearer <Clerk session token>`. Validated locally against Clerk JWKS. |
| Format | JSON. UUID strings. Dates `YYYY-MM-DD`. Timestamps RFC 3339 UTC. |
| Money | Integer minor units, never float. Default currency `VND` (no fractional unit, so 1 minor unit = 1 dong). |
| Pagination | `limit` (default 20, max 100) and opaque `cursor`. Stable sort with ID tie-breaker. |
| Request ID | Optional `X-Request-Id` (`[A-Za-z0-9_-]{1,64}`). Otherwise generated. Always echoed in the response header and in error bodies. |
| Write DTOs | Unknown fields rejected. Request body size bounded. |
| Idempotency | `Idempotency-Key` header on booking creation and payment creation `[M2/M4]`. |

### 1.3 Error envelope

Every error uses this shape. No stack traces, no raw database text.

```json
{
  "error": {
    "code": "ROOM_UNAVAILABLE",
    "message": "The room is not available for the requested dates.",
    "request_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7"
  }
}
```

| HTTP | Meaning | Example codes |
|---|---|---|
| 400 | Malformed request | `MALFORMED_REQUEST`, `UNKNOWN_FIELD` |
| 401 | Unauthenticated | `UNAUTHENTICATED` |
| 403 | Forbidden (role or ownership) | `FORBIDDEN`, `USER_BLOCKED` |
| 404 | Resource absent (also used when hiding unowned resources) | `NOT_FOUND` |
| 405 | Method not allowed | `METHOD_NOT_ALLOWED` |
| 409 | Conflict | `ROOM_UNAVAILABLE`, `IDEMPOTENCY_CONFLICT`, `INVALID_STATE_TRANSITION`, `HOLD_EXPIRED` |
| 422 | Invalid business input | `INVALID_DATE_RANGE`, `CAPACITY_EXCEEDED`, `HOTEL_NOT_BOOKABLE`, `HOTEL_INCOMPLETE` |
| 500 | Unexpected failure | `INTERNAL_ERROR` |
| 503 | Dependency unavailable (readiness only) | `SERVICE_UNAVAILABLE` |

These codes are public API values. Changes require an explicit contract review and an OpenAPI update.

### 1.4 Authentication and authorization

- `/health/live`, `/health/ready`, `/openapi.yaml`, `/scalar/**` and `/v3/api-docs/**` are anonymous.
- Product endpoints under `/v1/**` require `Authorization: Bearer <Clerk session token>`.
- Missing, malformed, expired, not-yet-valid, wrong-issuer, wrong-signature, wrong-audience and rejected
  authorized-party tokens all return the same `401 UNAUTHENTICATED` envelope.
- Authenticated callers without a required role or ownership return `403 FORBIDDEN`. Endpoints that hide the
  existence of another owner's resource return `404 NOT_FOUND` instead.
- Identity always comes from the verified token. Local roles and ownership checks are introduced in M1 and must
  not be inferred from unapproved token claims.

---
