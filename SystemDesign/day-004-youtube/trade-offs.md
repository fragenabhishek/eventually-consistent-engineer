# trade-offs.md — Decisions, Trade-offs & Failure Modes

## 1) Async Transcoding vs Synchronous
**Decision:** Upload API only finalizes the raw asset; transcoding runs **asynchronously** via queue + workers.

**Why:** Keeps upload reliability high and avoids holding connections while encoding.

**Trade-off:** Video not immediately playable; needs status handling (PROCESSING).

---

## 2) ABR (HLS/DASH) with Segments
**Decision:** Store content as **short segments** + **manifests**.

**Why:** Enables seamless bitrate switching and efficient CDN caching.

**Trade-off:** More objects/metadata; packaging complexity.

---

## 3) Codec Choices (H.264 vs H.265 vs AV1)
**Decision:** Start with H.264 for broad compatibility; optionally add H.265/AV1 for bandwidth savings.

**Trade-off:** AV1 encoding is slower (more compute) but can reduce CDN egress.

---

## 4) Storage Explosion (Raw + Multiple Renditions)
**Decision:** Keep raw + required renditions; apply lifecycle rules:
- Hot videos: keep all renditions readily available.
- Cold videos: move higher resolutions to cold tier; rehydrate on demand.

**Trade-off:** Cold start for rarely watched high-res content.

---

## 5) CDN vs Origin
**Decision:** CDN is the primary serving layer; origin is shielded and scalable.

**Why:** Read:Write ~10,000:1 and global low-latency requirement.

**Trade-off:** Cache invalidation and token revocation complexity.

Mitigation:
- short TTL for manifests
- signed URLs
- versioned segment paths

---

## 6) View Count: Accuracy vs Freshness
**Decision:** Near real-time view counts use **approximate, sharded counters**.

**Why:** Exact increments at 5B streams/day are expensive.

**Trade-off:** Counts may be temporarily off; reconcile with batch jobs.

---

## 7) Search Index Consistency
**Decision:** Async indexing from metadata changes.

**Trade-off:** Newly published video may take seconds/minutes to appear.

Mitigation:
- dual-read fallback (metadata DB for owner’s drafts)
- prioritize indexing for trending creators

---

## 8) Failure Modes & Handling

### A) Upload Interrupted
- Use multipart upload + upload sessions.
- `GET /uploads/{id}` returns missing parts; client resumes.

### B) Duplicate Completion / Retries
- Idempotency keys on `complete`.

### C) Transcoding Worker Failures
- Jobs are retryable; store progress checkpoints.
- Poison queue for repeated failures; mark video as FAILED with reason.

### D) Queue Backlog / Spike (viral uploads)
- Autoscale workers.
- Apply admission control; prioritize premium creators.

### E) CDN Cache Miss Storm (viral video)
- Origin shield tier + pre-warming for first segments.
- Rate-limit origin.

### F) Metadata DB Outage
- Graceful degradation:
  - playback can continue for cached content
  - new uploads paused
  - read replicas for metadata reads

### G) Counter Store Outage
- Buffer events in Kafka; rebuild counters from stream.

### H) Abuse / View Fraud
- Deduplicate by session; anomaly detection; delayed validation.

---

## 9) Operational Considerations
- Monitor: transcoding time per rendition, playback start p95/p99, rebuffer ratio.
- Cost levers: codec ladder tuning, segment duration, cold storage policies.

