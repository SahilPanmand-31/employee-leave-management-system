package com.elms.elms.exception;

// Thrown when a leave breaks a business rule
// (bad dates, overlap, not enough balance, wrong status) -> HTTP 400
public class InvalidLeaveRequestException extends RuntimeException {

    public InvalidLeaveRequestException(String message) {
        super(message);
    }
}
