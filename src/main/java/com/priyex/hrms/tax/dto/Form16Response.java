package com.priyex.hrms.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Form16Response {

    // Header Details
    private String certificateNumber;
    private String lastUpdatedOn;

    // Employer Details (Part A)
    private Long companyId;
    private String employerLegalName;
    private String employerBrandName;
    private String employerAddress;
    private String employerCityState;
    private String employerPan;
    private String employerTan;

    // Employee Details (Part A)
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String employeePan;
    private String employeeDesignation;
    private String employeeDepartment;
    private String employeeAddress;

    // Assessment & Period Details
    private String financialYear; // "2026-2027"
    private String assessmentYear; // "2027-2028"
    private String periodFrom;
    private String periodTo;
    private String chosenRegime; // "NEW" or "OLD"

    // Quarterly Summary of TDS deposited (Part A)
    private List<TdsQuarterSummary> quarterlyTds;
    private BigDecimal totalTdsDeposited;

    // Computation of Salary & Deductions (Part B)
    private BigDecimal grossSalary;
    private BigDecimal allowancesUnderSection10; // HRA etc
    private BigDecimal balanceSalary;
    private BigDecimal standardDeduction;
    private BigDecimal professionalTax;
    private BigDecimal incomeChargeableUnderSalaries;

    // Chapter VI-A Deductions (Part B)
    private BigDecimal deduction80C;
    private BigDecimal deduction80CCD;
    private BigDecimal deduction80D;
    private BigDecimal deductionSection24;
    private BigDecimal otherChapterViADeductions;
    private BigDecimal totalDeductionsChapterViA;

    // Tax Computation (Part B)
    private BigDecimal totalTaxableIncome;
    private BigDecimal taxOnTotalIncome;
    private BigDecimal rebateUnder87A;
    private BigDecimal taxAfterRebate;
    private BigDecimal cess4Percent;
    private BigDecimal netTaxPayable;
    private BigDecimal totalTaxDeductedAtSource;
    private BigDecimal refundOrBalanceDue;

    // Signatory
    private String verificationPlace;
    private String verificationDate;
    private String signatoryName;
    private String signatoryDesignation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TdsQuarterSummary {
        private String quarter; // "Q1 (Apr - Jun)", "Q2 (Jul - Sep)", "Q3 (Oct - Dec)", "Q4 (Jan - Mar)"
        private BigDecimal totalAmountCredited;
        private BigDecimal taxDeducted;
        private BigDecimal taxDeposited;
        private String bsrCode;
        private String challanDate;
        private String challanSerialNo;
    }
}
