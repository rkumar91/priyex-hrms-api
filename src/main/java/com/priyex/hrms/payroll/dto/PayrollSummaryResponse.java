package com.priyex.hrms.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollSummaryResponse {
    private BigDecimal grossEarnings;
    private BigDecimal statutoryDeductions;
    private BigDecimal netPayout;
    private Integer activeEmployeesCount;
    private String currentPeriod;
    private String processingStatus;
}
