# Day 004 — Video Streaming Platform (YouTube)

> **Interview Goal:** Design a platform where users upload, process, and stream videos at massive scale.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

You're designing the backend for a video hosting and streaming platform. Users upload videos from any device; the platform processes them into multiple resolutions and makes them available globally with minimal buffering. Think YouTube, Vimeo, or Dailymotion.

---

## Functional Requirements

1. Users can **upload** videos (up to 10 GB).
2. The system **transcodes** uploaded videos into multiple resolutions: 360p, 480p, 720p, 1080p, 4K.
3. Users can **stream** any video — playback starts within 3 seconds, adapts to network speed.
4. Users can **search** videos by title, description, and tags.
5. Users can **like**, **comment**, and **subscribe** to channels.
6. The system tracks **view counts** and shows them in near real-time.
7. Videos have a **recommendation feed** on the home page.
8. Creators can see **analytics**: views, watch time, drop-off rate per video.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Latency (playback start) | < 3 seconds globally |
| Upload throughput | Support multi-GB uploads reliably |
| Scale | 2 billion users; 500 hours of video uploaded per minute |
| Read:Write ratio | ~10,000:1 (far more streams than uploads) |
| Consistency | Eventual (view counts, likes); Strong (upload status) |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Videos uploaded/day | 720,000 (500 hrs/min × 60 × 24 / avg 5-min video ≈ 144,000 hrs → ~720K videos) |
| Avg raw video size | 500 MB |
| Storage/day (raw + 5 resolutions) | ~2.2 PB |
| Streams/day | 5 billion |
| Bandwidth for streaming | ~500 Gbps peak |

---

## Core API to Design

```
POST   /videos/upload              → initiate upload (return uploadUrl + videoId)
GET    /videos/{videoId}           → get video metadata + stream URLs
GET    /videos/{videoId}/stream    → return adaptive bitrate manifest (HLS/DASH)
GET    /search?q={query}           → search videos
POST   /videos/{videoId}/like      → like a video
POST   /videos/{videoId}/comment   → add a comment
GET    /feed                       → personalized home feed
GET    /videos/{videoId}/analytics → creator analytics
```

---

## Key Challenges to Think About

- **Transcoding pipeline:** Video transcoding is CPU-intensive. How do you scale it without blocking the upload API?
- **Adaptive bitrate streaming (ABR):** How does the player switch between 360p and 1080p seamlessly as network changes?
- **CDN strategy:** With 5 billion streams/day, you can't serve everything from origin. How do you route users to the nearest edge node?
- **View count accuracy vs speed:** View counts are shown in near real-time for trending videos. Exact counters at this scale are expensive — what's the trade-off?
- **Search at scale:** YouTube has 800M videos. How do you build a fast search index?
- **Hot vs cold content:** 90% of views go to 5% of videos. How do you use this to optimize storage and caching?
- **Upload resumability:** A 5 GB upload on a mobile network may fail midway. How do you resume it?

---

## Clarifying Questions (practice asking these in an interview)

1. Do we need live streaming, or only on-demand pre-recorded videos?
2. What is the maximum video duration/size allowed?
3. Should we support DRM (content protection for paid content)?
4. How fresh do view counts need to be (real-time vs 1-hour delay acceptable)?
5. Do we need to support captions/subtitles auto-generation?
6. What geographies need to be covered (global vs regional)?

---

## Concepts Tested

`CDN` · `Adaptive Bitrate Streaming (HLS/DASH)` · `Message Queue for async transcoding` · `Object Storage` · `Search Indexing` · `Sharded counters` · `Microservices` · `Rate limiting uploads`
