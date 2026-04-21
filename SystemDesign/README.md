# System Design — Daily Practice

One system design problem per day. Build deep intuition for distributed systems — one design at a time.

---

## How to Use This Section

Each day has **two phases**:

### Phase 1 — Read the requirements (already done for all 20 days)
```
day-XXX-problem/
  README.md    ← problem statement, requirements, capacity estimation,
                  key challenges, clarifying questions
```
Open the README. Read the scenario. Try to sketch the architecture yourself on paper before looking at the solution.

### Phase 2 — Write your design (you fill these in as practice)
```
day-XXX-problem/
  design.md      ← HLD, components, APIs, data models, scalability
  trade-offs.md  ← decisions made and why, failure modes, migration triggers
  diagram.md     ← Mermaid architecture + sequence diagrams
```
Use `TEMPLATE.md` as a starting point. Days 001–006 are fully solved — use them as a reference for style and depth.

---

## Learning Path (Suggested Order)

Work through in this order. Each group builds on the previous.

### Week 1 — Foundation (simpler, high-impact)
| Day | Problem | Why First |
|-----|---------|-----------|
| 001 | URL Shortener | Classic intro — caching, KGS, redirect patterns |
| 002 | File Sharing (Pastebin) | Extends 001 with object storage + burn-after-read |
| 011 | Rate Limiter | Pure infrastructure — used in every other system |
| 012 | Distributed Cache | Redis internals — used in every other system |
| 017 | API Gateway | Single entry point pattern — ties everything together |

### Week 2 — Storage & Sync
| Day | Problem | Core Concepts |
|-----|---------|--------------|
| 003 | Cloud Storage (Dropbox) | Chunked upload, delta sync, deduplication |
| 014 | Web Crawler | BFS, Bloom filters, politeness |
| 018 | Search Engine | Inverted index, ranking, distributed indexing |
| 010 | Typeahead | Trie, Top-K, edge caching |

### Week 3 — Real-Time & Social
| Day | Problem | Core Concepts |
|-----|---------|--------------|
| 006 | Instagram | Fan-out on write, CDN for media, story TTL |
| 007 | Twitter | Fan-out, Snowflake IDs, trending algorithm |
| 019 | News Feed | Ranking pipeline, ML features, cursor pagination |
| 008 | WhatsApp | WebSocket, exactly-once delivery, group fan-out |
| 009 | Uber | Geohashing, real-time location, matching |

### Week 4 — Streaming & Commerce
| Day | Problem | Core Concepts |
|-----|---------|--------------|
| 004 | YouTube | Transcoding pipeline, adaptive bitrate, CDN |
| 005 | Netflix | CDN architecture, content pre-positioning, DRM |
| 013 | Notification System | Multi-channel, deduplication, scheduling |
| 015 | Ticket Booking | Distributed locking, hold-and-pay, flash sale |
| 016 | Payment System | Saga pattern, idempotency, immutable ledger |
| 020 | E-Commerce | Combines everything — treat as final exam |

---

## Index

| Day | Folder | Topic | Category | Status |
|-----|--------|-------|----------|--------|
| 001 | [day-001-tiny-url](./day-001-tiny-url/README.md) | URL Shortener (TinyURL) | Web / Storage | ✅ Fully solved |
| 002 | [day-002-file-sharing](./day-002-file-sharing/README.md) | File Sharing System (Pastebin) | Web / Storage | ✅ Fully solved |
| 003 | [day-003-dropbox](./day-003-dropbox/README.md) | Cloud Storage (Dropbox) | Storage / Sync | ✅ Fully solved |
| 004 | [day-004-youtube](./day-004-youtube/README.md) | Video Streaming (YouTube) | Streaming / CDN | ✅ Fully solved |
| 005 | [day-005-netflix](./day-005-netflix/README.md) | Netflix (Streaming + CDN) | Streaming / CDN | ✅ Fully solved |
| 006 | [day-006-instagram](./day-006-instagram/README.md) | Instagram (Photo Feed) | Social / Feed | ✅ Fully solved |
| 007 | [day-007-twitter](./day-007-twitter/README.md) | Twitter (Social Feed + Timeline) | Social / Feed | 📋 Requirements ready — design it! |
| 008 | [day-008-whatsapp](./day-008-whatsapp/README.md) | WhatsApp (Chat / Messaging) | Messaging / Real-time | 📋 Requirements ready — design it! |
| 009 | [day-009-uber](./day-009-uber/README.md) | Uber (Ride-Sharing / Location) | Geospatial / Real-time | 📋 Requirements ready — design it! |
| 010 | [day-010-typeahead](./day-010-typeahead/README.md) | Google Typeahead / Autocomplete | Search / Trie | 📋 Requirements ready — design it! |
| 011 | [day-011-rate-limiter](./day-011-rate-limiter/README.md) | Rate Limiter | Infrastructure | 📋 Requirements ready — design it! |
| 012 | [day-012-distributed-cache](./day-012-distributed-cache/README.md) | Distributed Cache (Redis-like) | Infrastructure | 📋 Requirements ready — design it! |
| 013 | [day-013-notification-system](./day-013-notification-system/README.md) | Notification System | Messaging / Push | 📋 Requirements ready — design it! |
| 014 | [day-014-web-crawler](./day-014-web-crawler/README.md) | Web Crawler | Data Ingestion | 📋 Requirements ready — design it! |
| 015 | [day-015-ticket-booking](./day-015-ticket-booking/README.md) | Ticket Booking (BookMyShow) | Commerce / Concurrency | 📋 Requirements ready — design it! |
| 016 | [day-016-payment-system](./day-016-payment-system/README.md) | Payment System | Finance / Consistency | 📋 Requirements ready — design it! |
| 017 | [day-017-api-gateway](./day-017-api-gateway/README.md) | API Gateway | Infrastructure | 📋 Requirements ready — design it! |
| 018 | [day-018-search-engine](./day-018-search-engine/README.md) | Search Engine (Google-lite) | Search / Indexing | 📋 Requirements ready — design it! |
| 019 | [day-019-news-feed](./day-019-news-feed/README.md) | News Feed Aggregator | Social / Feed | 📋 Requirements ready — design it! |
| 020 | [day-020-ecommerce](./day-020-ecommerce/README.md) | E-Commerce Platform (Amazon-lite) | Commerce / Catalog | 📋 Requirements ready — design it! |

---

## Must-Learn Concepts — Coverage Map

| Concept | Days Covered |
|---------|-------------|
| **Caching (Redis, CDN, cache-aside)** | 001, 002, 003, 004, 005, 006, 010, 011, 012, 017 |
| **CAP Theorem & consistency trade-offs** | All |
| **SQL vs NoSQL selection** | 001, 002, 003, 007, 012, 016, 020 |
| **Consistent Hashing** | 005, 007, 009, 012 |
| **Fan-out on write vs fan-in on read** | 006, 007, 019 |
| **CDN & edge caching** | 004, 005, 006, 010 |
| **Object Storage (S3-style)** | 002, 003, 004, 005, 006, 020 |
| **Chunked / resumable upload** | 003, 004 |
| **Message queues (Kafka / SQS)** | 001, 006, 008, 013, 016, 019, 020 |
| **Idempotency & exactly-once delivery** | 008, 013, 015, 016 |
| **Saga pattern (distributed transactions)** | 015, 016, 020 |
| **Event sourcing / CQRS** | 016, 020 |
| **Outbox pattern** | 016, 020 |
| **Rate limiting algorithms** | 011, 017 |
| **Distributed locking (Redis SETNX)** | 009, 015, 016 |
| **Optimistic vs pessimistic locking** | 015, 016 |
| **Database sharding & partitioning** | 007, 009, 018 |
| **Read replicas & CQRS read model** | 001, 002, 015, 020 |
| **Bloom filters** | 014, 018 |
| **Inverted index & search** | 018, 020 |
| **Trie / prefix trees** | 010 |
| **Geohashing / S2 cells** | 009 |
| **Adaptive bitrate streaming (HLS/DASH)** | 004, 005 |
| **WebSocket / SSE / long-polling** | 008, 009, 019 |
| **Distributed ID generation (Snowflake)** | 007 |
| **Service Discovery & API Gateway** | 017, 020 |
| **Circuit Breaker pattern** | 017, 020 |
| **LRU / LFU eviction** | 010, 012 |
| **Content deduplication (SimHash, SHA)** | 003, 014 |
| **Delta sync / chunked transfers** | 003 |
| **Notification fan-out (push, email, SMS)** | 006, 007, 013, 020 |
| **Virtual waiting room / queue** | 015 |
| **Real-time feed ranking pipeline** | 006, 007, 019 |
| **DRM & content protection** | 005 |

---

> **Reference designs:** See `day-001-tiny-url/`, `day-002-file-sharing/`, `day-003-dropbox/`, `day-004-youtube/`, `day-005-netflix/`, and `day-006-instagram/` for fully worked examples.  
> **Blank template:** Copy `TEMPLATE.md` into your day folder as a starting point for `design.md`.
