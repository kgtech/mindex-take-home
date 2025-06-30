package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.dto.EmployeeProjection;
import com.mindex.challenge.data.dto.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    // Fail fast approach to limit resources used and prevent a bad user experience
    // Prevents memory issues if hierarchy is over 10000.
    // Also enables circuit breaker pattern to allow for max size and max processing time.
    // If I were working in an actual production environment these would be stored where they could be modified
    // they could  have variations based on the client.
    private static final int MAX_HIERARCHY_SIZE = 10000;
    private static final int MAX_PROCESSING_TIME_MS = 30000; // 30 seconds


    @Autowired
    private EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    /**
     * Calculates and returns the reporting structure for an employee using iterative DFS traversal.
     * This approach prevents stack overflow issues and provides better error handling compared to recursion.
     * Returns Optional<ReportingStructure> to minimize controller logic.
     *
     * @param employeeId the ID of the employee to get reporting structure for
     * @return Optional containing ReportingStructure if employee exists, empty if not found
     */
    @Override
    public Optional<ReportingStructure> getReportingStructure(String employeeId) {
        LOG.info("Calculating reporting structure for employee [{}]", employeeId);

        // Load the full employee object for the result
        Employee employee = employeeRepository.findByEmployeeId(employeeId);
        if (employee == null) {
            LOG.info("No employee found for employeeId [{}] - returning empty result", employeeId);
            return Optional.empty();
        }

        long startTime = System.currentTimeMillis();
        int totalReports = calculateReportsIteratively(employeeId);
        long endTime = System.currentTimeMillis();

        LOG.info("Reporting structure calculation completed for employee [{}]. " +
                        "Total reports: {}, Processing time: {}ms",
                employeeId, totalReports, (endTime - startTime));

        ReportingStructure reportingStructure = new ReportingStructure(employee, totalReports);
        return Optional.of(reportingStructure);
    }

    /**
     * Iterative DFS implementation for calculating total reports.
     * Uses Stack<String> for employee IDs to process and Set<String> for tracking visited employees.
     *
     * Benefits over recursive approach:
     * - No stack overflow risk (uses heap memory instead of call stack)
     * - Better error handling and monitoring capabilities
     * - Predictable memory usage
     * - Production safety limits and circuit breakers
     *
     * @param rootEmployeeId the starting employee ID for traversal
     * @return total count of all direct and indirect reports
     */
    private int calculateReportsIteratively(String rootEmployeeId) {
        // Stack for DFS traversal - stores employee IDs to process
        Stack<String> employeesToProcess = new Stack<>();

        // Set for tracking unique employee IDs - prevents duplicate counting
        Set<String> uniqueReports = new HashSet<>();

        // Performance and safety tracking
        int totalProcessed = 0;
        long startTime = System.currentTimeMillis();

        // Add root employee's direct reports to processing stack
        EmployeeProjection rootEmployee = employeeRepository.findProjectedByEmployeeId(rootEmployeeId);
        if (rootEmployee == null || rootEmployee.getDirectReports() == null) {
            return 0;
        }

        // Initialize stack with root employee's direct reports
        for (Employee directReport : rootEmployee.getDirectReports()) {
            String reportId = directReport.getEmployeeId();
            if (reportId != null && uniqueReports.add(reportId)) {
                employeesToProcess.push(reportId);
            }
        }

        // Iterative DFS traversal
        while (!employeesToProcess.isEmpty()) {
            String currentEmployeeId = employeesToProcess.pop();
            totalProcessed++;

            // Production safety: Circuit breaker for large hierarchies
            if (totalProcessed > MAX_HIERARCHY_SIZE) {
                LOG.error("Hierarchy size exceeded maximum limit of {} employees. " +
                        "Stopping traversal for employee [{}]", MAX_HIERARCHY_SIZE, rootEmployeeId);
                throw new RuntimeException("Employee hierarchy too large to process safely");
            }

            // Production safety: Time-based circuit breaker
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > MAX_PROCESSING_TIME_MS) {
                LOG.error("Processing time exceeded maximum limit of {}ms. " +
                        "Stopping traversal for employee [{}]", MAX_PROCESSING_TIME_MS, rootEmployeeId);
                throw new RuntimeException("Employee hierarchy processing timeout");
            }

            // Progress logging for large hierarchies
            if (totalProcessed % 100 == 0) {
                LOG.info("Processed {} employees so far for reporting structure of [{}]",
                        totalProcessed, rootEmployeeId);
            }

            // Load current employee's projection (optimized query)
            EmployeeProjection currentEmployee = employeeRepository.findProjectedByEmployeeId(currentEmployeeId);

            if (currentEmployee == null) {
                LOG.warn("Employee [{}] not found during traversal - skipping", currentEmployeeId);
                continue;
            }

            // Process direct reports of current employee
            List<Employee> directReports = currentEmployee.getDirectReports();
            if (directReports != null && !directReports.isEmpty()) {
                for (Employee directReport : directReports) {
                    String reportId = directReport.getEmployeeId();

                    if (reportId == null) {
                        LOG.warn("Found null employeeId in direct reports for employee [{}] - skipping",
                                currentEmployeeId);
                        continue;
                    }

                    // Add to processing stack only if not already visited
                    // Set.add() returns true if element was newly added (not duplicate)
                    if (uniqueReports.add(reportId)) {
                        employeesToProcess.push(reportId);
                        LOG.debug("Added employee [{}] to processing queue", reportId);
                    } else {
                        LOG.debug("Employee [{}] already processed - preventing duplicate count", reportId);
                    }
                }
            }
        }

        LOG.info("Iterative traversal completed. Total employees processed: {}, Unique reports found: {}",
                totalProcessed, uniqueReports.size());

        return uniqueReports.size();
    }






}
