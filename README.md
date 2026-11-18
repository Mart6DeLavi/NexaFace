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
face-service/  Python FastAPI face analysis service
sdk/           JavaScript SDK (planned, Phase 7)
demo-client/   Demo integration site (planned, Phase 7)
```

## Tech stack

- Backend: Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Spring Validation, PostgreSQL
- Frontend: Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS 4 (shadcn/ui planned)
- Face service: Python 3, FastAPI, Uvicorn, OpenCV, MediaPipe, ONNX Runtime

## Requirements

- JDK 21+
- Gradle wrapper is included (Gradle 8.13), no separate installation required
- Node.js 20.9+ with npm
- Python 3.11+
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
| `JWT_SECRET` | development-only default | JWT signing key, at least 32 bytes |
| `JWT_EXPIRATION` | `PT1H` | Access token lifetime (ISO-8601 duration) |

The frontend reads `NEXT_PUBLIC_API_URL` (default `http://localhost:8080`). Copy
`frontend/.env.example` to `frontend/.env.local` to override it.

The face service reads these optional variables:

| Variable | Default | Description |
|---|---|---|
| `FACE_DETECTOR_MODEL` | `models/blaze_face_short_range.tflite` | MediaPipe face detector model |
| `FACE_MIN_DETECTION_CONFIDENCE` | `0.5` | Minimum detection confidence |
| `FACE_MIN_SIZE_PX` | `80` | Minimum face box size in pixels |
| `FACE_BLUR_THRESHOLD` | `60` | Minimum Laplacian variance before an image is "blurry" |
| `FACE_BRIGHTNESS_MIN` / `FACE_BRIGHTNESS_MAX` | `40` / `220` | Accepted mean brightness range |
| `FACE_MAX_IMAGE_BYTES` | `5242880` | Maximum upload size |

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

## Authentication API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register with email and password, returns a JWT |
| POST | `/api/auth/login` | Log in with password, returns a JWT |
| GET | `/api/auth/me` | Current user (requires `Authorization: Bearer <token>`) |

Example:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"password123"}'

curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

Passwords are stored as BCrypt hashes. Tokens are signed with `JWT_SECRET` and expire after
`JWT_EXPIRATION`.

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

## Running the face service

The face service is internal: only the Spring Boot backend calls it, browsers never access it
directly.

```bash
cd face-service
python3 -m venv .venv
.venv/bin/pip install -r requirements-dev.txt
.venv/bin/python -m uvicorn main:app --host 127.0.0.1 --port 8000
```

Verify it is up:

```bash
curl http://localhost:8000/health
# {"status":"UP","service":"face-service"}
```

Run the tests:

```bash
cd face-service
.venv/bin/python -m pytest
```

If port 8000 is already taken on your machine, pass another port to `--port`.

### Face analysis API

| Method | Endpoint | Description |
|---|---|---|
| POST | `/face/detect` | Detect faces, check image quality (`multipart/form-data`, field `image`) |
| POST | `/face/embedding` | Generate a 128-d embedding for the single face (`multipart/form-data`, field `image`) |
| POST | `/face/compare` | Compare two embeddings with cosine similarity (JSON) |
| POST | `/face/liveness` | Basic challenge-response liveness check (`turn_left`, `turn_right`, `blink`) from a frame sequence |

```bash
curl -F "image=@photo.jpg;type=image/jpeg" http://localhost:8000/face/detect
curl -F "image=@photo.jpg;type=image/jpeg" http://localhost:8000/face/embedding
curl -X POST http://localhost:8000/face/compare \
  -H "Content-Type: application/json" \
  -d '{"embedding1": [...], "embedding2": [...]}'
curl -X POST "http://localhost:8000/face/liveness?challenge=turn_right" \
  -F "images=@frame1.jpg;type=image/jpeg" \
  -F "images=@frame2.jpg;type=image/jpeg" \
  -F "images=@frame3.jpg;type=image/jpeg"
```

Detection responses contain every detected face (box and confidence) and a quality report: blur
score, brightness, face count and a list of issues (`BLURRY`, `TOO_DARK`, `TOO_BRIGHT`,
`MULTIPLE_FACES`, `FACE_TOO_SMALL`).

Embeddings are L2-normalized. `/face/embedding` requires exactly one face (`MULTIPLE_FACES`
otherwise). `/face/compare` returns `similarity`, `match` and the configured `threshold`
(`FACE_MATCH_THRESHOLD`, default `0.363`).

Errors use stable codes: `INVALID_IMAGE` (400), `IMAGE_TOO_LARGE` (413), `NO_FACE_DETECTED`
(422), `MULTIPLE_FACES` (422), `INVALID_EMBEDDING` (400), `EMBEDDING_DIMENSION_MISMATCH` (400),
`INVALID_FRAME_COUNT` (400).

Recognition uses SFace (`models/face_recognition_sface_2021dec.onnx`, Apache 2.0) via ONNX
Runtime, with alignment based on the MediaPipe keypoints.

Liveness is a demo challenge-response check based on MediaPipe Face Landmarker
(`models/face_landmarker.task`): the service compares the nose offset relative to the eyes across
3–20 frames (`turn_left` / `turn_right`, threshold `FACE_LIVENESS_TURN_THRESHOLD`, default `0.08`)
or the eye aspect ratio (`blink`, threshold `FACE_LIVENESS_BLINK_THRESHOLD`, default `0.19`). This is
not protection against professional photo, video or deepfake attacks.

## Status

The project is at Phase 4 (Face Enrollment): authentication, face detection, quality checks,
embeddings, matching and basic liveness are implemented. See the roadmap in [PLAN.md](PLAN.md) for
the current checklist and next tasks.
