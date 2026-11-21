package com.elms.elms.service;

import java.util.List;

import com.elms.elms.dto.LeaveApplyRequest;
import com.elms.elms.dto.LeaveResponse;

public interface LeaveService {

    // Employee applies for leave
    LeaveResponse applyLeave(Long employeeId, LeaveApplyRequest request);

    // Get all pending leave requests
    List<LeaveResponse> getPendingLeaves();

    // Approve leave (deducts the days from the leave balance)
    LeaveResponse approveLeave(Long leaveId);

    // Reject leave
    LeaveResponse rejectLeave(Long leaveId);

    // Get leave by ID
    LeaveResponse getLeaveById(Long leaveId);

    // Leave history of an employee
    List<LeaveResponse> getLeaveHistory(Long employeeId);

    // Cancel a pending leave request
    LeaveResponse cancelLeave(Long leaveId);
}
