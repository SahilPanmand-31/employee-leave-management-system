package com.elms.elms.dto;

import com.elms.elms.entity.LeaveBalance;

// Leave balance summary of one employee.
public record LeaveBalanceResponse(
        Long employeeId,
        String employeeName,
        int totalLeaves,
        int usedLeaves,
        int remainingLeaves) {

    public static LeaveBalanceResponse from(LeaveBalance balance) {
        return new LeaveBalanceResponse(
                balance.getEmployee().getId(),
                balance.getEmployee().getName(),
                balance.getTotalLeaves(),
                balance.getTotalLeaves() - balance.getRemainingLeaves(),
                balance.getRemainingLeaves());
    }
}
