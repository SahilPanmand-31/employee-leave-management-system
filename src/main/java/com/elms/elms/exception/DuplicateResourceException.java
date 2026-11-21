package com.elms.elms.exception;

// Thrown when a unique value already exists (email, username) -> HTTP 409
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
