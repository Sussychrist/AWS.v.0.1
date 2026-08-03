
# 02_Business_Rules_Specification

## Abnormal Management System (AMS)

**Version:** 1.1  
**Status:** Draft  
**Author:** Nguyen Hoa Thuan  

---

## 1. Purpose

This document defines the business rules for the Abnormal Management System (AMS) MVP.

These rules are used as the reference for:

- Backend validation
- Frontend validation
- Database constraints
- REST API behavior
- Security behavior
- Testing
- AI-assisted code generation

This version updates the original draft by adding:

- Clear rule IDs
- Traceability to PRD
- Enforcement location
- Role and permission rules
- Exact image limits
- Abnormal number generation rules
- Status transition rules
- Search behavior rules
- Audit log details
- Master data rules
- User management rules

---

## 2. Rule ID Convention

This document uses module-based rule IDs.

| Prefix | Module |
|---|---|
| BR-USR | User Management |
| BR-AUTH | Authentication and Authorization |
| BR-MST | Master Data: Process Step and Department |
| BR-ABN | Abnormal Management |
| BR-ABNNO | Abnormal Number Generation |
| BR-STAT | Abnormal Status Transition |
| BR-IMG | Image Management |
| BR-SRCH | Search |
| BR-AUD | Audit Log |

---

## 3. Traceability to PRD

| PRD Rule | Updated Rule ID | Description |
|---|---|---|
| BR-001 | BR-ABN-001 | Unique Abnormal Number |
| BR-002 | BR-IMG-001 | One abnormal can contain multiple images |
| BR-003 | BR-ABN-004 | Process Step is mandatory |
| BR-004 | BR-ABN-005 | Responsible Department is mandatory |
| BR-005 | BR-ABN-006 | Priority values: LOW/MEDIUM/HIGH |
| BR-006 | BR-ABN-007 | Default Status is OPEN |
| BR-007 | BR-ABN-002 | Title is required |
| BR-008 | BR-ABN-003 | Description is required |
| BR-009 | BR-ABN-009 | Due Date cannot be earlier than Created Date |
| BR-010 | BR-AUD-001 | Create/Update/Delete operations are recorded in Audit Log |

---

## 4. Role and Permission Matrix

### 4.1 Roles

```text
ADMIN
USER
```

### 4.2 Permission Matrix

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
| Search abnormalities | Yes | Yes |
| Manage users | Yes | No |
| Manage process steps | Yes | No |
| Manage departments | Yes | No |
| View audit log | Yes | No |

### 4.3 Notes

- Normal users can create and edit abnormalities.
- Normal users cannot delete abnormalities.
- Administrators can delete abnormalities.
- Normal users cannot access user management, master data management, or audit log screens.

---

# 5. User Management Rules

| Rule ID | Rule | Details | Enforcement | Error Message |
|---|---|---|---|---|
| BR-USR-001 | Username is mandatory | Username cannot be empty. | Frontend + Backend + DB NOT NULL | Username is required |
| BR-USR-002 | Username must be unique | Username must be unique case-insensitively. | Backend + DB unique constraint | Username already exists |
| BR-USR-003 | Password must be hashed | Password must be stored using BCrypt. | Backend | N/A |
| BR-USR-004 | Password must not be returned | API responses must never include password or password hash. | Backend | N/A |
| BR-USR-005 | Full name is mandatory | Full name cannot be empty. | Frontend + Backend + DB NOT NULL | Full name is required |
| BR-USR-006 | Email must be valid | If email is provided, it must be valid format. | Frontend + Backend | Email format is invalid |
| BR-USR-007 | Default role is USER | New users default to USER unless ADMIN explicitly sets role. | Backend + DB default | N/A |
| BR-USR-008 | Default status is ACTIVE | New users default to ACTIVE. | Backend + DB default | N/A |
| BR-USR-009 | Physical user deletion is not allowed | Users must be deactivated by setting STATUS = INACTIVE. | Backend | User deletion is not allowed. Please deactivate the user. |
| BR-USR-010 | Admin cannot deactivate own account | An administrator cannot deactivate their own account. | Backend | You cannot deactivate your own account |

---

# 6. Authentication and Authorization Rules

| Rule ID | Rule | Details | Enforcement | Error Message |
|---|---|---|---|---|
| BR-AUTH-001 | Only ACTIVE users may log in | Users with STATUS = INACTIVE cannot log in. | Backend | Account is inactive. Please contact administrator. |
| BR-AUTH-002 | Invalid login uses generic error | Do not reveal whether username or password is wrong. | Backend | Invalid username or password |
| BR-AUTH-003 | JWT is required for protected APIs | All non-public APIs require valid JWT token. | Backend | Unauthorized |
| BR-AUTH-004 | Admin-only functions must be protected | User management, master data management, and audit log require ADMIN role. | Backend | Forbidden |
| BR-AUTH-005 | Token expiration is configurable | JWT access token expiration should be configurable. Default: 8 hours. | Backend | Session expired. Please log in again. |

---

# 7. Master Data Rules

Master data includes:

```text
PROCESS_STEP
DEPARTMENT
```

| Rule ID | Rule | Details | Enforcement | Error Message |
|---|---|---|---|---|
| BR-MST-001 | Process Step code is mandatory | STEP_CODE cannot be empty. | Frontend + Backend + DB NOT NULL | Process Step code is required |
| BR-MST-002 | Process Step code must be unique | STEP_CODE must be unique. | Backend + DB unique constraint | Process Step code already exists |
| BR-MST-003 | Process Step name is mandatory | STEP_NAME cannot be empty. | Frontend + Backend + DB NOT NULL | Process Step name is required |
| BR-MST-004 | Department code is mandatory | DEPARTMENT_CODE cannot be empty. | Frontend + Backend + DB NOT NULL | Department code is required |
| BR-MST-005 | Department code must be unique | DEPARTMENT_CODE must be unique. | Backend + DB unique constraint | Department code already exists |
| BR-MST-006 | Department name is mandatory | DEPARTMENT_NAME cannot be empty. | Frontend + Backend + DB NOT NULL | Department name is required |
| BR-MST-007 | Master data status values are fixed | Allowed values: ACTIVE, INACTIVE. | Backend + DB check constraint | Invalid status |
| BR-MST-008 | Inactive master data cannot be selected | Inactive Process Step or Department cannot be selected for new or edited abnormalities. | Frontend + Backend | Selected master data is inactive |
| BR-MST-009 | Historical records remain valid | Existing abnormalities remain valid if related Process Step or Department becomes inactive. | Database + Backend | N/A |
| BR-MST-010 | Physical deletion is not allowed | Process Step and Department must not be physically deleted if referenced by abnormalities. | Backend + DB foreign key | Cannot delete because this record is in use |
| BR-MST-011 | Deactivation is the delete mechanism | Delete master data by setting STATUS = INACTIVE. | Backend | N/A |

---

# 8. Abnormal Management Rules

| Rule ID | Rule | Details | Enforcement | Error Message |
|---|---|---|---|---|
| BR-ABN-001 | Abnormal Number must be unique | Every abnormal record must have a unique ABNORMAL_NO. | Backend + DB unique constraint | Abnormal Number already exists |
| BR-ABN-002 | Title is mandatory | TITLE cannot be empty. Maximum length: 200. | Frontend + Backend + DB NOT NULL | Title is required |
| BR-ABN-003 | Description is mandatory | DESCRIPTION cannot be empty. | Frontend + Backend + DB NOT NULL | Description is required |
| BR-ABN-004 | Process Step is mandatory | PROCESS_STEP_ID must be provided. | Frontend + Backend + DB NOT NULL | Process Step is required |
| BR-ABN-005 | Responsible Department is mandatory | DEPARTMENT_ID must be provided. | Frontend + Backend + DB NOT NULL | Responsible Department is required |
| BR-ABN-006 | Priority values are fixed | Allowed values: LOW, MEDIUM, HIGH. Default: MEDIUM. | Frontend + Backend + DB check constraint | Invalid priority |
| BR-ABN-007 | Default Status is OPEN | New abnormalities default to OPEN. | Backend + DB default | N/A |
| BR-ABN-008 | Reporter is automatically assigned | REPORTER_ID is assigned from logged-in user during creation. | Backend | N/A |
| BR-ABN-009 | Due Date cannot be earlier than Created Date | Compare by date only. DUE_DATE must be greater than or equal to TRUNC(CREATE_TIME). | Frontend + Backend | Due Date cannot be earlier than Created Date |
| BR-ABN-010 | Update must track updater | On update, system must set UPDATE_TIME and UPDATE_BY. | Backend | N/A |
| BR-ABN-011 | Delete abnormal is admin-only | Only ADMIN can delete abnormal records. | Backend | Forbidden |
| BR-ABN-012 | Abnormal Number is immutable | ABNORMAL_NO cannot be changed after creation. | Backend + DB | Abnormal Number cannot be changed |
| BR-ABN-013 | Process Step must be active | Selected Process Step must be ACTIVE at create/update time. | Backend | Selected Process Step is inactive |
| BR-ABN-014 | Department must be active | Selected Department must be ACTIVE at create/update time. | Backend | Selected Department is inactive |

---

# 9. Abnormal Number Generation Rules

| Rule ID | Rule | Details | Enforcement |
|---|---|---|---|
| BR-ABNNO-001 | Abnormal Number format | Format: `AB-YYYYMMDD-NNNNN` | Backend |
| BR-ABNNO-002 | Example | Example: `AB-20260801-00001` | Backend |
| BR-ABNNO-003 | Generated on create | Number is generated automatically when abnormal is created. | Backend |
| BR-ABNNO-004 | Daily counter | Counter resets by date using ABNORMAL_NO_COUNTER table. | Backend + DB |
| BR-ABNNO-005 | Not editable | User cannot manually edit Abnormal Number. | Backend + Frontend |
| BR-ABNNO-006 | Not reused | Deleted abnormal numbers must not be reused. | Backend |
| BR-ABNNO-007 | Concurrency safe | Number generation must use row locking to avoid duplicate numbers. | Backend |
| BR-ABNNO-008 | Maximum daily number | Maximum daily suffix is 99999. If exceeded, raise business error. | Backend |

### Generation Logic

```text
1. Get current date.
2. Lock row in ABNORMAL_NO_COUNTER for current date.
3. If row does not exist, insert row with LAST_NUMBER = 0.
4. Increment LAST_NUMBER by 1.
5. Format number:
   AB- + YYYYMMDD + - + LPAD(LAST_NUMBER, 5, '0')
6. Save generated number into ABNORMAL.ABNORMAL_NO.
```

Example:

```text
Date: 2026-08-01
LAST_NUMBER: 0
Next number: 1
ABNORMAL_NO: AB-20260801-00001
```

---

# 10. Abnormal Status Transition Rules

| Rule ID | Rule | Details | Enforcement |
|---|---|---|---|
| BR-STAT-001 | Allowed status values | OPEN, PROCESSING, CLOSED | Backend + DB check constraint |
| BR-STAT-002 | New abnormal starts as OPEN | Default status for new abnormal is OPEN. | Backend + DB default |
| BR-STAT-003 | OPEN can move to PROCESSING or CLOSED | Allowed from OPEN. | Backend |
| BR-STAT-004 | PROCESSING can move to OPEN or CLOSED | Allowed from PROCESSING. | Backend |
| BR-STAT-005 | CLOSED can be reopened | CLOSED can move back to PROCESSING. | Backend |
| BR-STAT-006 | Status change is audited | Status change must be recorded in Audit Log. | Backend |

### Allowed Status Transitions

```text
OPEN → PROCESSING
OPEN → CLOSED

PROCESSING → OPEN
PROCESSING → CLOSED

CLOSED → PROCESSING
```

---

# 11. Image Management Rules

| Rule ID | Rule | Details | Enforcement | Error Message |
|---|---|---|---|---|
| BR-IMG-001 | Multiple images allowed | One abnormal may contain multiple images. | Backend + DB | N/A |
| BR-IMG-002 | Maximum images per abnormal | Maximum 10 images per abnormal. | Frontend + Backend | Maximum 10 images allowed per abnormal |
| BR-IMG-003 | Maximum file size | Maximum 5 MB per image. | Frontend + Backend | Image size must not exceed 5 MB |
| BR-IMG-004 | Allowed formats | Only JPEG and PNG are allowed. | Frontend + Backend + DB check constraint | Only JPEG and PNG images are allowed |
| BR-IMG-005 | Original file name is stored | System must store original uploaded file name. | Backend | N/A |
| BR-IMG-006 | Stored file name is UUID-based | Physical file name must be UUID-based to avoid conflicts. | Backend | N/A |
| BR-IMG-007 | Uploaded by is automatic | UPLOADED_BY is assigned from logged-in user. | Backend | N/A |
| BR-IMG-008 | Uploaded time is automatic | UPLOADED_TIME is assigned by system. | Backend + DB default | N/A |
| BR-IMG-009 | Image requires existing abnormal | Image upload requires valid ABNORMAL_ID. | Backend | Abnormal record not found |
| BR-IMG-010 | Delete image removes metadata | Deleting image deletes ABNORMAL_IMAGE row. | Backend | N/A |
| BR-IMG-011 | Delete image removes physical file | Deleting image deletes physical file from storage. | Backend | N/A |
| BR-IMG-012 | Delete abnormal removes images | Deleting abnormal deletes related image metadata and physical files. | Backend | N/A |
| BR-IMG-013 | Image deletion is audited | Image upload and delete should be recorded in Audit Log. | Backend | N/A |

---

# 12. Search Rules

| Rule ID | Rule | Details | Enforcement |
|---|---|---|---|
| BR-SRCH-001 | Keyword search fields | Keyword searches ABNORMAL_NO and TITLE. | Backend |
| BR-SRCH-002 | Keyword search is case-insensitive | Keyword matching should be case-insensitive. | Backend |
| BR-SRCH-003 | Filter by Process Step | Search supports PROCESS_STEP_ID filter. | Backend |
| BR-SRCH-004 | Filter by Department | Search supports DEPARTMENT_ID filter. | Backend |
| BR-SRCH-005 | Filter by Status | Search supports STATUS filter. | Backend |
| BR-SRCH-006 | Filter by Priority | Search supports PRIORITY filter. | Backend |
| BR-SRCH-007 | Date range filter | Date range filters CREATE_TIME. | Backend |
| BR-SRCH-008 | Default sorting | Default sort is CREATE_TIME descending. | Backend |
| BR-SRCH-009 | Pagination | Search results must be paginated. | Backend + Frontend |
| BR-SRCH-010 | Default page size | Default page size is 10. | Backend + Frontend |
| BR-SRCH-011 | Maximum page size | Maximum page size is 100. | Backend |
| BR-SRCH-012 | Inactive master data in history | Inactive Process Step or Department can still appear in historical records. | Backend |

---

# 13. Audit Log Rules

| Rule ID | Rule | Details | Enforcement |
|---|---|---|---|
| BR-AUD-001 | CUD operations are audited | CREATE, UPDATE, DELETE operations must be recorded. | Backend |
| BR-AUD-002 | Audited entities | USER_INFO, ABNORMAL, ABNORMAL_IMAGE, PROCESS_STEP, DEPARTMENT. | Backend |
| BR-AUD-003 | Audit fields | Audit log must record USER_ID, ACTION, ENTITY_NAME, ENTITY_ID, ENTITY_NO, DESCRIPTION, ACTION_TIME, IP_ADDRESS. | Backend + DB |
| BR-AUD-004 | Audit log is append-only | Audit log records cannot be edited. | Backend + DB |
| BR-AUD-005 | Audit log cannot be deleted | Audit log records cannot be deleted through application. | Backend |
| BR-AUD-006 | Audit after successful operation | Audit log is written after successful business operation. | Backend |
| BR-AUD-007 | Audit failure handling | If audit log cannot be written, system must raise/log error. | Backend |
| BR-AUD-008 | Audit log access is admin-only | Only ADMIN can view audit log. | Backend |

### Recommended Audit Events

```text
CREATE USER_INFO
UPDATE USER_INFO
DELETE USER_INFO

CREATE ABNORMAL
UPDATE ABNORMAL
DELETE ABNORMAL

CREATE ABNORMAL_IMAGE
DELETE ABNORMAL_IMAGE

CREATE PROCESS_STEP
UPDATE PROCESS_STEP
DELETE PROCESS_STEP

CREATE DEPARTMENT
UPDATE DEPARTMENT
DELETE DEPARTMENT
```

---

# 14. Validation Error Messages

| Field / Action | Error Message |
|---|---|
| Username missing | Username is required |
| Username duplicate | Username already exists |
| Password missing | Password is required |
| Full name missing | Full name is required |
| Email invalid | Email format is invalid |
| Title missing | Title is required |
| Description missing | Description is required |
| Process Step missing | Process Step is required |
| Department missing | Responsible Department is required |
| Priority invalid | Invalid priority |
| Status invalid | Invalid status |
| Due Date invalid | Due Date cannot be earlier than Created Date |
| Image type invalid | Only JPEG and PNG images are allowed |
| Image too large | Image size must not exceed 5 MB |
| Too many images | Maximum 10 images allowed per abnormal |
| Inactive Process Step | Selected Process Step is inactive |
| Inactive Department | Selected Department is inactive |
| Inactive user login | Account is inactive. Please contact administrator. |
| Invalid login | Invalid username or password |
| Forbidden | You do not have permission to perform this action |

---

# 15. Enforcement Matrix

| Area | Frontend | Backend | Database |
|---|---|---|---|
| Mandatory fields | Yes | Yes | NOT NULL |
| Unique username | Yes | Yes | Unique constraint |
| Unique abnormal number | No | Yes | Unique constraint |
| Password encryption | No | Yes | No |
| Role-based access | UI hiding | Yes | No |
| Priority values | Dropdown | Enum validation | CHECK constraint |
| Status values | Dropdown | Enum validation | CHECK constraint |
| Default status OPEN | No | Yes | DEFAULT |
| Due Date validation | Yes | Yes | Optional |
| Image format | Yes | Yes | CHECK constraint |
| Image size | Yes | Yes | CHECK constraint |
| Max images | Yes | Yes | No |
| Audit log | No | Yes | Insert AUDIT_LOG |
| Master data active check | Dropdown filter | Yes | No |
| Historical master data | Display only | Yes | FK restriction |

---

# 16. Future Business Rules

The following rules are excluded from MVP but should be considered for future versions:

```text
Reporting by Week
Reporting by Month
Reporting by Process Step
Reporting by Responsible Department
Excel Export
Email Notifications
Dashboard Analytics
QR Code Scanning
Mobile Application
Workflow Approval
AI-assisted Analysis
Field-level Audit Log
Login History
Password Expiration
Account Lockout
```

---

# 17. Final Acceptance

This business rules specification is ready for development when:

```text
All rule IDs are traceable to PRD.
All permissions are approved by business owner.
All validation rules have enforcement location.
All error messages are defined.
Abnormal number generation is confirmed.
Status transitions are confirmed.
Image limits are confirmed.
Audit log scope is confirmed.
Search behavior is confirmed.
Master data deactivation behavior is confirmed.
```

---

# 18. Important Decisions Made in This Version

This version assumes:

```text
Normal USER can create abnormal.
Normal USER can edit abnormal.
Normal USER cannot delete abnormal.
ADMIN can delete abnormal.
Image upload happens after abnormal record exists.
Physical image files are deleted by backend service.
Master data is deactivated, not physically deleted.
Audit log is append-only and cannot be deleted.
Keyword search searches ABNORMAL_NO and TITLE only.
```

If the business wants normal users to delete abnormalities, update:

```text
BR-ABN-011
```

and the permission matrix.
