package com.franchise.management.domain.exception;

/**
 * Raised when a domain invariant or business rule is violated (maps to HTTP 400).
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
