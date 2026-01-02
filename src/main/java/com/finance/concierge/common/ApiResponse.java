package com.finance.concierge.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Common API Response wrapper for all endpoints
 * Provides consistent response structure across the application
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private String message;

    private T data;

    private String error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String path;

    private Integer statusCode;

    /**
     * Create a successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Create a successful response with data and message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .build();
    }

    /**
     * Create an error response with status code
     */
    public static <T> ApiResponse<T> error(String error, Integer statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .statusCode(statusCode)
                .build();
    }
}

