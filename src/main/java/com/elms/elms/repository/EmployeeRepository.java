package com.elms.elms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.elms.elms.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Employee findByEmail(String email);
}
