package com.mindex.challenge.exception;

public class EmployeeNotFoundException extends RuntimeException {

    private final String employeeId;

    public EmployeeNotFoundException(String employeeId) {
        super("Employee not found: " + employeeId);
        this.employeeId = employeeId;
    }

    public EmployeeNotFoundException(String employeeId, String message) {
        super(message);
        this.employeeId = employeeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }
}