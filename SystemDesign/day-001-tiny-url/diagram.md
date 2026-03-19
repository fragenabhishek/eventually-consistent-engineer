# Day 001 - Diagrams (TinyURL)

## 1. System Architecture

```
                               +----------------------+
                               |   tiny.ly Platform   |
                               +----------------------+

Client
  |
  v
+----------------+      +------------------+
| CDN + WAF      | ---> | Load Balancer    |
+----------------+      +------------------+
                               |
              +----------------+----------------+
              |                                 |
              v                                 v
     +------------------+              +------------------+
     | URL Write Service|              | Redirect Service |
     +--------+---------+              +--------+---------+
              |                                 |
      +-------+--------+                +-------+--------+
      |                |                |                |
      v                v                v                v
+-------------+  +-------------+  +-------------+  +--------------+
| KGS + Redis |  | PostgreSQL  |  | Redis Cache |  | Kafka Topic  |
| key pool    |  | primary     |  | key->long   |  | url.clicked  |
+-------------+  +------+------+  +------+------+  +------+-------+
                        |                  |                |
                        v                  |                v
                 +-------------+           |        +---------------+
                 | Read Replica| <---------+        | Analytics svc |
                 +-------------+                    +-------+-------+
                                                              |
                                                              v
                                                      +---------------+
                                                      | OLAP / reports |
                                                      +---------------+
```

## 2. Create Flow (Sequence)

```
1. Client -> Write Service: POST /api/v1/urls
2. Write Service: validate URL, alias, expiry
3. Write Service -> KGS Redis pool: POP shortKey
4. Write Service -> PostgreSQL: INSERT mapping
5. Write Service -> Redis Cache: SET shortKey -> longUrl
6. Write Service -> Client: 201 with shortUrl
```

## 3. Redirect Flow (Sequence)

```
1. Client -> Redirect Service: GET /{shortKey}
2. Redirect Service -> Redis: GET shortKey
3. If cache hit: return 302 Location immediately
4. If cache miss: Redirect Service -> DB: SELECT by shortKey
5. If active mapping found: backfill Redis and return 302
6. Redirect Service -> Kafka: publish click event (async)
7. If not found/expired/deleted: return 404 or 410
```

## 4. Background Jobs

```
KGS refill worker:
- Generates Base62 keys in batches
- Pushes into Redis pool
- Triggers alert if available keys below threshold

Expiry cleanup worker:
- Scans for expired URLs
- Marks deleted/inactive
- Removes stale cache keys
```
