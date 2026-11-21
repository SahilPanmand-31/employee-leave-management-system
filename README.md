# Employee Leave Management System

A Spring Boot REST API to manage employees and their leave requests, secured with JWT and role-based access.

Employees apply for leave, managers approve or reject it, and every employee's leave balance is created and updated automatically.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4 (Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation) |
| ORM | Hibernate |
| Database | PostgreSQL (H2 in-memory for tests) |
| Security | JWT (jjwt 0.12) + BCrypt password hashing |
| API Docs | springdoc-openapi 3 (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMvc (`@WebMvcTest`, `@SpringBootTest`) |
| Build / Run | Maven Wrapper, Docker, Docker Compose |

---

## Features

- **Employee management**: create, read, update, delete, and search by name and department
- **Leave workflow**: apply, approve, reject, and cancel leave, plus leave history with an optional status filter
- **Automatic leave balance**: 20 days are created with every new employee and deducted only when a leave is approved
- **Leave validation**
  - All required fields are checked with Bean Validation (`@Valid`)
  - End date must be on or after the start date
  - No leave for past dates
  - No overlapping PENDING or APPROVED leave for the same employee
  - The employee must have enough balance left
  - Leave can be approved, rejected, or cancelled only while it is PENDING, so the balance is never deducted twice
- **DTO pattern**: entities are never exposed directly; requests and responses use Java `record` DTOs
- **Global exception handling**: `@RestControllerAdvice` returns one consistent JSON error body
- **JWT authentication** with two roles: `EMPLOYEE` and `MANAGER`
- **Swagger UI** with an "Authorize" button for JWT
- **Docker**: one command starts the app and PostgreSQL
- **No secrets in code**: DB credentials and the JWT secret come from environment variables

---

## Architecture

The request flow is **Controller → Service → Repository → Database**, with DTOs at the edges:

```
Client ──► JwtFilter ──────────────► Controller ──► Service ──► Repository ──► PostgreSQL
              (checks token)            (@Valid DTO)   (business     (Spring Data
                                                       rules)         JPA)
                        Exceptions ──► GlobalExceptionHandler ──► JSON error response
```

### Folder Structure

```
src/main/java/com/elms/elms
├── EmployeeLeaveManagementSystemApplication.java
├── config
│   ├── OpenApiConfig.java            # Swagger + JWT "Authorize" button
│   └── SecurityConfig.java           # URL rules per role, stateless session, BCrypt
├── controller
│   ├── AuthController.java           # /auth/register, /auth/login
│   ├── EmployeeController.java       # /employees/**
│   └── LeaveController.java          # /leaves/**
├── dto                               # request/response records (+ validation)
│   ├── EmployeeRequest / EmployeeResponse
│   ├── LeaveApplyRequest / LeaveResponse / LeaveBalanceResponse
│   ├── RegisterRequest / LoginRequest / AuthResponse
│   └── ErrorResponse                 # common shape for errors
├── entity
│   ├── Employee, LeaveRequest, LeaveBalance, AppUser
│   └── LeaveStatus, Role (enums)
├── exception
│   ├── GlobalExceptionHandler.java   # @RestControllerAdvice
│   ├── ResourceNotFoundException     # 404
│   ├── DuplicateResourceException    # 409
│   └── InvalidLeaveRequestException  # 400
├── repository                        # Spring Data JPA interfaces
├── security
│   ├── JwtUtil.java                  # create / parse tokens
│   └── JwtFilter.java                # reads "Authorization: Bearer <token>"
└── service                           # interfaces + implementations (business rules)

src/test/java/com/elms/elms
├── service/EmployeeServiceImplTest.java   # Mockito unit tests
├── service/LeaveServiceImplTest.java      # Mockito unit tests
├── LeaveFlowIntegrationTest.java          # full flow on H2 with real JWTs
└── EmployeeLeaveManagementSystemApplicationTests.java
```

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

## Authentication Flow (example)

**1. Register a manager and an employee**

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"manager1","password":"secret123","role":"MANAGER"}'

curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"employee1","password":"secret123"}'
```

**2. Log in and copy the token**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"manager1","password":"secret123"}'
```

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "manager1",
  "role": "MANAGER"
}
```

**3. Call protected endpoints with the token**

```bash
TOKEN=eyJhbGciOiJIUzI1NiJ9...

curl -X POST http://localhost:8080/employees \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Sahil","email":"sahil@example.com","department":"IT","role":"Java Developer"}'

curl -X POST http://localhost:8080/leaves/apply/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"leaveType":"CASUAL","startDate":"2027-01-10","endDate":"2027-01-12","reason":"Family function"}'

curl -X PUT http://localhost:8080/leaves/approve/1 -H "Authorization: Bearer $TOKEN"
```

**How it works:** on login, the password is checked against the BCrypt hash, and a JWT signed with HMAC-SHA256 is returned. It holds the username, role, and expiry. `JwtFilter` validates the token on every request and sets the logged-in user (username and role read from the token, no database lookup). `SecurityConfig` then decides access by role. The server keeps no session (stateless).

> For this demo, anyone can register as MANAGER. In a real system, managers would be created by an admin.

### Error format

Errors thrown in controllers and services use the same JSON body (a 401 or 403 from the security filter, such as a missing token, returns only the status code):

```json
{
  "timestamp": "2026-11-20T18:30:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/leaves/apply/1",
  "validationErrors": {
    "startDate": "Start Date is required",
    "reason": "Reason is required"
  }
}
```

| Status | When |
|---|---|
| 400 | Validation failed, bad dates, overlapping leave, insufficient balance, leave not PENDING |
| 401 | Missing, invalid, or expired token, or a wrong login |
| 403 | Logged in but the role is not allowed (e.g. EMPLOYEE approving leave) |
| 404 | Employee, leave, or balance not found |
| 409 | Email or username already exists |

---

## How to Run

### Option 1: Local (Java 17 + PostgreSQL)

1. Create a database, e.g. `Employee_DB`.
2. Set your credentials as environment variables. The values in `application.properties` are only placeholders:

   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/Employee_DB
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_real_password
   export JWT_SECRET=a-long-random-secret-with-at-least-32-characters
   ```

   (In IntelliJ or STS, add them under *Run Configuration → Environment variables*.)
3. Run the app:

   ```bash
   ./mvnw spring-boot:run
   ```

The API starts on `http://localhost:8080`.

### Option 2: Docker Compose (app + PostgreSQL)

```bash
cp .env.example .env      # then edit the passwords and the JWT secret
docker compose up --build
```

This starts PostgreSQL 16 and the app (multi-stage Docker build, runs as a non-root user). The app waits until the database is healthy.

### Environment variables

| Variable | Default (placeholder) | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/Employee_DB` | JDBC URL |
| `DB_USERNAME` | `postgres` | DB user |
| `DB_PASSWORD` | `your_password` | DB password |
| `JWT_SECRET` | dev-only value | HMAC key, at least 32 characters |
| `JWT_EXPIRATION_MS` | `3600000` (1 hour) | Token lifetime |
| `SHOW_SQL` | `false` | Print SQL to the console |

---

## Swagger / OpenAPI

- Swagger UI: **http://localhost:8080/swagger-ui.html**
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Call `/auth/login`, click **Authorize**, and paste the token. All requests from Swagger then include it.

---

## Tests

```bash
./mvnw test
```

The tests use the `test` profile with an **H2 in-memory database**, so PostgreSQL is not needed. There are 21 tests:

- `EmployeeServiceImplTest` and `LeaveServiceImplTest`: JUnit 5 + Mockito unit tests for the business rules (duplicate email, date checks, overlap, balance deduction, double-approval protection)
- `LeaveFlowIntegrationTest`: the full flow with real JWTs (register → login → create employee → apply → approve → balance check, plus 401/403 checks)
- `EmployeeLeaveManagementSystemApplicationTests`: the application context starts

---

## Future Improvements

- Link a login account to an employee, so employees can only apply for or view their own leave
- Refresh tokens and logout (token blacklist)
- Exclude weekends and public holidays when counting leave days
- Separate balances per leave type (casual, sick, earned) with a yearly reset
- Email notifications on approval or rejection
- Database migrations with Flyway instead of `ddl-auto=update`
- CI pipeline with GitHub Actions
