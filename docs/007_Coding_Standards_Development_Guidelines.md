
# 08_Coding_Standards_Development_Guidelines

## Abnormal Management System (AMS)

**Version:** 1.1  
**Status:** Draft  
**Author:** Nguyen Hoa Thuan  

---

## 1. Purpose

This document defines the coding standards and development guidelines for the Abnormal Management System (AMS).

It is used by:

- Human developers
- AI coding agents such as Qwen Code, Gemini CLI, Claude Code, Cursor, or Copilot
- Reviewers and testers

The goal is to keep the codebase:

```text
Consistent
Simple
Maintainable
Easy for AI tools to extend
Aligned with AMS business rules
```

---

## 2. Change Log

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-07-31 | Initial draft |
| 1.1 | 2026-08-01 | Added concrete backend standards, frontend standards, API response standards, Oracle/JPA standards, AI coding agent rules, examples, and Definition of Done |

---

## 3. General Principles

All code must follow these principles:

```text
1. Keep the MVP scope small.
2. Do not add features not defined in the PRD or specifications.
3. Follow the existing project structure.
4. Reuse existing utilities before creating new ones.
5. Prefer simple, explicit code over clever code.
6. Backend is the source of truth for security and business rules.
7. Frontend validation is for user experience only.
8. All APIs must use the standard response format.
9. All business errors must use BusinessException.
10. Do not expose entities directly in REST APIs.
```

---

## 4. Documentation Rules

The source of truth for AMS is:

```text
docs/00_PRD.md
docs/01_DATABASE_DESIGN.md
docs/02_BUSINESS_RULES.md
docs/03_REST_API.md
docs/04_SCREEN_SPEC.md
docs/05_SYSTEM_ARCHITECTURE.md
docs/06_SECURITY_DESIGN.md
docs/07_CODING_STANDARDS.md
docs/TASKS.md
docs/database.sql
```

Rules:

```text
Do not change business rules inside code without updating documentation.
Do not invent new API endpoints without updating REST API Specification.
Do not add database columns without updating Database Design and database.sql.
If a requirement is unclear, list assumptions before implementing.
```

---

# 5. Backend Standards

## 5.1 Backend Technology Rules

```text
Java 21
Spring Boot 3
Spring Web
Spring Data JPA
Spring Security
Jakarta Validation
Oracle Database
Maven
JWT authentication
SLF4J / Logback logging
```

Do not add major dependencies unless necessary.

Avoid unless approved:

```text
Lombok
MapStruct
GraphQL
Spring WebFlux
Redis
Kafka
Elasticsearch
```

MVP decision:

```text
Do not use Lombok.
Use standard Java getters/setters.
Use Java records for DTOs.
Use manual mapping in service layer.
```

---

## 5.2 Backend Package Structure

Base package:

```text
com.ams
```

Recommended structure:

```text
com.ams
├── AmsApplication.java
├── common
│   ├── ApiResponse.java
│   ├── PageResponse.java
│   └── Constants.java
├── config
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   ├── JpaConfig.java
│   └── FileStorageConfig.java
├── security
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsServiceImpl.java
│   └── CurrentUser.java
├── exception
│   ├── GlobalExceptionHandler.java
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   └── ErrorResponse.java
├── audit
│   ├── AuditLog.java
│   ├── AuditLogRepository.java
│   └── AuditService.java
├── user
│   ├── UserInfo.java
│   ├── UserRepository.java
│   ├── UserService.java
│   ├── UserController.java
│   └── dto
├── master
│   ├── ProcessStep.java
│   ├── Department.java
│   ├── ProcessStepRepository.java
│   ├── DepartmentRepository.java
│   ├── ProcessStepService.java
│   ├── DepartmentService.java
│   ├── ProcessStepController.java
│   ├── DepartmentController.java
│   └── dto
├── abnormal
│   ├── Abnormal.java
│   ├── AbnormalNoCounter.java
│   ├── AbnormalRepository.java
│   ├── AbnormalService.java
│   ├── AbnormalController.java
│   ├── AbnormalNoGenerator.java
│   └── dto
└── image
    ├── AbnormalImage.java
    ├── ImageRepository.java
    ├── ImageService.java
    ├── ImageController.java
    └── FileStorageService.java
```

---

## 5.3 Backend Naming Conventions

| Item | Convention | Example |
|---|---|---|
| Package | lowercase | `com.ams.abnormal` |
| Class | PascalCase | `AbnormalService` |
| Interface | PascalCase | `UserRepository` |
| Method | camelCase | `createAbnormal()` |
| Variable | camelCase | `abnormalId` |
| Constant | UPPER_SNAKE_CASE | `DEFAULT_PAGE_SIZE` |
| REST path | kebab-case | `/api/process-steps` |
| JSON field | camelCase | `abnormalNo` |
| Database table | UPPER_SNAKE_CASE | `ABNORMAL_IMAGE` |
| Database column | UPPER_SNAKE_CASE | `CREATE_TIME` |

---

## 5.4 Layer Rules

```text
Controller
  - Handles HTTP requests
  - Validates request DTO
  - Calls service
  - Returns ApiResponse
  - No business logic

Service
  - Contains business logic
  - Enforces business rules
  - Manages transactions
  - Maps entity to DTO
  - Calls repositories and other services

Repository
  - Data access only
  - Extends Spring Data JPA
  - No business logic

Entity
  - Maps to Oracle table
  - Not returned directly from controller

DTO
  - Used for API request and response
  - Implemented as Java record where possible
```

---

## 5.5 Entity Standards

Rules:

```text
Use @Entity and @Table.
Use @Id and @GeneratedValue with Oracle sequence.
Use allocationSize = 1.
Do not use GenerationType.IDENTITY.
Use Long for NUMBER(19) IDs.
Use LocalDateTime for TIMESTAMP columns.
Use LocalDate for DATE-only columns.
Add @Column(name = "...") for all columns.
Do not expose entities in REST responses.
```

Example:

```java
@Entity
@Table(name = "ABNORMAL")
public class Abnormal {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "seq_abnormal"
    )
    @SequenceGenerator(
        name = "seq_abnormal",
        sequenceName = "SEQ_ABNORMAL",
        allocationSize = 1
    )
    @Column(name = "ABNORMAL_ID")
    private Long abnormalId;

    @Column(name = "ABNORMAL_NO", nullable = false, unique = true, length = 30)
    private String abnormalNo;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "PRIORITY", nullable = false, length = 20)
    private String priority;

    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    @Column(name = "DUE_DATE")
    private LocalDate dueDate;

    @Column(name = "CREATE_TIME", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "UPDATE_TIME")
    private LocalDateTime updateTime;
}
```

---

## 5.6 DTO Standards

Rules:

```text
Use Java records for DTOs.
Use separate request and response DTOs.
Use Jakarta Validation annotations on request DTOs.
Do not include password or password hash in response DTOs.
Do not accept server-generated fields from client.
```

Example request DTO:

```java
public record CreateAbnormalRequest(

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    String title,

    @NotBlank(message = "Description is required")
    String description,

    @NotNull(message = "Process Step is required")
    Long processStepId,

    @NotNull(message = "Responsible Department is required")
    Long departmentId,

    @NotBlank(message = "Invalid priority")
    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "Invalid priority")
    String priority,

    LocalDate dueDate
) {}
```

Example response DTO:

```java
public record AbnormalResponse(
    Long abnormalId,
    String abnormalNo,
    String title,
    String description,
    Long processStepId,
    String processStepName,
    Long departmentId,
    String departmentName,
    String priority,
    String status,
    LocalDate dueDate,
    Long reporterId,
    String reporterName,
    LocalDateTime createTime,
    LocalDateTime updateTime
) {}
```

---

## 5.7 Controller Standards

Rules:

```text
Use @RestController.
Use @RequestMapping with /api prefix.
Use plural nouns.
Use @Valid for request body validation.
Return ApiResponse.
Use correct HTTP status.
Keep controllers thin.
```

Example:

```java
@RestController
@RequestMapping("/api/abnormals")
public class AbnormalController {

    private final AbnormalService abnormalService;

    public AbnormalController(AbnormalService abnormalService) {
        this.abnormalService = abnormalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AbnormalResponse> create(
            @Valid @RequestBody CreateAbnormalRequest request) {
        AbnormalResponse response = abnormalService.createAbnormal(request);
        return ApiResponse.success(
            "Abnormal " + response.abnormalNo() + " created successfully",
            response
        );
    }
}
```

---

## 5.8 Service Standards

Rules:

```text
Use @Service.
Use @Transactional for write operations.
Use constructor injection.
Do not use field injection.
Enforce business rules in service layer.
Throw BusinessException for rule violations.
Throw ResourceNotFoundException when entity is missing.
Call AuditService after successful CUD operations.
```

Example:

```java
@Service
public class AbnormalService {

    private final AbnormalRepository abnormalRepository;
    private final AbnormalNoGenerator abnormalNoGenerator;
    private final AuditService auditService;

    public AbnormalService(
            AbnormalRepository abnormalRepository,
            AbnormalNoGenerator abnormalNoGenerator,
            AuditService auditService) {
        this.abnormalRepository = abnormalRepository;
        this.abnormalNoGenerator = abnormalNoGenerator;
        this.auditService = auditService;
    }

    @Transactional
    public AbnormalResponse createAbnormal(CreateAbnormalRequest request) {
        // business logic here
        // generate abnormal number
        // save entity
        // auditService.record(...)
        // return response DTO
    }
}
```

---

## 5.9 Repository Standards

Rules:

```text
Extend JpaRepository.
Use JpaSpecificationExecutor for search endpoints.
Do not put business logic in repositories.
Avoid native SQL unless necessary.
Use derived queries or Specifications.
```

Example:

```java
public interface AbnormalRepository extends
        JpaRepository<Abnormal, Long>,
        JpaSpecificationExecutor<Abnormal> {
}
```

---

## 5.10 Validation Standards

Rules:

```text
Use Jakarta Validation annotations.
Validate request DTOs in controller with @Valid.
Validate business rules in service layer.
Return field errors using standard validation error format.
```

Common annotations:

```java
@NotNull
@NotBlank
@Size
@Pattern
@Email
@Positive
@FutureOrPresent
```

Business validation examples:

```text
Due Date cannot be earlier than Created Date.
Process Step must be ACTIVE.
Department must be ACTIVE.
Maximum 10 images per abnormal.
Status transition must be allowed.
Admin cannot deactivate own account.
```

---

## 5.11 Exception Handling Standards

Use global exception handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

Exception types:

| Exception | HTTP Status | Usage |
|---|---|---|
| `BusinessException` | 400 | Business rule violation |
| `ResourceNotFoundException` | 404 | Entity not found |
| `MethodArgumentNotValidException` | 400 | Field validation error |
| `AccessDeniedException` | 403 | Insufficient role |
| `AuthenticationException` | 401 | Authentication failure |
| `MaxUploadSizeExceededException` | 413 | File too large |
| `Exception` | 500 | Unexpected error |

Rules:

```text
Do not return stack traces to client.
Do not expose SQL errors to client.
Do not expose filesystem paths to client.
Log stack traces server-side.
Return user-friendly message to client.
```

---

## 5.12 Security Standards

Rules:

```text
Use BCrypt for passwords.
Never store plaintext passwords.
Never return password or password hash.
Use JWT for authentication.
Use stateless session management.
Enforce role rules in backend.
Do not rely on frontend hiding for security.
Use @CurrentUser or SecurityContext to get logged-in user.
Do not trust userId from client request body.
```

Endpoint protection:

```text
Public:
  POST /api/auth/login

Authenticated:
  Most endpoints

ADMIN only:
  /api/users/**
  /api/audit-logs/**
  POST /api/process-steps
  PUT /api/process-steps/{id}
  POST /api/departments
  PUT /api/departments/{id}
  DELETE /api/abnormals/{id}
```

---

## 5.13 File Upload Standards

Rules:

```text
Use multipart/form-data.
Field name: files.
Validate file size <= 5 MB.
Validate MIME type: image/jpeg or image/png.
Validate extension: jpg, jpeg, png.
Generate UUID stored file name.
Do not use original file name on disk.
Store original file name in database only.
Normalize and validate file path.
Prevent path traversal.
Delete physical file when image metadata is deleted.
```

Example stored path:

```text
./uploads/abnormal-images/2026/08/01/9f8c2a1e-44d1-4c0a-b1e2-7a9f0c3d1e11.jpg
```

---

## 5.14 Audit Log Standards

Rules:

```text
Audit CREATE, UPDATE, DELETE operations.
Audit entities:
  USER_INFO
  ABNORMAL
  ABNORMAL_IMAGE
  PROCESS_STEP
  DEPARTMENT
Use AuditService.
Write audit log in same transaction as business operation.
Do not allow audit log update or delete through API.
Only ADMIN can view audit logs.
```

AuditService example signature:

```java
public void record(
    String action,
    String entityName,
    Long entityId,
    String entityNo,
    String description
) {}
```

---

## 5.15 Configuration Standards

Rules:

```text
Use application.yml.
Separate dev and prod profiles.
Do not commit secrets.
Use environment variables for secrets.
Use ddl-auto = validate in production.
Manage schema using database.sql.
```

Recommended environment variables:

```text
AMS_DB_URL
AMS_DB_USERNAME
AMS_DB_PASSWORD
AMS_JWT_SECRET
AMS_UPLOAD_ROOT_PATH
```

---

## 5.16 Logging Standards

Rules:

```text
Use SLF4J.
Use meaningful log messages.
Log errors with exceptions.
Do not log passwords.
Do not log JWT tokens.
Do not log sensitive request bodies.
Do not return stack traces to client.
```

Good example:

```java
log.error("Failed to create abnormal record", ex);
```

Bad example:

```java
log.info("User password: " + password);
```

---

# 6. Frontend Standards

## 6.1 Frontend Technology Rules

```text
React
Vite
Material UI
Axios
React Router
JavaScript for MVP
TypeScript optional for future
```

MVP decision:

```text
Use JavaScript, not TypeScript, to keep MVP simple.
Do not use Redux.
Use React Context for authentication state.
Use custom hooks or component state for server data.
Use Axios for API calls.
```

---

## 6.2 Frontend Folder Structure

```text
frontend/src
├── main.jsx
├── App.jsx
├── theme.js
├── api
│   ├── axiosClient.js
│   ├── authApi.js
│   ├── abnormalApi.js
│   ├── imageApi.js
│   ├── userApi.js
│   ├── masterApi.js
│   └── auditApi.js
├── context
│   └── AuthContext.jsx
├── components
│   ├── common
│   │   ├── DataTable.jsx
│   │   ├── ConfirmDialog.jsx
│   │   ├── Toast.jsx
│   │   ├── PriorityChip.jsx
│   │   └── StatusChip.jsx
│   └── layout
│       ├── AppLayout.jsx
│       ├── Sidebar.jsx
│       ├── Header.jsx
│       └── ProtectedRoute.jsx
├── pages
│   ├── auth
│   │   └── Login.jsx
│   ├── abnormals
│   │   ├── AbnormalList.jsx
│   │   ├── AbnormalCreate.jsx
│   │   ├── AbnormalDetail.jsx
│   │   └── AbnormalEdit.jsx
│   ├── admin
│   │   ├── UserManagement.jsx
│   │   ├── ProcessStepManagement.jsx
│   │   ├── DepartmentManagement.jsx
│   │   └── AuditLog.jsx
│   └── Profile.jsx
├── hooks
│   ├── useAbnormals.js
│   └── useAuth.js
└── utils
    ├── constants.js
    ├── formatDate.js
    └── validation.js
```

---

## 6.3 Frontend Naming Conventions

| Item | Convention | Example |
|---|---|---|
| Component file | PascalCase | `AbnormalList.jsx` |
| Hook file | camelCase with `use` prefix | `useAbnormals.js` |
| Utility file | camelCase | `formatDate.js` |
| Component name | PascalCase | `AbnormalList` |
| Function | camelCase | `handleSubmit` |
| Constant | UPPER_SNAKE_CASE | `DEFAULT_PAGE_SIZE` |
| API module | camelCase with `Api` suffix | `abnormalApi.js` |

---

## 6.4 API Client Standards

Rules:

```text
All API calls must go through api modules.
Do not call Axios directly inside components.
Use one Axios instance.
Attach JWT token using request interceptor.
Handle 401 globally.
Extract error message from standard API error response.
```

Example:

```javascript
import axios from "axios";

const axiosClient = axios.create({
  baseURL: "/api",
});

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("accessToken");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
```

---

## 6.5 Routing Standards

Routes:

```text
/login
/abnormals
/abnormals/new
/abnormals/:id
/abnormals/:id/edit
/users
/process-steps
/departments
/audit-logs
/profile
```

Rules:

```text
/login is public.
All other routes require JWT.
Admin routes require ADMIN role.
ProtectedRoute checks token and role.
After login, redirect to /abnormals.
After logout, redirect to /login.
```

---

## 6.6 Form Standards

Rules:

```text
Mark required fields with *.
Show validation message below field.
Disable submit button while submitting.
Use controlled inputs or simple form state.
Validate before calling API.
Show API field errors under fields.
Show success toast after save.
Show error toast for server errors.
```

Example form state:

```javascript
const [form, setForm] = useState({
  title: "",
  description: "",
  processStepId: "",
  departmentId: "",
  priority: "MEDIUM",
  dueDate: "",
});
```

---

## 6.7 UI State Standards

Every data screen must handle:

```text
Loading state
Empty state
Error state
Success state
```

Examples:

```text
Loading: spinner or skeleton
Empty: "No abnormalities found."
Error: "Something went wrong. Please try again."
Success: toast message
```

---

## 6.8 Styling Standards

Rules:

```text
Use Material UI components.
Use MUI theme for colors and spacing.
Avoid custom CSS unless necessary.
Use responsive desktop layout.
Use chips for Priority and Status.
Use confirmation dialog before destructive actions.
```

Priority chip colors:

```text
HIGH = red
MEDIUM = amber
LOW = green
```

Status chip colors:

```text
OPEN = blue
PROCESSING = amber
CLOSED = gray
```

---

# 7. Database Standards

Rules:

```text
Use Oracle Database.
Use NUMBER(19) for IDs.
Use TIMESTAMP(6) for timestamps.
Use DATE for date-only fields.
Use VARCHAR2 for strings.
Use CLOB for long descriptions.
Use sequences for primary keys.
Use NOT NULL for mandatory fields.
Use UNIQUE constraints for unique fields.
Use CHECK constraints for enum-like values.
Use foreign keys for relationships.
Use indexes for common search columns.
```

JPA rules:

```text
Use GenerationType.SEQUENCE.
Use allocationSize = 1.
Do not use GenerationType.IDENTITY.
Use ddl-auto = validate in production.
Do not let Hibernate create production schema.
```

Naming rules:

```text
Tables: UPPER_CASE
Columns: UPPER_CASE_WITH_UNDERSCORE
Primary key: concise singular ID
Foreign key: REFERENCED_TABLE_ID
Index: IDX_TABLE_COLUMN
Constraint: PK_, UQ_, FK_, CHK_
```

---

# 8. API Standards

Rules:

```text
Use RESTful plural endpoints.
Use HTTP methods correctly.
Use standard response wrapper.
Use pagination for list endpoints.
Use camelCase JSON fields.
Use proper HTTP status codes.
Do not expose entities directly.
Do not accept server-generated fields from client.
```

Standard success response:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

Standard paginated response:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

Standard validation error:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

HTTP status rules:

```text
200 OK
201 Created
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
413 Payload Too Large
415 Unsupported Media Type
500 Internal Server Error
```

---

# 9. Testing Standards

For MVP, full test coverage is not required, but critical logic must be tested.

Recommended backend tests:

```text
Login with valid user
Login with inactive user
Login with wrong password
Create abnormal with missing title
Create abnormal with inactive process step
Create abnormal with invalid priority
Create abnormal generates abnormal number
Due date validation
Status transition validation
USER cannot delete abnormal
ADMIN can delete abnormal
Upload invalid image type
Upload oversized image
Audit log is created for abnormal create
```

Recommended frontend manual tests:

```text
Login success
Login failure
Abnormal list pagination
Abnormal search
Create abnormal
Edit abnormal
Delete abnormal as ADMIN
Delete button hidden for USER
Upload JPEG/PNG
Upload invalid file
View audit log as ADMIN
USER cannot access admin screens
```

Rules:

```text
Use JUnit 5 for backend tests.
Use AssertJ where useful.
Use MockMvc for controller tests.
Do not write brittle tests for trivial getters/setters.
Focus tests on business rules and security rules.
```

---

# 10. Git Workflow

Branch naming:

```text
feature/short-description
fix/short-description
docs/short-description
chore/short-description
```

Examples:

```text
feature/abnormal-crud
fix/login-validation
docs/update-api-spec
chore/add-gitignore
```

Commit message format:

```text
type: short message
```

Allowed types:

```text
feat
fix
docs
style
refactor
test
chore
```

Examples:

```text
feat: add abnormal create API
fix: validate due date against created date
docs: update REST API specification
test: add abnormal number generation tests
```

Rules:

```text
Keep commits small and focused.
Do not commit secrets.
Do not commit upload files.
Do not commit IDE settings.
Use .gitignore properly.
```

---

# 11. Code Quality Checklist

Before considering a task complete, check:

```text
Code compiles.
No unused imports.
No commented-out dead code.
No hardcoded secrets.
No TODO without explanation.
Business rule is implemented in backend.
Validation message matches specification.
API response uses standard format.
Error handling is centralized.
Database field names match specification.
Frontend field names match API response.
Loading, empty, and error states are handled.
Security rule is enforced in backend.
Audit log is written where required.
```

---

# 12. AI Coding Agent Rules

These rules apply to any AI coding agent:

```text
Qwen Code
Gemini CLI
Claude Code
Cursor
GitHub Copilot
```

## 12.1 Context Rules

Before writing code, the AI agent should read:

```text
docs/00_PRD.md
docs/01_DATABASE_DESIGN.md
docs/02_BUSINESS_RULES.md
docs/03_REST_API.md
docs/04_SCREEN_SPEC.md
docs/05_SYSTEM_ARCHITECTURE.md
docs/06_SECURITY_DESIGN.md
docs/07_CODING_STANDARDS.md
docs/TASKS.md
```

If the task involves database:

```text
Read docs/database.sql
```

If the task involves UI:

```text
Read docs/04_SCREEN_SPEC.md
```

If the task involves API:

```text
Read docs/03_REST_API.md
```

---

## 12.2 Implementation Rules

AI agent must:

```text
Follow existing project structure.
Follow existing naming conventions.
Use standard ApiResponse and PageResponse.
Use DTOs, not entities, in controllers.
Use Oracle sequences with allocationSize = 1.
Use LocalDateTime for timestamps.
Use LocalDate for date-only fields.
Use Jakarta Validation.
Use BusinessException for business rule violations.
Use ResourceNotFoundException for missing records.
Enforce security rules in backend.
Write audit log for CUD operations.
Keep changes small and focused.
```

AI agent must not:

```text
Add features outside MVP scope.
Change business rules silently.
Rename existing APIs without updating docs.
Expose entities directly.
Return password or password hash.
Use GenerationType.IDENTITY.
Use field injection.
Add unnecessary dependencies.
Generate huge unrelated refactors.
Store plaintext passwords.
Log JWT tokens or passwords.
```

---

## 12.3 Ambiguity Rules

If a requirement is unclear, AI agent must:

```text
Stop.
List assumptions.
Ask for confirmation.
Or implement the simplest MVP-compatible option.
```

Example:

```text
Assumption: Normal USER cannot delete abnormal because BR-ABN-011 says delete is ADMIN-only.
```

---

## 12.4 Task Output Rules

After completing a task, AI agent should report:

```text
Files changed
Features implemented
Business rules enforced
API endpoints affected
Database changes, if any
Remaining work
```

Example:

```text
Implemented:
- POST /api/abnormals
- Abnormal number generation
- Title/description validation
- Audit log for CREATE ABNORMAL

Business rules enforced:
- BR-ABN-001
- BR-ABN-002
- BR-ABN-003
- BR-ABN-007
- BR-ABNNO-001

Files changed:
- backend/src/main/java/com/ams/abnormal/AbnormalController.java
- backend/src/main/java/com/ams/abnormal/AbnormalService.java
- backend/src/main/java/com/ams/abnormal/AbnormalRepository.java
```

---

# 13. Definition of Done

A development task is done when:

```text
It matches the relevant specification.
It compiles without errors.
It follows coding standards.
Backend validation is implemented.
Business rules are enforced.
Security rules are enforced.
API response format is correct.
Error messages match specification.
Database changes match database.sql.
Frontend uses correct API fields.
Loading, empty, and error states are handled.
Audit logging is implemented where required.
No secrets are committed.
No unrelated changes are included.
```

---

# 14. Recommended Development Order

For AI-assisted development, implement in this order:

```text
1. Project setup
2. Common response and exception handling
3. Database entities
4. Security and JWT login
5. User management
6. Master data APIs
7. Abnormal CRUD
8. Abnormal number generation
9. Image upload and deletion
10. Audit log
11. Search and pagination
12. React setup
13. Login page
14. Abnormal screens
15. Admin screens
16. Manual testing
```

---

# 15. Final Summary

This project should remain:

```text
Simple
Explicit
Specification-driven
AI-friendly
MVP-focused
```

Most important rules:

```text
Do not expose entities.
Do not trust frontend.
Do not add non-MVP features.
Use standard API response.
Use Oracle sequences.
Use DTOs.
Use Jakarta Validation.
Use centralized exception handling.
Use audit logging for CUD operations.
Use AI agents task-by-task, not all-at-once.
```
