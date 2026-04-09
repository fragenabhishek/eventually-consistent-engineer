# Video Streaming Platform (YouTube) — System Design

## Problem Statement
Design a massively scalable video hosting and streaming platform where users can **upload** videos, the system **processes/transcodes** them into multiple renditions, and viewers can **stream globally** with minimal buffering. Think YouTube/Vimeo.

---

## Functional Requirements
1. **Upload videos** up to **10 GB** reliably (resumable).
2. **Transcode** each upload into: **360p, 480p, 720p, 1080p, 4K**.
3. **Stream** any video with:
   - **Playback start < 3 seconds** globally.
   - **Adaptive bitrate streaming** (ABR) that adapts to network speed.
4. **Search** videos by **title, description, tags**.
5. Social features:
   - **Like**, **comment**, **subscribe** to channels.
6. **View counts** shown **near real-time**.
7. **Recommendations feed** on home page.
8. **Creator analytics**: views, watch time, drop-off rate per video.

### Out of Scope (Clarify in interview)
- Live streaming (unless required).
- DRM / paid content protection (unless required).
- Full content moderation details (copyright, policy enforcement) beyond basic hooks.

---

## Non-Functional Requirements
| Property | Target |
|---|---|
| Availability | **99.99%** |
| Playback start latency | **< 3 seconds globally** |
| Upload throughput | Multi‑GB uploads reliably (resumable) |
| Scale | **2B users**; **500 hours/min** uploaded |
| Read:Write ratio | **~10,000:1** |
| Consistency | **Eventual** (views/likes); **Strong** (upload status, video publish state) |

---

## Capacity Estimation (Given)
- **Uploads/day**: ~720,000 videos
- **Avg raw size**: 500 MB
- **Storage/day** (raw + 5 resolutions): ~2.2 PB
- **Streams/day**: 5 billion
- **Peak bandwidth**: ~500 Gbps

### Derived / Sanity Checks
- **Uploads/sec avg**: 720k / 86,400 ≈ **8.3 uploads/sec** (peak much higher).
- **Streams/sec avg**: 5B / 86,400 ≈ **57,870 streams/sec** (peak could be 5–10×).
- **Daily egress** rough: 500 Gbps peak suggests CDN is mandatory; origin must be protected via caching + tiered storage.

---

## Core APIs (from prompt)
- `POST /videos/upload` → initiate upload (return `uploadUrl` + `videoId`)
- `GET  /videos/{videoId}` → metadata + stream URLs
- `GET  /videos/{videoId}/stream` → ABR manifest (HLS/DASH)
- `GET  /search?q=...` → search videos
- `POST /videos/{videoId}/like` → like
- `POST /videos/{videoId}/comment` → comment
- `GET  /feed` → personalized home feed
- `GET  /videos/{videoId}/analytics` → creator analytics

> Design extends upload with session/chunk endpoints and adds channel/subscribe endpoints.

---

## Key Interview Themes
- Async, scalable **transcoding pipeline** (queue + worker fleet).
- **ABR packaging** (HLS/DASH) & segment storage.
- Global **CDN strategy** (edge caching, signed URLs, origin shielding).
- Near real-time counters (views/likes) with correctness trade-offs.
- Search indexing & recommendation system separation.
