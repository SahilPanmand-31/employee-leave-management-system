package com.elms.elms.dto;

import com.elms.elms.entity.Role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Data for creating a login account.
// role is optional and defaults to EMPLOYEE.
public record RegisterRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3 to 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        String password,

        Role role) {
}
