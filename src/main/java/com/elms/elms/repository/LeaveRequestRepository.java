package com.elms.elms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveRequest;
import com.elms.elms.entity.LeaveStatus;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // All leave requests with a given status (e.g. PENDING)
    List<LeaveRequest> findByStatus(LeaveStatus status);

    // Full leave history of an employee, newest first
    List<LeaveRequest> findByEmployeeOrderByStartDateDesc(Employee employee);

    // Overlap check: two date ranges overlap when
    // existing.start <= new.end AND existing.end >= new.start.
    // Only PENDING and APPROVED leaves count; rejected/cancelled dates can be applied for again.
    @Query("""
            SELECT COUNT(l) > 0 FROM LeaveRequest l
            WHERE l.employee.id = :employeeId
              AND l.status IN (com.elms.elms.entity.LeaveStatus.PENDING, com.elms.elms.entity.LeaveStatus.APPROVED)
              AND l.startDate <= :endDate
              AND l.endDate >= :startDate
            """)
    boolean existsOverlappingLeave(@Param("employeeId") Long employeeId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
}
