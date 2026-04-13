# Day 003 — Diagrams: Cloud Storage (Dropbox)

## 1. System Architecture

```mermaid
flowchart TB
  client[Desktop / Mobile / Web Client]

  client --> cdn[CDN]
  client --> lb[Load Balancer]
  cdn --> s3[(Object Storage S3)]

  lb --> gw[API Gateway + Auth]

  gw --> meta[Metadata Service]
  gw --> file[File Service]
  gw --> sync[Sync Service]

  meta --> pg[(PostgreSQL: files / versions / shares)]
  meta --> rcache[(Redis: hot metadata)]

  file --> dedup[(Dedup Index Redis)]
  file --> s3

  sync --> kafka[(Kafka: file.changed)]
  kafka --> syncConsumer[Sync Consumer]
  syncConsumer --> sync

  s3 --> thumb[Thumbnail Service]
  thumb --> s3

  workers[Background Workers] --> pg
  workers --> s3
  workers --> rcache
```

---

## 2. Chunked Upload Flow (Sequence)

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant F as File Service
  participant D as Dedup Index
  participant S as S3
  participant M as Metadata Service
  participant K as Kafka

  C->>C: Split file into 4MB chunks, compute SHA-256 per chunk
  C->>F: POST /upload/init  {fileId, parentVersionId, chunkHashes[]}
  F->>D: Which hashes already exist?
  D-->>F: knownHashes[]
  F-->>C: Upload these chunks (unknown only) + pre-signed S3 URLs
  C->>S: PUT chunk (parallel, direct to S3)
  S-->>C: 200 OK per chunk
  C->>F: POST /upload/complete  {fileId, chunkManifest[]}
  F->>D: Increment ref_count for each chunk
  F->>M: Create file_version row
  M-->>F: OK
  F->>K: Publish file.changed event
  F-->>C: 201 Created (versionId)
```

---

## 3. Sync / Delta Delivery Flow (Sequence)

```mermaid
sequenceDiagram
  autonumber
  participant DeviceA as Device A (uploader)
  participant FS as File Service
  participant K as Kafka
  participant SC as Sync Consumer
  participant DeviceB as Device B (another device)

  DeviceA->>FS: POST /upload/complete
  FS->>K: Publish {userId, fileId, versionId, event: "FILE_UPDATED"}
  K->>SC: Consume event
  SC->>SC: Look up all active WebSocket sessions for userId
  SC->>DeviceB: Push delta {fileId, versionId, changeType}
  DeviceB->>DeviceB: Compare with local manifest
  DeviceB->>FS: GET /files/{fileId}/version/{versionId}/chunks (only changed chunks)
  FS->>DeviceB: Pre-signed S3 URLs for new chunks
  DeviceB->>DeviceB: Download + assemble locally
```

---

## 4. Conflict Detection and Resolution

```mermaid
flowchart TD
  upload[Device B tries to upload] --> check{Does parentVersionId match server's latest?}
  check -- Yes, no conflict --> normal[Normal version created]
  check -- No, diverged --> conflict[Conflict detected]
  conflict --> copy[Create conflict copy: filename_device_B_conflicted.ext]
  copy --> bothSaved[Both versions saved in version history]
  bothSaved --> notify[Notify user via Sync Service]
```

---

## 5. Background Jobs

```mermaid
flowchart LR
  subgraph Version_Cleanup
    v1[Find file_versions beyond retention limit] --> v2[Delete DB rows]
    v2 --> v3[Decrement chunk ref_count]
    v3 --> v4[GC: delete S3 objects where ref_count = 0]
  end

  subgraph Virus_Scan
    s1[New upload event] --> s2[Scan chunk bytes]
    s2 --> s3{Clean?}
    s3 -- Yes --> s4[Release quarantine flag]
    s3 -- No --> s5[Block access + alert owner]
  end

  subgraph Quota_Check
    q1[Daily quota usage job] --> q2{Approaching limit?}
    q2 -- 80% --> q3[Send warning email]
    q2 -- 100% --> q4[Block new uploads]
  end
```
