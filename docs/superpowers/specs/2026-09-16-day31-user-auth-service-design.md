# Day 31 — Extract `user-auth-service`

Carve the auth/user half out of the `assetservice` monolith into a standalone
Spring Cloud service on its own `userdb`, wired to Eureka + config-server.

## Decisions (approved)
- **Move + gut the monolith now** (not copy). `assetservice` keeps compiling; its
  asset endpoints stay JWT-protected.
- **Full wiring**: `spring.config.import=configserver:...` + eureka-client.
- **`userdb`**: same Postgres server, new logical DB (`jdbc:...:5432/userdb`).

## New module `user-auth-service/` (`com.itasset.userauth`, port 8081)
- **pom**: Boot 4.1.1, Java 21, Spring Cloud `2025.1.3` BOM. Deps: webmvc, data-jpa,
  validation, security, jjwt-api/impl/jackson 0.12.6, postgresql, `spring-cloud-starter-netflix-eureka-client`, `spring-cloud-starter-config`.
- **Relocated from monolith**: `User`, `RefreshToken`, `Role`, `UserRepository`,
  `RefreshTokenRepository`, `UserService`, `RefreshTokenService`, `AuthController`,
  `UserController`, DTOs (`AuthResponse`/`UserResponse`/`LoginRequest`/`RegisterRequest`),
  `DuplicateResourceException`, auth tests.
- **Copied (also kept in monolith)**: `JwtUtil`, `JwtAuthFilter`, `SecurityConfig`.
- **New**: `UserAuthServiceApplication`, trimmed `GlobalExceptionHandler`
  (validation + Duplicate), `application.yaml` (name + config import + `userdb`).
- **Skipped**: `JpaAuditingConfig` — neither `User` nor `RefreshToken` is audited (YAGNI).

## config-server
Flesh out `config/user-auth-service.yml`: keep `server.port: 8081`, add `userdb`
datasource + `jwt.*` (secret env-injected, shared with monolith so its filter
validates tokens this service issues).

## Gut `assetservice`
- Delete all relocated files above.
- `Assignment`: `@ManyToOne User user` → `Long userId` (+ accessors). `AssignmentRepository.findByUserId*` derived queries still resolve.
- `AssignmentService`: drop `UserRepository`; assign/query by `userId`. `employeeAssets`
  returns `"user #"+userId` for the name — cross-service name lookup deferred
  (`ponytail:` note).
- `DataSeeder`: drop user seeding.

## Checkpoint
`user-auth-service` starts → connects to `userdb` → registers in Eureka →
`/register` + `/login` work standalone. Monolith still builds; asset endpoints still
enforce JWTs issued by `user-auth-service` (shared `jwt.secret`).
