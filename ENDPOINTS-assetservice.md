# assetservice — Endpoints

Core domain: assets + assignments (assign/return) + read-only history views.

## /api/assets — `AssetController`
| Method | Path | Auth | In | Out | Notes |
|---|---|---|---|---|---|
| POST | /api/assets | ADMIN | `AssetRequest` | `AssetResponse` | create (mapper: `AssetMapper`) |
| GET | /api/assets | JWT | pageable params | `Page<AssetResponse>` | list, paged |
| GET | /api/assets/stats/by-category | JWT | — | `Map<String,Long>` | counts per category |
| GET | /api/assets/{id} | JWT | — | `AssetResponse` | one asset |
| PUT | /api/assets/{id} | ADMIN | `AssetRequest` | `AssetResponse` | update |
| DELETE | /api/assets/{id} | ADMIN | — | 204 | delete |

## /api/assignments — `AssignmentController`
| Method | Path | Auth | In | Out | Notes |
|---|---|---|---|---|---|
| POST | /api/assignments | ADMIN | `AssignRequest` + Principal | `AssignmentResponse` (201) | assign asset→user; entity→DTO via `AssignmentResponse.from` |
| POST | /api/assignments/{id}/return | ADMIN | path `id` only | `AssignmentResponse` | close assignment, asset→ACTIVE |

## read-only views — `AssignmentQueryController`
| Method | Path | Auth | In | Out | Notes |
|---|---|---|---|---|---|
| GET | /api/assets/{id}/assignments | JWT | — | `List<AssignmentResponse>` | asset history, newest-first |
| GET | /api/users/{id}/assignments | JWT | — | `List<AssignmentResponse>` | user history, newest-first |
| GET | /api/employees/{id}/assets | JWT | — | `EmployeeAssetsResponse` | what a user currently holds |

## misc — `HelloController`
| Method | Path | Auth | Notes |
|---|---|---|---|
| GET | /api/hello | JWT | smoke test |

Note two mapper styles here: injected `AssetMapper` bean (assets) vs static `AssignmentResponse.from()` (assignments) — same job, different spelling.
