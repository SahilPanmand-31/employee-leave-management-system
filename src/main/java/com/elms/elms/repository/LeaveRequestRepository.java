package com.elms.elms.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveRequest;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // Get all pending leave requests
    List<LeaveRequest> findByStatus(String status);

    // Get all leave requests of an employee
    List<LeaveRequest> findByEmployee(Employee employee);

    // Get leave requests of an employee by status
    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, String status);

    // Find leave by ID
    Optional<LeaveRequest> findById(Long id);

    // Check overlapping leave request
    boolean existsByEmployeeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Employee employee,
            LocalDate endDate,
            LocalDate startDate);

}