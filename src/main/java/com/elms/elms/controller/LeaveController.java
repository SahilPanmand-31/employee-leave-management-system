package com.elms.elms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.elms.elms.dto.LeaveApplyRequest;
import com.elms.elms.dto.LeaveResponse;
import com.elms.elms.service.LeaveService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // Apply Leave
    @PostMapping("/apply/{employeeId}")
    public ResponseEntity<LeaveResponse> applyLeave(
            @PathVariable Long employeeId,
            @Valid @RequestBody LeaveApplyRequest request) {

        LeaveResponse savedLeave = leaveService.applyLeave(employeeId, request);

        return new ResponseEntity<>(savedLeave, HttpStatus.CREATED);
    }

    // Get All Pending Leaves (MANAGER only)
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponse>> getPendingLeaves() {

        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    // Leave History of an Employee (newest first)
    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<LeaveResponse>> getLeaveHistory(@PathVariable Long employeeId) {

        return ResponseEntity.ok(leaveService.getLeaveHistory(employeeId));
    }

    // Get Leave By Id
    @GetMapping("/{leaveId}")
    public ResponseEntity<LeaveResponse> getLeave(@PathVariable Long leaveId) {

        return ResponseEntity.ok(leaveService.getLeaveById(leaveId));
    }

    // Approve Leave (MANAGER only)
    @PutMapping("/approve/{leaveId}")
    public ResponseEntity<LeaveResponse> approveLeave(@PathVariable Long leaveId) {

        return ResponseEntity.ok(leaveService.approveLeave(leaveId));
    }

    // Reject Leave (MANAGER only)
    @PutMapping("/reject/{leaveId}")
    public ResponseEntity<LeaveResponse> rejectLeave(@PathVariable Long leaveId) {

        return ResponseEntity.ok(leaveService.rejectLeave(leaveId));
    }

    // Cancel a Pending Leave
    @PutMapping("/cancel/{leaveId}")
    public ResponseEntity<LeaveResponse> cancelLeave(@PathVariable Long leaveId) {

        return ResponseEntity.ok(leaveService.cancelLeave(leaveId));
    }
}
