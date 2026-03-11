
=== WHAT TO EXPECT IN ROUND 1 ===
• Core Java (11/17): concurrency, collections, Streams, JVM memory/GC.
• Spring & Spring Boot: REST, DI, configuration, JPA/Hibernate, transactions, security basics, testing.
• Microservices & Messaging: REST vs gRPC, resiliency, Kafka fundamentals.
• Databases: SQL design & optimization, NoSQL use-cases (Mongo/Redis), caching.
• Quality & Performance: testing strategy, profiling, caching, throughput/latency.
• CI/CD & DevOps: Git, Maven/Gradle, pipelines, Docker (K8s basics optional).
• Security & Infosec posture: OAuth2/OIDC/JWT, token hygiene, secret scanning, patch SLAs, EOL.
• Observability: logging/metrics/tracing.
• Resume deep-dive: Kafka workloads, modernization wins, cloud deployments, automation/GenAI.

=== QUESTION LIST ===

-- Core Java (11/17) --
1) Thread vs Runnable vs Callable – differences and use-cases?
2) ExecutorService vs ForkJoinPool; where to use CompletableFuture?
3) Race condition vs deadlock; patterns to avoid them?
4) HashMap internals; impact of equals/hashCode?
5) Streams vs loops; pitfalls of parallel streams?
6) JVM memory model & GC basics; finding leaks?
7) Benefits of immutability; how to implement?
8) Newer Java features you used (var/records/switch) and why?

-- Spring & Spring Boot --
9) How DI works; bean scopes; @ConfigurationProperties vs @Value?
10) Build a REST controller with validation + global exception handling?
11) Spring Data JPA – paging/sorting; N+1 problem and fixes?
12) Transactions – propagation/isolation; optimistic vs pessimistic locking?
13) Caching with @Cacheable (Redis) and invalidation strategies?
14) Spring Security – filter chain, method vs URL security, password storage?
15) Spring Boot Actuator – health/metrics; securing endpoints?
16) Unit vs integration tests; when to use Testcontainers?

-- Microservices & Messaging --
17) REST vs gRPC – trade-offs?
18) Designing idempotent APIs; retry/timeout/circuit breaker (Resilience4j)?
19) Kafka basics – topics, partitions, consumer groups, offsets?
20) Exactly-once vs at-least-once; dedup (outbox, idempotency keys)?
21) Event schema evolution (Avro/Schema Registry) & compatibility?
22) Saga – choreography vs orchestration; eventual consistency?

-- Databases (SQL & NoSQL) --
23) Choosing SQL vs NoSQL (Mongo/Redis) – when and why?
24) Indexing strategies; when indexes hurt writes?
25) Query optimization – EXPLAIN; diagnosing slow queries?
26) ACID & isolation levels; phantom reads?
27) Redis cache design – TTL, eviction, hot keys?
28) Modeling in Mongo – embed vs reference; migrations?

-- Testing & Quality --
29) Test pyramid; where do contract tests fit?
30) Example of a flaky test you fixed (JUnit/Mockito tips)?
31) Integration tests across DB and Kafka?

-- Performance & Reliability --
32) Finding/fixing a latency bottleneck – tools and approach?
33) Thread-pool sizing for blocking vs non-blocking workloads?
34) When caching hurts consistency; mitigation?

-- CI/CD, Git, Containers, Cloud --
35) Preferred Git branching strategy; merge vs rebase?
36) CI pipeline stages you set up (build, test, SAST, artifact, deploy)?
37) Maven vs Gradle – trade-offs; dependency convergence?
38) Docker image hardening – multi-stage, non-root, minimal base?
39) K8s basics – Deployment vs StatefulSet; Service; probes?
40) Cloud primitives used on AWS/Azure and why?

-- Security & Infosec (aligned to project) --
41) OAuth2 flows implemented; where OIDC adds value; JWT structure & best practices?
42) Enforcing token expiration and rotating credentials (≤30 days) in GitHub/CI?
43) Detecting/removing hard-coded secrets; tools and process?
44) What does 'revoke stale API keys' mean; how to identify stale?
45) Handling outside collaborators in GitHub – review/approval/removal?
46) Vulnerability management cadence; prioritization and patching to SLAs?
47) Ensuring no EOL software in stack; tracking lifecycles?
48) Keeping 3rd-party libraries current (Dependabot/SBOM); handling risky upgrades?

-- Observability --
49) Structured logging & correlation IDs (ELK)?
50) Backend SLOs/metrics (latency/throughput/errors) and Prometheus/Grafana?
51) Distributed tracing – when it helped debug prod?

-- Collaboration & Practices --
52) What do you look for in a code review?
53) Working model with DevOps during deploy/rollback?
54) Slicing a feature for Agile delivery under ambiguity?

-- Resume Deep-Dive --
55) 30% performance improvement – how measured; key changes?
56) Kafka setup – partitions, group strategy, retries/DLQ, volumes?
57) CI/CD you implemented – stages, quality/security gates, versioning?
58) Docker/K8s – image strategy; requests/limits; rollout (blue/green/canary)?
59) Automation (Shell/PLSQL) – what and how 80% effort reduction?
60) Cloud exposure (AWS/Azure) – deployments, secrets, cost/reliability wins?
61) GenAI/LLM integrations – API design, safeguards, measurable impact?

=== SUGGESTED SHORT ANSWERS (Cheat Sheet) ===
1) Thread executes code; Runnable supplies run(); Callable returns value/throws checked exceptions; use Callable with futures for results/timeouts.
2) ExecutorService = general thread pools; ForkJoinPool = work-stealing for divide-and-conquer; CompletableFuture = async composition, non-blocking callbacks.
3) Race: unsynchronized shared state; Deadlock: cyclic lock waiting; fixes: immutability, confinement, synchronized/locks ordering, timeouts, tryLock.
4) HashMap uses hash -> bucket -> tree/bin list; poor hash/equals causes collisions; must keep equals/hashCode consistent for key lookup.
5) Streams concise & parallelizable; pitfalls: stateful lambdas, side effects, unordered parallel ops; prefer simple terminals and collectors.
6) Heap (young/old), stack, metaspace; GC types (G1/ZGC); detect leaks via heap dumps, MAT, profilers; watch for static refs/caches.
7) Immutable objects are thread-safe, cache-friendly; implement with final fields, no setters, defensive copies.
8) var reduces verbosity; records = immutable data carriers; switch patterns simplify branching; justify with readability and safety.
9) Spring DI via container; scopes: singleton/prototype/request/session; @ConfigurationProperties binds hierarchical config, @Value for single values.
10) Use @RestController, DTO + Bean Validation (@Valid); global errors via @ControllerAdvice/@ExceptionHandler; return ProblemDetails-style payloads.
11) Use PagingAndSortingRepository/Pageable; fix N+1 with fetch joins, EntityGraph, batch size, or projection.
12) Propagation (REQUIRED, REQUIRES_NEW); Isolation (read committed/serializable); optimistic (version field) vs pessimistic (SELECT FOR UPDATE).
13) @Cacheable/@CacheEvict; choose Redis; set TTL, version keys, and invalidate on writes; beware cache stampede/hot keys.
14) Delegating filter chain; prefer bcrypt/argon2; method security via @PreAuthorize; resource server for JWT validation.
15) Actuator /health,/metrics,/info; secure with auth, management port, expose only needed endpoints.
16) Unit = isolate via mocks; Integration = real DB/brokers (Testcontainers); use @SpringBootTest for slice tests when needed.
17) REST = human/web friendly; gRPC = binary/proto, faster/typed, needs HTTP/2; choose by client ecosystem and latency needs.
18) Idempotency via deterministic ids; retries with backoff; timeouts; circuit breaker/bulkhead via Resilience4j.
19) Kafka: topics partitioned for parallelism; consumer groups share partitions; offsets track progress; rebalance on membership change.
20) Exactly-once via idempotent producer + transactional consumer or outbox; commonly at-least-once with dedupe keys and idempotent handlers.
21) Use Avro + Schema Registry; prefer backward-compatible changes (add optional fields).
22) Saga: choreography (events) vs orchestration (central coordinator); accept eventual consistency and compensating actions.
23) SQL for relational integrity/joins; Mongo for flexible docs; Redis for ephemeral cache or counters; justify by access patterns.
24) Index selective columns; avoid over-indexing on heavy writes; composite indexes follow leftmost rule.
25) Use EXPLAIN/EXPLAIN ANALYZE; fix with indexes, rewrite joins, limit result sets, denormalize when justified.
26) ACID; isolation controls phenomena (dirty/non-repeatable/phantom); use proper isolation per use-case.
27) Set TTL; choose eviction (LRU/LFU); shard hot keys; use cache-aside pattern.
28) Embed for locality; reference for reuse and growth; plan migrations with versioned scripts/backfills.
29) Pyramid: more unit than integration than E2E; contract tests validate inter-service APIs.
30) Stabilize by removing sleeps, using awaitility, fixing shared state, adding deterministic seams.
31) Use Testcontainers for DB/Kafka; run consumer-producer tests with embedded brokers or containers.
32) Reproduce with load tests; profile (Flight Recorder, async-profiler); fix hotspots (indexes, caching, batching, pool tuning).
33) Blocking: more threads (size ~ cores * (1 + wait/compute)); Non-blocking/reactive: small pools sized to cores.
34) Stale reads and write-loss; mitigate with cache invalidation, short TTLs, write-through or event-driven updates.
35) Trunk-based or GitFlow; rebase for clean history, merge for auditability; enforce PR checks.
36) Stages: build, unit, SAST, integration, artifact, deploy; gates on quality/coverage/security.
37) Maven: convention/stability; Gradle: performance/flexibility; manage convergence via BOMs and dependencyManagement.
38) Multi-stage builds; run as non-root; minimal base (distroless/alpine); pin digests; scan images.
39) Deployment = stateless; StatefulSet = stable IDs/storage; use readiness/liveness probes; ConfigMap/Secret for config.
40) Choose EC2/Lambda/RDS/S3 (or Azure equivalents) based on workload; manage IAM/roles and secret managers.
41) OAuth2 authZ; OIDC adds identity; JWT = header.payload.sig; best practices: short TTL, rotate keys (kid), audience/scope checks.
42) Org policy: fine-grained PATs with ≤30d expiry; rotate via automation; revoke unused via audit logs.
43) Scan with GitHub secret scanning/Gitleaks/TruffleHog; remove secrets, rotate keys, move to vault/env.
44) Stale = unused N days; detect from access logs; revoke and notify owner; document rotation.
45) Periodic review of outside collaborators; least privilege; auto-expire access; audit & remove if unapproved.
46) Continuous scans (SCA/SAST/VA); prioritize Critical/High; patch within SLA (e.g., 7/30 days); track exceptions.
47) Maintain inventory/SBOM; check vendor lifecycle; plan upgrades before EOL; isolate legacy where needed.
48) Use Dependabot/Snyk; maintain SBOM; test upgrades in staging; pin versions and monitor CVEs.
49) JSON structured logs with requestId/correlationId; centralize in ELK; log at appropriate levels.
50) Track p95 latency, RPS, error rate, saturation; Prometheus scrapes; Grafana dashboards with alerts.
51) Use OpenTelemetry; traces reveal cross-service latency; fix by narrowing slow spans.
52) Reviews: correctness, security, performance, readability; insist on small PRs and tests.
53) Pre-deploy checklists, canaries, feature flags; rollback via versioned artifacts.
54) Slice by vertical increments (API + DB + UI); define acceptance criteria; iterate with feedback.
55) Baseline with load tests; profile; wins from batching, connection pooling, caching, efficient queries; measure % improvement.
56) Partitions ≈ throughput/consumer count; assign consumer groups; retries with backoff; DLQ for poison messages; record volumes.
57) Pipeline includes unit/integration, quality gates, security scans; version artifacts (SemVer/commit SHA).
58) Images slim & non-root; set CPU/memory requests/limits; rollouts via blue/green or canary; monitor and rollback if needed.
59) Identify repetitive steps; script with Shell/SQL; schedule; log and report; measure effort reduction.
60) Deploy to AWS/Azure using IaC; store secrets in vault; optimize costs (right-size, autoscale, spot).
61) Design REST wrapper; add rate limits, input filtering, and PII redaction; log prompts/metrics; report impact.
