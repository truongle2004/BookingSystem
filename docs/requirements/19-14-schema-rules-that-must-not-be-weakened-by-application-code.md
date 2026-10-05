### 19.14 Schema rules that must not be weakened by application code

The following are database-level invariants:

- unique Clerk identity;
- one pending owner request per user;
- one room number per hotel;
- room -> room type must stay within the same hotel;
- valid check-in/check-out date order;
- non-negative money fields;
- one saved relation per `(user, hotel)`;
- one review per completed reservation in the initial model;
- one reaction per `(review, user)`;
- webhook event deduplication;
- idempotency-key uniqueness within its scope;
- no overlapping blocking reservation item for the same physical room;
- one cover image per hotel/room type.

Application code adds authorization, state-transition validation, pricing logic and friendly error mapping, but it must not be the only protection for these invariants.
