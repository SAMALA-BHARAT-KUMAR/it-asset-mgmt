# Project Structure — derived from the 90-Day Plan

Two structures, because the plan changes shape:

- **Phase 0–1 (Days 1–28):** ONE monolith app (`assetservice/`). Build the real business logic here first.
- **Phase 2+ (Days 29–90):** SPLIT into multiple microservice modules (sibling folders under the repo root).

Rule: **when a day tells you to create a class, make that folder (if new) and put the class in it.** Nothing is pre-created.

---

## PART 1 — The Monolith (Days 1–28) — inside `assetservice/`

Base package: `com.itasset.assetservice`
Standard Spring layered structure. Each folder = a Java package.

```
assetservice/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/itasset/assetservice/
    │   │   ├── AssetserviceApplication.java   (main class — already there)
    │   │   │
    │   │   ├── controller/     ← HTTP layer (@RestController)
    │   │   │   ├── HelloController.java          Day 3   ✅ done
    │   │   │   ├── AssetController.java          Day 11
    │   │   │   ├── AuthController.java           Day 18
    │   │   │   ├── AssignmentController.java     Day 23–25
    │   │   │   ├── EmployeeController.java       Day 26  (GET /api/employees/{id}/assets)
    │   │   │   └── UserController.java           Day 20  (GET /api/users/me)
    │   │   │
    │   │   ├── service/        ← business logic (@Service)
    │   │   │   ├── AssetService.java             Day 11
    │   │   │   ├── UserService.java              Day 16
    │   │   │   └── AssignmentService.java        Day 23
    │   │   │
    │   │   ├── repository/     ← DB access (extends JpaRepository)
    │   │   │   ├── AssetRepository.java          Day 5 / 9
    │   │   │   ├── CategoryRepository.java       Day 9
    │   │   │   ├── LocationRepository.java       Day 9
    │   │   │   ├── UserRepository.java           Day 15
    │   │   │   └── AssignmentRepository.java     Day 23
    │   │   │
    │   │   ├── entity/         ← JPA tables (@Entity)
    │   │   │   ├── Asset.java                    Day 4 / 8
    │   │   │   ├── Category.java                 Day 8
    │   │   │   ├── Location.java                 Day 8
    │   │   │   ├── User.java                     Day 15
    │   │   │   ├── Assignment.java               Day 22
    │   │   │   ├── AssetStatus.java  (enum)      Day 4 / 6
    │   │   │   └── Role.java         (enum)      Day 15
    │   │   │
    │   │   ├── dto/            ← API shape (records) — keep entities off the wire
    │   │   │   ├── AssetRequestDto.java          Day 10
    │   │   │   ├── AssetResponseDto.java         Day 10
    │   │   │   ├── CategoryDto.java              Day 10
    │   │   │   ├── LocationDto.java              Day 10
    │   │   │   ├── AssetSummaryDto.java          Day 26
    │   │   │   ├── EmployeeAssetsResponseDto.java Day 26
    │   │   │   └── UserSummaryDto.java           Day 40 (for cross-service calls)
    │   │   │
    │   │   ├── mapper/         ← entity ↔ dto conversion
    │   │   │   └── AssetMapper.java              Day 10
    │   │   │
    │   │   ├── exception/      ← custom exceptions + global handler
    │   │   │   ├── GlobalExceptionHandler.java   Day 12  (@ControllerAdvice)
    │   │   │   ├── ResourceNotFoundException.java Day 12
    │   │   │   ├── DuplicateResourceException.java Day 12
    │   │   │   ├── AssetNotAvailableException.java Day 23
    │   │   │   ├── AssignmentNotFoundException.java Day 24
    │   │   │   └── InvalidAssignmentStateException.java Day 24
    │   │   │
    │   │   ├── security/       ← JWT + filters
    │   │   │   ├── JwtUtil.java                  Day 18
    │   │   │   └── JwtAuthFilter.java            Day 19
    │   │   │
    │   │   └── config/         ← beans, security config
    │   │       └── SecurityConfig.java           Day 17
    │   │
    │   └── resources/
    │       └── application.properties (or .yml — Day 8 switches to yml)
    │
    └── test/java/com/itasset/assetservice/
        ├── service/AssetServiceTest.java         Day 14
        ├── service/AssignmentServiceTest.java    Day 27
        └── security/JwtUtilTest.java             Day 21
```

---

## PART 2 — The Microservices Split (Day 29 onward)

Starting Day 29, the monolith is broken into **separate Maven modules** — each its own Spring Boot app, own folder, own DB. They sit side-by-side under the repo root:

```
it-asset-mgmt/
├── discovery-server/      Day 29  Eureka (service registry)      port 8761
├── config-server/         Day 30  Spring Cloud Config            port 8888
├── gateway/               Day 34/36  Spring Cloud Gateway        port 8080  (single entry point)
├── user-auth-service/     Day 31  users/roles/JWT  → DB: userdb
├── asset-service/         Day 32  assets/assignments → DB: assetdb
├── audit-service/         Day 51  Kafka consumer   → DB: auditdb
├── ai-service/            Day 73  RAG + agentic chat → DB: pgvector
├── docker-compose.yml     Day 34  wires everything together
└── PROJECT-STRUCTURE.md   (this file)
```

Each service module repeats the **same internal layered structure** from Part 1
(`controller/ service/ repository/ entity/ dto/ mapper/ exception/ config/`),
plus feature-specific packages as the plan adds them:

| Package added | Where | Day | What |
|---------------|-------|-----|------|
| `client/`     | asset-service | 39 | Feign/gRPC clients calling other services (`UserClient`) |
| `event/`      | asset-service, audit-service | 50–54 | Kafka event records + `@KafkaListener` consumers |
| `saga/`       | asset-service | 54 | choreographed saga steps |
| `websocket/`  | asset-service, ai-service | 61 | `WebSocketConfig`, STOMP handlers |
| `graphql/` + `resources/graphql/schema.graphqls` | asset-service | 64 | GraphQL controllers + schema |
| `grpc/` + `resources/proto/*.proto` | user-auth-service | 66 | gRPC proto + server |
| `ingestion/` `retrieval/` `embedding/` | ai-service | 76–78 | RAG pipeline |
| `agent/` (tools + loop) | ai-service | 82–85 | agentic tool-calling |

---

## Infrastructure (containers, not Java modules) — added to docker-compose.yml

| Container | Day | Purpose |
|-----------|-----|---------|
| postgres (userdb, assetdb) | 33–34 | per-service databases |
| Kafka (+ Zookeeper/KRaft)  | 50 | event streaming |
| Redis        | 38 / 57 | rate limiting + caching |
| Zipkin       | 46 | distributed tracing |
| MinIO        | 59 | object storage (invoices/photos) |
| pgvector     | 75 | RAG embeddings store |

---

## The 4 phases at a glance

- **Phase 0** (Days 1–7): toolchain + first Spring Boot CRUD → monolith
- **Phase 1** (Days 8–28): core domain (assets, users/JWT, assignments) → still monolith
- **Phase 2** (Days 29–56): split into services + discovery/config/gateway/Kafka/resilience
- **Phase 3** (Days 57–72): Redis, MinIO, WebSocket, GraphQL, gRPC, Testcontainers
- **Phase 4** (Days 73–90): AI layer — RAG (pgvector) + agentic tool-calling + streaming chat
