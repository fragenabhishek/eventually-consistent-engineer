# Spring Debugging Playbook
### Printable Cheat Sheet for Production Incidents

> Use this during on-call or interview drills.  
> Goal: move from symptom to root cause fast, then add a prevention step.

---

## 0) 5-Step Incident Loop

1. **Stabilize first**: rollback, reduce traffic, or disable risky feature flags.
2. **Capture evidence**: logs with `traceId`, metrics, thread dump, failing payload.
3. **Narrow blast radius**: app only vs downstream vs infra vs config.
4. **Fix safely**: smallest valid patch + canary rollout.
5. **Prevent repeat**: test, alert, guardrail, runbook update.

---

## 1) Fast Triage Checklist (First 10 Minutes)

- **Health**: `/actuator/health`, readiness/liveness, dependency status
- **Errors**: top exception types by count in last 15 min
- **Latency**: p95/p99 split by endpoint
- **Resources**: CPU, heap, GC pause, thread count, DB pool usage
- **Recent changes**: deployment, config, feature flags, schema, secrets
- **Scope**: all users vs one tenant/region/profile

---

## 2) Spring Boot 25 Scenario Quick Fix Matrix

| # | Symptom | First Check | Most Likely Cause | Practical Fix |
|---|---------|-------------|-------------------|---------------|
| 1 | 500s after deployment | logs + release diff | config/feature drift | rollback or disable flag, patch and canary |
| 2 | `BeanCreationException` | full stacktrace root cause | missing bean/property/init failure | add bean/property, validate config at startup |
| 3 | `NoSuchBeanDefinitionException` in prod only | active profile + component scan | profile-conditional bean missing | align `@Profile`/`@Conditional` and add prod test |
| 4 | Circular dependency | bean dependency graph | bidirectional service wiring | refactor with coordinator/interface/events |
| 5 | `HttpMessageNotReadableException` | payload + DTO + Jackson config | type/date/enum mismatch | fix contract or DTO mapping, add request tests |
| 6 | `LazyInitializationException` | where entity is accessed | lazy relation used outside tx | fetch in service (`JOIN FETCH`) and return DTO |
| 7 | wrong config properties | effective config sources | typo/missing/overridden key | move to `@ConfigurationProperties` + `@Validated` |
| 8 | `DataIntegrityViolationException` | SQL state / constraint name | unique/FK/not-null/length violation | validate inputs, handle as 409/400, fix schema/data |
| 9 | `TransactionRequiredException` | call path to update | no active tx or self-invocation | add `@Transactional` on proxied public method |
| 10 | latency spike after logging | logger level + appenders | sync I/O + payload logging | async appenders, sample logs, avoid full bodies |
| 11 | memory leak symptoms | heap dump + dominator tree | unbounded caches/static refs/listeners | bounded cache, cleanup hooks, remove stale refs |
| 12 | intermittent DB connect failures | pool metrics + DB timeout | stale sockets/pool mismatch/network blips | tune Hikari timeouts + retries + maxLifetime |
| 13 | downstream timeout | client timeout and retry policy | slow dependency + no circuit breaker | set timeouts, retries with jitter, fallback |
| 14 | env configuration failure | active profile/env vars | wrong profile or missing secret | strict profile policy and startup fail-fast checks |
| 15 | duplicate request processing | idempotency key presence | client retries without dedupe | idempotency key store + unique constraint |
| 16 | crashes from unhandled exceptions | top uncaught exceptions | no global error mapping | `@ControllerAdvice` + stable error model |
| 17 | high thread usage | thread dump states | unbounded executors/blocking calls | bounded pools, backpressure, remove blocking paths |
| 18 | scheduled job runs multiple times | pod count + schedule setup | each instance runs job | distributed lock (ShedLock) or singleton scheduler |
| 19 | dependency conflict at deploy | dependency tree/BOM | transitive version mismatch | align with Boot BOM, exclude conflicting transitives |
| 20 | inconsistent concurrent updates | tx isolation + lock strategy | lost update/write skew | optimistic lock (`@Version`) or row lock + retry |
| 21 | logs not enough to debug | log schema + trace coverage | no context IDs / weak structure | JSON logs + MDC traceId + boundary logs |
| 22 | API gateway fails on downstream errors | route-level metrics | no timeout/circuit/fallback | per-route resilience policy + graceful fallback |
| 23 | app unresponsive under load | thread dump + DB pool + GC | bottleneck saturation | load test, tune pools/indexes, cap concurrency |
| 24 | zero-downtime deployment needed | readiness + schema compatibility | incompatible rollout steps | rolling/blue-green + backward-compatible DB changes |
| 25 | prod behavior differs from local | profile/env/data/parity | environment/data topology mismatch | reproduce in prod-like staging and diff systematically |

---

## 3) High-Value Snippets

### Global Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIssue(Exception ex) {
        return ResponseEntity.status(409).body(Map.of("code", "DATA_CONFLICT"));
    }
}
```

### Typed and Validated Configuration
```java
@Validated
@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
    @NotNull Duration timeout,
    @Min(1) @Max(5) int maxRetries
) {}
```

### Idempotent Write Guard
```java
// Pseudocode
if (idempotencyStore.contains(key)) return idempotencyStore.getResponse(key);
Response response = process();
idempotencyStore.save(key, response);
return response;
```

---

## 4) Production Hardening Defaults

- Timeouts on all outbound calls (no infinite waits)
- Retry with exponential backoff + jitter (idempotent ops only)
- Circuit breaker on unstable dependencies
- Bounded thread pools and queue limits
- Structured logs with `traceId`/`spanId`
- Actuator + alerts for error rate, latency, saturation
- Profile-aware integration tests (`dev`, `staging`, `prod`)

---

## 5) Interview 60-Second Answer Template

When asked any debugging question:

1. **Classify**: app bug, config, dependency, data, infra.
2. **Observe**: logs + metrics + traces + recent change.
3. **Isolate**: reproduce one failing path with trace ID.
4. **Mitigate**: rollback/flag/fallback to restore service.
5. **Fix + Prevent**: code/config patch, test, monitor, runbook.

Use this structure and then give one concrete example.
