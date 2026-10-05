## 17. Additional validation scenarios for the expanded model

| Scenario | Required result |
|---|---|
| Search by dates returns hotel with no matching rooms | Hotel excluded from availability-aware results |
| Two-room reservation, one item becomes unavailable during hold | Entire reservation hold fails; no partial held items persist |
| Rate changes after reservation is held | Held/confirmed snapshot remains unchanged |
| Flexible vs non-refundable plan for same room type | Different price and cancellation snapshot are returned |
| Customer changes dates | Availability rechecked and price difference recorded |
| Modification requires extra payment but payment fails | Original confirmed reservation remains valid |
| Modification creates refund | Reservation update and refund obligation commit atomically |
| Saved hotel becomes suspended | Saved relation remains, public details are not leaked |
| Notification worker crashes after DB commit | Outbox event remains retryable |
| Duplicate notification delivery | Consumer/delivery layer behaves idempotently |
| Same reservation contains two rooms | One reservation owns two reservation items and one aggregate total |
| Guest differs from account owner | Account owner controls booking; guest has no authorization rights |

---
