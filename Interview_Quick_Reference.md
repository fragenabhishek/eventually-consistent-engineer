# Interview Quick Reference
### The Night-Before Cheat Sheet for Java Backend + System Design Interviews

> **How to use:** Scan each section and say the answer aloud. If you hesitate, re-read that section in the full story guides. This is your final review, not your first read.

---

## Reading Map

| Topic | Section |
|-------|---------|
| Java Core | [Java Fundamentals](#java-fundamentals) |
| Java 8+ | [Modern Java](#modern-java) |
| Multithreading | [Concurrency](#concurrency) |
| Spring Boot | [Spring & Spring Boot](#spring--spring-boot) |
| Spring Debugging | [Spring Boot Debugging Scenarios](#spring-boot-debugging-scenarios) |
| Microservices | [Microservices](#microservices) |
| Kafka | [Kafka & Messaging](#kafka--messaging) |
| Databases | [Databases](#databases) |
| Design Patterns | [Design Patterns](#design-patterns-quick-pick) |
| System Design | [System Design Concepts](#system-design-concepts) |
| Behavioral | [Behavioral Questions](#behavioral-questions) |

---

# Java Fundamentals

**Q: Difference between `==` and `.equals()`?**  
`==` compares references (memory address). `.equals()` compares logical content. For `String`, `Integer` cache, etc., always use `.equals()`.

**Q: Why is `String` immutable in Java?**  
Security (no tampering with file paths/SQL), thread safety (safe to share), and String pool reuse (same literal → same object in heap).

**Q: `HashMap` vs `ConcurrentHashMap` vs `Hashtable`?**

| | HashMap | ConcurrentHashMap | Hashtable |
|--|---------|-------------------|-----------|
| Thread-safe | ❌ | ✅ (segment locks) | ✅ (full lock) |
| Null keys | ✅ (1 null key) | ❌ | ❌ |
| Performance | Fastest (single thread) | Fast (concurrent) | Slowest |
| Use when | Single thread | Multi-thread | Legacy — avoid |

**Q: How does `HashMap` work internally?**  
Array of buckets (Node[]). `hash(key)` → bucket index. Collision → linked list (or red-black tree if > 8 nodes). Resize when load factor > 0.75 (doubles capacity, rehash all entries).

**Q: What is the contract between `equals()` and `hashCode()`?**  
If `a.equals(b)` is true → `a.hashCode() == b.hashCode()` must be true. Violating this breaks HashMap (objects with same logical value go to different buckets and are never found).

**Q: `ArrayList` vs `LinkedList`?**  
`ArrayList` — O(1) random access, O(n) insert/delete in middle. `LinkedList` — O(n) access, O(1) insert/delete at head/tail. Use `ArrayList` almost always; `LinkedList` only for queue/deque use cases.

**Q: What is the difference between `Comparable` and `Comparator`?**  
`Comparable` — natural ordering, implemented by the class itself (`compareTo`). `Comparator` — external ordering, passed to sort methods (`compare`). Use `Comparator` when you need multiple sort orders or can't modify the class.

**Q: `final`, `finally`, `finalize` — difference?**  
`final` — variable can't be reassigned, method can't be overridden, class can't be subclassed. `finally` — always runs in try-catch (except `System.exit()`). `finalize` — called by GC before reclaiming object (deprecated in Java 9, avoid).

**Q: What is a memory leak in Java? Give an example.**  
Object is no longer needed but still referenced, preventing GC. Classic examples: `static` collections growing unbounded, event listeners never removed, `ThreadLocal` not cleaned after thread pool reuse.

---

# Modern Java

**Q: What are the main features of Java 8?**  
Lambda expressions, Streams API, `Optional`, `default` interface methods, `java.time` (LocalDate, etc.), `CompletableFuture`, functional interfaces (`Function`, `Predicate`, `Consumer`, `Supplier`).

**Q: What is a Stream? How is it different from a Collection?**  
A Stream is a pipeline for processing data — lazy (elements computed on demand), single-use (can't be reused), and doesn't store data. A Collection stores data eagerly and can be iterated multiple times.

**Q: Explain `map` vs `flatMap`.**  
`map(f)` transforms each element: `Stream<T>` → `Stream<R>`. `flatMap(f)` transforms and flattens: `Stream<Stream<R>>` → `Stream<R>`. Use `flatMap` when each element maps to a collection.

```java
// map: each name → its length
List<Integer> lengths = names.stream().map(String::length).collect(toList());

// flatMap: each sentence → its words
List<String> words = sentences.stream().flatMap(s -> Arrays.stream(s.split(" "))).collect(toList());
```

**Q: What is `Optional`? Why use it?**  
A container that may or may not contain a non-null value. Eliminates `NullPointerException` by forcing callers to handle the absent case. Use `.orElse()`, `.orElseThrow()`, `.ifPresent()`.

**Q: What is a functional interface?**  
An interface with exactly one abstract method (SAM). Annotated `@FunctionalInterface`. Can be used as a lambda target. Examples: `Runnable`, `Callable`, `Comparator`, `Function<T,R>`, `Predicate<T>`.

**Q: `CompletableFuture` — what is it and when do you use it?**  
Async computation that can be chained. Use when you need non-blocking I/O, parallel tasks, or combining results from multiple async operations.

```java
CompletableFuture.supplyAsync(() -> fetchUser(id))
    .thenApply(user -> fetchOrders(user))
    .thenAccept(orders -> display(orders))
    .exceptionally(ex -> { log(ex); return null; });
```

**Q: What are the new features since Java 11/17?**  
Records (Java 16), Sealed classes (Java 17), Text blocks (Java 15), `var` type inference (Java 10), Pattern matching for `instanceof` (Java 16), Switch expressions (Java 14).

---

# Concurrency

**Q: `Thread` vs `Runnable` vs `Callable`?**  
`Runnable` — no return value, no checked exception. `Callable<T>` — returns a value, can throw checked exception. `Thread` — is-a Runnable, avoid extending it. Use `ExecutorService` to submit `Callable`.

**Q: What is the `synchronized` keyword?**  
Makes a method or block mutually exclusive. Only one thread can hold the monitor lock at a time. Ensures visibility and atomicity but is slow due to lock contention.

**Q: `volatile` vs `synchronized`?**  
`volatile` — ensures visibility (all threads see latest write) but not atomicity (read-modify-write like `i++` is not safe). `synchronized` — both visibility and atomicity. Use `volatile` for simple flags, `synchronized` (or `AtomicXxx`) for compound operations.

**Q: `ReentrantLock` vs `synchronized`?**  
`ReentrantLock` offers: tryLock (non-blocking attempt), lockInterruptibly, fairness option, multiple Condition objects. More power but more responsibility (must `unlock()` in finally). Use `synchronized` by default; `ReentrantLock` when you need advanced control.

**Q: What is a deadlock? How do you prevent it?**  
Two threads each hold a lock the other needs → both wait forever.  
Prevention: always acquire locks in the same order; use `tryLock` with timeout; minimize lock scope.

**Q: `ExecutorService` — what is it?**  
Thread pool manager. Avoids creating/destroying threads per task. Use `Executors.newFixedThreadPool(n)` for CPU tasks, `newCachedThreadPool()` for short I/O tasks, `newScheduledThreadPool()` for periodic tasks.

**Q: What is `CountDownLatch` vs `CyclicBarrier`?**  
`CountDownLatch` — one or more threads wait until count reaches 0 (not reusable). `CyclicBarrier` — N threads all wait for each other at a barrier point (reusable). Latch: "wait until all tasks done". Barrier: "all start next phase together".

**Q: What is the `happens-before` guarantee?**  
A JMM rule ensuring that actions before a lock release are visible to any subsequent lock acquire on the same monitor. Also applies to `volatile` writes, `Thread.start()`, `Thread.join()`.

---

# Spring & Spring Boot

**Q: What is Dependency Injection (DI)? Why use it?**  
Objects receive their dependencies from outside rather than creating them. DI makes classes testable (inject mocks), loosely coupled (depend on interfaces), and configurable (swap implementations without code change).

**Q: `@Component` vs `@Service` vs `@Repository` vs `@Controller`?**  
All are Spring-managed beans (`@Component` is the root annotation). `@Service` marks business logic, `@Repository` marks data access (also wraps DB exceptions to Spring exceptions), `@Controller` marks web layer. Use semantic annotations — they signal intent and enable targeted features (e.g., `@Repository` gets persistence exception translation).

**Q: What is `@Autowired`? What are the types of injection?**  
Marks a field, constructor, or setter for automatic dependency injection.  
- **Constructor injection** — preferred; makes dependencies explicit and supports immutability.  
- **Setter injection** — for optional dependencies.  
- **Field injection** — convenient but hides dependencies; hard to test without Spring.

**Q: What is the Spring Bean lifecycle?**  
Instantiate → Populate properties → `@PostConstruct` → Ready (in use) → `@PreDestroy` → Destroy. Or implement `InitializingBean.afterPropertiesSet()` / `DisposableBean.destroy()`.

**Q: How does `@Transactional` work?**  
Spring creates a proxy around the method. On method entry, it starts (or joins) a transaction. On success, it commits. On unchecked exception, it rolls back. Pitfall: `@Transactional` on a `private` method is silently ignored (proxy can't intercept).

**Q: `@Transactional(propagation=REQUIRED)` vs `REQUIRES_NEW`?**  
`REQUIRED` (default) — join existing transaction if one exists; start new if none. `REQUIRES_NEW` — always start a new transaction, suspend the outer one. Use `REQUIRES_NEW` for audit logging that must commit even if outer transaction rolls back.

**Q: What is Spring Boot auto-configuration?**  
`@EnableAutoConfiguration` scans classpath for libraries and automatically configures beans. E.g., if `H2` is on classpath, it configures a DataSource. Override with `application.properties` or explicit `@Bean`. `@ConditionalOnClass`, `@ConditionalOnMissingBean` control when beans are created.

**Q: How do you handle exceptions in a REST API?**  
`@ControllerAdvice` + `@ExceptionHandler` — global handler for all controllers. Return a structured error response (status code, error code, message). Example:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }
}
```

**Q: How does Spring Security work?**  
Filter chain intercepts every request. `UsernamePasswordAuthenticationFilter` extracts credentials → `AuthenticationManager` validates → `SecurityContextHolder` stores the authenticated principal. For JWT: custom filter extracts token, validates signature, sets context. `@PreAuthorize("hasRole('ADMIN')")` secures individual methods.

**Q: What is the difference between `@PathVariable` and `@RequestParam`?**  
`@PathVariable` — extracted from the URL path: `GET /users/{id}`. `@RequestParam` — from query string: `GET /users?role=admin`. Use `@PathVariable` for resource identifiers, `@RequestParam` for optional filters.

**Q: How do you make a Spring Boot app production-ready?**  
Actuator (health, metrics, info endpoints), structured logging (JSON + tracing IDs), external configuration (env vars / Vault), graceful shutdown, readiness + liveness probes, connection pool tuning (HikariCP), circuit breakers.

---

# Spring Boot Debugging Scenarios

**1) Your Spring Boot application starts returning 500 errors after deployment. How will you debug it?**  
Start from evidence: check logs, Actuator health, and deployment diff (`env`, config, image tag). Reproduce one failing API with correlation ID.  
**Example:** 500 appears only in prod after deploy; logs show `NullPointerException` in mapper because `FEATURE_X_ENABLED=true` in prod activated code path not tested locally. Fix by null-safe mapping + integration test under prod-like profile.

**2) You face `BeanCreationException` during application startup. What could be wrong?**  
Common causes: missing dependency bean, invalid `@Value` property, constructor cycle, or exception inside `@PostConstruct`.  
**Example:** `@Value("${jwt.secret}")` missing in prod config causes startup failure. Add required property, fail-fast validation via `@ConfigurationProperties` + `@Validated`.

**3) Your service throws `NoSuchBeanDefinitionException` in production but works locally. How will you fix it?**  
Check profile-conditional beans (`@Profile`, `@ConditionalOnProperty`) and package scanning boundaries.  
**Example:** local runs with `dev` profile where mock bean exists; prod uses `prod` and bean is absent. Create prod implementation bean and add profile-specific test.

**4) You get `CircularDependencyException` between beans. How will you resolve it?**  
Break bidirectional dependency by extracting an interface, introducing orchestration service, or event-based interaction.  
**Example:** `OrderService -> PaymentService -> OrderService`. Move shared logic to `OrderPaymentCoordinator`; both services depend on coordinator-facing abstractions, not each other.

**5) Your API throws `HttpMessageNotReadableException` for valid requests. How will you debug it?**  
Validate request `Content-Type`, DTO field types, Jackson config, and date/enum formats.  
**Example:** client sends `"amount":"100"` while DTO expects `BigDecimal` but custom deserializer rejects string. Accept string format or enforce numeric payload in API contract.

**6) You observe `LazyInitializationException` in production. How will you fix it?**  
Do not rely on Open Session in View. Fetch needed associations inside transaction (`JOIN FETCH`, entity graph) and map to DTO.  
**Example:** controller accesses `order.getItems()` after transaction closed. Fix in service: `findOrderWithItems(orderId)` and return DTO.

**7) Your application fails due to incorrect configuration properties. How will you manage configs properly?**  
Use typed `@ConfigurationProperties`, validation, environment-specific files, and secrets manager.  
**Example:** typo `payment.timout` silently ignored. Move to `PaymentProperties` class with `@Validated` so startup fails if `timeout` is missing/invalid.

**8) You get `DataIntegrityViolationException` while saving data. What could be the issue?**  
Usually unique key, foreign key, not-null, or length constraint violation. Inspect root SQL exception and failed values.  
**Example:** duplicate email insert violates unique index. Handle with business check + catch-and-map to 409 Conflict.

**9) Your service throws `TransactionRequiredException` during updates. How will you handle transactions?**  
Ensure write methods run inside `@Transactional`, and avoid self-invocation bypassing proxy.  
**Example:** `updateStatus()` called from same class private method without proxy interception. Move to separate transactional service or call through bean proxy.

**10) Your API response time suddenly increases after enabling logging. How will you optimize it?**  
Reduce sync/verbose logging, switch to async appenders, avoid logging large payloads, and sample debug logs.  
**Example:** full request/response body logging adds 120ms p95. Log only metadata (`path`, `status`, `latency`, `traceId`) and sample 1% payload logs.

**11) You see memory leaks in your Spring Boot application. How will you detect and fix them?**  
Capture heap dump, analyze dominator tree (MAT/YourKit), track object growth with GC logs and metrics.  
**Example:** static `Map<String, SessionData>` never evicts. Replace with Caffeine cache (TTL + max size) and close resources/listeners.

**12) Your application fails to connect to the database intermittently. How will you debug it?**  
Check DB/network errors, connection pool settings, max connections, and timeout mismatches.  
**Example:** Hikari `maxLifetime` > DB idle timeout, causing stale sockets. Set `maxLifetime` lower than DB timeout and tune `connectionTimeout`.

**13) You face timeout issues while calling another service. How will you handle it?**  
Configure connect/read timeouts, retries with backoff+jitter, circuit breaker, and fallback path.  
**Example:** downstream p99 spikes to 4s; your timeout is 10s causing thread pile-up. Set 1s timeout + 2 retries + fallback cached response.

**14) Your application fails due to incorrect environment configuration. How will you manage profiles?**  
Use strict profile strategy (`dev`, `staging`, `prod`), immutable config per environment, and startup validation.  
**Example:** prod accidentally starts with `dev` profile. Enforce profile via deployment manifest and fail startup if forbidden profile in prod.

**15) You observe duplicate requests being processed. How will you ensure idempotency?**  
Use idempotency key (header/body), unique constraint, and request state store.  
**Example:** payment API retried by client creates two charges. Store `(idempotencyKey, response)` and return original response for retries.

**16) Your service crashes due to unhandled exceptions. How will you implement global exception handling?**  
Add `@ControllerAdvice` for API errors, map known exceptions to stable codes, and keep fallback 500 handler.  
**Example:** `IllegalArgumentException` bubbles up and crashes request path. Map to 400 with error code `VALIDATION_ERROR`.

**17) You see high thread usage in your application. How will you optimize it?**  
Inspect thread dump, identify blocked/waiting threads, tune pools, and remove blocking calls from request threads.  
**Example:** unbounded async executor creates thousands of threads. Replace with bounded `ThreadPoolTaskExecutor` and queue limits.

**18) Your scheduled job runs multiple times unexpectedly. How will you fix it?**  
In multi-instance deployment, each pod runs `@Scheduled` unless coordinated. Use distributed lock or single-job runner.  
**Example:** 3 pods execute nightly settlement 3 times. Add ShedLock with DB lock table so only one instance runs at a time.

**19) Your application fails during deployment due to dependency conflicts. How will you resolve it?**  
Check dependency tree, align versions via BOM, and exclude conflicting transitive dependencies.  
**Example:** `NoSuchMethodError` from mixed Jackson versions. Import Spring Boot BOM and remove explicit old Jackson dependency.

**20) You observe inconsistent data due to concurrent transactions. How will you fix it?**  
Choose proper isolation, optimistic/pessimistic locking, and retry on conflict where safe.  
**Example:** lost update on inventory decrement. Add version column (`@Version`) and retry conflict path.

**21) Your logs are insufficient to debug issues. How will you improve logging?**  
Adopt structured JSON logs, include `traceId/spanId/userId`, log at boundaries, and standardize error fields.  
**Example:** add MDC filter that injects `traceId` into every log line and response header, enabling request-level tracing.

**22) Your API Gateway returns errors due to downstream failures. How will you handle it?**  
Implement per-route timeout, circuit breaker, fallback responses, and health-aware routing.  
**Example:** user-profile service down returns 500 for all pages. Gateway serves partial response with cached profile summary and warning flag.

**23) Your application becomes unresponsive under load. How will you debug it?**  
Correlate CPU, GC, thread dumps, DB pool saturation, and slow query logs under load test.  
**Example:** p99 latency explodes because DB pool exhausted (50/50 active, long SQL). Add indexes, reduce transaction time, and tune pool/concurrency limits.

**24) You need to deploy a new version without downtime. How will you achieve it?**  
Use rolling or blue-green deployment with readiness probes, backward-compatible DB changes, and canary verification.  
**Example:** deploy `v2` to 10% traffic, compare error rate/latency, then ramp to 100%; keep rollback switch ready.

**25) Your application behaves differently in production vs local. How will you approach debugging?**  
Diff runtime conditions: profile, env vars, JVM flags, external dependencies, data shape, and traffic patterns. Reproduce with prod-like setup.  
**Example:** local uses in-memory DB and no cache; prod uses Postgres + Redis causing stale-read race. Reproduce in staging with same topology and fix cache invalidation.

---

# Microservices

**Q: Monolith vs Microservices — when to choose which?**  
Monolith: small team, early product, simpler deployment. Microservices: independent scaling needs, separate release cycles, multiple teams, clear domain boundaries. Don't split prematurely — "Start monolith, extract microservices at pain points."

**Q: How do microservices communicate?**  
- **Sync:** REST (HTTP/JSON) or gRPC (HTTP/2, binary, strongly typed). Use when caller needs immediate response.  
- **Async:** Message broker (Kafka, RabbitMQ, SQS). Use for decoupled processing, resilience, fan-out.

**Q: What is the Circuit Breaker pattern?**  
Wraps calls to a downstream service. Tracks failure rate. When it exceeds a threshold, the circuit "opens" — all calls immediately fail-fast (no waiting for timeout). After a cooldown, allows a test request through. Implements: Resilience4j in Java/Spring.

**Q: What is the Saga pattern? When do you need it?**  
Manages distributed transactions across microservices without a 2PC lock. Each service does its local transaction and publishes an event. If a step fails, compensating transactions undo previous steps. Use whenever an operation spans multiple services and must be atomic.

**Q: Service Discovery — what is it?**  
In a dynamic environment, services spin up/down with different IPs. A service registry (Consul, Eureka) tracks live instances. Client-side discovery: caller queries registry and picks an instance. Server-side: load balancer queries registry.

**Q: What is the API Gateway pattern?**  
Single entry point for all clients. Handles auth, rate limiting, routing, SSL termination, and response aggregation. Clients talk to one address; the gateway fans out to relevant microservices.

**Q: What is the Outbox Pattern?**  
Solves the dual-write problem: writing to your DB and publishing an event atomically. Write event to an `outbox` table in the same DB transaction. A separate publisher reads the outbox and publishes to Kafka. Guarantees the event is published if and only if the DB write succeeded.

**Q: How do you handle failures in a microservice call chain?**  
Retry with exponential backoff + jitter (avoid retry storms), Circuit Breaker (fail fast), Timeout (don't wait forever), Fallback (return cached/default response), Bulkhead (limit concurrent calls to one downstream service).

---

# Kafka & Messaging

**Q: What is Kafka? How is it different from RabbitMQ?**

| | Kafka | RabbitMQ |
|--|-------|---------|
| Model | Log-based pub/sub | Queue-based (push) |
| Retention | Configurable (days/weeks) | Consumed = deleted |
| Consumers | Any consumer can re-read | One consumer per message |
| Throughput | Millions/sec | Thousands/sec |
| Use for | Event streaming, audit log | Task queues, RPC |

**Q: What is a Kafka topic, partition, and offset?**  
**Topic** — named stream of messages. **Partition** — ordered, immutable log; a topic has N partitions for parallelism. **Offset** — position of a message within a partition. Consumers track their own offset — they control what they've read.

**Q: How does Kafka guarantee ordering?**  
Ordering is guaranteed **within a partition**, not across partitions. To order all messages for a user, use `userId` as the partition key — all messages for that user land in the same partition.

**Q: What are consumer groups?**  
A group of consumers sharing consumption of a topic. Each partition is consumed by exactly one consumer in the group (parallel processing). All groups independently consume all partitions (fan-out). Add consumers in a group → scales throughput. Add consumer groups → fan-out to more services.

**Q: What is at-least-once vs exactly-once delivery?**  
- **At-most-once:** Commit offset before processing → message can be lost if consumer crashes.  
- **At-least-once:** Commit offset after processing → message can be reprocessed on crash (must handle duplicates with idempotency).  
- **Exactly-once:** Kafka Transactions (producer + consumer in one atomic operation). Complex, but possible.

**Q: What is the Outbox + Kafka pattern?**  
See [Outbox Pattern](#microservices) above. Database transaction writes event to outbox table → Kafka Connect / Debezium reads via CDC (change data capture) → publishes to Kafka.

---

# Databases

**Q: SQL vs NoSQL — when to use which?**

| | SQL | NoSQL |
|--|-----|-------|
| Schema | Fixed (enforced) | Flexible (schema-less) |
| ACID | Full support | Varies (usually eventual) |
| Joins | Native | Denormalize instead |
| Scale | Vertical (+ read replicas) | Horizontal (native sharding) |
| Use for | Transactions, reporting, complex queries | Massive scale, flexible schema, simple lookups |

**Q: What is database indexing?**  
Data structure (usually B-tree) that speeds up queries on indexed columns. Trades write speed for read speed. Without index: full table scan O(n). With index: O(log n). Add indexes on columns used in `WHERE`, `JOIN`, `ORDER BY`. Too many indexes slow down writes.

**Q: What is the N+1 query problem?**  
Fetching N orders, then fetching customer for each → 1 + N DB queries. Fix: use `JOIN` (one query) or batch loading (`IN` clause). In JPA: `@OneToMany(fetch = EAGER)` can cause N+1; use `JOIN FETCH` in JPQL.

**Q: What is database sharding?**  
Splitting data across multiple DB instances (shards) based on a shard key (e.g., userId mod N). Enables horizontal scaling. Challenges: cross-shard joins are hard, resharding is expensive, hot shards if key is skewed.

**Q: What is the CAP Theorem?**  
In a distributed system, you can only guarantee 2 of 3: **Consistency** (every read gets latest write), **Availability** (every request gets a response), **Partition Tolerance** (works despite network splits). Since network partitions are a reality, you choose CP (Zookeeper, HBase) or AP (Cassandra, DynamoDB).

**Q: What is a database transaction and ACID?**  
**Atomicity** — all or nothing. **Consistency** — constraints are never violated. **Isolation** — concurrent transactions don't interfere. **Durability** — committed data survives crashes.

**Q: What are the isolation levels?**

| Level | Dirty Read | Non-repeatable Read | Phantom Read |
|-------|-----------|--------------------|----|
| Read Uncommitted | ✅ possible | ✅ | ✅ |
| Read Committed | ❌ | ✅ possible | ✅ |
| Repeatable Read | ❌ | ❌ | ✅ possible |
| Serializable | ❌ | ❌ | ❌ |

Default in MySQL (InnoDB): Repeatable Read. Default in PostgreSQL: Read Committed.

**Q: What is optimistic vs pessimistic locking?**  
**Pessimistic:** Lock row at read time (`SELECT FOR UPDATE`). Safe but reduces concurrency. **Optimistic:** Read with version number; on write, check version hasn't changed. No lock, high concurrency, but retries on conflict. Use optimistic for low-contention scenarios; pessimistic for high-contention (inventory, seat booking).

---

# Design Patterns Quick Pick

> The fastest way to answer "which pattern would you use here?"

| Scenario | Pattern |
|----------|---------|
| Only one instance of a DB connection pool | **Singleton** |
| Create objects without specifying the exact class | **Factory Method** |
| Build complex objects step-by-step with validation | **Builder** |
| Add logging, retry, or auth around an existing service | **Decorator** |
| Simplify a complex subsystem (HomeTheater, AWS SDK) | **Facade** |
| Multiple payment methods swappable at runtime | **Strategy** |
| Notify all subscribers when state changes (events) | **Observer** |
| Lazy-load or access-control a resource | **Proxy** |
| Support undo/redo in a text editor or transaction | **Command** or **Memento** |
| Objects of different types treated uniformly (file tree) | **Composite** |
| Adapt incompatible interfaces (legacy system) | **Adapter** |
| Escalate support tickets through tiers | **Chain of Responsibility** |
| Traffic light, shopping cart states | **State** |
| Decouple order types from payment methods | **Bridge** |
| Share flyweight for 1M bullets in a game | **Flyweight** |
| Walk a tree performing operations without modifying it | **Visitor** |
| Beverage preparation — same steps, different details | **Template Method** |
| UI form where components react to each other | **Mediator** |
| Evaluate salary rules as expressions | **Interpreter** |
| Iterate a custom collection | **Iterator** |

---

# System Design Concepts

**Q: How do you approach a system design interview?**  
1. **Clarify requirements** (5 min) — functional + non-functional, scale.  
2. **Capacity estimation** (3 min) — users, QPS, storage, bandwidth.  
3. **High-level design** (10 min) — components, APIs, data flow.  
4. **Deep dive** (15 min) — pick 2–3 hard problems and solve them.  
5. **Trade-offs** (5 min) — what you'd do differently at 10× scale.

**Q: How do you scale a read-heavy system?**  
Add read replicas, add caching (Redis/Memcached), use CDN for static assets, horizontal scale the API layer, denormalise for faster reads.

**Q: How do you scale a write-heavy system?**  
Sharding (partition data), async writes (write to queue, process later), CQRS (separate write model from read model), batching writes, use LSM-tree storage (Cassandra) that makes writes fast.

**Q: What is consistent hashing and why does it matter?**  
Arranges nodes on a ring. Each key maps to the nearest node clockwise. When a node is added/removed, only K/N keys move (vs. all keys with mod-hash). Used in CDN routing, distributed caches (Redis Cluster, Memcached), database sharding.

**Q: Cache strategies — what are the differences?**

| Strategy | How | Use when |
|----------|-----|----------|
| Cache-aside (lazy) | App reads cache; on miss, loads from DB + populates cache | Most common; works well for read-heavy |
| Write-through | Every write goes to cache AND DB synchronously | Need strong consistency; accepts write latency |
| Write-behind | Write to cache; DB updated asynchronously | Need low write latency; accept brief inconsistency |
| Read-through | Cache fetches from DB automatically on miss | Simpler app code; cache handles DB interaction |

**Q: What is the thundering herd problem and how do you fix it?**  
A popular cache key expires → thousands of requests simultaneously hit the DB. Fix: mutex lock (one request rebuilds cache, others wait), probabilistic early expiry (refresh before expiry), or background refresh (never let hot keys fully expire).

**Q: SQL vs NoSQL — which DB for which use case?**

| Use Case | Best DB |
|----------|--------|
| User accounts, orders, payments | PostgreSQL / MySQL (ACID) |
| User sessions, rate limiting counters | Redis |
| Time-series metrics, IoT data | InfluxDB / TimescaleDB |
| Product catalog (flexible schema) | MongoDB / DynamoDB |
| Social graph (follows, friends) | Neo4j / Amazon Neptune |
| Full-text search | Elasticsearch |
| Chat message history (append-only) | Cassandra |
| Analytics, reporting | BigQuery / Redshift |

**Q: How do you design for high availability?**  
Eliminate single points of failure (replicate everything), use health checks + auto-failover, design for graceful degradation, implement circuit breakers, use multi-AZ/multi-region deployment, test with chaos engineering (kill random nodes).

---

# Behavioral Questions

**Q: Tell me about a time you dealt with a production incident.**  
Structure: Situation → Action → Result → Learning. Key: show that you were calm, methodical (check dashboards, narrow the blast radius, fix forward), and ran a blameless postmortem.

**Q: How do you handle technical disagreements with teammates?**  
Listen to understand their reasoning, ask questions to surface assumptions, propose a time-boxed experiment or a shared evaluation criteria, then align on the decision. Document the decision and the rationale.

**Q: Describe a time you improved system performance.**  
Show the before-state (measurement), your hypothesis, what you changed, and the after-measurement. Interviewers want to see you measure-before-you-optimize, not guess.

**Q: How do you prioritise when you have multiple urgent tasks?**  
Assess impact and urgency (2×2 matrix), communicate to stakeholders, unblock others first (dependencies), get clarity on what "done" means before starting, time-box.

**Q: Tell me about a technical decision you regret.**  
Pick a real one. Show self-awareness: what you decided, what signals you missed, what you'd do differently. This shows growth mindset and intellectual honesty.

**Q: What do you do when requirements keep changing?**  
Accept that requirements evolve. Build for flexibility where uncertainty is high (interfaces, configuration over hardcoding), push back on frequent pivots with data (cost of change vs value), get acceptance criteria in writing before building.

---

## Final Checklist — Day Before the Interview

- [ ] Refresh 3 Java internals questions (HashMap, GC, memory model)
- [ ] Refresh Spring Boot: DI, `@Transactional`, auto-configuration
- [ ] Practice one system design question end-to-end (pick from `SystemDesign/`)
- [ ] Know your top 5 design patterns cold (Singleton, Factory, Builder, Observer, Strategy)
- [ ] Have 3 STAR-format behavioral stories ready (incident, conflict, achievement)
- [ ] Know the time/space complexity of common collections (ArrayList, LinkedList, TreeMap, PriorityQueue)
- [ ] Understand CAP theorem and be able to apply it to a specific database
- [ ] Review the project you'll talk about most — know the architecture, tech choices, and the hardest problem you solved

---

> **Deep dives:** For any topic here, read the full chapter in `The_Backend_Story.md` or `HLD_Story.md`.  
> **System Design practice:** Open any day in `SystemDesign/` and design it on paper before reading any solutions.  
> **Pattern code:** All 23 GoF patterns with runnable examples live in `src/main/java/`.
