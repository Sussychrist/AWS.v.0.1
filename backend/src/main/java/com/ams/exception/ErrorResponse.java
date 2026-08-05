package com.ams.exception;

import java.util.List;

/**
 * Error response for validation and business rule failures.
 * Matches 05_REST_API_Specification Section 3.3 exactly.
 */
public record ErrorResponse(
    boolean success,
    String message,
    List<FieldError> errors
) {
    
    /**
     * Nested record for field-level validation errors.
     * 
     * @param field the field name that failed validation
     * @param message the validation error message
     */
    public record FieldError(String field, String message) {
    }

    /**
     * Create an error response with field-level errors.
     *
     * @param message the main error message
     * @param errors list of field errors
     * @return ErrorResponse
     */
    public static ErrorResponse of(String message, List<FieldError> errors) {
        return new ErrorResponse(false, message, errors);
    }

    /**
     * Create a simple error response without field errors.
     *
     * @param message the error message
     * @return ErrorResponse with empty errors list
     */
    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, message, List.of());
    }
}
