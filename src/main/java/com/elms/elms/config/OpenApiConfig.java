package com.elms.elms.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

// Swagger UI: http://localhost:8080/swagger-ui.html
// Click "Authorize", paste the token from /auth/login, and every request sends it.
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Employee Leave Management System API",
                version = "1.0",
                description = "Manage employees, apply for leave and approve/reject leave with JWT security"),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
