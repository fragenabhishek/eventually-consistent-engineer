# Day 001 - Trade-offs (TinyURL)

## 1. Core Decisions and Why

### Redirect code: 302 vs 301

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| 301 Permanent | Better browser cache behavior | Repeat visits may bypass server, reducing analytics quality | No |
| 302 Temporary | Every visit reaches service; accurate analytics | Slightly higher repeated traffic | Yes |

Decision: Use `302` for V1 because observability and analytics are key product requirements.

---

### Key generation: hash, counter, or KGS

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Hash(longUrl) + truncate | Deterministic | Collision handling needed | No |
| Counter + Base62 | Simple and collision-free | Counter hotspot / SPOF risk | No |
| KGS pool + Base62 | Collision-free, low latency, decoupled from request path | Extra moving parts | Yes |

Decision: Use KGS pool to remove create-path bottleneck and keep predictable latency.

---

### Storage: SQL first or NoSQL first

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| PostgreSQL | ACID writes, mature tooling, easier operations | Vertical scaling limits at very high scale | Yes (initial) |
| Cassandra/Dynamo style | Massive horizontal scale | Operational complexity, eventual consistency model | Later |

Decision: Start with PostgreSQL and move to NoSQL when growth requires it.

---

### Analytics path: synchronous vs asynchronous

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Sync update on redirect | Simple model | Increases redirect latency, tighter coupling | No |
| Async via Kafka | Fast redirect path, scalable consumers | Eventual consistency in stats | Yes |

Decision: Use async analytics and accept slight lag in dashboard counts.

---

## 2. Secondary Design Trade-offs

### Custom alias handling

- Strict uniqueness check at write time.
- If alias exists, return `409 Conflict`.
- Trade-off: better correctness, slightly slower create path due to uniqueness check.

### Dedupe behavior

- V1 may create multiple short keys for same long URL.
- Trade-off: simpler write path now, can add optional dedupe later.

### Expiration handling

- Lazy expiration on read + background cleanup job.
- Trade-off: simpler writes, eventual cleanup delay.

---

## 3. Failure Modes and Mitigations

| Failure Mode | Impact | Mitigation |
|--------------|--------|------------|
| KGS pool runs low | Create API latency or failures | Auto-refill workers, alerts on pool depth thresholds |
| Redis unavailable | Higher DB read load | Fallback to DB + rate-limit protection |
| DB primary down | Redirect misses fail | Serve cache hits, fail over to replica, circuit breaker |
| Kafka unavailable | Analytics event drop risk | Retry + dead-letter queue |
| Cache stampede on hot key | DB overload | Mutex/request coalescing + jittered TTL |
| Abuse by bot traffic | Infra and cost spike | WAF + per-IP and per-user rate limits |

---

## 4. Migration Triggers (When to Re-architect)

- Redirect QPS consistently saturates Redis cluster
- Create QPS reaches tens of thousands per second
- Single SQL cluster storage/IO nearing limits
- Multi-region latency or data locality requirements emerge

At those points, move to partitioned or NoSQL primary storage and multi-region replication.
