package com.mindex.challenge.service.util;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Static utility class for common validation operations.
 * Provides fast, pure validation logic without dependencies.
 * Used across service layers for consistent validation behavior.
 */
public final class ValidationUtils {

    /**
     * Minimum salary allowed for compensation records.
     * Set to 0 to allow zero salaries (interns, volunteers, etc.)
     */
    public static final BigDecimal MIN_SALARY = BigDecimal.ZERO;

    /**
     * Maximum salary allowed for compensation records.
     * Set to a reasonable upper limit to prevent data entry errors.
     */
    public static final BigDecimal MAX_SALARY = new BigDecimal("10000000.00"); // 10 million

    /**
     * Maximum number of years in the future for effective dates.
     * Prevents unrealistic future dates while allowing reasonable planning.
     */
    public static final int MAX_FUTURE_YEARS = 5;

    /**
     * Maximum number of years in the past for effective dates.
     * Prevents ancient dates while allowing historical records.
     */
    public static final int MAX_PAST_YEARS = 50;

    // Private constructor to prevent instantiation
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates that a string is not null, empty, or only whitespace.
     *
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws RuntimeException if validation fails
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (value == null) {
            throw new RuntimeException(fieldName + " cannot be null");
        }
        if (value.trim().isEmpty()) {
            throw new RuntimeException(fieldName + " cannot be empty or only whitespace");
        }
    }

    /**
     * Validates that a salary is within acceptable range.
     *
     * @param salary the salary to validate
     * @throws RuntimeException if validation fails
     */
    public static void validateSalaryRange(BigDecimal salary) {
        if (salary == null) {
            throw new RuntimeException("Salary cannot be null");
        }

        if (salary.compareTo(MIN_SALARY) < 0) {
            throw new RuntimeException("Salary cannot be negative: " + salary);
        }

        if (salary.compareTo(MAX_SALARY) > 0) {
            throw new RuntimeException("Salary exceeds maximum allowed: " + salary +
                    " (max: " + MAX_SALARY + ")");
        }

        // Check for reasonable decimal places (cents only)
        if (salary.scale() > 2) {
            throw new RuntimeException("Salary cannot have more than 2 decimal places: " + salary);
        }
    }

    /**
     * Validates that an effective date is reasonable (not too far in past or future).
     *
     * @param effectiveDate the date to validate (can be null - will default to today)
     * @throws RuntimeException if validation fails
     */
    public static void validateEffectiveDate(LocalDate effectiveDate) {
        // Null is allowed - service layer will default to today
        if (effectiveDate == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate maxPastDate = today.minusYears(MAX_PAST_YEARS);
        LocalDate maxFutureDate = today.plusYears(MAX_FUTURE_YEARS);

        if (effectiveDate.isBefore(maxPastDate)) {
            throw new RuntimeException("Effective date is too far in the past: " + effectiveDate +
                    " (earliest allowed: " + maxPastDate + ")");
        }

        if (effectiveDate.isAfter(maxFutureDate)) {
            throw new RuntimeException("Effective date is too far in the future: " + effectiveDate +
                    " (latest allowed: " + maxFutureDate + ")");
        }
    }

    /**
     * Validates currency code format (3-letter ISO code).
     *
     * @param currency the currency code to validate (can be null - will default to USD)
     * @throws RuntimeException if validation fails
     */
    public static void validateCurrencyCode(String currency) {
        // Null is allowed - service layer will default to USD
        if (currency == null || currency.trim().isEmpty()) {
            return;
        }

        String trimmedCurrency = currency.trim().toUpperCase();

        // Basic format validation (3 letters)
        if (!trimmedCurrency.matches("^[A-Z]{3}$")) {
            throw new RuntimeException("Invalid currency code format: " + currency +
                    " (must be 3 letters, e.g., USD, EUR, GBP)");
        }
    }

    /**
     * Validates employee ID format (basic UUID-like format check).
     *
     * @param employeeId the employee ID to validate
     * @throws RuntimeException if validation fails
     */
    public static void validateEmployeeIdFormat(String employeeId) {
        validateNotEmpty(employeeId, "Employee ID");

        // Basic UUID format check (not strict validation)
        if (!employeeId.matches("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")) {
            throw new RuntimeException("Invalid employee ID format: " + employeeId +
                    " (must be UUID format)");
        }
    }
}