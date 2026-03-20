# Day 001 - Design: URL Shortener (TinyURL)

## 1. High-Level Architecture

```
Client
       |
       v
[CDN/WAF]
       |
       v
[Load Balancer]
       |
       +--> [URL Write Service] ----> [PostgreSQL Primary]
       |             |                         |
       |             +--> [Redis Cache]        +--> [Read Replicas]
       |             |
       |             +--> [KGS Service + Redis Key Pool]
       |
       +--> [Redirect Service] ----> [Redis Cache] ----> [DB Fallback]
                                                        |
                                                        +--> [Kafka: click events] --> [Analytics Consumers] --> [OLAP Store]
```

---

## 2. Services and Responsibilities

### URL Write Service

- Validates input URL and custom alias
- Allocates short key (from KGS pool or custom alias)
- Persists mapping in DB
- Writes hot mapping to Redis cache

### Redirect Service

- Handles `GET /{shortKey}` at high QPS
- Fetches long URL from Redis first, DB fallback second
- Returns `302` redirect
- Emits click event asynchronously

### KGS (Key Generation Service)

- Pre-generates unique Base62 keys in batches
- Pushes keys into Redis list/set pool
- Write service pops one key per URL create request

### Analytics Pipeline

- Redirect service publishes click events to Kafka
- Consumers aggregate by minute/day
- Writes to analytics store without affecting redirect path latency

---

## 3. Key Generation Strategy

### Candidate Approaches

1. Counter + Base62
- Easy implementation
- Centralized counter can become bottleneck

2. Hash(longUrl) + truncation
- Deterministic for same URL
- Collision handling required

3. KGS pool (chosen)
- Collision-free if generated from unique ID space
- Very low create latency
- Decouples key generation from write requests

Capacity note:

- Base62 keyspace for length 7: $62^7 \approx 3.5 \times 10^{12}$
- Enough for long-term growth, with room for future scale

---

## 4. Data Model

### Primary table: urls

| Column | Type | Notes |
|--------|------|-------|
| short_key | VARCHAR(12) PRIMARY KEY | Lookup key |
| long_url | TEXT NOT NULL | Original destination |
| created_at | TIMESTAMP NOT NULL | Creation timestamp |
| expires_at | TIMESTAMP NULL | Optional TTL |
| user_id | UUID NULL | Creator |
| is_deleted | BOOLEAN DEFAULT FALSE | Soft delete |

Indexes:

- PK index on `short_key`
- Optional index on `user_id, created_at`
- Optional partial index for active records (`is_deleted=false`)

### Optional dedupe table (future)

| Column | Type | Notes |
|--------|------|-------|
| url_hash | CHAR(64) PK | SHA-256 of normalized URL |
| short_key | VARCHAR(12) | Existing mapping |

---

## 5. Request Flows

### Create Short URL Flow

1. `POST /api/v1/urls` arrives at write service.
2. Validate URL format and allowed domains.
3. If custom alias exists, reserve alias with uniqueness check.
4. Else pop key from KGS pool in Redis.
5. Insert row in DB.
6. Cache key->URL in Redis.
7. Return short URL.

### Redirect Flow

1. `GET /{shortKey}` arrives at redirect service.
2. Redis lookup for short key.
3. Cache hit: return `302` quickly.
4. Cache miss: query DB primary/read replica.
5. If found and active, backfill cache and return `302`.
6. Emit click event asynchronously.
7. If not found/expired/deleted, return `404/410`.

---

## 6. Caching Strategy

- Redis value: long URL + expiry metadata
- TTL aligned to URL expiry
- Negative caching for invalid keys (short TTL) to protect DB
- Use request coalescing or Redis lock on hot misses to avoid stampede

---

## 7. Scalability Plan

### Read scaling

- Stateless redirect service behind load balancer
- Horizontal scaling based on CPU/QPS
- Redis cluster for cache throughput

### Write scaling

- Stateless write service horizontal scale
- KGS worker shards to refill key pool
- DB partitioning by short key prefix when table grows very large

### Storage evolution

- Start with PostgreSQL
- Move to Cassandra/DynamoDB style storage when write throughput and global scale demand it

---

## 8. Reliability and Resilience

- Multi-AZ deployment for services, Redis, and DB
- Circuit breaker: if DB is degraded, continue serving cached URLs
- Dead-letter queue for failed click events
- Retry with exponential backoff for Kafka publish and analytics writes

---

## 9. Security and Abuse Controls

- URL safety checks (malware/phishing blocklist)
- Per-IP and per-user rate limiting on create APIs
- Input validation and canonicalization to prevent parser bypass
- Optional signed admin APIs for delete/disable operations

---

## 10. SLOs and Observability

Suggested SLOs:

- Redirect success rate: >= 99.99%
- Redirect latency P99: < 10 ms (service)
- Create latency P99: < 100 ms

Key metrics:

- Cache hit ratio
- Redirect QPS and error rate
- KGS pool depth and refill lag
- Kafka lag for analytics consumers
