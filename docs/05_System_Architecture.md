# 05_System_Architecture

## Abnormal Management System (AMS)

**Version:** 1.1  
**Status:** Draft  
**Author:** Nguyen Hoa Thuan  

---

## 1. Purpose

Describes the technical architecture of AMS: project structure, backend and frontend design, security, file storage, concurrency handling, audit logging, exception handling, configuration, and deployment.

This document turns the PRD, Database Design, Business Rules, Screen Spec, and REST API Spec into a buildable technical blueprint.

---

## 2. Change Log

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-07-31 | Initial draft |
| 1.1 | 2026-08-01 | Added project structure, backend package layout, frontend structure, security filter chain, file storage architecture, abnormal number concurrency design, audit log implementation, exception taxonomy, configuration management, and sequence diagrams |

---

## 3. Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 21 (LTS) |
| Backend Framework | Spring Boot | 3.x |
| ORM | Spring Data JPA / Hibernate | 6.x |
| Security | Spring Security + JWT (jjwt) | 6.x |
| Validation | Jakarta Bean Validation | 3.x |
| Database | Oracle | 19c / 21c |
| Connection Pool | HikariCP | (Spring Boot default) |
| Build Tool | Maven | 3.9+ |
| Frontend | React | 18+ |
| UI Library | Material UI (MUI) | 5+ |
| Frontend Build | Vite | 5+ |
| HTTP Client | Axios | 1+ |
| Routing | React Router | 6+ |
| Language (FE) | JavaScript or TypeScript | TS recommended |

---

## 4. Project Structure

Single repository, two top-level modules:

```text
ams/
├── backend/                      # Spring Boot application
│   ├── src/main/java/com/ams/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── application-prod.yml
│   └── pom.xml
├── frontend/                     # React application
│   ├── src/
│   ├── index.html
│   └── package.json
├── docs/                         # All specification documents
└── README.md
```

---

## 5. High-Level Architecture

```text
┌─────────────────┐
│   Browser        │
│   (Chrome/Edge)  │
└────────┬─────────┘
         │ HTTPS / JSON
         ▼
┌─────────────────┐        ┌──────────────────────┐
│  React SPA       │        │  File System          │
│  (Material UI)   │        │  ./uploads/...        │
└────────┬─────────┘        └──────────▲───────────┘
         │ REST /api                   │ read/write
         ▼                             │
┌──────────────────────────────────────┴───────────┐
│                Spring Boot Application            │
│  ┌──────────┐  ┌──────────┐  ┌───────────────┐   │
│  │ Security │→ │Controller│→ │    Service     │   │
│  │ (JWT)    │  │  (REST)  │  │ (Business Logic)│  │
│  └──────────┘  └──────────┘  └───────┬───────┘   │
│                                       ▼           │
│                              ┌───────────────┐    │
│                              │  Repository    │    │
│                              │  (JPA)         │    │
│                              └───────┬───────┘    │
└──────────────────────────────────────┼────────────┘
                                        ▼
                              ┌──────────────────┐
                              │  Oracle Database  │
                              └──────────────────┘
```

---

## 6. Backend Architecture

### 6.1 Layered Architecture

```text
Controller  →  Service  →  Repository  →  Database
    │             │
    │             └── FileStorageService (images)
    │             └── AuditService (audit log)
    │             └── AbnormalNoGenerator (numbering)
    └── DTO ↔ Entity mapping
```

| Layer | Responsibility |
|---|---|
| Controller | HTTP handling, request validation, response wrapping |
| Service | Business logic, transaction boundary, rule enforcement |
| Repository | Data access via Spring Data JPA |
| Entity | JPA mapping to Oracle tables |
| DTO | Request/response objects (never expose entities) |

### 6.2 Package Structure

```text
com.ams
├── AmsApplication.java
├── common/
│   ├── ApiResponse.java            # {success, message, data}
│   ├── PageResponse.java           # {content, page, size, totals}
│   └── Constants.java
├── config/
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   ├── JpaConfig.java
│   └── FileStorageConfig.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsServiceImpl.java
│   └── CurrentUser.java            # annotation to inject logged-in user
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   └── ErrorResponse.java
├── audit/
│   ├── AuditLog.java               # entity
│   ├── AuditLogRepository.java
│   └── AuditService.java
├── user/
│   ├── UserInfo.java
│   ├── UserRepository.java
│   ├── UserService.java
│   ├── UserController.java
│   └── dto/ (UserRequest, UserResponse, LoginRequest, LoginResponse)
├── master/
│   ├── ProcessStep.java
│   ├── Department.java
│   ├── repositories, services, controllers, dtos
├── abnormal/
│   ├── Abnormal.java
│   ├── AbnormalNoCounter.java
│   ├── AbnormalRepository.java
│   ├── AbnormalService.java
│   ├── AbnormalController.java
│   ├── AbnormalNoGenerator.java
│   └── dto/
└── image/
    ├── AbnormalImage.java
    ├── ImageRepository.java
    ├── ImageService.java
    ├── ImageController.java
    └── FileStorageService.java
```

### 6.3 DTO Strategy

```text
Use Java records for DTOs (immutable, concise).
Manual mapping in Service layer (no MapStruct for MVP).
Entities are NEVER returned directly from controllers.
```

Example:

```java
public record AbnormalRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String description,
    @NotNull Long processStepId,
    @NotNull Long departmentId,
    @NotNull String priority,
    LocalDate dueDate
) {}
```

### 6.4 Pagination Convention

```text
Use Spring Data Pageable.
page is zero-based.
Default size = 10, max = 100.
Default sort = createTime,desc.
Wrap result in PageResponse.
```

---

## 7. Frontend Architecture

### 7.1 Folder Structure

```text
frontend/src
├── main.jsx
├── App.jsx
├── api/
│   ├── axiosClient.js          # Axios instance + JWT interceptor
│   ├── authApi.js
│   ├── abnormalApi.js
│   ├── imageApi.js
│   ├── userApi.js
│   └── auditApi.js
├── context/
│   └── AuthContext.jsx         # JWT + current user
├── components/
│   ├── common/ (DataTable, ConfirmDialog, Toast, Chip)
│   └── layout/ (Sidebar, Header, ProtectedRoute)
├── pages/
│   ├── auth/Login.jsx
│   ├── abnormals/ (List, Create, Detail, Edit)
│   ├── admin/ (Users, ProcessSteps, Departments, AuditLog)
│   └── Profile.jsx
├── hooks/
│   └── useAbnormals.js
├── utils/
│   └── constants.js
└── theme.js                    # MUI theme
```

### 7.2 API Client Layer

```text
Single Axios instance with baseURL = /api.
Request interceptor attaches Authorization: Bearer {token}.
Response interceptor:
  401 → clear token → redirect to /login.
  error → extract message from standard error body.
All API calls go through api/*.js modules (never inline in components).
```

### 7.3 State Management

```text
Auth state: React Context (AuthContext).
Server data: component state + custom hooks (no Redux for MVP).
Form state: react-hook-form (recommended) or controlled inputs.
```

### 7.4 Routing and Protected Routes

```text
Public:  /login
Protected (any role): /abnormals, /abnormals/*, /profile
Protected (ADMIN only): /users, /process-steps, /departments, /audit-logs
ProtectedRoute component checks JWT + role.
```

---

## 8. Security Architecture

### 8.1 Security Filter Chain

```text
Request
  → CorsFilter
  → JwtAuthenticationFilter (parse + validate token)
  → Spring Security authorization (role check)
  → Controller
```

Configuration decisions:

```text
Session: STATELESS (no HTTP session)
CSRF: disabled (stateless JWT API)
CORS: allow frontend origin
Public endpoints: POST /api/auth/login, GET /api/images/{id}/file (with token)
All others: authenticated
Admin endpoints: hasRole('ADMIN')
```

### 8.2 Authentication Flow

```text
1. POST /api/auth/login {username, password}
2. AuthenticationManager verifies credentials (BCrypt)
3. Check user STATUS = ACTIVE (BR-AUTH-001)
4. JwtTokenProvider generates token (subject = username, role claim)
5. Return {accessToken, tokenType, expiresIn, user}
6. Frontend stores token (memory or localStorage)
7. Every request: Authorization: Bearer {token}
8. JwtAuthenticationFilter validates token on each request
```

### 8.3 Password Handling

```text
Encoder: BCryptPasswordEncoder
Never store plaintext.
Never return password or hash in any response (BR-USR-004).
```

### 8.4 CORS

```text
Allowed origin: http://localhost:5173 (dev), production domain (prod)
Allowed methods: GET, POST, PUT, DELETE, OPTIONS
Allowed headers: Authorization, Content-Type
```

---

## 9. File Storage Architecture

### 9.1 Storage Layout

```text
{ams.upload.root-path}/
└── abnormal-images/
    └── 2026/
        └── 08/
            └── 01/
                └── 9f8c2a1e-44d1-4c0a-b1e2-7a9f0c3d1e11.jpg
```

```text
Root path configurable: ams.upload.root-path (default ./uploads)
Stored file name: {UUID}.{extension} (BR-IMG-006)
Database stores relative path in FILE_PATH.
```

### 9.2 Upload Flow

```text
1. POST /api/abnormals/{id}/images (multipart, field: files)
2. Validate abnormal exists (BR-IMG-009)
3. Validate count <= 10 (BR-IMG-002)
4. Validate size <= 5MB (BR-IMG-003)
5. Validate MIME type jpeg/png (BR-IMG-004)
6. Generate UUID file name
7. Write file to storage path
8. Insert ABNORMAL_IMAGE metadata row
9. Record audit log
10. Return image metadata + imageUrl
```

### 9.3 Deletion Flow

```text
Delete single image:
  delete ABNORMAL_IMAGE row → delete physical file → audit

Delete abnormal:
  for each image: delete physical file
  cascade delete ABNORMAL_IMAGE rows
  delete ABNORMAL row
  audit
```

```text
Physical file deletion is best-effort:
  if file missing, log warning but continue (do not fail the transaction).
```

---

## 10. Abnormal Number Generation Architecture

Concurrency-safe daily numbering using `ABNORMAL_NO_COUNTER`.

```text
Format: AB-YYYYMMDD-NNNNN (BR-ABNNO-001)
```

Generation logic (inside create-abnormal transaction):

```sql
-- 1. Lock the row for today (creates it if missing)
SELECT LAST_NUMBER FROM ABNORMAL_NO_COUNTER
WHERE SEQ_DATE = :today FOR UPDATE;

-- if no row:
INSERT INTO ABNORMAL_NO_COUNTER (SEQ_DATE, LAST_NUMBER) VALUES (:today, 0);

-- 2. Increment
UPDATE ABNORMAL_NO_COUNTER SET LAST_NUMBER = LAST_NUMBER + 1
WHERE SEQ_DATE = :today RETURNING LAST_NUMBER INTO :next;

-- 3. Format
ABNORMAL_NO = 'AB-' || TO_CHAR(:today,'YYYYMMDD') || '-' || LPAD(:next,5,'0')
```

```text
Row locking (FOR UPDATE) prevents duplicate numbers under concurrency.
Runs in the same transaction as abnormal insert.
Max 99999 per day → BusinessException if exceeded (BR-ABNNO-008).
```

---

## 11. Audit Log Architecture

```text
Implementation: explicit AuditService calls in Service layer.
Transaction: same transaction as the business operation (BR-AUD-006).
Scope: CREATE/UPDATE/DELETE on USER_INFO, ABNORMAL, ABNORMAL_IMAGE,
       PROCESS_STEP, DEPARTMENT (BR-AUD-002).
Access: ADMIN only (BR-AUD-008).
Append-only: no update/delete endpoints (BR-AUD-004/005).
```

AuditService signature:

```java
void record(String action, String entityName, Long entityId,
            String entityNo, String description);
// user + IP resolved from SecurityContext + request automatically
```

---

## 12. Exception Handling Architecture

Global handler via `@RestControllerAdvice`.

| Exception | HTTP | Use Case |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Field validation → field errors |
| `BusinessException` | 400 | Rule violation (custom message) |
| `ResourceNotFoundException` | 404 | Entity not found |
| `AccessDeniedException` | 403 | Wrong role |
| `AuthenticationException` | 401 | Bad/missing token |
| `MaxUploadSizeExceededException` | 413 | Image too large |
| `Exception` (fallback) | 500 | Unexpected error |

All errors return the standard error body from the REST API Spec.

---

## 13. Configuration Management

`application.yml` (shared):

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 50MB
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.OracleDialect

ams:
  upload:
    root-path: ./uploads
  jwt:
    expiration: 28800   # 8 hours (BR-AUTH-005)
```

`application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/ORCLPDB
    username: ams_dev
    password: ******
  jpa:
    show-sql: true
```

```text
Secrets (DB password, JWT secret) via environment variables, never committed.
Profiles: dev, prod.
ddl-auto = validate in all environments (schema managed by database.sql).
```

---

## 14. Key Sequence Diagrams

### 14.1 Create Abnormal

```text
Client          Controller        Service        NoGenerator     DB
  │  POST /abnormals │                │                │           │
  │─────────────────>│ validate DTO   │                │           │
  │                  │───────────────>│ begin tx       │           │
  │                  │                │───────────────>│ lock+incr │
  │                  │                │                │──────────>│
  │                  │                │<── AB-...──────│           │
  │                  │                │ insert ABNORMAL──────────>│
  │                  │                │ auditService.record()────>│
  │                  │                │ commit tx      │           │
  │                  │<── 201 ────────│                │           │
  │<── 201 + data ───│                │                │           │
```

### 14.2 Upload Image

```text
Client        Controller       ImageService     FileStorage      DB
  │ multipart    │                │                │              │
  │─────────────>│ validate       │                │              │
  │              │───────────────>│ check count/size/type         │
  │              │                │ UUID name       │              │
  │              │                │───────────────>│ write file   │
  │              │                │ insert metadata─────────────>│
  │              │                │ audit ───────────────────────>│
  │<── 201 ──────│                │                │              │
```

---

## 15. Deployment Architecture

```text
┌────────────────────────────────────────────┐
│              Application Server             │
│  ┌──────────────┐    ┌──────────────────┐  │
│  │ Nginx /       │    │ Spring Boot JAR   │  │
│  │ React static  │───>│ :8080             │  │
│  │ :80 / :443    │    │ /api/*            │  │
│  └──────────────┘    └────────┬─────────┘  │
│        serves SPA             │             │
│        + proxies /api         │             │
│                          ┌────┴─────┐       │
│                          │ ./uploads │       │
│                          └──────────┘       │
└───────────────────────────────┼─────────────┘
                                 │ JDBC :1521
                        ┌────────▼─────────┐
                        │  Oracle DB Server │
                        └──────────────────┘
```

```text
Frontend: build with `npm run build` → static files served by Nginx.
Nginx proxies /api/* to Spring Boot :8080 (avoids CORS in prod).
Backend: `java -jar ams-backend.jar --spring.profiles.active=prod`.
Database: separate Oracle server; schema created by docs/database.sql.
Uploads: persistent volume mounted at ams.upload.root-path.
```

---

## 16. Design Principles

```text
Layered architecture with clear separation of concerns.
DTO pattern — entities never leak to the API.
Stateless JWT authentication.
Centralized exception handling.
Transaction-scoped audit logging.
Configuration externalized via profiles + environment variables.
Schema managed by SQL scripts, validated by Hibernate (ddl-auto=validate).
Oracle sequence-based IDs (allocationSize = 1).
Role-based authorization enforced in backend, mirrored in UI.
```

---

