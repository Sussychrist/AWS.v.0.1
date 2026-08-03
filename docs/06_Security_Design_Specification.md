# 06_Security_Design_Specification

## Abnormal Management System (AMS)

**Version:** 1.1  
**Status:** Draft  
**Author:** Nguyen Hoa Thuan  

---

## 1. Purpose

This document defines the security design for the Abnormal Management System (AMS) MVP.

It covers:

- Authentication
- Authorization
- JWT token management
- Password security
- API security
- File upload security
- Input validation
- Audit logging
- Error handling
- Secrets management
- Database security
- Security testing checklist

This document must be read together with:

```text
00_PRD.md
01_DATABASE_DESIGN.md
02_BUSINESS_RULES.md
03_REST_API.md
04_SCREEN_SPEC.md
05_SYSTEM_ARCHITECTURE.md
```

---

## 2. Change Log

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-07-31 | Initial draft |
| 1.1 | 2026-08-01 | Added JWT details, permission matrix, Spring Security design, file upload limits, CORS/CSRF decisions, secrets management, secure logging rules, and security test checklist |

---

## 3. Security Objectives

The AMS security design must ensure:

```text
1. Only authenticated users can access the system.
2. Only ACTIVE users can log in.
3. Passwords are never stored or returned in plaintext.
4. Administrative functions are restricted to ADMIN users.
5. Uploaded files are validated and stored safely.
6. API errors do not expose sensitive system details.
7. Important actions are recorded in the audit log.
8. Secrets are not committed to source control.
9. The backend is the authoritative security boundary.
```

Important principle:

```text
Frontend role-based visibility is for UX only.
All security rules must be enforced by the backend.
```

---

## 4. Authentication Design

### 4.1 Authentication Method

AMS uses:

```text
Spring Security
BCrypt password hashing
Stateless JWT authentication
```

There is no server-side HTTP session.

---

### 4.2 Login Flow

```text
1. User submits username and password.
2. Backend loads user by username.
3. Backend verifies password using BCrypt.
4. Backend checks user STATUS = ACTIVE.
5. Backend generates JWT access token.
6. Backend returns token and basic user info.
7. Frontend stores token.
8. Frontend sends token in Authorization header for all protected requests.
```

Login endpoint:

```text
POST /api/auth/login
```

Request:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 28800,
    "user": {
      "userId": 1,
      "username": "admin",
      "fullName": "Administrator",
      "role": "ADMIN"
    }
  }
}
```

---

### 4.3 Login Security Rules

| Rule ID | Rule |
|---|---|
| SEC-AUTH-001 | Passwords must be verified using BCrypt. |
| SEC-AUTH-002 | Only users with STATUS = ACTIVE may log in. |
| SEC-AUTH-003 | Invalid login must return a generic error message. |
| SEC-AUTH-004 | System must not reveal whether username exists. |
| SEC-AUTH-005 | Password must never be returned in login response. |
| SEC-AUTH-006 | Password hash must never be returned by any API. |
| SEC-AUTH-007 | Login endpoint must be public. |
| SEC-AUTH-008 | All other endpoints must require authentication unless explicitly public. |

Generic invalid login message:

```text
Invalid username or password
```

Inactive account message:

```text
Account is inactive. Please contact administrator.
```

---

## 5. JWT Token Management

### 5.1 Token Type

```text
Token type: Bearer
Header format: Authorization: Bearer {token}
```

---

### 5.2 Token Claims

JWT payload should include:

```json
{
  "sub": "admin",
  "userId": 1,
  "role": "ADMIN",
  "iat": 1754020800,
  "exp": 1754049600
}
```

| Claim | Description |
|---|---|
| sub | Username |
| userId | User primary key |
| role | ADMIN or USER |
| iat | Issued at time |
| exp | Expiration time |

Do not store sensitive data in JWT claims.

Do not store:

```text
Password
Password hash
Email
IP address
```

---

### 5.3 Token Expiration

```text
Default expiration: 8 hours
Configurable property: ams.jwt.expiration
```

Example:

```yaml
ams:
  jwt:
    expiration: 28800
```

MVP decision:

```text
No refresh token in Version 1.
When token expires, user must log in again.
```

---

### 5.4 Token Validation

Every protected request must pass through:

```text
JwtAuthenticationFilter
```

Validation steps:

```text
1. Read Authorization header.
2. Check header starts with "Bearer ".
3. Parse JWT.
4. Validate signature.
5. Validate expiration.
6. Extract username and role.
7. Set Spring Security authentication context.
```

---

### 5.5 Token Failure Behavior

| Case | HTTP Status | Behavior |
|---|---|---|
| Missing token | 401 | Return Unauthorized |
| Malformed token | 401 | Return Unauthorized |
| Expired token | 401 | Return Unauthorized |
| Invalid signature | 401 | Return Unauthorized |
| Valid token but insufficient role | 403 | Return Forbidden |

Frontend behavior:

```text
On 401 response:
  Clear stored token.
  Redirect to /login.
  Show message: "Session expired. Please log in again."
```

---

### 5.6 Token Storage

MVP decision:

```text
Frontend may store JWT in localStorage.
```

Reason:

```text
Simple for internal MVP.
```

Risk:

```text
localStorage is vulnerable to XSS if the application renders unsafe HTML.
```

Mitigation:

```text
React escapes output by default.
Do not use dangerouslySetInnerHTML.
Validate all input.
Use secure HTTP headers.
```

Future recommendation:

```text
Use httpOnly secure cookie with CSRF protection if security requirements increase.
```

---

### 5.7 Logout

AMS logout is client-side:

```text
1. Frontend removes JWT token.
2. Frontend redirects to /login.
```

Server-side endpoint:

```text
POST /api/auth/logout
```

This endpoint may return `200 OK`, but it does not invalidate the JWT because JWT is stateless.

Future enhancement:

```text
Token blacklist or refresh token rotation.
```

---

## 6. Authorization Design

### 6.1 Roles

```text
ADMIN
USER
```

---

### 6.2 Permission Matrix

| Function | ADMIN | USER |
|---|---|---|
| Login | Yes | Yes |
| View abnormal list | Yes | Yes |
| View abnormal detail | Yes | Yes |
| Create abnormal | Yes | Yes |
| Edit abnormal | Yes | Yes |
| Delete abnormal | Yes | No |
| Upload image | Yes | Yes |
| Delete image | Yes | Yes |
| View images | Yes | Yes |
| Manage users | Yes | No |
| Manage process steps | Yes | No |
| Manage departments | Yes | No |
| View audit log | Yes | No |
| View own profile | Yes | Yes |

---

### 6.3 Endpoint Protection Rules

| Endpoint Pattern | Method | Required Role |
|---|---|---|
| `/api/auth/login` | POST | Public |
| `/api/auth/me` | GET | Authenticated |
| `/api/users/**` | GET, POST, PUT | ADMIN |
| `/api/process-steps` | GET | Authenticated |
| `/api/process-steps` | POST | ADMIN |
| `/api/process-steps/{id}` | PUT | ADMIN |
| `/api/departments` | GET | Authenticated |
| `/api/departments` | POST | ADMIN |
| `/api/departments/{id}` | PUT | ADMIN |
| `/api/abnormals` | GET | Authenticated |
| `/api/abnormals` | POST | Authenticated |
| `/api/abnormals/{id}` | GET | Authenticated |
| `/api/abnormals/{id}` | PUT | Authenticated |
| `/api/abnormals/{id}` | DELETE | ADMIN |
| `/api/abnormals/{id}/images` | POST | Authenticated |
| `/api/images/{id}/file` | GET | Authenticated |
| `/api/images/{id}` | DELETE | Authenticated |
| `/api/audit-logs` | GET | ADMIN |

---

### 6.4 Spring Security Rules

Recommended configuration:

```text
Session management: STATELESS
CSRF: disabled for stateless JWT API
Authentication: JwtAuthenticationFilter
Password encoder: BCryptPasswordEncoder
Authorization: role-based rules in SecurityFilterChain
```

Example rule summary:

```java
http
  .csrf(csrf -> csrf.disable())
  .sessionManagement(session -> session
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/api/auth/login").permitAll()
      .requestMatchers(HttpMethod.GET, "/api/process-steps").authenticated()
      .requestMatchers(HttpMethod.GET, "/api/departments").authenticated()
      .requestMatchers("/api/users/**").hasRole("ADMIN")
      .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
      .requestMatchers(HttpMethod.DELETE, "/api/abnormals/**").hasRole("ADMIN")
      .anyRequest().authenticated()
  );
```

---

### 6.5 Authorization Rules

| Rule ID | Rule |
|---|---|
| SEC-AUTHZ-001 | Backend must enforce role rules on every request. |
| SEC-AUTHZ-002 | Hiding UI elements is not a security control. |
| SEC-AUTHZ-003 | USER role cannot delete abnormalities. |
| SEC-AUTHZ-004 | USER role cannot access user management APIs. |
| SEC-AUTHZ-005 | USER role cannot access master data management APIs. |
| SEC-AUTHZ-006 | USER role cannot access audit log APIs. |
| SEC-AUTHZ-007 | ADMIN role cannot deactivate own account. |
| SEC-AUTHZ-008 | Access denied must return HTTP 403. |

---

## 7. Password Policy

### 7.1 MVP Password Rules

| Rule ID | Rule |
|---|---|
| SEC-PWD-001 | Password must not be empty. |
| SEC-PWD-002 | Minimum password length is 8 characters. |
| SEC-PWD-003 | Password must be stored using BCrypt. |
| SEC-PWD-004 | Plaintext password must never be stored. |
| SEC-PWD-005 | Plaintext password must never be logged. |
| SEC-PWD-006 | Password must never be returned in API response. |
| SEC-PWD-007 | Password hash must never be returned in API response. |

---

### 7.2 Password Creation

When ADMIN creates a user:

```text
Password is required.
Password is hashed with BCrypt before saving.
```

When editing a user:

```text
Password field is optional.
If password is omitted, keep existing password hash.
If password is provided, hash and replace existing password.
```

---

### 7.3 Future Password Enhancements

Excluded from MVP:

```text
Password complexity policy
Password expiration
Password history
Forgot password
Email password reset
Account lockout
Multi-factor authentication
```

---

## 8. API Security

### 8.1 Transport Security

| Environment | Rule |
|---|---|
| Development | HTTP allowed on localhost |
| Production | HTTPS required |

Production rule:

```text
All API traffic must use HTTPS.
HTTP requests should be redirected to HTTPS.
```

---

### 8.2 CORS

MVP CORS rules:

```text
Allowed origin:
  http://localhost:5173 for local frontend development
  Production frontend domain in production

Allowed methods:
  GET, POST, PUT, DELETE, OPTIONS

Allowed headers:
  Authorization
  Content-Type

Allow credentials:
  false for token header MVP
```

Production recommendation:

```text
Use Nginx reverse proxy.
Serve React static files and proxy /api/* to Spring Boot.
This avoids CORS issues in production.
```

---

### 8.3 CSRF

```text
CSRF protection is disabled for the stateless JWT REST API.
```

Reason:

```text
The API uses Authorization: Bearer token.
It does not use cookie-based session authentication.
```

If future implementation switches to httpOnly cookie authentication:

```text
CSRF protection must be re-enabled.
```

---

### 8.4 HTTP Security Headers

Recommended headers:

| Header | Value |
|---|---|
| X-Content-Type-Options | nosniff |
| X-Frame-Options | DENY |
| Cache-Control | no-store for sensitive API responses |
| Strict-Transport-Security | max-age=31536000; includeSubDomains |
| Content-Security-Policy | default-src 'self' |

Spring Security provides many secure headers by default. Customize only when needed.

---

### 8.5 Request Size Limits

```text
spring.servlet.multipart.max-file-size = 5MB
spring.servlet.multipart.max-request-size = 50MB
```

Reason:

```text
Maximum one image is 5 MB.
A request may upload up to 10 images.
```

---

## 9. Input Validation

### 9.1 General Rules

| Rule ID | Rule |
|---|---|
| SEC-VAL-001 | All input must be validated on the backend. |
| SEC-VAL-002 | Frontend validation is for UX only. |
| SEC-VAL-003 | Use Jakarta Validation annotations. |
| SEC-VAL-004 | Reject invalid enum values. |
| SEC-VAL-005 | Reject invalid ID values. |
| SEC-VAL-006 | Enforce maximum field lengths. |
| SEC-VAL-007 | Do not trust client-supplied user ID or role. |
| SEC-VAL-008 | Use parameterized queries / JPA to prevent SQL injection. |

---

### 9.2 Backend Validation Examples

```java
@NotBlank
@Size(max = 200)
private String title;

@NotBlank
private String description;

@NotNull
private Long processStepId;

@NotNull
private Long departmentId;

@Pattern(regexp = "LOW|MEDIUM|HIGH")
private String priority;
```

---

### 9.3 Output Encoding

```text
React escapes rendered values by default.
Do not render user-provided content as HTML.
Do not use dangerouslySetInnerHTML for abnormal title or description.
```

---

## 10. File Upload Security

### 10.1 Upload Rules

| Rule ID | Rule |
|---|---|
| SEC-FILE-001 | Only authenticated users may upload images. |
| SEC-FILE-002 | Upload requires an existing abnormal record. |
| SEC-FILE-003 | Maximum 10 images per abnormal. |
| SEC-FILE-004 | Maximum 5 MB per image. |
| SEC-FILE-005 | Allowed MIME types: image/jpeg and image/png. |
| SEC-FILE-006 | Allowed extensions: .jpg, .jpeg, .png. |
| SEC-FILE-007 | Original file name must not be used as stored file name. |
| SEC-FILE-008 | Stored file name must be UUID-based. |
| SEC-FILE-009 | Files must be stored outside the web root if possible. |
| SEC-FILE-010 | File access must go through authenticated API. |
| SEC-FILE-011 | Physical file deletion is performed by backend service. |

---

### 10.2 File Validation

Backend must validate:

```text
File is not empty
File size <= 5 MB
MIME type is image/jpeg or image/png
File extension is jpg, jpeg, or png
Abnormal exists
Current image count <= 9 before adding one more
```

Recommended validation order:

```text
1. Validate abnormal exists.
2. Validate current image count.
3. Validate file size.
4. Validate MIME type.
5. Validate extension.
6. Generate UUID file name.
7. Save file.
8. Save metadata.
```

---

### 10.3 File Storage Layout

```text
{ams.upload.root-path}/
└── abnormal-images/
    └── 2026/
        └── 08/
            └── 01/
                └── 9f8c2a1e-44d1-4c0a-b1e2-7a9f0c3d1e11.jpg
```

Configuration:

```yaml
ams:
  upload:
    root-path: ./uploads
```

---

### 10.4 Path Traversal Protection

| Rule ID | Rule |
|---|---|
| SEC-FILE-012 | Application must prevent path traversal attacks. |
| SEC-FILE-013 | Do not use user-supplied file names to build storage path. |
| SEC-FILE-014 | Normalize and validate resolved file path. |
| SEC-FILE-015 | Reject paths containing `..`. |

Recommended Java approach:

```text
Resolve file path from configured root only.
Use Path.normalize().
Verify final path starts with root path.
```

---

### 10.5 Image Access

Images are served through:

```text
GET /api/images/{id}/file
```

Rules:

```text
Requires valid JWT.
Backend reads metadata from ABNORMAL_IMAGE.
Backend streams file bytes.
Backend sets correct Content-Type.
Backend must not expose server filesystem path.
```

Do not expose upload directory as public static resources.

---

## 11. Audit Logging Security

### 11.1 Audit Rules

| Rule ID | Rule |
|---|---|
| SEC-AUD-001 | CREATE, UPDATE, DELETE operations must be audited. |
| SEC-AUD-002 | Audit log must record user ID and username. |
| SEC-AUD-003 | Audit log must record action. |
| SEC-AUD-004 | Audit log must record entity name. |
| SEC-AUD-005 | Audit log must record entity ID when available. |
| SEC-AUD-006 | Audit log must record entity number when available. |
| SEC-AUD-007 | Audit log must record timestamp. |
| SEC-AUD-008 | Audit log should record client IP address. |
| SEC-AUD-009 | Audit log records must be append-only. |
| SEC-AUD-010 | Audit log records must not be editable through application. |
| SEC-AUD-011 | Audit log records must not be deletable through application. |
| SEC-AUD-012 | Only ADMIN users may view audit logs. |

---

### 11.2 Audited Entities

```text
USER_INFO
ABNORMAL
ABNORMAL_IMAGE
PROCESS_STEP
DEPARTMENT
```

---

### 11.3 Audited Actions

```text
CREATE
UPDATE
DELETE
```

Optional future actions:

```text
LOGIN
LOGOUT
LOGIN_FAILED
IMAGE_DOWNLOAD
```

---

## 12. Error Handling Security

### 12.1 Error Response Rules

| Rule ID | Rule |
|---|---|
| SEC-ERR-001 | API must return standardized error response. |
| SEC-ERR-002 | API must not expose stack traces to client. |
| SEC-ERR-003 | API must not expose SQL errors to client. |
| SEC-ERR-004 | API must not expose server filesystem paths. |
| SEC-ERR-005 | Validation errors may include field names and messages. |
| SEC-ERR-006 | Unexpected errors should return generic message. |

---

### 12.2 Standard Error Format

Business/validation error:

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

Unexpected error:

```json
{
  "success": false,
  "message": "An unexpected error occurred. Please try again later.",
  "data": null
}
```

---

### 12.3 HTTP Status Usage

| Status | Use |
|---|---|
| 400 | Validation error or business rule violation |
| 401 | Missing, invalid, or expired JWT |
| 403 | Authenticated user does not have required role |
| 404 | Resource not found |
| 413 | Uploaded file too large |
| 415 | Unsupported image format |
| 500 | Unexpected server error |

---

## 13. Secure Logging Rules

Application logs must be useful but safe.

### 13.1 What to Log

```text
Application startup
Configuration profile
Security failures
Authentication failures
Authorization failures
Unexpected exceptions
File upload failures
Audit failures
```

### 13.2 What Must Not Be Logged

```text
Passwords
Password hashes
JWT tokens
Full request bodies containing passwords
Sensitive personal data unless necessary
Stack traces sent to client
```

Rule:

```text
Stack traces may be written to server logs.
Stack traces must not be returned in API responses.
```

---

## 14. Secrets and Configuration Security

### 14.1 Secrets Rules

| Rule ID | Rule |
|---|---|
| SEC-CFG-001 | JWT secret must not be committed to source control. |
| SEC-CFG-002 | Database password must not be committed to source control. |
| SEC-CFG-003 | Production configuration must use environment variables. |
| SEC-CFG-004 | Default passwords must be changed before production. |
| SEC-CFG-005 | Development and production profiles must be separated. |

---

### 14.2 Recommended Environment Variables

```text
AMS_DB_URL
AMS_DB_USERNAME
AMS_DB_PASSWORD
AMS_JWT_SECRET
AMS_UPLOAD_ROOT_PATH
```

Example:

```yaml
spring:
  datasource:
    url: ${AMS_DB_URL}
    username: ${AMS_DB_USERNAME}
    password: ${AMS_DB_PASSWORD}

ams:
  jwt:
    secret: ${AMS_JWT_SECRET}
  upload:
    root-path: ${AMS_UPLOAD_ROOT_PATH:./uploads}
```

---

## 15. Database Security

### 15.1 Database Rules

| Rule ID | Rule |
|---|---|
| SEC-DB-001 | Application must connect using a dedicated database user. |
| SEC-DB-002 | Database user should have only required table privileges. |
| SEC-DB-003 | Production database user should not have DROP or ALTER rights. |
| SEC-DB-004 | Hibernate ddl-auto must be `validate` in production. |
| SEC-DB-005 | Schema changes must be applied through SQL scripts. |
| SEC-DB-006 | Passwords must be stored as BCrypt hashes. |
| SEC-DB-007 | Use database constraints for fixed enum values. |
| SEC-DB-008 | Use unique constraints for username, codes, and abnormal number. |

---

### 15.2 Recommended Privileges

Application database user should have:

```text
SELECT
INSERT
UPDATE
DELETE
```

On AMS tables only.

It should not have:

```text
DROP
ALTER
CREATE
GRANT
```

in production.

---

## 16. Security Test Checklist

Before considering MVP secure enough, test the following.

### 16.1 Authentication Tests

```text
Login with valid credentials returns JWT.
Login with wrong password returns generic error.
Login with non-existing username returns generic error.
Login with inactive user returns inactive account error.
Password is not returned in login response.
Password hash is not returned by any user API.
Protected API without token returns 401.
Protected API with invalid token returns 401.
Protected API with expired token returns 401.
```

---

### 16.2 Authorization Tests

```text
USER cannot access GET /api/users.
USER cannot access POST /api/users.
USER cannot access PUT /api/users/{id}.
USER cannot access GET /api/audit-logs.
USER cannot access POST /api/process-steps.
USER cannot access POST /api/departments.
USER cannot access DELETE /api/abnormals/{id}.
ADMIN can access admin endpoints.
Authenticated user can access allowed abnormal endpoints.
```

---

### 16.3 Abnormal Business Rule Tests

```text
Create abnormal without title fails.
Create abnormal without description fails.
Create abnormal without process step fails.
Create abnormal without department fails.
Create abnormal with inactive process step fails.
Create abnormal with inactive department fails.
Create abnormal with invalid priority fails.
Create abnormal with invalid status fails.
Due date earlier than created date fails.
Abnormal number is generated by server.
Abnormal number cannot be changed on update.
Status transition rules are enforced.
```

---

### 16.4 File Upload Tests

```text
Upload JPEG succeeds.
Upload PNG succeeds.
Upload GIF fails.
Upload PDF fails.
Upload executable file fails.
Upload file larger than 5 MB fails.
Upload more than 10 images per abnormal fails.
Upload to non-existing abnormal fails.
Stored file name is UUID-based.
Original file name is stored in database only.
Delete image removes database row.
Delete image removes physical file.
Delete abnormal removes related image rows.
Delete abnormal removes physical image files.
Image download requires valid JWT.
```

---

### 16.5 Input Security Tests

```text
SQL injection attempts are rejected or safely handled.
XSS input is escaped in UI.
Long title over 200 characters is rejected.
Invalid enum values are rejected.
Invalid numeric IDs are rejected.
Path traversal file names are rejected.
```

---

### 16.6 Audit Tests

```text
Create abnormal writes audit log.
Update abnormal writes audit log.
Delete abnormal writes audit log.
Create user writes audit log.
Update user writes audit log.
Create process step writes audit log.
Create department writes audit log.
Upload image writes audit log.
Delete image writes audit log.
Audit log cannot be edited through API.
Audit log cannot be deleted through API.
```

---

## 17. Future Security Enhancements

Excluded from MVP but recommended for future versions:

```text
Refresh tokens
Token blacklist
MFA
Password reset by email
Password complexity rules
Password expiration
Account lockout after failed login attempts
Login history
Failed login audit
Malware scanning for uploaded files
Image content validation by magic bytes
Rate limiting
API request throttling
httpOnly secure cookie authentication
CSRF protection if cookie auth is used
Centralized secret management
Vulnerability scanning
Dependency scanning
```

---

## 18. Final Acceptance

This security design is ready for implementation when:

```text
Spring Security filter chain is defined.
JWT generation and validation are implemented.
BCrypt password encoding is implemented.
Role-based endpoint protection is implemented.
Permission matrix matches Business Rules Specification.
File upload validation matches image rules.
Uploaded files are stored using UUID names.
Audit logging is implemented for CUD operations.
Standard error responses are implemented.
Secrets are externalized.
Production uses HTTPS.
Security test checklist passes.
```

---

## 19. Summary of Important MVP Security Decisions

```text
Authentication: JWT Bearer token
Session: Stateless
CSRF: Disabled for stateless JWT API
Password storage: BCrypt
Token expiration: 8 hours default
Refresh token: Not included in MVP
Token storage: localStorage acceptable for internal MVP
File upload limit: 10 images, 5 MB each
Allowed image types: JPEG and PNG
Stored file name: UUID-based
Image access: Authenticated API only
Audit log: Append-only, ADMIN-only access
Production transport: HTTPS required
Secrets: Environment variables only
Database schema management: SQL scripts + Hibernate validate
```
