# 90-Day Java Backend + AI Build Plan

> IT Asset Management System — Microservices, RAG & Agentic AI
> Start: Sat, 05 Sep 2026 → Finish: Thu, 03 Dec 2026 · ~4 hours/day, weekends included
> Stack: Java 21 · Spring Boot · Spring Cloud · Kafka · Redis · MinIO · Claude AI
>
> **Source:** transcribed verbatim from `90-Day-Java-Backend-AI-Plan.pdf`. This is the canonical day-by-day plan.
> For the repo file map, see [PROJECT-STRUCTURE.md](PROJECT-STRUCTURE.md). Search **both** files.

## Status legend
- ✅ done · ⚠️ partial · ⬜ not started

| Phase | Days | Status |
|-------|------|--------|
| Phase 0 — Foundations | 1–7 | ✅ |
| Phase 1 — Core Domain | 8–28 | up to Day 19 ✅, Day 20–21 ⚠️, 22+ ⬜ |
| Phase 2 — Microservices & Infra | 29–56 | ⬜ |
| Phase 3 — Cross-cutting & Protocols | 57–72 | ⬜ |
| Phase 4 — AI Layer & Polish | 73–90 | ⬜ |

**Current position (2026-09-11):** fully done and **committed** through **Day 19**. Day 20 `/api/users/me` built + committed, but role-based `@PreAuthorize` still missing. Day 21 `JwtUtilTest` built + committed, but `AuthControllerTest` + role×endpoint matrix missing. Refresh-token flow is an addendum extra (beyond the base plan). Working tree is clean except this file.

---

## How to use this plan
- One row = one day. Do the Build task, spend ~30–45 min on the Study item, then verify the Checkpoint before moving on.
- If a day's Checkpoint isn't green, don't skip — carry it to the next day. Weekly review days exist for exactly this.
- Commit to git every single day. A daily commit streak is half the discipline.
- Dates assume you start and work every day. Slipping a few days is normal — shift the tail, keep the order.
- Study items build the Java language depth (collections, streams, concurrency, generics, JVM) that the framework alone won't teach.

---

# Phase 0 — Foundations
**Week 1 · Days 1–7** — Toolchain, Java refresher, Spring Boot basics, first Asset CRUD.

### DAY 1 · Sat, 05 Sep 2026 — Environment Setup ✅
- **Focus:** Get the toolchain installed and verified so nothing blocks you later.
- **Build:** Install JDK 21 (Temurin), Maven, IntelliJ IDEA Community, Docker, PostgreSQL 16 (Docker fine). Create GitHub repo `it-asset-mgmt`, clone it. Install `psql`/DBeaver.
- **Study:** Skim Java 21 highlights — records, `var`, switch expressions, text blocks.
- **Checkpoint:** `java -version` shows 21, `mvn -version` uses that JDK, `docker ps` shows the `pg` container, `psql` connects.

### DAY 2 · Sun, 06 Sep 2026 — Git Basics + Repo Skeleton ✅
- **Focus:** Get comfortable with the daily git workflow.
- **Build:** Create top-level structure (`asset-service/`, `.gitignore`, root `README.md`). Practice `git add`/`commit`/`push` — 3+ commits. Write a throwaway `Scratch.java` building a `List<String>` of asset tags and a `Map<String,String>` of tag→status.
- **Study:** Java collections overview — `List`, `Set`, `Map` and `ArrayList`/`HashSet`/`HashMap`.
- **Checkpoint:** `git log` shows 3+ commits pushed; scratch program prints list and map.

### DAY 3 · Mon, 07 Sep 2026 — Spring Boot Hello World ✅
- **Focus:** Stand up first Spring Boot app and understand auto-configuration.
- **Build:** Generate `asset-service` (Maven, Java 21, Spring Boot 3.3+, Spring Web). Add `HelloController` with `GET /api/hello`. Run via `mvn spring-boot:run`.
- **Study:** Java streams basics — `.stream().filter().map().collect()`.
- **Checkpoint:** `curl localhost:8080/api/hello` returns the string; streams exercise prints filtered/mapped list.

### DAY 4 · Tue, 08 Sep 2026 — First Entity + JPA + Postgres Connection ✅
- **Focus:** Connect Spring Boot to Postgres and persist your first domain object.
- **Build:** Add `spring-boot-starter-data-jpa` + `postgresql` driver. Configure datasource + `ddl-auto=update`. Create `Asset` entity (id, assetTag, category, serialNumber, status). Confirm Hibernate creates the table.
- **Study:** Java `enum` types (you'll want `AssetStatus` soon).
- **Checkpoint:** `asset` table exists in Postgres with correct columns.

### DAY 5 · Wed, 09 Sep 2026 — Repository + Basic CRUD Endpoints ✅
- **Focus:** Wire Spring Data JPA repository and expose real CRUD over HTTP.
- **Build:** `AssetRepository extends JpaRepository<Asset, Long>`. `AssetController` with POST/GET(all)/GET(one)/PUT/DELETE. Thin `AssetService` in between (constructor injection).
- **Study:** `Optional<T>` — `.orElseThrow()` vs `.get()`, return 404 on missing id.
- **Checkpoint:** All 5 CRUD endpoints work end-to-end; POST returns 201, bad id returns 404.

### DAY 6 · Thu, 10 Sep 2026 — Refactor with Enum + DTOs + Basic Validation ✅
- **Focus:** Clean up the skeleton — separate API shape from entity, validate input.
- **Build:** Convert `status` to `AssetStatus` enum (`@Enumerated(STRING)`). Add validation starter, `AssetRequest` record with `@NotBlank`, `@Valid` on POST/PUT. Minimal `@ExceptionHandler` → 400.
- **Study:** Java `record` types — why DTOs fit records.
- **Checkpoint:** Posting `{"assetTag":""}` → 400 with clear body; valid asset → 201.

### DAY 7 · Fri, 11 Sep 2026 — Review, Catch-Up, Consolidate ✅
- **Focus:** Lighter day — solidify, fix rough edges.
- **Build:** Re-run every endpoint end-to-end, fix anything broken. Document the 5 endpoints in README. Optional: `GET /api/assets?status=DEPLOYED` derived query.
- **Study:** Recap week — reread your Day 2/3/5 snippets.
- **Checkpoint:** Clean clone → `mvn spring-boot:run` → every CRUD endpoint works first try.

---

# Phase 1 — Core Domain
**Weeks 2–4 · Days 8–28** — Assets, Users/Auth (JWT), Assignments & lifecycle. The real business logic.

### DAY 8 · Sat, 12 Sep 2026 — Domain modeling: Asset, Category, Location ✅
- **Focus:** Core JPA entities and relationships.
- **Build:** `Category` (id, name, description), `Location` (id, building, floor, room), `Asset` (id, assetTag, serialNumber, name, category `@ManyToOne`, location `@ManyToOne`, status, purchaseDate, warrantyExpiryDate, amcExpiryDate, createdAt). Add `AssetStatus` enum. Configure Postgres datasource.
- **Study:** JPA relationships — `@ManyToOne`/`@OneToMany`, LAZY vs EAGER, `equals()`/`hashCode()` care.
- **Checkpoint:** App boots; `\dt` shows `assets`, `categories`, `locations` with correct FKs.

### DAY 9 · Sun, 13 Sep 2026 — Repositories + seed data ✅
- **Focus:** Spring Data JPA repositories.
- **Build:** `AssetRepository`, `CategoryRepository`, `LocationRepository`. `CommandLineRunner` (or `data.sql`) seeding 5 categories, 3 locations, 10 assets across statuses.
- **Study:** Query derivation (`findByStatus`, `findBySerialNumber`); `JpaRepository` vs `CrudRepository`.
- **Checkpoint:** Startup log prints seeded row counts read via each repository.

### DAY 10 · Mon, 14 Sep 2026 — DTOs + mapping ✅
- **Focus:** Keep entities off the wire.
- **Build:** `AssetRequestDto`, `AssetResponseDto`, `CategoryDto`, `LocationDto`. `AssetMapper` (plain static methods) with `toEntity()`/`toDto()`.
- **Study:** Streams — `list.stream().map(AssetMapper::toDto).collect(...)`.
- **Checkpoint:** A test round-trips an `Asset` through `toDto`→`toEntity` and fields match.

### DAY 11 · Tue, 15 Sep 2026 — AssetController CRUD ✅
- **Focus:** REST layer wiring.
- **Build:** `AssetService` (business logic) + `AssetController` with POST/GET(one)/GET(all)/PUT/DELETE.
- **Study:** Layered architecture (Controller→Service→Repository); `Optional.orElseThrow()` for 404.
- **Checkpoint:** All 5 endpoints work against seeded data; `GET /api/assets/9999` → 404.

### DAY 12 · Wed, 16 Sep 2026 — Bean Validation + global exception handling ✅
- **Focus:** Reject bad input, centralize error responses.
- **Build:** `@NotBlank`/`@Size`/`@Pattern` on `AssetRequestDto`. `ResourceNotFoundException`, `DuplicateResourceException`, `GlobalExceptionHandler` (`@ControllerAdvice`): validation → 400, not-found → 404, `DataIntegrityViolation`/duplicate → 409.
- **Study:** Bean Validation annotations; custom exception hierarchy under `RuntimeException`.
- **Checkpoint:** POST missing `serialNumber` → 400 with field message; duplicate serial → 409.

### DAY 13 · Thu, 17 Sep 2026 — Pagination + filtering + sorting ✅
- **Focus:** Real list queries, streams deep-dive.
- **Build:** `GET /api/assets?status=&categoryId=&page=&size=&sort=` using `Pageable` + `AssetSpecification` (JPA Criteria). Bonus `GET /api/assets/stats/by-category` via `Collectors.groupingBy`.
- **Study:** Streams heavy day — `groupingBy`, `counting`, `partitioningBy`, method references.
- **Checkpoint:** Filtered/paginated/sorted `Page<AssetResponseDto>` correct; stats endpoint accurate.

### DAY 14 · Fri, 18 Sep 2026 — Week 2 review, tests, refactor ✅
- **Focus:** Consolidate before moving on.
- **Build:** `AssetServiceTest` (JUnit 5 + Mockito) covering create/get/update/delete/not-found. Review controllers for consistent status codes.
- **Study:** Re-skim collections/streams notes.
- **Checkpoint:** `mvn test` green; full Asset CRUD + filter/pagination re-verified end to end.

### DAY 15 · Sat, 19 Sep 2026 — User entity + Role ✅
- **Focus:** Identity model.
- **Build:** `User` entity (id, fullName, email, username, passwordHash, department, `role` enum: ADMIN, EMPLOYEE). `UserRepository`. Seed 2 users.
- **Study:** Enums with fields/behavior.
- **Checkpoint:** `users` table exists with seeded rows; `findByUsername("admin")` returns the admin.

### DAY 16 · Sun, 20 Sep 2026 — Password hashing + UserService ✅
- **Focus:** Never store plaintext passwords.
- **Build:** Add `spring-boot-starter-security`. Define `BCryptPasswordEncoder` `@Bean`. `UserService.registerUser()` hashes before saving; `UserRepository.findByUsername`.
- **Study:** Generics intro — `Optional<T>`, `JpaRepository<T, ID>`.
- **Checkpoint:** Registering stores a `$2a$` hash; `passwordEncoder.matches(raw, hash)` is true.

### DAY 17 · Mon, 21 Sep 2026 — Spring Security config skeleton ✅
- **Focus:** Lock the app down by default.
- **Build:** `SecurityConfig` with a `SecurityFilterChain`: `permitAll()` on `/api/auth/**`, `authenticated()` elsewhere, CSRF disabled, `SessionCreationPolicy.STATELESS`.
- **Study:** Generics — bounded type params and `? extends` wildcards.
- **Checkpoint:** Unauthenticated `GET /api/assets` → 401; `POST /api/auth/login` (not yet implemented) still reachable.

### DAY 18 · Tue, 22 Sep 2026 — JWT login endpoint ✅
- **Focus:** Issue tokens.
- **Build:** Add `jjwt`. `JwtUtil` (generate with `sub`/`role`/`exp`; validate/parse). `AuthController` with `POST /api/auth/login` accepting username/password, authenticating, returning the JWT.
- **Study:** Generics — `Function<T, R>` and a small generic utility method.
- **Checkpoint:** `curl POST /api/auth/login` with valid creds returns 200 + JWT; decoding it shows correct `sub`/`role`/`exp`.

### DAY 19 · Wed, 23 Sep 2026 — JwtAuthFilter + securing endpoints ✅
- **Focus:** Validate tokens on every request.
- **Build:** `JwtAuthFilter extends OncePerRequestFilter` — extract `Bearer` token, validate via `JwtUtil`, populate `SecurityContextHolder`. Register before `UsernamePasswordAuthenticationFilter`.
- **Study:** Generics — how `OncePerRequestFilter` and `Collection<? extends GrantedAuthority>` use bounded generics.
- **Checkpoint:** `GET /api/assets` without a token → 401; with a valid Bearer token → 200 with data.

### DAY 20 · Thu, 24 Sep 2026 — Role-based access control ✅
- **Focus:** ADMIN vs EMPLOYEE permissions.
- **Build:** `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` on `AssetController` POST/PUT/DELETE; EMPLOYEE limited to GET. Add `GET /api/users/me` returning the authenticated user pulled from `SecurityContext`. **← both done: `/me` + RBAC.**
- **Study:** How role checks compose with `Collection<? extends GrantedAuthority>`.
- **Checkpoint:** Logged in as EMPLOYEE, `POST /api/assets` → 403; as ADMIN → 201.

### DAY 21 · Fri, 25 Sep 2026 — Week 3 review, tests, refactor ⚠️ (tests done, matrix not run live)
- **Focus:** Prove the security layer works.
- **Build:** `JwtUtilTest` (generate/parse round-trip, expired-token case) ✅ and `AuthControllerTest` (happy path + bad credentials + unknown user, Mockito) ✅. Still TODO: manually run the full role × endpoint access matrix against a live app. Tidy `SecurityConfig` (optional).
- **Study:** Recap generics notes.
- **Checkpoint:** `mvn test` green; documented access matrix (ADMIN/EMPLOYEE × GET/POST/PUT/DELETE) behaves as expected.

### DAY 22 · Sat, 26 Sep 2026 — Assignment entity + lifecycle transition rules ⬜
- **Focus:** Model who has what, and valid status moves.
- **Build:** `Assignment` entity (id, `asset` `@ManyToOne`, `user` `@ManyToOne`, assignedAt, returnedAt nullable, assignedBy, notes). `canTransitionTo(AssetStatus)` on `AssetStatus` encoding the legal lifecycle (PROCURED→DEPLOYED→IN_REPAIR→RETIRED→DISPOSED, no backward).
- **Study:** Enums with behavior — methods and per-constant logic.
- **Checkpoint:** `assignments` table exists; `RETIRED.canTransitionTo(DEPLOYED)` false, `PROCURED.canTransitionTo(DEPLOYED)` true.

### DAY 23 · Sun, 27 Sep 2026 — AssignmentService: assign asset ⬜
- **Focus:** First transactional multi-table write.
- **Build:** `POST /api/assignments {assetId, userId}` → `AssignmentService.assignAsset()`: verify asset not already assigned (throw `AssetNotAvailableException` → 409), create `Assignment` row, flip `Asset.status` to DEPLOYED, all in one `@Transactional`.
- **Study:** `Optional` deep dive — `orElseThrow`, `.map()`/`.filter()` chains.
- **Checkpoint:** Assigning an already-assigned asset → 409; a success updates `assignments` and `assets` together (no partial writes).

### DAY 24 · Mon, 28 Sep 2026 — Check-in / return logic ⬜
- **Focus:** Close the assignment loop.
- **Build:** `POST /api/assignments/{id}/return` → `returnAsset()` sets `returnedAt = now()`, flips asset status back, `@Transactional`. Add `AssignmentNotFoundException` + `InvalidAssignmentStateException` wired into `GlobalExceptionHandler`.
- **Study:** Narrow custom exceptions vs reusing generic ones.
- **Checkpoint:** Returning an already-returned assignment → 400; `GET /api/assets/{id}` confirms status flipped after return.

### DAY 25 · Tue, 29 Sep 2026 — Assignment history + audit fields ⬜
- **Focus:** Who did what, when.
- **Build:** `GET /api/assets/{id}/assignments` and `GET /api/users/{id}/assignments` for full history. Add `createdBy`/`createdAt`/`updatedBy`/`updatedAt` via Spring Data JPA Auditing (`@CreatedDate`, `@CreatedBy`, `AuditorAware<String>` reading username from `SecurityContext`).
- **Study:** `Optional` inside `AuditorAware#getCurrentAuditor()`; streams `.sorted(Comparator.comparing(...).reversed())`.
- **Checkpoint:** New rows auto-populate audit columns; history endpoints return assignments newest-first.

### DAY 26 · Wed, 30 Sep 2026 — Flagship endpoint: employee assets query ⬜
- **Focus:** The natural-language query this whole system exists to answer.
- **Build:** `GET /api/employees/{id}/assets` → `EmployeeAssetsResponseDto { employeeName, List<AssetSummaryDto> assets, int count }` where `AssetSummaryDto` = serialNumber + assetTag + name. Backed by `AssignmentRepository.findByUserIdAndReturnedAtIsNull(userId)`, mapped with a stream, `.size()` for count.
- **Study:** Streams recap — nested DTOs from a query result, `Collectors.toList()` + `.count()`.
- **Checkpoint:** `GET /api/employees/3/assets` returns exactly the serials currently assigned, count matches DB.

### DAY 27 · Thu, 01 Oct 2026 — Unit tests for the service layer (Mockito) ⬜
- **Focus:** Lock in behavior with tests.
- **Build:** `AssignmentServiceTest` with `@ExtendWith(MockitoExtension.class)`, `@Mock` repos, `@InjectMocks` service. Cover assign success, assign-when-already-assigned failure, return success, return-when-already-returned failure, employee-assets query. Use `ArgumentCaptor`.
- **Study:** JUnit 5 + Mockito basics — `@Mock`, `@InjectMocks`, `verify()`, `ArgumentCaptor`, `when()`.
- **Checkpoint:** `mvn test` shows ≥8 passing `AssignmentService` tests with real assertions on captured arguments.

### DAY 28 · Fri, 02 Oct 2026 — Phase 1 consolidation ⬜
- **Focus:** Prove the whole core domain works end to end before splitting into services.
- **Build:** Full regression: `mvn test`, then manually walk the lifecycle via Postman (create asset → assign → return → retire → employee assets). Tag commit `phase1-core-domain-complete`.
- **Study:** Recap Optional/exceptions/testing; jot 3 concrete improvements before Phase 2.
- **Checkpoint:** Entire Phase 1 flow runs error-free; all tests green; git tag created.

---

# Phase 2 — Microservices & Infrastructure
**Weeks 5–8 · Days 29–56** — Split into services, discovery, gateway, Feign, resilience, tracing, Kafka, Saga.

### DAY 29 · Sat, 03 Oct 2026 — Discovery server (Eureka) ⬜
- **Build:** New module `discovery-server`, `spring-cloud-starter-netflix-eureka-server`, `@EnableEurekaServer`, port 8761, `register-with-eureka=false`, `fetch-registry=false`.
- **Study:** Service discovery pattern — client-side vs server-side.
- **Checkpoint:** `discovery-server` runs; Eureka dashboard at `localhost:8761` shows empty instance list.

### DAY 30 · Sun, 04 Oct 2026 — Config server (Spring Cloud Config) ⬜
- **Build:** New module `config-server`, `@EnableConfigServer`, port 8888, backed by a git repo (or `native`) holding shared `application.yml` + per-service files.
- **Study:** 12-factor config; Spring `Environment`/`PropertySource` resolution order.
- **Checkpoint:** `localhost:8888/application/default` returns JSON.

### DAY 31 · Mon, 05 Oct 2026 — Extract user-auth-service ⬜
- **Build:** New module `user-auth-service`. Move User/Role, repos, auth controller, JWT filter from monolith. Own Postgres DB `userdb`. Add eureka-client + config-client, `spring.config.import=configserver:...`.
- **Study:** Database-per-service — why sharing one DB recreates the monolith.
- **Checkpoint:** `user-auth-service` starts, connects to `userdb`, appears in Eureka, `/login`/`/register` work standalone.

### DAY 32 · Tue, 06 Oct 2026 — Extract asset-service ⬜
- **Build:** New module `asset-service` (keeps Asset + Assignment together). Own DB `assetdb`. Same Eureka + config wiring. User FK becomes a plain `userId` (no cross-DB join).
- **Study:** Bounded contexts (DDD) — why `asset-service` stores only `userId`.
- **Checkpoint:** `asset-service` runs, registers as `ASSET-SERVICE`, "assets for employee X" query works using only `userId`.

### DAY 33 · Wed, 07 Oct 2026 — Dockerize both services ⬜
- **Build:** Multi-stage `Dockerfile` per service (build stage → slim JRE stage) for discovery, config, user-auth, asset. Run each with `docker run` passing DB/Eureka URLs as env. Two Postgres containers (`userdb`, `assetdb`).
- **Study:** Multi-stage Docker builds — don't ship the JDK + build tool.
- **Checkpoint:** All four images build cleanly; each container starts with correct env vars.

### DAY 34 · Thu, 08 Oct 2026 — docker-compose wires it all together ⬜
- **Build:** `docker-compose.yml` with discovery, config, postgres-user, postgres-asset, user-auth, asset services. Use service names as hostnames. Add a bare-bones `gateway` module registered in Eureka.
- **Study:** Docker Compose networking — default bridge, DNS-based discovery.
- **Checkpoint:** `docker-compose up` brings up six containers; Eureka shows `USER-AUTH-SERVICE`, `ASSET-SERVICE`, `GATEWAY`.

### DAY 35 · Fri, 09 Oct 2026 — Week 5 review + integration test ⬜
- **Build:** No new services. Full `docker-compose up`, curl through each service directly (not gateway yet): register, login, create asset, assign, employee query. Note which properties are still hardcoded vs from config-server.
- **Study:** Recap discovery, config externalization, DB-per-service — 3 sentences each.
- **Checkpoint:** Every core flow works end-to-end via direct service calls; zero services need manual restart to pick up config.

### DAY 36 · Sat, 10 Oct 2026 — Gateway routing ⬜
- **Build:** In `gateway`, add `spring-cloud-starter-gateway` + Eureka client. Enable `discovery.locator` or explicit routes: `/api/auth/**` → `lb://USER-AUTH-SERVICE`, `/api/assets/**` → `lb://ASSET-SERVICE`. Port 8080.
- **Study:** Spring Cloud Gateway is WebFlux/reactive — how it differs from Servlet MVC.
- **Checkpoint:** `curl localhost:8080/api/auth/login` and `.../api/assets/...` both routed correctly.

### DAY 37 · Sun, 11 Oct 2026 — JWT validation filter at the gateway ⬜
- **Build:** Custom `GatewayFilterFactory` (or global `GlobalFilter`) reading `Authorization`, validating JWT signature/expiry (reuse JWT util), rejecting 401, forwarding valid tokens downstream. Apply to `/api/assets/**`, leave `/api/auth/**` open.
- **Study:** Reactive filter chain (`Mono`/`Flux`) vs servlet `OncePerRequestFilter`.
- **Checkpoint:** `/api/assets/**` without a valid JWT → 401 at gateway; valid token passes through.

### DAY 38 · Mon, 12 Oct 2026 — Rate limiting with Redis ⬜
- **Build:** Redis container in compose. `spring-cloud-starter-gateway` `RequestRateLimiter` backed by `data-redis-reactive`, `RedisRateLimiter` (e.g. 10 rps, burst 20) keyed by `KeyResolver` (IP or user from JWT). Apply to asset route.
- **Study:** Token bucket algorithm — replenish-rate vs burst-capacity.
- **Checkpoint:** Hammering `/api/assets/**` (~30 req/sec) returns 429s once the bucket empties.

### DAY 39 · Tue, 13 Oct 2026 — OpenFeign: asset-service calls user-auth-service ⬜
- **Build:** In `asset-service`, `spring-cloud-starter-openfeign`, `@EnableFeignClients`, `@FeignClient(name="user-auth-service")` `UserClient` with `getUserById(Long id)` hitting an internal `GET /internal/users/{id}`. Use in assignment flow to validate the user exists.
- **Study:** Declarative HTTP clients — Feign vs `RestTemplate`/`WebClient`.
- **Checkpoint:** Assigning to a bogus `userId` fails clearly (404 surfaced); a real `userId` succeeds.

### DAY 40 · Wed, 14 Oct 2026 — DTO contracts between services ⬜
- **Build:** `UserSummaryDto` (id, name, email, role — no password hash, no JPA) as the Feign response type instead of full `User`. Internal endpoint behind explicit DTO mapping.
- **Study:** Why coupling wire format to entity structure breaks independent deployability.
- **Checkpoint:** The Feign call in `asset-service` only ever sees `UserSummaryDto` fields.

### DAY 41 · Thu, 15 Oct 2026 — Centralize remaining config in config-server ⬜
- **Build:** Move gateway rate-limit settings, shared JWT signing secret, DB connection strings into config-server's git repo (`gateway.yml`, `user-auth-service.yml`, `asset-service.yml`). Use Spring profiles (`application-docker.yml` vs `application-local.yml`).
- **Study:** Spring profiles and property precedence.
- **Checkpoint:** Changing the rate limit or JWT secret in config-server + restart picks up the new value with no code change.

### DAY 42 · Fri, 16 Oct 2026 — Week 6 review + integration test ⬜
- **Build:** Full flow through the gateway only: register, login, create asset, assign (Feign call), hit rate limit → 429, expired/tampered JWT → 401. Note any config still hardcoded.
- **Study:** Recap gateway responsibilities (routing, auth, rate limiting) vs service's job.
- **Checkpoint:** Every flow passes going only through `localhost:8080`; zero direct backend calls needed.

### DAY 43 · Sat, 17 Oct 2026 — Circuit breaker on the Feign call ⬜
- **Build:** `spring-cloud-starter-circuitbreaker-resilience4j` in `asset-service`. Wrap `UserClient` with `@CircuitBreaker(name="userService", fallbackMethod="userFallback")`, fallback returns cached/default `UserSummaryDto` or clear error. Configure thresholds.
- **Study:** Circuit breaker states — closed, open, half-open.
- **Checkpoint:** Stopping `user-auth-service` makes the assignment endpoint fail fast with fallback instead of hanging.

### DAY 44 · Sun, 18 Oct 2026 — Retry + timeout ⬜
- **Build:** Resilience4j `Retry` (3 attempts, exponential backoff) + `TimeLimiter` (2s) around the Feign call, stacked with the circuit breaker (timeout innermost, then retry, then breaker). Log each retry.
- **Study:** Exponential backoff — why naive immediate retries cause retry storms.
- **Checkpoint:** A briefly-slow `user-auth-service` triggers visible retries and eventually succeeds or falls back cleanly, caller never blocks past the timeout.

### DAY 45 · Mon, 19 Oct 2026 — Bulkhead + thread pool isolation ⬜
- **Build:** Resilience4j `Bulkhead`/`ThreadPoolBulkhead` around the Feign call, capping concurrent calls independent of the app's thread pool. Load-test while `user-auth-service` is slow.
- **Study:** Thread pools — `ExecutorService`, fixed vs cached, unbounded pool exhaustion.
- **Checkpoint:** While the Feign call is saturated/slow, unrelated asset-service endpoints stay responsive.

### DAY 46 · Tue, 20 Oct 2026 — Distributed tracing (Micrometer + Zipkin) ⬜
- **Build:** `micrometer-tracing-bridge-brave` + `zipkin-reporter-brave` to gateway, asset, user-auth. Zipkin container (port 9411). Trigger the assignment flow, view the trace.
- **Study:** Trace vs span vs correlation ID.
- **Checkpoint:** Zipkin UI shows one trace with spans for gateway, asset-service, and the Feign call into user-auth-service.

### DAY 47 · Wed, 21 Oct 2026 — Correlation IDs + structured logging ⬜
- **Build:** Add trace ID to each service's log pattern (`logback-spring.xml`) so every line shows `traceId=...`. Switch to a JSON log encoder (or plain pattern with trace id).
- **Study:** MDC (Mapped Diagnostic Context) — thread-local per-request logging metadata.
- **Checkpoint:** `grep`-ing one trace ID across the three services' logs shows the full request path in order.

### DAY 48 · Thu, 22 Oct 2026 — Concurrency deep dive: async + CompletableFuture ⬜
- **Build:** Refactor one read-heavy asset endpoint to fetch user data async via `CompletableFuture.supplyAsync` on a dedicated `ExecutorService`, combine with `.thenApply`/`.thenCombine`. Tune pool size, benchmark before/after.
- **Study:** `ExecutorService` pool types, `CompletableFuture` composition, Feign's own thread pool interaction.
- **Checkpoint:** Refactored endpoint returns same correct data; a small concurrent benchmark (20 parallel requests) shows measurably better throughput.

### DAY 49 · Fri, 23 Oct 2026 — Week 7 review + integration test ⬜
- **Build:** With full stack up: kill `user-auth-service` mid-load, confirm circuit breaker opens, assignment degrades gracefully; bring it back, confirm breaker closes. Check the trace in Zipkin.
- **Study:** Recap resilience patterns — retry vs circuit breaker vs bulkhead vs timeout.
- **Checkpoint:** The kill-and-recover test is observed end-to-end (logs + Zipkin + Resilience4j state) and documented.

### DAY 50 · Sat, 24 Oct 2026 — Kafka setup + first published event ⬜
- **Build:** Kafka (+ Zookeeper/KRaft) containers in compose. In `asset-service`, `spring-kafka`, `AssetAssigned` event record (assetId, userId, assignedAt, eventId). On assignment, publish via `KafkaTemplate` to `asset-events`. Confirm with `kafka-console-consumer`.
- **Study:** Pub-sub vs point-to-point — why Kafka topics allow multiple independent consumers.
- **Checkpoint:** Assigning an asset produces a visible `AssetAssigned` message on `asset-events`.

### DAY 51 · Sun, 25 Oct 2026 — audit-service consumes events ⬜
- **Build:** New module `audit-service` — own DB `auditdb`, Eureka + config client, `spring-kafka` consumer. `@KafkaListener(topics="asset-events", groupId="audit-service")` writes each event to `audit_log`. Also publish `AssetReturned` from asset-service.
- **Study:** Consumer groups and offsets.
- **Checkpoint:** Both `AssetAssigned` and `AssetReturned` events show up as rows in `audit_log`.

### DAY 52 · Mon, 26 Oct 2026 — Notification on event ⬜
- **Build:** In `audit-service` (or a second `@KafkaListener`), add a notification listener on `asset-events` logging a human-readable line ("User 42 was assigned Asset SN-1001"). Separate `groupId` so it consumes independently.
- **Study:** At-least-once delivery — why a consumer crash-restart can replay an event.
- **Checkpoint:** One `AssetAssigned` event produces both an audit_log row and a notification log line; restarting mid-stream doesn't lose events.

### DAY 53 · Tue, 27 Oct 2026 — Idempotent consumers ⬜
- **Build:** `processed_events` table in `auditdb` with a unique constraint on `eventId`. In the audit listener, attempt insert (`ON CONFLICT DO NOTHING`) before writing the audit log — skip if already handled. Test by replaying an old offset.
- **Study:** Idempotency keys — using a unique event id as a dedupe key (applies to payment APIs, webhooks).
- **Checkpoint:** Re-delivering the same event (same `eventId`) produces exactly one audit_log row, not two.

### DAY 54 · Wed, 28 Oct 2026 — Saga pattern: assign flow with compensation ⬜
- **Build:** Redesign assign as a choreographed saga: asset-service sets status `PENDING`, publishes `AssetAssignmentRequested`. A listener validates user active, publishes `AssetAssignmentConfirmed` or `...Failed`. asset-service listens: Confirmed → `ASSIGNED`; Failed → revert to `AVAILABLE` (compensating action).
- **Study:** Saga — choreography vs orchestration.
- **Checkpoint:** A forced-failure case (inactive user) leaves the asset back in `AVAILABLE` via the compensating event, not stuck in `PENDING`.

### DAY 55 · Thu, 29 Oct 2026 — Saga failure testing + eventual consistency ⬜
- **Build:** Small test script triggering concurrent assign attempts on the same asset, killing a consumer mid-processing and restarting, confirming the asset always ends in a consistent terminal state (`ASSIGNED` xor `AVAILABLE`), never stuck `PENDING`.
- **Study:** Eventual consistency guarantees — the tradeoff (no immediate cross-service atomicity) for availability + loose coupling.
- **Checkpoint:** The concurrent/kill test ends with the asset in a valid terminal state every run, no manual DB fixups.

### DAY 56 · Fri, 30 Oct 2026 — Phase 2 final review + full-stack integration ⬜
- **Build:** `docker-compose up` the complete stack (discovery, config, gateway, user-auth, asset, audit, Postgres DBs, Kafka, Redis, Zipkin). Run one full scenario through the gateway: register → login → create → assign (Feign + breaker) → confirm Kafka event → audit_log + notification → Zipkin trace → force a failure + confirm saga compensation. Write a lightweight ADR log.
- **Study:** Recap the whole phase — why each piece exists.
- **Checkpoint:** Full scenario runs clean in one sitting with `docker-compose up`, no manual restarts; decisions notes file exists.

---

# Phase 3 — Cross-cutting & Protocols
**Weeks 9–10 · Days 57–72** — Redis, MinIO, WebSockets, GraphQL, gRPC, Testcontainers.

### DAY 57 · Sat, 31 Oct 2026 — Redis Cache Setup + First @Cacheable ⬜
- **Build:** `spring-boot-starter-data-redis` + `spring-boot-starter-cache` in asset-service. `RedisCacheManager` with 10-min TTL. `@EnableCaching`, `@Cacheable(value="assetsByEmployee", key="#employeeId")` on `getAssetsForEmployee`. Redis container.
- **Study:** JVM memory model — heap vs stack vs metaspace; `jcmd <pid> VM.flags`.
- **Checkpoint:** Calling `GET /assets/employee/{id}` twice logs one SQL query and one cache hit.

### DAY 58 · Sun, 01 Nov 2026 — Cache Eviction, TTL Tuning, More Cached Reads ⬜
- **Build:** `@CacheEvict(value="assetsByEmployee", ...)` on assign/unassign/update. `@Cacheable("assetById")` on `getAssetById`. A second shorter-TTL region (`assetStats` 2 min). Inspect keys with `redis-cli`.
- **Study:** Object lifecycle & generational GC — young/old gen, minor vs major GC.
- **Checkpoint:** Updating an asset's status immediately reflects in the next GET (no stale cache); `redis-cli TTL` shows expiry counting down.

### DAY 59 · Mon, 02 Nov 2026 — MinIO Setup + Invoice Upload ⬜
- **Build:** MinIO in compose. Bucket `asset-invoices` on startup. `io.minio:minio` client. `POST /assets/{id}/invoice` accepting `MultipartFile`, upload via `putObject`, objectKey = assetId + "/" + UUID + "-" + filename. Nullable `invoice_object_key` column (migration).
- **Study:** GC algorithms — Serial vs Parallel vs G1 vs ZGC.
- **Checkpoint:** Uploading a PDF stores an object in MinIO (visible at :9001) and the asset row has a non-null `invoice_object_key`.

### DAY 60 · Tue, 03 Nov 2026 — Download / Presigned URLs + Device Photos ⬜
- **Build:** `GET /assets/{id}/invoice-url` returning a presigned URL (`getPresignedObjectUrl`, 10-min expiry). Repeat upload for `POST /assets/{id}/photo` into `asset-photos` bucket. Content-type whitelist + max size check.
- **Study:** Heap sizing flags — `-Xms`/`-Xmx`, `MaxMetaspaceSize`, container-aware JVM (cgroup limits).
- **Checkpoint:** Hitting `invoice-url` returns a URL that downloads the PDF and expires after 10 min.

### DAY 61 · Wed, 04 Nov 2026 — WebSocket/STOMP Setup + Live Asset Status Push ⬜
- **Build:** `spring-boot-starter-websocket`. `WebSocketConfig implements WebSocketMessageBrokerConfigurer`: `/ws` endpoint (+ SockJS), simple broker on `/topic`, app prefix `/app`. In `AssetService`, after status change, `convertAndSend("/topic/assets/" + assetId, statusEvent)`. Throwaway HTML/JS test page subscribing to `/topic/assets/{id}`.
- **Study:** Stack memory & `StackOverflowError` — per-thread stack, `-Xss`.
- **Checkpoint:** Changing an asset's status via REST triggers a message in the subscribed test client within ~1s.

### DAY 62 · Thu, 05 Nov 2026 — Live Notifications Feed + Secured STOMP ⬜
- **Build:** `@MessageMapping("/notifications.ack")` handler for client acks. `/topic/notifications/{userId}` fed by a Kafka consumer republishing Saga/asset events as STOMP. `ChannelInterceptor` on STOMP `CONNECT` validating the JWT from STOMP headers, rejecting unauthenticated. Restrict per-user topics.
- **Study:** `OutOfMemoryError` types — heap space, Metaspace, GC overhead, unable to create native thread.
- **Checkpoint:** Unauthenticated STOMP CONNECT rejected; authenticated user only receives notifications for their own user id (test with two sessions).

### DAY 63 · Fri, 06 Nov 2026 — Review + Consolidate WebSockets (light day) ⬜
- **Build:** Re-read `WebSocketConfig`, the status publisher, notification interceptor end-to-end. Fix flaky reconnect in the test client. Confirm STOMP JWT check doesn't silently pass on missing header. Tidy topic naming.
- **Study:** Recap Days 57–62 heap/stack/GC/OOM notes; read one real GC log line.
- **Checkpoint:** WebSocket demo survives server restart + client reconnect without manual refresh; topic naming consistent.

### DAY 64 · Sat, 07 Nov 2026 — Spring GraphQL Setup + Queries ⬜
- **Build:** `spring-boot-starter-graphql` in asset-service. `schema.graphqls` with `type Asset`, `type Query { asset(id: ID!): Asset, assetsByEmployee(employeeId: ID!): [Asset!]! }`. `@Controller` with `@QueryMapping` delegating to `AssetService` (reusing `@Cacheable`). Test in GraphiQL at `/graphiql`.
- **Study:** Java references — strong/soft/weak/phantom; `WeakHashMap`/`SoftReference`.
- **Checkpoint:** A GraphQL query for `assetsByEmployee(employeeId: X) { id name status }` returns the same data as the REST endpoint.

### DAY 65 · Sun, 08 Nov 2026 — GraphQL Mutation + N+1 Awareness ⬜
- **Build:** `type Mutation { updateAssetStatus(id: ID!, status: String!): Asset! }` via `@MutationMapping` calling the same service method (cache stays consistent). Nested field `assignedEmployee: Employee` via `@SchemaMapping` calling the Feign client — observe N+1 in logs. Optionally batch with `BatchLoader`/`DataLoader`.
- **Study:** Memory leaks in Spring apps — unbounded caches, uncleared `ThreadLocal`, listener/subscriber refs.
- **Checkpoint:** `updateAssetStatus` mutation changes the DB and reflects in both REST and GraphQL; N+1 evidence noted.

### DAY 66 · Mon, 09 Nov 2026 — gRPC Proto Definition + Server ⬜
- **Build:** Pick the simplest Feign call (asset → user-auth `getEmployeeById`). `protobuf-gradle-plugin`/`protobuf-maven-plugin` + `grpc-spring-boot-starter` in user-auth-service. `employee.proto`: `service EmployeeService { rpc GetEmployee(EmployeeRequest) returns (EmployeeResponse); }`. Implement `EmployeeGrpcService`, gRPC port 9090.
- **Study:** JVM profiling tools — Java Flight Recorder (`jcmd JFR.start`), `jstat -gcutil`.
- **Checkpoint:** `grpcurl -plaintext localhost:9090 list` shows `EmployeeService`; `GetEmployee` returns a valid response.

### DAY 67 · Tue, 10 Nov 2026 — gRPC Client Integration + Benchmark vs REST ⬜
- **Build:** `grpc-client-spring-boot-starter` in asset-service, configure channel to `user-auth-service:9090`. Inject blocking stub via `@GrpcClient`, replace the Feign call from Day 65's resolver. Keep old Feign client behind a flag. Manual benchmark: 100 calls REST/Feign vs gRPC.
- **Study:** Heap dump analysis — `jcmd GC.heap_dump`, Eclipse MAT/VisualVM dominator tree.
- **Checkpoint:** `assignedEmployee` resolves via gRPC end-to-end; one-line benchmark note in project notes.

### DAY 68 · Wed, 11 Nov 2026 — Testcontainers: Postgres Integration Tests ⬜
- **Build:** `testcontainers`, `testcontainers-postgresql`, `testcontainers-junit-jupiter` in asset-service test scope. `AssetRepositoryIT` with `@Testcontainers` + `@Container static PostgreSQLContainer`, wired via `@DynamicPropertySource`. Test the cached hot path query + one DB-constraint test (duplicate insert rejected).
- **Study:** Escape analysis & JIT compilation — C1 vs C2 tiers, inlining, stack allocation.
- **Checkpoint:** `mvn test -Dtest=AssetRepositoryIT` spins up a real Postgres container, passes, unique-constraint test fails correctly against real DB.

### DAY 69 · Thu, 12 Nov 2026 — Testcontainers: Kafka Saga Integration Test ⬜
- **Build:** `testcontainers-kafka`. `AssetAssignmentSagaIT` with `@Container static KafkaContainer`, override `spring.kafka.bootstrap-servers` via `@DynamicPropertySource`. Publish initial "assign" command, poll (test `KafkaConsumer` or `Awaitility`) for success/compensation event. Cover happy path + compensating path.
- **Study:** Connection pool & thread pool sizing under memory pressure — HikariCP `maximumPoolSize` vs heap.
- **Checkpoint:** `AssetAssignmentSagaIT` passes against real Kafka in a container, both paths, using `Awaitility` not `Thread.sleep`.

### DAY 70 · Fri, 13 Nov 2026 — Review + Consolidate + Fix Flaky Tests (light day) ⬜
- **Build:** Run the full suite 3× in a row, note intermittent failures (timing, container not ready). Fix root causes, not retries. Confirm no leaked containers (`docker ps` clean). Consolidate `@DynamicPropertySource` into a shared `AbstractIntegrationTest`.
- **Study:** Recap Days 66–69 profiling/heap dumps/pool sizing.
- **Checkpoint:** Full test suite passes 3 consecutive runs with zero flakes; `docker ps` clean afterward.

### DAY 71 · Sat, 14 Nov 2026 — Testcontainers: Redis IT + Full End-to-End Flow Test ⬜
- **Build:** Testcontainers Redis. `AssetCacheIT` asserting DB→cache→evict behavior against real Redis. `FullAssetLifecycleIT` chaining Postgres + Kafka + Redis containers: create → assign (Saga) → cached read → update status → verify eviction.
- **Study:** Native memory tracking (NMT) and off-heap — `-XX:NativeMemoryTracking=summary`, Netty/direct ByteBuffers.
- **Checkpoint:** `FullAssetLifecycleIT` passes standalone using only Testcontainers, no external services.

### DAY 72 · Sun, 15 Nov 2026 — JVM/Memory Wrap-up + Phase Consolidation ⬜
- **Build:** Final smoke pass across all Phase 3 features in one session. Write a one-page "capacity planning checklist": expected heap per service, Redis maxmemory policy, Kafka consumer footprint, connection pool sizes.
- **Study:** Capacity planning heuristics — `-Xmx` as a function of container memory, GC pause-time goals.
- **Checkpoint:** Every Phase 3 feature works in one continuous run-through; capacity-planning checklist written.

---

# Phase 4 — AI Layer & Polish
**Weeks 11–13 · Days 73–90** — RAG (pgvector), agentic tool-calling, streaming chat, final demo & README.

### DAY 73 · Mon, 16 Nov 2026 — ai-service scaffolding ⬜
- **Build:** New Spring Boot `ai-service` (Web, WebSocket, Validation, Actuator, Config Client, Eureka). Add `com.anthropic:anthropic-java` SDK. Register with Eureka/Config. Add to compose. Route `/api/ai/**` through the gateway (JWT filter reused).
- **Study:** Spring Cloud Gateway route predicates/filters; `@RefreshScope`/config-server binding `anthropic.api-key` from env.
- **Checkpoint:** ai-service registers in Eureka, shows in gateway routes, `docker-compose up` brings it up healthy.

### DAY 74 · Tue, 17 Nov 2026 — first Claude API call from Java ⬜
- **Build:** `AnthropicConfig` building `AnthropicClient` via `AnthropicOkHttpClient.fromEnv()` (reads `ANTHROPIC_API_KEY`). `ChatController` `POST /api/chat` taking `{message: string}`, calling `client.messages().create(...)` with `Model.CLAUDE_OPUS_4_8`, maxTokens 1024, returning the first text block. Input validation + typed exception handler for SDK exceptions.
- **Study:** `@ControllerAdvice`/`@ExceptionHandler`; Claude API basics — `Message.content()` is a list of blocks, check block type before `.text()`.
- **Checkpoint:** `curl POST localhost:8080/api/ai/chat -d '{"message":"hello"}'` (through gateway) returns a real Claude Opus 4.8 response as JSON.

### DAY 75 · Wed, 18 Nov 2026 — pgvector setup ⬜
- **Build:** `pgvector/pgvector:pg16` service in compose. `ai_documents` table: id, source, chunk_text, embedding vector(1536), metadata jsonb, created_at. Enable `vector` extension via migration. Spring Data JPA (or JDBC) entity + repository for `AiDocument`.
- **Study:** What an embedding vector is; cosine similarity; pgvector index types (`ivfflat` vs `hnsw`) conceptually.
- **Checkpoint:** `docker-compose up` starts pgvector, migration runs clean, `SELECT * FROM ai_documents;` works.

### DAY 76 · Thu, 19 Nov 2026 — document ingestion pipeline ⬜
- **Build:** 3–5 short markdown/text docs (asset use policy, warranty terms, offboarding/return policy). `IngestionService`: read files from `resources/policy-docs/`, chunk (~500 tokens, ~50 overlap, split on paragraphs), insert unembedded rows (embedding = null). One-off `POST /api/ai/admin/ingest` (admin-only) to trigger.
- **Study:** Why chunk size/overlap matters for retrieval quality; a plain Java sliding-window chunker.
- **Checkpoint:** Hitting `/api/ai/admin/ingest` populates `ai_documents` with dozens of chunked rows, embedding still null.

### DAY 77 · Fri, 20 Nov 2026 — embeddings ⬜
- **Build:** Add an embedding provider — Voyage AI API (Anthropic's recommended embeddings partner); call `voyage-3` via `RestClient`, one call per chunk (or batched). Store returned float vector into `embedding` via pgvector's Java binding. Keep embedding client behind an `EmbeddingProvider` interface.
- **Study:** Why embeddings are provider-specific (not from the Messages API); batching to cut latency/cost.
- **Checkpoint:** Every `ai_documents` row has a non-null `embedding`.

### DAY 78 · Sat, 21 Nov 2026 — similarity search + retrieval ⬜
- **Build:** pgvector `ivfflat`/`hnsw` index on `embedding` with `vector_cosine_ops`. `RetrievalService.search(String query, int topK)`: embed the query, `SELECT chunk_text, source FROM ai_documents ORDER BY embedding <=> :queryVector LIMIT :topK`. `POST /api/ai/retrieve-test` to eyeball results.
- **Study:** pgvector operators — `<=>` (cosine), `<->` (L2), `<#>` (inner product); cosine is the safe default for Voyage.
- **Checkpoint:** Querying "what's the laptop warranty period" returns the warranty-terms chunk as the top result.

### DAY 79 · Sun, 22 Nov 2026 — REVIEW / consolidate (light day) ⬜
- **Build:** No new features. Re-read ai-service end to end (config → chat → ingestion → embeddings → retrieval). Fix rough edges (null checks on embedding failures, retry/backoff around Voyage, confirm ingest is role-gated). Short internal README for ai-service.
- **Study:** Light — Spring `RestClient` retry/backoff + Anthropic SDK `max_retries` behavior.
- **Checkpoint:** ai-service builds clean, all endpoints from days 73–78 work through a full compose run, recap notes written.

### DAY 80 · Mon, 23 Nov 2026 — RAG-aware chat endpoint ⬜
- **Build:** Update `POST /api/chat`: retrieve top-K (e.g. 4) chunks via `RetrievalService`, build a system prompt ("Answer using only the following context. If the answer isn't in the context, say so." + chunks), call `client.messages().create(...)` with that system prompt + the user question. Return the answer and the source chunks.
- **Study:** Prompt structure — system prompt vs user message; grounding instructions reduce hallucination.
- **Checkpoint:** "What's the warranty on a laptop" returns an answer matching the ingested doc; an out-of-docs question gets an honest "I don't have that information."

### DAY 81 · Tue, 24 Nov 2026 — RAG polish + citations ⬜
- **Build:** Extend chat response DTO with `sources: [{document, excerpt}]`. Handle the no-relevant-chunks case (cosine distance above threshold → skip RAG). Minimal integration test: ingest a known doc, ask a question, assert the answer references the right source.
- **Study:** Light — cosine-distance thresholds as a relevance heuristic.
- **Checkpoint:** A policy-question test passes automatically; chat response includes which source doc(s) backed the answer.

### DAY 82 · Wed, 25 Nov 2026 — agentic tools: schema + service client ⬜
- **Build:** `AssetServiceClient` (`RestClient`/`WebClient`) calling the assets/assignments services via the gateway, forwarding the caller's JWT. Claude tool schema for `get_assets_by_employee`: `input_schema` with `employee_id` (string, required), `strict: true`. Tool's Java handler calls the client, returns JSON with serials + count.
- **Study:** Claude tool_use flow — Claude returns `stop_reason: "tool_use"`, you execute the tool, send back a `tool_result` with matching `tool_use_id`; loop until `stop_reason: "end_turn"`.
- **Checkpoint:** Calling the tool handler directly (unit test, no LLM) returns correct asset data for a known employee ID.

### DAY 83 · Thu, 26 Nov 2026 — tool-calling loop (manual) ⬜
- **Build:** `AgentService.chat(String userMessage)`: maintain a `messages` list, call `client.messages().create(...)` with `tools=[get_assets_by_employee]`, loop while `response.stopReason() == TOOL_USE` — dispatch each `tool_use` block, append `tool_result` blocks in a single user message, call again. Cap iterations (`max_continuations = 5`). Wire `/api/chat` to use `AgentService` when the question needs live data.
- **Study:** Why multiple tool_use results in one turn must be returned together (parallel tool use semantics).
- **Checkpoint:** "What assets does employee E123 have — serial numbers and count" through `/api/chat` triggers a real tool call and returns a correct natural-language answer built from live data.

### DAY 84 · Fri, 27 Nov 2026 — second + third tools; combine with RAG ⬜
- **Build:** Add `get_warranties` (input: asset_id or employee_id) and `search_assets` (input: free-text query, category filter) tools, each with its own handler. Register all three tools + the RAG-context system prompt on the same `messages.create()` call — Claude decides per-question whether to pull policy context, call a tool, both, or neither. Make tool descriptions prescriptive about *when* to call.
- **Study:** Tool description quality drives trigger rate — tighten "when to use" language.
- **Checkpoint:** All three tools callable from `/api/chat`; a mixed question ("what's the warranty policy, and does employee E123 have anything out of warranty") triggers both a policy lookup and a tool call.

### DAY 85 · Sat, 28 Nov 2026 — multi-step agentic questions ⬜
- **Build:** Harden the loop for multi-hop questions (list all employees' asset counts → find max → call getWarranties for that employee's assets). Confirm the loop handles multiple round-trips (increase max_continuations, log each tool call). Add a `search_assets`-backed "list all employees with assets" path if needed.
- **Study:** Light — how `output_config.effort`/thinking affects tool-use depth for complex multi-step questions.
- **Checkpoint:** "Who has the most assets, and are any of them out of warranty?" produces a correct multi-step answer, visibly using 2+ sequential tool calls in the logs.

### DAY 86 · Sun, 29 Nov 2026 — REVIEW / consolidate (light day) ⬜
- **Build:** No new features. Review the full agent loop, tool handlers, and error paths (tool call fails → `is_error: true` returned so Claude can recover). Add rate-limit/timeout handling around Claude calls (confirm SDK `max_retries` sane). Clean up logging so each tool call + result is traceable.
- **Study:** Light — error-handling chain (catch specific Anthropic SDK exceptions before the base class).
- **Checkpoint:** A forced tool failure (bad employee ID) results in a graceful natural-language response from Claude, not a 500.

### DAY 87 · Mon, 30 Nov 2026 — streaming over WebSocket (STOMP) ⬜
- **Build:** STOMP handler `@MessageMapping("/chat.send")` in ai-service that calls `client.messages().stream(...)`/`.streamRaw()` instead of `.create()`. On each `text_delta` event, push a partial-message frame via `convertAndSendToUser(...)` on a per-session destination (`/queue/chat-reply`). On message end, send a final "done" frame. Handle tool_use blocks mid-stream (buffer, execute, continue the loop).
- **Study:** Spring STOMP `@MessageMapping` + `SimpMessagingTemplate` server-push; `@SendTo` (broadcast) vs `convertAndSendToUser` (targeted).
- **Checkpoint:** Connecting a STOMP client and sending a chat message shows the reply arriving incrementally, not all at once.

### DAY 88 · Tue, 01 Dec 2026 — role-based access via gateway/JWT ⬜
- **Build:** Confirm the gateway's JWT filter extracts roles/claims. Add a route-level role check (`ROLE_IT_ADMIN`) on `/api/ai/**` (both the REST chat endpoint and the WebSocket handshake — STOMP CONNECT frames need JWT validated via a `ChannelInterceptor`). Reject unauthorized with a clear 403 before any Claude call. Add a test: non-admin JWT rejected, admin JWT succeeds.
- **Study:** Spring Security `ChannelInterceptor` for STOMP CONNECT-time auth — why WS auth needs its own hook.
- **Checkpoint:** A non-admin JWT is rejected on both the REST and WebSocket chat paths with no Claude API call; an admin JWT works end-to-end with streaming.

### DAY 89 · Wed, 02 Dec 2026 — end-to-end hardening ⬜
- **Build:** `docker-compose up` full stack, manually exercise: RAG policy question, single-tool question, multi-step agentic question, streaming reply, unauthorized rejection. Fix full-stack breaks (discovery timing, env wiring, route ordering). Add health checks for ai-service's Anthropic + pgvector connectivity to `/actuator/health`. Tighten `application.yml` (no secrets committed, sane timeouts, streaming `max_tokens`).
- **Study:** Light — Spring Boot Actuator custom `HealthIndicator`s.
- **Checkpoint:** A clean `docker-compose up` from scratch supports the full "ask about my assets" flow through the gateway with JWT, streamed over WebSocket, grounded in RAG + tools.

### DAY 90 · Thu, 03 Dec 2026 — FINALE: demo, README, resume writeup, buffer ⬜
- **Build:** (1) Full end-to-end demo script: login → JWT → "what assets does employee X have" (tool-use) → policy question (RAG) → multi-step question (multi-tool) → watch it stream. (2) Top-level README: architecture diagram, setup, "why these choices". (3) 3–5 resume bullets quantifying the build. (4) Buffer/catch-up for anything from days 73–89 still rough.
- **Study:** Light — system-design overview; re-skim your own architecture as if explaining it in an interview.
- **Checkpoint:** The full demo runs clean start to finish on a fresh `docker-compose up`, README accurately describes the running system, resume bullets written and saved.
