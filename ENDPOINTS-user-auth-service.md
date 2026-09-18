# user-auth-service — Endpoints

Base: identity, login, JWT issue/refresh, current user.

## /api/auth — `AuthController`
| Method | Path | Auth | In | Out | Notes |
|---|---|---|---|---|---|
| POST | /api/auth/register | public | `RegisterRequest` | `AuthResponse` (201) | creates account (EMPLOYEE), hashes pw, issues access+refresh |
| POST | /api/auth/login | public | `LoginRequest` | `AuthResponse` (200) | verify pw vs stored hash, issue access+refresh |
| POST | /api/auth/refresh | refresh cookie | `refreshToken` cookie | `AuthResponse` (200) | rotates refresh, mints new access token |
| POST | /api/auth/logout | refresh cookie | `refreshToken` cookie | 204 | revokes refresh server-side + clears cookie |

## /api/users — `UserController`
| Method | Path | Auth | In | Out | Notes |
|---|---|---|---|---|---|
| GET | /api/users/me | JWT | — | `UserResponse` | current logged-in user from `Authentication` |

Access token in body, refresh token in httpOnly cookie.
