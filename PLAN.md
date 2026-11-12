
# NextFace — Project Plan & Technical Specification

> The main project document. It describes the architecture, technologies, component interaction, business logic, and development sequence.
>
> The AI developer must use this document as the primary source of requirements and gradually update the task completion statuses.

---

# 1. Project Overview

## 1.1. Project Description

NextFace is an educational prototype of a centralized user biometric authentication system using face recognition.

The main idea is to give third-party websites the ability to use NextFace to authenticate their users.

Instead of implementing their own registration and login system, a third-party developer integrates NextFace into their website using a small JavaScript SDK.

The user is redirected to the official NextFace website, authenticates by face, and then returns to the website from which authorization was started.

All user data, passwords, and biometric templates are stored and processed on the NextFace side.

Third-party websites must not independently:
- Register users in NextFace.
- Store passwords of NextFace users.
- Obtain face photographs.
- Obtain biometric embeddings.
- Perform face recognition.
- Process biometric data.

## 1.2. Main Goals

- Create a centralized user authentication system.
- Implement user registration via email and password.
- Implement biometric profile enrollment.
- Implement authorization via face recognition.
- Implement basic Liveness detection.
- Provide users with a Dashboard.
- Provide developers with a Developer Portal.
- Create a simple JavaScript SDK.
- Demonstrate NextFace integration with a third-party website.

## 1.3. Project Constraints

The project is an educational prototype, not a commercial biometric security system.

Do not implement:
- Kubernetes.
- Jenkins.
- CI/CD.
- Nginx.
- Redis.
- Microservices for each individual function.
- Mobile applications.
- A proprietary neural network for face recognition.
- Full-fledged commercial infrastructure.
- A complex monitoring system.

Do not complicate the architecture without necessity.

---

# 2. Technology Stack

## 2.1. Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS
- shadcn/ui
- MediaDevices API for browser camera access.

Responsibilities:
- Displaying the user interface.
- Registration and login.
- Working with the camera.
- Displaying the face recognition process.
- Dashboard.
- Developer Portal.
- Managing user settings.
- JavaScript SDK integration.

## 2.2. Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Validation
- JWT
- BCrypt
- PostgreSQL Driver

Responsibilities:
- User registration.
- User authorization.
- JWT generation and validation.
- User profile management.
- Biometric profile management.
- Third-party application management.
- Processing integration requests.
- Creating and validating one-time authorization codes.
- Interaction with the Face Service.
- Working with PostgreSQL.

## 2.3. Face Recognition Service

- Python 3
- FastAPI
- OpenCV
- MediaPipe
- ONNX Runtime
- NumPy
- Pydantic

Responsibilities:
- Receiving face images.
- Face detection.
- Image quality checks.
- Obtaining facial landmarks.
- Generating face embeddings.
- Comparing embeddings.
- Basic Liveness detection.

IMPORTANT:

MediaPipe is used for face detection and landmark identification, but it does not perform identity identification by itself.

Identification requires a separate model that generates face embeddings.

Do not treat an ordinary RGB camera as a real 3D scanner. The project uses face geometry estimation from a regular image.

## 2.4. Database

PostgreSQL is the only primary database of the project.

It is used to store:
- Users.
- Passwords as hashes.
- Biometric templates.
- Third-party applications.
- Authorization codes.
- Authorization history.

Do not use Redis.

## 2.5. General Principles

- REST API for component communication.
- JSON for data transfer.
- JWT for NextFace user sessions.
- HTTPS is mandatory in real deployment.
- Store all secrets in environment variables.
- Do not store passwords in plaintext.
- Do not transfer embeddings to third-party websites.
- Do not place the Client Secret in the frontend.
- Do not store biometric data in localStorage.

---

# 3. System Architecture

## 3.1. Overall Structure

```text
NextFace/
│
├── frontend/
│   ├── app/
│   ├── components/
│   ├── hooks/
│   ├── services/
│   ├── lib/
│   └── public/
│
├── backend/
│   └── src/main/
│       ├── java/com/nextface/
│       │   ├── auth/
│       │   ├── user/
│       │   ├── face/
│       │   ├── application/
│       │   ├── integration/
│       │   ├── audit/
│       │   ├── config/
│       │   └── common/
│       │
│       └── resources/
│           └── application.yml
│
├── face-service/
│   ├── app/
│   │   ├── detection/
│   │   ├── recognition/
│   │   ├── liveness/
│   │   ├── schemas/
│   │   └── routes/
│   ├── models/
│   ├── main.py
│   └── requirements.txt
│
├── sdk/
│   └── javascript/
│       ├── src/
│       ├── package.json
│       └── tsconfig.json
│
├── demo-client/
│   └── Next.js test application
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

## 3.2. System Components

### Frontend

A single NextFace web application.

Contains:
- Landing Page.
- Registration.
- Login.
- Face Scanner.
- Dashboard.
- Profile Settings.
- Developer Portal.
- Application Details.

### Backend

A single Spring Boot application with a modular architecture.

Modules:

1. Auth Module.
2. User Module.
3. Face Module.
4. Application Module.
5. Integration Module.
6. Audit Module.

Do not split these modules into separate Spring Boot applications.

### Face Service

A single Python FastAPI application.

It contains:
- Face Detection.
- Face Embedding Generation.
- Face Matching.
- Liveness Detection.

Do not create separate Python services for each function.

### Database

A single PostgreSQL database for all NextFace components.

The Face Service must not connect directly to PostgreSQL. All operations of saving and retrieving user biometric templates are performed through the Spring Boot Backend.

---

# 4. Component Communication

## 4.1. Frontend → Backend

The Frontend interacts with the Backend via REST API.

Example:

```text
Frontend
   |
   | POST /api/auth/register
   | Content-Type: application/json
   |
   v
Spring Boot Backend
   |
   v
PostgreSQL
```

The Frontend must not access PostgreSQL directly.

## 4.2. Backend → Face Service

The Backend interacts with the Python Face Service via HTTP REST API.

Example:

```text
Spring Boot
    |
    | POST /face/embedding
    | Image data
    |
    v
FastAPI
    |
    v
Face Recognition Model
    |
    v
Embedding
    |
    v
Spring Boot
```

The Face Service returns analysis results but does not manage users and does not issue JWTs.

## 4.3. Backend → PostgreSQL

Spring Boot uses Spring Data JPA to work with the database.

All operations with users, applications, biometric profiles, and authorization codes are performed through the Backend.

## 4.4. Overall Diagram

```text
                  USER
                   |
                   v
             NEXTFACE FRONTEND
                   |
                   | REST API
                   v
             SPRING BOOT
              /         \
             /           \
            v             v
       POSTGRESQL     FACE SERVICE
                           |
                           v
                    FACE RECOGNITION
```

---

# 5. Database Design

Create the following tables.

## 5.1. users

Stores user accounts.

Fields:

- id: UUID, Primary Key.
- email: VARCHAR, Unique, Not Null.
- password_hash: VARCHAR, Not Null.
- created_at: TIMESTAMP.
- updated_at: TIMESTAMP.
- enabled: BOOLEAN.

## 5.2. face_profiles

Stores biometric templates.

Fields:

- id: UUID, Primary Key.
- user_id: UUID, Foreign Key, Unique.
- embedding: BYTEA.
- model_version: VARCHAR.
- created_at: TIMESTAMP.
- updated_at: TIMESTAMP.

Do not store face photographs unless necessary.

The embedding is sensitive biometric data. Restrict access and protect the data at rest.

## 5.3. applications

Stores third-party applications that use NextFace.

Fields:

- id: UUID, Primary Key.
- owner_id: UUID, Foreign Key.
- name: VARCHAR.
- client_id: VARCHAR, Unique.
- client_secret_hash: VARCHAR.
- redirect_url: VARCHAR.
- created_at: TIMESTAMP.
- enabled: BOOLEAN.

The Client Secret is generated when the application is created.

Show it to the owner only at creation time or upon an explicit reissue.

## 5.4. authorization_codes

Stores temporary authorization codes.

Fields:

- id: UUID, Primary Key.
- user_id: UUID, Foreign Key.
- application_id: UUID, Foreign Key.
- code_hash: VARCHAR.
- expires_at: TIMESTAMP.
- used: BOOLEAN.
- created_at: TIMESTAMP.

Codes:
- Are one-time.
- Have a short lifetime.
- Are stored as a hash.
- Are bound to a specific application.
- Cannot be reused.

## 5.5. audit_logs

Stores the event history.

Fields:

- id: UUID, Primary Key.
- user_id: UUID, Foreign Key, Nullable.
- application_id: UUID, Foreign Key, Nullable.
- event_type: VARCHAR.
- success: BOOLEAN.
- created_at: TIMESTAMP.

Do not log passwords, JWTs, Client Secrets, face images, or embeddings.

---

# 6. Authentication Logic

## 6.1. Registration

The user registers via email and password.

Sequence:

1. The user opens the Registration Page.
2. Enters email and password.
3. The Frontend sends POST /api/auth/register.
4. The Backend validates the data.
5. The Backend checks email uniqueness.
6. The password is hashed with BCrypt.
7. A user is created in PostgreSQL.
8. The Backend returns the registration result.
9. The user proceeds to face enrollment.

After successful registration, either issue a JWT or prompt the user to log in.

## 6.2. Password Login

1. The user enters email and password.
2. The Frontend sends POST /api/auth/login.
3. The Backend retrieves the user from PostgreSQL.
4. The Backend verifies the password via BCrypt.
5. On successful verification, it generates a JWT.
6. The Frontend receives the token and creates a user session.

## 6.3. JWT

Use JWT to authorize requests to the NextFace Backend.

Requirements:

- Sign tokens with a secure key.
- Specify an expiration.
- Verify the signature and expiration.
- Do not put sensitive information in the payload.
- Protect the API via Spring Security.
- Use a short-lived access token.

For the educational prototype, a refresh token may be implemented separately if it is actually needed.

---

# 7. Face Recognition Logic

## 7.1. Face Detection

Use MediaPipe for:
- Face detection.
- Landmark identification.
- Checking the face position in the frame.
- Verifying that there is exactly one face in the frame.

If no face is found, return a clear error.

## 7.2. Face Embedding

Use a separate face recognition model compatible with ONNX Runtime.

Algorithm:

1. Receive the image.
2. Find the face.
3. Perform face alignment.
4. Prepare the image for the model.
5. Obtain the embedding.
6. Normalize the embedding.
7. Return the embedding to the Backend.

Use the same model and the same preprocessing for enrollment and authorization.

Pin the model version.

## 7.3. Face Matching

Use cosine similarity or another method provided by the chosen model for comparison.

The comparison result must contain:

- similarity score.
- match result.
- model version.

Keep the match threshold in configuration.

Do not choose the threshold arbitrarily and do not assume it is universal: validate it on test samples.

## 7.4. Liveness Detection

Implement a simple demonstration liveness check.

Possible actions:

- Turn the head left.
- Turn the head right.
- Blink.

Use MediaPipe landmarks to analyze movement.

IMPORTANT:

This is a basic Liveness demonstration, not full protection against professional attacks using photographs, videos, or deepfakes.

---

# 8. Face Enrollment Flow

Biometric profile enrollment.

```text
USER
 |
 v
Login / Registration
 |
 v
Open Face Scanner
 |
 v
Request Camera Permission
 |
 v
Capture Face
 |
 v
Frontend sends image to Backend
 |
 v
Backend calls Face Service
 |
 v
Face Detection
 |
 v
Face Alignment
 |
 v
Generate Embedding
 |
 v
Backend receives Embedding
 |
 v
Save Embedding in PostgreSQL
 |
 v
Enrollment Successful
```

Rules:

- Require an authenticated user session.
- Allow enrollment of only one face.
- Do not accept frames without a face.
- Do not accept multiple faces.
- Check image quality.
- Do not overwrite an existing embedding without explicit confirmation.
- On re-enrollment, update the biometric profile correctly.

---

# 9. Face Login Flow

```text
USER
 |
 v
Open NextFace Login
 |
 v
Select Face Login
 |
 v
Request Camera Permission
 |
 v
Capture Face
 |
 v
Send Image to Backend
 |
 v
Backend calls Face Service
 |
 v
Generate Embedding
 |
 v
Load Stored Embedding
 |
 v
Compare Embeddings
 |
 +---- No Match ----> Authentication Failed
 |
 +---- Match
         |
         v
   Generate JWT
         |
         v
   Authentication Successful
```

Rules:

- The user must exist.
- The user must have a registered Face Profile.
- Return an error if no face is present.
- If image quality is low, prompt to repeat the scan.
- Do not issue a JWT on mismatch.
- Limit the number of attempts.
- Record the result in audit_logs.

---

# 10. Third-party Integration

## 10.1. Main Idea

A third-party website developer must be able to integrate NextFace as simply as possible.

All user accounts and biometric data reside on the NextFace side.

The third-party website must not receive:
- Passwords.
- Face photographs.
- Face embeddings.
- Internal NextFace user data.

It receives only the permitted user data and confirmation of successful authorization.

## 10.2. Developer Portal

Create a Developer Portal section.

Functions:

- Create an application.
- View the Client ID.
- Generate the Client Secret.
- Configure the Redirect URL.
- View connected applications.
- Delete an application.
- Reissue the Client Secret.

When registering an application, the Redirect URL must be validated.

Do not allow arbitrary redirect addresses.

## 10.3. Authorization Flow

```text
THIRD-PARTY WEBSITE
        |
        | User clicks Login with NextFace
        |
        v
NEXTFACE HOSTED LOGIN PAGE
        |
        v
USER AUTHENTICATION
        |
        v
FACE RECOGNITION
        |
        v
AUTHENTICATION SUCCESS
        |
        v
GENERATE ONE-TIME AUTHORIZATION CODE
        |
        v
REDIRECT TO REGISTERED REDIRECT URL
        |
        v
THIRD-PARTY BACKEND
        |
        | Exchange authorization code
        |
        v
NEXTFACE BACKEND
        |
        | Validate code and application
        |
        v
RETURN USER IDENTITY DATA
        |
        v
THIRD-PARTY BACKEND CREATES ITS OWN SESSION
```

The authorization code must:
- Be random and cryptographically strong.
- Have a short lifetime.
- Be used only once.
- Be bound to a specific application.
- Be stored in the DB as a hash.

The Client Secret is used only by the server side of the third-party website.

Do not transfer the Client Secret to the browser.

## 10.4. JavaScript SDK

Create a small SDK:

```typescript
import { NextFace } from "@nextface/sdk";

const nextface = new NextFace({
    clientId: "YOUR_CLIENT_ID"
});

nextface.login();
```

The SDK only performs:
- Configuration validation.
- Building the authorization URL.
- Redirecting to NextFace.
- Handling the user return if necessary.

Do not perform face recognition inside the SDK.

Do not store biometric data on the third-party website side.

For the prototype, the SDK may be a minimal JavaScript/TypeScript package.

---

# 11. REST API

## 11.1. Spring Boot API

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Registration |
| POST | /api/auth/login | Password login |
| GET | /api/auth/me | Current user |
| POST | /api/auth/logout | Logout |

### Face

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/face/enroll | Register face |
| POST | /api/face/login | Login using face |
| DELETE | /api/face/profile | Delete face profile |

### Applications

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/applications | List applications |
| POST | /api/applications | Create application |
| GET | /api/applications/{id} | Application details |
| DELETE | /api/applications/{id} | Delete application |

### Integration

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/integration/authorize | Start authorization |
| POST | /api/integration/token | Exchange authorization code |
| GET | /api/integration/userinfo | Get authorized user data |

### User

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/users/me | User profile |
| PUT | /api/users/me | Update profile |
| DELETE | /api/users/me | Delete account |

## 11.2. Python Face Service API

| Method | Endpoint | Description |
|---|---|---|
| POST | /face/detect | Detect face |
| POST | /face/embedding | Generate embedding |
| POST | /face/compare | Compare embeddings |
| POST | /face/liveness | Basic liveness check |

The Face Service must not issue JWTs or create users on its own.

---

# 12. Frontend Pages

## 12.1. Public Pages

### Landing Page

- NextFace description.
- Key advantages.
- How the system works.
- Register button.
- Login button.
- Developers button.

### Registration Page

- Email.
- Password.
- Confirm Password.
- Validation.
- Link to Login.

### Login Page

- Email and Password Login.
- Face Login.
- Link to Registration.

## 12.2. Face Scanner

The main biometric authorization screen.

Elements:

- Camera Preview.
- Face Frame.
- Scanning Animation.
- Face Detection Status.
- Liveness Instructions.
- Success State.
- Error State.
- Cancel Button.

Possible states:

1. Waiting for camera.
2. Camera permission requested.
3. Camera active.
4. Face not detected.
5. Face detected.
6. Adjust your position.
7. Liveness check.
8. Recognition in progress.
9. Authentication successful.
10. Authentication failed.

## 12.3. Dashboard

- User Information.
- Account Status.
- Face Profile Status.
- Connected Applications.
- Recent Login Activity.
- Security Settings.

## 12.4. Developer Portal

- Applications List.
- Create Application.
- Application Details.
- Client ID.
- Client Secret.
- Redirect URL.
- Integration Instructions.

## 12.5. Profile Settings

- Change Email.
- Change Password.
- Delete Face Profile.
- Delete Account.
- Logout.

---

# 13. UI/UX Requirements

The project is a prototype for a Human-Computer Interaction course.

Pay special attention to the user's interaction with the interface.

Mandatory principles:

- Simple navigation.
- Clear element names.
- Minimum number of steps.
- Clear feedback.
- Visible loading states.
- Understandable errors.
- Confirmation of important actions.
- Ability to cancel scanning.
- Responsive design.
- Keyboard accessibility of the interface.

Pay special attention to the Face Scanner.

The user must always understand:
- What the system is currently doing.
- Whether the camera sees their face.
- Whether they are positioned correctly.
- Whether a check is in progress.
- Whether authorization was successful.
- Why an error occurred.

---

# 14. Security Requirements

Even for an educational prototype, follow these rules:

- Store passwords only as BCrypt hashes.
- Sign JWTs with a secret key.
- Validate JWT expiration.
- Do not store the Client Secret in the frontend.
- Do not transfer embeddings to third-party websites.
- Validate the Redirect URL.
- Use one-time authorization codes.
- Limit the lifetime of codes.
- Limit the number of Face Login attempts.
- Validate input data.
- Do not log secrets or biometrics.
- Restrict access to the Face Service.
- Do not allow direct browser access to the internal Face Service.

Biometric embeddings must be protected at rest and in transit.

For real-world use, GDPR and biometric data processing requirements must be taken into account. The educational prototype must not be presented as a production-ready system.

---

# 15. Development Roadmap

All tasks must be performed sequentially.

The AI developer must independently update the checklists in this document after implementing and verifying each task.

Statuses:
- [ ] Not Started.
- [x] Completed and tested.
- [~] In Progress.
- [!] Blocked / Needs attention.

Do not mark [x] if:
- The code is only written but not verified.
- The endpoint is not tested.
- The component does not start.
- The integration is not verified.
- The feature is only partially implemented.

## Phase 1 — Project Setup

- [ ] Create the project structure.
- [ ] Initialize Next.js.
- [ ] Initialize Spring Boot.
- [ ] Initialize FastAPI.
- [ ] Configure PostgreSQL.
- [ ] Configure the environment configuration.
- [ ] Configure CORS.
- [ ] Verify that all components start.
- [ ] Create a basic README.

**Definition of Done:** all applications start and can interact.

## Phase 2 — Backend Authentication

- [ ] Create the User entity.
- [ ] Create the UserRepository.
- [ ] Create the Registration DTO.
- [ ] Create the Login DTO.
- [ ] Implement registration.
- [ ] Add BCrypt.
- [ ] Implement Login.
- [ ] Implement JWT generation.
- [ ] Implement JWT validation.
- [ ] Configure Spring Security.
- [ ] Implement GET /api/auth/me.
- [ ] Verify registration and login via the API.

**Definition of Done:** a user can register and authenticate via JWT.

## Phase 3 — Face Service

- [ ] Create the FastAPI application.
- [ ] Integrate OpenCV.
- [ ] Integrate MediaPipe.
- [ ] Configure Face Detection.
- [ ] Verify the number of faces in the frame.
- [ ] Verify image quality.
- [ ] Choose a face recognition model.
- [ ] Verify the model license.
- [ ] Configure ONNX Runtime.
- [ ] Implement Face Alignment.
- [ ] Implement Embedding Generation.
- [ ] Implement Embedding Normalization.
- [ ] Implement Face Matching.
- [ ] Implement basic Liveness.
- [ ] Add error handling.
- [ ] Test the Face Service API.

**Definition of Done:** the Face Service is able to detect a face, create an embedding, and compare two templates.

## Phase 4 — Face Enrollment

- [ ] Create the FaceProfile entity.
- [ ] Create the FaceProfileRepository.
- [ ] Implement POST /api/face/enroll.
- [ ] Validate the user's JWT.
- [ ] Receive the image from the frontend.
- [ ] Pass the image to the Face Service.
- [ ] Obtain the embedding.
- [ ] Save the embedding in PostgreSQL.
- [ ] Implement Face Profile update.
- [ ] Implement Face Profile deletion.
- [ ] Verify the full face enrollment scenario.

**Definition of Done:** a registered user can link their face to the account.

## Phase 5 — Face Authentication

- [ ] Implement POST /api/face/login.
- [ ] Receive the face image.
- [ ] Generate the embedding.
- [ ] Retrieve the stored Face Profile.
- [ ] Perform Face Matching.
- [ ] Configure the threshold.
- [ ] Handle the absence of a match.
- [ ] Handle the absence of a Face Profile.
- [ ] Implement attempt limiting.
- [ ] Generate a JWT on successful recognition.
- [ ] Record the result in audit_logs.
- [ ] Verify successful and unsuccessful authorization.

**Definition of Done:** a user can log in to NextFace via face recognition.

## Phase 6 — Frontend

- [ ] Create the Landing Page.
- [ ] Create the Registration Page.
- [ ] Create the Login Page.
- [ ] Implement registration forms.
- [ ] Implement login forms.
- [ ] Implement JWT session handling.
- [ ] Create the Camera Component.
- [ ] Implement camera permission acquisition.
- [ ] Create the Face Scanner UI.
- [ ] Implement scanning states.
- [ ] Connect Face Enrollment.
- [ ] Connect Face Login.
- [ ] Create the Dashboard.
- [ ] Create Profile Settings.
- [ ] Create Connected Applications.
- [ ] Create the Developer Portal.
- [ ] Verify interface responsiveness.
- [ ] Verify error handling.

**Definition of Done:** the main features are available through the graphical interface.

## Phase 7 — Third-party Integration

- [ ] Create the Application entity.
- [ ] Create the ApplicationRepository.
- [ ] Implement application registration.
- [ ] Generate the Client ID.
- [ ] Generate the Client Secret.
- [ ] Store the Client Secret as a hash.
- [ ] Implement Redirect URL validation.
- [ ] Create the AuthorizationCode entity.
- [ ] Implement authorization code generation.
- [ ] Implement expiration.
- [ ] Implement one-time use of the code.
- [ ] Implement Client ID validation.
- [ ] Implement Client Secret validation.
- [ ] Implement authorization code exchange.
- [ ] Implement issuance of permitted user data.
- [ ] Create a minimal JavaScript SDK.
- [ ] Implement the login() method.
- [ ] Create the demo-client.
- [ ] Verify the full integration scenario.

**Definition of Done:** a third-party website can use NextFace to authorize a user.

## Phase 8 — Testing and Finalization

- [ ] Verify user registration.
- [ ] Verify password login.
- [ ] Verify JWT.
- [ ] Verify face enrollment.
- [ ] Verify face login.
- [ ] Verify the absence of a face.
- [ ] Verify multiple faces in the frame.
- [ ] Verify low image quality.
- [ ] Verify Liveness.
- [ ] Verify an incorrect password.
- [ ] Verify an expired JWT.
- [ ] Verify an incorrect authorization code.
- [ ] Verify code reuse.
- [ ] Verify an incorrect Redirect URL.
- [ ] Verify the demo-client integration.
- [ ] Verify API protection.
- [ ] Fix discovered bugs.
- [ ] Update the README.
- [ ] Add project launch instructions.

**Definition of Done:** all main user scenarios work and are verified.

---

# 16. AI Developer Instructions

This section is mandatory for the AI developer.

Follow these rules when working on NextFace.

## 16.1. General Rules

1. Study the current project structure before starting work.
2. Read this PROJECT_PLAN.md in full.
3. Check which tasks have already been completed.
4. Do not rewrite existing working code without necessity.
5. Do not create duplicate classes and components.
6. Follow the existing architecture.
7. Do not add technologies that are not in the Technology Stack without necessity.
8. Do not complicate the project.
9. Do not implement several large phases at once.
10. After completing each phase, verify that it works.

## 16.2. Task Management

After completing each task:

1. Verify the result.
2. Run the necessary tests.
3. Verify component interaction.
4. Update the corresponding checkbox in PROJECT_PLAN.md.
5. If necessary, add a brief note about the implementation.

Do not mark a task as completed if it has not been verified.

If a task is partially completed, use [~].

If a task is blocked, use [!] and write the reason.

## 16.3. Architecture Rules

- The Frontend communicates with the Backend via REST API.
- The Backend communicates with the Face Service via HTTP.
- The Backend is the only component that works with PostgreSQL.
- The Face Service is responsible only for image processing and analysis.
- JWTs are issued only by the Spring Boot Backend.
- All user data belongs to NextFace.
- The SDK does not perform face recognition.
- Third-party websites do not receive biometric data.
- Do not use an OAuth Server or additional infrastructure components.

For integration, use the simple custom one-time authorization code mechanism described in section 10.

## 16.4. Code Quality

- Use clear class and method names.
- Separate Controller, Service, and Repository.
- Use DTOs for the API.
- Do not put business logic in the Controller.
- Add error handling.
- Validate incoming requests.
- Do not duplicate business logic.
- Follow TypeScript typing.
- Use Pydantic schemas in FastAPI.
- Add tests for critical scenarios.

## 16.5. Work Process

Work in small iterations.

For each iteration:

1. Define a specific task.
2. Study the related files.
3. Implement the task.
4. Verify the result.
5. Update the checklist.
6. Report what has been done and what remains.

Do not move on to the next large phase if the current one contains critical errors.

If a contradiction is found between this document and the existing implementation, first describe the problem and propose a solution rather than silently changing the architecture.

---

# 17. Project Completion Criteria

The project is considered complete when:

- [ ] A user can register an account.
- [ ] A user can log in via email and password.
- [ ] A user can enroll a face.
- [ ] The system can recognize a registered user.
- [ ] The system issues a JWT after successful authorization.
- [ ] A user can manage their profile.
- [ ] A developer can register a third-party application.
- [ ] A developer can obtain a Client ID and Client Secret.
- [ ] A third-party website can redirect the user to NextFace.
- [ ] NextFace can return a one-time authorization code.
- [ ] A third-party server can exchange the code for permitted user data.
- [ ] The SDK provides a simple way to start the authorization process.
- [ ] The Demo Client demonstrates successful integration.
- [ ] The main scenarios are tested.
- [ ] The README contains launch instructions.

---

# 18. Current Project Status

The AI Developer must keep this section up to date.

**Current Phase:** Not Started

**Last Completed Task:** None

**Current Task:** None

**Known Issues:** None

**Next Recommended Action:** Phase 1 — Project Setup

**Last Updated:** Not Started

Update this section after every work session.

Do not delete the history of completed tasks and do not reset already checked checkboxes.

---

# END OF PROJECT PLAN
