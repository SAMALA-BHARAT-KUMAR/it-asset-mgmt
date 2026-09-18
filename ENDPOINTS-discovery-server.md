# discovery-server — Endpoints

No custom REST controllers. Netflix Eureka server (`@EnableEurekaServer`) — server code
lives in the dependency. Exposes the standard Eureka registry API:

| Method | Path | Notes |
|---|---|---|
| GET | / | Eureka dashboard (browser UI on :8761) |
| GET/POST/PUT/DELETE | /eureka/apps/... | service registry REST API (clients register + heartbeat here) |
| GET | /actuator/health | health |

We wrote no endpoints; `@EnableEurekaServer` is the only switch. Clients hit `/eureka` on :8761.
