# Employee Leave Management System

A Spring Boot REST API to manage employees and their leave requests, secured with JWT and role-based access.

Employees apply for leave, managers approve or reject it, and every employee's leave balance is created and updated automatically.

## Tech Stack

Java 17 · Spring Boot 4 (Web MVC, Data JPA, Security, Validation) · Hibernate · PostgreSQL · JWT (jjwt) + BCrypt · Swagger (springdoc-openapi) · Maven

## Features

- **Employee management**: create, read, update, delete, and search by name and department
- **Leave workflow**: apply, approve, reject, and cancel leave, plus leave history with an optional status filter
- **Automatic leave balance**: 20 days are created with every new employee and deducted only when a leave is approved
- **Leave validation**: required fields (`@Valid`), end date not before start date, no past dates, no overlapping PENDING/APPROVED leave, enough balance left, and only PENDING leave can be approved, rejected, or cancelled
- **DTOs**: entities are never exposed directly; requests and responses use Java `record` DTOs
- **Global exception handling**: `@RestControllerAdvice` returns one consistent JSON error body
- **JWT authentication** with two roles: `EMPLOYEE` and `MANAGER`
- **Swagger UI** with an "Authorize" button for JWT

Request flow: **Controller → Service → Repository → Database**.

---

## API Endpoints (16)

**Roles:** `Public` means no token is needed. `Any` means any logged-in user (EMPLOYEE or MANAGER).

### Authentication

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Create an account (role defaults to EMPLOYEE) |
| POST | `/auth/login` | Public | Log in and receive a JWT |

### Employees

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/employees` | MANAGER | Create an employee (and a 20-day leave balance) |
| GET | `/employees` | Any | List all employees |
| GET | `/employees/{id}` | Any | Get one employee |
| PUT | `/employees/{id}` | MANAGER | Update an employee |
| DELETE | `/employees/{id}` | MANAGER | Delete an employee (with their balance and leaves) |
| GET | `/employees/search?name=sah&department=IT` | Any | Search by name and/or department (both optional) |
| GET | `/employees/{id}/leave-balance` | Any | Total, used, and remaining leave days |

### Leaves

| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/leaves/apply/{employeeId}` | Any | Apply for leave (status PENDING) |
| GET | `/leaves/pending` | MANAGER | All pending leaves |
| GET | `/leaves/history/{employeeId}` | Any | Leave history of an employee (newest first) |
| GET | `/leaves/{leaveId}` | Any | Get one leave request |
| PUT | `/leaves/approve/{leaveId}` | MANAGER | Approve and deduct days from the balance |
| PUT | `/leaves/reject/{leaveId}` | MANAGER | Reject |
| PUT | `/leaves/cancel/{leaveId}` | Any | Cancel a PENDING leave |

List endpoints return a plain JSON array.

---

## How JWT Login Works

1. Register with `POST /auth/register` (`{"username":"manager1","password":"secret123","role":"MANAGER"}`; role defaults to EMPLOYEE).
2. Log in with `POST /auth/login` and copy the `token` from the response.
3. Send it on every protected request: `Authorization: Bearer <token>`.

**How it works:** on login, the password is checked against the BCrypt hash, and a JWT signed with HMAC-SHA256 is returned. It holds the username, role, and expiry. `JwtFilter` validates the token on every request and sets the logged-in user (username and role read from the token, no database lookup). `SecurityConfig` then decides access by role. The server keeps no session (stateless).

> For this demo, anyone can register as MANAGER. In a real system, managers would be created by an admin.

---

## How to Run Locally

**Needs:** Java 17 and PostgreSQL.

1. Create a database named `Employee_DB` in PostgreSQL.
2. Set your database password as an environment variable (never put it in the code):

   ```bash
   export DB_PASSWORD=your_real_password
   ```

   In IntelliJ: *Run → Edit Configurations → Environment variables* → `DB_PASSWORD=your_real_password`.
   Optional: `DB_URL`, `DB_USERNAME` (default `postgres`), `JWT_SECRET` (at least 32 characters).
3. Start the app:

   ```bash
   ./mvnw spring-boot:run
   ```

   Or open the project in IntelliJ and run `EmployeeLeaveManagementSystemApplication`.

The API starts on `http://localhost:8080`.

---

## Swagger / OpenAPI

- Swagger UI: **http://localhost:8080/swagger-ui.html**
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Call `/auth/login`, click **Authorize**, and paste the token. All requests from Swagger then include it.
