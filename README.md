# Employee Leave Management System

## Overview
A Spring Boot REST API project for managing employees and leave requests.

## Tech Stack
- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- REST APIs
- Postman

## Features
- Employee Management
- Apply Leave
- Approve Leave
- Reject Leave
- Automatic Leave Balance Creation
- Leave Balance Update on Approval
- Duplicate Leave Validation

## API Endpoints

### Employee APIs
- POST /employees
- GET /employees
- GET /employees/{id}

### Leave APIs
- POST /leaves/apply/{employeeId}
- GET /leaves/pending
- PUT /leaves/approve/{leaveId}
- PUT /leaves/reject/{leaveId}

## Future Improvements
- Employee Update/Delete
- Leave History
- Search Employee
- JWT Authentication
- Swagger Documentation
- Global Exception Handling
- DTO Pattern
