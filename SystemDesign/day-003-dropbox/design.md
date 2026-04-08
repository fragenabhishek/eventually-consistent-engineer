# design.md — High Level Design (HLD)

## 1) High-Level Architecture
We split the system into two planes:

1. **Metadata plane (strong consistency)**: file/folder tree, versions, permissions, share links, search index.
2. **Data plane (high durability, scalable throughput)**: chunk/blob storage, CDN distribution, background processing.

### Core Components
- **API Gateway / Edge**
  - AuthN/AuthZ (OAuth/JWT), rate limiting, request routing.
- **Metadata Service**
  - File/folder CRUD, version pointers, ACLs, share links.
  - Strongly consistent DB.
- **Upload Service**
  - Creates upload sessions, issues pre-signed upload URLs, tracks chunk receipts, commits uploads.
- **Download Service**
  - Resolves file version → chunk list → generates signed CDN/object URLs.
- **Sync Service**
  - Device state tracking (cursors), change feed, push notifications.
- **Chunking/Dedup Service**
  - Client-assisted chunking (rolling hash) and server verification.
  - Content-addressable storage (CAS) via chunk hashes.
- **Background Workers**
  - Thumbnail generation, virus scanning, OCR (optional), retention/GC.
- **Event Bus**
  - Kafka/PubSub for metadata change events and processing pipelines.
- **Search Service**
  - Name search via inverted index (e.g., Elastic/OpenSearch) or DB secondary index.

### Storage
- **Metadata DB (strong)**: relational (sharded) or NewSQL (Spanner/Cockroach) for strong consistency.
- **Object Storage (durable)**: multi-AZ object store for chunks/blobs.
- **Cache**: Redis/Memcached for hot metadata, share link resolution.
- **CDN**: edge caching for downloads + thumbnails.

---

## 2) Data Model
### Entities

#### Users
- `user_id (PK)`
- `email`, `status`, `plan`, `quota_bytes`, `used_bytes`

#### Devices
- `device_id (PK)`, `user_id (FK)`
- `last_seen`, `client_version`
- `sync_cursor` (monotonic position in change feed)

#### Folders
- `folder_id (PK)`
- `owner_user_id`
- `parent_folder_id` (nullable for root)
- `name`
- `created_at`, `updated_at`
- `deleted_at` (nullable; soft delete/trash)

#### Files (logical file entry)
- `file_id (PK)`
- `owner_user_id`
- `parent_folder_id`
- `name`
- `mime_type`
- `current_version_id`
- `is_deleted`, `deleted_at`
- `created_at`, `updated_at`

#### FileVersions
- `version_id (PK)`
- `file_id (FK)`
- `version_number` (1..N)
- `size_bytes`
- `content_manifest_id` (points to ordered list of chunks)
- `etag` (hash of manifest)
- `created_by_device_id`
- `created_at`

#### Manifests
Represents an ordered list of chunks.
- `manifest_id (PK)`
- `chunks` (list of `(chunk_hash, offset, length)`)
- `total_size`
- `rolling_hash_params` (optional)

#### Chunks (content-addressed)
- `chunk_hash (PK)` (SHA-256)
- `size_bytes`
- `storage_object_key`
- `ref_count`
- `created_at`

#### Shares / ACLs
- `share_id (PK)`
- `resource_type` (file|folder)
- `resource_id`
- `grantee_user_id` (or group)
- `permission` (view|edit)
- `created_at`, `expires_at (optional)`

#### ChangeLog (for sync)
Append-only log of metadata changes.
- `seq (PK, monotonic)`
- `user_id` (partition key)
- `event_type` (create/update/delete/move/share/version)
- `resource_id`, `resource_type`
- `payload` (small JSON)
- `created_at`

> Strong metadata: file tree, versions, ACLs live in strongly consistent DB.
> Eventual sync: clients converge via ChangeLog + retries.

---

## 3) Key Workflows

### A) Chunked, Resumable Upload (5 GB)
**Goal**: tolerate network drops, allow resume, maximize throughput.

#### API Sequence
1) `POST /files/upload` → creates **upload session**
   - Request: `{ parentFolderId, fileName, sizeBytes, mimeType, clientChunking: true/false }`
   - Response: `{ fileId, uploadId, preferredChunkSize, uploadUrls: [...] }`

2) Client splits file into chunks (e.g., 4–16 MB) using **content-defined chunking** (CDC) for better delta sync.
   - For each chunk: compute `chunk_hash`.

3) Client asks server which chunks are missing:
   - `POST /uploads/{uploadId}/chunks:check` with list of `chunk_hash`.
   - Server returns missing hashes.

4) Client uploads only missing chunks via **pre-signed URLs** directly to object storage:
   - `PUT {signedUrl}` per chunk (or multipart upload).

5) Client commits upload:
   - `POST /uploads/{uploadId}/commit` with ordered chunk list (manifest).
   - Metadata Service creates new FileVersion, updates `current_version_id`, appends ChangeLog events.

#### Resuming
- Upload session stores received chunk hashes and expiry.
- Client can call `GET /uploads/{uploadId}` to resume (returns missing chunks).

#### Idempotency
- `uploadId` + `chunk_hash` makes chunk PUT idempotent.
- `commit` uses an idempotency key to avoid double version creation.

---

### B) Download
1) `GET /files/{fileId}`
2) Download Service checks ACL → resolves `current_version_id` → manifest.
3) Returns either:
   - a single signed URL for a packed object (optional optimization), or
   - a manifest with signed URLs per chunk (client reassembles), or
   - a CDN URL for cached blob.

**Hot path optimization**:
- For small files store as single object; for large files chunked.
- CDN caches popular content; signed URLs prevent unauthorized access.

---

### C) Sync Across Devices
**Model**: per-user change feed with cursor.

1) Device subscribes:
   - `GET /sync/changes?cursor=...` returns list of changes + new cursor.
2) Server can also push hints via WebSocket / push notifications (APNS/FCM) to reduce polling.
3) Device applies metadata changes and downloads/upload deltas as needed.

**Eventual consistency**:
- Change events propagate asynchronously.
- Metadata changes are strongly consistent at source of truth; devices converge.

---

### D) Offline Mode
- Client maintains a **local journal** of operations:
  - rename/move/delete, plus new content manifests.
- When online:
  - Replays operations with idempotency keys.
  - Sync engine fetches remote changes and resolves conflicts.

---

### E) Versioning (last 30)
- Each committed upload creates a new `FileVersion` referencing a manifest.
- Maintain last **30** versions per file.
- Older versions are marked for deletion; chunk refcounts decremented by GC worker.

**Restore**:
- `POST /files/{fileId}/restore` with `version_id`.
- Creates a new version whose manifest points to restored version (fast, metadata-only) and updates current pointer.

---

### F) Sharing (file/folder)
- Share entry created in `Shares/ACLs`.
- Permission evaluation during metadata and download:
  - direct ownership OR share grants via folder inheritance.
- Optional: share links with token (`/s/{token}`) + expiry.

---

### G) Search by Name
- Name-only search:
  - Option A: DB index on `(owner_user_id, name_prefix)` for prefix search.
  - Option B: Async indexing to Search Service for fuzzy search.

---

### H) Thumbnails (Async)
- On new image/PDF upload commit → publish `FILE_VERSION_CREATED` event.
- Worker downloads needed bytes (range requests), generates thumbnail variants, stores in object storage, updates metadata.

---

## 4) Consistency & Concurrency

### Metadata
- Strong consistency via transactions:
  - rename/move updates folder entries atomically.
  - version creation updates file pointer + changelog in one transaction.

### Conflict Resolution
When the same logical file is edited concurrently (especially offline):
- Use `base_version_id` provided by client on commit.
- If server current != base:
  - If file is binary or merge not supported → create **conflicted copy**:
    - `filename (conflicted copy from <device> at <time>)`
  - If text and merge supported (optional) → attempt 3-way merge.

### Last Writer Wins vs Conflicted Copy
- Default: **conflicted copy** (Dropbox-style) to avoid silent data loss.

---

## 5) Storage Backend Choice
### Object Storage vs Block Storage
- **Object storage** fits best: huge scale, immutable blobs, cheap, multi-AZ durability.
- Blocks/chunks are natural objects; manifests provide file assembly.

### Content-Addressable Storage (Dedup)
- Store chunks by `chunk_hash`.
- If 1,000 users upload same file/chunks, only one copy stored; metadata references shared chunks.
- Refcount + GC to reclaim when no versions reference a chunk.

### Delta Sync
- With CDC, small edits change only nearby chunks; only changed chunks re-upload.

---

## 6) API Details (Extended)

### Upload
- `POST /files/upload`
  - Creates file entry (or placeholder) + upload session.

- `POST /uploads/{uploadId}/chunks:check`
  - Request: `{ chunkHashes: [...] }`
  - Response: `{ missing: [...], present: [...] }`

- `POST /uploads/{uploadId}/chunkUrls`
  - Request: `{ chunkHashes: [...], sizes: [...] }`
  - Response: `{ urls: { chunkHash: signedUrl } }`

- `POST /uploads/{uploadId}/commit`
  - Request: `{ fileId, baseVersionId, manifest: [...], totalSize, idempotencyKey }`
  - Response: `{ versionId, etag }`

### Metadata ops
- `PUT /files/{fileId}/rename` `{ newName }`
- `PUT /files/{fileId}/move` `{ newParentFolderId }`
- `DELETE /files/{fileId}` (soft delete → trash)

### Versions
- `GET /files/{fileId}/versions`
- `POST /files/{fileId}/restore` `{ versionId }`

### Sharing
- `POST /files/{fileId}/share` `{ granteeUserId, permission }`
- `POST /folders/{folderId}/share` `{ granteeUserId, permission }`

### Sync
- `GET /sync/changes?cursor=...&limit=...`
- `POST /sync/ack` (optional)

---

## 7) Scalability Plan

### Partitioning
- Shard metadata by `user_id` (owner) to localize file tree operations.
- Changelog partitioned by `user_id` for efficient sync streaming.

### Caching
- Cache folder listings and file metadata (short TTL + invalidation via events).
- CDN for downloads/thumbnails.

### Multi-Region
- Active-active metadata per region is complex with strong consistency; practical approach:
  - Choose a **home region per user** for metadata writes.
  - Read replicas in other regions; client routed to home for writes.
- Data plane (object storage) replicated multi-AZ and optionally cross-region.

### Hot-Spot Mitigation
- Large shared folders: use pagination and maintain materialized views per folder.
- Share ACL evaluation cached.

---

## 8) Security
- TLS everywhere, signed URLs for blob access.
- Encryption at rest (KMS-managed keys).
- Per-tenant quotas, abuse detection.
- Virus/malware scanning pipeline.

---

## 9) Observability
- Metrics: upload success rate, chunk dedup ratio, sync lag, metadata p99 latency.
- Tracing across API → DB → object store.
- Audit logs for share and delete actions.

---

## 10) What Happens on Delete?
- Soft delete → Trash (retention window configurable).
- Permanent delete triggers:
  - remove metadata pointers
  - decrement chunk refcounts
  - GC removes chunks with `ref_count == 0`.

