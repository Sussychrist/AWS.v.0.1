# 03_Screen_Specification

## Abnormal Management System (AMS)

**Version:** 1.1  
**Status:** Draft  
**Author:** Nguyen Hoa Thuan  

---

## 1. Purpose

This document defines all screens of the AMS MVP: layout, fields, validation, actions, states, role access, and API mapping.

It is the reference for frontend development and AI-assisted code generation.

---

## 2. Change Log

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-07-31 | Initial draft |
| 1.1 | 2026-08-01 | Added Audit Log screen; added role access and API mapping; aligned due date rule with BR-ABN-009; moved image upload out of Create screen (BR-IMG-009); Delete abnormal is ADMIN-only (BR-ABN-011); replaced user/master data deletion with Activate/Deactivate (BR-USR-009, BR-MST-011); added states, pagination, chip colors, field lengths |

---

## 3. Screen Inventory and Access

| # | Screen | Route | ADMIN | USER |
|---|---|---|---|---|
| 1 | Login | `/login` | Public | Public |
| 2 | Abnormal List | `/abnormals` | Yes | Yes |
| 3 | Create Abnormal | `/abnormals/new` | Yes | Yes |
| 4 | Abnormal Detail | `/abnormals/{id}` | Yes | Yes |
| 5 | Edit Abnormal | `/abnormals/{id}/edit` | Yes | Yes |
| 6 | User Management | `/users` | Yes | No |
| 7 | Process Step Management | `/process-steps` | Yes | No |
| 8 | Department Management | `/departments` | Yes | No |
| 9 | Audit Log | `/audit-logs` | Yes | No |
| 10 | Profile | `/profile` | Yes | Yes |

Unauthorized access to ADMIN screens shows:

```text
"You do not have permission to access this page."
```

---

## 4. Global Layout and Navigation

```text
+--------------------------------------------------------------+
| LOGO  Abnormal Management System        [user name] [Logout] |
+------------+-------------------------------------------------+
| MENU       |                                                 |
|            |                                                 |
| Abnormal   |              Page Content                       |
| Users *    |                                                 |
| Process *  |                                                 |
| Dept *     |                                                 |
| Audit *    |                                                 |
| Profile    |                                                 |
|            |                                                 |
+------------+-------------------------------------------------+
  * = ADMIN only (hidden for USER role)
```

Navigation rules:

```text
After login → redirect to /abnormals
After logout → redirect to /login
Expired/invalid JWT → redirect to /login with message "Session expired. Please log in again."
Menu items are hidden based on role (backend still enforces access).
```

---

## 5. Common UI Standards

| Standard | Rule |
|---|---|
| Required fields | Marked with `*` |
| Validation messages | Displayed below the field in red |
| Delete/Deactivate | Confirmation dialog before action |
| Notifications | Success toast (green), Error toast (red) |
| Loading state | Spinner or skeleton while API is running |
| Empty state | Friendly message + suggested action |
| Error state | "Something went wrong. Please try again." with Retry button |
| Pagination | Default page size 10; options 10/20/50; max 100 |
| Date format | `yyyy-MM-dd` |
| Timestamp format | `yyyy-MM-dd HH:mm` |
| Priority chip colors | HIGH = red, MEDIUM = amber, LOW = green |
| Status chip colors | OPEN = blue, PROCESSING = amber, CLOSED = gray |
| Browsers | Chrome and Edge (desktop) |

---

## 6. Screen Specifications

---

### 6.1 Login

**Purpose:** Authenticate users.  
**Route:** `/login`  
**Access:** Public  
**API:** `POST /api/auth/login`

```text
+--------------------------------+-----------------------------+
|                                |        Sign In              |
|   Abnormal Management          |                             |
|   System                       |  Username *  [__________]   |
|                                |  Password *  [__________]   |
|   Record. Track. Resolve.      |                             |
|                                |  [        Login         ]   |
|                                |                             |
+--------------------------------+-----------------------------+
```

| Field | Type | Required | Max | Notes |
|---|---|---|---|---|
| Username | Text | Yes | 50 |  |
| Password | Password | Yes |  |  |

Behavior:

```text
Success → store JWT → redirect to /abnormals
Invalid credentials → "Invalid username or password" (generic, BR-AUTH-002)
Inactive account → "Account is inactive. Please contact administrator." (BR-AUTH-001)
Empty fields → "Username is required" / "Password is required"
```

---

### 6.2 Abnormal List

**Purpose:** View, filter, and search abnormalities.  
**Route:** `/abnormals`  
**Access:** ADMIN, USER  
**API:** `GET /api/abnormals`

```text
Abnormalities                                              [+ Create]
+---------------------------------------------------------------------+
| Keyword [________________]  Status [____]  Priority [____]          |
| Process Step [____]  Department [____]  Created [from] ~ [to]       |
| [Search] [Reset]                                                    |
+---------------------------------------------------------------------+
| Abnormal No | Title | Step | Dept | Priority | Status | Due | ...   |
|-------------|-------|------|------|----------|--------|-----|-------|
| AB-2026...  | ...   | ...  | ...  | [HIGH]   | [OPEN] | ... | 👁 ✎ 🗑|
+---------------------------------------------------------------------+
Rows per page: [10 v]        1–10 of 57            [<] 1 2 3 ... [>]
```

Filter fields:

| Field | Type | Source | Notes |
|---|---|---|---|
| Keyword | Text | Free input | Searches ABNORMAL_NO and TITLE, case-insensitive (BR-SRCH-001/002) |
| Status | Dropdown | OPEN, PROCESSING, CLOSED | Optional |
| Priority | Dropdown | LOW, MEDIUM, HIGH | Optional |
| Process Step | Dropdown | `GET /api/process-steps` | Optional |
| Department | Dropdown | `GET /api/departments` | Optional |
| Created From / To | Date | Date picker | Filters CREATE_TIME (BR-SRCH-007) |

Table columns:

```text
Abnormal No | Title | Process Step | Department | Priority | Status |
Due Date | Created Time | Reporter | Actions
```

Behavior:

```text
Default sort: CREATE_TIME descending (BR-SRCH-008)
Row click → open Abnormal Detail
View action → Abnormal Detail
Edit action → Edit Abnormal
Delete action → ADMIN only (BR-ABN-011); confirmation dialog:
  "Delete abnormal {ABNORMAL_NO}? Related images will also be deleted."
Empty state → "No abnormalities found." + [Create Abnormal] button
```

---

### 6.3 Create Abnormal

**Purpose:** Create a new abnormal record.  
**Route:** `/abnormals/new`  
**Access:** ADMIN, USER  
**API:** `POST /api/abnormals`, `GET /api/process-steps`, `GET /api/departments`

```text
Create Abnormal
+----------------------------------------------------------+
| Title *               [_______________________________]  |
| Description *         [                               ]  |
|                     [                               ]  |
| Process Step *        [Select....................v]      |
| Responsible Dept *    [Select....................v]      |
| Priority *            [MEDIUM...................v]       |
| Due Date              [yyyy-MM-dd]                       |
|                                                          |
| ⓘ Images can be uploaded after the record is saved.      |
|                                                          |
|                              [Cancel]   [Save]           |
+----------------------------------------------------------+
```

| Field | Type | Required | Max | Default | Validation |
|---|---|---|---|---|---|
| Title | Text | Yes | 200 |  | "Title is required" |
| Description | Textarea | Yes | CLOB |  | "Description is required" |
| Process Step | Dropdown | Yes |  |  | ACTIVE only (BR-ABN-013); "Process Step is required" |
| Responsible Department | Dropdown | Yes |  |  | ACTIVE only (BR-ABN-014); "Responsible Department is required" |
| Priority | Dropdown | Yes |  | MEDIUM | LOW/MEDIUM/HIGH (BR-ABN-006) |
| Due Date | Date picker | No |  |  | Must be >= today (BR-ABN-009); "Due Date cannot be earlier than Created Date" |

Important notes:

```text
Abnormal No is NOT shown here — it is generated by backend on save (BR-ABNNO-003).
Reporter is assigned automatically from logged-in user (BR-ABN-008).
Status is always OPEN on create (BR-ABN-007).
No image upload on this screen (BR-IMG-009). Show helper text:
  "Images can be uploaded after the record is saved."
```

Behavior:

```text
Save success → toast "Abnormal {ABNORMAL_NO} created successfully"
             → redirect to Abnormal Detail (so user can upload images)
Save failure → show field errors / error toast
Cancel → back to Abnormal List (no confirmation, discard form)
```

---

### 6.4 Abnormal Detail

**Purpose:** View abnormal information and manage images.  
**Route:** `/abnormals/{id}`  
**Access:** ADMIN, USER  
**API:** `GET /api/abnormals/{id}`, `POST /api/abnormals/{id}/images`, `DELETE /api/images/{id}`

```text
[< Back]   AB-20260801-00001   [OPEN]                 [Edit] [Delete*]
+--------------------------------------------------------------------+
| Title          Machine stopped suddenly                            |
| Description    Machine stopped during production run...            |
| Process Step   Assembly        Responsible Dept  Maintenance       |
| Priority       [HIGH]          Due Date          2026-08-05        |
| Reporter       Nguyen Van A    Created           2026-08-01 10:00  |
| Updated By     —               Updated           —                 |
+--------------------------------------------------------------------+
| Images (3/10)                                        [Upload Image]|
| [img x] [img x] [img x]                                            |
+--------------------------------------------------------------------+
  * Delete button visible for ADMIN only
```

Display rules:

```text
Abnormal No: read-only (BR-ABN-012)
Status and Priority shown as colored chips
Timestamps: yyyy-MM-dd HH:mm
```

Image section rules:

```text
Show thumbnails with delete (x) button
Click thumbnail → open full-size preview (lightbox)
[Upload Image] → file picker, multiple files allowed
Counter shows current/max: "Images (3/10)" (BR-IMG-002)
Validation:
  Over 10 images → "Maximum 10 images allowed per abnormal"
  Over 5 MB → "Image size must not exceed 5 MB"
  Wrong type → "Only JPEG and PNG images are allowed"
Delete image → confirmation dialog → remove metadata + physical file
```

Actions:

```text
Edit → Edit Abnormal screen
Delete → ADMIN only (BR-ABN-011), confirmation dialog,
         then redirect to Abnormal List with success toast
```

---

### 6.5 Edit Abnormal

**Purpose:** Update abnormal details, status, and images.  
**Route:** `/abnormals/{id}/edit`  
**Access:** ADMIN, USER  
**API:** `PUT /api/abnormals/{id}`, image APIs same as Detail

```text
Edit Abnormal   AB-20260801-00001 (read-only)
+----------------------------------------------------------+
| Title *            [_______________________________]     |
| Description *      [_______________________________]     |
| Process Step *     [Assembly.....................v]      |
| Responsible Dept * [Maintenance..................v]      |
| Priority *         [HIGH.........................v]      |
| Status             [PROCESSING...................v]      |
| Due Date           [2026-08-05]                          |
| Images (3/10)      [img x][img x][img x] [Upload Image]  |
|                                                          |
|                              [Cancel]   [Save]           |
+----------------------------------------------------------+
```

| Field | Type | Required | Notes |
|---|---|---|---|
| Abnormal No | Read-only |  | Cannot be changed (BR-ABN-012) |
| Title | Text | Yes | Same as Create |
| Description | Textarea | Yes | Same as Create |
| Process Step | Dropdown | Yes | ACTIVE options only |
| Responsible Department | Dropdown | Yes | ACTIVE options only |
| Priority | Dropdown | Yes | LOW/MEDIUM/HIGH |
| Status | Dropdown | Yes | Only allowed transitions (BR-STAT) |
| Due Date | Date picker | No | Must be >= Created Date (BR-ABN-009) |

Status dropdown options by current status:

```text
Current OPEN       → options: OPEN, PROCESSING, CLOSED
Current PROCESSING → options: PROCESSING, OPEN, CLOSED
Current CLOSED     → options: CLOSED, PROCESSING
```

Behavior:

```text
Save success → toast "Abnormal {ABNORMAL_NO} updated successfully"
             → back to Abnormal Detail
Backend sets UPDATE_TIME and UPDATE_BY automatically (BR-ABN-010)
Cancel → back to Abnormal Detail
```

---

### 6.6 User Management

**Purpose:** Manage system users.  
**Route:** `/users`  
**Access:** ADMIN only (BR-AUTH-004)  
**API:** `GET /api/users`, `POST /api/users`, `PUT /api/users/{id}`

```text
User Management                                        [+ Create User]
+--------------------------------------------------------------------+
| Keyword [____________]   Status [____]        [Search] [Reset]     |
+--------------------------------------------------------------------+
| Username | Full Name | Email | Department | Role | Status | Actions|
| admin    | Admin...  | ...   | Production | ADMIN| [ACTIVE]| ✎ ⏻  |
+--------------------------------------------------------------------+
```

Create / Edit form fields:

| Field | Type | Required | Max | Default | Notes |
|---|---|---|---|---|---|
| Username | Text | Yes (create only) | 50 |  | Unique, case-insensitive (BR-USR-002); read-only in edit |
| Password | Password | Yes (create only) |  |  | Not shown in edit (BR-USR-004) |
| Full Name | Text | Yes | 100 |  |  |
| Email | Text | No | 100 |  | Valid format (BR-USR-006) |
| Department | Dropdown | No |  |  | ACTIVE departments |
| Role | Dropdown | Yes |  | USER | ADMIN / USER (BR-USR-007) |
| Status | Dropdown | Yes |  | ACTIVE | ACTIVE / INACTIVE (BR-USR-008) |

Behavior:

```text
No physical delete (BR-USR-009).
Action column shows Activate / Deactivate toggle (⏻) with confirmation:
  "Deactivate user {username}? They will no longer be able to log in."
Admin cannot deactivate own account (BR-USR-010):
  button disabled with tooltip "You cannot deactivate your own account."
Create success → toast "User created successfully"
Duplicate username → "Username already exists"
```

---

### 6.7 Process Step Management

**Purpose:** Maintain Process Step master data.  
**Route:** `/process-steps`  
**Access:** ADMIN only  
**API:** `GET /api/process-steps`, `POST /api/process-steps`, `PUT /api/process-steps/{id}`

| Field | Type | Required | Max | Notes |
|---|---|---|---|---|
| Step Code | Text | Yes | 30 | Unique (BR-MST-002); read-only in edit |
| Step Name | Text | Yes | 100 | (BR-MST-003) |
| Description | Text | No | 255 |  |
| Sort Order | Number | No |  | For dropdown ordering |
| Status | Dropdown | Yes |  | ACTIVE / INACTIVE |

Behavior:

```text
Table columns: Step Code | Step Name | Description | Sort Order | Status | Actions
Actions: Edit, Activate/Deactivate
No physical delete (BR-MST-010/011).
Deactivate confirmation:
  "Deactivate process step {code}? It can no longer be selected for new abnormalities. Existing records are not affected."
Duplicate code → "Process Step code already exists"
```

---

### 6.8 Department Management

**Purpose:** Maintain Department master data.  
**Route:** `/departments`  
**Access:** ADMIN only  
**API:** `GET /api/departments`, `POST /api/departments`, `PUT /api/departments/{id}`

| Field | Type | Required | Max | Notes |
|---|---|---|---|---|
| Department Code | Text | Yes | 30 | Unique (BR-MST-005); read-only in edit |
| Department Name | Text | Yes | 100 | (BR-MST-006) |
| Description | Text | No | 255 |  |
| Sort Order | Number | No |  | For dropdown ordering |
| Status | Dropdown | Yes |  | ACTIVE / INACTIVE |

Behavior: same pattern as Process Step Management.

---

### 6.9 Audit Log (NEW)

**Purpose:** View system audit history.  
**Route:** `/audit-logs`  
**Access:** ADMIN only (BR-AUD-008)  
**API:** `GET /api/audit-logs`

```text
Audit Log
+--------------------------------------------------------------------+
| Action [____]  Entity [____]  Username [____]  Time [from]~[to]    |
| [Search] [Reset]                                                   |
+--------------------------------------------------------------------+
| Action Time       | User  | Action   | Entity   | Entity No | Desc |
| 2026-08-01 10:00  | admin | [CREATE] | ABNORMAL | AB-2026.. | ...  |
+--------------------------------------------------------------------+
```

Filters:

| Field | Type | Options |
|---|---|---|
| Action | Dropdown | CREATE, UPDATE, DELETE |
| Entity | Dropdown | ABNORMAL, ABNORMAL_IMAGE, USER_INFO, PROCESS_STEP, DEPARTMENT |
| Username | Text | Free input |
| Action Time From / To | Date | Date range |

Behavior:

```text
Read-only screen: no Edit, no Delete buttons (BR-AUD-004/005)
Action chips: CREATE = green, UPDATE = blue, DELETE = red
Default sort: ACTION_TIME descending
Paginated (default 10)
```

---

### 6.10 Profile

**Purpose:** View logged-in user information.  
**Route:** `/profile`  
**Access:** ADMIN, USER  
**API:** `GET /api/auth/me`

Display (read-only):

```text
Username     admin
Full Name    Administrator
Email        admin@company.com
Department   Production
Role         ADMIN
Status       ACTIVE
```

```text
Editing own profile is not included in MVP (future scope).
```

---

## 7. Field Length Reference (aligned with Database Design)

| Field | Max Length |
|---|---|
| USERNAME | 50 |
| FULL_NAME | 100 |
| EMAIL | 100 |
| STEP_CODE / DEPARTMENT_CODE | 30 |
| STEP_NAME / DEPARTMENT_NAME | 100 |
| Master DESCRIPTION | 255 |
| ABNORMAL_NO | 30 |
| TITLE | 200 |
| ABNORMAL DESCRIPTION | CLOB |

---
