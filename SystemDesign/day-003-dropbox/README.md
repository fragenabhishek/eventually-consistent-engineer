# Cloud Storage System (Dropbox / Google Drive) — System Design

## Problem Statement
Build a cloud file storage and synchronization service used by **hundreds of millions** of users. Users can upload/download files from any device and have changes automatically synced across devices. The system must be highly available, extremely durable (never lose data), support file operations (delete/rename/move), versioning (last 30 versions), sharing, name search, and offline mode.

---

## Functional Requirements
1. **Upload files** (any type, up to **5 GB** per file) with **resumable, chunked upload**.
2. **Download files** from any device.
3. **Automatic sync** across devices when files are updated.
4. **File/folder operations**: delete, rename, move.
5. **Versioning**: restore any of the **last 30 versions**.
6. **Sharing**: share file/folder with users; permissions: **view/edit**.
7. **Search**: by **file/folder name**.
8. **Offline mode**: changes made offline sync when connectivity returns.

## Non‑Functional Requirements
| Property | Target |
|---|---|
| Availability | **99.99%** |
| Durability | **99.999999999%** |
| Latency | **< 200 ms** for small file metadata reads & small downloads |
| Upload throughput | Large files uploaded in **chunks**, **resumable** |
| Consistency | **Strong** for metadata; **eventual** for sync propagation |
| Scale | **500M users**, **~1B files** |

---

## Capacity Estimation (Given)
- **Users**: 500M total, 50M daily active
- **Average file size**: 500 KB
- **New files/day**: 100M uploads
- **New storage/day**: ~50 TB/day (raw)
- **Total storage**: ~500 PB (with replicas)
- **Read:Write**: ~10:1

### Derived/Back-of-Envelope
- **Peak DAU concurrency** (rough): 50M DAU, assume 10% concurrently active → 5M active sessions.
- **Upload QPS** (rough): 100M/day ≈ 1157 uploads/sec avg; peak could be 5–10×.
- **Metadata ops QPS**: significantly higher due to sync polling/streaming events; plan for tens of thousands QPS per region.

---

## Core APIs (from prompt)
- `POST   /files/upload` → upload a file (return `fileId`)
- `GET    /files/{fileId}` → download a file
- `DELETE /files/{fileId}` → delete a file
- `PUT    /files/{fileId}/rename` → rename a file
- `GET    /files/{fileId}/versions` → list version history
- `POST   /files/{fileId}/restore` → restore a version
- `POST   /files/{fileId}/share` → share with another user
- `GET    /folders/{folderId}` → list folder contents

> Note: in the design we extend upload to include **upload sessions** + **chunk commit** endpoints; the original can remain a convenience wrapper.

---

## Out of Scope (Explicit)
- Real-time collaborative editing (Google Docs style) is **out of scope**.
- Full-text search inside file contents is **out of scope** (name-only search).

---

## What to Look for in the Design
- Separation of **metadata** (strongly consistent) from **blob data** (durable object storage).
- Efficient **chunked/resumable uploads**, **deduplication**, and **delta sync**.
- Event-driven sync propagation and conflict resolution for offline edits.
- Operational considerations: multi-region, retries, idempotency, GC, quotas, monitoring.
