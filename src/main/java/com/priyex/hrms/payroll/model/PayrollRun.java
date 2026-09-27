package com.priyex.hrms.payroll.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRun {
    private Long id;
    private Long companyId;
    private Integer payrollMonth;
    private Integer payrollYear;
    private String payrollName;
    private Integer totalEmployees;
    private BigDecimal totalGross;
    private BigDecimal totalDeductions;
    private BigDecimal totalNet;
    private String status; // DRAFT, PROCESSED, DISBURSED
    private LocalDate disbursementDate;
    private Long processedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
