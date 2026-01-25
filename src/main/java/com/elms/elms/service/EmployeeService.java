package com.elms.elms.service;

import java.util.List;
import com.elms.elms.entity.Employee;

public interface EmployeeService {

    Employee createEmployee(Employee employee);

    Employee getEmployeeById(Long id);

    List<Employee> getAllEmployees();
}
