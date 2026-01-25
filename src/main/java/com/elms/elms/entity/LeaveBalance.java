package com.elms.elms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_balance")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalLeaves;

    private int remainingLeaves;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    // Constructors
    public LeaveBalance() {
    }

    public LeaveBalance(int totalLeaves, int remainingLeaves, Employee employee) {
        this.totalLeaves = totalLeaves;
        this.remainingLeaves = remainingLeaves;
        this.employee = employee;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getTotalLeaves() {
        return totalLeaves;
    }

    public void setTotalLeaves(int totalLeaves) {
        this.totalLeaves = totalLeaves;
    }

    public int getRemainingLeaves() {
        return remainingLeaves;
    }

    public void setRemainingLeaves(int remainingLeaves) {
        this.remainingLeaves = remainingLeaves;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
