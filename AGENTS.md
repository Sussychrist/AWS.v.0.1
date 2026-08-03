# AI Agent Coding Rules for AMS

This document defines rules for AI coding agents (Qwen Code, Gemini CLI, Claude Code, Cursor, Copilot) working on the Abnormal Management System.

## Core Principles

1. **Read specs first** - Always read relevant documentation before implementing
2. **One task at a time** - Implement only the assigned task, nothing more
3. **Follow conventions** - Adhere to coding standards and architecture docs
4. **No MVP scope creep** - Do not add features outside the PRD scope
5. **Verify before committing** - Ensure code compiles and tests pass

## Required Reading Before Any Task

Always read these files before starting:
- `docs/08_Implementation_Plan_Task_Breakdown.md` - Understand current phase
- `docs/07_Coding_Standards_Development_Guidelines.md` - Follow coding conventions
- Relevant spec documents for the task

## Good Prompt Pattern

```text
Read these files:
- docs/08_Implementation_Plan_Task_Breakdown.md
- docs/05_System_Architecture.md
- docs/04_REST_API_Specification.md
- docs/02_Business_Rules_Specification.md

Implement only this task:
Phase 8, Task T8.5: Implement AbnormalNoGenerator.

Requirements:
- Use ABNORMAL_NO_COUNTER table
- Use row locking (SELECT ... FOR UPDATE)
- Generate format AB-YYYYMMDD-NNNNN
- Atomic operation in @Transactional method
- Daily limit 99999: prevent creation with business error
- Gaps in numbering acceptable
- Do not implement frontend
- Do not add unrelated changes

After finishing, list:
- Files changed
- Business rules enforced
- Remaining work
```

## Bad Prompt Pattern

```text
Build the whole AMS system.
```

## Implementation Order

Follow this exact sequence:

1. Phase 0: Documentation and repository preparation ✓
2. Phase 1: Project skeleton
3. Phase 2: Database schema and seed data
4. Phase 3: Common infrastructure
5. Phase 4: Security and authentication
6. Phase 5: Audit core
7. Phase 6: User management backend
8. Phase 7: Master data backend
9. Phase 8: Abnormal management backend
10. Phase 9: Image management backend
11. Phase 10: Frontend foundation
12. Phase 11: Frontend abnormal screens
13. Phase 12: Frontend admin screens
14. Phase 13: Testing and hardening
15. Phase 14: Deployment preparation

## Definition of Done

Every task must:
- Match the relevant specification
- Compile without errors
- Follow coding standards
- Use standard ApiResponse/PageResponse
- Use DTOs, not entities, in controllers
- Enforce business rules in backend
- Enforce security rules in backend
- Return correct HTTP status codes
- Write audit log where required
- Handle loading/empty/error states (frontend)
- Not include unrelated changes
- Not add non-MVP features

## File Naming Conventions

- Java classes: PascalCase (e.g., `AbnormalService.java`)
- React components: PascalCase (e.g., `AbnormalList.jsx`)
- API modules: camelCase with Api suffix (e.g., `abnormalApi.js`)
- Config files: kebab-case (e.g., `application-dev.yml`)

## Security Rules

1. Never commit secrets or passwords
2. Passwords must be BCrypt hashed (cost factor 10)
3. JWT secret must come from environment variables
4. Never expose stack traces in production
5. Always validate user authorization server-side
6. JWT expiration: 8 hours for all roles

## Database Rules

1. Use Oracle sequences for ID generation
2. allocationSize = 1 for sequence generators
3. Never use GenerationType.IDENTITY
4. Use LocalDateTime for TIMESTAMP(6)
5. Use Long for NUMBER(19)
6. Seed order: DEPARTMENT → PROCESS_STEP → USER_INFO

## Error Handling

1. Use GlobalExceptionHandler for consistent responses
2. Return proper HTTP status codes (400, 401, 403, 404, 500)
3. Wrap all responses in ApiResponse structure
4. Log errors but don't expose details to clients

## Key Business Rules

### Status Transitions (exactly these, no others)
- OPEN → PROCESSING ✓
- OPEN → CLOSED ✓
- PROCESSING → OPEN ✓
- PROCESSING → CLOSED ✓
- CLOSED → PROCESSING ✓
- CLOSED → OPEN ✗ (explicitly forbidden)

### Abnormal Number Generation
- Format: AB-YYYYMMDD-NNNNN
- Daily counter with row locking
- Max 99999 per day, then reject with business error
- Gaps acceptable, uniqueness enforced

### Image Upload
- Max 10 images per abnormal
- Max 5MB per image
- JPEG/PNG only
- Reject entire batch if exceeds remaining slots (no partial uploads)

### Audit Logging
- Mandatory for CREATE/UPDATE/DELETE on: ABNORMAL, USER_INFO, PROCESS_STEP, DEPARTMENT, ABNORMAL_IMAGE
- ENTITY_NO populated with business identifier
- Audit failure rolls back entire transaction

### Password Rules
- Minimum length: 8 characters
- Optional in PUT /api/users/{id} (omit/null = keep existing)
- BCrypt hash with cost factor 10

## Commit Messages

Use conventional commits:
```
feat: implement abnormal number generator
fix: handle null pointer in user service
docs: update REST API specification
refactor: extract file storage logic
test: add integration tests for login flow
```
