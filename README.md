# Student Task Management System (STMS)

A full-stack, production-ready web application for managing academic tasks, courses, and student–lecturer collaboration.

---

## Table of Contents
1. [Tech Stack](#tech-stack)
2. [System Architecture](#system-architecture)
3. [Prerequisites](#prerequisites)
4. [Backend Setup](#backend-setup)
5. [Frontend Setup](#frontend-setup)
6. [Default Credentials](#default-credentials)
7. [Password Policy](#password-policy)
8. [API Overview](#api-overview)
9. [Role-Based Access Control](#role-based-access-control)
10. [Features](#features)
11. [CSV Import](#csv-import)
12. [Project Structure](#project-structure)

---

## Tech Stack

### Backend
| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.2 |
| Spring Security + JWT | Latest |
| Spring Data JPA / Hibernate | Latest |
| MySQL | 8.0+ |
| Maven | 3.9+ |
| Lombok | Latest |
| OpenAPI / Swagger | 2.3 |
| OpenCSV | 5.9 |

### Frontend
| Technology | Version |
|---|---|
| Angular | 20+ |
| Angular Material | 20+ |
| TypeScript | 5.4 |
| RxJS | 7.8 |

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Angular 20 Frontend  (localhost:4200)                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐  │
│  │  Admin   │ │ Lecturer │ │ Student  │ │    Auth      │  │
│  │  Module  │ │  Module  │ │  Module  │ │    Module    │  │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────┘  │
│       JWT Interceptor → HTTP → Proxy → :8080/api            │
└─────────────────────────────────────────────────────────────┘
                          │
                    REST API (JSON)
                          │
┌─────────────────────────────────────────────────────────────┐
│  Spring Boot 3 Backend  (localhost:8080)                    │
│  ┌──────────────────┐  ┌──────────────────────────────┐    │
│  │  Spring Security │  │  Controllers                  │    │
│  │  JWT Filter      │  │  /auth  /admin  /lecturer     │    │
│  └──────────────────┘  │  /student  /tasks  /notifs    │    │
│                        └──────────────────────────────┘    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Service Layer (Business Logic)                       │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  JPA Repositories → MySQL 8 (stms_db)                │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- **MySQL 8.0+**
- **Node.js 20+** and **npm 10+**
- **Angular CLI 20** (`npm install -g @angular/cli`)

---

## Backend Setup

### 1. Create the MySQL database
```sql
CREATE DATABASE stms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure database credentials
Edit `stms-backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/stms_db?createDatabaseIfNotExist=true
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

### 3. Build and run
```bash
cd stms-backend
mvn clean install -DskipTests
mvn spring-boot:run
```

The backend starts at **http://localhost:8080**

### 4. Swagger UI
Visit **http://localhost:8080/api/swagger-ui.html** to explore all REST endpoints interactively.

---

## Frontend Setup

### 1. Install dependencies
```bash
cd stms-frontend
npm install
```

### 2. Run development server
```bash
npm start
```
The frontend starts at **http://localhost:4200** and proxies `/api` calls to `:8080`.

### 3. Production build
```bash
npm run build:prod
```
Output is in `dist/stms-frontend/` — deploy to any static host or behind NGINX.

---

## Default Credentials

The system seeds a default administrator on first startup:

| Field | Value |
|---|---|
| Email | `admin@stms.ac.tz` |
| Password | `Admin@123` |
| Role | ADMIN |
| First Login | false (no password change required) |

---

## Password Policy

| Rule | Detail |
|---|---|
| Default password | User's **LASTNAME in UPPERCASE** (e.g. `DOE`) |
| First login | System forces password change before any other action |
| BCrypt | All passwords hashed with BCrypt (strength 10) |
| Admin reset | Admin can reset any user's password to default via the Users page |

---

## API Overview

All endpoints (except `/auth/login`) require a JWT Bearer token.

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Login and receive JWT |
| POST | `/api/auth/change-password` | Change own password |
| GET  | `/api/auth/me` | Get current user profile |

### Admin – Users (`/api/admin/users`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/users` | List all users (search, filter by role, paginate) |
| POST | `/api/admin/users` | Create user |
| PUT | `/api/admin/users/{id}` | Update user |
| PATCH | `/api/admin/users/{id}/toggle-status` | Activate/deactivate |
| DELETE | `/api/admin/users/{id}` | Delete user |
| POST | `/api/admin/users/{id}/reset-password` | Reset to default password |
| POST | `/api/admin/users/import` | Bulk import students from CSV |
| GET | `/api/admin/users/import/template` | Download CSV template |

### Admin – Courses (`/api/admin/courses`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/courses` | List all courses |
| POST | `/api/admin/courses` | Create course |
| PUT | `/api/admin/courses/{id}` | Update course |
| DELETE | `/api/admin/courses/{id}` | Delete course |
| POST | `/api/admin/courses/{id}/lecturers/{lid}` | Assign lecturer |
| DELETE | `/api/admin/courses/{id}/lecturers/{lid}` | Remove lecturer |

### Lecturer (`/api/lecturer`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/lecturer/dashboard` | Dashboard stats |
| GET | `/api/lecturer/courses` | Assigned courses |
| POST | `/api/lecturer/tasks` | Create task |
| GET | `/api/lecturer/tasks` | My tasks |
| PUT | `/api/lecturer/tasks/{id}` | Update task |
| DELETE | `/api/lecturer/tasks/{id}` | Delete task |
| GET/POST | `/api/tasks/{id}/comments` | View/add comments |

### Student (`/api/student`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/student/dashboard` | Dashboard stats |
| GET | `/api/student/courses/available` | Courses to enroll in |
| GET | `/api/student/courses/enrolled` | Enrolled courses |
| POST | `/api/student/courses/{id}/enroll` | Enroll in course |
| DELETE | `/api/student/courses/{id}/unenroll` | Unenroll |
| GET | `/api/student/tasks` | Tasks from enrolled courses |
| GET/POST | `/api/tasks/{id}/comments` | View/add comments |

### Notifications (`/api/notifications`)
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/notifications` | Get notifications (paginated) |
| GET | `/api/notifications/unread-count` | Count unread |
| PATCH | `/api/notifications/{id}/read` | Mark as read |
| PATCH | `/api/notifications/mark-all-read` | Mark all as read |

---

## Role-Based Access Control

| Feature | ADMIN | LECTURER | STUDENT |
|---|:---:|:---:|:---:|
| Create/Edit/Delete Users | ✅ | ❌ | ❌ |
| Reset Password | ✅ | ❌ | ❌ |
| Bulk CSV Import | ✅ | ❌ | ❌ |
| Create/Edit/Delete Courses | ✅ | ❌ | ❌ |
| Assign Lecturers to Courses | ✅ | ❌ | ❌ |
| System-wide Reports | ✅ | ❌ | ❌ |
| View Assigned Courses | ❌ | ✅ | ❌ |
| Create/Edit/Delete Tasks | ❌ | ✅ | ❌ |
| View Enrolled Course Tasks | ❌ | ❌ | ✅ |
| Enroll/Unenroll Courses | ❌ | ❌ | ✅ |
| Comment on Tasks | ❌ | ✅ | ✅ |
| Receive Notifications | ❌ | ✅ | ✅ |

---

## CSV Import

### Template format
```csv
registration_number,first_name,middle_name,last_name,email
T21-0001-2022,John,M,Doe,john@university.ac.tz
T21-0002-2022,Jane,,Smith,jane@university.ac.tz
```

### Rules
- Header row is required and is automatically skipped
- `middle_name` may be empty
- Duplicate `registration_number` or `email` records are rejected with an error message
- Import summary shows: total records, success count, failed count, per-row error messages
- Download the template from Admin → Users → "CSV Template" button

---

## Features

| Feature | Status |
|---|---|
| JWT Authentication | ✅ |
| Role-Based Access Control | ✅ |
| First-login Password Change | ✅ |
| BCrypt Password Encoding | ✅ |
| Admin: Full User CRUD | ✅ |
| Admin: Bulk Student CSV Import | ✅ |
| Admin: Course & Lecturer Management | ✅ |
| Admin: System Reports & Charts | ✅ |
| Lecturer: Course View & Student List | ✅ |
| Lecturer: Task CRUD | ✅ |
| Lecturer: Comment & Reply | ✅ |
| Student: Course Enrollment | ✅ |
| Student: Task Viewing | ✅ |
| Student: Task Comments | ✅ |
| Real-time Notification System | ✅ |
| Dark / Light Theme Toggle | ✅ |
| Responsive Layout | ✅ |
| Swagger / OpenAPI Docs | ✅ |
| Global Exception Handling | ✅ |
| Pagination & Search | ✅ |

---

## Project Structure

```
stms/
├── stms-backend/
│   ├── src/main/java/com/stms/
│   │   ├── config/          # Security, OpenAPI, DataSeeder
│   │   ├── controller/      # REST controllers per role
│   │   ├── dto/             # Request/Response DTOs
│   │   ├── entity/          # JPA entities + enums
│   │   ├── exception/       # Custom exceptions + GlobalExceptionHandler
│   │   ├── repository/      # Spring Data JPA repositories
│   │   ├── security/        # JWT utility, filter, UserDetailsService
│   │   └── service/         # Interfaces + implementations
│   └── src/main/resources/
│       └── application.properties
│
└── stms-frontend/
    ├── proxy.conf.json
    └── src/app/
        ├── core/
        │   ├── guards/      # authGuard, roleGuards
        │   ├── interceptors/# jwtInterceptor
        │   ├── models/      # TypeScript interfaces
        │   └── services/    # HTTP service layer
        └── modules/
            ├── auth/        # Login, ChangePassword
            ├── admin/       # Dashboard, Users, Courses, Reports, Notifications
            ├── lecturer/    # Dashboard, Courses, Tasks, Notifications
            ├── student/     # Dashboard, Courses, Tasks, Notifications
            └── shared/      # Shell layout, NotificationsPage, Unauthorized
```

---

## Environment Variables

For production, override these in `application.properties` or via environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://prod-host:3306/stms_db
SPRING_DATASOURCE_USERNAME=stms_user
SPRING_DATASOURCE_PASSWORD=secure_password
JWT_SECRET=your_256bit_secret_key_here
JWT_EXPIRATION=86400000
```
