# Day 012 — Distributed Cache (Redis-like System)

> **Interview Goal:** Design a distributed in-memory cache that can replace Redis for a large-scale application.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Your application's database is becoming a bottleneck. Frequently read data (user sessions, product details, trending items) is fetched millions of times per second, but it changes rarely. A cache layer that serves this data from memory would reduce database load by 90% — but it needs to work across dozens of servers, handle failures, and maintain consistency.

---

## Functional Requirements

1. **Set / Get / Delete** key-value pairs from the cache.
2. Support **TTL (time-to-live)** per key — keys automatically expire after a set duration.
3. Support **eviction policies** when memory is full: LRU (Least Recently Used), LFU (Least Frequently Used), FIFO.
4. Cache is **distributed** — data spread across multiple nodes with no single point of failure.
5. Clients can connect via a **simple TCP or HTTP API** (similar to Redis protocol).
6. Support **cache-aside**, **write-through**, and **write-behind** patterns (configurable per use case).
7. Cache nodes can be **added or removed** without restarting the cluster (dynamic scaling).

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Latency | < 1 ms for GET; < 2 ms for SET |
| Availability | 99.99% (no downtime on node failures) |
| Consistency | Eventual (cache may serve stale data briefly during failover) |
| Scale | Hundreds of nodes; petabytes of total cache data |
| Throughput | 1M operations/second per node |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Total cache size needed | 10 TB |
| Memory per node | 128 GB |
| Nodes needed | ~80 primary nodes |
| Ops/second (total cluster) | 50M/sec |
| Avg key-value size | 1 KB |
| TTL range | 1 second to 7 days |

---

## Core API to Design

```
SET   key value [EX seconds]       → store a key with optional TTL
GET   key                          → retrieve a value (null if miss/expired)
DEL   key [key ...]                → delete one or more keys
TTL   key                          → time remaining before expiry
MGET  key [key ...]                → batch get multiple keys
MSET  key value [key value ...]    → batch set
EXISTS key                         → check if a key exists
FLUSH                              → clear all cache (admin only)

Admin:
GET  /nodes                        → list all cluster nodes and health
POST /nodes                        → add a new node to the cluster
```

---

## Key Challenges to Think About

- **Data partitioning — which node stores which key?**
  - Naive mod-hash: `node = hash(key) % N`. Problem: adding/removing a node rebalances almost all keys.
  - **Consistent hashing:** Only ~K/N keys move when a node is added/removed. How does it work?
- **LRU eviction at scale:** The classic LRU requires a doubly-linked list + hash map. In a hot cache with 1M keys, maintaining an exact LRU is expensive. Redis uses an approximate LRU — how?
- **TTL expiry:** You can't scan all keys every second to expire them. Redis uses lazy expiry + probabilistic background sweeps. How does that work?
- **Replication for fault tolerance:** If a primary node dies, can a replica take over without data loss? What is the trade-off between synchronous and asynchronous replication?
- **Cache stampede (thundering herd):** A popular key expires at the same time. 10,000 requests simultaneously hit the DB to recompute it. How do you prevent this?
- **Write-behind consistency:** Cache is written first, DB updated asynchronously. If the cache node crashes before DB write, data is lost. How do you handle this?
- **Serialisation:** Values can be strings, JSON, or binary. How does the cache handle different value types efficiently?

---

## Clarifying Questions (practice asking these in an interview)

1. Are we designing the cache server itself, or just the client integration strategy?
2. What consistency guarantee is required: strict, eventual, or read-your-writes?
3. Should the cache support complex data structures (lists, sets, sorted sets) like Redis, or just key-value?
4. Is multi-tenancy required (different customers sharing the same cache cluster)?
5. Do we need persistence (data survives server restart) or is pure in-memory fine?
6. What is the expected hit rate? This determines how much the cache actually helps.

---

## Concepts Tested

`Consistent Hashing` · `LRU / LFU eviction` · `TTL and lazy expiry` · `Replication (sync vs async)` · `Cache stampede / mutex lock` · `Cache-aside vs write-through patterns` · `Distributed consensus` · `Memory management`
