# Day 006 — Trade-offs: Photo Feed & Social Network (Instagram)

## 1) Core Design Decisions

### Decision 1: Feed Fan-out Strategy

| Option | How it works | Pros | Cons | Verdict |
|--------|-------------|------|------|---------|
| **Fan-out on write (push)** | On post, push to all followers' feed queues | O(1) read, always fast | Write storm for celebrities (600M followers) | ✅ Use for non-celebrities |
| **Fan-in on read (pull)** | At read time, query all followed users' posts and merge | No write amplification | O(n) merge at read, slow for users following 5K accounts | ✅ Use for celebrities at read time |
| **Hybrid (push + pull)** | Push for < 1M followers; pull for celebrities | Balances both extremes | More complex logic; need celebrity flag | ✅ Chosen approach |

**Celebrity threshold:** 1M followers (configurable). When exceeded, a background job flips `is_celebrity=true`; fanout is disabled going forward. On read, celebrity posts are merged from the Post DB.

**Fanout consistency:** Feed is eventually consistent. A follower may see a new post within 30–60 seconds of posting (propagation time through the worker fleet). Acceptable for social feeds.

---

### Decision 2: Feed Storage — Redis vs DB-backed feed

| Option | Pros | Cons |
|--------|------|------|
| **Redis Sorted Set** | Sub-millisecond reads, native sorted set, TTL-based eviction | Memory cost; volatile (must rebuild on eviction) |
| **Cassandra feed table** | Durable, cheap, handles huge fan-out | Slower reads (disk-backed); more complex pagination |
| **Hybrid: Redis + Cassandra** | Redis for hot users; Cassandra as durable fallback | Operational complexity |

**Decision:** Redis as the primary feed cache (sorted set, capped at 1,000 items), with durable fan-out in a Cassandra `user_feed` table as the source of truth. Cold starts read from Cassandra and repopulate Redis.

---

### Decision 3: Media Storage Format

| Option | Pros | Cons |
|--------|------|------|
| **Single original only** | Simple | Serving large originals to mobile wastes bandwidth |
| **Multiple pre-rendered sizes** | Fast delivery at each device resolution | Storage multiplied; pipeline complexity |
| **On-demand resize (imgproxy/Thumbor)** | Storage-efficient; flexible | Latency on first request; origin load spikes |

**Decision:** Pre-render 3 sizes (1080p, 720p, thumbnail) on upload via async pipeline. CDN caches immutable paths forever. On-demand resize only for unusual sizes (e.g., profile picture crops).

---

### Decision 4: Story Expiry Mechanism

| Option | Pros | Cons |
|--------|------|------|
| **Background scanner (cron)** | Simple to implement | O(N) scan every hour; misses exact expiry; wastes DB CPU |
| **Cassandra native TTL** | Zero-overhead; handled internally by engine | Hard to extend TTL after creation |
| **Redis TTL + lazy delete** | Exact expiry; automatic eviction | Redis is volatile; need durable backup |
| **Kafka-scheduled expiry event** | Decoupled; reliable | Adds pipeline complexity |

**Decision:** Cassandra native TTL (86400s) for the stories table. Redis sorted set with `EXPIREAT` for the story-feed query (no scanning needed). S3 lifecycle rule deletes media after 48h.

---

### Decision 5: Like Count Storage — Exact vs Approximate

| Option | Pros | Cons |
|--------|------|------|
| **Relational count query** (`SELECT COUNT(*)`) | Always exact | Expensive at 4B likes/day; lock contention |
| **Redis INCR (sharded counters)** | O(1) increment; fast read | Approximate; lost on Redis eviction |
| **Denormalized column in Post table** | Fast read; single source | Write contention on hot posts |
| **Write-behind + eventual sync** | Handles spikes; no contention | Brief inconsistency |

**Decision:** Redis `INCR` sharded by `post_id mod 100` for live counts; async Kafka consumer persists aggregated counts to the Post DB every 30s. Post table `like_count` column reflects the last-synced value (eventual). Displayed counts round to thousands ("1.2M likes") at > 10K, hiding small inaccuracies.

---

## 2) Secondary Trade-offs

### Notification Aggregation Window Size
- **30s window:** Feels real-time; moderate aggregation.
- **5 min window:** Heavy aggregation; fewer notifications for creator; less "realtime" feel.
- **Decision:** 30s window for comments/direct mentions (high signal), 5-min window for like counts (low signal per event).

### Follow Graph — SQL vs Wide-Column
- SQL `follows` table with composite index `(follower_id, following_id)` works fine up to ~10B rows.
- Cassandra/Dynamo makes more sense when a single user's follower list is tens of millions and you need sub-10ms paginated reads.
- **Decision:** Cassandra with two tables (`followers_by_user`, `following_by_user`) for O(1) partition key lookups.

### Content Moderation
- Sync (block upload until scanned): safer but adds latency.
- Async (allow upload, retroactively remove if flagged): faster UX, brief window of visibility.
- **Decision:** Async for images (< 1% flagged); allow optimistic posting. Sync for videos > 30s (higher risk).

---

## 3) Failure Modes & Mitigations

| Failure | Impact | Mitigation |
|---------|--------|-----------|
| Fan-out worker crashes mid-way | Some followers miss post in feed | Kafka offsets not committed until page of followers processed; retry resumes where left off |
| Redis feed cache wiped | Cold start for all users | Fall back to Cassandra `user_feed`; backfill Redis on first read |
| Follow Graph service down | Can't follow/unfollow; fan-out pauses | Queue follow events in Kafka; replay when recovered; reads fall back to cached copy |
| Media pipeline backlogged | Posts stuck in PROCESSING | Auto-scale Media Workers off Kafka consumer lag metric; alert on SLA breach |
| Post DB shard failure | Writes fail; reads degraded | Read replicas serve reads; writes re-routed to standby; brief write pause |
| CDN origin unavailable | Image loads fail globally | CDN serves from cache; high TTL (1 year) means most images still available |
| Story expiry delayed | Stories visible past 24h | Cassandra TTL is strict; Redis TTL-based feed check as secondary gate |
| Notification service overloaded | Delayed notifications | Kafka provides backpressure; clients poll on reconnect as fallback |

---

## 4) Migration Triggers (when to revisit decisions)

| Signal | Trigger to revisit |
|--------|--------------------|
| Feed latency p99 > 300ms | Move celebrity threshold lower; add more Redis shards |
| Fan-out worker lag > 2 min | Increase worker parallelism; shard Kafka topic by user_id range |
| Redis memory > 80% | Cap feed at 500 items; move older entries to Cassandra only |
| Like counter drift > 5% | Add a nightly reconciliation job from raw events in Kafka |
| Search index lag > 5 min | Dedicate search indexer consumer group with higher priority |
