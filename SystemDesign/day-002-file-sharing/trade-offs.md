# Day 002 - Trade-offs (File Sharing System / Pastebin)

## 1. Core Decisions and Why

### Metadata + object storage vs single relational DB

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Store everything in SQL | Simpler querying and transactions | DB grows quickly, expensive storage, larger backup windows | No |
| Metadata in SQL + content in object store | Better cost profile, better scaling for large content | Two-step read path, slightly more complexity | Yes |

Decision: Keep metadata in PostgreSQL and store content in object storage.

---

### ID generation: random Base62 vs counter

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Counter + Base62 | Simple and ordered IDs | Predictable IDs, harder to secure/enumeration risk, central bottleneck | No |
| Random Base62 | Stateless generation, harder to enumerate | Collision checks required | Yes |

Decision: Use random Base62 IDs with uniqueness retry.

---

### Read path analytics: synchronous vs asynchronous

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Sync increment on read | Simpler consistency model | Higher latency on hot path | No |
| Async events via Kafka | Fast reads and scalable aggregation | Eventual consistency in counters | Yes |

Decision: Publish view events asynchronously and aggregate outside read path.

---

### Burn-after-read implementation

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Best-effort (no lock) | Very simple | Race conditions can allow multiple reads | No |
| Atomic consume flag with lock/compare-and-set | Correct one-time semantics | Slightly higher complexity | Yes |

Decision: Use atomic consume semantics for correctness.

---

## 2. Secondary Design Trade-offs

### Public vs unlisted defaults

- V1 default should be unlisted for privacy safety.
- Trade-off: weaker discoverability, better accidental exposure protection.

### Content size limits

- Enforce strict upper limit per paste in V1.
- Trade-off: avoids abuse and protects latency, but limits flexibility.

### Expiration strategy

- Lazy check on read + async cleanup worker.
- Trade-off: simple writes and predictable reads, eventual physical deletion delay.

---

## 3. Failure Modes and Mitigations

| Failure Mode | Impact | Mitigation |
|--------------|--------|------------|
| Redis unavailable | Higher DB/object-store load | Fallback reads + adaptive rate limiting |
| Object store latency spike | Read latency increases | Cache hot content, retries with timeout budget |
| DB primary outage | Metadata read/write failures | Multi-AZ failover + read-only degradation mode |
| Kafka outage | Analytics lag or drop risk | Retry, outbox pattern, dead-letter queue |
| Hot paste traffic burst | Service saturation | CDN + cache + per-key request coalescing |
| Abuse/spam floods | Cost and trust issues | WAF, IP throttling, content moderation pipeline |

---

## 4. Migration Triggers (When to Re-architect)

- Read QPS frequently exceeds single-region cache capacity
- Object storage egress cost dominates infrastructure budget
- Need full-text search over public pastes at large scale
- Compliance requires stronger data residency controls
- Need authenticated private pastes and shared workspaces

At that point, introduce multi-region active-active read architecture, search indexing pipelines, and stronger tenancy isolation.
