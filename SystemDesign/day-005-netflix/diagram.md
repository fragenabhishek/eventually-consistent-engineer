# Day 005 — Diagrams: Streaming Platform (Netflix)

---

## 1) High-Level Architecture

```mermaid
flowchart LR
  subgraph Clients
    A["TV / Web / Mobile App"]
  end

  subgraph Edge
    G["API Gateway / Edge"]
  end

  subgraph ControlPlane["Control Plane"]
    AUTH["Auth Service"]
    GEO["Geo and Entitlement Service"]
    SESS["Session Service - concurrent streams"]
    PROF["Profile Service"]
  end

  subgraph MetadataPlane["Catalog and Metadata"]
    CAT["Catalog Service"]
    META["Title Metadata Service"]
    IMG["Artwork and Image Service"]
    SRCH["Search Index"]
  end

  subgraph DeliveryPlane["Playback and DRM"]
    PLAY["Playback Service"]
    MAN["Manifest Service - HLS and DASH"]
    TOK["Token Service - signed URLs"]
    DRM["DRM License Service"]
    MAP["Edge Mapping Service - GeoDNS and telemetry"]
  end

  subgraph CDN["CDN - Open Connect like"]
    EDGE1["ISP Edge Cache Cluster"]
    MID["Regional Mid-tier Shield"]
  end

  subgraph Origin["Origin"]
    OBJ["Object Storage - segments manifests tracks"]
  end

  subgraph DataPlane["Data and ML"]
    EVT["Watch Event Ingestion"]
    OLAP["Analytics Store"]
    REC["Recommendations"]
  end

  A -->|HTTPS| G
  G --> AUTH
  G --> CAT
  G --> PLAY

  CAT --> META
  CAT --> SRCH
  META --> IMG

  PLAY --> GEO
  PLAY --> SESS
  PLAY --> MAP
  PLAY --> MAN
  MAN --> TOK
  TOK --> EDGE1

  A -->|manifest and segments| EDGE1
  EDGE1 -->|cache miss| MID --> OBJ

  A -->|DRM license| DRM

  A -->|watch events| EVT
  EVT --> OLAP
  OLAP --> REC
  REC --> G
```

---

## 2) Playback Start Flow (under 2 seconds)

```mermaid
sequenceDiagram
  autonumber
  participant C as Client Player
  participant GW as API Gateway
  participant P as Playback Service
  participant S as Session Service
  participant E as Edge Mapping
  participant M as Manifest Service
  participant CDN as ISP Edge CDN
  participant D as DRM License

  C->>GW: GET /stream/{titleId}?quality=auto
  GW->>P: authorize request
  P->>S: validate or create stream session
  S-->>P: allowed + sessionId
  P->>E: select best edge cluster
  E-->>P: edge hostname list
  P->>M: build master manifest
  M-->>P: signed manifest URL
  P-->>C: manifest URL + sessionId

  C->>CDN: GET master manifest
  CDN-->>C: 200 cached
  C->>D: POST license request
  D-->>C: license keys
  C->>CDN: GET first segments low bitrate
  CDN-->>C: 200 segments
  Note over C: Playback starts
```

---

## 3) Content Pre-positioning (Nightly Push)

```mermaid
flowchart TB
  subgraph Signals
    H["Historical Views"]
    N["New Releases"]
    L["Locale Signals - language and holidays"]
    T["Trending Signals"]
  end

  F["Demand Forecast Model"]
  P["Placement Planner"]
  C["Capacity Constraints - disk and egress"]
  K["Select Top Titles and Variants"]
  D["Distribution Scheduler"]
  X["Copy Segments to ISP Edges"]

  subgraph Edges
    E1["Edge Cluster A"]
    E2["Edge Cluster B"]
    E3["Edge Cluster C"]
  end

  Signals --> F
  F --> P
  C --> P
  P --> K
  K --> D
  D --> X
  X --> E1
  X --> E2
  X --> E3
```

---

## 4) Failover Mid-Stream (Edge Down)

```mermaid
sequenceDiagram
  autonumber
  participant C as Client Player
  participant CDN1 as Edge Cluster 1
  participant CDN2 as Edge Cluster 2
  participant MID as Mid-tier Shield

  C->>CDN1: GET segment 120 current bitrate
  CDN1--x C: timeout or 5xx

  C->>CDN2: retry segment 120 alternate edge
  alt cache hit
    CDN2-->>C: 200 segment 120
  else cache miss
    CDN2->>MID: fetch segment 120
    MID-->>CDN2: 200 segment 120
    CDN2-->>C: 200 segment 120
  end

  Note over C: ABR may downshift to maintain buffer
```
