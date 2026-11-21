package com.elms.elms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_balance")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int totalLeaves;

    @Column(nullable = false)
    private int remainingLeaves;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    // Default Constructor
    public LeaveBalance() {
    }

    // Parameterized Constructor
    public LeaveBalance(int totalLeaves, int remainingLeaves, Employee employee) {
        this.totalLeaves = totalLeaves;
        this.remainingLeaves = remainingLeaves;
        this.employee = employee;
    }

    // Getters and Setters

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
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