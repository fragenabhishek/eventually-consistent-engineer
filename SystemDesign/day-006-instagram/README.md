# Day 006 — Photo Feed & Social Network (Instagram)

> **Interview Goal:** Design a social photo-sharing platform with a real-time feed, followers, and media storage.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Instagram has 2 billion active users who share photos and short videos, follow each other, and scroll a personalized feed. The core challenge is: how do you efficiently generate and serve a personalized feed for 2 billion people — especially when someone like Cristiano Ronaldo (600M followers) posts a photo?

---

## Functional Requirements

1. Users can **upload photos and short videos** (up to 60 seconds).
2. Users can **follow / unfollow** other users.
3. Users see a **personalized feed** of posts from people they follow (newest first, with ranking).
4. Users can **like** and **comment** on posts.
5. Users can **search** for people and hashtags.
6. Users can view their own **profile** (bio, post count, follower/following count, posts grid).
7. Support **stories** — photos/videos that expire after 24 hours.
8. Support **direct messages** between users.
9. Push **notifications** for likes, comments, follows, and messages.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Feed load latency | < 200 ms (p99) |
| Photo upload latency | < 2 seconds for upload confirmation |
| Scale | 2B users; 500M daily active; 100M posts/day |
| Read:Write ratio | ~1000:1 (feed reads vastly outnumber posts) |
| Consistency | Eventual (feed, likes); Strong (follow graph) |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Users | 2B total, 500M DAU |
| Posts/day | 100M photos + 10M videos |
| Avg photo size (compressed) | 200 KB |
| Storage/day | ~20 TB |
| Feed reads/day | 5B (each DAU loads feed ~10 times) |
| Likes/day | 4B |

---

## Core API to Design

```
POST   /posts                      → upload a photo/video
DELETE /posts/{postId}             → delete a post
POST   /posts/{postId}/like        → like/unlike
POST   /posts/{postId}/comment     → add comment
POST   /users/{userId}/follow      → follow a user
GET    /feed                       → load personalized feed (paginated)
GET    /users/{userId}/profile     → get user profile + posts
GET    /search?q={query}           → search users / hashtags
POST   /stories                    → upload a story (24h TTL)
GET    /stories/{userId}           → view stories of a user
```

---

## Key Challenges to Think About

- **Feed generation — Push vs Pull:**
  - **Pull (fan-in):** At feed load time, fetch posts from all followed users. Simple, but slow for users who follow 5,000 people.
  - **Push (fan-out):** When a user posts, push to all followers' feed queues. Fast reads, but Cristiano's 600M followers creates a massive write storm.
  - **Hybrid:** Push for normal users, pull for celebrity posts. How do you decide the threshold?
- **The celebrity problem (hot key):** One Ronaldo post generates 600M write operations in seconds. How do you handle this?
- **Image storage & CDN:** Where do you store photos? How do you serve them globally without latency?
- **Photo processing pipeline:** Resizing, thumbnail generation, and filter application should be async. What message queue pattern fits?
- **Notification fan-out:** A viral post gets 1M likes in 10 minutes. How do you avoid notification spam to the creator?
- **Story TTL:** Stories expire after 24 hours. How do you efficiently expire millions of stories without a background job scanning every row?
- **Search:** Hashtag search needs to be fast and ranked. How do you index 100M posts/day?

---

## Clarifying Questions (practice asking these in an interview)

1. Should the feed be purely chronological or ranked (algorithmic)?
2. Do we need to support public vs private accounts?
3. Is the DM feature full real-time chat, or asynchronous messaging?
4. Do we need to detect and filter inappropriate content (NSFW)?
5. What is the maximum resolution for uploaded photos/videos?
6. Should stories be viewable without an account (public stories)?

---

## Concepts Tested

`Fan-out on write vs fan-in on read` · `Celebrity / hot-key problem` · `CDN for media` · `Object storage` · `Event-driven architecture` · `Notification service` · `Redis for feed caching` · `Consistent Hashing`
