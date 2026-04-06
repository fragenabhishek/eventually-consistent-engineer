# Day 005 — Video Streaming + CDN (Netflix)

> **Interview Goal:** Design a subscription-based video streaming service focused on CDN architecture and content delivery.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Netflix serves 250 million subscribers across 190 countries. Unlike YouTube (user-generated), Netflix hosts a fixed catalog of licensed/original content — but it must stream it reliably to any device, any network, anywhere in the world. The key engineering challenge is delivering massive video files globally with minimal latency and buffering.

---

## Functional Requirements

1. Users can **browse** a content catalog (movies, TV shows, documentaries).
2. Users can **stream** any title — playback adapts to network conditions.
3. Users can **download** titles for offline viewing (mobile only).
4. The system remembers **watch progress** and resumes where the user left off.
5. Provide **personalized recommendations** on the home page.
6. Support **multiple profiles** per account (e.g., kids profile, adult profile).
7. Support **subtitles and audio tracks** in multiple languages.
8. Enforce **concurrent stream limits** per subscription tier (Basic = 1, Standard = 2, Premium = 4).

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Streaming latency | Playback starts < 2 seconds |
| Buffering rate | < 0.1% of streaming time |
| Scale | 250M subscribers; peak 100M concurrent streams |
| Content catalog | ~15,000 titles, each in 10+ resolution/bitrate variants |
| Bandwidth | Netflix accounts for ~15% of global internet traffic at peak |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Subscribers | 250M |
| Peak concurrent streams | 100M |
| Avg bitrate per stream | 5 Mbps |
| Peak bandwidth required | 500 Tbps |
| Avg movie size (4K) | ~20 GB |
| Total content storage | ~15,000 titles × 10 variants × 20 GB ≈ 3 PB |

---

## Core API to Design

```
GET    /catalog                         → browse movies/shows (paginated)
GET    /catalog/{titleId}               → get title metadata, available streams
GET    /stream/{titleId}?quality=auto   → return adaptive stream manifest (HLS/DASH)
POST   /watchHistory/{titleId}          → save watch progress
GET    /watchHistory                    → resume watching list
GET    /recommendations                 → personalized home feed
GET    /downloads/{titleId}             → get offline download URL (mobile)
POST   /sessions/validate               → check concurrent stream count
```

---

## Key Challenges to Think About

- **Open Connect (Netflix's CDN):** Netflix places servers inside ISP data centres. How does a client know which edge server to hit? What's the routing strategy?
- **Content pre-positioning:** Rather than pulling content from origin on demand, Netflix pre-loads popular content to edge servers during off-peak hours. How do you decide *what* to pre-position *where*?
- **Adaptive bitrate:** A user on a 4G train starts streaming 1080p, goes through a tunnel, and the bitrate drops to 360p. How does the player switch seamlessly without rebuffering?
- **DRM (Digital Rights Management):** Licensed content must be protected. How does encryption work without adding latency to every request?
- **Concurrent stream enforcement:** If a user opens Netflix on 5 devices simultaneously with a 2-stream plan, how do you detect and enforce the limit reliably?
- **Recommendation engine:** Netflix claims 80% of streams come from recommendations. What data do you need to build this? What are the latency requirements?
- **Fault tolerance:** What happens if an ISP-level edge server goes down mid-stream?

---

## Clarifying Questions (practice asking these in an interview)

1. Are we designing the entire system, or focusing on the streaming/CDN component?
2. Do we need to handle live sports/events or only on-demand content?
3. Should recommendation be real-time or batch-computed overnight?
4. How do we handle geo-restrictions (a title available in the US but not in India)?
5. What's the SLA for concurrent stream enforcement — eventual or immediate?
6. Do we need to support 4K + HDR streaming from day one?

---

## Concepts Tested

`CDN architecture` · `Edge server placement` · `HLS/DASH adaptive streaming` · `Content pre-positioning` · `DRM` · `Consistent Hashing (CDN routing)` · `Recommendation systems` · `Session management`
