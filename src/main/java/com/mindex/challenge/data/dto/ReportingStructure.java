package com.mindex.challenge.data.dto;

import com.mindex.challenge.data.Employee;

/**
 * Data Transfer Object representing an employee's reporting structure.
 * Contains the employee information and the total count of all their direct and indirect reports.
 * 
 * Uses full Employee object for reusability as discussed.
 *
 */
public class ReportingStructure {
    
    private Employee employee;
    private int numberOfReports;
    
    public ReportingStructure() {
    }
    
    public ReportingStructure(Employee employee, int numberOfReports) {
        this.employee = employee;
        this.numberOfReports = numberOfReports;
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    public int getNumberOfReports() {
        return numberOfReports;
    }
    
    public void setNumberOfReports(int numberOfReports) {
        this.numberOfReports = numberOfReports;
    }
    
    @Override
    public String toString() {
        return "ReportingStructure{" +
                "employee=" + employee +
                ", numberOfReports=" + numberOfReports +
                '}';
    }
}
