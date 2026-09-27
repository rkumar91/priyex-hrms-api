package com.priyex.hrms.payroll.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutePayrollRequest {

    @NotNull(message = "Payroll month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer payrollMonth;

    @NotNull(message = "Payroll year is required")
    @Min(value = 2020, message = "Year must be valid")
    private Integer payrollYear;

    private LocalDate disbursementDate;

    private Integer workingDays;
}
