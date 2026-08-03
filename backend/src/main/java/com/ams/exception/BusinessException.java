package com.ams.exception;

/**
 * Exception thrown when a business rule is violated.
 * Returns HTTP 400 Bad Request.
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
