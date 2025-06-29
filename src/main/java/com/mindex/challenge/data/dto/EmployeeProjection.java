
package com.mindex.challenge.data.dto;

import com.mindex.challenge.data.Employee;
import java.util.List;

/**
 * Projection interface for optimized Employee data loading.
 * Spring Data MongoDB has excellent support for projection interfaces.
 * Only loads employeeId and directReports fields to optimize performance
 * and reduce network/memory overhead.
 * 
 * Uses projection interface over records for better Spring Data MongoDB support.
 */
public interface EmployeeProjection {

    String getEmployeeId();
    List<Employee> getDirectReports();
}
