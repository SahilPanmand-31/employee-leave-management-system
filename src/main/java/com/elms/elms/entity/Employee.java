package com.elms.elms.entity;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "employee")
// Validation lives in the request DTOs (dto package), the entity only maps the table.
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String department;

    // Job designation, e.g. "Java Developer" (not the security role)
    private String role;

    // One Employee -> One Leave Balance
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private LeaveBalance leaveBalance;

    // One Employee -> Many Leave Requests
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<LeaveRequest> leaveRequests;

    // Default Constructor
    public Employee() {
    }

    // Parameterized Constructor
    public Employee(String name, String email, String department, String role) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.role = role;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LeaveBalance getLeaveBalance() {
        return leaveBalance;
    }

    public void setLeaveBalance(LeaveBalance leaveBalance) {
        this.leaveBalance = leaveBalance;
    }

    public List<LeaveRequest> getLeaveRequests() {
        return leaveRequests;
    }

    public void setLeaveRequests(List<LeaveRequest> leaveRequests) {
        this.leaveRequests = leaveRequests;
    }
}