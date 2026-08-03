package com.ams.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Returns HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
