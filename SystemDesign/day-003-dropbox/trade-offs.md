# Day 003 — Trade-offs: Cloud Storage (Dropbox)

## 1. Core Decisions

### Chunked parallel upload vs single-file upload

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Single upload | Simple client | All-or-nothing; no partial resume; slow for large files | No |
| Chunked sequential | Resumable | Slow; no parallelism | No |
| Chunked parallel + pre-signed S3 URLs | Fast, resumable, zero app-server bandwidth | Client complexity; more round-trips | **Yes** |

**Rationale:** A 5 GB file on a mobile connection may take 30+ minutes. Without resumability, every dropout restarts the upload. Parallel chunk uploads max out available bandwidth. Pre-signed URLs mean app servers never touch file bytes — they only coordinate metadata.

---

### Metadata in SQL vs NoSQL

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| PostgreSQL (relational) | Joins across files/folders/versions/shares; ACID; rich querying | Vertical scale limits at extreme scale | **Yes (V1)** |
| DynamoDB / Cassandra | Massive horizontal scale | No joins; complex queries for folder trees and version history | Later if needed |

**Rationale:** File metadata is relational by nature — file lives in folder, folder has owner, file has versions, version has chunks, file is shared with users. SQL joins are natural. Migrate to NoSQL only when single-region PostgreSQL saturates.

---

### Content-addressable storage (CAS) for deduplication

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Store every upload as a new S3 object | Simple | 1,000 users upload same file → 1,000 copies stored | No |
| CAS: SHA-256(chunk) → deduplicated S3 object | Massive storage savings (20-60%) | Hash collision risk (negligible); chunk index overhead | **Yes** |

**Rationale:** At 500M users, without dedup, popular files (e.g., shared corporate documents, software installers) would be stored millions of times. CAS eliminates that. SHA-256 collision probability is negligible (~10⁻⁷⁷ per pair) and acceptable.

---

### Conflict resolution strategy

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Last-write-wins | Zero user friction | Silently loses work — unacceptable | No |
| Server-side merge (OT/CRDT) | Seamless UX | Works only for plain text; breaks binary, Office files | No |
| Conflict copy (both versions preserved) | No data loss; always safe | User must manually merge | **Yes** |

**Rationale:** Dropbox and most cloud storage systems choose conflict copies because they support all file types uniformly. For text/code files, this is slightly worse UX than a merge — but it is safe for every file format.

---

### Sync delivery: polling vs WebSocket vs webhook

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Client polling (e.g., every 30 s) | Simple server; stateless | High latency for sync; wasteful at scale | No |
| WebSocket (persistent connection) | Near-real-time delivery; efficient | Server must manage millions of open connections | **Yes** |
| Webhook (push to client) | Server-initiated | Mobile clients can't receive webhooks when backgrounded | No for mobile |

**Rationale:** Dropbox uses a long-polling / WebSocket hybrid. The persistent connection enables near-real-time sync (< 5 s) without repeated polling. Kafka absorbs change events and fans out to the correct connected clients.

---

## 2. Secondary Trade-offs

### Version retention limit
- Keep last 30 versions (or 30 days for free tier; 180 days for paid).
- Trade-off: simpler storage and quota management vs occasionally losing older versions.
- Background worker prunes versions asynchronously — no impact on hot path.

### Chunk size (4 MB)
- Smaller chunks → better dedup granularity, more round-trips.
- Larger chunks → fewer round-trips, worse dedup for small changes.
- 4 MB is the Dropbox-published chunk size; good balance.

### Pre-signed URL expiry
- URLs expire in 15 minutes — short enough to prevent link sharing but long enough for large uploads.
- If upload of a chunk takes longer (slow connection), client requests a fresh URL.

### Thumbnail generation
- Async (non-blocking): thumbnail failure never blocks file access.
- Trade-off: short delay before preview appears in UI. Acceptable.

---

## 3. Failure Modes and Mitigations

| Failure Mode | Impact | Mitigation |
|--------------|--------|------------|
| File Service unavailable | Cannot coordinate uploads | Multi-AZ; circuit breaker; retry in client |
| S3 region outage | Upload/download failure | S3 cross-region replication for critical data |
| Kafka lag (sync delay) | Sync notifications delayed | Monitor consumer lag; add partitions; auto-scale consumers |
| Dedup index inconsistency | Chunks thought to exist but deleted | Ref-count with periodic GC sweep |
| Conflict not detected | Silent data loss | Detect at upload via parent version ID mismatch |
| Client crash mid-upload | Partial upload | Client re-reads manifest on next open; resumes from last committed chunk |
| Virus scan queue backup | Infected files briefly accessible | Quarantine flag set at scan start; released on clean result |
| Quota exceeded silently | User loses data | Soft warning at 80%; hard stop at 100%; email notification |

---

## 4. Migration Triggers (When to Re-architect)

- Metadata DB write throughput exceeds PostgreSQL capacity → shard by `owner_id` or move file_versions to Cassandra
- Sync Service can't maintain millions of WebSocket connections on current fleet → add connection multiplexing (e.g., QUIC) or sharded connection pools
- Dedup index outgrows Redis memory → move to persistent distributed hash store (DynamoDB or RocksDB-based)
- Global users need data residency (GDPR) → introduce regional data plane with cross-region metadata replication only for indexes
