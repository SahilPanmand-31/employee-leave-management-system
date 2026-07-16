package com.elms.elms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.elms.elms.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find employee by email
    Employee findByEmail(String email);

    // Find employee by exact name
    Employee findByName(String name);

    // Search employees by name
    List<Employee> findByNameContainingIgnoreCase(String name);

    // Find all employees in a department
    List<Employee> findByDepartment(String department);

    // Check if email already exists
    boolean existsByEmail(String email);

}