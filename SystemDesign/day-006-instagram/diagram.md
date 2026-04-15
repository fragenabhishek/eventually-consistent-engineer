# Day 006 — Diagrams: Photo Feed & Social Network (Instagram)

## 1) High-Level Architecture

```mermaid
flowchart LR
  subgraph Clients
    APP["Mobile / Web App"]
  end

  subgraph EdgeLayer["Edge"]
    GW["API Gateway - Auth and Rate Limiting"]
  end

  subgraph InteractionPlane["Interaction Plane"]
    PS["Post Service"]
    FG["Follow Graph Service"]
    LS["Like Service"]
    CS["Comment Service"]
    SS["Story Service"]
    PROF["Profile Service"]
    SRCH["Search Service - Elasticsearch"]
  end

  subgraph MediaPlane["Media Plane"]
    MS["Media Service - pre-signed URLs"]
    S3["Object Storage - S3"]
    CDN["CDN - CloudFront"]
    MW["Media Worker - resize and thumbnail"]
  end

  subgraph FeedPlane["Feed Plane"]
    FS["Feed Service"]
    REDIS["Redis - feed sorted sets"]
    CASS_FEED["Cassandra - durable feed store"]
  end

  subgraph EventBus["Event Bus - Kafka"]
    K1["post-events"]
    K2["engagement-events"]
    K3["fanout-tasks"]
  end

  subgraph AsyncWorkers["Async Workers"]
    FW["Feed Fanout Worker"]
    NW["Notification Aggregator"]
    SI["Search Indexer"]
  end

  subgraph NotifPlane["Notification Plane"]
    NS["Notification Service"]
    PUSH["FCM / APNs"]
  end

  APP -->|HTTPS| GW
  GW --> PS
  GW --> FG
  GW --> FS
  GW --> SS
  GW --> SRCH
  GW --> PROF

  PS --> MS
  MS --> S3
  S3 -->|media| CDN
  CDN -->|images and video| APP

  PS -->|POST_CREATED| K1
  K1 --> MW
  MW --> S3
  MW -->|POST_READY| K3

  K3 --> FW
  FW --> FG
  FW --> REDIS
  FW --> CASS_FEED

  FS --> REDIS
  FS --> CASS_FEED
  FS --> PS

  LS -->|LIKE_EVENT| K2
  CS -->|COMMENT_EVENT| K2
  FG -->|FOLLOW_EVENT| K2

  K2 --> NW
  K2 --> SI
  NW --> NS
  NS --> PUSH
  SI --> SRCH
```

---

## 2) Post Upload Flow

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant GW as API Gateway
  participant PS as Post Service
  participant MS as Media Service
  participant S3 as Object Storage
  participant K as Kafka
  participant MW as Media Worker
  participant FW as Fanout Worker
  participant REDIS as Redis Feed Cache

  C->>GW: POST /posts caption and mediaCount
  GW->>PS: create post PENDING
  PS->>MS: request pre-signed upload URLs
  MS-->>PS: signed URLs for each file
  PS-->>C: postId and uploadUrls

  C->>S3: PUT files directly to signed URLs

  C->>GW: POST /posts/{postId}/finalize
  GW->>PS: mark PROCESSING
  PS->>K: publish POST_CREATED

  K->>MW: consume POST_CREATED
  MW->>S3: read original files
  MW->>S3: write resized variants 1080p 720p thumb
  MW->>K: publish POST_READY

  K->>FW: consume POST_READY
  FW->>FW: fetch follower list in pages
  FW->>REDIS: push postId to each follower feed sorted set
  Note over FW: Celebrities skipped - pull on read instead

  PS->>PS: mark PUBLISHED
```

---

## 3) Feed Read Flow (Hybrid Fan-out)

```mermaid
flowchart TB
  A["GET /feed - client request"]
  B["Feed Service"]
  C{"User's feed in Redis?"}
  D["Read feed sorted set from Redis\ncovers non-celebrity posts"]
  E["Cold-start pull from Cassandra\nrepopulate Redis"]
  F["Get celebrity accounts user follows\nfrom Follow Graph Service cache"]
  G["Fetch recent posts from each celebrity\nPost Service DB"]
  H["Merge and rank\nchronological or ML score"]
  I["Batch enrich\nlike count comment count likedByMe"]
  J["Return top 20 posts to client"]

  A --> B
  B --> C
  C -- yes --> D
  C -- no --> E
  E --> D
  D --> H
  B --> F
  F --> G
  G --> H
  H --> I
  I --> J
```

---

## 4) Notification Aggregation Flow

```mermaid
sequenceDiagram
  autonumber
  participant U1 as User A likes post
  participant U2 as User B likes post
  participant K as Kafka engagement-events
  participant NA as Notification Aggregator
  participant NS as Notification Service
  participant PUSH as FCM / APNs
  participant CR as Creator

  U1->>K: LIKE event postId userId
  U2->>K: LIKE event postId userId
  Note over K: 49998 more LIKE events in 30s window

  K->>NA: consume window of events
  NA->>NA: aggregate by recipient and event type
  NA->>NS: send one notification\n"UserA and 49999 others liked your photo"

  NS->>PUSH: single push notification
  PUSH->>CR: one notification delivered
```

---

## 5) Story Expiry Strategy

```mermaid
flowchart LR
  subgraph Write
    A["POST /stories"]
    B["Story Service"]
    C["Cassandra - TTL 86400s"]
    D["Redis - EXPIREAT now plus 86400s"]
    E["S3 - stories prefix"]
  end

  subgraph Read
    F["GET /stories/feed"]
    G["Read from Redis\n only non-expired keys returned"]
    H["Cassandra tombstones expired rows automatically"]
  end

  subgraph Cleanup
    I["S3 lifecycle rule\ndelete stories older than 48h"]
  end

  A --> B
  B --> C
  B --> D
  B --> E

  F --> G
  G --> H

  E --> I
```
