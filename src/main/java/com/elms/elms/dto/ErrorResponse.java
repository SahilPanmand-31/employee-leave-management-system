package com.elms.elms.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

// The single JSON error format returned by every failing request, for example:
// { "timestamp": "...", "status": 404, "error": "Not Found",
//   "message": "Employee not found with id: 5", "path": "/employees/5" }
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors) {

    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }
}
