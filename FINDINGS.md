# IT Asset Management — Findings & State of the System

> Daily-review snapshot of what's built, how it works, and what's next.
> Read this first before diving into files. Generated 2026-09-22. Project ≈ **Day 32 of 90**.

---

## 0. TL;DR

A Spring Boot 4.1.1 / Java 21 / Spring Cloud 2025.1.3 microservices platform for tracking IT
assets (laptops, monitors, etc.), who holds them, and their lifecycle. Started as a monolith,
now mid-way through being split into microservices.

**Four running modules today:**

| Service | Port | Role | DB |
|---|---|---|---|
| `discovery-server` | 8761 | Netflix Eureka registry | — |
| `config-server` | 8888 | Spring Cloud Config (native file-based) | — |
| `user-auth-service` | 8081 | Users, roles, JWT issuance + refresh tokens | `userdb` |
| `assetservice` | 8082 (yaml) / 8080 (compose) | Assets, categories, locations, assignments; JWT **validator** | `assetdb` |

**Auth model:** `user-auth-service` *signs* JWTs; `assetservice` *validates* them — both share `JWT_SECRET`.
Access token (JWT, 1h) in response body; refresh token (opaque UUID, 7d) in httpOnly cookie.

**Not built yet:** API gateway, true DB-per-service in compose, observability, resilience, Kafka,
Redis, the whole Phase 4 AI/RAG layer. See §7.

---

## 1. Architecture

```
                         ┌────────────────────────┐
                         │  discovery-server       │  Eureka registry, :8761
                         │  (@EnableEurekaServer)  │  ECS-JSON structured logs
                         └───────────┬────────────┘
                          register / │ lookup by name
             ┌───────────────────────┼────────────────────────┐
   ┌─────────▼────────┐   ┌──────────▼─────────┐    ┌──────────▼─────────┐
   │ config-server    │   │ user-auth-service  │    │ assetservice        │
   │ :8888 native FS  │   │ :8081  JWT ISSUER  │    │ :8082  JWT VALIDATOR │
   │ serves *.yml     │   │                    │    │                      │
   └─────────┬────────┘   └──────────┬─────────┘    └──────────┬─────────┘
   config pull on boot ──────────────┴──── same JWT_SECRET ────┘
   (spring.config.import=configserver, fail-fast)
                                     │
                          ┌──────────▼──────────┐
                          │ postgres :5432       │  compose: ONE shared DB (itasset)
                          │  userdb / assetdb    │  plan wants 2 separate containers
                          └─────────────────────┘
```

- **Discovery:** business services register in Eureka, resolve each other by logical name.
- **Config:** each service imports config from config-server on boot; `fail-fast: true` means
  **a service won't start if config-server is unreachable** (after 6 retries).
- **Auth:** stateless JWT validation, no DB lookup on the hot path (see §3, §4).
- **DB-per-service:** intended (`userdb`, `assetdb`), but committed `docker-compose.yml` still
  points both at one shared `itasset` Postgres with separate tables. See §5 + §6.

---

## 2. Tech stack

- **Runtime:** Java 21, Spring Boot **4.1.1**, Spring Cloud **2025.1.3** (note: Spring Boot 4 line —
  uses `spring.config.import`, no `bootstrap.yml`; artifacts are `-webmvc`/`-webmvc-test`).
- **Persistence:** PostgreSQL, Spring Data JPA, `ddl-auto: update` (no Flyway yet).
- **Security:** Spring Security, JWT via `jjwt 0.12.6` (HS256), BCrypt password hashing.
- **Docs:** springdoc-openapi 3.1.1 → Swagger UI at `/swagger-ui.html` (whitelisted).
- **Build/ship:** multi-stage Dockerfiles (`maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jre-alpine`,
  non-root `app` user), GitLab CI → Harbor registry.

---

## 3. user-auth-service (`:8081`, `com.itasset.userauth`)

Owns users, roles, JWT issuance, and the refresh-token lifecycle. Extracted from the monolith on Day 31.

### Domain
- **User** (`users` table): `id`, `fullName`, `email` (unique), `username` (unique),
  `passwordHash` (BCrypt, never exposed in any DTO), `department`, `role` (enum, stored by name).
- **RefreshToken** (`refresh_tokens`): `id`, `token` (opaque UUID, unique — *not* a JWT),
  `username`, `expiresAt`.
- **Role enum:** exactly two — `ADMIN("Administrator")`, `EMPLOYEE("Employee")`.

### API
| Method | Path | Auth | Behavior |
|---|---|---|---|
| POST | `/api/auth/register` | public | Creates user, **forces `EMPLOYEE`**, hashes pw, issues access + refresh cookie. 201. |
| POST | `/api/auth/login` | public | Verifies pw via `passwordEncoder.matches`; bad creds → 401. Issues access + refresh. |
| POST | `/api/auth/refresh` | cookie | Rotates refresh token, re-issues access. Missing cookie → 401. |
| POST | `/api/auth/logout` | cookie | Revokes refresh token server-side, clears cookie. 204. |
| GET | `/api/users/me` | valid JWT | Returns caller's profile (from JWT subject). |

- `AuthResponse` = `{accessToken, username, role}` — **refresh token is never in the body**, only the cookie.
- `UserResponse` = all fields **except** `passwordHash`.
- **No user CRUD**, no admin-promotion endpoint. Register hardcodes `EMPLOYEE` ("admins promoted later").

### Auth mechanics
- **Access JWT:** HS256, subject=username, `role` claim, exp = now + `jwt.expiration-ms` (1h). Secret from config.
- **Refresh cookie:** `refreshToken`, `httpOnly`, `secure(false)` (⚠ flip to true behind HTTPS),
  `path=/api/auth`, `SameSite=Lax`, maxAge = seconds to expiry.
- **TTLs:** access 1h (`3600000`), refresh 7d (`604800000`).
- **Rotation (`RefreshTokenService`):** `rotate()` **deletes old row then creates new** (one-time use).
  No reuse-detection lineage — a replayed old token just 401s "Invalid refresh token".
- **Revocation:** only refresh tokens are revocable (DB-backed). Access tokens are **not** — a stolen
  access token stays valid its full 1h regardless of logout (standard access/refresh tradeoff, deferred).

### Security wiring
- `SecurityConfig`: stateless, CSRF off, `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`.
- Public: `/api/auth/**`, `/api/hello`, swagger. Everything else authenticated.
- `JwtAuthFilter`: reads `Authorization: Bearer`, validates signature+expiry (no DB), sets
  `ROLE_<role>` authority. `@EnableMethodSecurity` on but no `@PreAuthorize` used yet.

### Exceptions & tests
- `GlobalExceptionHandler`: validation → 400 (field map); `DuplicateResourceException` → 409.
  (Its Javadoc still references monolith fields `assetTag`/`serialNumber` — stale copy-paste.)
- Tests: `AuthControllerTest` (login happy/401×2), `UserServiceTest` (real BCrypt, dup → 409),
  `JwtUtilTest` (roundtrip/tamper/expiry), `UserRepositoryTest`.
- ⚠ **`UserRepositoryTest` expects a seeded `admin` "Alice Admin" ADMIN user, but NO seeding exists**
  (no `data.sql`, no `CommandLineRunner`). This test fails as-is. Gap to fix.

---

## 4. assetservice (`:8082`, `com.itasset.assetservice`)

Owns the asset catalog, categories, locations, and the assignment lifecycle. Validates JWTs
minted by user-auth-service. Stores only `userId` references (users live in the other service).

### Domain
- **Asset** (`asset`): `id`, `assetTag`, `name`, `serialNumber`, `@ManyToOne category`,
  `@ManyToOne location`, `status` (enum). No entity-level unique constraints (uniqueness enforced
  only in service code — see §8 race note).
- **Assignment** (`assignments`, extends `Auditable`): `id`, `@ManyToOne asset`, `userId` (no FK —
  cross-service), `assignedAt`, `returnedAt` (**null = still out**, the "open assignment" sentinel),
  `assignedBy`, `notes`.
- **Category**: `id`, `name`, `description`. **Location**: `id`, `building`, `floor`, `room` (strings).
- **Auditable** (`@MappedSuperclass`): `createdAt/By`, `updatedAt/By` — auto-filled from SecurityContext
  (`"system"` when unauthenticated, e.g. seeding). **Only Assignment is audited.**
- **AssetStatus** — 5-value state machine with `canTransitionTo()`:
  `ACTIVE ↔ DEPLOYED`, `ACTIVE/DEPLOYED → IN_REPAIR/RETIRED`, `IN_REPAIR → ACTIVE/RETIRED`,
  `RETIRED → DISPOSED`, `DISPOSED → {}` (terminal). One-way toward DISPOSED.

### API
**AssetController** `/api/assets`:
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/assets` | ADMIN | create (201); rejects dup assetTag/serialNumber (409) |
| GET | `/api/assets?status=&categoryId=&page=&size=&sort=` | any auth | paged, filterable → `Page<AssetResponse>` |
| GET | `/api/assets/stats/by-category` | any auth | `{category: count}`, single GROUP BY |
| GET | `/api/assets/{id}` | any auth | 404 if missing |
| PUT | `/api/assets/{id}` | ADMIN | full update |
| DELETE | `/api/assets/{id}` | ADMIN | 204 |

**AssignmentController** `/api/assignments`:
| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/assignments` | ADMIN | assign; records `principal` as `assignedBy` (201) |
| POST | `/api/assignments/{id}/return` | ADMIN | return |

**AssignmentQueryController** (no `@PreAuthorize` — any authed user reads):
- `GET /api/assets/{id}/assignments` — asset history (newest first)
- `GET /api/users/{id}/assignments` — user history
- `GET /api/employees/{id}/assets` — **flagship**: what an employee currently holds (`EmployeeAssetsResponse`)

**HelloController**: `GET /api/hello` → `"Asset Service is alive"` (public).

### Service logic
- **AssetService:** dup-guard on create/update (only re-checks when value changed — avoids self-collision);
  `countByCategory` (null → `"(uncategorized)"`); 404s on missing.
- **AssignmentService** (`@Transactional` — two writes, all-or-nothing):
  - `assignAsset`: load asset → reject if open assignment exists (409) → check `canTransitionTo(DEPLOYED)`
    (409) → create Assignment + flip asset to `DEPLOYED`.
  - `returnAsset`: reject if already returned (400) → stamp `returnedAt` + flip asset to `ACTIVE`.
  - `employeeAssets`: returns open assignments as `AssetSummary` (carries `assignmentId` for return);
    employee name is a placeholder `"user #<id>"` (no cross-service lookup yet).

### Repositories & search
- `AssetRepository`: `JpaSpecificationExecutor` + derived queries + GROUP BY stats query.
- `AssignmentRepository`: `existsByAssetIdAndReturnedAtIsNull`, history + open-assignment finders.
- `AssetSpecifications.filter(status, categoryId)`: dynamic WHERE; `LEFT JOIN FETCH` category+location
  on the entity query but **not** the count query (correct N+1 fix that stays paging-compatible).

### Security, mapper, exceptions
- Same JWT filter/config as user-auth (validates only, no DB). ADMIN gates all mutations; reads need any JWT.
  `BCryptPasswordEncoder` bean exists but is unused (no local login).
- **AssetMapper** (`@Component`) is the only mapper — `toEntity`/`toResponse` with repo lookups (404 on
  bad category/location id). `AssignmentResponse.from()` is a separate static-factory style (two patterns coexist).
- `GlobalExceptionHandler`: validation → 400, `ResourceNotFound` → 404, `Duplicate`/`AssetNotAvailable` → 409,
  `InvalidAssignmentState` → 400. No catch-all 500 handler.

### Seeding & tests
- **DataSeeder** (`CommandLineRunner`, idempotent via `count()==0`): 5 categories, 3 locations,
  10 assets `AST-001…AST-010`. **No users** (moved out). Stale comment about which statuses it cycles.
- Tests: `AssetServiceTest`, `AssignmentServiceTest`, `AssetStatusTest`, `JwtUtilTest`, `AssetMapperTest`,
  + `contextLoads()` (⚠ needs live config-server + DB, brittle in CI). No `@WebMvcTest`/`@DataJpaTest`,
  no ADMIN-vs-EMPLOYEE 403 integration test.

---

## 5. Infrastructure services

### config-server (`:8888`)
- `@EnableConfigServer`, **native file-based** backend (`spring.profiles.active: native`,
  search-location `classpath:/config`). Serves three files from its own classpath:
  - `application.yml` (shared): Eureka registry URL only.
  - `asset-service.yml`: `server.port: 8082` + `jwt.*` block.
  - `user-auth-service.yml`: `server.port: 8081` + same `jwt.*` block (shared secret is intentional).
- ⚠ Served profile name is `asset-service` (hyphen) but the module/container is `assetservice`.
- Prod intent: switch to a private **git** backend + `{cipher}` encryption (Day 30/41, PLANNED).

### discovery-server (`:8761`)
- `@EnableEurekaServer`, `register-with-eureka: false`, `fetch-registry: false`.
- ECS-structured JSON logs to stdout (for FluentBit/VictoriaLogs).
- ⚠ Shared Eureka `defaultZone` = `http://localhost:8761` — correct for host-local runs but **wrong
  inside Docker** (should be `http://discovery-server:8761`); cross-network registration not wired.

---

## 6. Orchestration, CI/CD, secrets

### docker-compose.yml — **5 containers** (default bridge network, 1 named volume `pgdata`)
| Service | Ports | Depends on |
|---|---|---|
| `postgres:17` | 5432 | healthcheck `pg_isready` |
| `discovery-server` | 8761 | — |
| `config-server` | 8888 | discovery-server |
| `user-auth-service` | 8081 | postgres (healthy) + config-server |
| `assetservice` | **8080** (host) | postgres (healthy) |

- ⚠ **Not yet true DB-per-service:** one shared `postgres` (`itasset`), both services get the same
  `SPRING_DATASOURCE_URL`. Plan wants `postgres-user` + `postgres-asset`.
- ⚠ assetservice publishes host **8080** but its served config says `server.port: 8082` — reconcile
  before a clean `docker-compose up`.
- No gateway, Redis, Kafka, Zipkin, MinIO, or pgvector containers.

### CI/CD (`.gitlab-ci.yml`)
- Runs only on `main`. Single `deploy` stage, one job per service, **gated by `changes:`** (a service
  builds only when its own files change).
- Maven runs **inside** each Dockerfile → runner needs only Docker (no host JDK/Maven).
- Pushes to **Harbor** `harbor.neusix.ai/micro-services/<service>`, tags `<short-sha>` + `latest`.
  Robot login `'robot$micro-services+it-asset-management-git'` (single-quoted to preserve `$`).
- **Dual remote:** repo pushed to both GitHub and GitLab; GitLab runs the pipeline.

### Secrets (`SECRETS.txt` + `.env`)
- **Model:** OpenBao (KV v2, `secret/it-asset/<service>`) is source of truth; in-cluster ESO syncs
  OpenBao → K8s Secrets → env. Locally, docker-compose reads the same var names from a gitignored `.env`.
- **WIRED:** postgres creds, `SPRING_DATASOURCE_*`, `JWT_SECRET`.
  **PLANNED:** Eureka dashboard auth, config-server auth, `{cipher}`/`ENCRYPT_KEY`, git config backend.
- ⚠ **`.env` key scheme is out of sync with compose:** `.env` supplies `DB_HOST`/`USERAUTH_DB_*`/`ASSET_DB_*`
  (pointing at an office Postgres `10.6.4.99:6666`), but compose interpolates `POSTGRES_*`/`SPRING_DATASOURCE_*`.
  `.env` also holds a real DB password + real base64 JWT secret in plaintext. Reconcile.

---

## 7. What is NOT built yet

- **API Gateway** — none. No `gateway/` module, no single public entry point, no gateway-level JWT
  filter or rate limiting. Services are directly exposed.
- **True DB-per-service** — compose still shares one Postgres.
- **Observability** — no tracing (Zipkin/Micrometer), no metrics (Prometheus/Grafana), no correlation
  IDs. Only discovery-server emits structured logs. No readiness/liveness probes.
- **Resilience** — no Resilience4j, no OpenFeign inter-service calls.
- **Messaging** — no Kafka, no audit-service, no Saga.
- **Phase 3 stack** — no Redis, MinIO, WebSocket/STOMP, GraphQL, gRPC, Testcontainers.
- **Phase 4 AI** — no ai-service, pgvector, Anthropic/Voyage, RAG, or agentic tooling.
- **Security hardening** (all PLANNED) — Eureka dashboard open, config-server open, no encryption,
  no TLS, no HA (single config/discovery instance), no Flyway (still `ddl-auto: update`).

---

## 8. Known bugs / tech debt (priority order)

1. **⚠ Assignment race condition (called out in PROJECT-STRUCTURE as #1):** `existsByAssetIdAndReturnedAtIsNull`
   is a TOCTOU check with no DB constraint backstop — two concurrent assigns can double-assign one asset.
   Fix = optimistic locking (`@Version` + 409) and/or a partial-unique index on open assignments.
2. **Uniqueness only in service code:** `assetTag`/`serialNumber` have no DB unique index — concurrent
   double-create can race past `existsBy…`.
3. **`UserRepositoryTest` fails** — expects a seeded admin that no seeder creates (§3).
4. **`.env` ↔ compose key mismatch** (§6) — env won't wire correctly as written.
5. **assetservice port drift** — 8082 (config) vs 8080 (compose publish).
6. **Eureka `defaultZone` = localhost** — won't work across the Docker network.
7. **No instant access-token revocation** — deferred by design (refresh-token model).
8. **Stale docs/comments:** 90-DAY-PLAN header says "Day 19" (git is Day 32); DataSeeder status comment;
   user-auth `DuplicateResourceException` Javadoc references asset fields.
9. **Dead surface:** `CategoryDto`/`LocationDto` + repos have no CRUD endpoints (seed-only);
   `AssetService.findAll()`, some derived queries unused.

---

## 9. Roadmap (90-day plan)

- **Phases:** 0 Foundations (1–7) ✅ · 1 Core Domain (8–28) ✅ · 2 Microservices & Infra (29–56) ◐ in progress
  · 3 Cross-cutting & Protocols (57–72) · 4 AI Layer & Polish (73–90).
- **Done (≈Days 1–32):** asset CRUD + JPA + validation + exception handling; full domain model +
  pagination/filtering/stats; User/Role + BCrypt + Spring Security + JWT + RBAC + `/users/me`;
  assignment lifecycle + history + employee-assets; refresh-token flow; **Phase 2 start** —
  discovery-server (29), config-server (30), extract user-auth-service (31), extract asset-service +
  containerize both + Dockerfiles + compose + GitLab CI to Harbor + OpenBao/ESO spec (31–32).
- **Next (Days 33+):** Day 33 two Postgres containers · Day 34 gateway module + 6-container compose ·
  Day 35 integration review · 36–41 gateway routing + JWT-at-gateway + Redis rate-limiting + OpenFeign +
  git config backend · 43–49 Resilience4j + tracing + correlation IDs · 50–56 Kafka + audit-service + Saga.
- **Phase 3 (57–72):** Redis cache, MinIO, WebSocket/STOMP, GraphQL, gRPC, Testcontainers.
- **Phase 4 (73–90):** `ai-service` using `com.anthropic:anthropic-java` against **Claude Opus 4.8**,
  RAG over **pgvector** (`vector(1536)`, ivfflat/hnsw) with **Voyage AI (`voyage-3`)** embeddings,
  agentic tool-calling (`get_assets_by_employee`, `get_warranties`, `search_assets`), streaming replies
  over WebSocket/STOMP, `ROLE_IT_ADMIN` gating, final demo/README.

---

## 11. In-flight: Microsoft admin service (Graph API) — started Day 32

A **new microservice** being scoped now: automates Microsoft 365 / Entra admin tasks for the
`neusix.com` tenant (create/disable users, DL & Teams groups, license assignment, Outlook mail,
role grants) via **Microsoft Graph API**. Will act as its own agentic surface later (Phase 4).

### How it authenticates (decided)
- **Client-credentials OAuth** (app logs in *as itself*, no user, no browser) → **no Redirect URI**.
- Power comes from **Graph application permissions + admin consent**, not the login flow.
- App registered in Entra as an **identity/"badge"** for the future service; code doesn't exist yet.

### Entra app registration (DONE — 2026-09-22)
- **Portal:** entra.microsoft.com → Entra ID → App registrations.
- **App name:** `it-asset-mgmt-graph`, single-tenant (My organization only), State: Activated.
- **Tenant (Directory) ID:** `f0c33f0b-551c-4ff0-a953-f4de575a79d6`  *(identifier, not a secret)*
- **Client (Application) ID:** `254f39ff-221a-4cf6-9d79-dd4f6c657d6c`  *(identifier, not a secret)*
- **Client secret:** ✅ created (desc `it-asset-graph`). **Value lives ONLY in gitignored `.env`
  as `GRAPH_CLIENT_SECRET`** (never in git, same model as `JWT_SECRET` §6). Secret ID is just a
  portal label — not stored. Rotate before its 6-month expiry.
- **`.env` keys added:** `GRAPH_TENANT_ID`, `GRAPH_CLIENT_ID`, `GRAPH_CLIENT_SECRET`.
- Registered by Global Admin `bharat.kumar@neusix.com` (Samala Bharat Kumar).

### API permissions (IN PROGRESS)
- Added **Microsoft Graph → Application permissions**: `Mail.Read` + `Mail.Send`.
  (`User.Read` delegated is Microsoft's auto-added default — harmless, unused, ignored.)
- ⚠ **RESUME POINT:** must click **"Grant admin consent for NeuSix"** and confirm both Mail rows show
  **green ✅ "Granted"** in the Status column. Until green = requested only, calls get access-denied.
- Later scopes (add + re-consent when those features are built): `Mail.ReadWrite`, `User.ReadWrite.All`,
  `Group.ReadWrite.All`, `TeamMember.ReadWrite.All`, etc.
- ⚠ App perms are tenant-wide (every mailbox). Scope mail down via **Application Access Policy**
  before pointing at real inboxes.

### Key concepts settled this session
- **Runtime needs NO admin login.** Admin (bharat.kumar) was used only to (1) register the app and
  (2) grant consent. After that the service authenticates **as the app itself** using the 3 `.env`
  values — no human, no MFA, no redirect. The app *is* its own account.
- **No Redirect URI** — client-credentials flow has no browser/user, so none needed.
- **We don't build an API for Graph — Graph IS the API.** Our service is a **middleman**:
  inward face = its own REST (e.g. `POST /api/mail/send`) that sibling services call;
  outward face = a Graph client using `.env` creds. `assetsvc → mail-service → Graph → back`.
- **Microsoft is free for this:** no charge for app registration, Graph calls, or OAuth tokens.
  Only pre-existing M365 license seats cost (unchanged). Throttling (429) is the only limit.

### Deferred decision — "Sign in with Microsoft" (Entra OIDC / SSO)
- Separate feature from the mail service. Would let `@neusix.com` users log into OUR app with their
  Microsoft account instead of the JWT username/password in user-auth-service.
- **Free** on Entra Free (current plan); needs a redirect URI + delegated/OIDC (NOT the app-only flow
  above). P1/P2 premium features not required. Single-tenant → **neusix.com users only** by default.
- Alt already known to user: **Keycloak** (self-hosted OIDC). Same OIDC contract either way, so
  user-auth-service integration is provider-agnostic.
- **DECISION: deferred.** Finish mail service first; add SSO to user-auth-service as its own feature later.

### Still TODO (code side, tomorrow)
- Service module not created yet. Will match sibling services (Spring Boot 4.1.1, config-server
  import, Eureka client, Dockerfile, GitLab CI → Harbor).
- First slice: Graph client-credentials auth wiring (MSAL or raw token endpoint) reading `GRAPH_*`
  from env + one `POST /api/mail/send` endpoint + live test send. Then scope the next 2–3 admin tasks.

> Tenant licenses seen: M365 Business Basic/Standard (include Exchange → mail works), Teams Exploratory,
> Power Automate Free. Unlicensed users have no mailbox — Graph can't send as them.
> Secret was typed in plaintext in chat this session → rotate it once before real use (hygiene).

---

## 10. Reference files

- Plan & vision: `90-DAY-PLAN.md`, `PROJECT-STRUCTURE.md` (PART 3 = production-gap audit).
- Endpoints: `ENDPOINTS-user-auth-service.md`, `ENDPOINTS-assetservice.md`, `ENDPOINTS-config-server.md`,
  `ENDPOINTS-discovery-server.md`, `ENDPOINTS.md` (monolith-era).
- Secrets: `SECRETS.txt`, `.env.example`.
- Orchestration: `docker-compose.yml`, `.gitlab-ci.yml`, per-service `Dockerfile` + `.dockerignore`.
