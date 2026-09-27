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
public class CtcBreakdownResponse {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String designationName;
    private String departmentName;

    private BigDecimal annualCtc;
    private BigDecimal monthlyGross;
    private BigDecimal monthlyNetSalary;

    // Monthly Earnings
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal specialAllowance;
    private BigDecimal medicalAllowance;
    private BigDecimal conveyanceAllowance;
    private BigDecimal performanceBonus;

    // Monthly Deductions
    private BigDecimal epfEmployee;
    private BigDecimal esicEmployee;
    private BigDecimal professionalTax;
    private BigDecimal tdsTax;
    private BigDecimal totalDeductions;

    // Employer Retirals (Part of Annual CTC)
    private BigDecimal epfEmployer;
    private BigDecimal esicEmployer;
    private BigDecimal gratuity;
}
