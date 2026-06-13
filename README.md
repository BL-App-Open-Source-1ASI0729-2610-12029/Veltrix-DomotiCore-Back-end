# DomotiCore Backend

REST API companion for the **DomotiCore** Angular frontend. Exposes the same resources as `json-server` with JWT authentication, OpenAPI documentation, and bounded-context architecture.

## Requirements

- **Java 17+** (OpenJDK recommended)
- **Maven 3.9+** (or use the Maven Wrapper once generated)

## Quick start

```powershell
# Windows (desde la raíz del repo)
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
./mvnw spring-boot:run
```

No necesitas instalar Maven globalmente: el proyecto incluye **Maven Wrapper** (`mvnw` / `mvnw.cmd`).

The API starts at **http://localhost:8080/api/v1**.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- H2 console (dev): http://localhost:8080/h2-console

### Demo credentials

| Email | Password |
|-------|----------|
| `admin@domoticore.local` | `SecurePass123` |

## Frontend integration

Update the Angular app (`DomotiCore/src/environments/environment.ts`):

```typescript
apiUrl: 'http://localhost:8080/api/v1',
```

CORS is enabled for `http://localhost:4200`.

### Auth flow

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | `{ name, email, password }` → JWT + user |
| POST | `/api/v1/auth/login` | `{ email, password }` → JWT + user |
| GET | `/api/v1/users/{id}` | User profile (no password) |
| PATCH | `/api/v1/users/{id}` | Update `accountType`, `onboardingCompleted`, profile |

Send mutations with header: `Authorization: Bearer <token>`.

GET endpoints are public in dev so the dashboard loads without a token; POST/PATCH/DELETE require JWT.

## Architecture

```
com.domoticore
├── iam                 users, auth, onboarding
├── devicecontrol       devices-overview, device-details
├── security            cameras, locks, authorized-users, security-log
├── automation          recipe, builder-*, suggested-templates
├── history             activity-streams, history-*, notification-feed
├── settings            user-profile
└── shared              security, JSON resource store, exceptions, seed
```

Each context follows **domain → application → infrastructure → presentation**.

JSON collections mirror `DomotiCore/server/db.json` exactly and are seeded on first startup from `src/main/resources/data/db.json`.

## Phase 1 resources

Standard CRUD (`GET` list, `GET/{id}`, `POST`, `PATCH/{id}`, `DELETE/{id}`):

- `users` (JPA entity, separate from JSON store)
- `devices-overview`, `device-details`
- `activity-streams`, `history-summary`, `history-insights`, `notification-feed`
- `security-cameras`, `smart-locks`, `authorized-users`, `security-log`
- `automation-recipe`, `automation-builder-triggers`, `automation-builder-conditions`, `automation-builder-actions`, `automation-suggested-templates`
- `user-profile`

## Phase 2 (implemented)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/automation/rules` | SME automation rules |
| PATCH | `/api/v1/automation/rules/{id}` | Update rule |
| POST | `/api/v1/automation/rules/{id}/toggle` | Toggle rule active |
| GET | `/api/v1/automation/group-schedules` | Group schedules |
| GET | `/api/v1/automation/shutdown-protocol` | Shutdown protocol |
| GET | `/api/v1/automation/efficiency-insights` | Efficiency KPIs |
| GET | `/api/v1/automation/active-rule-timeline` | Timeline snapshot |
| GET | `/api/v1/automation/active-scenes` | Active scenes |
| POST | `/api/v1/automation/active-scenes/{id}/toggle` | Toggle scene |
| GET | `/api/v1/automation/upcoming-events` | Upcoming events |
| POST | `/api/v1/automation/upcoming-events/{id}/toggle` | Toggle event |
| GET | `/api/v1/automation/smart-suggestion` | Smart suggestion |
| GET | `/api/v1/team-management` | Team + zones snapshot |
| GET | `/api/v1/operations-hub/snapshot?range=thisMonth` | SME hub KPIs |
| GET/PATCH | `/api/v1/business-profile` | Business profile |
| PATCH | `/api/v1/device-details/{id}/temperature` | Target/current temp |
| PATCH | `/api/v1/device-details/{id}/operation-mode` | Mode / eco / fan |
| PATCH | `/api/v1/device-details/{id}/timer` | Scheduled timer |

Seed data: `src/main/resources/data/phase2.json` (loaded on startup if collections are empty).

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | HTTP port |
| `domoticore.cors.allowed-origins` | `http://localhost:4200` | CORS origin |
| `domoticore.jwt.secret` | dev secret | Change in production |
| `domoticore.jwt.expiration-ms` | `86400000` | Token TTL (24h) |

Production profile (`spring.profiles.active=prod`) uses PostgreSQL via `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`.

## Tests

```powershell
.\mvnw.cmd test
```

## Windows setup (solo Java 17)

Si `mvn` no está instalado, usa el wrapper del repo. Solo necesitas JDK 17:

```powershell
winget install Microsoft.OpenJDK.17
```

Luego en una terminal nueva:

```powershell
cd C:\Users\User\Documents\GitHub\Veltrix-DomotiCore-Back-end
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.19.10-hotspot"
.\mvnw.cmd spring-boot:run
```

Includes unit tests for `AuthService` and `DevicesOverviewController`.

## Error format

```json
{
  "message": "Human-readable message",
  "code": "NOT_FOUND",
  "timestamp": "2026-06-11T12:00:00Z"
}
```