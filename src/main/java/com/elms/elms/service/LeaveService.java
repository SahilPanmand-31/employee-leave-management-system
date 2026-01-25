package com.elms.elms.service;

import java.util.List;
import com.elms.elms.entity.LeaveRequest;

public interface LeaveService {

    LeaveRequest applyLeave(Long employeeId, LeaveRequest leaveRequest);

    List<LeaveRequest> getPendingLeaves();

    LeaveRequest approveLeave(Long leaveId);

    LeaveRequest rejectLeave(Long leaveId);
}
