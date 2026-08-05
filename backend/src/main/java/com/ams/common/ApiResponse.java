package com.ams.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Standard API response wrapper.
 * Matches 05_REST_API_Specification Section 3.2.
 * 
 * @param <T> The type of the response data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data) {

    /**
     * Create a success response with data.
     *
     * @param message the success message
     * @param data the response data payload
     * @param <T> the type of the data
     * @return success ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Create a success response with no data.
     *
     * @param message the success message
     * @return success ApiResponse with null data
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /**
     * Create an error response.
     *
     * @param message the error message
     * @return error ApiResponse
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
