package com.elms.elms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.elms.elms.entity.LeaveRequest;
import com.elms.elms.service.LeaveService;

@RestController
@RequestMapping("/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @PostMapping("/apply/{employeeId}")
    public LeaveRequest applyLeave(@PathVariable Long employeeId,
                                   @RequestBody LeaveRequest leaveRequest) {
        return leaveService.applyLeave(employeeId, leaveRequest);
    }

    @GetMapping("/pending")
    public List<LeaveRequest> getPendingLeaves() {
        return leaveService.getPendingLeaves();
    }

    @PutMapping("/approve/{leaveId}")
    public LeaveRequest approveLeave(@PathVariable Long leaveId) {
        return leaveService.approveLeave(leaveId);
    }

    @PutMapping("/reject/{leaveId}")
    public LeaveRequest rejectLeave(@PathVariable Long leaveId) {
        return leaveService.rejectLeave(leaveId);
    }
}
