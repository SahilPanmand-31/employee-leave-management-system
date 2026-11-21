package com.elms.elms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.elms.elms.dto.EmployeeRequest;
import com.elms.elms.dto.EmployeeResponse;
import com.elms.elms.dto.LeaveBalanceResponse;
import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveBalance;
import com.elms.elms.exception.DuplicateResourceException;
import com.elms.elms.exception.ResourceNotFoundException;
import com.elms.elms.repository.EmployeeRepository;
import com.elms.elms.repository.LeaveBalanceRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    // Every new employee starts with 20 leave days
    public static final int DEFAULT_TOTAL_LEAVES = 20;

    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    // Constructor injection: dependencies are final and easy to mock in unit tests
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               LeaveBalanceRepository leaveBalanceRepository) {
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    // Create Employee
    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {

        // Check duplicate email
        if (employeeRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        // Save Employee
        Employee employee = new Employee(request.name(), request.email(), request.department(), request.role());
        Employee savedEmployee = employeeRepository.save(employee);

        // Create Leave Balance automatically
        LeaveBalance leaveBalance = new LeaveBalance(DEFAULT_TOTAL_LEAVES, DEFAULT_TOTAL_LEAVES, savedEmployee);
        leaveBalanceRepository.save(leaveBalance);

        return EmployeeResponse.from(savedEmployee);
    }

    // Get Employee By Id
    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return EmployeeResponse.from(findEmployee(id));
    }

    // Get All Employees
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    // Update Employee
    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {

        Employee existingEmployee = findEmployee(id);

        // Another employee may already use the new email
        if (employeeRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        existingEmployee.setName(request.name());
        existingEmployee.setEmail(request.email());
        existingEmployee.setDepartment(request.department());
        existingEmployee.setRole(request.role());

        return EmployeeResponse.from(employeeRepository.save(existingEmployee));
    }

    // Delete Employee
    @Override
    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = findEmployee(id);

        // cascade = ALL on Employee removes the leave balance and leave requests as well
        employeeRepository.delete(employee);
    }

    // Search Employees
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> searchEmployees(String name, String department) {

        // null means "not filtered", so we search with "", which matches everything
        String nameFilter = (name == null) ? "" : name.trim();
        String departmentFilter = (department == null) ? "" : department.trim();

        return employeeRepository
                .findByNameContainingIgnoreCaseAndDepartmentContainingIgnoreCase(nameFilter, departmentFilter)
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    // Leave Balance of an Employee
    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getLeaveBalance(Long employeeId) {

        Employee employee = findEmployee(employeeId);

        LeaveBalance balance = leaveBalanceRepository.findByEmployee(employee)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave balance not found for employee id: " + employeeId));

        return LeaveBalanceResponse.from(balance);
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }
}
