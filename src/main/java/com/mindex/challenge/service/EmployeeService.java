package com.mindex.challenge.service;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.dto.ReportingStructure;

import java.util.Optional;

public interface EmployeeService {
    Employee create(Employee employee);
    Employee read(String id);
    Employee update(Employee employee);
    Optional<ReportingStructure> getReportingStructure(String id);
}
