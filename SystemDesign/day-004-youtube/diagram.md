# Day 004 — Diagrams: Video Streaming (YouTube)

## 1) High-Level Architecture

```mermaid
flowchart LR
  subgraph Clients
    C["Web / Mobile / TV Player"]
  end

  subgraph Edge
    G["API Gateway / Edge + WAF"]
  end

  subgraph CoreServices
    U["Upload Service"]
    M["Video Metadata Service"]
    O["Object Storage - raw and renditions"]
    E["Event Bus - Kafka or PubSub"]
    T["Transcode Orchestrator"]
    W["Transcoding Worker Fleet"]
    P["Packaging Service - HLS and DASH"]
    S["Search Indexer"]
  end

  subgraph Delivery
    CDN["CDN - global edge cache"]
  end

  subgraph SidePlane["Engagement and Analytics"]
    L["Likes Comments Subscriptions"]
    K["Counters Service"]
    A["Analytics OLAP Store"]
    R["Feed and Recommendation"]
  end

  C -->|HTTPS| G
  G -->|upload and metadata| U
  G -->|playback search feed| M

  U -->|signed URLs| O
  U -->|status updates| M

  M --> E
  E --> T
  E --> S
  T --> W
  W -->|segments and renditions| O
  W --> P
  P -->|publish manifests| O

  O --> CDN
  CDN --> C

  C --> L
  L --> K
  L --> A
  A --> R
```

---

## 2) Upload → Transcode → Publish Flow

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant U as Upload Service
  participant DB as Metadata DB
  participant OS as Object Storage
  participant B as Event Bus
  participant O as Orchestrator
  participant W as Worker Fleet
  participant P as Packaging Service
  participant S as Search Indexer

  C->>U: POST /videos/upload
  U->>DB: create video status UPLOADING
  U-->>C: uploadId + signed part URLs
  C->>OS: PUT multipart chunks
  C->>U: POST /uploads/{id}/complete
  U->>DB: set status PROCESSING
  U->>B: publish VIDEO_UPLOADED
  B->>O: enqueue encode jobs
  O->>W: run renditions and thumbnails
  W->>OS: store segments
  W->>P: package HLS and DASH manifests
  P->>OS: write manifests
  W->>DB: set status READY + manifestKey
  B->>S: index for search and feed signals
```

---

## 3) Playback (ABR) Flow

```mermaid
sequenceDiagram
  autonumber
  participant P as Player
  participant M as Metadata and Playback Service
  participant C as CDN Edge
  participant SH as Origin Shield
  participant O as Origin Storage

  P->>M: GET /videos/{videoId}/stream
  M-->>P: signed CDN URL to master manifest
  P->>C: GET master.m3u8 or manifest.mpd
  C-->>P: manifest response
  P->>C: GET first media segments

  alt cache hit
    C-->>P: segment bytes
  else cache miss
    C->>SH: fetch segment
    SH->>O: fetch from origin
    O-->>SH: segment bytes
    SH-->>C: segment bytes
    C-->>P: segment bytes
  end

  Note over P: ABR switches rendition for next segment based on throughput and buffer
```

---

## 4) View Counts + Analytics Flow

```mermaid
flowchart TB
  P["Player"] -->|POST watch events| I["Ingestion Service"]
  I --> B["Event Bus"]

  B --> SP["Stream Processor"]
  SP --> SC["Sharded Counters - Redis or KV"]
  SC --> DC["Durable Counters Store"]

  B --> AP["Analytics Pipeline"]
  AP --> OL["OLAP Store - watch time and drop-off"]

  DC --> FR["Feed and Recommendation"]
  OL --> FR
  FR -->|ranked feed signals| P
```

