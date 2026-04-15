# Day 005 — Design: Streaming Platform (Netflix)

## 1) High-Level Architecture (Netflix-like)
We separate the system into 4 planes:

1. **Control plane**: account, profiles, entitlements, geo rules, concurrent session enforcement.
2. **Metadata plane**: catalog, title metadata, tracks (audio/subtitles), images.
3. **Delivery plane**: manifest generation, DRM licensing, CDN edge selection and tokenization.
4. **Data/ML plane**: event ingestion, recommendations, analytics.

### 1.1 Key Components

#### Edge & Access
- **API Gateway**: auth, rate-limits, routing to microservices.
- **Auth Service**: login, token issuance (JWT/OAuth), device registration.
- **Geo Service**: resolves user region (IP + account settings) for licensing.

#### Catalog & Metadata
- **Catalog Service**: browse/search; category rails.
- **Title Metadata Service**: per-title details, available variants, track lists.
- **Image Service**: artwork selection per profile/locale.

#### Streaming Delivery
- **Playback Service**:
  - validates entitlement + geo availability
  - checks concurrent streams
  - selects CDN edge and returns signed URLs
- **Manifest Service**:
  - generates **HLS/DASH** master + variant manifests
  - includes track groups (audio/subtitles)
- **DRM License Service**:
  - Widevine/PlayReady/FairPlay licenses
  - issues keys for offline + streaming
- **Token Service**:
  - generates short-lived signed tokens for CDN URLs (prevents hotlinking)

#### CDN (Open Connect-like)
- **Edge Cache Nodes (inside ISPs)**:
  - store segments/manifests/thumbnails
  - report health, capacity, cache metrics
- **Regional Mid-tier / Origin Shield**:
  - absorbs cache misses, reduces origin blast radius
- **Origin Storage**:
  - durable object storage with encoded segments (multi-region)

#### Pre-positioning & Operations
- **Content Placement Service**:
  - predicts demand per region/ISP
  - schedules nightly pushes to edges (off-peak)
- **Distribution Pipeline**:
  - copies segments to edge clusters (rsync-like or object replication)

#### Data/ML
- **Event Ingestion**: watch events, play failures, QoE metrics.
- **Recommendations**: offline training + online ranking.
- **Analytics Store (OLAP)**: per-title/segment QoE, completion, drop-offs.

---

## 2) Storage & Data Model

### 2.1 Strong Control Data (Relational/NewSQL)
- Accounts, profiles, subscriptions, entitlements, concurrency sessions.

### 2.2 Metadata & Catalog (Search + DB)
- Title docs in a search index (OpenSearch/Elastic) for browse/search.
- Metadata DB for authoritative title + track data.

### 2.3 Media Assets
- **Object storage** for:
  - `segments/{titleId}/{encodeProfile}/seg_{n}.m4s`
  - `manifests/{titleId}/{profile}/master.m3u8` or `manifest.mpd`
  - `subtitles/{titleId}/{lang}/{format}` (VTT/TTML)
  - `audio/{titleId}/{lang}/{codec}/seg_{n}.m4s`

### 2.4 Data Models (logical)

#### Account
- `account_id (PK)`
- `email_hash`
- `subscription_tier` (basic|standard|premium)
- `max_concurrent_streams` (1|2|4)
- `country`
- `created_at`, `status`

#### Profile
- `profile_id (PK)`
- `account_id (FK)`
- `name`
- `maturity_level` (kids|teen|adult)
- `language_preferences[]`

#### Title
- `title_id (PK)`
- `type` (movie|episode|series)
- `name`, `description`, `genres[]`
- `availability` (by country/region)
- `artwork_keys[]`
- `default_audio_lang`, `available_audio_langs[]`
- `available_subtitle_langs[]`

#### EncodeVariant
- `title_id`
- `variant_id`
- `resolution` (240p..4K)
- `bitrate_kbps`
- `codec` (H264/H265/AV1)
- `drm_scheme` (cenc|cbcs)
- `segment_duration_s` (2–6)
- `segment_prefix_key`

#### WatchProgress
- `profile_id`
- `title_id`
- `position_ms`
- `updated_at`

#### StreamSession
- `session_id (PK)`
- `account_id`
- `profile_id`
- `device_id`
- `title_id`
- `started_at`, `last_heartbeat_at`
- `state` (active|ended|expired)
- `ip_prefix`, `region`

---

## 3) Core APIs (Interview-friendly)

### 3.1 Catalog
- `GET /catalog?pageToken=...&filters=...`
  - returns rails (Trending, Continue Watching, Genres) + paging

- `GET /catalog/{titleId}`
  - returns metadata + track options + rating/maturity gating

### 3.2 Playback (Manifest)
- `GET /stream/{titleId}?quality=auto&format=hls|dash&profileId=...`
  - Validates entitlement + geo + maturity
  - Enforces concurrent streams
  - Selects best edge
  - Returns **signed** URL to master manifest

### 3.3 Watch History
- `POST /watchHistory/{titleId}`
  - body: `{ profileId, positionMs, durationMs, eventType }`

- `GET /watchHistory?profileId=...`

### 3.4 Recommendations
- `GET /recommendations?profileId=...`

### 3.5 Offline Downloads
- `GET /downloads/{titleId}?profileId=...&quality=...`
  - returns URL + DRM offline license policy (expiry, renewal)

### 3.6 Concurrent Stream Enforcement
- `POST /sessions/validate`
  - body: `{ accountId, profileId, deviceId, titleId }`
  - returns: `{ sessionId, allowed, reason }`

Additional (recommended):
- `POST /sessions/heartbeat` to keep session active
- `POST /sessions/end` on stop

---

## 4) Critical Workflows

### A) Playback Start (<2s) — End-to-end
1. Client calls **/stream/{titleId}**.
2. Playback Service:
   - verifies auth token
   - checks geo availability + maturity
   - calls Session Service to validate concurrency
   - selects CDN edge
   - returns signed URL to manifest
3. Player fetches manifest from edge and immediately requests first 1–2 segments.

**Optimizations:**
- Cache title metadata at edge (or in regional caches).
- Keep manifests small and cacheable (short TTL + versioned paths).
- Use 2s segments for fast startup.

---

### B) CDN Routing Strategy (How client finds an edge)
We use a combination:
- **DNS-based mapping** (GeoDNS) + real-time telemetry.
- **Anycast** for initial edge ingress in some regions.
- **Consistent hashing** to keep a subscriber sticky to a nearby cluster while allowing failover.

Selection inputs:
- client IP prefix/ASN (ISP)
- proximity and RTT
- edge health/load/capacity
- cache hit probability for requested title

Output:
- Edge hostname `edge-xyz.isp.netflixcdn.net` embedded into manifest URLs.

---

### C) Content Pre-positioning (Push vs Pull)
Netflix-like approach favors **push**:
- Nightly/off-peak windows: populate edges with predicted hot titles.
- Daytime: edges can still **pull on miss** from mid-tier/origin.

Placement decision (per region/ISP cluster):
- demand forecast using:
  - historical views
  - new releases
  - social/trend signals
  - locality (language, country, holidays)
- capacity constraints per edge
- choose top-K titles & top renditions (often not all 10 variants everywhere)

---

### D) Adaptive Bitrate Streaming (ABR)
- Encode each title into multiple bitrates/resolutions.
- Package into **HLS/DASH**.
- Player algorithm:
  - starts at a safe bitrate (or uses network estimate)
  - measures throughput & buffer
  - switches rendition for next segment (no re-download of already buffered segments)

Tunnel scenario:
- buffer drains → player downshifts to 360p → continues requesting smaller segments.

---

### E) DRM without per-segment latency
- Segments are encrypted using **CENC/CBCS**.
- Player obtains license via **DRM License Service** once per playback session (not per segment).
- License contains decryption keys for track representations.

Offline:
- device requests an **offline license** bound to device; includes expiry & renewal policy.

---

### F) Concurrent Stream Enforcement
**Goal:** enforce limits reliably with low latency.

Approach:
- Maintain `StreamSession` records in a strongly consistent store (or strongly consistent session coordinator).
- On validate:
  - count active sessions for account
  - if < limit → create session
  - else deny or revoke oldest session depending on product policy

Heartbeat:
- clients send periodic heartbeats; missing heartbeats expire sessions.

Edge cases:
- app crash → session expires after TTL.

---

## 5) Scalability & Reliability

### 5.1 Multi-region
- **Control/metadata**: multi-region active-active for reads; writes routed to home region per account.
- **Media**: origin replicated; edges inside ISPs provide locality.

### 5.2 Hot vs Cold optimization
- 90% of views on 5% titles:
  - keep hot titles replicated widely at edges.
  - cold titles stored primarily at mid-tier/origin; pulled on demand.

### 5.3 SLO-driven caching
- Always ensure **first segments** of popular titles are present at edges.
- Maintain a "startup set" per edge.

### 5.4 Observability
- QoE metrics: startup time, rebuffer ratio, bitrate switches.
- Edge metrics: hit ratio, eviction, disk usage, health.
- Control metrics: session validate p99, deny rate.

---

## 6) Security & Compliance
- Signed URLs (short TTL) + token binding.
- DRM license enforcement.
- Geo restrictions enforced at playback authorization and license issuance.
- Audit logs for account/session anomalies.

