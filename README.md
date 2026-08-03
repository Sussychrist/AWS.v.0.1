# Abnormal Management System (AMS)

Internal web application to digitize manufacturing abnormality tracking.

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.x, Oracle 21c
- **Frontend:** React 18+, Material UI 5+
- **Security:** JWT authentication

## Quick Start

### Prerequisites

- Java 21+
- Node.js 18+
- Oracle Database 19c/21c
- Maven 3.9+

### Database Setup

Run the schema script before starting the application:

```bash
# Connect to Oracle as AMS user
sqlplus ams/password@localhost:1521/XEPDB1 @docs/database.sql
```

**Important:** Seed data is inserted in order: DEPARTMENT → PROCESS_STEP → USER_INFO

### Backend Setup

```bash
cd backend

# Configure environment variables (production)
export DB_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
export DB_USERNAME=ams
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key-here
export AMS_UPLOAD_ROOT_PATH=/path/to/uploads  # Optional, defaults to ./uploads

# Or edit application-dev.yml for development
mvn spring-boot:run
```

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

## Documentation

See `/docs` folder for detailed specifications:
- `00_PRD.md` - Product requirements
- `01_Database_Design_Specification.md` - Database schema
- `02_Business_Rules_Specification.md` - Validation rules
- `03_Screen_Specification.md` - UI layouts
- `04_REST_API_Specification.md` - API contracts
- `05_System_Architecture.md` - Technical architecture
- `06_Security_Design_Specification.md` - Security rules
- `07_Coding_Standards_Development_Guidelines.md` - Coding conventions
- `08_Implementation_Plan_Task_Breakdown.md` - Implementation phases
- `database.sql` - Oracle DDL script

## Default Admin Account

- **Username:** `admin`
- **Password:** `admin123`

⚠️ **IMPORTANT:** Change this password immediately after deployment!

## Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `DB_URL` | Oracle JDBC connection URL | Yes | - |
| `DB_USERNAME` | Database username | Yes | - |
| `DB_PASSWORD` | Database password | Yes | - |
| `JWT_SECRET` | Secret key for JWT signing | Yes | - |
| `AMS_UPLOAD_ROOT_PATH` | Root path for file uploads | No | `./uploads` |

## Project Structure

```
ams/
├── backend/                    # Spring Boot application
│   ├── src/main/java/com/ams/
│   │   ├── config/            # Configuration classes
│   │   ├── controller/        # REST controllers
│   │   ├── service/           # Business logic
│   │   ├── repository/        # Data access
│   │   ├── entity/            # JPA entities
│   │   ├── dto/               # Data transfer objects
│   │   ├── security/          # Security components
│   │   ├── exception/         # Exception handling
│   │   └── common/            # Shared utilities
│   └── src/main/resources/
│       ├── application.yml    # Base configuration
│       ├── application-dev.yml # Development profile
│       └── application-prod.yml # Production profile
├── frontend/                   # React application
│   └── src/
│       ├── api/               # API client modules
│       ├── components/        # Reusable components
│       ├── pages/             # Screen components
│       ├── context/           # React context
│       ├── hooks/             # Custom hooks
│       └── utils/             # Utilities
├── docs/                       # Specifications
├── AGENTS.md                   # AI agent coding rules
├── README.md                   # This file
└── .gitignore
```

## Password Reset Process (MVP)

For MVP, password resets are handled by ADMIN through user management:
1. ADMIN logs in
2. Navigate to User Management
3. Edit the user
4. Enter new password (min 8 characters)
5. Save (password is BCrypt hashed automatically)

## Key Business Rules Summary

### Status Transitions
Allowed: OPEN→PROCESSING, OPEN→CLOSED, PROCESSING→OPEN, PROCESSING→CLOSED, CLOSED→PROCESSING  
Forbidden: CLOSED→OPEN

### Abnormal Number Format
`AB-YYYYMMDD-NNNNN` (e.g., AB-20240801-00001)

### Image Upload Limits
- Max 10 images per abnormal record
- Max 5MB per image
- JPEG/PNG formats only

### Audit Logging
All CREATE, UPDATE, DELETE operations on abnormalities, users, master data, and images are logged.
