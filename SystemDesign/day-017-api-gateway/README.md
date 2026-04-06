# Day 017 — API Gateway

> **Interview Goal:** Design an API Gateway that acts as the single entry point for all client-to-microservice communication.  
> **Your job today:** Read the requirements, sketch the architecture yourself, then check `design.md` (when added).

---

## The Scenario

Your company has grown from a monolith to 50 microservices. Clients (mobile apps, web, third-party partners) need to call these services. Without a gateway, every client must know the address of every service, handle auth for each, deal with different versioning schemes, and retry failed calls. An API Gateway solves this by being the single intelligent front door.

---

## Functional Requirements

1. **Route** incoming requests to the correct backend microservice based on path, method, and headers.
2. **Authentication & Authorization** — validate JWT/OAuth tokens before forwarding requests; reject unauthenticated traffic.
3. **Rate limiting** — enforce per-client, per-endpoint request limits.
4. **Request / Response transformation** — modify headers, aggregate multiple service responses into one, translate REST ↔ gRPC.
5. **Load balancing** — distribute requests across multiple instances of each microservice.
6. **Circuit breaker** — if a downstream service is failing, fail fast and return a fallback response.
7. **SSL termination** — handle HTTPS at the gateway, forward HTTP internally.
8. **API versioning** — route `/v1/orders` and `/v2/orders` to different service versions.
9. **Observability** — log every request/response, emit metrics, and propagate distributed tracing headers.
10. **Request aggregation** — one mobile app API call triggers calls to 3 services; aggregate and return one response.

---

## Non-Functional Requirements

| Property | Target |
|----------|--------|
| Latency overhead | < 10 ms added per request |
| Availability | 99.999% (gateway is in the critical path of all traffic) |
| Throughput | 1M requests/second |
| Security | TLS 1.3, no internal credentials leak to clients |
| Scalability | Horizontally scalable — add gateway nodes under load |
| Config changes | Route rule changes take effect within 30 seconds (no restart) |

---

## Capacity Estimation

| Metric | Estimate |
|--------|----------|
| Total requests/day | 50B |
| Requests/second (avg) | ~600,000 |
| Requests/second (peak) | 1M |
| Gateway nodes | 50–100 (with load balancer in front) |
| Auth token validation | < 1 ms (cached JWT public keys) |
| Average request size | 2 KB |

---

## Core Components to Design

```
Client (Web / Mobile / Third-party)
    ↓
[Load Balancer]
    ↓
[API Gateway Layer]
    ├── SSL Termination
    ├── Auth Validator (JWT / OAuth introspection)
    ├── Rate Limiter (per client / per endpoint)
    ├── Request Router (path + method → service)
    ├── Request Transformer (header injection, body mapping)
    ├── Circuit Breaker
    ├── Request Aggregator
    └── Observability (logging, metrics, tracing)
    ↓
[Service Discovery / Registry]
    ↓
Backend Microservices (Order, User, Product, Payment, ...)
```

---

## Key Challenges to Think About

- **Stateless design:** The gateway adds 10 ms per request. If it needs to hit a DB for every auth check, it adds 50–100 ms. How do you validate JWT tokens without a DB call? (Hint: public key verification + claim caching)
- **Rate limiting across gateway nodes:** If you have 50 gateway nodes, a per-node rate limiter means a client can make 50× their limit. How do you implement a distributed rate limiter efficiently?
- **Circuit breaker state:** A circuit breaker tracks failure rate per downstream service. With 50 gateway nodes, each tracks its own failure rate independently. How do you get a consistent view of service health?
- **Hot config reloading:** A new microservice is added, or a route changes. You need all 50 gateway nodes to pick up the new routing config within 30 seconds. How? (Hint: config service + polling or pub/sub)
- **Request aggregation latency:** A mobile app's "home screen" API calls 5 services in parallel. The total latency = max(individual latencies). How do you implement this in a non-blocking way? What happens if one service is slow?
- **Graceful degradation:** The product recommendation service is down. Should the gateway return an error for the whole home screen, or return the home screen with an empty recommendations section?
- **Long-lived connections:** WebSocket and Server-Sent Events connections must be proxied through the gateway. How does this differ from regular HTTP request routing?

---

## Clarifying Questions (practice asking these in an interview)

1. Is this an internal gateway (backend-to-backend) or external (client-facing)?
2. Do we need to support WebSocket connections or only HTTP?
3. Is auth fully centralised here, or do downstream services also validate?
4. What's the latency budget for the gateway — how much overhead is acceptable?
5. Do we need a developer portal for third-party API consumers?
6. Should the gateway handle A/B testing traffic splits and canary deployments?

---

## Concepts Tested

`JWT validation (asymmetric keys)` · `Distributed rate limiting (Redis)` · `Circuit Breaker pattern` · `Service Discovery (Consul, Eureka)` · `Request aggregation (BFF pattern)` · `Hot config reload` · `Sidecar vs gateway patterns` · `Observability (tracing headers, metrics)`
