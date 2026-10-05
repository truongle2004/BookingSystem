### 19.15 Open schema decisions

The following are intentionally **not required before M0/M1 migration** and must not block early work:

1. Promotion stacking and eligibility `[M8]`.
2. Loyalty-level persistence `[M8]`.
3. Full-text/geospatial search implementation; basic city/destination search ships first.
4. Room-type inventory-pool model. The current contract holds concrete physical rooms.
5. Selective per-item cancellation. Initial cancellation is aggregate-level.
6. Multiple currencies and FX conversion. `VND` is the initial operational currency.
7. Exact Vietnam tax configuration; calculated tax/fee components are snapshotted regardless of source configuration.

These decisions require future forward migrations only when their milestone is approved.
