# Day 001 — Design: URL Shortener

## High-Level Architecture

```
Client
  │
  ▼
[Load Balancer]
  │
  ├──► [Write Service]  ──► [DB: PostgreSQL / Cassandra]
  │           │
  │           └──► [Cache: Redis]
  │
  └──► [Redirect Service] ──► [Cache: Redis] ──► [DB fallback]
```

## Core Components

### 1. URL Encoding (Short Key Generation)

- Generate a 6-character key using Base62 (`[a-zA-Z0-9]`) → 62^6 ≈ 56 billion combinations
- **Option A — Counter + Base62:** Global auto-increment ID encoded to Base62. Simple but single-point-of-failure for counter.
- **Option B — MD5/SHA-256 hash + truncation:** Hash the long URL, take first 6 chars. Risk of collision.
- **Option C — Pre-generated key service (KGS):** Background service pre-generates keys and stores unused ones in a pool. Redirect service fetches from pool. No collision risk, fast.

> **Decision:** Use a Key Generation Service (KGS) with a key pool in Redis. Eliminates hot-increment bottleneck and avoids hash collisions.

### 2. Data Model

**urls table**

| Column | Type | Notes |
|--------|------|-------|
| short_key | VARCHAR(8) PK | The generated slug |
| long_url | TEXT | Original URL |
| user_id | UUID | Owner (nullable for anonymous) |
| created_at | TIMESTAMP | |
| expires_at | TIMESTAMP | NULL = never |
| click_count | BIGINT | Updated async |

### 3. Read Path (Redirect)

1. Client hits `GET /aB3kX`
2. Redirect Service checks **Redis cache** (TTL-based)
3. Cache miss → query DB → populate cache → HTTP 301/302 redirect
4. Use **302 (temporary)** not 301 to keep analytics accurate (301 is cached by browser)

### 4. Write Path

1. Client sends `POST /shorten { longUrl, customAlias?, ttl? }`
2. Write Service validates URL (regex + optional DNS check)
3. Fetch a pre-generated key from KGS pool (Redis `LPOP`)
4. Insert into DB; write to cache
5. Return short URL

### 5. Analytics (Async)

- On redirect, publish event to **Kafka** topic `url.clicked`
- Analytics consumers aggregate click counts, geo, referrer
- Written to separate analytics DB (ClickHouse / BigQuery) — does not slow down read path

## API Design

```
POST   /api/v1/shorten
       Body: { "longUrl": "...", "alias": "custom" (opt), "ttlDays": 30 (opt) }
       Response: { "shortUrl": "https://short.ly/aB3kX" }

GET    /{shortKey}
       Response: 302 Location: <longUrl>

DELETE /api/v1/{shortKey}    (authenticated)

GET    /api/v1/{shortKey}/stats
       Response: { clicks, createdAt, expiresAt }
```

## Database Choice

- **Primary store:** PostgreSQL (ACID for writes, simple to operate)
- **If write throughput grows 10x:** Cassandra with `short_key` as partition key — O(1) reads

## Caching Strategy

- Cache: Redis with LRU eviction
- TTL in cache mirrors `expires_at` in DB
- Cache ~20% of hot URLs → covers ~80% of traffic (Zipf distribution)
