# System Design — Daily Practice

One system design problem per day. Each entry covers requirements, components, architecture decisions, and trade-offs.

## Structure

Each day folder follows the format `day-{number}-{problem-name}/` and contains:

```
day-001-tiny-url/
  README.md        ← problem statement, requirements, capacity estimation
  design.md        ← HLD, components, APIs, data models, scalability
  trade-offs.md    ← decisions made and why, failure modes
  diagram.md       ← ASCII architecture + data flow diagrams
```

---

## Index

| Day | Folder | Topic | Category | Status |
|-----|--------|-------|----------|--------|
| 001 | [day-001-tiny-url](./day-001-tiny-url/README.md) | URL Shortener (TinyURL) | Web / Storage | ✅ Done |
| 002 | [day-002-file-sharing](./day-002-file-sharing/README.md) | File Sharing System (Pastebin) | Web / Storage | 🔜 Up Next |
| 003 | [day-003-dropbox](./day-003-dropbox/README.md) | Cloud Storage (Dropbox) | Storage / Sync | ⬜ Pending |
| 004 | [day-004-youtube](./day-004-youtube/README.md) | Video Streaming (YouTube) | Streaming / CDN | ⬜ Pending |
| 005 | [day-005-netflix](./day-005-netflix/README.md) | Netflix (Streaming + CDN) | Streaming / CDN | ⬜ Pending |
| 006 | [day-006-instagram](./day-006-instagram/README.md) | Instagram (Photo Feed) | Social / Feed | ⬜ Pending |
| 007 | [day-007-twitter](./day-007-twitter/README.md) | Twitter (Social Feed + Timeline) | Social / Feed | ⬜ Pending |
| 008 | [day-008-whatsapp](./day-008-whatsapp/README.md) | WhatsApp (Chat / Messaging) | Messaging / Real-time | ⬜ Pending |
| 009 | [day-009-uber](./day-009-uber/README.md) | Uber (Ride-Sharing / Location) | Geospatial / Real-time | ⬜ Pending |
| 010 | [day-010-typeahead](./day-010-typeahead/README.md) | Google Typeahead / Autocomplete | Search / Trie | ⬜ Pending |
| 011 | [day-011-rate-limiter](./day-011-rate-limiter/README.md) | Rate Limiter | Infrastructure | ⬜ Pending |
| 012 | [day-012-distributed-cache](./day-012-distributed-cache/README.md) | Distributed Cache (Redis-like) | Infrastructure | ⬜ Pending |
| 013 | [day-013-notification-system](./day-013-notification-system/README.md) | Notification System | Messaging / Push | ⬜ Pending |
| 014 | [day-014-web-crawler](./day-014-web-crawler/README.md) | Web Crawler | Data Ingestion | ⬜ Pending |
| 015 | [day-015-ticket-booking](./day-015-ticket-booking/README.md) | Ticket Booking (BookMyShow) | Commerce / Concurrency | ⬜ Pending |
| 016 | [day-016-payment-system](./day-016-payment-system/README.md) | Payment System | Finance / Consistency | ⬜ Pending |
| 017 | [day-017-api-gateway](./day-017-api-gateway/README.md) | API Gateway | Infrastructure | ⬜ Pending |
| 018 | [day-018-search-engine](./day-018-search-engine/README.md) | Search Engine (Google-lite) | Search / Indexing | ⬜ Pending |
| 019 | [day-019-news-feed](./day-019-news-feed/README.md) | News Feed Aggregator | Social / Feed | ⬜ Pending |
| 020 | [day-020-ecommerce](./day-020-ecommerce/README.md) | E-Commerce Platform (Amazon-lite) | Commerce / Catalog | ⬜ Pending |

---

## Must-Learn Concepts Covered Across These Designs

| Concept | Appears In |
|---------|-----------|
| Consistent Hashing | 012, 005, 007 |
| CAP Theorem & trade-offs | All |
| SQL vs NoSQL selection | 001, 002, 003, 007 |
| CDN & edge caching | 004, 005 |
| Message queues (Kafka/SQS) | 001, 006, 008, 013, 019 |
| Idempotency & exactly-once delivery | 008, 016 |
| Database sharding & partitioning | 007, 009, 018 |
| Bloom filters | 014, 018 |
| Rate limiting algorithms | 011, 017 |
| Distributed locking | 015, 016 |
| Event sourcing / CQRS | 016, 020 |
| Geohashing / proximity search | 009 |
| Trie / prefix trees | 010 |
| WebSocket / long-polling | 008, 009 |

---

> Goal: Build deep intuition for distributed systems, one design at a time.
