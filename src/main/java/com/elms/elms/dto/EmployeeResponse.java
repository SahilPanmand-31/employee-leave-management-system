package com.elms.elms.dto;

import com.elms.elms.entity.Employee;

// Data we send back for an employee (no internal relations, no lazy-loading issues).
public record EmployeeResponse(
        Long id,
        String name,
        String email,
        String department,
        String role) {

    // Converts the entity into the response DTO
    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getRole());
    }
}
