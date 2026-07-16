package com.elms.elms.service;

import java.util.List;

import com.elms.elms.entity.Employee;

public interface EmployeeService {

    // Create Employee
    Employee createEmployee(Employee employee);

    // Get Employee By Id
    Employee getEmployeeById(Long id);

    // Get All Employees
    List<Employee> getAllEmployees();

    // Update Employee
    Employee updateEmployee(Long id, Employee employee);

    // Delete Employee
    void deleteEmployee(Long id);

    // Search Employee By Name
    List<Employee> searchEmployeeByName(String name);

    // Get Employees By Department
    List<Employee> getEmployeesByDepartment(String department);

}