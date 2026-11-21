package com.elms.elms.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elms.elms.dto.LeaveApplyRequest;
import com.elms.elms.dto.LeaveResponse;
import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveBalance;
import com.elms.elms.entity.LeaveRequest;
import com.elms.elms.entity.LeaveStatus;
import com.elms.elms.exception.InvalidLeaveRequestException;
import com.elms.elms.exception.ResourceNotFoundException;
import com.elms.elms.repository.EmployeeRepository;
import com.elms.elms.repository.LeaveBalanceRepository;
import com.elms.elms.repository.LeaveRequestRepository;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public LeaveServiceImpl(LeaveRequestRepository leaveRequestRepository,
                            EmployeeRepository employeeRepository,
                            LeaveBalanceRepository leaveBalanceRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    // Apply Leave
    @Override
    @Transactional
    public LeaveResponse applyLeave(Long employeeId, LeaveApplyRequest request) {

        Employee employee = findEmployee(employeeId);

        // Rule 1: end date must be on or after start date
        if (request.endDate().isBefore(request.startDate())) {
            throw new InvalidLeaveRequestException("End Date cannot be before Start Date");
        }

        // Rule 2: no leave in the past (also checked by @FutureOrPresent on the DTO)
        if (request.startDate().isBefore(LocalDate.now())) {
            throw new InvalidLeaveRequestException("Cannot apply leave for past dates");
        }

        // Rule 3: no overlap with another pending/approved leave of the same employee
        if (leaveRequestRepository.existsOverlappingLeave(employeeId, request.startDate(), request.endDate())) {
            throw new InvalidLeaveRequestException("Leave already exists for selected dates");
        }

        // Rule 4: the employee must have enough leave days left
        long requestedDays = LeaveResponse.countDays(request.startDate(), request.endDate());
        if (requestedDays > findBalance(employee).getRemainingLeaves()) {
            throw new InvalidLeaveRequestException("Insufficient Leave Balance");
        }

        LeaveRequest leave = new LeaveRequest(
                request.leaveType(),
                request.startDate(),
                request.endDate(),
                request.reason(),
                LeaveStatus.PENDING,
                employee);

        return LeaveResponse.from(leaveRequestRepository.save(leave));
    }

    // Pending Leaves
    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeaves() {
        return leaveRequestRepository.findByStatus(LeaveStatus.PENDING).stream()
                .map(LeaveResponse::from)
                .toList();
    }

    // Approve Leave
    @Override
    @Transactional
    public LeaveResponse approveLeave(Long leaveId) {

        LeaveRequest leave = findLeave(leaveId);
        ensurePending(leave);

        LeaveBalance balance = findBalance(leave.getEmployee());

        long leaveDays = LeaveResponse.countDays(leave.getStartDate(), leave.getEndDate());

        if (balance.getRemainingLeaves() < leaveDays) {
            throw new InvalidLeaveRequestException("Insufficient Leave Balance");
        }

        // Deduct the approved days from the balance
        balance.setRemainingLeaves(balance.getRemainingLeaves() - (int) leaveDays);
        leaveBalanceRepository.save(balance);

        leave.setStatus(LeaveStatus.APPROVED);

        return LeaveResponse.from(leaveRequestRepository.save(leave));
    }

    // Reject Leave
    @Override
    @Transactional
    public LeaveResponse rejectLeave(Long leaveId) {

        LeaveRequest leave = findLeave(leaveId);
        ensurePending(leave);

        leave.setStatus(LeaveStatus.REJECTED);

        return LeaveResponse.from(leaveRequestRepository.save(leave));
    }

    // Get Leave By Id
    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long leaveId) {
        return LeaveResponse.from(findLeave(leaveId));
    }

    // Leave History of an Employee
    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaveHistory(Long employeeId) {

        Employee employee = findEmployee(employeeId);

        return leaveRequestRepository.findByEmployeeOrderByStartDateDesc(employee).stream()
                .map(LeaveResponse::from)
                .toList();
    }

    // Cancel Leave
    @Override
    @Transactional
    public LeaveResponse cancelLeave(Long leaveId) {

        LeaveRequest leave = findLeave(leaveId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveRequestException("Only Pending Leave can be cancelled");
        }

        // We keep the record for history and only change its status
        leave.setStatus(LeaveStatus.CANCELLED);

        return LeaveResponse.from(leaveRequestRepository.save(leave));
    }

    // ---------- helper methods ----------

    // A leave can be approved or rejected only once
    private void ensurePending(LeaveRequest leave) {
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new InvalidLeaveRequestException(
                    "Leave is already " + leave.getStatus() + ", only PENDING leave can be processed");
        }
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));
    }

    private LeaveRequest findLeave(Long leaveId) {
        return leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + leaveId));
    }

    private LeaveBalance findBalance(Employee employee) {
        return leaveBalanceRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave balance not found for employee id: " + employee.getId()));
    }
}
