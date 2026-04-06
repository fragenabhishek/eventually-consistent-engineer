# Day 019 — News Feed Aggregator

> **Interview Goal:** Design a personalised news feed that aggregates content from multiple sources and ranks it for each user.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Think of Facebook's News Feed, LinkedIn's home feed, or a personalised news aggregator like Flipboard. Users follow people, pages, and topics. The system collects new content from all sources and presents a ranked, personalised feed — showing content that is most relevant and engaging to each user. The challenge: 500 million users, each with a unique feed, updating in near real-time.

---

## Functional Requirements

1. Users can **follow** people, pages, topics, and hashtags.
2. Users see a **personalised home feed** composed of posts from their follows, ranked by relevance.
3. Feed includes content of multiple types: **text posts, photos, videos, shared articles, events**.
4. Feed is **near real-time** — a post from a followed user appears within 30 seconds.
5. Users can **like, comment, and share** posts from the feed.
6. Support **infinite scroll** — load more posts as the user scrolls.
7. Feed is **ranked** (not just chronological) — more relevant, engaging content ranked higher.
8. Users can **filter** their feed: "All", "Friends only", "Pages only".
9. Track **engagement signals** (impressions, clicks, time-spent) to improve future ranking.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Feed load latency | < 200 ms (cached) |
| Feed freshness | New posts from follows appear within 30 seconds |
| Scale | 500M DAU; each loads feed 5–10 times/day |
| Posts/day | 500M new posts |
| Read:Write ratio | ~1000:1 |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Daily active users | 500M |
| Feed reads/day | 3B (500M × 6 loads) |
| Feed reads/second (avg) | ~35,000 |
| Posts/day | 500M |
| Posts/second | ~6,000 |
| Avg post size (metadata) | 500 bytes |
| Engagement events/day | 5B (likes, views, shares) |

---

## Core API to Design

```
GET    /feed                            → personalised home feed (paginated cursor-based)
    query: cursor, limit, filter
    returns: [posts sorted by rank score, with next_cursor]

POST   /posts                           → create a post
POST   /posts/{postId}/like             → like or unlike
POST   /posts/{postId}/comment          → add a comment
POST   /posts/{postId}/share            → share a post
POST   /follows                         → follow a user, page, or topic
DELETE /follows/{entityId}              → unfollow

POST   /feed/events                     → report engagement event (impression, click)
    body: { postId, eventType, durationMs }
```

---

## Key Challenges to Think About

- **Feed generation strategy — Push vs Pull (same as Instagram/Twitter Day 006/007, but with ranking):**
  - With 500M users, you can't compute each person's feed on demand in 200 ms.
  - Pre-computing and caching each user's feed is feasible — but 500M cached feeds × 50 posts × 500 bytes = 12.5 TB just in Redis.
  - What's the right hybrid approach? Think about which users get fan-out-on-write vs fan-in-on-read.
  
- **Ranking algorithm:** The feed isn't chronological — it's personalised. What signals matter?
  - Recency (how fresh is the post?)
  - Social affinity (how often do you interact with this author?)
  - Engagement rate (how much do others like/comment on this type of post?)
  - User's own preferences (based on past engagement)
  - How do you compute a ranking score without making each feed load slow?

- **Real-time vs batch ranking:** Computing a full ML-based ranking score for thousands of candidate posts per user in 200 ms is extremely hard. How do you split the ranking into:
  - **Candidate generation** (fast, cheap: which 1000 posts are candidates for this user?)
  - **Scoring** (heavyweight ML model, applied to top-K candidates)
  - **Re-ranking** (apply diversity rules, filters, ads)

- **Feed pagination with ranking:** Unlike a chronological feed (just use timestamps), a ranked feed changes as new posts arrive and engagement shifts. How do you implement stable pagination so users don't see duplicates or miss posts on scroll?

- **Engagement feedback loop:** A post that gets lots of likes early gets shown to more people, which generates more likes. How do you prevent this from creating a "rich get richer" echo chamber?

- **Storage for engagement signals:** 5B engagement events/day = 58,000/sec. You need to store these and use them in ranking within minutes. What pipeline handles this?

---

## Clarifying Questions (practice asking these in an interview)

1. Is this a social network feed (friends/follows), or a content aggregator (topics/sources)?
2. Is the ranking purely algorithmic, or can users choose "chronological mode"?
3. How should ads be inserted into the feed?
4. Should we support real-time live content (stories, live videos) in the feed?
5. Do we need to support groups or communities with their own feeds?
6. How quickly must engagement signals (likes) affect ranking — seconds or hours?

---

## Concepts Tested

`Fan-out on write vs read` · `Feed ranking pipeline (candidate gen → scoring → re-ranking)` · `Cursor-based pagination` · `Event streaming (Kafka)` · `ML feature store` · `Redis sorted sets (feed cache)` · `Engagement feedback loop` · `Celebrity / hot account problem`
