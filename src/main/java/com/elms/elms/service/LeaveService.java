package com.elms.elms.service;

import java.util.List;

import com.elms.elms.entity.LeaveRequest;

public interface LeaveService {

    // Employee applies for leave
    LeaveRequest applyLeave(Long employeeId, LeaveRequest leaveRequest);

    // Get all pending leave requests
    List<LeaveRequest> getPendingLeaves();

    // Approve leave
    LeaveRequest approveLeave(Long leaveId);

    // Reject leave
    LeaveRequest rejectLeave(Long leaveId);

    // Get leave by ID
    LeaveRequest getLeaveById(Long leaveId);

    // Get all leave requests of an employee
    List<LeaveRequest> getLeavesByEmployee(Long employeeId);

    // Get approved leaves of an employee
    List<LeaveRequest> getApprovedLeaves(Long employeeId);

    // Get rejected leaves of an employee
    List<LeaveRequest> getRejectedLeaves(Long employeeId);

    // Cancel leave request
    void cancelLeave(Long leaveId);
}