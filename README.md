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

### Backend Setup

```bash
cd backend
# Configure environment variables or edit application-dev.yml
export DB_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
export DB_USERNAME=ams
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key-here

mvn spring-boot:run
```

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

## Documentation

See `/docs` folder for detailed specifications.

## Default Admin Account

- Username: `admin`
- Password: `admin123`

## Project Structure

```
ams/
├── backend/          # Spring Boot application
├── frontend/         # React application
├── docs/             # Specifications
└── README.md
```
