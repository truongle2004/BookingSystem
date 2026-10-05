### 9.6 Operations `[M0 to M3]`

#### UC-49: Seed development data (Developer) `[M1]`

A dev/test-only command that is safe to run repeatedly. It creates: one admin, one owner, one customer, one `PUBLISHED` hotel, two room types, three rooms. A second run changes nothing. It refuses to run when `APP_ENV=production`.

#### UC-50: Create the first admin in production (Operator) `[M1]`

A documented one-off SQL statement that inserts the role row for a user who has already signed in once. No endpoint and no claim-based bootstrap. The README documents it and the audit log records it.

#### UC-51: Expiration worker run (System) `[M3]`

- Polls on a configured interval and expires up to a configured batch of stale `HELD` bookings.
- Uses the same room-first locks as booking creation.
- Two workers can run safely at once.
- Restart mid-batch loses nothing because expiry is a state check, not in-memory state.

#### UC-52: Clerk outage or key rotation (Operator) `[M0]`

- If JWKS cannot be fetched, protected endpoints return 401 (or 503) while `/health/live` stays 200. The decision must be documented. **Proposed:** 401 for tokens that cannot be verified, with the error logged at warn level.
- Health endpoints never depend on Clerk.
- Key rotation is handled by JWKS caching and refresh. Test it with a rotated key.

#### UC-53: Oversized or malformed request (any client) `[M0]`

| Input | Response |
|---|---|
| Body over the size limit | 413 `PAYLOAD_TOO_LARGE` (proposed code) |
| Invalid JSON | 400 `MALFORMED_REQUEST` |
| Wrong content type | 415 `UNSUPPORTED_MEDIA_TYPE` (proposed) |
| `GET /v1/unknown-path` with a valid token | 404 `NOT_FOUND` |
| `DELETE /health/live` | 405 `METHOD_NOT_ALLOWED` |
