# Day 001 - URL Shortener (TinyURL)

**Date:** 2026-03-19  
**Category:** Web / Storage  
**Difficulty:** Medium

---

## 1. Problem Statement

Design a URL shortening service like TinyURL.

Given a long URL, the system should generate a short URL and redirect users to the original URL when that short URL is visited.

Example:

- Input: `https://www.example.com/a/very/long/path?utm_source=social`
- Output: `https://tiny.ly/aB3kX9`

---

## 2. Functional Requirements

- Create short URL for a valid long URL
- Redirect short URL to original URL
- Support custom alias (optional)
- Support expiration (optional)
- Support delete/deactivate short URL
- Support basic analytics: clicks and last-visited time

### Out of Scope (V1)

- Auth, billing, and paid plans
- QR code generation
- Rich analytics dashboard with segmentation

---

## 3. Non-Functional Requirements

- High availability for redirect API (target 99.99%)
- Low redirect latency, P99 < 10 ms at service layer
- Read-heavy architecture (redirect traffic is much higher than create traffic)
- Durable mapping storage
- Eventual consistency acceptable for analytics counters

---

## 4. Back-of-the-Envelope Estimation

Assumptions:

- New short URLs/day: 100M
- Redirects/day: 10B
- Average stored bytes per mapping row: 500B

Traffic:

- Write QPS: $100,000,000 / 86,400 \approx 1,157/s$
- Read QPS: $10,000,000,000 / 86,400 \approx 115,740/s$

Storage:

- Daily: $100,000,000 \times 500B \approx 50GB/day$
- Yearly: $50 \times 365 \approx 18.25TB/year$

Cache:

- If top 20% keys generate ~80% traffic, caching hot keys can reduce most DB reads.

---

## 5. API Contracts (V1)

### Create

`POST /api/v1/urls`

Request:

```json
{
	"longUrl": "https://example.com/products/123",
	"customAlias": "my-product",
	"expireAt": "2026-12-31T23:59:59Z"
}
```

Response:

```json
{
	"shortKey": "aB3kX9",
	"shortUrl": "https://tiny.ly/aB3kX9",
	"longUrl": "https://example.com/products/123",
	"expireAt": "2026-12-31T23:59:59Z"
}
```

### Redirect

`GET /{shortKey}`

Response: `302 Found` with `Location` header pointing to long URL.

### Stats

`GET /api/v1/urls/{shortKey}/stats`

Response:

```json
{
	"shortKey": "aB3kX9",
	"clickCount": 123456,
	"createdAt": "2026-03-19T10:00:00Z",
	"lastVisitedAt": "2026-03-19T12:45:00Z"
}
```

### Delete

`DELETE /api/v1/urls/{shortKey}`

Response: `204 No Content`

---

## 6. Data Model (Logical)

### urls

| Column | Type | Notes |
|--------|------|-------|
| short_key | VARCHAR(12) PK | Unique URL key |
| long_url | TEXT | Original URL |
| user_id | UUID NULL | Null for anonymous |
| created_at | TIMESTAMP | Creation time |
| expires_at | TIMESTAMP NULL | Optional expiry |
| is_deleted | BOOLEAN | Soft delete |

### url_analytics_daily

| Column | Type | Notes |
|--------|------|-------|
| short_key | VARCHAR(12) | URL key |
| day | DATE | Aggregate bucket |
| clicks | BIGINT | Daily clicks |

---

## 7. Interview Talking Points

- How to generate collision-free short keys?
- 301 vs 302 in redirects?
- Why analytics should be asynchronous?
- How to avoid cache stampedes?
- When to migrate SQL -> NoSQL?

---

## 8. Final V1 Decisions

- Key generation: pre-generated key pool (KGS) + Base62 keys
- Primary storage: PostgreSQL
- Cache: Redis for hot key lookups
- Redirect: HTTP 302 for analytics correctness
- Analytics: Kafka + async consumers
