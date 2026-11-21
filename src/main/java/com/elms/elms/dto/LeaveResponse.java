package com.elms.elms.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.elms.elms.entity.LeaveRequest;
import com.elms.elms.entity.LeaveStatus;

// Data we send back for a leave request.
public record LeaveResponse(
        Long id,
        Long employeeId,
        String employeeName,
        String leaveType,
        LocalDate startDate,
        LocalDate endDate,
        long numberOfDays,
        String reason,
        LeaveStatus status) {

    public static LeaveResponse from(LeaveRequest leave) {
        return new LeaveResponse(
                leave.getId(),
                leave.getEmployee().getId(),
                leave.getEmployee().getName(),
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                countDays(leave.getStartDate(), leave.getEndDate()),
                leave.getReason(),
                leave.getStatus());
    }

    // Both start and end date are counted, so 10th to 12th = 3 days
    public static long countDays(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}
