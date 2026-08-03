
# 04_REST_API_Specification

## Abnormal Management System (AMS)

**Version:** 1.1  
**Status:** Draft  
**Author:** Nguyen Hoa Thuan  

---

## 1. Purpose

Defines the REST API contract between the React frontend and Spring Boot backend.

This document is the reference for:

- Backend controller development
- Frontend API client development
- Integration testing
- AI-assisted code generation

---

## 2. Change Log

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-07-31 | Initial draft |
| 1.1 | 2026-08-01 | Fixed authorization errors; removed user DELETE (BR-USR-009); added PUT for master data; added GET /api/auth/me; added GET /api/images/{id}/file; added request/response examples; added query parameters; added pagination format; added file upload specification; added field-level validation errors |

---

## 3. General Conventions

| Convention | Rule |
|---|---|
| Base URL | `/api` |
| Protocol | HTTP (dev) / HTTPS (prod) |
| Content-Type | `application/json` (except image upload/download) |
| Authentication | JWT Bearer token in `Authorization` header |
| Date format | `yyyy-MM-dd` |
| Timestamp format | `yyyy-MM-dd'T'HH:mm:ss` |
| ID type | Number (Long) |
| Naming | camelCase in JSON |
| Plural nouns | `/api/users`, `/api/abnormals` |

---

## 4. Standard Response Format

### 4.1 Success Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {}
}
```

### 4.2 Paginated Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 57,
    "totalPages": 6
  }
}
```

### 4.3 Business Error Response

```json
{
  "success": false,
  "message": "Due Date cannot be earlier than Created Date",
  "data": null
}
```

### 4.4 Validation Error Response

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

---

## 5. HTTP Status Codes

| Code | Meaning | Usage |
|---|---|---|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST |
| 400 | Bad Request | Validation error, business rule violation |
| 401 | Unauthorized | Missing/invalid/expired JWT |
| 403 | Forbidden | Valid JWT but insufficient role |
| 404 | Not Found | Resource does not exist |
| 413 | Payload Too Large | Image exceeds 5 MB |
| 415 | Unsupported Media Type | Wrong image format |
| 500 | Internal Server Error | Unexpected server error |

---

## 6. Security

```text
JWT Bearer token is required for all endpoints except POST /api/auth/login.
ADMIN role is required for admin endpoints (enforced by backend, not just UI).
Passwords are stored as BCrypt hashes.
Passwords and password hashes are NEVER returned in any API response.
Only ACTIVE users can log in (BR-AUTH-001).
Invalid login returns generic message (BR-AUTH-002).
CORS is configured to allow the React frontend origin.
```

---

## 7. Endpoint Summary

| Module | Method | Endpoint | Description | Authorization |
|---|---|---|---|---|
| Auth | POST | `/api/auth/login` | Authenticate and return JWT | Public |
| Auth | GET | `/api/auth/me` | Get current user info | Authenticated |
| Auth | POST | `/api/auth/logout` | Logout (client-side token removal) | Authenticated |
| User | GET | `/api/users` | List users (paginated) | **ADMIN** |
| User | POST | `/api/users` | Create user | **ADMIN** |
| User | PUT | `/api/users/{id}` | Update user (incl. activate/deactivate) | **ADMIN** |
| Process Step | GET | `/api/process-steps` | List process steps | Authenticated |
| Process Step | POST | `/api/process-steps` | Create process step | **ADMIN** |
| Process Step | PUT | `/api/process-steps/{id}` | Update process step | **ADMIN** |
| Department | GET | `/api/departments` | List departments | Authenticated |
| Department | POST | `/api/departments` | Create department | **ADMIN** |
| Department | PUT | `/api/departments/{id}` | Update department | **ADMIN** |
| Abnormal | GET | `/api/abnormals` | List/search abnormalities (paginated) | Authenticated |
| Abnormal | GET | `/api/abnormals/{id}` | Get abnormal detail | Authenticated |
| Abnormal | POST | `/api/abnormals` | Create abnormal | Authenticated |
| Abnormal | PUT | `/api/abnormals/{id}` | Update abnormal | Authenticated |
| Abnormal | DELETE | `/api/abnormals/{id}` | Delete abnormal | **ADMIN** |
| Image | POST | `/api/abnormals/{id}/images` | Upload images (multipart) | Authenticated |
| Image | GET | `/api/images/{id}/file` | Download/display image file | Authenticated |
| Image | DELETE | `/api/images/{id}` | Delete image | Authenticated |
| Audit | GET | `/api/audit-logs` | View audit logs (paginated) | **ADMIN** |

**Note:** There is no `DELETE /api/users/{id}`. Users are deactivated via `PUT /api/users/{id}` with `status = INACTIVE` (BR-USR-009).

---

## 8. Detailed Endpoint Specifications

---

### 8.1 Authentication

#### POST /api/auth/login

**Authorization:** Public

Request:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Response `200 OK`:

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

Errors:

```text
400 → "Invalid username or password" (generic, BR-AUTH-002)
400 → "Account is inactive. Please contact administrator." (BR-AUTH-001)
```

---

#### GET /api/auth/me

**Authorization:** Authenticated

Response `200 OK`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "userId": 1,
    "username": "admin",
    "fullName": "Administrator",
    "email": "admin@company.com",
    "departmentId": 1,
    "departmentName": "Production",
    "role": "ADMIN",
    "status": "ACTIVE"
  }
}
```

---

#### POST /api/auth/logout

**Authorization:** Authenticated

```text
Stateless JWT: server does not maintain sessions.
Logout is handled client-side by removing the token.
This endpoint may return 200 OK for compatibility but performs no server action.
```

---

### 8.2 User Management

#### GET /api/users

**Authorization:** ADMIN

Query parameters:

| Param | Type | Required | Default | Notes |
|---|---|---|---|---|
| page | int | No | 0 | Zero-based |
| size | int | No | 10 | Max 100 |
| keyword | string | No |  | Searches username, fullName |
| status | string | No |  | ACTIVE / INACTIVE |

Response `200 OK`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "userId": 2,
        "username": "user01",
        "fullName": "Nguyen Van A",
        "email": "user01@company.com",
        "departmentId": 1,
        "departmentName": "Production",
        "role": "USER",
        "status": "ACTIVE",
        "createTime": "2026-08-01T09:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Note:** Password and password hash are never returned (BR-USR-004).

---

#### POST /api/users

**Authorization:** ADMIN

Request:

```json
{
  "username": "user01",
  "password": "12345678",
  "fullName": "Nguyen Van A",
  "email": "user01@company.com",
  "departmentId": 1,
  "role": "USER",
  "status": "ACTIVE"
}
```

Response `201 Created`:

```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "userId": 2,
    "username": "user01",
    "fullName": "Nguyen Van A",
    "role": "USER",
    "status": "ACTIVE"
  }
}
```

Errors:

```text
400 → "Username already exists" (BR-USR-002)
400 → "Email format is invalid" (BR-USR-006)
```

---

#### PUT /api/users/{id}

**Authorization:** ADMIN

Request (password field is optional; omit to keep current password):

```json
{
  "fullName": "Nguyen Van B",
  "email": "user01@company.com",
  "departmentId": 2,
  "role": "USER",
  "status": "INACTIVE"
}
```

Rules:

```text
Username cannot be changed after creation.
Setting status = INACTIVE deactivates the user (BR-USR-009).
Admin cannot deactivate own account (BR-USR-010) → 400 "You cannot deactivate your own account"
```

Response `200 OK`: standard success response.

---

### 8.3 Master Data

#### GET /api/process-steps

**Authorization:** Authenticated

Query parameters:

| Param | Type | Required | Notes |
|---|---|---|---|
| status | string | No | ACTIVE / INACTIVE. Dropdowns use `status=ACTIVE` |

Response `200 OK`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "processStepId": 1,
      "stepCode": "STEP-010",
      "stepName": "Cutting",
      "description": "Cutting process",
      "sortOrder": 1,
      "status": "ACTIVE"
    }
  ]
}
```

#### POST /api/process-steps — Authorization: ADMIN

Request:

```json
{
  "stepCode": "STEP-010",
  "stepName": "Cutting",
  "description": "Cutting process",
  "sortOrder": 1,
  "status": "ACTIVE"
}
```

Errors: `400 → "Process Step code already exists"` (BR-MST-002)

#### PUT /api/process-steps/{id} — Authorization: ADMIN

Request: same as POST, except `stepCode` is read-only (ignored if sent).

Rules: Deactivation uses `status = INACTIVE` (BR-MST-011). No physical delete (BR-MST-010).

---

#### GET /api/departments

Same pattern as process steps.

Response item:

```json
{
  "departmentId": 1,
  "departmentCode": "DEPT-PROD",
  "departmentName": "Production",
  "description": "Production department",
  "sortOrder": 1,
  "status": "ACTIVE"
}
```

#### POST /api/departments — Authorization: ADMIN

Errors: `400 → "Department code already exists"` (BR-MST-005)

#### PUT /api/departments/{id} — Authorization: ADMIN

Same rules as process steps.

---

### 8.4 Abnormal Management

#### GET /api/abnormals

**Authorization:** Authenticated

Query parameters:

| Param | Type | Required | Default | Notes |
|---|---|---|---|---|
| page | int | No | 0 | Zero-based |
| size | int | No | 10 | Max 100 (BR-SRCH-011) |
| keyword | string | No |  | Searches ABNORMAL_NO and TITLE, case-insensitive (BR-SRCH-001/002) |
| processStepId | long | No |  | Filter |
| departmentId | long | No |  | Filter |
| status | string | No |  | OPEN / PROCESSING / CLOSED |
| priority | string | No |  | LOW / MEDIUM / HIGH |
| dateFrom | date | No |  | Filters CREATE_TIME (BR-SRCH-007) |
| dateTo | date | No |  | Filters CREATE_TIME |
| sort | string | No | `createTime,desc` | Default sort (BR-SRCH-008) |

Response `200 OK`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "abnormalId": 1,
        "abnormalNo": "AB-20260801-00001",
        "title": "Machine stopped suddenly",
        "processStepId": 2,
        "processStepName": "Assembly",
        "departmentId": 5,
        "departmentName": "Maintenance",
        "priority": "HIGH",
        "status": "OPEN",
        "dueDate": "2026-08-05",
        "createTime": "2026-08-01T10:00:00",
        "reporterId": 2,
        "reporterName": "Nguyen Van A",
        "imageCount": 3
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 57,
    "totalPages": 6
  }
}
```

---

#### GET /api/abnormals/{id}

**Authorization:** Authenticated

Response `200 OK`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "abnormalId": 1,
    "abnormalNo": "AB-20260801-00001",
    "title": "Machine stopped suddenly",
    "description": "Machine stopped during production run.",
    "processStepId": 2,
    "processStepName": "Assembly",
    "departmentId": 5,
    "departmentName": "Maintenance",
    "priority": "HIGH",
    "status": "OPEN",
    "dueDate": "2026-08-05",
    "reporterId": 2,
    "reporterName": "Nguyen Van A",
    "createTime": "2026-08-01T10:00:00",
    "updateTime": null,
    "updateByName": null,
    "images": [
      {
        "imageId": 10,
        "originalFileName": "machine.jpg",
        "fileSize": 245760,
        "mimeType": "image/jpeg",
        "uploadedTime": "2026-08-01T10:05:00",
        "uploadedByName": "Nguyen Van A",
        "imageUrl": "/api/images/10/file"
      }
    ]
  }
}
```

Errors: `404 → "Abnormal record not found"`

---

#### POST /api/abnormals

**Authorization:** Authenticated

Request:

```json
{
  "title": "Machine stopped suddenly",
  "description": "Machine stopped during production run.",
  "processStepId": 2,
  "departmentId": 5,
  "priority": "HIGH",
  "dueDate": "2026-08-05"
}
```

Server-side behavior:

```text
abnormalNo is generated by server: AB-YYYYMMDD-NNNNN (BR-ABNNO-001). Do NOT accept it in request.
status is forced to OPEN (BR-ABN-007). Do NOT accept it in request.
reporterId is assigned from JWT (BR-ABN-008). Do NOT accept it in request.
createTime is set by server.
Process Step and Department must be ACTIVE (BR-ABN-013/014).
CREATE action is recorded in Audit Log (BR-AUD-001).
```

Response `201 Created`:

```json
{
  "success": true,
  "message": "Abnormal AB-20260801-00001 created successfully",
  "data": {
    "abnormalId": 1,
    "abnormalNo": "AB-20260801-00001",
    "status": "OPEN",
    "createTime": "2026-08-01T10:00:00"
  }
}
```

Validation errors:

```text
400 → "Title is required" (BR-ABN-002)
400 → "Description is required" (BR-ABN-003)
400 → "Process Step is required" (BR-ABN-004)
400 → "Responsible Department is required" (BR-ABN-005)
400 → "Invalid priority" (BR-ABN-006)
400 → "Due Date cannot be earlier than Created Date" (BR-ABN-009)
400 → "Selected Process Step is inactive" (BR-ABN-013)
400 → "Selected Department is inactive" (BR-ABN-014)
```

---

#### PUT /api/abnormals/{id}

**Authorization:** Authenticated

Request:

```json
{
  "title": "Machine stopped suddenly - Line 2",
  "description": "Machine stopped during production run on Line 2.",
  "processStepId": 2,
  "departmentId": 5,
  "priority": "HIGH",
  "status": "PROCESSING",
  "dueDate": "2026-08-06"
}
```

Server-side behavior:

```text
abnormalNo cannot be changed (BR-ABN-012). Ignored if sent.
reporterId cannot be changed. Ignored if sent.
status must follow allowed transitions (BR-STAT-003/004/005).
updateTime and updateBy are set by server (BR-ABN-010).
UPDATE action is recorded in Audit Log.
```

Status transition validation:

```text
400 → "Invalid status transition from CLOSED to OPEN"
      (CLOSED can only move to PROCESSING — BR-STAT-005)
```

Response `200 OK`: standard success response.

---

#### DELETE /api/abnormals/{id}

**Authorization:** ADMIN (BR-ABN-011)

Server-side behavior:

```text
Deletes abnormal record.
Deletes related ABNORMAL_IMAGE rows (cascade).
Deletes physical image files via file service (BR-IMG-012).
DELETE action is recorded in Audit Log.
```

Response `200 OK`:

```json
{
  "success": true,
  "message": "Abnormal deleted successfully",
  "data": null
}
```

Errors:

```text
403 → "You do not have permission to perform this action" (non-ADMIN)
404 → "Abnormal record not found"
```

---

### 8.5 Image Management

#### POST /api/abnormals/{id}/images

**Authorization:** Authenticated  
**Content-Type:** `multipart/form-data`

Form fields:

| Field | Type | Required | Notes |
|---|---|---|---|
| files | file[] | Yes | Multiple files allowed |

Rules:

```text
Abnormal must exist (BR-IMG-009) → 404 if not found.
Max 10 images per abnormal including existing (BR-IMG-002) → 400.
Max 5 MB per file (BR-IMG-003) → 413.
Only image/jpeg and image/png (BR-IMG-004) → 415.
Original file name is stored (BR-IMG-005).
Stored file name is UUID-based (BR-IMG-006).
uploadedBy from JWT, uploadedTime from server (BR-IMG-007/008).
CREATE ABNORMAL_IMAGE recorded in Audit Log (BR-IMG-013).
```

Response `201 Created`:

```json
{
  "success": true,
  "message": "2 images uploaded successfully",
  "data": [
    {
      "imageId": 10,
      "originalFileName": "machine.jpg",
      "fileSize": 245760,
      "mimeType": "image/jpeg",
      "uploadedTime": "2026-08-01T10:05:00",
      "imageUrl": "/api/images/10/file"
    }
  ]
}
```

---

#### GET /api/images/{id}/file

**Authorization:** Authenticated

```text
Returns the binary image file.
Content-Type: image/jpeg or image/png (from MIME_TYPE).
Cache-Control: private, max-age=3600
404 → "Image not found"
```

---

#### DELETE /api/images/{id}

**Authorization:** Authenticated

Server-side behavior:

```text
Deletes ABNORMAL_IMAGE row (BR-IMG-010).
Deletes physical file from storage (BR-IMG-011).
DELETE ABNORMAL_IMAGE recorded in Audit Log.
```

Response `200 OK`:

```json
{
  "success": true,
  "message": "Image deleted successfully",
  "data": null
}
```

---

### 8.6 Audit Log

#### GET /api/audit-logs

**Authorization:** ADMIN (BR-AUD-008)

Query parameters:

| Param | Type | Required | Notes |
|---|---|---|---|
| page | int | No | Default 0 |
| size | int | No | Default 10, max 100 |
| action | string | No | CREATE / UPDATE / DELETE |
| entityName | string | No | ABNORMAL, ABNORMAL_IMAGE, USER_INFO, PROCESS_STEP, DEPARTMENT |
| username | string | No | Keyword search |
| dateFrom | date | No | Filters ACTION_TIME |
| dateTo | date | No | Filters ACTION_TIME |

Response `200 OK`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "auditId": 100,
        "userId": 1,
        "username": "admin",
        "action": "CREATE",
        "entityName": "ABNORMAL",
        "entityId": 1,
        "entityNo": "AB-20260801-00001",
        "description": "Created abnormal AB-20260801-00001",
        "actionTime": "2026-08-01T10:00:00",
        "ipAddress": "192.168.1.10"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

**Note:** Audit log is read-only. No POST/PUT/DELETE endpoints exist for audit logs (BR-AUD-004/005).

---

## 9. File Upload Specification

```text
Transport: multipart/form-data
Field name: files
Max files per request: 10
Max total images per abnormal: 10 (BR-IMG-002)
Max file size: 5 MB (BR-IMG-003)
Allowed MIME types: image/jpeg, image/png (BR-IMG-004)
Allowed extensions: .jpg, .jpeg, .png
Stored file name: {UUID}.{extension} (BR-IMG-006)
Storage path: {ams.upload.root-path}/abnormal-images/{yyyy}/{MM}/{dd}/{storedFileName}
Spring config: spring.servlet.multipart.max-file-size=5MB
               spring.servlet.multipart.max-request-size=50MB
```

---

## 10. Business Rule Traceability

| Rule | Enforced By |
|---|---|
| BR-USR-002 Unique username | POST/PUT /api/users |
| BR-USR-004 Password never returned | All user endpoints |
| BR-USR-009 No physical user deletion | No DELETE /api/users/{id} exists |
| BR-USR-010 Cannot deactivate self | PUT /api/users/{id} |
| BR-AUTH-001 Only ACTIVE login | POST /api/auth/login |
| BR-AUTH-002 Generic login error | POST /api/auth/login |
| BR-MST-010/011 No physical master delete | No DELETE endpoints for master data |
| BR-ABN-001 Unique abnormal number | POST /api/abnormals |
| BR-ABN-002–008 Mandatory fields, defaults | POST /api/abnormals |
| BR-ABN-009 Due date validation | POST/PUT /api/abnormals |
| BR-ABN-011 Admin-only delete | DELETE /api/abnormals/{id} |
| BR-ABN-012 Immutable abnormal number | PUT /api/abnormals/{id} |
| BR-ABN-013/014 Active master data check | POST/PUT /api/abnormals |
| BR-ABNNO-001–008 Number generation | POST /api/abnormals |
| BR-STAT-003–005 Status transitions | PUT /api/abnormals/{id} |
| BR-IMG-002–009 Image rules | POST /api/abnormals/{id}/images |
| BR-IMG-010–012 Image deletion | DELETE /api/images/{id}, DELETE /api/abnormals/{id} |
| BR-SRCH-001–011 Search behavior | GET /api/abnormals |
| BR-AUD-001–008 Audit logging | All CUD endpoints |

---

## 11. Final Acceptance

This API specification is ready for development when:

```text
All endpoints have method, path, and authorization defined.
All list endpoints have query parameters and pagination format.
All write endpoints have request/response examples.
All validation errors have field-level detail.
File upload rules match Business Rules Specification.
Authorization matches the permission matrix.
Audit log is read-only.
No DELETE endpoint exists for users or master data.
```
