package com.elms.elms.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Data the client sends to apply for leave.
// Date order (end >= start) and overlap checks are business rules, so they live in LeaveServiceImpl.
public record LeaveApplyRequest(

        @NotBlank(message = "Leave Type is required")
        String leaveType,

        @NotNull(message = "Start Date is required")
        @FutureOrPresent(message = "Start Date cannot be in the past")
        LocalDate startDate,

        @NotNull(message = "End Date is required")
        @FutureOrPresent(message = "End Date cannot be in the past")
        LocalDate endDate,

        @NotBlank(message = "Reason is required")
        @Size(max = 255, message = "Reason must be at most 255 characters")
        String reason) {
}
