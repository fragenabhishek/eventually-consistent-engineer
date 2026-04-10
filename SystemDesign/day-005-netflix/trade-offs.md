# trade-offs.md — Decisions, Trade-offs, Failure Modes

## 1) Push-based CDN (Pre-positioning) vs Pull-only CDN
**Decision:** Use **push-first** pre-positioning to ISP edges, with pull-on-miss fallback.

**Why:** Pull-only cannot meet <2s startup at 500 Tbps peak; origin would be overwhelmed.

**Trade-offs:**
- Requires accurate demand forecasting.
- Risk of wasted bandwidth/storage for mispredicted content.

Mitigations:
- refresh placement daily + reactive hot-content pushes
- store only a subset of renditions in smaller edges

---

## 2) Segment Duration (2s vs 6s)
**Decision:** Prefer **2–4s** segments for faster startup and smoother ABR.

**Trade-offs:**
- More objects and manifest entries.
- Higher overhead in CDN caches and request rate.

Mitigations:
- HTTP/2/3, connection reuse
- mid-tier aggregation

---

## 3) DRM Licensing
**Decision:** License per session (not per segment) with encrypted segments.

**Why:** Avoid adding latency on every segment request.

**Trade-offs:**
- License service becomes critical.

Mitigations:
- regional license servers
- caching of license responses where allowed
- graceful fallback to lower quality if license delays

---

## 4) Concurrent Stream Enforcement Consistency
**Decision:** **Strong** enforcement at playback start via session coordinator.

**Why:** Product requires reliable limits per tier.

**Trade-offs:**
- Adds dependency on session store; must be fast.

Mitigations:
- store sessions in strongly consistent KV (or SQL) with TTL
- accept a tiny race window (double start) and reconcile with revocation

---

## 5) Edge Selection: GeoDNS vs Anycast vs Client-side RTT probes
**Decision:** Use **GeoDNS + telemetry** with sticky mapping; optional Anycast entry.

**Trade-offs:**
- DNS caching can delay failover.

Mitigations:
- short DNS TTL for edge names
- multi-candidate edge list in manifest
- client retry logic

---

## 6) Metadata Freshness vs Cacheability
**Decision:** Cache catalog and title metadata aggressively, but:
- keep user-specific rails (Continue Watching) dynamic.

Trade-offs:
- stale catalog changes for a short period.

Mitigation:
- versioned catalog snapshots per region + invalidation events

---

## Failure Modes & Responses

### A) Edge server down mid-stream
- Player retries next segment from alternate edge (provided via manifest or DNS remap).
- Mid-tier/origin acts as fallback.

### B) ISP cluster outage
- DNS remaps to nearby ISP cluster/regional PoP.
- Expect slightly higher RTT; ABR drops bitrate to maintain buffering target.

### C) Origin brownout
- Shield protects origin; serve cached content.
- Deprioritize cold content pulls; keep hot startup segments available.

### D) License service outage
- Multi-region license servers.
- If hard down: playback blocked (for DRM) → show error + retry.
- If degraded: allow manifest fetch but delay playback start until license obtained.

### E) Session store outage
- Fail-closed or fail-open?
  - Usually **fail-closed** for enforcement correctness.
  - Optionally allow a short grace window for active sessions.

### F) Pre-positioning failure
- Pull-on-miss continues; may hurt startup time.
- Backfill during off-peak; alert on hit-rate drop.

### G) Token leakage / hotlinking
- Short TTL signed URLs + token binding to account/device.
- Rotate signing keys; revoke tokens.

---

## Cost Levers
- Codec ladder tuning (AV1 for bandwidth reduction vs compute cost).
- Store fewer high-bitrate variants at small edges.
- Cold storage lifecycle for rarely watched titles.

