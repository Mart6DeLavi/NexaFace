# NextFace

NextFace is an educational prototype of a centralized biometric authentication system based on
face recognition. Third-party websites can integrate it through a small JavaScript SDK: the user
authenticates on the NextFace website and returns to the integrating site with a one-time
authorization code.

The full technical specification and roadmap live in [PLAN.md](PLAN.md).

## Repository structure

```text
backend/       Spring Boot REST API (Java 21)
frontend/      Next.js web application (TypeScript, Tailwind CSS)
face-service/  Python FastAPI face recognition service (planned)
sdk/           JavaScript SDK (planned)
demo-client/   Demo integration site (planned)
```

## Tech stack

- Backend: Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Spring Validation, PostgreSQL
- Frontend: Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4 (shadcn/ui planned)
- Face service: Python 3, FastAPI, OpenCV, MediaPipe, ONNX Runtime (planned)

## Requirements

- JDK 21+
- Gradle wrapper is included (Gradle 8.13), no separate installation required
- Node.js 20.9+ with npm
- Docker (optional, for PostgreSQL) or a local PostgreSQL 17 instance

## Configuration

Copy `.env.example` to `.env` and adjust the values. The backend reads the following variables:

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/nextface` | JDBC URL |
| `DB_USERNAME` | `nextface` | Database user |
| `DB_PASSWORD` | `nextface` | Database password |
| `SERVER_PORT` | `8080` | Backend HTTP port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated allowed origins |

The frontend reads `NEXT_PUBLIC_API_URL` (default `http://localhost:8080`). Copy
`frontend/.env.example` to `frontend/.env.local` to override it.

## Running the backend

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Or create the database in a local PostgreSQL instance:

```bash
createuser --createdb nextface
createdb -O nextface nextface
```

Then run the backend:

```bash
cd backend
./gradlew bootRun
```

Verify it is up:

```bash
curl http://localhost:8080/api/health
# {"status":"UP","service":"nextface-backend"}
```

Run the tests:

```bash
cd backend
./gradlew test
```

## Running the frontend

```bash
cd frontend
npm install
npm run dev
```

The application is available at http://localhost:3000. The home page shows the backend connection
status by calling `GET /api/health`, so the backend should be running as well.

Production build:

```bash
cd frontend
npm run build
npm run start
```

Lint:

```bash
cd frontend
npm run lint
```

## Status

The project is at Phase 1 (Project Setup): the backend and the frontend are running and can
exchange requests. See the roadmap in [PLAN.md](PLAN.md) for the current checklist and next tasks.
