# DomotiCore Backend

REST API companion for the **DomotiCore Angular frontend**. The backend exposes JWT authentication, OpenAPI documentation, JSON-backed demo resources, PostgreSQL-ready persistence, and bounded-context packages for the smart-home and small-business flows.

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

For Vercel frontend deployment, the frontend must receive a public backend URL:

```text
NG_APP_API_URL=https://your-backend-domain.com/api/v1
```

The backend must be deployed separately, for example on Render, Railway, Fly.io or another Java-compatible host.

Production env vars:

| Variable | Example |
|----------|---------|
| `DATABASE_URL` | `jdbc:postgresql://host:5432/domoticore` |
| `DATABASE_USERNAME` | `domoticore` |
| `DATABASE_PASSWORD` | `...` |
| `JWT_SECRET` | long random secret, at least 256 bits |
| `DOMOTICORE_CORS_ORIGINS` | `https://your-app.vercel.app,http://localhost:4200` |

`localhost:8080` only works when frontend and backend are running on your own machine.

## Architecture

```text
com.domoticore
Ôö£ÔöÇÔöÇ iam                 auth, users, onboarding
Ôö£ÔöÇÔöÇ settings            user-profile/me
Ôö£ÔöÇÔöÇ devicecontrol       devices overview, details and commands
Ôö£ÔöÇÔöÇ security            cameras, locks, authorized users, logs
Ôö£ÔöÇÔöÇ automation          rules, schedules, scenes, zone configuration
Ôö£ÔöÇÔöÇ history             activity, notifications, cost analysis
Ôö£ÔöÇÔöÇ integrations        business profile
Ôö£ÔöÇÔöÇ teammanagement      SME team snapshot and mutations
Ôö£ÔöÇÔöÇ smeoperations       operations hub snapshots
ÔööÔöÇÔöÇ shared              security, JSON store, exceptions, seed, helpers
```

Contexts follow the project convention:

```text
domain -> application -> infrastructure -> presentation
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

### Device Detail Commands

| Method | Endpoint |
|--------|----------|
| PATCH | `/api/v1/device-details/{id}/temperature` |
| PATCH | `/api/v1/device-details/{id}/operation-mode` |
| PATCH | `/api/v1/device-details/{id}/timer` |

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
