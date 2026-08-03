# 09_Implementation_Plan_Task_Breakdown

## Abnormal Management System (AMS)

**Version:** 1.1  
**Status:** Draft  
**Author:** Nguyen Hoa Thuan  

---

## 1. Purpose

This document breaks the AMS MVP into implementation phases, tasks, acceptance criteria, and execution rules.

It is intended for:

```text
Human developers
AI coding agents such as Qwen Code, Gemini CLI, Claude Code, Cursor, or Copilot
Testing and deployment activities
```

This plan is designed to be executed **task-by-task**, not all at once.

---

## 2. Change Log

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-07-31 | Initial draft |
| 1.1 | 2026-08-01 | Added task IDs, acceptance criteria, dependencies, documentation preparation phase, common infrastructure phase, audit core phase, image storage tasks, abnormal number generation tasks, frontend phase breakdown, AI agent execution rules, and Definition of Done |

---

## 3. Implementation Principles

```text
1. Build foundation before features.
2. Build backend APIs before complex frontend screens.
3. Implement common response and exception handling early.
4. Implement security early.
5. Implement audit core before business modules.
6. Implement database schema from database.sql.
7. Keep tasks small and focused.
8. Verify each phase before moving to the next.
9. Do not add features outside MVP scope.
10. Follow the specifications in docs/.
```

---

## 4. Recommended Execution Strategy

For AI-assisted development, use this approach:

```text
Step 1: Give AI the relevant specification documents.
Step 2: Give AI one task or one small phase.
Step 3: Ask AI to implement only that task.
Step 4: Review code.
Step 5: Test manually or with automated tests.
Step 6: Commit.
Step 7: Move to next task.
```

Do not use this approach:

```text
"Build the whole AMS system now."
```

Use this approach:

```text
"Implement Phase 4, Task T4.3: JwtTokenProvider."
```

---

## 5. Preconditions

Before coding starts, ensure these exist:

```text
docs/00_PRD.md
docs/01_DATABASE_DESIGN.md
docs/02_BUSINESS_RULES.md
docs/03_REST_API.md
docs/04_SCREEN_SPEC.md
docs/05_SYSTEM_ARCHITECTURE.md
docs/06_SECURITY_DESIGN.md
docs/07_CODING_STANDARDS.md
docs/08_IMPLEMENTATION_PLAN.md
docs/database.sql
AGENTS.md
```

Recommended repository structure:

```text
ams/
├── backend/
├── frontend/
├── docs/
├── AGENTS.md
├── .gitignore
└── README.md
```

---

# 6. Phase Overview

| Phase | Name | Goal |
|---|---|---|
| Phase 0 | Documentation and Repository Preparation | Prepare specs, repo, and AI context |
| Phase 1 | Project Skeleton | Create backend and frontend base projects |
| Phase 2 | Database Schema and Seed Data | Create Oracle schema and initial data |
| Phase 3 | Common Infrastructure | API response, pagination, exceptions |
| Phase 4 | Security and Authentication | JWT login, security filter chain |
| Phase 5 | Audit Core | Audit entity, repository, service |
| Phase 6 | User Management Backend | Admin user APIs |
| Phase 7 | Master Data Backend | Process Step and Department APIs |
| Phase 8 | Abnormal Management Backend | Abnormal CRUD, search, number generation |
| Phase 9 | Image Management Backend | Upload, download, delete images |
| Phase 10 | Frontend Foundation | React setup, routing, auth, layout |
| Phase 11 | Frontend Abnormal Screens | List, create, detail, edit |
| Phase 12 | Frontend Admin Screens | Users, master data, audit log |
| Phase 13 | Testing and Hardening | Functional, security, integration testing |
| Phase 5

---

# Phase 0 — Documentation and Repository Preparation

## Goal

Prepare all documentation and repository files so AI agents can work with clear context.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T0.1 | Create Git repository | Initialize repository | Repo created |
| T0.2 | Create .gitignore | Ignore build output, node_modules, uploads, IDE files, secrets | No sensitive/build files committed |
| T0.3 | Convert docs to Markdown | Convert all specification documents into `.md` | All docs readable in repo |
| T0.4 | Create database.sql | Include tables, constraints, indexes, sequences | SQL script is complete |
| T0.5 | Create AGENTS.md | Define AI coding rules | AI agents can read global rules |
| T0.6 | Create README.md | Describe project, setup steps, environment variables | Developer can follow setup |
| T0.7 | Define environment variables | DB URL, DB username, DB password, JWT secret, upload path | Documented in README |

## Phase Exit Criteria

```text
Repository exists.
All docs are in Markdown.
database.sql exists.
AGENTS.md exists.
README explains how to run backend and frontend.
```

---

# Phase 1 — Project Skeleton

## Goal

Create empty but runnable backend and frontend projects.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T1.1 | Initialize Spring Boot backend | Java 21, Spring Boot 3, Maven | Backend compiles |
| T1.2 | Add backend dependencies | Web, JPA, Security, Validation, Oracle driver | Dependencies resolve |
| T1.3 | Create backend package structure | `com.ams` with common, config, security, exception, audit, user, master, abnormal, image | Packages exist |
| T1.4 | Configure application.yml | Dev profile, Oracle datasource, JPA settings | App starts with dev config |
| T1.5 | Configure prod profile | Use environment variables | Prod config has no secrets |
| T1.6 | Initialize React frontend | Vite + React | Frontend runs |
| T1.7 | Add frontend dependencies | MUI, Axios, React Router | Frontend compiles |
| T1.8 | Create frontend folder structure | api, components, pages, context, hooks, utils | Structure exists |
| T1.9 | Add basic health check | Backend `/api/health` or simple controller | Returns OK |
| T1.10 | Verify backend and frontend run locally | Backend and frontend start without errors | Both run |

## Phase Exit Criteria

```text
Backend starts.
Frontend starts.
Package structure exists.
Configuration profiles exist.
No business logic implemented yet.
```

---

# Phase 2 — Database Schema and Seed Data

## Goal

Create Oracle database schema and initial master data.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T2.1 | Create Oracle schema | Run `database.sql` | Tables created |
| T2.2 | Create sequences | SEQ_USER_INFO, SEQ_PROCESS_STEP, etc. | Sequences exist |
| T2.3 | Create constraints | PK, FK, unique, check constraints | Constraints exist |
| T2.4 | Create indexes | Abnormal search and audit indexes | Indexes exist |
| T2.5 | Create ABNORMAL_NO_COUNTER table | Daily abnormal number counter | Table exists |
| T2.6 | Seed departments | Production, QA, Maintenance, Planning | Data visible |
| T2.7 | Seed process steps | Cutting, Assembly, Inspection, Packing | Data visible |
| T2.8 | Seed admin user | Username `admin`, BCrypt password | Admin can be used later for login |
| T2.9 | Verify schema | Check tables, constraints, sequences | Schema matches Database Design |

## Phase Exit Criteria

```text
Oracle schema matches database.sql.
Seed data exists.
Admin user password is BCrypt hashed.
No plaintext password stored.
```

---

# Phase 3 — Common Infrastructure

## Goal

Create shared backend infrastructure before business modules.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T3.1 | Create ApiResponse | `{success, message, data}` | Standard success wrapper exists |
| T3.2 | Create PageResponse | `{content, page, size, totalElements, totalPages}` | Pagination wrapper exists |
| T3.3 | Create BusinessException | For business rule violations | Exception class exists |
| T3.4 | Create ResourceNotFoundException | For missing records | Exception class exists |
| T3.5 | Create ErrorResponse | Standard error body | Error DTO exists |
| T3.6 | Create GlobalExceptionHandler | `@RestControllerAdvice` | Handles validation, business, 404, 500 |
| T3.7 | Create Constants | Roles, statuses, priorities, actions | Constants exist |
| T3.8 | Create current-user utility | Get logged-in user from SecurityContext | Utility works |
| T3.9 | Create IP utility | Get client IP for audit | Utility works |

## Phase Exit Criteria

```text
All APIs can return standard response format.
All errors can return standard error format.
BusinessException returns HTTP 400.
ResourceNotFoundException returns HTTP 404.
Stack traces are not returned to client.
```

---

# Phase 4 — Security and Authentication

## Goal

Implement JWT authentication and Spring Security.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T4.1 | Create UserInfo entity | Maps USER_INFO table | Entity compiles |
| T4.2 | Create UserRepository | Find by username | Repository works |
| T4.3 | Create UserDetailsServiceImpl | Load user for Spring Security | User loaded |
| T4.4 | Create BCrypt password encoder | Bean configuration | Passwords hashed |
| T4.5 | Create JwtTokenProvider | Generate and validate JWT | Token works |
| T4.6 | Create JwtAuthenticationFilter | Validate Bearer token per request | Protected APIs require token |
| T4.7 | Create SecurityConfig | Stateless session, CSRF disabled, endpoint rules | Security rules work |
| T4.8 | Create CORS config | Allow frontend dev origin | Frontend can call API |
| T4.9 | Create LoginRequest/LoginResponse | DTOs | DTOs exist |
| T4.10 | Create AuthController | `/api/auth/login`, `/api/auth/me`, `/api/auth/logout` | Auth endpoints work |
| T4.11 | Implement login logic | Validate credentials, ACTIVE status, generate JWT | Login success/failure works |
| T4.12 | Implement inactive user rejection | BR-AUTH-001 | Inactive user cannot login |
| T4.13 | Implement generic login error | BR-AUTH-002 | No username enumeration |
| T4.14 | Implement `/api/auth/me` | Return current user info | Works with JWT |

## Phase Exit Criteria

```text
Login returns JWT.
Invalid login returns generic error.
Inactive user cannot login.
Protected endpoint without token returns 401.
Admin endpoint with USER token returns 403.
Password is never returned.
```

---

# Phase 5 — Audit Core

## Goal

Implement audit logging infrastructure before business modules.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T5.1 | Create AuditLog entity | Maps AUDIT_LOG table | Entity compiles |
| T5.2 | Create AuditLogRepository | Save and query audit logs | Repository works |
| T5.3 | Create AuditService | `record(action, entityName, entityId, entityNo, description)` | Service works |
| T5.4 | Capture current user | From SecurityContext | Audit includes user |
| T5.5 | Capture IP address | From request context | Audit includes IP |
| T5.6 | Capture timestamp | Server time | Audit includes action time |
| T5.7 | Ensure append-only | No update/delete APIs | Audit cannot be changed |

## Phase Exit Criteria

```text
AuditService can insert audit log.
Audit log includes user, action, entity, timestamp, IP.
Audit log is written in same transaction as business operation.
No API exists to edit/delete audit logs.
```

---

# Phase 6 — User Management Backend

## Goal

Implement admin-only user management APIs.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T6.1 | Create user DTOs | CreateUserRequest, UpdateUserRequest, UserResponse | DTOs exist |
| T6.2 | Create UserService | Create, update, list users | Service works |
| T6.3 | Create UserController | `/api/users` endpoints | Endpoints work |
| T6.4 | Implement list users | Pagination, keyword, status filter | Search works |
| T6.5 | Implement create user | Hash password, default role/status | User created |
| T6.6 | Implement unique username | Case-insensitive uniqueness | Duplicate rejected |
| T6.7 | Implement update user | Optional password, status change | User updated |
| T6.8 | Implement deactivate user | No physical delete | Status set INACTIVE |
| T6.9 | Prevent self-deactivation | BR-USR-010 | Admin cannot deactivate own account |
| T6.10 | Exclude password from response | BR-USR-004 | No password/hash returned |
| T6.11 | Add audit logging | CREATE/UPDATE USER_INFO | Audit written |
| T6.12 | Restrict to ADMIN | Security rule | USER role gets 403 |

## Phase Exit Criteria

```text
ADMIN can list, create, update users.
USER cannot access user APIs.
No DELETE /api/users/{id} exists.
Passwords are hashed.
Passwords are not returned.
Audit log records user create/update.
```

---

# Phase 7 — Master Data Backend

## Goal

Implement Process Step and Department APIs.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T7.1 | Create ProcessStep entity | Maps PROCESS_STEP | Entity compiles |
| T7.2 | Create Department entity | Maps DEPARTMENT | Entity compiles |
| T7.3 | Create repositories | JPA repositories | Repos work |
| T7.4 | Create master data DTOs | Request/response DTOs | DTOs exist |
| T7.5 | Implement GET /api/process-steps | Optional status filter | Dropdowns can load ACTIVE items |
| T7.6 | Implement POST /api/process-steps | ADMIN only | Process step created |
| T7.7 | Implement PUT /api/process-steps/{id} | ADMIN only | Process step updated |
| T7.8 | Implement GET /api/departments | Optional status filter | Dropdowns can load ACTIVE items |
| T7.9 | Implement POST /api/departments | ADMIN only | Department created |
| T7.10 | Implement PUT /api/departments/{id} | ADMIN only | Department updated |
| T7.11 | Enforce unique codes | STEP_CODE, DEPARTMENT_CODE | Duplicates rejected |
| T7.12 | Implement deactivate instead of delete | STATUS = INACTIVE | No physical delete |
| T7.13 | Add audit logging | CREATE/UPDATE master data | Audit written |

## Phase Exit Criteria

```text
Authenticated users can read master data.
ADMIN can create/update master data.
USER cannot create/update master data.
No physical delete endpoints exist.
Inactive master data remains available for historical records.
```

---

# Phase 8 — Abnormal Management Backend

## Goal

Implement core abnormal CRUD, search, status, and number generation.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T8.1 | Create Abnormal entity | Maps ABNORMAL table | Entity compiles |
| T8.2 | Create AbnormalNoCounter entity | Maps ABNORMAL_NO_COUNTER | Entity compiles |
| T8.3 | Create AbnormalRepository | JpaRepository + JpaSpecificationExecutor | Repository works |
| T8.4 | Create abnormal DTOs | Create, update, list, detail responses | DTOs exist |
| T8.5 | Implement AbnormalNoGenerator | Daily number with row locking | Generates AB-YYYYMMDD-NNNNN |
| T8.6 | Implement create abnormal | Title, description, process step, department, priority, due date | Abnormal created |
| T8.7 | Enforce default status OPEN | BR-ABN-007 | New abnormal is OPEN |
| T8.8 | Assign reporter automatically | From JWT | Reporter set correctly |
| T8.9 | Validate active process step | BR-ABN-013 | Inactive step rejected |
| T8.10 | Validate active department | BR-ABN-014 | Inactive department rejected |
| T8.11 | Validate due date | BR-ABN-009 | Earlier date rejected |
| T8.12 | Implement get abnormal detail | Include names and image metadata | Detail works |
| T8.13 | Implement update abnormal | Validate rules, set UPDATE_TIME/UPDATE_BY | Update works |
| T8.14 | Implement status transitions | BR-STAT rules | Invalid transitions rejected |
| T8.15 | Implement delete abnormal | ADMIN only | USER gets 403 |
| T8.16 | Implement search | Keyword, filters, date range, pagination | Search works |
| T8.17 | Implement default sorting | CREATE_TIME descending | List sorted correctly |
| T8.18 | Add audit logging | CREATE/UPDATE/DELETE ABNORMAL | Audit written |

## Phase Exit Criteria

```text
Abnormal number is generated automatically.
Abnormal number is unique and immutable.
Mandatory fields are validated.
Inactive master data cannot be selected.
Status transitions are enforced.
Search and pagination work.
USER cannot delete abnormal.
ADMIN can delete abnormal.
Audit log records abnormal CUD operations.
```

---

# Phase 9 — Image Management Backend

## Goal

Implement secure image upload, download, and deletion.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T9.1 | Create AbnormalImage entity | Maps ABNORMAL_IMAGE | Entity compiles |
| T9.2 | Create ImageRepository | JPA repository | Repository works |
| T9.3 | Create FileStorageService | Save/delete/read files | File operations work |
| T9.4 | Configure upload root path | `ams.upload.root-path` | Configurable |
| T9.5 | Implement upload endpoint | POST `/api/abnormals/{id}/images` | Upload works |
| T9.6 | Validate abnormal exists | BR-IMG-009 | 404 if missing |
| T9.7 | Validate max 10 images | BR-IMG-002 | Over limit rejected |
| T9.8 | Validate max 5 MB | BR-IMG-003 | Large file rejected |
| T9.9 | Validate MIME type | JPEG/PNG only | Invalid type rejected |
| T9.10 | Generate UUID file name | BR-IMG-006 | Stored name is UUID |
| T9.11 | Store original file name | DB metadata only | Original name saved |
| T9.12 | Prevent path traversal | Normalize/validate path | Malicious path rejected |
| T9.13 | Implement GET /api/images/{id}/file | Stream image bytes | Image displays |
| T9.14 | Implement DELETE /api/images/{id} | Delete metadata + physical file | Image removed |
| T9.15 | Integrate abnormal deletion | Delete related images/files | Abnormal delete removes images |
| T9.16 | Add audit logging | CREATE/DELETE ABNORMAL_IMAGE | Audit written |

## Phase Exit Criteria

```text
JPEG/PNG upload works.
Invalid format rejected.
Oversized file rejected.
More than 10 images rejected.
Stored file name is UUID-based.
Image download requires JWT.
Image deletion removes metadata and physical file.
Abnormal deletion removes related images and files.
```

---

# Phase 10 — Frontend Foundation

## Goal

Create React foundation, authentication, layout, and protected routing.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T10.1 | Create Axios client | Base URL `/api` | API calls centralized |
| T10.2 | Add JWT interceptor | Attach Bearer token | Token sent automatically |
| T10.3 | Add 401 interceptor | Clear token, redirect login | Session expiry handled |
| T10.4 | Create AuthContext | Store token/user | Auth state available |
| T10.5 | Create login API module | `authApi.js` | Login call works |
| T10.6 | Create Login page | Username/password form | Login UI works |
| T10.7 | Implement login error handling | Generic/inactive messages | Errors displayed |
| T10.8 | Create ProtectedRoute | Require JWT | Unauthenticated redirected |
| T10.9 | Create AdminRoute | Require ADMIN | USER blocked |
| T10.10 | Create AppLayout | Sidebar + header | Layout works |
| T10.11 | Create role-based sidebar | Hide admin menus for USER | Menu visibility correct |
| T10.12 | Create logout | Clear token, redirect | Logout works |
| T10.13 | Create Profile page | Call `/api/auth/me` | Profile displayed |
| T10.14 | Create MUI theme | Colors, typography | UI consistent |

## Phase Exit Criteria

```text
User can log in.
Token is stored.
Protected routes require JWT.
Admin routes require ADMIN.
Logout clears token.
Sidebar hides admin items for USER.
```

---

# Phase 11 — Frontend Abnormal Screens

## Goal

Implement abnormal list, create, detail, and edit screens.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T11.1 | Create abnormal API module | List, detail, create, update, delete | API module works |
| T11.2 | Create master data API module | Process steps, departments | Dropdown data loads |
| T11.3 | Create Abnormal List page | Table, filters, pagination | List works |
| T11.4 | Implement search filters | Keyword, status, priority, step, department, date range | Search works |
| T11.5 | Implement priority/status chips | Colored chips | UI matches spec |
| T11.6 | Implement row actions | View, edit, delete admin-only | Actions correct |
| T11.7 | Create Abnormal Create page | Form fields and validation | Create works |
| T11.8 | Load ACTIVE master data | Dropdowns show ACTIVE only | Inactive not selectable |
| T11.9 | Implement due date validation | Due date >= created date | Validation works |
| T11.10 | Redirect after create | Go to detail page | User can upload images |
| T11.11 | Create Abnormal Detail page | Show all fields and images | Detail works |
| T11.12 | Implement image gallery | Thumbnails, preview | Images display |
| T11.13 | Implement image upload | Max 10, 5 MB, JPEG/PNG | Upload works |
| T11.14 | Implement image delete | Confirmation dialog | Image removed |
| T11.15 | Create Abnormal Edit page | Edit fields and status | Edit works |
| T11.16 | Implement status transition rules | Dropdown options by current status | Invalid status hidden/rejected |
| T11.17 | Implement delete abnormal | ADMIN only confirmation | USER cannot delete |
| T11.18 | Implement loading/empty/error states | All screens | States handled |

## Phase Exit Criteria

```text
USER can create/edit abnormal.
USER cannot delete abnormal.
ADMIN can delete abnormal.
Images upload with validation.
Status transitions follow rules.
Search and pagination work.
All screens handle loading/empty/error states.
```

---

# Phase 12 — Frontend Admin Screens

## Goal

Implement admin-only screens.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T12.1 | Create user API module | List, create, update | API module works |
| T12.2 | Create User Management page | Table, search, status filter | User list works |
| T12.3 | Create user dialog/page | Form with role/status | User created |
| T12.4 | Edit user | Optional password, status | User updated |
| T12.5 | Deactivate/activate user | No physical delete | Status changed |
| T12.6 | Prevent self-deactivation UI | Disable button for current admin | UI matches rule |
| T12.7 | Create Process Step page | List, create, edit, deactivate | Master data works |
| T12.8 | Create Department page | List, create, edit, deactivate | Master data works |
| T12.9 | Create audit API module | List audit logs | API module works |
| T12.10 | Create Audit Log page | Filters, pagination, read-only table | Audit log works |
| T12.11 | Restrict admin screens | ADMIN only | USER cannot access |

## Phase Exit Criteria

```text
ADMIN can manage users.
ADMIN can manage process steps.
ADMIN can manage departments.
ADMIN can view audit logs.
USER cannot access admin screens.
No physical delete is available for users/master data.
```

---

# Phase 13 — Testing and Hardening

## Goal

Verify business rules, security rules, and main flows.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T13.1 | Test login flows | Valid, invalid, inactive | Works |
| T13.2 | Test JWT protection | Missing, expired, tampered token | 401 handled |
| T13.3 | Test role authorization | USER accessing admin APIs | 403 returned |
| T13.4 | Test abnormal validation | Missing fields, invalid enums | Errors correct |
| T13.5 | Test abnormal number generation | Multiple creates, same day | Unique numbers |
| T13.6 | Test status transitions | Valid/invalid transitions | Rules enforced |
| T13.7 | Test due date rule | Earlier date rejected | Validation works |
| T13.8 | Test image upload rules | Type, size, count | Rules enforced |
| T13.9 | Test image deletion | Metadata + file removed | Cleanup works |
| T13.10 | Test abnormal deletion | ADMIN only, image cleanup | Works |
| T13.11 | Test audit logs | CUD operations recorded | Audit complete |
| T13.12 | Test search/pagination | Filters, sorting, page size | Works |
| T13.13 | Test error responses | No stack traces exposed | Secure errors |
| T13.14 | Test password security | Hashed, not returned | Secure |
| T13.15 | Run security checklist | From Security Design Spec | Checklist passes |

## Phase Exit Criteria

```text
All MVP functional flows work.
All security checklist items pass.
All critical business rules pass.
No sensitive data exposed.
Audit logs are complete.
```

---

# Phase 14 — Deployment Preparation

## Goal

Prepare AMS for production deployment.

| Task ID | Task | Details | Acceptance Criteria |
|---|---|---|---|
| T14.1 | Build backend JAR | Maven package | JAR created |
| T14.2 | Build frontend | Vite build | Static files created |
| T14.3 | Configure environment variables | DB, JWT secret, upload path | No secrets in code |
| T14.4 | Configure Oracle production schema | Run database.sql | Prod schema ready |
| T14.5 | Configure upload volume | Persistent storage | Uploads persist |
| T14.6 | Configure Nginx | Serve frontend, proxy `/api` | Frontend and API work |
| T14.7 | Enable HTTPS | TLS certificate | HTTPS works |
| T14.8 | Set Hibernate ddl-auto=validate | Production safety | No schema auto-change |
| T14.9 | Smoke test production | Login, create abnormal, upload image | Production works |
| T14.10 | Create backup note | DB backup and upload backup | Documented |

## Phase Exit Criteria

```text
Application runs in production profile.
HTTPS works.
Secrets are externalized.
Upload files persist.
Database schema is managed by SQL script.
Smoke test passes.
```

---

# Future Phase — Reporting and Analytics

Excluded from MVP.

Possible future features:

```text
Weekly report
Monthly report
Report by Process Step
Report by Responsible Department
Dashboard analytics
Excel export
Email notifications
QR code scanning
Mobile application
Workflow approval
AI-assisted analysis
Refresh tokens
MFA
Password reset
Account lockout
Login history
```

---

# 15. Task Definition of Done

Every task is complete when:

```text
It matches the relevant specification.
It compiles without errors.
It follows Coding Standards.
It uses standard ApiResponse/PageResponse.
It uses DTOs, not entities, in controllers.
It enforces business rules in backend.
It enforces security rules in backend.
It returns correct HTTP status codes.
It writes audit log where required.
It handles loading/empty/error states for frontend tasks.
It does not include unrelated changes.
It does not add non-MVP features.
```

---

# 16. AI Agent Execution Rules

When using Qwen Code, Gemini CLI, or another AI coding agent:

## Good prompt pattern

```text
Read these files:
- AGENTS.md
- docs/07_CODING_STANDARDS.md
- docs/03_REST_API.md
- docs/02_BUSINESS_RULES.md

Implement only this task:
Phase 8, Task T8.5: Implement AbnormalNoGenerator.

Requirements:
- Use ABNORMAL_NO_COUNTER table.
- Use row locking.
- Generate format AB-YYYYMMDD-NNNNN.
- Do not implement frontend.
- Do not add unrelated changes.

After finishing, list:
- Files changed
- Business rules enforced
- Remaining work
```

## Bad prompt pattern

```text
Build the whole AMS system.
```

---

# 17. Recommended Development Order

Use this exact order:

```text
Phase 0: Documentation and repository preparation
Phase 1: Project skeleton
Phase 2: Database schema and seed data
Phase 3: Common infrastructure
Phase 4: Security and authentication
Phase 5: Audit core
Phase 6: User management backend
Phase 7: Master data backend
Phase 8: Abnormal management backend
Phase 9: Image management backend
Phase 10: Frontend foundation
Phase 11: Frontend abnormal screens
Phase 12: Frontend admin screens
Phase 13: Testing and hardening
Phase 14: Deployment preparation
```

---
