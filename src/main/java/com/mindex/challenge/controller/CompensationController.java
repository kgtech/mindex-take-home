package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.exception.CompensationValidationException;
import com.mindex.challenge.exception.EmployeeNotFoundException;
import com.mindex.challenge.service.CompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/v1/compensation")
public class CompensationController {

    private static final Logger LOG = LoggerFactory.getLogger(CompensationController.class);

    private final CompensationService compensationService;

    public CompensationController(CompensationService compensationService) {
        this.compensationService = compensationService;
    }

    /**
     * Creates a new compensation record for an employee.
     *
     * @param compensation the compensation data to create
     * @return the created compensation record with generated ID and system fields
     *
     * @throws CompensationValidationException if compensation data is invalid
     * @throws EmployeeNotFoundException if the specified employee does not exist
     */
    @PostMapping
    public ResponseEntity<Compensation> create(@RequestBody Compensation compensation) {
        LOG.debug("Received compensation create request: [{}]", compensation);

        try {
            Compensation createdCompensation = compensationService.create(compensation);
            LOG.info("Successfully created compensation [{}] for employee [{}]",
                    createdCompensation.getCompensationId(),
                    createdCompensation.getEmployeeId());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdCompensation);

        } catch (CompensationValidationException e) {
            LOG.warn("Compensation validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (EmployeeNotFoundException e) {
            LOG.warn("Employee not found for compensation creation: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves the active compensation record for a specified employee.
     *
     * @param employeeId the unique identifier of the employee
     * @return the active compensation record if found, otherwise 404 Not Found
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<Compensation> read(@PathVariable String employeeId) {
        LOG.debug("Received compensation read request for employee [{}]", employeeId);

        Optional<Compensation> compensation = compensationService.read(employeeId);

        if (compensation.isPresent()) {
            LOG.debug("Found compensation [{}] for employee [{}]",
                    compensation.get().getCompensationId(), employeeId);
            return ResponseEntity.ok(compensation.get());
        } else {
            LOG.info("No active compensation found for employee [{}]", employeeId);
            return ResponseEntity.notFound().build();
        }
    }
}