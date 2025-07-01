package com.mindex.challenge.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectReportSummary {
    private String employeeId;
    private String firstName;
    private String lastName;
    private String position;
    private String department;
}
