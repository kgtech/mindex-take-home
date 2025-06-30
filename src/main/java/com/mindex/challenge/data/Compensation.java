package com.mindex.challenge.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compensation {

    @Id
    private String compensationId;

    @Indexed
    private String employeeId;

    private BigDecimal salary;
    private LocalDate effectiveDate;
    private LocalDate endDate;        // For historical tracking
    private boolean isActive;         // Current vs historical compensation
    private String currency;          // "USD", "EUR", etc.


    public Compensation(String employeeId, BigDecimal salary, LocalDate effectiveDate, String currency) {
        this.employeeId = employeeId;
        this.salary = salary;
        this.effectiveDate = effectiveDate;
        this.currency = currency;
        this.isActive = true;  // New compensation is active by default
    }


    @Override
    public String toString() {
        return "Compensation{" +
                "compensationId='" + compensationId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", salary=" + salary +
                ", effectiveDate=" + effectiveDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                ", currency='" + currency + '\'' +
                '}';
    }
}