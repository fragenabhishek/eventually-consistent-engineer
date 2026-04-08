# trade-offs.md — Design Decisions, Trade-offs & Failure Modes

## 1) Metadata DB Choice
### Decision
Use a **strongly consistent** database for metadata (relational/NewSQL).

### Why
- Rename/move/version pointer updates require atomic transactions.
- Sharing/ACL checks and folder listings rely on consistent state.

### Trade-offs
- Strong consistency across regions is costly (latency, complexity).
- Mitigation: assign a **home region** for metadata writes; use read replicas.

---

## 2) Object Storage for File Data
### Decision
Store file bytes in **object storage** as immutable **chunks**, referenced by manifests.

### Why
- Massive scale, durability, cheap storage.
- Immutable objects align with versioning and dedup.

### Trade-offs
- Assembling chunks adds overhead for downloads.
- Mitigations:
  - For small files store as single object.
  - For large, optionally create a packed/compacted object in background.

---

## 3) Chunking Strategy: Fixed vs Content-Defined (CDC)
### Decision
Prefer **content-defined chunking** for delta sync.

### Why
- If bytes are inserted in the middle, fixed chunking invalidates all subsequent chunks.
- CDC (rolling hash) keeps boundaries stable → only small region changes.

### Trade-offs
- Client complexity and CPU cost.
- Mitigation: adapt chunk size; allow fallback to fixed chunking for low-power devices.

---

## 4) Deduplication Granularity
### Decision
Dedup at **chunk level** using content hash (CAS).

### Why
- Handles identical files and partially identical files.

### Trade-offs
- Requires refcounting and GC.
- Hash collisions are extremely unlikely with SHA-256; still treat as untrusted and verify size/optional secondary hash.

---

## 5) Sync: Polling vs Push
### Decision
Use a **change feed + cursor**; optionally add push notifications to wake clients.

### Why
- Pure push is hard on mobile networks; pure polling wastes resources.

### Trade-offs
- Eventual consistency: devices may lag.
- Mitigation: push hints + client backoff and periodic full reconciliation.

---

## 6) Conflict Resolution
### Decision
Default to **conflicted copies** when concurrent edits detected.

### Why
- Avoid silent data loss.

### Trade-offs
- User may see duplicate files.
- Mitigation: UI highlights conflicts; optional merge for text.

---

## 7) Versioning Storage Cost
### Decision
Keep last **30 versions** (as required) via manifests referencing chunks.

### Why
- Manifest indirection makes versions cheap when changes are small.

### Trade-offs
- Worst case (encrypted/compressed binaries) changes all chunks each time → storage grows.
- Mitigation: enforce per-user quota; server-side compaction; configurable retention.

---

## 8) Failure Modes & Handling

### A) Upload Interrupted Midway
- Upload session persists chunk receipts.
- Client resumes by querying missing chunks.

### B) Duplicate Commit / Retries
- Use idempotency keys and transactional commit.

### C) Object Store Outage / Partial Failure
- Multi-AZ replication.
- Retry PUTs with exponential backoff.
- Commit only after all chunks confirmed durable.

### D) Metadata DB Failover
- Use leader election/failover.
- Ensure changelog and file pointer update in same transaction.

### E) Event Bus Lag
- Sync still works via periodic polling of changes; event bus used for near-real-time hints and async jobs.

### F) Garbage Collection Bugs
- GC is dangerous: can delete live data.
- Use safety measures:
  - two-phase deletion (mark → sweep)
  - verify refcount snapshots
  - delay physical deletes

### G) Sharing Permission Leakage
- Ensure signed URLs are short-lived and bound to auth context.
- Validate ACL on each URL generation.

---

## 9) Operational Trade-offs
- **Home region for metadata** simplifies strong consistency but adds latency for traveling users.
- **Edge caching** reduces latency but complicates immediate revocation; mitigate via short TTL and token revocation lists.

