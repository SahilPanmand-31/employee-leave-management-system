package com.elms.elms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.elms.elms.entity.LeaveRequest;
import com.elms.elms.service.LeaveService;

import jakarta.validation.Valid;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;


@RestController
@RequestMapping("/leaves")
@Validated
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    // Apply Leave
    @PostMapping("/apply/{employeeId}")
    public ResponseEntity<LeaveRequest> applyLeave(
            @PathVariable Long employeeId,
            @Valid @RequestBody LeaveRequest leaveRequest) {

        LeaveRequest savedLeave = leaveService.applyLeave(employeeId, leaveRequest);

        return new ResponseEntity<>(savedLeave, HttpStatus.CREATED);
    }

    // Get All Pending Leaves
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveRequest>> getPendingLeaves() {

        List<LeaveRequest> pendingLeaves = leaveService.getPendingLeaves();

        return ResponseEntity.ok(pendingLeaves);
    }

    // Approve Leave
    @PutMapping("/approve/{leaveId}")
    public ResponseEntity<LeaveRequest> approveLeave(@PathVariable Long leaveId) {

        LeaveRequest approvedLeave = leaveService.approveLeave(leaveId);

        return ResponseEntity.ok(approvedLeave);
    }

    // Reject Leave
    @PutMapping("/reject/{leaveId}")
    public ResponseEntity<LeaveRequest> rejectLeave(@PathVariable Long leaveId) {

        LeaveRequest rejectedLeave = leaveService.rejectLeave(leaveId);

        return ResponseEntity.ok(rejectedLeave);
    }
}