# Day 007 — Social Feed & Timeline (Twitter / X)

> **Interview Goal:** Design a microblogging platform with tweets, follows, trending topics, and a real-time timeline.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Twitter (now X) has 400M monthly active users who post short messages (tweets) and consume a real-time feed of content from people they follow. The distinguishing features from Instagram are: text-first content, real-time trending topics, and the timeline must feel truly live — a tweet from a breaking news account should appear in followers' feeds within seconds.

---

## Functional Requirements

1. Users can **post tweets** (up to 280 characters, optionally with photos/videos).
2. Users can **retweet**, **quote-tweet**, **like**, and **reply** to tweets.
3. Users see a **home timeline** — tweets from people they follow, newest first (with ranking).
4. Users can **follow / unfollow** accounts.
5. Support **trending topics** — most-tweeted hashtags in a region over the last hour.
6. Users can **search** tweets by keyword, hashtag, or @mention.
7. Users receive **notifications** for likes, replies, retweets, and new followers.
8. Support **Twitter Spaces** (audio rooms) — out of scope for core design, mention only.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Timeline latency | < 200 ms for cached timelines |
| Tweet fan-out latency | New tweet visible in followers' timeline within 5 seconds |
| Scale | 400M MAU; 150M DAU; ~500M tweets/day |
| Read:Write ratio | ~100:1 |
| Trending refresh | Updated every 30 seconds |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Tweets/day | 500M |
| Tweets/second (avg) | ~6,000 |
| Tweets/second (peak) | ~30,000 (breaking news events) |
| Avg tweet size | 300 bytes |
| Storage/day (tweets only) | ~150 GB/day |
| Timeline reads/day | 15B (150M DAU × 100 reads each) |

---

## Core API to Design

```
POST   /tweets                         → post a tweet
DELETE /tweets/{tweetId}               → delete a tweet
POST   /tweets/{tweetId}/retweet       → retweet
POST   /tweets/{tweetId}/like          → like/unlike
GET    /timeline/home                  → home timeline (paginated)
GET    /timeline/user/{userId}         → profile timeline
POST   /users/{userId}/follow          → follow a user
GET    /trending?region={code}         → trending topics by region
GET    /search?q={query}               → search tweets/hashtags/users
GET    /notifications                  → user notifications
```

---

## Key Challenges to Think About

- **Timeline fan-out:**
  - Twitter originally used pull (compute timeline at request time). At scale, this is too slow.
  - Twitter now uses a hybrid: pre-computed timelines stored in Redis cache (fan-out on write).
  - Problem: When Elon Musk (100M+ followers) tweets, fan-out creates ~100M cache writes in seconds. How do you handle this?
- **Hotspot mitigation:** Breaking news causes massive read spikes on a few tweet IDs. How do you protect the database?
- **Trending algorithm:** Trending is not just "most hashtags ever" — it's "unusually high volume in the last hour compared to baseline." How do you compute this at 500M tweets/day?
- **Search freshness:** A tweet should appear in search results within 30 seconds of posting. How do you index 6,000 tweets/second?
- **Distributed timeline ordering:** Timelines should be ordered by time. But in a distributed system, who owns the canonical time? (Hint: Snowflake IDs)
- **Delete propagation:** When a tweet is deleted, it must disappear from followers' cached timelines. How?

---

## Clarifying Questions (practice asking these in an interview)

1. Should the timeline be pure chronological or algorithmically ranked (like Twitter's "For You" tab)?
2. Do we need to support polls, Twitter Spaces, or Fleets (stories)?
3. What does "trending" mean — global, national, or city-level?
4. How long should tweets be stored? Can users fully delete their data?
5. Do we need to support lists (curated groups of accounts to follow)?
6. Is real-time (WebSocket) delivery required for timeline, or polling acceptable?

---

## Concepts Tested

`Fan-out on write vs read` · `Snowflake ID (distributed ID generation)` · `Redis sorted sets for timelines` · `Trending computation (sliding window)` · `Search indexing (Elasticsearch)` · `Hot key problem` · `Event-driven architecture` · `Cache invalidation`
