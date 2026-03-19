# Day 001 — Diagrams

## System Architecture

```
                          ┌──────────────────────────────────────────┐
                          │              eventually-consistent.ly     │
                          └──────────────────────────────────────────┘

  ┌────────┐     POST /shorten       ┌───────────────┐     LPOP key     ┌──────────┐
  │ Client │ ───────────────────────►│  Write Service│ ────────────────►│  KGS     │
  └────────┘                         └───────┬───────┘                  │  Redis   │
                                             │ INSERT                   └──────────┘
                                             ▼
                                      ┌───────────┐
                                      │ PostgreSQL│
                                      └───────────┘

  ┌────────┐     GET /aB3kX          ┌───────────────┐   HIT  ┌──────────────┐
  │ Client │ ───────────────────────►│Redirect Service│───────►│ Redis Cache  │
  └────────┘                         └───────┬────────┘        └──────────────┘
               302 Location: longUrl         │ MISS
               ◄────────────────────         ▼
                                      ┌───────────┐
                                      │ PostgreSQL│
                                      └─────┬─────┘
                                            │ async event
                                            ▼
                                      ┌───────────┐   consume  ┌──────────────────┐
                                      │   Kafka   │ ──────────►│ Analytics Service│
                                      └───────────┘            └──────────────────┘
```

## Data Flow — Write

```
1. POST /shorten { longUrl }
2. Validate URL
3. LPOP shortKey from Redis KGS pool
4. INSERT (shortKey, longUrl, userId, createdAt) → PostgreSQL
5. SET cache[shortKey] = longUrl (TTL)
6. Return { shortUrl: "https://short.ly/aB3kX" }
```

## Data Flow — Read

```
1. GET /aB3kX
2. GET cache[aB3kX] → hit? → 302 redirect → done
3. miss → SELECT longUrl FROM urls WHERE short_key = 'aB3kX'
4. SET cache[aB3kX] = longUrl
5. Publish { shortKey, timestamp, ip, userAgent } → Kafka
6. 302 redirect
```
