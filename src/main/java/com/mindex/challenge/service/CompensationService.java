package com.mindex.challenge.service;

import com.mindex.challenge.data.Compensation;

import java.util.Optional;

/**
 * Service interface for Compensation operations.
 * Following the pattern as EmployeeService for consistency.
 */
public interface CompensationService {

    /**
     * Create a new compensation record for an employee.
     * Task 2 requirement: "create Compensation information"
     *
     * @param compensation the compensation to create
     * @return the created compensation with generated ID
     */
    Compensation create(Compensation compensation);

    /**
     * Read the current active compensation for an employee.
     * Task 2 requirement: "read Compensation information for a specific Employee"
     *
     * @param employeeId the employee ID to get compensation for
     * @return Optional containing the active compensation, empty if not found
     */
    Optional<Compensation> read(String employeeId);
}