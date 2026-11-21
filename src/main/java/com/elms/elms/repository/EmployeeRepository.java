package com.elms.elms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.elms.elms.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Check if email already exists (used on create)
    boolean existsByEmail(String email);

    // Check if ANOTHER employee already uses this email (used on update)
    boolean existsByEmailAndIdNot(String email, Long id);

    // Search by part of the name AND part of the department, ignoring case.
    // Passing "" for a parameter means "match everything" for that field.
    List<Employee> findByNameContainingIgnoreCaseAndDepartmentContainingIgnoreCase(String name, String department);
}
