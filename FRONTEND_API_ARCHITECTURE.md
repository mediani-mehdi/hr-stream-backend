# HR Stream API Architecture (Frontend Guide)

This document explains how the backend API is organized so a frontend developer can design screens, routing, state management, and API client integrations.

## 1) High-Level Architecture

Backend stack:
- Spring Boot 3.x (Java 17)
- PostgreSQL (primary data)
- Redis (cache/session-related support)
- MinIO (CV storage)
- JWT-based auth with Spring Security

Logical modules:
- `auth`: admin/hr authentication and password reset
- `candidate`: candidate CRUD + apply endpoints + CV endpoints
- `candidate/profile`: candidate self-managed profile endpoints
- `job`: job CRUD + status + AI generation + public job details
- `jobapplication`: applications lifecycle endpoints

There is **no global `/api/v1` prefix** currently. Endpoints are mounted directly (for example `/auth/login`, `/jobs`, `/applications`).

## 2) Authentication and Authorization

## JWT Model
- Login endpoints return a JWT token.
- Frontend should send `Authorization: Bearer <token>` on protected routes.
- API is stateless (`SessionCreationPolicy.STATELESS`).

## Public vs Protected Routes
Public:
- `/auth/welcome`
- `/auth/login`
- `/auth/register`
- `/auth/forgot-password`
- `/auth/reset-password`
- `/candidate/auth/**`
- `/candidates/apply/**`
- `/public/jobs/**`

Protected:
- everything else by default

Role-restricted:
- `/candidate/profile/**` requires `ROLE_CANDIDATE`
- `POST /jobs/*/apply` requires `ROLE_CANDIDATE`

## CORS
Current CORS is open for origins (`*`) with methods:
`GET, POST, PUT, PATCH, DELETE, OPTIONS`

Headers accepted:
- `authorization`
- `content-type`
- `x-auth-token`

## 3) Response and Error Contracts

## Success responses
The API currently uses mixed response styles:
- plain objects (entity/DTO)
- plain strings (some status endpoints)
- paginated wrappers (two different formats)

There is **not** a single global `ApiResponse<T>` wrapper at this time.

## Error response shape
Global error responses use this structure:

```json
{
  "timestamp": "2026-04-03T18:22:10.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "{field=must not be blank}",
  "path": "/candidate/profile/basic-info"
}
```

HTTP behavior you should expect:
- `400` for validation and illegal arguments
- `401` when token is missing/invalid
- `403` when role is insufficient
- `409` for business conflict (`IllegalStateException`)
- `500` for uncaught runtime errors

## 4) Pagination Patterns (Important)

There are two pagination formats in current API responses:

### A) Custom `PageResponse<T>`
Used by jobs list (`GET /jobs`):

```json
{
  "content": [],
  "page": 0,
  "size": 10,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

### B) Spring `Page<T>` JSON
Used by some endpoints like candidates/applications list. Shape includes fields like:
- `content`
- `number`
- `size`
- `totalElements`
- `totalPages`
- `first`
- `last`
- `sort`
- `pageable`

Frontend recommendation: normalize both shapes in API client utilities into one internal `PaginatedResult<T>` model.

## 5) Endpoint Map by Domain

## Auth (`/auth`)
- `GET /auth/welcome`
- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/forgot-password`
- `POST /auth/reset-password`

## Candidate Auth (`/candidate/auth`)
- `POST /candidate/auth/register`
- `POST /candidate/auth/login`

## Jobs (`/jobs` + public)
- `POST /jobs/save`
- `POST /jobs/{jobId}/{status}` (status update variant)
- `PATCH /jobs/{jobId}/status?status=OPEN`
- `PUT /jobs/{jobId}`
- `GET /jobs/{jobId}`
- `DELETE /jobs/{jobId}`
- `POST /jobs/generate-description`
- `POST /jobs/{jobId}/generate`
- `GET /jobs` (paginated)
- `GET /public/jobs/{slug}` (public)

## Applications (`/applications` + candidate apply)
- `GET /applications` (paginated)
- `PATCH /applications/{id}/status?status=...`
- `GET /applications/{id}`
- `DELETE /applications/{id}`
- `POST /jobs/{slug}/apply` (candidate role required)
- `POST /candidates/apply/{token}` (public flow)
- `POST /candidates/apply/{token}/with-resume` (multipart)

## Candidates (`/candidates`)
- `POST /candidates`
- `GET /candidates` (paginated)
- `GET /candidates/{id}`
- `PUT /candidates/{id}`
- `DELETE /candidates/{id}`
- `POST /candidates/{candidateId}/cv` (multipart)
- `GET /candidates/{candidateId}/cv`
- `DELETE /candidates/{candidateId}/cv`
- `GET /candidates/{id}/resume` (legacy)

## Candidate Profile (`/candidate/profile`)
All endpoints require `ROLE_CANDIDATE`:
- `GET /candidate/profile`
- `PUT /candidate/profile/basic-info`
- `POST/PUT/DELETE /candidate/profile/education/{educationId?}`
- `POST/PUT/DELETE /candidate/profile/experience/{experienceId?}`
- `POST/PUT/DELETE /candidate/profile/skills/{skillId?}`
- `POST/PUT/DELETE /candidate/profile/languages/{languageId?}`
- `GET /candidate/profile/completeness`

## 6) Frontend Integration Recommendations

- Create one API client module per domain (`authApi`, `jobApi`, `candidateApi`, `applicationApi`, `profileApi`).
- Add an HTTP interceptor that injects JWT from storage and handles `401/403` globally.
- Normalize pagination responses into one frontend model.
- Support both JSON and multipart flows for candidate apply/CV upload.
- Treat backend enums as strict union types in frontend (status, contract type, application status).
- Add defensive handling for string responses on some actions (for example status updates) rather than always expecting JSON objects.

## 7) Local Development for Frontend + Local Backend

Use infra-only docker compose for local dependencies:
- PostgreSQL
- MinIO
- Redis

File:
- `docker-compose.infra.yml`

Start infra:

```powershell
Set-Location "D:\hr-stream"
docker compose -f docker-compose.infra.yml up -d
```

Typical local backend profile:
- `dev` profile uses localhost defaults:
  - Postgres: `localhost:5432`, db `hr_stream`, user `postgres`, password `123654`
  - Redis: `localhost:6379`, password `redis123`
  - MinIO: `http://localhost:9000`, console `http://localhost:9001`, default `admin/admin123`

If needed, override with env vars:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_REDIS_HOST`
- `SPRING_REDIS_PORT`
- `SPRING_REDIS_PASSWORD`
- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`

## 8) Known Integration Notes

- API contracts are currently mixed (DTO + entity + string responses); frontend should rely on explicit per-endpoint typing.
- Some route naming is not fully REST-consistent (for example `/jobs/save`, `/jobs/{jobId}/{status}`), so build client methods around actual current paths.
- If schema changes happen (entity updates), restart the backend and verify Hibernate applies the expected schema update before local testing.

---

If you want, I can generate a second file with TypeScript interfaces (`types.ts`) and a ready-to-use endpoint map object for Axios/React Query.

