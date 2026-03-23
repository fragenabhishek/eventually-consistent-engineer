# Day 002 - File Sharing System (Pastebin)

**Date:** 2026-03-23  
**Category:** Web / Storage  
**Difficulty:** Medium

---

## 1. Problem Statement

Design a file sharing system like Pastebin where users can store and share text snippets using short links.

Given text content, the system should create a shareable URL. Anyone with the URL can view the content until it expires or is deleted.

Example:

- Input: `"How to reverse a linked list..."`
- Output: `https://paste.ly/p/9fK2Qa`

---

## 2. Functional Requirements

- Create a new paste from text content
- Retrieve paste by short ID
- Support optional expiry (10 min, 1 hour, 1 day, never)
- Support optional burn-after-read mode
- Support visibility: public or unlisted
- Support delete by owner token
- Capture basic analytics: view count and last viewed timestamp

### Out of Scope (V1)

- Full user authentication and multi-device sync
- Rich collaborative editing
- Binary file sharing (images/videos)
- Full-text search across all public pastes

---

## 3. Non-Functional Requirements

- High read availability (target 99.99%)
- Low read latency, P99 < 50 ms at service layer
- Durable storage for non-expired pastes
- Read-heavy architecture (view traffic dominates create traffic)
- Eventual consistency acceptable for analytics counters

---

## 4. Back-of-the-Envelope Estimation

Assumptions:

- New pastes/day: 50M
- Read requests/day: 500M
- Average content size: 2 KB
- Metadata per paste: 500 B

Traffic:

- Write QPS: $50,000,000 / 86,400 \approx 579/s$
- Read QPS: $500,000,000 / 86,400 \approx 5,787/s$

Storage:

- Per paste: $2 KB + 0.5 KB = 2.5 KB$
- Daily: $50,000,000 \times 2.5 KB \approx 125 GB/day$
- Yearly: $125 \times 365 \approx 45.6 TB/year$

Cache:

- If top 10% pastes generate ~80% reads, caching hot content greatly reduces DB/object-store reads.

---

## 5. API Contracts (V1)

### Create Paste

`POST /api/v1/pastes`

Request:

```json
{
  "content": "console.log('hello world');",
  "language": "javascript",
  "visibility": "unlisted",
  "expireInSeconds": 86400,
  "burnAfterRead": false
}
```

Response:

```json
{
  "pasteId": "9fK2Qa",
  "url": "https://paste.ly/p/9fK2Qa",
  "deleteToken": "a8b61b4d-8d32-4f0f-8a24-0a5b7ea4c311",
  "expiresAt": "2026-03-24T10:00:00Z"
}
```

### Read Paste

`GET /p/{pasteId}`

Response:

```json
{
  "pasteId": "9fK2Qa",
  "content": "console.log('hello world');",
  "language": "javascript",
  "visibility": "unlisted",
  "createdAt": "2026-03-23T10:00:00Z",
  "expiresAt": "2026-03-24T10:00:00Z"
}
```

### Delete Paste

`DELETE /api/v1/pastes/{pasteId}`

Headers: `X-Delete-Token: <token>`

Response: `204 No Content`

### Stats

`GET /api/v1/pastes/{pasteId}/stats`

Response:

```json
{
  "pasteId": "9fK2Qa",
  "viewCount": 124,
  "lastViewedAt": "2026-03-23T12:45:00Z"
}
```

---

## 6. Data Model (Logical)

### pastes

| Column | Type | Notes |
|--------|------|-------|
| paste_id | VARCHAR(12) PK | Public short ID |
| content_ref | TEXT | Pointer to object storage or inline content |
| content_hash | CHAR(64) | SHA-256 for integrity/dedupe (optional) |
| language | VARCHAR(32) NULL | Syntax hint |
| visibility | VARCHAR(16) | public/unlisted |
| burn_after_read | BOOLEAN | One-time view option |
| delete_token_hash | CHAR(64) | Secure delete authorization |
| created_at | TIMESTAMP | Creation time |
| expires_at | TIMESTAMP NULL | Optional expiry |
| is_deleted | BOOLEAN | Soft delete flag |

### paste_analytics_daily

| Column | Type | Notes |
|--------|------|-------|
| paste_id | VARCHAR(12) | Paste ID |
| day | DATE | Aggregate bucket |
| views | BIGINT | Daily views |

---

## 7. Interview Talking Points

- SQL only vs SQL + object storage?
- How to handle very large snippets safely?
- How to support burn-after-read without race conditions?
- Should public pastes be indexed for search in V1?
- How to prevent abuse (malware links, spam, scraping)?

---

## 8. Final V1 Decisions

- ID generation: random Base62 ID with uniqueness retry
- Primary metadata storage: PostgreSQL
- Content storage: object store (S3-compatible), inline for tiny payloads
- Cache: Redis for hot paste reads
- Analytics: Kafka + async aggregation
- Abuse controls: IP rate limiting + content scanning pipeline
