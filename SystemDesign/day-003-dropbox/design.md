# Day 003 — Design: Cloud Storage (Dropbox)

## 1. High-Level Architecture

```
Client (Desktop / Mobile / Web)
    |
    v
[CDN]  ←── serves file downloads for hot content
    |
    v
[Load Balancer]
    |
    +──► [API Gateway]
              |
              +──► [Auth Service]            ← JWT validation
              |
              +──► [Metadata Service]        ← file/folder hierarchy, versions, shares
              |         └──► [PostgreSQL: metadata]
              |         └──► [Redis: hot metadata cache]
              |
              +──► [File Service]            ← upload/download coordination
              |         └──► [Object Storage (S3)]   ← raw file chunks
              |         └──► [Dedup Index (Redis/DB)] ← content-hash → chunk ref
              |
              +──► [Sync Service]            ← WebSocket; pushes delta events to clients
              |         └──► [Kafka: file.changed events]
              |
              +──► [Notification Service]    ← email/push on share, comment, quota
              |
              └──► [Thumbnail Service]       ← async image/video preview generation

Background Workers:
  ├── Version Cleanup Worker  (prune versions beyond limit)
  ├── Quota Enforcement Worker
  └── Virus Scan Worker
```

---

## 2. Core Services and Responsibilities

### API Gateway
- Routes to Auth, Metadata, File, Sync services
- Enforces per-user rate limits
- SSL termination

### Metadata Service
- Manages the virtual file tree (folders, file names, paths)
- Stores file versions, owner, permissions, share links
- Does NOT store file bytes — only references to object storage
- Handles rename, move, delete, restore, version history

### File Service (Upload / Download)
- Receives chunked uploads; each chunk identified by its SHA-256 hash
- Performs **content-addressable storage**: if chunk hash already exists, skip upload (deduplication)
- Generates pre-signed S3 URLs for direct client → S3 uploads (off-loads bandwidth from app servers)
- Supports **resumable uploads**: client tracks which chunks are committed; resumes from last chunk on reconnect

### Sync Service
- Maintains a persistent WebSocket connection per client session
- When a file changes, publishes a `file.changed` event to Kafka
- Kafka consumers fan-out the event to all affected client sessions (e.g., all devices of a user, or all collaborators in a shared folder)
- Client receives delta event → pulls only changed metadata + new chunks

### Deduplication Index
- Maps `SHA-256(chunk)` → S3 object key
- Before uploading any chunk, client or File Service checks this index
- If hash exists → skip upload, just record the reference (zero-byte upload for unchanged content)
- Enables: 1,000 users uploading the same file → stored once

---

## 3. Key Design Decision: Chunked Upload Strategy

Files are split into fixed-size chunks (e.g., 4 MB each) before upload.

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| Single-file upload | Simple | Entire upload fails on network drop; no parallelism | No |
| Chunked sequential upload | Resumable; smaller retries | Still slow for large files | Partial |
| Chunked parallel upload | Resumable + fast (parallel chunk upload to S3) | Client complexity | **Yes** |

**Flow:**
1. Client splits file into 4 MB chunks.
2. Client sends chunk hashes to File Service (`POST /upload/init`).
3. File Service responds: "upload chunks X, Y, Z" (skipping already-known hashes).
4. Client uploads only missing chunks directly to S3 via pre-signed URLs.
5. Client sends `POST /upload/complete` — File Service assembles the manifest.
6. Metadata Service creates/updates the file record.

---

## 4. Key Design Decision: Delta Sync

When a 500 MB file changes by 2 KB, we must not re-upload the entire file.

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| Re-upload entire file | Simple | Wastes bandwidth | No |
| Block-level diff (rsync algorithm) | Only sends changed blocks | Requires block inventory on client | **Yes** |

**How it works:**
- Client maintains a local manifest: `{chunkIndex → SHA-256}` for every file.
- On file change, client recomputes chunk hashes and diffs against the stored manifest.
- Only chunks whose hash changed are uploaded.
- Result: a 500 MB file with a 2 KB edit → upload ~4 MB (one changed chunk).

---

## 5. Key Design Decision: Conflict Resolution

Two devices edit the same file simultaneously while offline.

| Approach | Pros | Cons | Decision |
|----------|------|------|----------|
| Last-write-wins | Simple | Silent data loss | No |
| Server-side merge | Works for text/code | Complex; breaks binary files | No |
| Create conflict copy | Safe; no data loss | User must resolve manually | **Yes** |

**Dropbox approach:** When device A's change and device B's change are both uploaded, the Sync Service detects the diverged parent version and creates `filename (Device A's conflicted copy).ext`. Both versions are preserved. User resolves manually.

---

## 6. Data Model

### files

| Column | Type | Notes |
|--------|------|-------|
| file_id | UUID PK | Unique file identity |
| owner_id | UUID | Owning user |
| parent_folder_id | UUID NULL | NULL = root |
| name | VARCHAR(255) | File name in this folder |
| is_deleted | BOOLEAN | Soft delete |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### file_versions

| Column | Type | Notes |
|--------|------|-------|
| version_id | UUID PK | |
| file_id | UUID | FK → files |
| size_bytes | BIGINT | |
| content_hash | CHAR(64) | SHA-256 of full content |
| chunk_manifest | JSONB | `[{index, hash, s3_key}]` |
| created_by_device | VARCHAR | Device identifier |
| created_at | TIMESTAMP | |

### folders

| Column | Type | Notes |
|--------|------|-------|
| folder_id | UUID PK | |
| owner_id | UUID | |
| parent_folder_id | UUID NULL | NULL = root |
| name | VARCHAR(255) | |
| is_deleted | BOOLEAN | |

### shares

| Column | Type | Notes |
|--------|------|-------|
| share_id | UUID PK | |
| resource_id | UUID | File or folder |
| resource_type | VARCHAR | 'file' or 'folder' |
| shared_with_user_id | UUID NULL | NULL = public link |
| permission | VARCHAR | 'view' or 'edit' |
| link_token | CHAR(32) NULL | For public link sharing |

### chunk_dedup_index

| Column | Type | Notes |
|--------|------|-------|
| chunk_hash | CHAR(64) PK | SHA-256 of chunk bytes |
| s3_key | VARCHAR | Location in object storage |
| ref_count | INT | How many versions reference this chunk |
| created_at | TIMESTAMP | |

**Indexes:**
- `files`: index on `(owner_id, parent_folder_id, is_deleted)`
- `file_versions`: index on `(file_id, created_at DESC)`
- `shares`: index on `(shared_with_user_id)` for "shared with me" view

---

## 7. Request Flows

### Upload File (new or updated)

1. Client computes chunk hashes for the file.
2. `POST /upload/init` → sends `{fileId, versionParentId, chunkHashes[]}`.
3. File Service checks dedup index → returns list of chunks to actually upload.
4. File Service generates pre-signed S3 URLs for each needed chunk.
5. Client uploads chunks directly to S3 in parallel.
6. Client calls `POST /upload/complete` with chunk manifest.
7. File Service writes new entry to `chunk_dedup_index` for new chunks.
8. Metadata Service creates new `file_versions` row + updates `files.updated_at`.
9. File Service publishes `file.changed` event to Kafka.
10. Sync Service delivers delta event to all connected clients of the same user.

### Download File

1. `GET /files/{fileId}?version={versionId}` → Metadata Service returns chunk manifest.
2. File Service generates pre-signed S3 URLs for each chunk (or redirects via CDN).
3. Client downloads chunks in parallel and assembles locally.

### Sync on Reconnect (client comes back online)

1. Client sends `GET /sync/delta?since={lastSyncTimestamp}`.
2. Sync Service returns list of `file.changed` events since that timestamp.
3. Client applies deltas: downloads changed files, moves/renames, deletes.

---

## 8. Caching Strategy

- **Hot file metadata:** Redis, key = `file:{fileId}:latest`, TTL = 5 minutes
- **Chunk dedup index:** Redis hash for frequently checked chunk hashes (L1 before DB)
- **CDN:** Caches S3 signed URLs for publicly shared files or thumbnails
- **Negative cache:** Short TTL (30 s) for "file not found" to protect DB

---

## 9. Scalability Plan

### Read scaling
- Metadata Service: stateless, horizontal scaling; read replicas for DB
- File download: direct S3 or CDN — completely bypasses app servers

### Write scaling
- File upload: goes directly to S3 via pre-signed URLs — app server only coordinates
- Metadata writes: PostgreSQL primary; partition `file_versions` by `file_id` hash if very large

### Storage growth
- Object storage (S3): natively scalable, charged per GB — no management needed
- Dedup reduces storage by 20-60% in practice
- Lifecycle policies move old versions to S3 Glacier after 30 days

---

## 10. Reliability and Resilience

- **Multi-AZ:** All services, DB, Redis, and Kafka deployed across 3 availability zones
- **S3 durability:** 99.999999999% — no additional replication needed
- **Chunk integrity:** SHA-256 verified after each chunk upload; corrupt chunks are re-uploaded
- **Sync delivery guarantee:** Kafka ensures at-least-once delivery of change events; clients apply events idempotently
- **Quota enforcement:** Soft quota warnings at 90%; hard stop at 100% with clear error message

---

## 11. SLOs and Key Metrics

**Latency targets:**
- Metadata API P99 < 100 ms
- Sync delta delivery P99 < 5 seconds from upload completion
- Thumbnail generation: < 30 seconds (async, non-blocking)

**Availability target:** 99.99%

**Key metrics:**
- Upload success rate and average chunk re-upload rate
- Sync lag (time between upload and client receiving delta event)
- Dedup ratio (bytes saved / bytes uploaded)
- Quota utilization distribution across users
- Virus scan queue depth and processing lag
