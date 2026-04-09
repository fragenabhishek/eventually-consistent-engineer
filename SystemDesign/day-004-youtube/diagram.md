# diagram.md — ASCII Architecture & Data Flow Diagrams

## 1) High-Level Architecture

```
             +--------------------------+
             |        Clients           |
             | Web / Mobile / TV Player |
             +------------+-------------+
                          |
                          | HTTPS
                          v
                  +-------+--------+
                  | API Gateway /  |
                  | Edge + WAF     |
                  +---+--------+---+
                      |        |
          upload/meta |        | playback/search/feed
                      v        v
              +-------+--+   +--+-------------------+
              | Upload Svc |   | Video Metadata Svc  |
              +-------+--+   +--+-------------------+
                      |               |
          signed URLs  |               | (strong)
                      v               v
              +-------+--------------------+
              |   Object Storage (Origin)  |
              | raw + segments + manifests |
              +-------+--------------------+
                      |
                      | events
                      v
              +-------+-----------------+
              | Event Bus (Kafka/PubSub)|
              +---+-----------+---------+
                  |           |
                  |           |
                  v           v
      +-----------+---+   +---+----------------+
      | Transcode Orchestr.|  | Search Indexer |
      +-----------+---+   +---+----------------+
                  |
          jobs     v
      +-----------+-------------------+
      | Transcoding Worker Fleet      |
      | (CPU/GPU autoscaled)          |
      +-----------+-------------------+
                  |
                  | renditions + segments
                  v
       +----------+--------------------+
       | Packaging Service (HLS/DASH)  |
       +----------+--------------------+
                  |
                  | publish manifests
                  v
             +----+-------------------+
             | CDN (global edge cache)|
             +----+-------------------+
                  |
                  | manifests/segments
                  v
               +--+----------------+
               | Video Player      |
               +-------------------+

Engagement + Analytics (side plane)
  - Likes/Comments/Subscriptions Services
  - Counters Service (near real-time)
  - Analytics OLAP store
  - Feed/Recommendation Service
```

---

## 2) Upload → Transcode → Publish Flow

```
Client
  |
  | 1) POST /videos/upload
  v
Upload Service -> Metadata DB (create video: status=UPLOADING)
  |
  | 2) return uploadId + signed part URLs
  v
Client ---- PUT parts ----> Object Storage (multipart)
  |
  | 3) POST /uploads/{id}/complete
  v
Upload Service -> Metadata DB (status=PROCESSING)
  |
  | 4) publish VIDEO_UPLOADED
  v
Event Bus -> Orchestrator -> enqueue jobs (360..4K, thumbs)
  |
  v
Worker Fleet -> encode -> store segments -> Packaging -> manifests
  |
  v
Metadata DB (status=READY, manifestKey)
  |
  v
Event Bus -> Search Indexer + Feed signals
```

---

## 3) Playback (ABR) Flow

```
Player
  |
  | 1) GET /videos/{videoId}/stream
  v
Metadata/Playback Service -> returns signed CDN URL to master manifest
  |
  v
CDN Edge -> serves master.m3u8 / manifest.mpd
  |
  | 2) Player requests segments (small, sequential)
  v
CDN Edge (cache hit) ----> Player
  |
  | cache miss
  v
Origin Shield -> Origin Storage -> CDN -> Player

Player adapts bitrate by requesting next segment from another rendition.
```

---

## 4) View Counts + Analytics Flow

```
Player -> POST /watchEvents (start/progress/end)
  |
  v
Ingestion Service -> Event Bus
  |
  +--> Stream Processor -> Sharded Counters (Redis/Key-Value)
  |         |
  |         +--> periodic aggregate -> Durable Counters Store
  |
  +--> Analytics Pipeline -> OLAP (watch time, drop-off)

Feed/Reco reads: counters + watch history + embeddings -> ranked feed
```

