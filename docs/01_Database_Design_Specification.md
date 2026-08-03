# 01_Database_Design_Specification

## Abnormal Management System (AMS)

**Version:** 1.1  
**Status:** Draft  
**Database:** Oracle  
**Document Owner:** Nguyen Hoa Thuan  

---

## 1. Purpose

This document defines the logical and physical database design for the Abnormal Management System (AMS) MVP.

It is the foundation for:

- Backend entity development
- REST API design
- Frontend data binding
- Audit logging
- Future reporting and analytics

---

## 2. Change Log

| Version | Date | Author | Description |
|---|---|---|---|
| 1.0 | 2026-07-31 | Nguyen Hoa Thuan | Initial draft |
| 1.1 | 2026-08-01 | Nguyen Hoa Thuan | Added constraints, defaults, audit columns, image metadata, delete behavior, abnormal number generation, file storage strategy, and Oracle DDL |

---

## 3. Database Standards

### 3.1 Database Platform

- Database: Oracle Database
- Character Set: AL32UTF8
- ORM: Spring Data JPA / Hibernate
- ID Generation: Oracle sequences
- Java ID Type: `Long`
- Timestamp Type: `LocalDateTime`

### 3.2 Naming Conventions

| Object | Convention | Example |
|---|---|---|
| Tables | UPPER_CASE | `ABNORMAL` |
| Columns | UPPER_CASE_WITH_UNDERSCORE | `CREATE_TIME` |
| Primary Keys | Concise singular identifier | `USER_ID`, `IMAGE_ID` |
| Foreign Keys | REFERENCED_TABLE_ID | `DEPARTMENT_ID` |
| Unique Constraints | UQ_TABLE_COLUMN | `UQ_ABNORMAL_NO` |
| Check Constraints | CHK_TABLE_COLUMN | `CHK_ABNORMAL_STATUS` |
| Foreign Key Constraints | FK_TABLE_REF | `FK_ABNORMAL_DEPARTMENT` |
| Indexes | IDX_TABLE_COLUMN | `IDX_ABNORMAL_STATUS` |
| Sequences | SEQ_TABLE | `SEQ_ABNORMAL` |

### 3.3 General Rules

- All IDs use `NUMBER(19)`.
- All timestamps use `TIMESTAMP(6)`.
- Default timestamp value is `SYSTIMESTAMP`.
- Mandatory fields must be defined as `NOT NULL`.
- Enum-like values are stored as `VARCHAR2` and controlled by check constraints.
- Master data uses `STATUS = ACTIVE / INACTIVE` instead of `IS_ACTIVE = Y/N`.
- Physical deletion of master data is not allowed if referenced by transactional data.
- Audit logs are never deleted.

---

## 4. Database Overview

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

`ABNORMAL_NO_COUNTER` is a system table used to generate daily abnormal numbers.

---

## 5. Entity Relationship Diagram

```text
USER_INFO (1:N) ABNORMAL
USER_INFO (1:N) AUDIT_LOG
USER_INFO (1:N) ABNORMAL_IMAGE
USER_INFO (N:1) DEPARTMENT

PROCESS_STEP (1:N) ABNORMAL
DEPARTMENT (1:N) ABNORMAL

ABNORMAL (1:N) ABNORMAL_IMAGE
```

---

## 6. Enumeration Definitions

### 6.1 User Role

```text
ADMIN
USER
```

### 6.2 User Status

```text
ACTIVE
INACTIVE
```

### 6.3 Master Data Status

Used by:

```text
PROCESS_STEP
DEPARTMENT
```

Values:

```text
ACTIVE
INACTIVE
```

### 6.4 Abnormal Priority

```text
LOW
MEDIUM
HIGH
```

### 6.5 Abnormal Status

```text
OPEN
PROCESSING
CLOSED
```

### 6.6 Audit Action

```text
CREATE
UPDATE
DELETE
```

### 6.7 Image MIME Type

```text
image/jpeg
image/png
```

---

## 7. Entity Specifications

---

## 7.1 USER_INFO

Stores application users.

| Column | Type | Nullable | Default | Description |
|---|---|---:|---:|---|
| USER_ID | NUMBER(19) | No | Sequence | Primary key |
| USERNAME | VARCHAR2(50) | No |  | Unique login name |
| PASSWORD_HASH | VARCHAR2(255) | No |  | BCrypt password hash |
| FULL_NAME | VARCHAR2(100) | No |  | User full name |
| EMAIL | VARCHAR2(100) | Yes |  | Unique email |
| DEPARTMENT_ID | NUMBER(19) | Yes |  | FK to DEPARTMENT |
| ROLE | VARCHAR2(20) | No | `'USER'` | ADMIN or USER |
| STATUS | VARCHAR2(20) | No | `'ACTIVE'` | ACTIVE or INACTIVE |
| CREATE_TIME | TIMESTAMP(6) | No | `SYSTIMESTAMP` | Created timestamp |
| UPDATE_TIME | TIMESTAMP(6) | Yes |  | Updated timestamp |

### Constraints

```text
PK_USER_INFO
UQ_USER_INFO_USERNAME
UQ_USER_INFO_EMAIL
CHK_USER_INFO_ROLE
CHK_USER_INFO_STATUS
FK_USER_INFO_DEPARTMENT
```

---

## 7.2 PROCESS_STEP

Stores manufacturing process steps.

| Column | Type | Nullable | Default | Description |
|---|---|---:|---:|---|
| PROCESS_STEP_ID | NUMBER(19) | No | Sequence | Primary key |
| STEP_CODE | VARCHAR2(30) | No |  | Unique step code |
| STEP_NAME | VARCHAR2(100) | No |  | Step name |
| DESCRIPTION | VARCHAR2(500) | Yes |  | Description |
| STATUS | VARCHAR2(20) | No | `'ACTIVE'` | ACTIVE or INACTIVE |
| SORT_ORDER | NUMBER(10) | No | `0` | Display order |
| CREATE_TIME | TIMESTAMP(6) | No | `SYSTIMESTAMP` | Created timestamp |
| UPDATE_TIME | TIMESTAMP(6) | Yes |  | Updated timestamp |

### Constraints

```text
PK_PROCESS_STEP
UQ_PROCESS_STEP_CODE
CHK_PROCESS_STEP_STATUS
```

---

## 7.3 DEPARTMENT

Stores responsible departments.

| Column | Type | Nullable | Default | Description |
|---|---|---:|---:|---|
| DEPARTMENT_ID | NUMBER(19) | No | Sequence | Primary key |
| DEPARTMENT_CODE | VARCHAR2(30) | No |  | Unique department code |
| DEPARTMENT_NAME | VARCHAR2(100) | No |  | Department name |
| DESCRIPTION | VARCHAR2(500) | Yes |  | Description |
| STATUS | VARCHAR2(20) | No | `'ACTIVE'` | ACTIVE or INACTIVE |
| SORT_ORDER | NUMBER(10) | No | `0` | Display order |
| CREATE_TIME | TIMESTAMP(6) | No | `SYSTIMESTAMP` | Created timestamp |
| UPDATE_TIME | TIMESTAMP(6) | Yes |  | Updated timestamp |

### Constraints

```text
PK_DEPARTMENT
UQ_DEPARTMENT_CODE
CHK_DEPARTMENT_STATUS
```

---

## 7.4 ABNORMAL

Stores abnormal records.

| Column | Type | Nullable | Default | Description |
|---|---|---:|---:|---|
| ABNORMAL_ID | NUMBER(19) | No | Sequence | Primary key |
| ABNORMAL_NO | VARCHAR2(30) | No |  | Unique abnormal number |
| TITLE | VARCHAR2(200) | No |  | Abnormal title |
| DESCRIPTION | CLOB | No |  | Abnormal description |
| PROCESS_STEP_ID | NUMBER(19) | No |  | FK to PROCESS_STEP |
| DEPARTMENT_ID | NUMBER(19) | No |  | FK to DEPARTMENT |
| PRIORITY | VARCHAR2(20) | No | `'MEDIUM'` | LOW, MEDIUM, HIGH |
| STATUS | VARCHAR2(20) | No | `'OPEN'` | OPEN, PROCESSING, CLOSED |
| DUE_DATE | DATE | Yes |  | Expected completion date |
| REPORTER_ID | NUMBER(19) | No |  | FK to USER_INFO |
| CREATE_TIME | TIMESTAMP(6) | No | `SYSTIMESTAMP` | Created timestamp |
| UPDATE_TIME | TIMESTAMP(6) | Yes |  | Updated timestamp |
| UPDATE_BY | NUMBER(19) | Yes |  | FK to USER_INFO |

### Constraints

```text
PK_ABNORMAL
UQ_ABNORMAL_NO
CHK_ABNORMAL_PRIORITY
CHK_ABNORMAL_STATUS
FK_ABNORMAL_PROCESS_STEP
FK_ABNORMAL_DEPARTMENT
FK_ABNORMAL_REPORTER
FK_ABNORMAL_UPDATE_BY
```

### Business Rule Enforcement

| Rule | Enforcement |
|---|---|
| BR-001 Unique Abnormal Number | Unique constraint + application generation |
| BR-003 Process Step mandatory | NOT NULL + backend validation |
| BR-004 Department mandatory | NOT NULL + backend validation |
| BR-005 Priority values | Check constraint + backend validation |
| BR-006 Default status OPEN | Database default + backend logic |
| BR-007 Title required | NOT NULL + backend validation |
| BR-008 Description required | NOT NULL + backend validation |
| BR-009 Due Date not earlier than Created Date | Backend validation |

---

## 7.5 ABNORMAL_IMAGE

Stores image metadata for abnormal records.

| Column | Type | Nullable | Default | Description |
|---|---|---:|---:|---|
| IMAGE_ID | NUMBER(19) | No | Sequence | Primary key |
| ABNORMAL_ID | NUMBER(19) | No |  | FK to ABNORMAL |
| ORIGINAL_FILE_NAME | VARCHAR2(255) | No |  | Original uploaded file name |
| STORED_FILE_NAME | VARCHAR2(255) | No |  | UUID-based stored file name |
| FILE_PATH | VARCHAR2(500) | No |  | Relative or absolute file path |
| FILE_SIZE | NUMBER(19) | No |  | File size in bytes |
| MIME_TYPE | VARCHAR2(100) | No |  | image/jpeg or image/png |
| UPLOADED_BY | NUMBER(19) | No |  | FK to USER_INFO |
| UPLOADED_TIME | TIMESTAMP(6) | No | `SYSTIMESTAMP` | Uploaded timestamp |

### Constraints

```text
PK_ABNORMAL_IMAGE
CHK_IMAGE_FILE_SIZE
CHK_IMAGE_MIME_TYPE
FK_IMAGE_ABNORMAL
FK_IMAGE_UPLOADED_BY
```

### Image Rules

```text
Maximum images per abnormal: 10
Maximum file size: 5 MB
Allowed MIME types: image/jpeg, image/png
Stored file name: UUID-based
Physical file deletion handled by application service
```

---

## 7.6 AUDIT_LOG

Stores audit history for create, update, and delete operations.

| Column | Type | Nullable | Default | Description |
|---|---|---:|---:|---|
| AUDIT_ID | NUMBER(19) | No | Sequence | Primary key |
| USER_ID | NUMBER(19) | No |  | FK to USER_INFO |
| ACTION | VARCHAR2(30) | No |  | CREATE, UPDATE, DELETE |
| ENTITY_NAME | VARCHAR2(100) | No |  | Entity name, e.g. ABNORMAL |
| ENTITY_ID | NUMBER(19) | Yes |  | Primary key of affected entity |
| ENTITY_NO | VARCHAR2(50) | Yes |  | Business number, e.g. ABNORMAL_NO |
| DESCRIPTION | VARCHAR2(1000) | Yes |  | Human-readable audit description |
| ACTION_TIME | TIMESTAMP(6) | No | `SYSTIMESTAMP` | Action timestamp |
| IP_ADDRESS | VARCHAR2(50) | Yes |  | Client IP address |

### Constraints

```text
PK_AUDIT_LOG
CHK_AUDIT_ACTION
FK_AUDIT_USER
```

### Audit Rules

```text
Audit logs are append-only.
Audit logs are not updated.
Audit logs are not deleted.
Audit logs remain even if the target entity is deleted.
```

---

## 7.7 ABNORMAL_NO_COUNTER

System table used to generate daily abnormal numbers.

| Column | Type | Nullable | Default | Description |
|---|---|---:|---:|---|
| SEQ_DATE | DATE | No |  | Primary key, sequence date |
| LAST_NUMBER | NUMBER(5) | No | `0` | Last used number for the date |
| CREATE_TIME | TIMESTAMP(6) | No | `SYSTIMESTAMP` | Created timestamp |
| UPDATE_TIME | TIMESTAMP(6) | Yes |  | Updated timestamp |

### Constraints

```text
PK_ABNORMAL_NO_COUNTER
CHK_ABNORMAL_NO_COUNTER_LAST_NUMBER
```

---

## 8. Relationship Matrix

| Parent | Child | Relationship | FK Column | Delete Behavior |
|---|---|---:|---|---|
| USER_INFO | ABNORMAL | 1:N | REPORTER_ID | Restrict user deletion |
| USER_INFO | ABNORMAL | 1:N | UPDATE_BY | Restrict user deletion |
| USER_INFO | ABNORMAL_IMAGE | 1:N | UPLOADED_BY | Restrict user deletion |
| USER_INFO | AUDIT_LOG | 1:N | USER_ID | Restrict user deletion |
| DEPARTMENT | USER_INFO | 1:N | DEPARTMENT_ID | Restrict department deletion |
| DEPARTMENT | ABNORMAL | 1:N | DEPARTMENT_ID | Restrict department deletion |
| PROCESS_STEP | ABNORMAL | 1:N | PROCESS_STEP_ID | Restrict process step deletion |
| ABNORMAL | ABNORMAL_IMAGE | 1:N | ABNORMAL_ID | Cascade DB rows, application deletes files |

---

## 9. Delete Behavior Matrix

| Entity | Delete Type | Rule |
|---|---|---|
| USER_INFO | Soft delete only | Set `STATUS = INACTIVE`. Do not physically delete if user has abnormalities or audit logs. |
| PROCESS_STEP | Soft delete only | Set `STATUS = INACTIVE`. Do not delete if referenced by abnormalities. |
| DEPARTMENT | Soft delete only | Set `STATUS = INACTIVE`. Do not delete if referenced by abnormalities or users. |
| ABNORMAL | Hard delete allowed | Delete abnormal record. Related image metadata is cascade deleted. Physical files are deleted by application service. |
| ABNORMAL_IMAGE | Hard delete allowed | Delete image metadata and physical file through application service. |
| AUDIT_LOG | Not deletable | Audit logs must be retained. |

---

## 10. Abnormal Number Generation

### Format

```text
AB-YYYYMMDD-NNNNN
```

Example:

```text
AB-20260801-00001
```

### Generation Rule

The application generates `ABNORMAL_NO` when creating a new abnormal.

Logic:

```text
1. Get current date.
2. Lock the row in ABNORMAL_NO_COUNTER for current date.
3. If row does not exist, insert row with LAST_NUMBER = 0.
4. Increment LAST_NUMBER by 1.
5. Format:
   AB- + YYYYMMDD + - + LPAD(LAST_NUMBER, 5, '0')
6. Save ABNORMAL_NO into ABNORMAL table.
```

Example:

```text
Current date: 2026-08-01
LAST_NUMBER: 0
Next number: 1
ABNORMAL_NO: AB-20260801-00001
```

### Notes

- `ABNORMAL_NO` is unique.
- Daily counter resets by date.
- Maximum daily number is `99999`.
- Concurrency is controlled by row locking.

---

## 11. File Storage Strategy

Physical images are stored outside the database.

### Storage Root

Configurable in `application.yml`:

```yaml
ams:
  upload:
    root-path: ./uploads
```

### Storage Path

```text
{root-path}/abnormal-images/{yyyy}/{MM}/{dd}/{stored-file-name}
```

Example:

```text
./uploads/abnormal-images/2026/08/01/9f8c2a1e-44d1-4c0a-b1e2-7a9f0c3d1e11.jpg
```

### Database Stores

```text
ORIGINAL_FILE_NAME
STORED_FILE_NAME
FILE_PATH
FILE_SIZE
MIME_TYPE
UPLOADED_BY
UPLOADED_TIME
```

### Rules

```text
Original file name is kept for display.
Stored file name uses UUID to avoid conflicts.
Only JPEG and PNG are allowed.
Maximum file size is 5 MB.
Maximum 10 images per abnormal.
Physical files are deleted by application service.
```

---

## 12. Index Strategy

### USER_INFO

```text
UQ_USER_INFO_USERNAME
UQ_USER_INFO_EMAIL
IDX_USER_INFO_DEPARTMENT
IDX_USER_INFO_STATUS
```

### PROCESS_STEP

```text
UQ_PROCESS_STEP_CODE
IDX_PROCESS_STEP_STATUS
```

### DEPARTMENT

```text
UQ_DEPARTMENT_CODE
IDX_DEPARTMENT_STATUS
```

### ABNORMAL

```text
UQ_ABNORMAL_NO
IDX_ABNORMAL_STATUS
IDX_ABNORMAL_PRIORITY
IDX_ABNORMAL_PROCESS_STEP
IDX_ABNORMAL_DEPARTMENT
IDX_ABNORMAL_REPORTER
IDX_ABNORMAL_CREATE_TIME
IDX_ABNORMAL_DUE_DATE
IDX_ABNORMAL_UPDATE_BY
IDX_ABNORMAL_LIST_01
IDX_ABNORMAL_SEARCH_01
```

Recommended composite indexes:

```text
IDX_ABNORMAL_LIST_01 ON ABNORMAL (STATUS, PRIORITY, CREATE_TIME)
IDX_ABNORMAL_SEARCH_01 ON ABNORMAL (ABNORMAL_NO, TITLE)
```

### ABNORMAL_IMAGE

```text
IDX_ABNORMAL_IMAGE_ABNORMAL
```

### AUDIT_LOG

```text
IDX_AUDIT_LOG_USER_TIME
IDX_AUDIT_LOG_ENTITY
IDX_AUDIT_LOG_ACTION_TIME
```

---

## 13. Sequence Strategy

```text
SEQ_USER_INFO
SEQ_PROCESS_STEP
SEQ_DEPARTMENT
SEQ_ABNORMAL
SEQ_ABNORMAL_IMAGE
SEQ_AUDIT_LOG
```

Recommended settings:

```text
START WITH 1
INCREMENT BY 1
NOCACHE
```

For production performance, `CACHE 20` can be used if gaps are acceptable.

---

## 14. Seed Data

### Default Admin User

```text
USERNAME: admin
PASSWORD: admin123
ROLE: ADMIN
STATUS: ACTIVE
```

Password must be stored as BCrypt hash.

### Default Departments

```text
DEPT-PROD Production
DEPT-QA Quality Assurance
DEPT-MAINT Maintenance
DEPT-PLAN Planning
```

### Default Process Steps

```text
STEP-010 Cutting
STEP-020 Assembly
STEP-030 Inspection
STEP-040 Packing
```

---

## 15. Appendix: Oracle DDL

```sql
--------------------------------------------------------
-- DEPARTMENT
--------------------------------------------------------
CREATE TABLE DEPARTMENT (
    DEPARTMENT_ID      NUMBER(19)     NOT NULL,
    DEPARTMENT_CODE    VARCHAR2(30)   NOT NULL,
    DEPARTMENT_NAME    VARCHAR2(100)  NOT NULL,
    DESCRIPTION        VARCHAR2(500),
    STATUS             VARCHAR2(20)   DEFAULT 'ACTIVE' NOT NULL,
    SORT_ORDER         NUMBER(10)     DEFAULT 0 NOT NULL,
    CREATE_TIME        TIMESTAMP(6)   DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATE_TIME        TIMESTAMP(6),
    CONSTRAINT PK_DEPARTMENT PRIMARY KEY (DEPARTMENT_ID),
    CONSTRAINT UQ_DEPARTMENT_CODE UNIQUE (DEPARTMENT_CODE),
    CONSTRAINT CHK_DEPARTMENT_STATUS CHECK (STATUS IN ('ACTIVE', 'INACTIVE'))
);
```

```sql
--------------------------------------------------------
-- PROCESS_STEP
--------------------------------------------------------
CREATE TABLE PROCESS_STEP (
    PROCESS_STEP_ID    NUMBER(19)     NOT NULL,
    STEP_CODE          VARCHAR2(30)   NOT NULL,
    STEP_NAME          VARCHAR2(100)  NOT NULL,
    DESCRIPTION        VARCHAR2(500),
    STATUS             VARCHAR2(20)   DEFAULT 'ACTIVE' NOT NULL,
    SORT_ORDER         NUMBER(10)     DEFAULT 0 NOT NULL,
    CREATE_TIME        TIMESTAMP(6)   DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATE_TIME        TIMESTAMP(6),
    CONSTRAINT PK_PROCESS_STEP PRIMARY KEY (PROCESS_STEP_ID),
    CONSTRAINT UQ_PROCESS_STEP_CODE UNIQUE (STEP_CODE),
    CONSTRAINT CHK_PROCESS_STEP_STATUS CHECK (STATUS IN ('ACTIVE', 'INACTIVE'))
);
```

```sql
--------------------------------------------------------
-- USER_INFO
--------------------------------------------------------
CREATE TABLE USER_INFO (
    USER_ID            NUMBER(19)     NOT NULL,
    USERNAME           VARCHAR2(50)   NOT NULL,
    PASSWORD_HASH      VARCHAR2(255)  NOT NULL,
    FULL_NAME          VARCHAR2(100)  NOT NULL,
    EMAIL              VARCHAR2(100),
    DEPARTMENT_ID      NUMBER(19),
    ROLE               VARCHAR2(20)   DEFAULT 'USER' NOT NULL,
    STATUS             VARCHAR2(20)   DEFAULT 'ACTIVE' NOT NULL,
    CREATE_TIME        TIMESTAMP(6)   DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATE_TIME        TIMESTAMP(6),
    CONSTRAINT PK_USER_INFO PRIMARY KEY (USER_ID),
    CONSTRAINT UQ_USER_INFO_USERNAME UNIQUE (USERNAME),
    CONSTRAINT UQ_USER_INFO_EMAIL UNIQUE (EMAIL),
    CONSTRAINT CHK_USER_INFO_ROLE CHECK (ROLE IN ('ADMIN', 'USER')),
    CONSTRAINT CHK_USER_INFO_STATUS CHECK (STATUS IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT FK_USER_INFO_DEPARTMENT FOREIGN KEY (DEPARTMENT_ID)
        REFERENCES DEPARTMENT (DEPARTMENT_ID)
);
```

```sql
--------------------------------------------------------
-- ABNORMAL
--------------------------------------------------------
CREATE TABLE ABNORMAL (
    ABNORMAL_ID        NUMBER(19)     NOT NULL,
    ABNORMAL_NO        VARCHAR2(30)   NOT NULL,
    TITLE              VARCHAR2(200)  NOT NULL,
    DESCRIPTION        CLOB           NOT NULL,
    PROCESS_STEP_ID    NUMBER(19)     NOT NULL,
    DEPARTMENT_ID      NUMBER(19)     NOT NULL,
    PRIORITY           VARCHAR2(20)   DEFAULT 'MEDIUM' NOT NULL,
    STATUS             VARCHAR2(20)   DEFAULT 'OPEN' NOT NULL,
    DUE_DATE           DATE,
    REPORTER_ID        NUMBER(19)     NOT NULL,
    CREATE_TIME        TIMESTAMP(6)   DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATE_TIME        TIMESTAMP(6),
    UPDATE_BY          NUMBER(19),
    CONSTRAINT PK_ABNORMAL PRIMARY KEY (ABNORMAL_ID),
    CONSTRAINT UQ_ABNORMAL_NO UNIQUE (ABNORMAL_NO),
    CONSTRAINT CHK_ABNORMAL_PRIORITY CHECK (PRIORITY IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT CHK_ABNORMAL_STATUS CHECK (STATUS IN ('OPEN', 'PROCESSING', 'CLOSED')),
    CONSTRAINT FK_ABNORMAL_PROCESS_STEP FOREIGN KEY (PROCESS_STEP_ID)
        REFERENCES PROCESS_STEP (PROCESS_STEP_ID),
    CONSTRAINT FK_ABNORMAL_DEPARTMENT FOREIGN KEY (DEPARTMENT_ID)
        REFERENCES DEPARTMENT (DEPARTMENT_ID),
    CONSTRAINT FK_ABNORMAL_REPORTER FOREIGN KEY (REPORTER_ID)
        REFERENCES USER_INFO (USER_ID),
    CONSTRAINT FK_ABNORMAL_UPDATE_BY FOREIGN KEY (UPDATE_BY)
        REFERENCES USER_INFO (USER_ID)
);
```

```sql
--------------------------------------------------------
-- ABNORMAL_IMAGE
--------------------------------------------------------
CREATE TABLE ABNORMAL_IMAGE (
    IMAGE_ID             NUMBER(19)     NOT NULL,
    ABNORMAL_ID          NUMBER(19)     NOT NULL,
    ORIGINAL_FILE_NAME   VARCHAR2(255)  NOT NULL,
    STORED_FILE_NAME     VARCHAR2(255)  NOT NULL,
    FILE_PATH            VARCHAR2(500)  NOT NULL,
    FILE_SIZE            NUMBER(19)     NOT NULL,
    MIME_TYPE            VARCHAR2(100)  NOT NULL,
    UPLOADED_BY          NUMBER(19)     NOT NULL,
    UPLOADED_TIME        TIMESTAMP(6)   DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_ABNORMAL_IMAGE PRIMARY KEY (IMAGE_ID),
    CONSTRAINT CHK_IMAGE_FILE_SIZE CHECK (FILE_SIZE > 0),
    CONSTRAINT CHK_IMAGE_MIME_TYPE CHECK (MIME_TYPE IN ('image/jpeg', 'image/png')),
    CONSTRAINT FK_IMAGE_ABNORMAL FOREIGN KEY (ABNORMAL_ID)
        REFERENCES ABNORMAL (ABNORMAL_ID) ON DELETE CASCADE,
    CONSTRAINT FK_IMAGE_UPLOADED_BY FOREIGN KEY (UPLOADED_BY)
        REFERENCES USER_INFO (USER_ID)
);
```

```sql
--------------------------------------------------------
-- AUDIT_LOG
--------------------------------------------------------
CREATE TABLE AUDIT_LOG (
    AUDIT_ID         NUMBER(19)      NOT NULL,
    USER_ID          NUMBER(19)      NOT NULL,
    ACTION           VARCHAR2(30)    NOT NULL,
    ENTITY_NAME      VARCHAR2(100)   NOT NULL,
    ENTITY_ID        NUMBER(19),
    ENTITY_NO        VARCHAR2(50),
    DESCRIPTION      VARCHAR2(1000),
    ACTION_TIME      TIMESTAMP(6)    DEFAULT SYSTIMESTAMP NOT NULL,
    IP_ADDRESS       VARCHAR2(50),
    CONSTRAINT PK_AUDIT_LOG PRIMARY KEY (AUDIT_ID),
    CONSTRAINT CHK_AUDIT_ACTION CHECK (ACTION IN ('CREATE', 'UPDATE', 'DELETE')),
    CONSTRAINT FK_AUDIT_USER FOREIGN KEY (USER_ID)
        REFERENCES USER_INFO (USER_ID)
);
```

```sql
--------------------------------------------------------
-- ABNORMAL_NO_COUNTER
--------------------------------------------------------
CREATE TABLE ABNORMAL_NO_COUNTER (
    SEQ_DATE        DATE           NOT NULL,
    LAST_NUMBER     NUMBER(5)      DEFAULT 0 NOT NULL,
    CREATE_TIME     TIMESTAMP(6)   DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATE_TIME     TIMESTAMP(6),
    CONSTRAINT PK_ABNORMAL_NO_COUNTER PRIMARY KEY (SEQ_DATE),
    CONSTRAINT CHK_ABNORMAL_NO_COUNTER_NUM CHECK (LAST_NUMBER >= 0 AND LAST_NUMBER <= 99999)
);
```

```sql
--------------------------------------------------------
-- SEQUENCES
--------------------------------------------------------
CREATE SEQUENCE SEQ_USER_INFO START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_PROCESS_STEP START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_DEPARTMENT START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_ABNORMAL START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_ABNORMAL_IMAGE START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_AUDIT_LOG START WITH 1 INCREMENT BY 1 NOCACHE;
```

```sql
--------------------------------------------------------
-- INDEXES
--------------------------------------------------------
CREATE INDEX IDX_USER_INFO_DEPARTMENT ON USER_INFO (DEPARTMENT_ID);
CREATE INDEX IDX_USER_INFO_STATUS ON USER_INFO (STATUS);

CREATE INDEX IDX_PROCESS_STEP_STATUS ON PROCESS_STEP (STATUS);
CREATE INDEX IDX_DEPARTMENT_STATUS ON DEPARTMENT (STATUS);

CREATE INDEX IDX_ABNORMAL_STATUS ON ABNORMAL (STATUS);
CREATE INDEX IDX_ABNORMAL_PRIORITY ON ABNORMAL (PRIORITY);
CREATE INDEX IDX_ABNORMAL_PROCESS_STEP ON ABNORMAL (PROCESS_STEP_ID);
CREATE INDEX IDX_ABNORMAL_DEPARTMENT ON ABNORMAL (DEPARTMENT_ID);
CREATE INDEX IDX_ABNORMAL_REPORTER ON ABNORMAL (REPORTER_ID);
CREATE INDEX IDX_ABNORMAL_CREATE_TIME ON ABNORMAL (CREATE_TIME);
CREATE INDEX IDX_ABNORMAL_DUE_DATE ON ABNORMAL (DUE_DATE);
CREATE INDEX IDX_ABNORMAL_UPDATE_BY ON ABNORMAL (UPDATE_BY);
CREATE INDEX IDX_ABNORMAL_LIST_01 ON ABNORMAL (STATUS, PRIORITY, CREATE_TIME);
CREATE INDEX IDX_ABNORMAL_SEARCH_01 ON ABNORMAL (ABNORMAL_NO, TITLE);

CREATE INDEX IDX_ABNORMAL_IMAGE_ABNORMAL ON ABNORMAL_IMAGE (ABNORMAL_ID);

CREATE INDEX IDX_AUDIT_LOG_USER_TIME ON AUDIT_LOG (USER_ID, ACTION_TIME);
CREATE INDEX IDX_AUDIT_LOG_ENTITY ON AUDIT_LOG (ENTITY_NAME, ENTITY_ID);
CREATE INDEX IDX_AUDIT_LOG_ACTION_TIME ON AUDIT_LOG (ACTION_TIME);
```

---

## 16. JPA Implementation Notes

When generating Spring Boot entities:

```text
Use Java 21
Use jakarta.persistence annotations
Use Long for NUMBER(19)
Use LocalDateTime for TIMESTAMP(6)
Use @SequenceGenerator with allocationSize = 1
Do not use GenerationType.IDENTITY
Do not expose entities directly in REST APIs
Use request/response DTOs
```

Example:

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_abnormal")
@SequenceGenerator(name = "seq_abnormal", sequenceName = "SEQ_ABNORMAL", allocationSize = 1)
@Column(name = "ABNORMAL_ID")
private Long abnormalId;
```

---

## 17. Final Acceptance

This database design is ready for backend development when:

```text
All tables are created
All constraints are applied
All sequences are created
All indexes are created
Seed data is inserted
Abnormal number generation is implemented
File upload metadata is stored correctly
Audit log is written for CREATE, UPDATE, DELETE operations
```