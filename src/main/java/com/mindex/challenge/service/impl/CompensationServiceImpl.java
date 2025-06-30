package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.exception.CompensationValidationException;
import com.mindex.challenge.exception.EmployeeNotFoundException;
import com.mindex.challenge.service.CompensationService;
import com.mindex.challenge.service.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class CompensationServiceImpl implements CompensationService {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationServiceImpl.class);
    public static final String DEFAULT_CURRENCY = "USD";

    private final CompensationRepository compensationRepository;
    private final EmployeeRepository employeeRepository;

    public CompensationServiceImpl(CompensationRepository compensationRepository,
                                   EmployeeRepository employeeRepository) {
        this.compensationRepository = compensationRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Creates a new compensation record for an employee. The method validates the input, checks the existence
     * of the associated employee, sets required fields, deactivates any existing active compensation for the
     * employee, and persists the new*/
    @Override
    public Compensation create(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        validateAndCheckEmployee(compensation);
        prepareCompensationForSave(compensation);

        Compensation savedCompensation = compensationRepository.save(compensation);
        logSuccessfulCreation(savedCompensation);

        return savedCompensation;
    }

    /**
     * Reads the compensation details for the specified employee identifier.
     * If the employee ID format is invalid or the employee is not found, an empty result is returned.
     * Otherwise, retrieves the active compensation data for the given employee.
     *
     * @param employeeId the unique identifier of the employee whose compensation is to be read
     * @return an Optional containing the active compensation details if found, or an empty Optional if
     * no active compensation exists or the employee ID is invalid/not found
     */
    @Override
    public Optional<Compensation> read(String employeeId) {
        LOG.debug("Reading compensation for employee [{}]", employeeId);

        if (!isValidEmployeeIdFormat(employeeId)) {
            LOG.info("Invalid employee ID format [{}] - returning empty result", employeeId);
            return Optional.empty();
        }

        if (findEmployee(employeeId).isEmpty()) {
            LOG.info("No employee found for employeeId [{}] - returning empty result", employeeId);
            return Optional.empty();
        }

        return findActiveCompensation(employeeId);
    }

    /**
     * Validates the provided Compensation object and performs necessary checks on the associated employee.
     * This method ensures that the provided Compensation object is valid, the associated employee exists,
     * and any existing active compensation for the employee is deactivated.
     *
     * @param compensation the Compensation object to validate and process
     */
    private void validateAndCheckEmployee(Compensation compensation) {
        validateCompensation(compensation);

        String employeeId = compensation.getEmployeeId();
        verifyEmployeeExists(employeeId);
        deactivateExistingCompensation(employeeId);
    }

    /**
     * Validates the provided Compensation object. This method ensures that the provided
     * Compensation object meets the required validation rules and throws a
     * CompensationValidationException if validation fails.
     *
     * @param compensation the Compensation object to validate
     */
    private void validateCompensation(Compensation compensation) {
        try {
            validateCompensationForCreation(compensation);
        } catch (RuntimeException e) {
            LOG.warn("Compensation validation failed: {}", e.getMessage());
            throw new CompensationValidationException("Validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies if an employee with the specified employee ID exists in the system.
     * If the employee is not found, an EmployeeNotFoundException is thrown.
     *
     * @param employeeId the unique identifier of the employee to verify
     * @throws EmployeeNotFoundException if no employee is found with the specified employee ID
     */
    private void verifyEmployeeExists(String employeeId) {
        findEmployee(employeeId)
                .orElseThrow(() -> {
                    LOG.warn("Employee not found for ID [{}] in database", employeeId);
                    return new EmployeeNotFoundException(employeeId);
                });
    }

    /**
     * Prepares a Compensation object for saving by setting default values and ensuring required fields are populated.
     * This method assigns a unique compensation ID, sets the effective date to the current date if not provided,
     * assigns the default currency if none is specified, marks the compensation as active, and clears the end date.
     *
     * @param compensation the Compensation object to be prepared for saving
     */
    private void prepareCompensationForSave(Compensation compensation) {
        compensation.setCompensationId(UUID.randomUUID().toString());
        compensation.setEffectiveDate(
                Optional.ofNullable(compensation.getEffectiveDate()).orElse(LocalDate.now())
        );
        compensation.setCurrency(
                Optional.ofNullable(compensation.getCurrency())
                        .filter(c -> !c.trim().isEmpty())
                        .orElse(DEFAULT_CURRENCY)
        );
        compensation.setActive(true);
        compensation.setEndDate(null);
    }

    /**
     * Finds and returns the active compensation record for the specified employee.
     * This method queries the database for the compensation associated with the given
     * employee identifier that is marked as active.
     *
     * @param employeeId the unique identifier of the employee whose active compensation
     *                   record is to be retrieved
     * @return an Optional containing the active Compensation object if found; otherwise,
     *         an empty Optional if no active compensation exists for the specified employee
     */
    private Optional<Compensation> findActiveCompensation(String employeeId) {
        Compensation activeCompensation = compensationRepository.findByEmployeeIdAndIsActive(employeeId, true);

        if (activeCompensation != null) {
            LOG.debug("Found active compensation [{}] for employee [{}]",
                    activeCompensation.getCompensationId(), employeeId);
            return Optional.of(activeCompensation);
        } else {
            LOG.info("No active compensation found for employee [{}]", employeeId);
            return Optional.empty();
        }
    }

    /**
     * Logs a message indicating the successful creation of a compensation record.
     *
     * @param savedCompensation the Compensation object that has been successfully created,
     *                          containing details such as the unique compensation ID,
     *                          employee ID, salary and currency
     */
    private void logSuccessfulCreation(Compensation savedCompensation) {
        LOG.info("Successfully created compensation [{}] for employee [{}] with salary [{}] {}",
                savedCompensation.getCompensationId(),
                savedCompensation.getEmployeeId(),
                savedCompensation.getSalary(),
                savedCompensation.getCurrency());
    }

    /**
     * Validates the provided Compensation object before its creation.
     * Ensures that the Compensation object is valid by checking for non-nullity,
     * valid employee ID, salary range, and effective date. Throws a runtime exception
     * if any validation fails.
     *
     * @param compensation the Compensation object to be validated for creation.
     *                     Must not be null and must contain valid employee ID,
     **/
    private void validateCompensationForCreation(Compensation compensation) {
        if (compensation == null) {
            throw new RuntimeException("Compensation cannot be null");
        }
        ValidationUtils.validateNotEmpty(compensation.getEmployeeId(), "Employee ID");
        ValidationUtils.validateSalaryRange(compensation.getSalary());
        ValidationUtils.validateEffectiveDate(compensation.getEffectiveDate());
    }

    /**
     * Finds an employee by their unique employee identifier.
     *
     * @param employeeId the unique identifier of the employee to be searched
     * @return an Optional containing the Employee object if found; otherwise, an empty Optional
     */
    private Optional<Employee> findEmployee(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeId(employeeId);
        return Optional.ofNullable(employee);
    }

    /**
     * Validates the format of an employee ID to ensure it is not empty.
     *
     * @param employeeId the unique identifier of the employee to validate
     * @return true if the employee ID format is valid, otherwise false
     */
    private boolean isValidEmployeeIdFormat(String employeeId) {
        try {
            ValidationUtils.validateNotEmpty(employeeId, "Employee ID");
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Deactivates the existing active compensation record for the specified employee.
     * If an active compensation is found for the given employee ID, the method marks it as inactive,
     * sets its end date to the current date, and persists the changes.
     *
     * @param employeeId the unique identifier of the employee whose active compensation record
     *                   needs to be deactivated
     */
    private void deactivateExistingCompensation(String employeeId) {
        Compensation existingActive = compensationRepository.findByEmployeeIdAndIsActive(employeeId, true);
        if (existingActive != null) {
            LOG.info("Deactivating existing active compensation [{}] for employee [{}]",
                    existingActive.getCompensationId(), employeeId);
            existingActive.setActive(false);
            existingActive.setEndDate(LocalDate.now());
            compensationRepository.save(existingActive);
        }
    }
}