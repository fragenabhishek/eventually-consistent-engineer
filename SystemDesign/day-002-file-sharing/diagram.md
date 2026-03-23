# Day 002 - Diagrams (File Sharing System / Pastebin)

## 1. System Architecture

```mermaid
flowchart TB
  client[Client] --> waf[CDN + WAF]
  waf --> lb[Load Balancer]

  lb --> write[Paste Write Service]
  lb --> read[Paste Read Service]

  write --> pg[(PostgreSQL Metadata)]
  write --> obj[(Object Store)]
  write --> cache[(Redis Cache)]

  read --> cache
  read --> pg
  read --> obj

  read --> kafka[(Kafka Topic: paste.viewed)]
  kafka --> analytics[Analytics Service]
  analytics --> olap[(OLAP / Reports)]

  workers[Cleanup Workers] --> pg
  workers --> obj
  workers --> cache
```

## 2. Create Flow (Sequence)

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant W as Paste Write Service
  participant O as Object Store
  participant P as PostgreSQL
  participant R as Redis

  C->>W: POST /api/v1/pastes
  W->>W: Validate size, visibility, expiry
  W->>W: Generate pasteId + delete token
  W->>O: PUT content blob
  O-->>W: blob key
  W->>P: INSERT metadata row
  P-->>W: OK
  W->>R: SET paste:{id}
  W-->>C: 201 Created (url, deleteToken)
```

## 3. Read Flow (Sequence)

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant S as Paste Read Service
  participant R as Redis
  participant P as PostgreSQL
  participant O as Object Store
  participant K as Kafka

  C->>S: GET /p/{pasteId}
  S->>R: GET paste:{id}

  alt Cache hit
    R-->>S: metadata + content
    S-->>C: 200 OK
    S->>K: Publish view event (async)
  else Cache miss
    R-->>S: miss
    S->>P: SELECT metadata by pasteId
    alt Active metadata found
      P-->>S: metadata
      S->>O: GET content blob
      O-->>S: content
      S->>R: SET paste:{id}
      S-->>C: 200 OK
      S->>K: Publish view event (async)
    else Not found/deleted/expired
      P-->>S: no row or inactive
      S-->>C: 404 or 410
    end
  end
```

## 4. Burn-After-Read Flow

```mermaid
flowchart LR
  readReq[GET /p/{id}] --> lock[Acquire short lock on pasteId]
  lock --> check{Already consumed?}
  check -- Yes --> gone[Return 410 Gone]
  check -- No --> serve[Serve content once]
  serve --> mark[Mark consumed/deleted]
  mark --> evict[Evict cache + schedule blob delete]
```
