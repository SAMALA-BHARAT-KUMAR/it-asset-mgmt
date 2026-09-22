# Days 33–35: Containerization, Orchestration & Pre-Gateway Hardening — Design

**Date:** 2026-09-22 · **Scope:** Days 33–35 of the 90-day plan (everything before Day 36 gateway routing) + interlocking bug fixes. **Backend only.**

## Goal

Bring the four-service system up as a self-contained local Docker stack with per-service databases and a
registered (but non-routing) gateway, verified end-to-end — and fix the known defects that would otherwise fail
an automated review (MergeMate). No gateway *routing* yet (that is Day 36).

## Current state (verified 2026-09-22)

- 4 services build; all have Dockerfiles + `.dockerignore`.
- `docker-compose.yml` has ONE shared `postgres` (`itasset`); both services share `SPRING_DATASOURCE_URL`.
- **`.env` is broken vs compose:** it supplies `DB_HOST`/`USERAUTH_DB_*`/`ASSET_DB_*` (office server `10.6.4.99:6666`),
  but compose interpolates `POSTGRES_*` + `SPRING_DATASOURCE_*` → those come out empty. `docker compose up` cannot work as-is.
- Eureka `defaultZone` in config-server shared `application.yml` = `http://localhost:8761` → wrong inside Docker.
- assetservice: served config says `server.port: 8082`, compose publishes `8080` (drift).
- `UserRepositoryTest` expects a seeded `admin` "Alice Admin" (ADMIN) that no seeder creates → test fails.
- `register` hardcodes `EMPLOYEE`; no ADMIN exists → ADMIN-only asset/assign endpoints can't be exercised.
- Assignment assign path has a TOCTOU race (no `@Version`, no DB unique backstop).

## Target state

- **Day 33:** `docker-compose.yml` has `postgres-user` (`userdb`) + `postgres-asset` (`assetdb`), separate volumes.
  Each service points at its own DB. Topology (hostnames/URLs) lives in compose; secrets (`POSTGRES_USER/PASSWORD`,
  `JWT_SECRET`) in `.env`. `.env` + `.env.example` reconciled to this scheme (fixes the mismatch).
- **Day 34:** new `gateway/` Spring Boot module — registers in Eureka + imports config, **no routing**. Ports:
  **gateway → 8080, assetservice → 8082** (fixes drift). config-server gains `gateway.yml`. Eureka `defaultZone`
  made Docker-aware via env override (`http://discovery-server:8761`). CI (`.gitlab-ci.yml`) gains a gateway job.
  Compose = **7 containers** (2 postgres + discovery + config + user-auth + asset + gateway). *(Plan says "six" —
  stale, predates the two-Postgres split. Correct count is 7.)*
- **Day 35:** seed an ADMIN user (`admin`/"Alice Admin", `Role.ADMIN`) in user-auth-service via a guarded
  `CommandLineRunner` — fixes `UserRepositoryTest` AND enables the admin flow. Bring the stack up; verify Eureka
  shows `USER-AUTH-SERVICE`, `ASSET-SERVICE`, `GATEWAY`; run register → login → create asset → assign → employee query.

## Interlocking bug fixes (approved)

- **#1 race (requested):** `@Version` optimistic locking on `Asset` (and `Assignment`); map
  `OptimisticLockingFailureException` → HTTP 409 in `GlobalExceptionHandler`.
- **#2 backstop:** DB unique constraints on `Asset.assetTag` + `Asset.serialNumber`.
- **#3:** admin seeder (also fixes the broken test).
- **#4/#5/#6:** `.env`↔compose reconcile, port drift, Eureka Docker zone (all above).
- **Safe cleanups:** stale comments (DataSeeder status comment, user-auth `DuplicateResourceException` javadoc), only
  clearly-dead code. Conservative; each verified. No speculative refactors before the review gate.

## Agent orchestration (avoids file clobbering)

- **Phase A — 3 parallel agents, disjoint file trees:**
  - A1: create `gateway/` module (all-new files; templates = discovery-server + config-server).
  - A2: `assetservice/` — `@Version` on Asset/Assignment, DB unique constraints, 409 mapping, stale-comment/dead-code cleanup, update/add unit tests (TDD for the lock).
  - A3: `user-auth-service/` — admin `CommandLineRunner` seeder (BCrypt, idempotent), confirm `UserRepositoryTest` passes, fix stale javadoc.
- **Phase B — single writer (main thread):** infra — `docker-compose.yml` (two postgres + gateway + ports + eureka env),
  config-server `application.yml` (Docker-aware zone) + new `gateway.yml`, `asset-service.yml` note, `.env` + `.env.example`, `.gitlab-ci.yml` gateway job.
- **Phase C — verify:** `mvn -q -DskipTests package` per changed service (compile), run unit tests, `docker compose config`,
  then best-effort `docker compose up` smoke + the Day-35 curl flow. Fix fallout.
- **Phase D — review:** `/code-review` on the diff; address findings before declaring done.

## Invariants / constraints

- Keep the house patterns: Spring Boot 4.1.1 / Spring Cloud 2025.1.3 / Java 21; multi-stage Dockerfile (maven build →
  jre-alpine, non-root `app`); `spring.config.import: configserver` + `fail-fast: true`; config-server native backend.
- Secrets never hardcoded in committed files; `.env` stays gitignored; `.env.example` carries placeholders only.
- No new dependencies beyond what a bare Eureka+config client needs. No gateway *routing* deps yet.

## Out of scope

Day 36+ (gateway routing, JWT-at-gateway, rate limiting), observability, Kafka, AI layer, any frontend.

## Success criteria

`docker compose up` brings up 7 healthy containers; Eureka lists the three apps; the register→login→create→assign→query
flow succeeds using the seeded admin; all unit tests pass; `/code-review` surfaces no correctness regressions.
