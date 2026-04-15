# Day 006 — Design: Photo Feed & Social Network (Instagram)

## 1) High-Level Architecture

The system is split into four planes:

1. **Interaction plane** — upload, like, comment, follow; strong consistency required for graph.
2. **Media plane** — blob storage, processing pipeline, CDN delivery.
3. **Feed plane** — the hardest part: generating a ranked, personalized feed at scale.
4. **Engagement/Notification plane** — eventual delivery of notifications, story expiry.

### Core Services

| Service | Responsibility |
|---------|---------------|
| API Gateway | Auth (JWT/OAuth), rate limiting, routing |
| Post Service | Create/delete posts, coordinate media upload |
| Media Service | Pre-signed upload URLs, async resizing/thumbnail pipeline |
| Follow Graph Service | Follow/unfollow, follower/following lists |
| Feed Service | Feed generation, caching, hybrid push/pull |
| Like Service | Like/unlike, approximate counts |
| Comment Service | CRUD comments, nested threading |
| Story Service | Create/delete stories, 24-hour TTL expiry |
| Notification Service | Push notifications with rate limiting/aggregation |
| Search Service | Users, hashtags, locations (Elasticsearch) |
| Profile Service | User bio, post count, aggregated stats |

---

## 2) Data Model (Logical)

### Post
```
post_id        UUID PK
user_id        UUID FK
media_keys[]   string[]     — S3 object keys (1-10 items)
caption        text
hashtags[]     string[]     — extracted on write, indexed
location_name  string (opt)
created_at     timestamp
like_count     int          — denormalized approximate counter
comment_count  int          — denormalized approximate counter
```

### Follow Graph (Cassandra / wide-column)
```
followers_by_user:  user_id → [follower_id, created_at]  — "Who follows me?"
following_by_user:  user_id → [following_id, created_at] — "Who do I follow?"
```
Both tables kept for O(1) lookups in each direction.

### User Feed Cache (Redis Sorted Set)
```
feed:{userId}  → sorted set  { post_id → score (timestamp or rank) }
```
Capped at 500–1,000 entries per user (most recent). Backed by a durable feed store for users who haven't opened the app in > N days.

### Like
```
post_id  UUID
user_id  UUID
created_at timestamp
PRIMARY KEY (post_id, user_id)
```

### Comment
```
comment_id      UUID PK
post_id         UUID
user_id         UUID
text            text
parent_id       UUID (nullable for top-level)
created_at      timestamp
```

### Story
```
story_id    UUID PK
user_id     UUID
media_key   string
created_at  timestamp
expires_at  timestamp   — created_at + 24h; DB TTL or Redis TTL
```

### Media Object (S3 layout)
```
posts/{userId}/{postId}/original.jpg
posts/{userId}/{postId}/1080.jpg
posts/{userId}/{postId}/720.jpg
posts/{userId}/{postId}/thumb.jpg
stories/{userId}/{storyId}/original.jpg
stories/{userId}/{storyId}/thumb.jpg
```

---

## 3) API Design

### 3.1 Posts
```
POST   /posts
  Body: { caption, hashtags[], locationName?, mediaCount }
  Response: { postId, mediaUploadUrls: [{key, uploadUrl}] }

POST   /posts/{postId}/finalize
  Body: { idempotencyKey }
  Triggers: async media processing + feed fan-out

DELETE /posts/{postId}

POST   /posts/{postId}/like        idempotent toggle
POST   /posts/{postId}/unlike

GET    /posts/{postId}/comments?cursor=...&limit=20
POST   /posts/{postId}/comments
  Body: { text, parentId? }
```

### 3.2 Feed
```
GET    /feed?cursor=...&limit=20
  Returns: [{ post, user, likeCount, commentCount, likedByMe }]
  — cursor-based pagination (timestamp or opaque token)
```

### 3.3 Follow Graph
```
POST   /users/{userId}/follow
POST   /users/{userId}/unfollow
GET    /users/{userId}/followers?cursor=...
GET    /users/{userId}/following?cursor=...
```

### 3.4 Stories
```
POST   /stories
  Body: { mediaCount: 1 }
  Response: { storyId, mediaUploadUrls }

GET    /stories/feed
  Returns: list of story "bubbles" for users you follow with unviewed count

GET    /stories/{userId}
  Returns: all active stories for user (expires_at > now)
```

### 3.5 Search
```
GET    /search?q=...&type=users|hashtags|all&cursor=...
```

---

## 4) Key Workflows

### A) Post Upload Flow
1. Client calls `POST /posts` → Post Service creates a `PENDING` post record and returns pre-signed S3 URLs (one per image/video via Media Service).
2. Client `PUT`s each file directly to S3.
3. Client calls `POST /posts/{postId}/finalize` → Post Service marks post as `PROCESSING`, publishes `POST_CREATED` event to Kafka.
4. **Media Processing Worker** (async):
   - Reads `POST_CREATED` from Kafka.
   - Resizes to multiple resolutions (1080, 720, thumbnail), runs content moderation check.
   - Writes all variants to S3.
   - Publishes `POST_READY` event.
5. **Feed Fanout Worker** (async, triggered by `POST_READY`):
   - Fetches the poster's follower list from Follow Graph Service.
   - For users with **< 1M followers**: pushes `post_id` to each follower's Redis feed sorted set.
   - For **celebrities (≥ 1M followers)**: skips fan-out (see hybrid strategy in Section 5).
6. Post status becomes `PUBLISHED` in Post Service DB.

**Why async media processing?** Resizing can take 500ms–5s depending on video. Client only waits for S3 URL, not processing.

---

### B) Feed Read Flow (Hybrid Fan-out)

```
GET /feed
  1. Feed Service reads user's pre-computed feed from Redis (sorted set).
     — Contains posts from non-celebrity users they follow (fan-out on write).

  2. Feed Service fetches celebrity posts separately:
     — Get list of celebrity accounts this user follows (cached).
     — For each, read their most recent N posts from Post Service.

  3. Merge + rank (chronological or ML score):
     — Union of pre-computed feed + celebrity posts, sorted by score.
     — Paginate and return top 20.

  4. Enrich: batch-fetch like counts, comment counts, liked-by-me flags.
```

**Celebrity threshold:** configurable flag on user record (e.g., `is_celebrity = true` when followers > 1M). Updated by a background job.

---

### C) Story Expiry (24-hour TTL)
- Stories stored in Cassandra with TTL column (native Cassandra TTL automatically tombstones at expiry).
- Story IDs also stored in Redis with `EXPIREAT` set to `created_at + 86400s`.
- Feed bubble query reads from Redis; expired keys are gone without any scan.
- Media objects in S3 cleaned by a daily lifecycle rule (delete objects with prefix `stories/` older than 48h).

---

### D) Notification Fan-out with Aggregation
When a viral post receives 500K likes in 10 minutes, we **must not** push 500K notifications to the creator.

Strategy:
1. All like/comment/follow events land in Kafka topic `engagement-events`.
2. **Notification Aggregator Worker** windows events per (recipient, event_type, target) over a 30-second tumbling window.
3. Aggregated result: `"Cristiano and 49,999 others liked your photo"` — one push notification per window, not 50,000.
4. Push via FCM/APNs through the Notification Service.

---

### E) Search Indexing
- On `POST_READY`: Post Service publishes to a `search-index` Kafka topic.
- Search Indexer writes to Elasticsearch index with fields: `userId`, `caption`, `hashtags[]`, `created_at`, `like_count`.
- Hashtag searches use a term query on `hashtags` field; user search on `username` + `display_name`.
- Search results ranked by a mix of recency + like_count.

---

## 5) Fan-out Strategy (The Core Design Decision)

### Problem
- Ronaldo has 600M followers.
- Fan-out on write = 600M Redis writes in minutes → not feasible.
- Fan-in on read = merge posts from all accounts you follow → too slow for users following 5,000 accounts.

### Hybrid Solution (Industry Standard)

| User Type | Strategy | Why |
|-----------|----------|-----|
| Regular user (< 1M followers) | **Fan-out on write** (push) | Manageable blast; fast reads for followers |
| Celebrity (≥ 1M followers) | **Fan-in on read** (pull at read time) | Avoids write storm |

**On write (non-celebrity post):**
- Fanout Worker pushes post_id to each follower's `feed:{userId}` Redis sorted set.
- Fanout is parallel (multi-threaded workers, paginated follower list).
- Feed capped at 1,000 items; oldest entries evicted.

**On read (feed load):**
- Step 1: Read pre-computed feed from Redis (covers non-celebrity follows).
- Step 2: For each celebrity followed → fetch their recent posts from Post Service.
- Step 3: Merge + rank.

---

## 6) Caching Strategy

| Layer | Cache | TTL | What's cached |
|-------|-------|-----|---------------|
| API Gateway | Redis | 60s | Auth tokens, rate-limit counters |
| Feed Service | Redis Sorted Set | ~1h active | Pre-computed feed (post_ids + scores) |
| Post metadata | Redis Hash | 5 min | post details for hot posts |
| Like counts | Redis Counter | eventual | Sharded per-post like counts |
| Follow Graph | Redis Set | 10 min | follower/following lists for hot accounts |
| Media | CDN (CloudFront) | 1 year | Immutable photo/video files |
| Manifests | CDN | 5 min | Short-lived manifest files |

---

## 7) Scalability Plan

### Sharding
- **Post DB**: sharded by `user_id` hash. All posts from a user live on the same shard → efficient `GET /users/{userId}/posts`.
- **Follow Graph (Cassandra)**: partitioned by `user_id`; both tables (followers + following) fit this model.
- **Like table (Cassandra)**: partitioned by `post_id` → all likes for a post together.
- **Comments (Cassandra)**: partitioned by `post_id`.

### Feed Service Scalability
- Redis cluster; feed keys distributed across shards by `user_id`.
- Feed Service is stateless and horizontally scalable.
- For inactive users (not opened app in > 7 days), feed is not pre-computed. On first open, a cold start pull is performed and then cached.

### Media Pipeline Scalability
- Media workers are stateless, pull from Kafka; auto-scale based on Kafka consumer group lag.
- S3 handles unlimited scale; CDN absorbs all reads.

### Follow Graph Hotspots
- Celebrity's follower list can be 600M rows. Read paginates in chunks of 5,000 for fan-out.
- Fanout is parallelized across worker fleet; each worker handles a page of followers.
- Estimated time for 600M follower fanout: with 1,000 parallel workers, ~minutes; acceptable for non-real-time feed.

---

## 8) Reliability

| Failure | Mitigation |
|---------|------------|
| Fanout Worker crash mid-fanout | Kafka consumer offset not committed until page done; retry resumes at checkpoint |
| Redis feed cache evicted | Cold-start pull from Post DB on read; re-populate cache |
| S3 object unavailable | Multi-AZ bucket; CDN serves stale from cache |
| Post DB shard failure | Read replica promoted; writes paused briefly |
| Like counter lost | Redis + async Kafka backup; sharded counters rebuilt from DB |
| Notification service down | Kafka retains events; drain when service recovers |

---

## 9) SLOs

| Metric | Target |
|--------|--------|
| Feed load p99 | < 200 ms |
| Photo upload confirmation | < 2 s |
| Story expiry | Within 5 min of 24h |
| Notification delivery | < 30 s (p95) |
| Search results | < 500 ms (p99) |
| Availability | 99.99% |
