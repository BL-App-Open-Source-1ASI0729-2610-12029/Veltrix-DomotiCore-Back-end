# DomotiCore Backend

REST API companion for the **DomotiCore Angular frontend**. The backend exposes JWT authentication, OpenAPI documentation, JSON-backed demo resources, PostgreSQL-ready persistence, and bounded-context packages for the smart-home and small-business flows.

## Production Deployment

| Component | Platform | Public URL |
|-----------|----------|------------|
| Frontend | Vercel | [veltrix-domoti-core-front-end-omega.vercel.app](https://veltrix-domoti-core-front-end-omega.vercel.app) |
| API | Render | [domoticore-api.onrender.com](https://domoticore-api.onrender.com) |
| PostgreSQL | Railway | Database only — dashboard requires Railway login |

[![Render API](https://img.shields.io/website?url=https://domoticore-api.onrender.com/actuator/health&label=Render%20API&up_message=online&down_message=offline)](https://domoticore-api.onrender.com/actuator/health)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D)](https://domoticore-api.onrender.com/swagger-ui.html)

### Architecture

```text
Vercel (Angular frontend)
        |
        v
Render (domoticore-api)  <-- Spring Boot API lives here
        |
        v
Railway (domoticore-db)  <-- PostgreSQL only
```

### Live endpoints (public — work in incognito, no login)

| Service | URL |
|---------|-----|
| Health check | https://domoticore-api.onrender.com/actuator/health |
| Swagger UI | https://domoticore-api.onrender.com/swagger-ui.html |
| OpenAPI JSON | https://domoticore-api.onrender.com/v3/api-docs |
| API base | https://domoticore-api.onrender.com/api/v1 |

### Railway (PostgreSQL)

| Item | Value |
|------|-------|
| Project | `adequate-courage` / `production` |
| Database service | `domoticore-db` |
| API service | **Not used** — keep API on Render |

Railway dashboard (requires Railway account — does not open in incognito):

- [Project overview](https://railway.com/project/5fd10694-2337-4ef4-b187-37e989a92ad8)
- [domoticore-db service](https://railway.com/project/5fd10694-2337-4ef4-b187-37e989a92ad8/service/a30ed271-9d10-4bdb-98db-3fa43c41458b)

**Render `DATABASE_URL` must point to Railway:**

```text
postgresql://postgres:<password>@nozomi.proxy.rlwy.net:18192/railway?sslmode=require
```

Use `DATABASE_PUBLIC_URL` from Railway → **Connect** → paste into Render → **Manual Deploy**.

### Fix red X on GitHub commits

If commits show **"adequate-courage - domoticore-api — Deployment failed"**, Railway is trying to deploy the API on every push and failing (missing `DATABASE_URL` on that service).

**Recommended fix:** In [Railway](https://railway.com/project/5fd10694-2337-4ef4-b187-37e989a92ad8), **delete or pause** the `domoticore-api` service. Keep only `domoticore-db`. The API should deploy only on **Render**.

Alternative: open `domoticore-api` → **Settings** → **Source** → **Disconnect** the GitHub repo from that service.

After that, pushes to GitHub will no longer show a failing Railway API check.

## Stack

- Java 17
- Spring Boot 3.3.5
- Spring Web, Spring Data JPA, Spring Security
- JWT with JJWT
- H2 in-memory for local default development
- PostgreSQL for persistent local/prod usage
- Flyway migrations
- Springdoc OpenAPI / Swagger UI
- Maven Wrapper

## Quick Start

From the backend repo:

```powershell
cd C:\Users\User\Documents\GitHub\Veltrix-DomotiCore-Back-end
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
.\mvnw.cmd spring-boot:run
```

The API starts at:

```text
http://localhost:8080/api/v1
```

Useful URLs:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health check: http://localhost:8080/actuator/health
- H2 console: http://localhost:8080/h2-console

Demo credentials:

| Email | Password |
|-------|----------|
| `admin@domoticore.local` | `SecurePass123` |

## Frontend Integration

For local Angular development against the deployed Render API, the frontend uses:

```typescript
apiUrl: 'https://domoticore-api.onrender.com/api/v1',
```

(`src/environments/environment.ts` in the frontend repo.)

For a local backend instead:

```typescript
apiUrl: 'http://localhost:8080/api/v1',
```

CORS on Render must allow your frontend origin. Default backend config allows:

```text
http://localhost:4200
http://127.0.0.1:4200
```

If the frontend is deployed on Vercel, set `DOMOTICORE_CORS_ORIGINS` on Render to include your Vercel URL.

Use the local Angular server for local backend testing:

```powershell
cd C:\Users\User\Documents\GitHub\Veltrix-DomotiCore-Front-End\DomotiCore
npm start
```

Open:

```text
http://localhost:4200
```

Do not test local login/register from a Vercel URL unless the backend is also deployed publicly. A deployed frontend cannot call your machine's `localhost:8080`.

## Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Creates a user and returns JWT + user |
| POST | `/api/v1/auth/login` | Returns JWT + user |
| GET | `/api/v1/users/{id}` | Reads the authenticated user's own account |
| PATCH | `/api/v1/users/{id}` | Updates own account, onboarding and account type |

Register payload:

```json
{
  "name": "Cesar",
  "email": "cesar@gmail.com",
  "password": "12345678"
}
```

Password must be at least 8 characters. Send authenticated mutations with:

```text
Authorization: Bearer <token>
```

## Data Persistence

Default local profile uses H2 in memory:

| Profile | Database | Persistence |
|---------|----------|-------------|
| default | H2 in-memory | Data is lost when backend restarts |
| `postgres` | Local PostgreSQL via Docker | Persistent |
| `prod` | PostgreSQL via env vars | Persistent |

Use PostgreSQL locally when you want users and changes to survive backend restarts:

```powershell
docker compose up -d
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
```

Schema is managed by Flyway:

```text
src/main/resources/db/migration/V1__init_schema.sql
```

Seed files:

- `src/main/resources/data/db.json`
- `src/main/resources/data/phase2.json`
- `src/main/resources/data/phase3.json`

## Deployment Notes

### Render (API)

The `prod` profile expects PostgreSQL. Render reads `DATABASE_URL` as `postgresql://...` (not JDBC). The app converts it automatically via `RenderDatabaseConfig`.

**Current setup:** API on Render, database on Railway. Set `DATABASE_URL` on Render to Railway `DATABASE_PUBLIC_URL` (with `?sslmode=require` if needed).

**If the service fails with `UnknownHostException: dpg-xxxxx-a`:**

1. Confirm `DATABASE_URL` uses Railway **public** host (`*.proxy.rlwy.net`), not an internal hostname
2. Redeploy after changing environment variables

**Required env vars on Render:**

| Variable | Example |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | Railway `DATABASE_PUBLIC_URL` |
| `JWT_SECRET` | long random secret, at least 256 bits |
| `DOMOTICORE_CORS_ORIGINS` | `https://veltrix-domoti-core-front-end-omega.vercel.app,http://localhost:4200` |
| `DOMOTICORE_FRONTEND_URL` | `https://veltrix-domoti-core-front-end-omega.vercel.app` |

### Railway (PostgreSQL only)

Do **not** deploy `domoticore-api` on Railway unless you configure all env vars and healthcheck. The recommended layout is **Render API + Railway DB** (see [Production Deployment](#production-deployment)).

### Vercel (frontend)

For Vercel frontend deployment, the frontend must receive a public backend URL:

```text
NG_APP_API_URL=https://domoticore-api.onrender.com/api/v1
```

The backend must be deployed separately on Render (or another Java host).

Production env vars:

| Variable | Example |
|----------|---------|
| `NG_APP_API_URL` | `https://domoticore-api.onrender.com/api/v1` |
| `JWT_SECRET` | long random secret, at least 256 bits |
| `DOMOTICORE_CORS_ORIGINS` | `https://veltrix-domoti-core-front-end-omega.vercel.app,http://localhost:4200` |

`localhost:8080` only works when frontend and backend are running on your own machine.

## Architecture

Each bounded context follows the [Learning Center](https://github.com/upc-pre-202610-1asi0729-11848/learning-center) DDD layout:

```text
domain/model -> application -> infrastructure -> presentation
```

```text
com.domoticore
├── iam                 auth, users, onboarding
├── settings            user-profile/me
├── devicecontrol       devices overview, details and commands
├── security            cameras, locks, authorized users, logs
├── automation          rules, schedules, scenes, zone configuration
├── history             activity, notifications, cost analysis
├── integrations        business profile, developer API
├── teammanagement      SME team snapshot and mutations
├── smeoperations       operations hub snapshots
├── dashboard           aggregated dashboard snapshot
├── export              CSV/Excel/PDF exports
├── gateway             gateway link and node management
├── maintenance         maintenance records
└── shared              cross-cutting domain, application, infrastructure and presentation
```

### Layer responsibilities

| Layer | Responsibility |
|-------|----------------|
| `domain/model` | Aggregates, entities, commands, queries, value objects |
| `application` | Use cases, application services, CQRS handlers |
| `infrastructure` | Persistence, DTOs, assemblers, security, config |
| `presentation` | REST controllers and API error handling |

### Shared kernel

```text
shared/
├── domain/model        Result, ApiError, domain exceptions
├── application         JsonResourceService and cross-context helpers
├── infrastructure      JPA repositories, JWT, Spring config, seed data
└── presentation        Base controllers, GlobalExceptionHandler
```

The project uses regular JPA for users and a generic `json_resources` table for demo/domain resources that mirror the frontend JSON contracts.

## Implemented Endpoints

### IAM

| Method | Endpoint |
|--------|----------|
| POST | `/api/v1/auth/register` |
| POST | `/api/v1/auth/login` |
| GET | `/api/v1/users/{id}` |
| PATCH | `/api/v1/users/{id}` |

### User Settings

These are scoped to the authenticated user:

| Method | Endpoint |
|--------|----------|
| GET | `/api/v1/user-profile/me` |
| PATCH | `/api/v1/user-profile/me` |

### Smart Home / Phase 1 CRUD

Standard CRUD pattern:

```text
GET    /api/v1/{resource}
GET    /api/v1/{resource}/{id}
POST   /api/v1/{resource}
PATCH  /api/v1/{resource}/{id}
DELETE /api/v1/{resource}/{id}
```

Resources:

- `devices-overview`
- `device-details`
- `activity-streams`
- `history-summary`
- `history-insights`
- `notification-feed`
- `security-cameras`
- `smart-locks`
- `authorized-users`
- `security-log`
- `automation-recipe`
- `automation-builder-triggers`
- `automation-builder-conditions`
- `automation-builder-actions`
- `automation-suggested-templates`

### Device Details

Use `PATCH /api/v1/device-details/{id}` for temperature, mode, timer, rename and power state updates.

### SME Automation

| Method | Endpoint |
|--------|----------|
| GET | `/api/v1/automation/rules` |
| PATCH | `/api/v1/automation/rules/{id}` |
| POST | `/api/v1/automation/rules/{id}/toggle` |
| GET | `/api/v1/automation/group-schedules` |
| GET | `/api/v1/automation/shutdown-protocol` |
| GET | `/api/v1/automation/efficiency-insights` |
| GET | `/api/v1/automation/active-rule-timeline` |
| GET | `/api/v1/automation/active-scenes` |
| POST | `/api/v1/automation/active-scenes/{id}/toggle` |
| GET | `/api/v1/automation/upcoming-events` |
| POST | `/api/v1/automation/upcoming-events/{id}/toggle` |
| GET | `/api/v1/automation/smart-suggestion` |

### SME User-Scoped Resources

These endpoints keep separate data per authenticated user by deriving the resource id from the JWT user id:

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET/PATCH | `/api/v1/business-profile` | Business and integration profile |
| GET/PATCH | `/api/v1/zone-configuration` | Zone budgets, schedules and audit log |
| GET/PATCH | `/api/v1/team-management` | Team members, permissions and zones |
| GET | `/api/v1/operations-hub/snapshot?range=thisMonth` | Operations KPIs by range |
| GET | `/api/v1/cost-analysis` | Billing, ROI and audit data |

Supported operations hub ranges:

```text
thisMonth
lastMonth
thisQuarter
```

## Security Notes

Current development behavior:

- Auth endpoints and Swagger are public.
- `GET /api/v1/**` is public for demo/read-heavy dashboard usage.
- Mutations require JWT.
- User account read/update is self-scoped.
- SME resources listed above are user-scoped when a JWT is used.

Before production, consider requiring JWT for more `GET` endpoints and introducing real tenant/organization ownership if multiple users should share the same business workspace.

## Error Format

```json
{
  "message": "Human-readable message",
  "code": "NOT_FOUND",
  "timestamp": "2026-06-11T12:00:00Z"
}
```

## Tests

Run backend tests:

```powershell
.\mvnw.cmd test
```

The suite covers:

- `AuthService`
- MVC controller behavior
- user-scoped profile and SME resources
- Flyway startup smoke test

## Common Issues

### Port 8080 is already in use

Check the process:

```powershell
netstat -ano | findstr :8080
```

DomotiCore expects port 8080. TicketFlow was moved to port 8090 to avoid conflicts.

### Vercel login/register does not hit local backend

This is expected if Vercel is configured without a public backend URL. Use local Angular for local backend testing:

```text
http://localhost:4200
```

For deployed frontend, set:

```text
NG_APP_API_URL=https://your-backend-domain.com/api/v1
```

### Registered users disappear after restart

That happens with default H2 in-memory. Use the `postgres` profile for persistent local data.

## Current Project Status

Implemented:

- REST backend companion for Angular frontend
- JWT login/register/onboarding
- Swagger/OpenAPI
- CORS for local Angular
- H2 + PostgreSQL profiles
- Flyway schema migration
- JSON resource seed and generic store
- Phase 1 dashboard/security/history/device resources
- Phase 2 SME automation/team/operations/business profile
- Phase 3 PostgreSQL readiness, health check, zone config, cost analysis
- Phase 4 user-scoped settings and SME resources

Remaining recommended work:

- Deploy backend publicly and connect Vercel through `NG_APP_API_URL`
- Use PostgreSQL in production
- Require JWT for more read endpoints
- Add real tenant/organization model if multiple users should share the same SME workspace
- Gradually replace remaining frontend-only mock modules with backend endpoints
