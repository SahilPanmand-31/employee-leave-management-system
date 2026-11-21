package com.elms.elms.exception;

// Thrown when a record (employee, leave, balance) does not exist -> HTTP 404
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
