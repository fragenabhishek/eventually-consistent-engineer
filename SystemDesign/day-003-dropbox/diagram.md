# diagram.md — ASCII Architecture & Data Flow Diagrams

## 1) High-Level Architecture

```
                +-------------------------+
                |        Clients          |
                |  Desktop / Mobile / Web |
                +-----------+-------------+
                            |
                            | HTTPS (Auth)
                            v
                   +--------+---------+
                   |  API Gateway /   |
                   |  Edge + RateLim  |
                   +---+----------+----+
                       |          |
          Metadata ops |          | Data plane (upload/download)
                       v          v
              +--------+--+    +--+------------------+
              | Metadata   |    | Upload/Download     |
              | Service    |    | Service             |
              +----+-------+    +----+----------------+
                   |                 |
          (strong) |                 | pre-signed URLs
                   v                 v
            +------+-------+    +---+------------------+
            | Metadata DB   |    | Object Storage (CAS) |
            | (SQL/NewSQL)  |    | chunks by hash       |
            +------+-------+    +---+------------------+
                   |
                   | events (file/version/share)
                   v
            +------+-------------------+
            | Event Bus (Kafka/PubSub) |
            +------+-------------------+
                   |
        +----------+-----------+------------------+
        |                      |                  |
        v                      v                  v
+-------+--------+   +---------+--------+  +------+---------+
| Sync Service   |   | Thumbnail Workers |  | Search Index   |
| change feed    |   | + virus scan      |  | name lookup    |
+-------+--------+   +---------+--------+  +------+---------+
        |                      |                  |
        | push hints           | thumbnails        |
        v                      v                  v
    +---+----+           +-----+-----+       +----+-----+
    | APNS/  |           | Object    |       | Query    |
    | FCM    |           | Storage   |       | Service  |
    +--------+           +-----------+       +----------+

                 +------------------------------+
                 | CDN (downloads/thumbnails)   |
                 +------------------------------+
```

---

## 2) Chunked Upload Data Flow

```
Client
  |
  | 1) POST /files/upload  (create upload session)
  v
Upload Service ---------------> Metadata Service -----> Metadata DB
  |                                   |
  | 2) return uploadId, chunkSize     |
  v                                   |
Client splits into chunks + hashes     |
  |
  | 3) POST /uploads/{id}/chunks:check (which hashes missing?)
  v
Upload Service
  |
  | 4) return signed URLs for missing chunks
  v
Client --------------- PUT chunk objects --------------> Object Storage
  |
  | 5) POST /uploads/{id}/commit (manifest)
  v
Upload Service ---- txn ----> Metadata DB (new version + changelog)
  |
  | 6) publish FILE_VERSION_CREATED
  v
Event Bus -> Sync Service / Thumbnail / Search
```

---

## 3) Sync Changes Flow (Eventual to Devices)

```
Metadata Service
  |
  | append ChangeLog(seq++) per user
  v
Metadata DB (ChangeLog)
  |
  | GET /sync/changes?cursor=...
  v
Client applies changes
  |
  | downloads new/changed content via signed CDN/object URLs
  v
CDN/Object Storage
```

Optional near-real-time hint:

```
Event Bus -> Sync Service -> APNS/FCM/WebSocket -> Client wakes -> pulls /sync/changes
```

---

## 4) Conflict Resolution (Offline edits)

```
Client A offline edits file F based on version V1
Client B online edits file F, produces V2

Client A comes online:
  POST /uploads/{id}/commit (baseVersionId=V1)

Server sees currentVersionId=V2 != V1
  -> create conflicted copy OR merge (if supported)
  -> emit changes for both entries
```

