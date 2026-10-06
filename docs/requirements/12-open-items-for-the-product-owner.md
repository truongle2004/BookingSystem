## 12. Open items for the product owner

| # | Question | Proposed default |
|---|---|---|
| 1 | Is `GET /v1/me` wanted in M1? | Yes |
| 2 | Admin endpoints for owner requests | `POST /v1/admin/owner-requests/{id}/approve` and `/reject` |
| 3 | 404 vs 403 for another owner's resource | 404 (avoids leaking existence) |
| 5 | Webhook signature header name | `X-Fake-Signature` (HMAC-SHA256) |
| 6 | Hotel search filters and sort | Use the M2 set defined in UC-08; add geospatial distance only after coordinates/index strategy are decided |
| 7 | Package name | `com.example.booking` |
| 8 | Limit on simultaneous holds per user (UC-40) | 3 `HELD` bookings, configurable |
| 9 | What an owner does about future bookings when a room goes out of service (UC-31) | Owner contacts admin. Admin cancels with refund (UC-42). No self-service cancel in MVP |
| 10 | Revoking owner role (UC-27) | Suspend the user's published hotels |
| 11 | Auto-move `REJECTED` hotel to `DRAFT` on edit (UC-33) | Yes |
| 12 | Behaviour when Clerk JWKS is unreachable (UC-52) | 401 and warn log |



---
