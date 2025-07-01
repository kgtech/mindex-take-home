package com.mindex.challenge.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents an employee in the system with various details such as
 * personal information, position, department, and reporting structure.
 * This class also allows representing hierarchical relationships
 * between employees through the directReports property.
 *
 * Features:
 * - Includes details like firstName, lastName, position, and department.
 * - Supports hierarchical structure through a list of directReports.
 *
 *  An instance of this class can be used to manage and represent
 * both individual employee details and organizational structure.
 *
 * Moved to Lombok annotations to remove boilerplate code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String position;
    private String department;
    private List<Employee> directReports;

}
