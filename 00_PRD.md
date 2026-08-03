
# 00_Product Requirements Document (PRD)

## Abnormal Management System (AMS)

**Version:** 1.1  
**MVP Version:** Yes  
**Document Status:** Draft  
**Author:** Nguyen Hoa Thuan  

---

## 1. Executive Summary

The Abnormal Management System (AMS) is an internal web application designed to digitize the process of recording, managing, and tracking abnormalities that occur during manufacturing operations.

Currently, abnormalities are recorded manually using Excel spreadsheets or paper-based documents. This process makes it difficult to monitor progress, analyze recurring issues, and generate reports for management.

The goal of Version 1 is to provide a centralized platform where users can create, edit, view, search, and track abnormal records while maintaining supporting images and essential production information.

The system is designed with scalability in mind so that reporting and analytics features can be added in future releases.

---

## 2. Change Log

| Version | Date | Author | Description |
|---|---|---|---|
| 1.0 | 2026-07-31 | Nguyen Hoa Thuan | Initial MVP PRD |
| 1.1 | 2026-08-01 | Nguyen Hoa Thuan | Updated permissions, image rules, abnormal number format, master data rules, audit log scope, search behavior, status values, and user flow |

---

## 3. Background

Manufacturing processes generate various abnormalities, including machine failures, quality defects, material issues, and operational mistakes.

Current challenges:

```text
Data is scattered across Excel files.
There is no centralized repository.
Searching historical records is inefficient.
Reports require manual compilation.
There is no standardized recording process.
Trend analysis is difficult.
Traceability is weak.
```

---

## 4. Problem Statement

The current abnormal management process lacks standardization and centralized data management, making tracking, reporting, and decision-making inefficient.

---

## 5. Goals

### 5.1 Business Goals

```text
Digitize abnormal management.
Replace Excel-based tracking.
Improve data consistency.
Reduce manual reporting effort.
Improve traceability.
Support future reporting and analytics.
```

### 5.2 Product Goals

```text
User authentication
User management
Abnormal management
Image management
Search functionality
Audit logging
Role-based access control
```

---

## 6. Non-Goals

Version 1 explicitly excludes:

```text
Dashboard analytics
Reports
Excel export
Email notifications
QR code scanning
Mobile application
Workflow approval
AI-assisted analysis
Real-time MES integration
Advanced trend analysis
```

---

## 7. User Personas and Permissions

### 7.1 Administrator

Administrator responsibilities:

```text
Manage users
Maintain master data
Access all abnormalities
Delete abnormalities
View audit logs
```

### 7.2 User

User responsibilities:

```text
Record abnormalities
Update abnormal records
Upload images
Search historical records
View abnormal details
```

### 7.3 Permission Summary

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
| View profile | Yes | Yes |

---

## 8. Scope

### 8.1 Included in MVP

```text
Login
Logout
User management
Process Step management
Department management
Abnormal creation
Abnormal editing
Abnormal deletion by ADMIN only
Abnormal viewing
Abnormal search
Image upload
Image viewing
Image deletion
Audit log
Role-based access control
```

### 8.2 Excluded from MVP

```text
Reports
Dashboard
Notifications
QR Code
Mobile App
AI features
Excel Export
Workflow Approval
```

---

## 9. User Stories

| ID | Role | User Story |
|---|---|---|
| US-001 | User | As a User, I want to create an abnormal record so production issues can be tracked. |
| US-002 | User | As a User, I want to upload images as evidence after creating an abnormal record. |
| US-003 | User | As a User, I want to search abnormalities so I can find historical records. |
| US-004 | User | As a User, I want to edit abnormal details so records remain accurate. |
| US-005 | Administrator | As an Administrator, I want to manage user accounts. |
| US-006 | Administrator | As an Administrator, I want to manage Process Step master data. |
| US-007 | Administrator | As an Administrator, I want to manage Department master data. |
| US-008 | Administrator | As an Administrator, I want to delete abnormal records when necessary. |
| US-009 | Administrator | As an Administrator, I want to view audit logs for traceability. |
| US-010 | User | As a User, I want to view my profile information. |

---

## 10. Functional Requirements

---

### FR-001 User Authentication

The system shall allow users to log in using username and password.

Requirements:

```text
Authentication uses JWT.
Only ACTIVE users can log in.
Passwords are stored as BCrypt hashes.
Invalid login returns a generic error message.
Logout is performed client-side by removing the JWT token.
```

---

### FR-002 User Management

The system shall allow Administrators to manage users.

Requirements:

```text
ADMIN can create users.
ADMIN can edit users.
ADMIN can activate or deactivate users.
ADMIN cannot deactivate their own account.
Physical user deletion is not allowed.
Passwords are never returned in API responses.
Username must be unique.
Full name is mandatory.
Email must be valid if provided.
Default role is USER.
Default status is ACTIVE.
```

---

### FR-003 Process Step Management

The system shall allow Administrators to manage Process Step master data.

Requirements:

```text
ADMIN can create Process Steps.
ADMIN can edit Process Steps.
ADMIN can activate or deactivate Process Steps.
Process Step code must be unique.
Process Step name is mandatory.
Physical deletion is not allowed if referenced by abnormalities.
Inactive Process Steps cannot be selected for new abnormalities.
Historical abnormalities remain valid.
```

---

### FR-004 Department Management

The system shall allow Administrators to manage Department master data.

Requirements:

```text
ADMIN can create Departments.
ADMIN can edit Departments.
ADMIN can activate or deactivate Departments.
Department code must be unique.
Department name is mandatory.
Physical deletion is not allowed if referenced by abnormalities or users.
Inactive Departments cannot be selected for new abnormalities.
Historical abnormalities remain valid.
```

---

### FR-005 Create Abnormal

The system shall allow ADMIN and USER roles to create abnormal records.

Mandatory fields:

```text
Title
Description
Process Step
Responsible Department
Priority
```

Optional fields:

```text
Due Date
```

System behavior:

```text
Abnormal Number is generated automatically by the system.
Abnormal Number format is AB-YYYYMMDD-NNNNN.
Default Status is OPEN.
Default Priority is MEDIUM if not selected.
Reporter is assigned automatically from the logged-in user.
Created time is assigned automatically by the system.
Selected Process Step must be ACTIVE.
Selected Department must be ACTIVE.
Due Date cannot be earlier than Created Date.
```

---

### FR-006 Edit Abnormal

The system shall allow ADMIN and USER roles to edit abnormal records.

Editable fields:

```text
Title
Description
Process Step
Responsible Department
Priority
Status
Due Date
```

Non-editable fields:

```text
Abnormal Number
Reporter
Created Time
```

System behavior:

```text
Update time is updated automatically.
Updated user is recorded automatically.
Selected Process Step must be ACTIVE.
Selected Department must be ACTIVE.
Status changes must follow allowed status transitions.
Due Date cannot be earlier than Created Date.
```

Allowed status transitions:

```text
OPEN → PROCESSING
OPEN → CLOSED

PROCESSING → OPEN
PROCESSING → CLOSED

CLOSED → PROCESSING
```

---

### FR-007 Delete Abnormal

The system shall allow ADMIN users to delete abnormal records.

Requirements:

```text
Only ADMIN can delete abnormalities.
USER role cannot delete abnormalities.
Deleting an abnormal deletes related image metadata.
Deleting an abnormal deletes related physical image files.
Delete action is recorded in Audit Log.
```

---

### FR-008 View Abnormal

The system shall allow authenticated users to view abnormal details.

Detail view should display:

```text
Abnormal Number
Title
Description
Process Step
Responsible Department
Priority
Status
Due Date
Reporter
Created Time
Updated Time
Updated By
Images
```

---

### FR-009 Search Abnormal

The system shall allow authenticated users to search and filter abnormalities.

Search supports:

```text
Keyword
Process Step
Department
Status
Priority
Created Date range
```

Keyword search:

```text
Keyword searches Abnormal Number and Title.
Keyword search is case-insensitive.
```

Default behavior:

```text
Results are paginated.
Default page size is 10.
Default sorting is Created Time descending.
```

---

### FR-010 Image Management

The system shall allow authenticated users to upload, view, and delete images for abnormal records.

Requirements:

```text
One abnormal may contain multiple images.
Maximum 10 images per abnormal.
Maximum 5 MB per image.
Allowed formats: JPEG and PNG.
Image upload requires an existing abnormal record.
Original file name is stored in database.
Physical file name is UUID-based.
Image deletion removes database metadata.
Image deletion removes physical file.
Image access requires authentication.
```

---

### FR-011 Audit Log

The system shall record Create, Update, and Delete operations in an Audit Log.

Audited entities:

```text
USER_INFO
ABNORMAL
ABNORMAL_IMAGE
PROCESS_STEP
DEPARTMENT
```

Audit log records should include:

```text
User
Action
Entity name
Entity ID
Entity Number, when available
Description
Timestamp
IP address
```

Rules:

```text
Audit logs are append-only.
Audit logs cannot be edited through the application.
Audit logs cannot be deleted through the application.
Only ADMIN can view audit logs.
```

---

### FR-012 Profile

The system shall allow authenticated users to view their own profile.

Profile displays:

```text
Username
Full Name
Email
Department
Role
Status
```

MVP behavior:

```text
Profile is read-only.
Editing own profile is excluded from MVP.
```

---

## 11. Business Rules Summary

This PRD summarizes the main business rules.

The detailed authoritative business rules are defined in:

```text
02_BUSINESS_RULES.md
```

| Rule ID | Rule |
|---|---|
| BR-001 | Every abnormal must have a unique Abnormal Number. |
| BR-002 | Abnormal Number format is `AB-YYYYMMDD-NNNNN`. |
| BR-003 | Abnormal Number is generated automatically and cannot be edited. |
| BR-004 | Title is mandatory. |
| BR-005 | Description is mandatory. |
| BR-006 | Process Step is mandatory. |
| BR-007 | Responsible Department is mandatory. |
| BR-008 | Selected Process Step must be ACTIVE. |
| BR-009 | Selected Department must be ACTIVE. |
| BR-010 | Priority values are LOW, MEDIUM, HIGH. |
| BR-011 | Default Priority is MEDIUM. |
| BR-012 | Default Status is OPEN. |
| BR-013 | Allowed Status values are OPEN, PROCESSING, CLOSED. |
| BR-014 | Due Date cannot be earlier than Created Date. |
| BR-015 | Reporter is assigned automatically from logged-in user. |
| BR-016 | One abnormal may contain multiple images. |
| BR-017 | Maximum 10 images per abnormal. |
| BR-018 | Maximum image size is 5 MB. |
| BR-019 | Allowed image formats are JPEG and PNG. |
| BR-020 | Image upload requires an existing abnormal record. |
| BR-021 | Deleting an abnormal removes related image metadata and physical files. |
| BR-022 | Only ADMIN can delete abnormalities. |
| BR-023 | USER can create and edit abnormalities. |
| BR-024 | Only ACTIVE users may log in. |
| BR-025 | Physical user deletion is not allowed. |
| BR-026 | Master data is deactivated, not physically deleted. |
| BR-027 | Historical abnormalities remain valid after master data deactivation. |
| BR-028 | All Create/Update/Delete operations are recorded in Audit Log. |
| BR-029 | Audit Log cannot be edited or deleted through the application. |
| BR-030 | Only ADMIN can view Audit Log. |

---

## 12. Data Model Overview

Core entities:

```text
USER_INFO
PROCESS_STEP
DEPARTMENT
ABNORMAL
ABNORMAL_IMAGE
AUDIT_LOG
ABNORMAL_NO_COUNTER
```

Relationships:

```text
One USER_INFO can create many ABNORMAL records.
One PROCESS_STEP can be referenced by many ABNORMAL records.
One DEPARTMENT can be referenced by many ABNORMAL records.
One ABNORMAL can have many ABNORMAL_IMAGE records.
One USER_INFO can generate many AUDIT_LOG records.
One ABNORMAL_NO_COUNTER row is used per date.
```

---

## 13. User Flow

Updated MVP flow:

```text
Login
→ Abnormal List
→ Create Abnormal
→ Save
→ Abnormal Detail
→ Upload Images
→ Edit Abnormal if needed
→ Delete Abnormal if ADMIN
```

Reason:

```text
Image upload requires an existing abnormal record.
Therefore, images are uploaded after the abnormal record is saved.
```

---

## 14. Search and Filtering

Search is available on the Abnormal List screen.

Filters:

```text
Keyword
Status
Priority
Process Step
Department
Created Date From
Created Date To
```

Keyword search fields:

```text
Abnormal Number
Title
```

Default list behavior:

```text
Paginated results
Default page size: 10
Default sort: Created Time descending
```

---

## 15. Image Management

Image rules:

```text
Maximum 10 images per abnormal
Maximum 5 MB per image
Allowed formats: JPEG and PNG
Uploaded after abnormal record exists
Stored using UUID-based file name
Original file name stored in database
Physical file deleted when image is deleted
Physical files deleted when abnormal is deleted
Image access requires authentication
```

---

## 16. Audit Log

Audit log records important operations.

Audited actions:

```text
CREATE
UPDATE
DELETE
```

Audited entities:

```text
USER_INFO
ABNORMAL
ABNORMAL_IMAGE
PROCESS_STEP
DEPARTMENT
```

Audit log access:

```text
ADMIN only
```

Audit log behavior:

```text
Append-only
Cannot be edited
Cannot be deleted through application
```

---

## 17. Security and Compliance

MVP security requirements:

```text
JWT authentication
BCrypt password hashing
Role-based authorization
ADMIN-only protection for user management, master data, audit log, and abnormal deletion
Only ACTIVE users can log in
Passwords are never returned in API responses
Uploaded files are validated by size and MIME type
Uploaded files are stored using UUID-based names
API errors do not expose stack traces
Secrets are stored outside source control
HTTPS is required in production
```

---

## 18. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Performance | Normal operations should respond in less than 2 seconds |
| Security | JWT authentication and encrypted passwords |
| Reliability | Oracle Database as system of record |
| Maintainability | Layered architecture |
| Compatibility | Chrome and Edge desktop browsers |
| Availability | Internal business-hour usage is sufficient for MVP |
| Data Integrity | Foreign keys, unique constraints, and audit logging |
| Traceability | All CUD operations recorded in Audit Log |

---

## 19. Assumptions and Constraints

### Assumptions

```text
The system is used internally by factory staff.
Users have desktop access to Chrome or Edge.
Administrator is responsible for user account creation.
Master data is maintained by Administrator.
Image volume is moderate for MVP.
Reporting is not required in Version 1.
```

### Constraints

```text
MVP scope must remain small.
No workflow approval.
No external notification system.
No mobile application.
No dashboard analytics.
No Excel export.
```

---

## 20. Acceptance Criteria

The MVP is accepted when:

```text
Users can log in and log out.
Only ACTIVE users can log in.
ADMIN can manage users.
Users cannot be physically deleted.
ADMIN can manage Process Steps.
ADMIN can manage Departments.
Master data can be activated or deactivated.
USER can create abnormal records.
USER can edit abnormal records.
ADMIN can delete abnormal records.
USER cannot delete abnormal records.
Abnormal Number is generated automatically.
Abnormal Number is unique and not editable.
Title and Description are mandatory.
Process Step and Department are mandatory.
Priority values are LOW, MEDIUM, HIGH.
Default Status is OPEN.
Due Date cannot be earlier than Created Date.
Users can upload JPEG/PNG images.
Maximum image size is 5 MB.
Maximum 10 images per abnormal.
Images can be viewed and deleted.
Deleting abnormal removes related images.
Search supports keyword, status, priority, process step, department, and date range.
Audit log records CREATE, UPDATE, DELETE operations.
Audit log is visible to ADMIN only.
Audit log cannot be edited or deleted.
API errors do not expose sensitive technical details.
Application works on Chrome and Edge.
```

---

## 21. Future Scope

Possible future features:

```text
Dashboard
Weekly Report
Monthly Report
Report by Process Step
Report by Responsible Department
Excel Export
Email Notifications
QR Code Scanning
Mobile Application
Workflow Approval
AI-assisted Analysis
Login History
Password Reset
MFA
Account Lockout
Field-level Audit Log
```

---

## 22. Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 21 |
| Framework | Spring Boot 3 |
| ORM | Spring Data JPA |
| Security | Spring Security + JWT |
| Frontend | React |
| UI Library | Material UI |
| Database | Oracle |
| Build Tool | Maven |
| API Style | RESTful JSON |

---

## 23. Related Documents

```text
01_DATABASE_DESIGN.md
02_BUSINESS_RULES.md
03_REST_API.md
04_SCREEN_SPEC.md
05_SYSTEM_ARCHITECTURE.md
06_SECURITY_DESIGN.md
07_CODING_STANDARDS.md
08_IMPLEMENTATION_PLAN.md
database.sql
```

---

## 24. Glossary

| Term | Meaning |
|---|---|
| Abnormal | An unexpected manufacturing issue or deviation |
| Process Step | A manufacturing step where the abnormal occurred |
| Responsible Department | Department responsible for handling the abnormal |
| Audit Log | System record of important create/update/delete actions |
| ADMIN | Administrator role with full management access |
| USER | Normal user who can create/edit abnormalities |
| JWT | JSON Web Token used for authentication |
| MVP | Minimum Viable Product |