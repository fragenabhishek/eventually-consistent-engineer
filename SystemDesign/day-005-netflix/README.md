# Video Streaming + CDN (Netflix) — System Design

## Problem Statement
Design a **subscription-based** video streaming service that delivers a fixed catalog of licensed/original content to **250M subscribers across 190 countries**. The core challenge is **global content delivery**: start playback in **< 2 seconds**, keep buffering **< 0.1%**, and support **peak 100M concurrent streams** using a CDN architecture (Netflix-like *Open Connect*).

---

## Functional Requirements
1. Browse a **content catalog** (movies/TV/shows) with pagination.
2. **Stream** any title using **adaptive bitrate streaming** (ABR) (HLS/DASH).
3. **Offline downloads** on mobile (DRM protected).
4. Remember **watch progress** (resume where left off).
5. Personalized **recommendations** on home page.
6. **Multiple profiles** per account (kids/adult), separate watch history.
7. **Subtitles** and **audio tracks** in multiple languages.
8. Enforce **concurrent stream limits** by tier:
   - Basic = 1
   - Standard = 2
   - Premium = 4

### Clarifications (typical interview)
- On-demand only (no live sports) unless required.
- Geo-restrictions apply (title availability differs by country).
- DRM required for licensed content.

---

## Non-Functional Requirements
| Property | Target |
|---|---|
| Availability | 99.99% |
| Playback start latency | < 2 seconds |
| Buffering rate | < 0.1% of streaming time |
| Scale | 250M subs; peak 100M concurrent streams |
| Catalog size | ~15,000 titles; 10+ variants each |
| Bandwidth | ~15% of global traffic at peak |

Consistency expectations:
- **Strong**: subscription tier, session validation, playback authorization.
- **Eventual**: recommendations, analytics counters.

---

## Capacity Estimation (Given)
- Subscribers: 250M
- Peak concurrent streams: 100M
- Avg bitrate per stream: 5 Mbps
- Peak bandwidth required: 500 Tbps
- Avg movie size (4K): ~20 GB
- Total content storage: 15,000 titles × 10 variants × 20 GB ≈ 3 PB

### Back-of-envelope sanity checks
- **Peak egress**: 100M × 5 Mbps = 500,000,000 Mbps = **500 Tbps** ✔
- **Origin cannot serve peak**: must use **ISP-embedded CDN edges**, aggressive caching, and pre-positioning.

---

## Core APIs (from prompt)
- `GET  /catalog` → browse titles (paginated)
- `GET  /catalog/{titleId}` → title metadata, available streams
- `GET  /stream/{titleId}?quality=auto` → ABR manifest (HLS/DASH)
- `POST /watchHistory/{titleId}` → save watch progress
- `GET  /watchHistory` → resume list
- `GET  /recommendations` → personalized feed
- `GET  /downloads/{titleId}` → offline download URL (mobile)
- `POST /sessions/validate` → enforce concurrent streams

---

## What Interviewers Look For
- CDN routing strategy (DNS/Anycast, consistent hashing, proximity/health).
- Pre-positioning strategy and how it adapts to regional demand.
- ABR streaming details (segments, manifests, player behavior).
- DRM key/license flow without per-segment latency.
- Session management for concurrent limits that is reliable under failures.
- Failure handling: edge outage mid-stream, origin brownout, token revocation.
