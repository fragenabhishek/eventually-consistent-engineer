# Day 001 — Trade-offs

## Decisions & Reasoning

### 302 vs 301 Redirect

| | 301 Permanent | 302 Temporary |
|-|--------------|---------------|
| Browser caches | Yes | No |
| Server hit on repeat visits | No | Yes |
| Analytics accuracy | Poor | Accurate |

**Chose 302** — analytics data is a core feature; losing server hits would break click counting.

---

### KGS vs Hash-based Key Generation

| | Hash + truncate | Counter + Base62 | KGS pool |
|-|----------------|-----------------|----------|
| Collision risk | Yes | No | No |
| Single point of failure | No | Yes (counter) | Mitigated (replicated Redis) |
| Complexity | Low | Low | Medium |
| Performance | Fast | Fast | Fastest (pre-generated) |

**Chose KGS pool** — scales horizontally, no collision risk, predictable latency.

---

### SQL vs NoSQL

- Started with **PostgreSQL**: simpler, ACID, easier to reason about at moderate scale.
- Migration path to **Cassandra** if write throughput exceeds ~50k/sec.
- Avoid premature optimization.

---

### Synchronous vs Async Analytics

- Click counting done **asynchronously via Kafka** to keep redirect latency minimal.
- Acceptable trade-off: click counts are eventually consistent (lag of a few seconds).

---

## What Could Go Wrong (Failure Modes)

| Failure | Mitigation |
|---------|-----------|
| KGS pool empty | Keep pool pre-filled; alert at 20% remaining |
| DB down on redirect | Extend cache TTL as circuit breaker |
| Cache stampede on popular URL | Probabilistic early expiration / mutex lock |
| Short key collision (custom alias) | Return 409 Conflict, ask user to choose another |
