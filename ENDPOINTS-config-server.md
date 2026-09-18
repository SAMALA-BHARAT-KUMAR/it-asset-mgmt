# config-server — Endpoints

No custom REST controllers. Spring Cloud Config Server (`@EnableConfigServer`) exposes
the standard config-fetch endpoints automatically:

| Method | Path | Notes |
|---|---|---|
| GET | /{application}/{profile}[/{label}] | config for an app+profile (e.g. `/asset-service/docker`) |
| GET | /{application}-{profile}.yml | same, as raw YAML |
| GET | /{application}-{profile}.properties | same, as properties |
| GET | /actuator/health | health |

Serves config from native file backend, not written by us. Clients (auth/asset) pull on boot.
