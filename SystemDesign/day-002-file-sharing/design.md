# Day 002 - Design: File Sharing System (Pastebin)

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
       +--> [Paste Write Service] ----> [PostgreSQL: metadata]
       |             |                          |
       |             +--> [Object Store: content blobs]
       |             +--> [Redis Cache]
       |
       +--> [Paste Read Service] -----> [Redis Cache] ----> [DB + Object Store Fallback]
                     |
                     +--> [Kafka: paste.viewed] --> [Analytics Consumers] --> [OLAP/Timeseries]

Background Workers:
- Expiry cleanup worker
- Burn-after-read worker
- Abuse scan worker
```

---

## 2. Services and Responsibilities

### Paste Write Service

- Validates payload size and supported language tags
- Generates unique short ID
- Stores content in object storage (or inline for tiny content)
- Writes metadata row in PostgreSQL
- Caches hot metadata for faster reads
- Returns share URL and delete token

### Paste Read Service

- Handles `GET /p/{pasteId}` at high read volume
- Reads from Redis cache first, DB/object storage fallback second
- Enforces expiry and visibility checks
- Handles burn-after-read semantics atomically
- Publishes async view event for analytics

### Analytics Pipeline

- Read service emits view events to Kafka
- Consumers aggregate counts by minute/day
- Writes aggregates to analytics store
- Keeps read path low latency and decoupled

### Cleanup and Policy Workers

- Expiry worker marks expired pastes deleted and evicts cache
- Burn-after-read worker finalizes one-time pastes after first access
- Abuse scan worker flags suspicious content and can quarantine

---

## 3. Data Storage Strategy

### Why split metadata and content?

- Metadata (small, indexed, transactional) fits relational DB well
- Content blobs (large, variable) are cheaper and simpler in object store
- This avoids database bloat and keeps index performance predictable

### Tables

- `pastes` for metadata and pointers
- `paste_analytics_daily` for aggregated metrics

### Object Storage Key Pattern

- `pastes/{yyyy}/{mm}/{dd}/{pasteId}.txt`
- Enables lifecycle policies and easier cold storage transitions

---

## 4. ID Generation Strategy

### Candidate approaches

1. Random Base62 IDs (chosen)
- Easy to generate in stateless services
- Retry on collision (very low probability with proper length)

2. Global counter + Base62
- Predictable and simple mapping
- Hotspot/SPOF risk without careful sharding

3. Hash(content)
- Dedupes same content naturally
- Leaks similarity patterns and can aid enumeration

Decision: Use random Base62 IDs of length 6-8 for V1.

---

## 5. Request Flows

### Create Paste Flow

1. Client sends `POST /api/v1/pastes`.
2. Validate content size, visibility, and expiry bounds.
3. Generate `pasteId` and delete token.
4. Persist content blob in object storage.
5. Insert metadata row in PostgreSQL.
6. Write cache entry for hot lookup.
7. Return URL and delete token.

### Read Paste Flow

1. Client calls `GET /p/{pasteId}`.
2. Check Redis for metadata and (optionally) content.
3. On cache miss, load metadata from DB and content from object store.
4. Validate not deleted and not expired.
5. For burn-after-read: atomically mark consumed and prevent second read.
6. Emit `paste.viewed` event asynchronously.
7. Return content.

### Delete Flow

1. Client calls `DELETE /api/v1/pastes/{pasteId}` with delete token.
2. Service validates token hash.
3. Marks paste as deleted in DB.
4. Schedules asynchronous blob deletion and cache eviction.

---

## 6. Caching Strategy

- Cache key: `paste:{pasteId}`
- Cached value: metadata + content for small/medium pastes
- TTL based on `expiresAt` (or bounded max TTL)
- Negative cache for not-found IDs (short TTL)
- Request coalescing on misses to prevent stampede

---

## 7. Scalability Plan

### Read scaling

- Stateless read service horizontally scaled behind load balancer
- Redis cluster for high QPS and low-latency access
- CDN cache for public hot pastes to offload origin

### Write scaling

- Stateless write service horizontal scaling
- Batch writes for analytics only; metadata writes remain transactional

### Storage growth strategy

- Object store lifecycle rules move old content to cheaper tier
- DB partitioning by creation date for very high row counts
- Optional archive/deletion compaction jobs

---

## 8. Reliability and Resilience

- Multi-AZ deployment for services and DB
- Read-path fallback if cache is unavailable
- Outbox/retry pattern for view event delivery
- Dead-letter queue for malformed or poison events
- Idempotent delete and cleanup workers

---

## 9. Security and Abuse Controls

- Input size limits and MIME/type validation
- Optional malware scanning for shared links/content
- Per-IP and per-tenant rate limiting
- Delete tokens stored as hashes, never plaintext
- Optional CAPTCHA/challenge for anonymous high-volume creators

---

## 10. SLOs and Observability

Suggested SLOs:

- Read success rate: >= 99.99%
- Read latency P99: < 50 ms (service only)
- Create latency P99: < 150 ms

Key metrics:

- Cache hit ratio and miss latency
- DB query latency and connection pool saturation
- Object storage read/write latency and error rate
- View event lag and consumer throughput
- Expired/deleted cleanup backlog
