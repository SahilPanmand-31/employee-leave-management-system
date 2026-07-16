package com.elms.elms.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveBalance;
import com.elms.elms.entity.LeaveRequest;
import com.elms.elms.repository.EmployeeRepository;
import com.elms.elms.repository.LeaveBalanceRepository;
import com.elms.elms.repository.LeaveRequestRepository;

@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    // Apply Leave
    @Override
    public LeaveRequest applyLeave(Long employeeId, LeaveRequest leaveRequest) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee Not Found"));

        if (leaveRequest.getStartDate().isAfter(leaveRequest.getEndDate())) {
            throw new RuntimeException("Start Date cannot be after End Date");
        }

        if (leaveRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Cannot apply leave for past dates");
        }

        boolean alreadyApplied = leaveRequestRepository
                .existsByEmployeeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employee,
                        leaveRequest.getEndDate(),
                        leaveRequest.getStartDate());

        if (alreadyApplied) {
            throw new RuntimeException("Leave already exists for selected dates");
        }

        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus("PENDING");

        return leaveRequestRepository.save(leaveRequest);
    }

    // Pending Leaves
    @Override
    public List<LeaveRequest> getPendingLeaves() {
        return leaveRequestRepository.findByStatus("PENDING");
    }

    // Approve Leave
    @Override
    public LeaveRequest approveLeave(Long leaveId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave Request Not Found"));

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployee(leave.getEmployee())
                .orElseThrow(() -> new RuntimeException("Leave Balance Not Found"));

        long leaveDays = ChronoUnit.DAYS.between(
                leave.getStartDate(),
                leave.getEndDate()) + 1;

        if (balance.getRemainingLeaves() < leaveDays) {
            throw new RuntimeException("Insufficient Leave Balance");
        }

        balance.setRemainingLeaves(
                balance.getRemainingLeaves() - (int) leaveDays);

        leaveBalanceRepository.save(balance);

        leave.setStatus("APPROVED");

        return leaveRequestRepository.save(leave);
    }

    // Reject Leave
    @Override
    public LeaveRequest rejectLeave(Long leaveId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave Request Not Found"));

        leave.setStatus("REJECTED");

        return leaveRequestRepository.save(leave);
    }

    // Get Leave By Id
    @Override
    public LeaveRequest getLeaveById(Long leaveId) {

        return leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave Request Not Found"));
    }

    // Get Employee Leave History
    @Override
    public List<LeaveRequest> getLeavesByEmployee(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee Not Found"));

        return leaveRequestRepository.findByEmployee(employee);
    }

    // Approved Leaves
    @Override
    public List<LeaveRequest> getApprovedLeaves(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee Not Found"));

        return leaveRequestRepository.findByEmployeeAndStatus(employee, "APPROVED");
    }

    // Rejected Leaves
    @Override
    public List<LeaveRequest> getRejectedLeaves(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee Not Found"));

        return leaveRequestRepository.findByEmployeeAndStatus(employee, "REJECTED");
    }

    // Cancel Leave
    @Override
    public void cancelLeave(Long leaveId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave Request Not Found"));

        if (!leave.getStatus().equalsIgnoreCase("PENDING")) {
            throw new RuntimeException("Only Pending Leave can be cancelled");
        }

        leaveRequestRepository.delete(leave);
    }
}