package com.mindex.challenge.data.dto;

import com.mindex.challenge.data.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary representation of an Employee without directReports.
 * Used in ReportingStructure to avoid duplication with DirectReportSummary list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSummary {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String position;
    private String department;

    /**
     * Convenience constructor to create from Employee entity
     */
    public EmployeeSummary(Employee employee) {
        this.employeeId = employee.getEmployeeId();
        this.firstName = employee.getFirstName();
        this.lastName = employee.getLastName();
        this.position = employee.getPosition();
        this.department = employee.getDepartment();
    }
}