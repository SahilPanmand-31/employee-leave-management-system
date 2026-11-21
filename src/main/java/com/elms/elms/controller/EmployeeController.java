package com.elms.elms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.elms.elms.dto.EmployeeRequest;
import com.elms.elms.dto.EmployeeResponse;
import com.elms.elms.dto.LeaveBalanceResponse;
import com.elms.elms.service.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Create Employee
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse savedEmployee = employeeService.createEmployee(request);

        return new ResponseEntity<>(savedEmployee, HttpStatus.CREATED);
    }

    // Get All Employees
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {

        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // Search Employees, e.g. /employees/search?name=sah&department=IT (both optional)
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponse>> searchEmployees(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department) {

        return ResponseEntity.ok(employeeService.searchEmployees(name, department));
    }

    // Get Employee By Id
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {

        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // Update Employee
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {

        return ResponseEntity.ok(employeeService.updateEmployee(id, request));
    }

    // Delete Employee
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {

        employeeService.deleteEmployee(id);

        return ResponseEntity.noContent().build();
    }

    // Leave Balance of an Employee
    @GetMapping("/{id}/leave-balance")
    public ResponseEntity<LeaveBalanceResponse> getLeaveBalance(@PathVariable Long id) {

        return ResponseEntity.ok(employeeService.getLeaveBalance(id));
    }
}
