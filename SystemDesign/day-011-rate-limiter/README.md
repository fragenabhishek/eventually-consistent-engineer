# Day 011 — Rate Limiter

> **Interview Goal:** Design a rate limiting system that can be used as middleware across all services.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Your company has a public API used by thousands of clients. Without rate limiting:
- A buggy client can loop and send 10,000 requests/second, crashing your servers.
- A malicious actor can brute-force a login endpoint.
- A single tenant can consume all capacity, degrading service for others.

You need a rate limiter that can be dropped in front of any service — transparent, fast, and distributed.

---

## Functional Requirements

1. **Throttle requests** per client (by user ID, API key, or IP address).
2. Support **multiple rate limit rules**: e.g., 100 requests/minute AND 10,000 requests/day.
3. Return **HTTP 429 Too Many Requests** with a `Retry-After` header when limit is exceeded.
4. Support **different limits per endpoint**: login = 5/min, search = 100/min, upload = 10/min.
5. **Soft limits** for some clients (burst allowance): a client with 100 req/min can briefly burst to 150.
6. Limits should apply across **all servers** in the cluster, not per-server (distributed rate limiting).
7. Graceful handling: the system should still work (but not rate-limit) if the limiter itself is unavailable.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Latency overhead | < 5 ms added per request |
| Availability | 99.99% (rate limiter must not become a single point of failure) |
| Accuracy | At most 1% over-counting or under-counting acceptable |
| Scale | 10M requests/second across all services |
| Consistency | Eventual (small over-limit acceptable; better than blocking all traffic) |

---

## Rate Limiting Algorithms to Know

| Algorithm | How It Works | Pros | Cons |
|-----------|-------------|------|------|
| **Token Bucket** | Bucket fills at a fixed rate; each request costs 1 token | Allows bursts, simple | State per client |
| **Leaky Bucket** | Requests queue up and drain at a fixed rate | Smooth output | Drops bursts, needs queue |
| **Fixed Window Counter** | Count requests in current time window (e.g., this minute) | Very simple | Boundary spikes (100 at 0:59, 100 at 1:00 = 200 in 2s) |
| **Sliding Window Log** | Store timestamp of each request; count in rolling window | Accurate | High memory |
| **Sliding Window Counter** | Weighted interpolation of current + previous window | Good accuracy + low memory | Slight approximation |

---

## Core API to Design

```
# Client-facing (HTTP middleware — no direct API, transparent)
→ Every incoming request passes through the rate limiter before reaching the service

# Internal admin API
POST   /limits                    → create a rate limit rule
PUT    /limits/{ruleId}           → update a rule
DELETE /limits/{ruleId}           → delete a rule
GET    /limits/{clientId}         → get current usage for a client

# Headers returned with every response
X-RateLimit-Limit:      100
X-RateLimit-Remaining:  42
X-RateLimit-Reset:      1711000000   (Unix timestamp when limit resets)
```

---

## Key Challenges to Think About

- **Where does the rate limiter live?** As an API Gateway filter? As a sidecar? As a library in each service? Each has different trade-offs for latency and consistency.
- **Distributed counter storage:** The counter for "user X has made 42 requests this minute" must be shared across all servers. Redis with atomic `INCR` and `EXPIRE` is a common choice — but what happens when Redis is down?
- **Race condition:** Two servers check the counter simultaneously. Both see 99/100, both increment, now it's 101. How do you prevent this with atomic operations?
- **Sliding window implementation:** Redis sorted sets can store request timestamps for sliding window log. How do you expire old entries efficiently?
- **Rule hierarchy:** A user is on the Free tier (100 req/min), but endpoint `/search` has its own limit (200 req/min). Which applies? Can they be combined?
- **Soft limits / burst:** Token bucket naturally allows burst. How do you configure burst size separately from refill rate?
- **Multi-region:** A user makes requests from Europe and Asia simultaneously. How do you enforce a global limit with low latency across regions?

---

## Clarifying Questions (practice asking these in an interview)

1. Should the rate limiter be in the API gateway, or distributed in each service?
2. Is it per-user, per-IP, per-API-key, or all three?
3. Is it acceptable to let 1–2% of requests slip through over the limit (eventual consistency)?
4. Do we need different limits per endpoint, or a global per-user limit?
5. Should blocked requests be queued and retried, or immediately rejected?
6. Do we need a self-service portal for customers to view their usage?

---

## Concepts Tested

`Token Bucket / Sliding Window algorithms` · `Redis atomic operations (INCR, EXPIRE, Lua scripts)` · `API Gateway pattern` · `Distributed counters` · `Race condition prevention` · `HTTP 429 + Retry-After` · `Circuit Breaker (fallback when limiter is down)`
