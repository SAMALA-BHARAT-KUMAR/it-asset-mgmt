# API Endpoints — IT Asset Management

Complete HTTP surface. 17 endpoints across 6 controllers.

Walkthrough order (end-to-end): **Auth → Users → Assets → Assignments → Queries**.

---

## 🔓 Auth — `/api/auth` (public)
`controller/AuthController.java`

| # | Method | Path | What it does |
|---|--------|------|--------------|
| 1 | POST | `/api/auth/register` | Create user, auto-login, issue JWT + refresh cookie |
| 2 | POST | `/api/auth/login` | Verify credentials (BCrypt), issue JWT + refresh cookie |
| 3 | POST | `/api/auth/refresh` | Rotate refresh token → new access token |
| 4 | POST | `/api/auth/logout` | Invalidate refresh token / clear cookie |

## 📦 Assets — `/api/assets` (JWT required)
`controller/AssetController.java`

| # | Method | Path | Access | What it does |
|---|--------|------|--------|--------------|
| 5 | POST | `/api/assets` | **ADMIN** | Create asset |
| 6 | GET | `/api/assets` | any auth | List all assets |
| 7 | GET | `/api/assets/stats/by-category` | any auth | Count assets grouped by category |
| 8 | GET | `/api/assets/{id}` | any auth | Get one asset |
| 9 | PUT | `/api/assets/{id}` | **ADMIN** | Update asset |
| 10 | DELETE | `/api/assets/{id}` | **ADMIN** | Delete asset |

## 👤 Users — `/api/users`
`controller/UserController.java`

| # | Method | Path | What it does |
|---|--------|------|--------------|
| 11 | GET | `/api/users/me` | Current authenticated user (from SecurityContext) |

## 🔄 Assignments — `/api/assignments`
`controller/AssignmentController.java`

| # | Method | Path | Access | What it does |
|---|--------|------|--------|--------------|
| 12 | POST | `/api/assignments` | **ADMIN** | Assign an asset to a user |
| 13 | POST | `/api/assignments/{id}/return` | **ADMIN** | Return an assigned asset |

## 🔎 Assignment queries (any auth, read-only)
`controller/AssignmentQueryController.java`

| # | Method | Path | What it does |
|---|--------|------|--------------|
| 14 | GET | `/api/assets/{id}/assignments` | Assignment history for an asset |
| 15 | GET | `/api/users/{id}/assignments` | Assignment history for a user |
| 16 | GET | `/api/employees/{id}/assets` | Flagship: assets currently held by an employee |

## 👋 Misc
`HelloController.java`

| # | Method | Path | What it does |
|---|--------|------|--------------|
| 17 | GET | `/api/hello` | Health/smoke check |
