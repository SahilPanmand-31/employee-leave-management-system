package com.elms.elms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveBalance;
import com.elms.elms.repository.EmployeeRepository;
import com.elms.elms.repository.LeaveBalanceRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    // Create Employee
    @Override
    public Employee createEmployee(Employee employee) {

        // Check duplicate email
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Email already exists.");
        }

        // Save Employee
        Employee savedEmployee = employeeRepository.save(employee);

        // Create Leave Balance
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setEmployee(savedEmployee);
        leaveBalance.setTotalLeaves(20);
        leaveBalance.setRemainingLeaves(20);

        leaveBalanceRepository.save(leaveBalance);

        return savedEmployee;
    }

    // Get Employee By Id
    @Override
    public Employee getEmployeeById(Long id) {

        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee Not Found"));
    }

    // Get All Employees
    @Override
    public List<Employee> getAllEmployees() {

        return employeeRepository.findAll();
    }

    // Update Employee
    @Override
    public Employee updateEmployee(Long id, Employee employee) {

        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee Not Found"));

        existingEmployee.setName(employee.getName());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setDepartment(employee.getDepartment());
        existingEmployee.setRole(employee.getRole());

        return employeeRepository.save(existingEmployee);
    }

    // Delete Employee
    @Override
    public void deleteEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee Not Found"));

        employeeRepository.delete(employee);
    }

    // Search Employee
    @Override
    public List<Employee> searchEmployeeByName(String name) {

        return employeeRepository.findByNameContainingIgnoreCase(name);
    }

    // Get Employees By Department
    @Override
    public List<Employee> getEmployeesByDepartment(String department) {

        return employeeRepository.findByDepartment(department);
    }

}