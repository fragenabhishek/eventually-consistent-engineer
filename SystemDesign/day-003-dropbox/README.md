# Day 003 — Cloud Storage System (Dropbox / Google Drive)

> **Interview Goal:** Design a file storage and sync service used by millions.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

You're a backend engineer at a startup building a cloud storage product. Users want to upload files from any device, access them anywhere, and automatically sync changes across all their devices. Think Dropbox, Google Drive, or OneDrive.

---

## Functional Requirements

1. Users can **upload** files (any type, up to 5 GB per file).
2. Users can **download** files from any device.
3. Files automatically **sync** across all devices when updated.
4. Users can **delete**, **rename**, and **move** files and folders.
5. Support **versioning** — users can restore any of the last 30 versions of a file.
6. Support **sharing** — a user can share a file or folder with other users (view or edit permission).
7. Users can **search** their files by name.
8. Support for **offline mode** — changes made offline sync when connectivity returns.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Availability | 99.99% |
| Durability | 99.999999999% (files must never be lost) |
| Read/write latency | < 200 ms for small files |
| Upload throughput | Large files uploaded in chunks (resumable) |
| Consistency | Eventual consistency for sync; strong for metadata |
| Scale | 500 million users, ~1 billion files stored |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Users | 500M total, 50M daily active |
| Avg file size | 500 KB |
| New files/day | 100M uploads |
| Storage/day | ~50 TB new data per day |
| Total storage | ~500 PB (with replicas) |
| Read:Write ratio | ~10:1 (mostly reads/syncs) |

---

## Core API to Design

```
POST   /files/upload           → upload a file (return fileId)
GET    /files/{fileId}         → download a file
DELETE /files/{fileId}         → delete a file
PUT    /files/{fileId}/rename  → rename a file
GET    /files/{fileId}/versions → list version history
POST   /files/{fileId}/restore → restore a version
POST   /files/{fileId}/share   → share with another user
GET    /folders/{folderId}     → list folder contents
```

---

## Key Challenges to Think About

- **Chunked upload:** How do you handle 5 GB files? What happens if upload is interrupted midway?
- **Deduplication:** If 1,000 users upload the same 500 MB file, do you store it 1,000 times?
- **Delta sync:** When a 1 GB file changes by 1 KB, do you re-upload the whole file?
- **Conflict resolution:** What happens when the same file is edited on two devices simultaneously while offline?
- **Storage backend:** Block storage vs object storage — which suits this use case and why?
- **Metadata vs file data:** Should they be stored in the same system? What are the trade-offs?
- **Thumbnail generation:** How would you generate thumbnails for images asynchronously?

---

## Clarifying Questions (practice asking these in an interview)

1. Do we need real-time collaboration (simultaneous editing like Google Docs)?
2. What is the maximum file size per upload?
3. How many versions should we keep per file?
4. Do we need mobile clients (different sync behaviour)?
5. Should deleted files go to a trash/recycle bin before permanent deletion?
6. Is file search full-text (content) or name-only?

---

## Concepts Tested

`Object Storage (S3)` · `Chunked Upload` · `CDN` · `Metadata DB` · `Deduplication (Content-addressable storage)` · `Event-driven sync` · `Conflict resolution` · `Versioning`
