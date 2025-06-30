package com.mindex.challenge.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;

/**
 * Global exception handler using Spring's RFC 7807 Problem Details.
 * Provides standardized error responses following web standards.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors - HTTP 400 Bad Request
     */
    @ExceptionHandler(CompensationValidationException.class)
    public ResponseEntity<ProblemDetail> handleCompensationValidation(
            CompensationValidationException ex, WebRequest request) {

        LOG.warn("Compensation validation error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Compensation Validation Error");
        problemDetail.setType(URI.create("/errors/validation"));
        problemDetail.setProperty("code", "VALIDATION_ERROR");

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle employee not found errors - HTTP 404 Not Found
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEmployeeNotFound(
            EmployeeNotFoundException ex, WebRequest request) {

        LOG.warn("Employee not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Employee Not Found");
        problemDetail.setType(URI.create("/errors/not-found"));
        problemDetail.setProperty("code", "EMPLOYEE_NOT_FOUND");
        problemDetail.setProperty("employeeId", ex.getEmployeeId());

        return ResponseEntity.notFound().build();
    }

    /**
     * Handle general IllegalArgumentException - HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        LOG.warn("Illegal argument error: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create("/errors/bad-request"));
        problemDetail.setProperty("code", "BAD_REQUEST");

        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Handle hierarchy processing errors - HTTP 422 Unprocessable Entity
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        String message = ex.getMessage();

        // Check for specific business rule violations
        if (message != null && (message.contains("hierarchy too large") || message.contains("processing timeout"))) {
            LOG.warn("Business rule violation: {}", message);

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    message
            );
            problemDetail.setTitle("Business Rule Violation");
            problemDetail.setType(URI.create("/errors/business-rule"));
            problemDetail.setProperty("code", "BUSINESS_RULE_VIOLATION");

            return ResponseEntity.unprocessableEntity().body(problemDetail);
        }

        // General runtime exceptions - HTTP 500 Internal Server Error
        LOG.error("Unexpected runtime error", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("/errors/internal"));
        problemDetail.setProperty("code", "INTERNAL_ERROR");

        return ResponseEntity.internalServerError().body(problemDetail);
    }

    /**
     * Catch-all exception handler - HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(
            Exception ex, WebRequest request) {

        LOG.error("Unexpected error occurred", ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("/errors/internal"));
        problemDetail.setProperty("code", "INTERNAL_ERROR");

        return ResponseEntity.internalServerError().body(problemDetail);
    }
}