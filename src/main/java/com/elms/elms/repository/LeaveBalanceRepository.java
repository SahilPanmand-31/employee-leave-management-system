package com.elms.elms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.elms.elms.entity.Employee;
import com.elms.elms.entity.LeaveBalance;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployee(Employee employee);

    boolean existsByEmployee(Employee employee);

    void deleteByEmployee(Employee employee);

}