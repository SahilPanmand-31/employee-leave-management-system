package com.elms.elms.service;

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

    @Override
    public LeaveRequest applyLeave(Long employeeId, LeaveRequest leaveRequest) {

        Employee employee = employeeRepository.findById(employeeId).orElse(null);

        if (employee == null) {
            return null;
        }

        leaveRequest.setEmployee(employee);
        leaveRequest.setStatus("PENDING");

        return leaveRequestRepository.save(leaveRequest);
    }

    @Override
    public List<LeaveRequest> getPendingLeaves() {
        return leaveRequestRepository.findByStatus("PENDING");
    }

    @Override
    public LeaveRequest approveLeave(Long leaveId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId).orElse(null);

        if (leave == null) {
            return null;
        }

        leave.setStatus("APPROVED");

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeId(leave.getEmployee().getId());

        if (balance != null) {
            balance.setRemainingLeaves(balance.getRemainingLeaves() - 1);
            leaveBalanceRepository.save(balance);
        }

        return leaveRequestRepository.save(leave);
    }

    @Override
    public LeaveRequest rejectLeave(Long leaveId) {

        LeaveRequest leave = leaveRequestRepository.findById(leaveId).orElse(null);

        if (leave == null) {
            return null;
        }

        leave.setStatus("REJECTED");
        return leaveRequestRepository.save(leave);
    }
}
