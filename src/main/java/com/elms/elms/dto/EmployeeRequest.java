package com.elms.elms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Data the client sends to create or update an employee.
// @Valid in the controller triggers these checks before the service is called.
public record EmployeeRequest(

        @NotBlank(message = "Employee name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        String email,

        @NotBlank(message = "Department is required")
        String department,

        // Job designation, e.g. "Java Developer"
        @NotBlank(message = "Role is required")
        String role) {
}
