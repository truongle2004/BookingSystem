## 11. Validation scenarios (from the original spec)

| Scenario | Required result |
|---|---|
| 100 concurrent holds, one room, same dates | Exactly one `HELD`. Others conflict. No overlap persisted. |
| Checkout date equals next check-in | Both bookings allowed. |
| Expired hold, worker stopped | New hold succeeds after transactional cleanup. |
| Callback at `expires_at` | Booking not confirmed. Refund obligation created. |
| Same event delivered five times | One local transition, one obligation set. |
| DB failure mid event processing | No partial commit. Retry succeeds. |
| Customer supplies another customer's ID | Identity from auth. Access denied. |
| Owner links room to another hotel's room type | Rejected by application and database. |
| Price changes after hold | Existing booking and payment unchanged. |
| Room made `OUT_OF_SERVICE` | New holds rejected. Existing bookings preserved. |
| Review of `HELD` or `CONFIRMED` booking | Rejected. `COMPLETED` owned stay accepted once. |
| Cancelled reservation gets late success | No reactivation. Refund obligation persisted. |
