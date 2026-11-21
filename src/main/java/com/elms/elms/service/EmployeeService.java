package com.elms.elms.service;

import java.util.List;

import com.elms.elms.dto.EmployeeRequest;
import com.elms.elms.dto.EmployeeResponse;
import com.elms.elms.dto.LeaveBalanceResponse;

public interface EmployeeService {

    // Create Employee (also creates a leave balance of 20 days)
    EmployeeResponse createEmployee(EmployeeRequest request);

    // Get Employee By Id
    EmployeeResponse getEmployeeById(Long id);

    // Get All Employees
    List<EmployeeResponse> getAllEmployees();

    // Update Employee
    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    // Delete Employee (its leave balance and leave requests are removed too)
    void deleteEmployee(Long id);

    // Search Employees by name and/or department
    List<EmployeeResponse> searchEmployees(String name, String department);

    // Leave balance of an employee
    LeaveBalanceResponse getLeaveBalance(Long employeeId);
}
