package com.elms.elms.entity;

// All possible states of a leave request.
// Stored as text in the database (PENDING / APPROVED / REJECTED / CANCELLED).
public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED
}
