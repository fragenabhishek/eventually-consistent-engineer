# diagram.md — Mermaid Architecture & Data Flow Diagrams

## 1) High-Level Architecture

```mermaid
flowchart LR
  subgraph Clients
    A[TV / Web / Mobile App]
  end

  subgraph Edge
    G[API Gateway / Edge]
  end

  subgraph ControlPlane[Control Plane]
    AUTH[Auth Service]
    GEO[Geo / Entitlement Service]
    SESS[Session Service<br/>(concurrent streams)]
    PROF[Profile Service]
  end

  subgraph MetadataPlane[Catalog & Metadata]
    CAT[Catalog Service]
    META[Title Metadata Service]
    IMG[Artwork/Image Service]
    SRCH[Search Index]
  end

  subgraph DeliveryPlane[Playback & DRM]
    PLAY[Playback Service]
    MAN[Manifest Service<br/>(HLS/DASH)]
    TOK[Token Service<br/>(signed URLs)]
    DRM[DRM License Service]
    MAP[Edge Mapping Service<br/>(GeoDNS/telemetry)]
  end

  subgraph CDN[CDN / Open Connect-like]
    EDGE1[ISP Edge Cache Cluster]
    MID[Regional Mid-tier / Shield]
  end

  subgraph Origin[Origin]
    OBJ[Object Storage<br/>(segments, manifests, tracks)]
  end

  subgraph DataPlane[Data / ML]
    EVT[Watch Event Ingestion]
    REC[Recommendations]
    OLAP[Analytics (OLAP)]
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
  MAN --> EDGE1

  A -->|manifest + segments| EDGE1
  EDGE1 -->|miss| MID --> OBJ

  A -->|DRM license| DRM

  A -->|watch events| EVT --> OLAP
  OLAP --> REC
  REC --> G
```

---

## 2) Playback Start Flow (<2s)

```mermaid
sequenceDiagram
  autonumber
  participant C as Client Player
  participant GW as API Gateway
  participant P as Playback Service
  participant S as Session Service
  participant M as Manifest Service
  participant E as Edge Mapping
  participant CDN as ISP Edge CDN
  participant DRM as DRM License

  C->>GW: GET /stream/{titleId}?quality=auto
  GW->>P: authorize request
  P->>S: validate/create StreamSession
  S-->>P: allowed + sessionId
  P->>E: select best edge cluster
  E-->>P: edge hostname(s)
  P->>M: build HLS/DASH master manifest
  M-->>P: signed manifest URL
  P-->>C: manifest URL + sessionId

  C->>CDN: GET master manifest
  CDN-->>C: 200 (cached)
  C->>DRM: POST license request
  DRM-->>C: license (keys)
  C->>CDN: GET first segments (low bitrate)
  CDN-->>C: 200 segments
  Note over C: Playback starts
```

---

## 3) Content Pre-positioning (Nightly Push)

```mermaid
flowchart TB
  subgraph Forecast
    H[Historical Views]
    N[New Releases]
    L[Locale Signals<br/>(language/holidays)]
    T[Trending Signals]
    F[Demand Forecast Model]
  end

  subgraph Planner
    P[Placement Planner]
    C[Capacity Constraints<br/>(edge disk/egress)]
    K[Select Top-K Titles + Variants]
  end

  subgraph Distribution
    D[Distribution Scheduler]
    X[Copy Segments to ISP Edges]
  end

  subgraph Edges
    E1[Edge Cluster A]
    E2[Edge Cluster B]
    E3[Edge Cluster C]
  end

  H-->F
  N-->F
  L-->F
  T-->F
  F-->P
  C-->P
  P-->K
  K-->D
  D-->X
  X-->E1
  X-->E2
  X-->E3
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

  C->>CDN1: GET seg_120 (current bitrate)
  CDN1--x C: timeout / 5xx
  C->>CDN2: retry seg_120 (alternate edge)
  alt cache hit
    CDN2-->>C: 200 seg_120
  else cache miss
    CDN2->>MID: fetch seg_120
    MID-->>CDN2: 200 seg_120
    CDN2-->>C: 200 seg_120
  end
  Note over C: ABR may downshift to maintain buffer
```

