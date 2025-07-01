package com.mindex.challenge.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing an employee's reporting structure.
 * Contains the employee information and the total count of all their direct and indirect reports.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportingStructure {
    
    private EmployeeSummary employee;
    private int numberOfReports;
    private List<DirectReportSummary> directReports;

    @Override
    public String toString() {
        return "ReportingStructure{" +
                "employee=" + employee +
                ", numberOfReports=" + numberOfReports +
                ", directReportsCount=" + (directReports != null ? directReports.size() : 0) +
                '}';
    }

}
