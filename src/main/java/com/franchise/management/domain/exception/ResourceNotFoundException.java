package com.franchise.management.domain.exception;

/**
 * Base type for "entity does not exist" errors (maps to HTTP 404).
 */
public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }
}
