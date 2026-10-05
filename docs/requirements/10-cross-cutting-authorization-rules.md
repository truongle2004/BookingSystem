## 10. Cross-cutting authorization rules

| Rule | Behaviour |
|---|---|
| Identity | Always from the verified token. Never from a header, path or body. |
| Roles | Stored in the local database, not trusted from claims unless documented and tested. |
| Ownership | Role is not enough. Owners touch only hotels where `owner_id` equals their local user. |
| Customers | See only their own bookings and payments. |
| Public search | Only `PUBLISHED` hotels. |
| Suspension | Blocks new bookings. Existing bookings are preserved. |
| `OUT_OF_SERVICE` room | Blocks new holds. Existing bookings are preserved. |
