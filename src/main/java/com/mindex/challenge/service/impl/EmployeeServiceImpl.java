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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

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
     * Calculates and returns the reporting structure for an employee.
     * Uses DFS traversal with Set<String> for unique counting as discussed.
     * Returns Optional<ReportingStructure> to minimize controller logic.
     *
     * @param employeeId the ID of the employee to get reporting structure for
     * @return Optional containing ReportingStructure if employee exists, empty if not found
     */
    @Override
    public Optional<ReportingStructure> getReportingStructure(String id) {
        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            LOG.info("No employee found for id [{}]", id);
            return Optional.empty();
        }

        //Use for tracking unique employee ids, leveraging the Set collection to prevent counting duplicate ids.
        Set<String> uniqueEmployeeIds = new HashSet<>();

        //DFS traversal
        long startTime = System.currentTimeMillis();
        int totalReports = calcu

        EmployeeProjection employeeProjection = employeeRepository.findProjectedByEmployeeId(id);

        return Optional.of(new ReportingStructure(employee, employeeProjection.getDirectReports().size()));
    }





}
