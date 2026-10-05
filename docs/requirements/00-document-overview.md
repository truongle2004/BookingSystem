# Booking System: Requirements, Use Cases and API Examples

Status: **v3 schema-ready product draft** for agent implementation. Intended location: `docs/requirements.md`.

This revision keeps the original booking-engine guarantees (authorization, concurrency, idempotency, payment/refund safety) and expands the product toward a focused **Booking.com-like accommodation platform**. It does **not** attempt to clone flights, car rental, attractions, taxis, or the full Booking.com ecosystem.

**Precedence:** this document refines `booking-system-agent-spec.md`. Where Section 2 (BO Decisions) conflicts with the original spec body, this document wins.

**Current assignment: Milestone 0 only.** Everything tagged `[M1]` to `[M8]` is planned direction. Do not implement it, and do not represent it as working in the README or OpenAPI file.

**Product target:** accommodation discovery, reservation, payment, property operations, communication and trust. The target architecture must support multi-room reservations and rate plans even if the earliest implementation milestones ship a smaller subset.

---
