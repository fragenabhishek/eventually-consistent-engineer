# Day 004 — Design: Video Streaming (YouTube)

## 1) High-Level Architecture
The system is split into planes:

1. **Control/Metadata plane** (strong consistency): video state, ownership, publish status, channel relationships.
2. **Media plane** (throughput/durability): raw uploads, transcoded renditions, ABR manifests, thumbnails.
3. **Engagement plane** (eventual): views, likes, comments, subscriptions, recommendations.

### Core Components
- **API Gateway / Edge**
  - Auth (OAuth/JWT), WAF, rate limiting, request routing.
- **Upload Service**
  - Creates upload sessions, issues pre-signed chunk URLs, tracks progress.
- **Video Metadata Service**
  - Video CRUD, publish state, entitlement/visibility, tags.
- **Transcoding Orchestrator**
  - Consumes upload-complete events, enqueues jobs per rendition.
- **Transcoding Worker Fleet**
  - Autoscaled compute (CPU/GPU) for encode; outputs segments.
- **Packaging Service**
  - Generates **HLS/DASH** manifests, encryption keys (optional), rendition ladders.
- **Origin Storage**
  - Object storage for raw + processed segments + manifests + thumbnails.
- **CDN**
  - Global edge caching for segments/manifests/thumbnails; origin shield.
- **Search Service**
  - Indexes title/description/tags (Elastic/OpenSearch or similar).
- **Engagement Services**
  - Likes, comments, subscriptions.
- **Counters Service**
  - Near real-time view counts, likes counts (sharded counters).
- **Feed/Recommendation Service**
  - Candidate generation + ranking; online serving.
- **Analytics Pipeline**
  - Watch events ingestion, sessionization, retention/drop-off computation.
- **Event Bus**
  - Kafka/PubSub for upload state, encode complete, watch events, indexing.

---

## 2) Storage Choices

### 2.1 Metadata DB (Strong)
- NewSQL or sharded relational DB.
- Strong consistency for:
  - upload status transitions (UPLOADING → PROCESSING → READY/FAILED)
  - publish/unpublish
  - ownership, visibility

### 2.2 Media Storage (Durable)
- Object storage (multi-AZ), lifecycle policies.
- Layout:
  - `raw/{videoId}/source` (original)
  - `renditions/{videoId}/{resolution}/seg_{n}.m4s` (or `.ts`)
  - `manifests/{videoId}/master.m3u8` / `manifest.mpd`
  - `thumbs/{videoId}/{size}.jpg`

### 2.3 Caches
- CDN edge cache (primary for reads).
- Redis for hot metadata, auth tokens, rate-limit counters.

---

## 3) Data Model (Logical)

### Video
- `video_id (PK)`
- `owner_user_id`
- `channel_id`
- `title`, `description`
- `tags[]`
- `visibility` (private|unlisted|public)
- `status` (uploading|processing|ready|failed|deleted)
- `duration_ms`
- `created_at`, `published_at`
- `source_object_key`
- `master_manifest_key` (HLS/DASH)

### Rendition
- `video_id`
- `resolution` (360p..4K)
- `codec` (H264/H265/AV1)
- `bitrate_kbps`
- `segment_count`
- `segment_prefix_key`
- `ready_at`

### Channel
- `channel_id (PK)`
- `owner_user_id`
- `name`, `about`

### Subscription
- `subscriber_user_id`
- `channel_id`
- `created_at`

### Comment
- `comment_id (PK)`
- `video_id`
- `user_id`
- `text`
- `parent_comment_id` (nullable)
- `created_at`
- `like_count` (denormalized; eventual)

### Like
- `video_id`
- `user_id`
- `created_at`

### Counters (materialized)
- `video_id`
- `view_count`
- `like_count`
- `comment_count`
- `updated_at`

### WatchEvent (stream)
- `user_id (optional)`, `device_id`
- `video_id`
- `session_id`
- `event_type` (start|progress|pause|end|buffer)
- `position_ms`
- `bitrate`, `resolution`
- `ts`

---

## 4) API Design (Extended)

### 4.1 Upload (Resumable)
**Initiate**
- `POST /videos/upload`
  - Request: `{ title, description, tags, sizeBytes, contentType }`
  - Response: `{ videoId, uploadId, chunkSize, chunkUrls|uploadUrl }`

**Upload chunks** (direct to object storage)
- `POST /uploads/{uploadId}/parts:sign` → signed URLs for parts
- Client `PUT` each chunk to signed URL

**Complete**
- `POST /uploads/{uploadId}/complete`
  - Request: `{ parts: [{partNumber, etag}], idempotencyKey }`
  - Effects: marks source as uploaded; emits `VIDEO_UPLOADED` event.

**Resume**
- `GET /uploads/{uploadId}` → missing parts

### 4.2 Playback
- `GET /videos/{videoId}` → metadata + status + thumbnail URLs
- `GET /videos/{videoId}/stream?format=hls|dash`
  - Returns signed CDN URL to master manifest.

### 4.3 Search
- `GET /search?q=...&filters=...&page=...`

### 4.4 Engagement
- `POST /videos/{videoId}/like` (idempotent)
- `POST /videos/{videoId}/comment` `{ text, parentCommentId? }`
- `POST /channels/{channelId}/subscribe`

### 4.5 Feed
- `GET /feed` (personalized ranking)

### 4.6 Analytics
- `GET /videos/{videoId}/analytics?range=...`
  - Returns: `{ views, watchTime, avgViewDuration, dropOffCurve[] }`

---

## 5) Key Workflows

### A) Upload → Transcode Pipeline (Async)
1) Client initiates upload → receives `uploadId` + chunk signing endpoint.
2) Client uploads chunks directly to object storage.
3) Client calls complete. Upload Service:
   - finalizes multipart upload
   - updates video status to **PROCESSING** (strong)
   - emits `VIDEO_UPLOADED(videoId, sourceKey)`
4) Orchestrator enqueues jobs: one per resolution (plus thumbnails).
5) Worker encodes segments, uploads to storage.
6) Packaging service produces HLS/DASH manifests.
7) Metadata updated to **READY**; emits `VIDEO_READY`.

**Why async?** Transcoding is expensive; keep upload API fast and reliable.

---

### B) Adaptive Bitrate Streaming (ABR)
- Encode multiple renditions with different bitrates/resolutions.
- Package into:
  - **HLS**: master playlist references variant playlists.
  - **DASH**: MPD manifest references representations.
- Player requests small **segments** (2–6 seconds). It switches renditions by requesting next segment from another representation.

**Low startup latency**
- Keep first segments very small (e.g., 2s) and ensure CDN caches them.

---

### C) CDN Strategy
- Viewer requests manifest/segments from nearest CDN edge.
- Cache keys include `videoId/resolution/segmentNumber`.
- **Origin shield** tier reduces origin load.
- Use signed URLs / token auth:
  - short TTL for public; enforce geo/age restrictions if needed.

Hot vs Cold
- Hot videos stay cached at edges; cold videos served from regional cache or origin.
- Lifecycle: cold renditions moved to cheaper storage tiers; keep at least one mid-quality for fast start.

---

### D) View Counting (Near Real-Time)
1) Player sends watch events (start/progress) to ingestion endpoint.
2) Stream processor (Flink/Spark/Kafka Streams) filters for valid views (anti-fraud heuristics).
3) Update **sharded counters**:
   - per-video shards (e.g., 100–1000 shards) in Redis/Key-Value store.
4) Periodically aggregate shards into durable store (Cassandra/Bigtable) + materialized view.

Trade-off:
- Near real-time counts are approximate; eventual correction via batch recomputation.

---

### E) Search at Scale
- Index document per video: `{title, description, tags, channel, publishedAt, popularity}`.
- Async index updates on publish/edit via event bus.
- Query service handles relevance + filters; caches popular queries.

---

### F) Recommendations (Feed)
Separate offline training from online serving:
- Offline: build embeddings, candidate sets (subscriptions, similar videos), trend signals.
- Online: rank candidates using features (user history, watch time, freshness).
- Keep online p99 low with precomputed candidates + real-time re-ranking.

---

### G) Analytics
- Ingest watch events → sessionize → compute:
  - watch time per video
  - retention curve / drop-off
  - device/network breakdown
- Store in OLAP (Druid/ClickHouse/BigQuery) for queries.

---

## 6) Scalability & Reliability

### Partitioning
- Metadata sharded by `video_id` and/or `owner_user_id`.
- Counters sharded by `video_id`.
- Comments partitioned by `video_id`.

### Multi-Region
- Media served globally via CDN; origin in multiple regions.
- Metadata: regional write with replication; or “home region per channel”.
- Use active-active for read-heavy services (search, feed, counters).

### Backpressure
- Queue depth controls transcoding rate.
- Admission control for uploads during spikes (rate limit by user/region).

### SLOs
- Playback start: prioritize caching first segments, prefetch manifests, and keep auth checks lightweight.

---

## 7) Security & Abuse
- Content scanning hooks (malware, policy).
- Signed URLs + token auth.
- Rate limiting & upload quota.
- Anti-fraud for view count inflation (device fingerprinting, anomaly detection).

