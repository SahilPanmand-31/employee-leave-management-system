package com.elms.elms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.elms.elms.entity.LeaveBalance;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    LeaveBalance findByEmployeeId(Long employeeId);
}
