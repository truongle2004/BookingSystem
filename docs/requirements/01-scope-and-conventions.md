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

Codes beyond `ROOM_UNAVAILABLE` and `IDEMPOTENCY_CONFLICT` are proposed names. Agents may rename them, but must keep them stable once published in `api/openapi.yaml`.

---
