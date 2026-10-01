package com.priyex.hrms.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Form12bbResponse {

    private String financialYear; // "2026-2027"
    private String assessmentYear; // "2027-2028"

    // Employee
    private Long employeeId;
    private String employeeName;
    private String employeePan;
    private String employeeDesignation;
    private String employeeDepartment;
    private String employeeAddress;

    // Employer
    private String employerLegalName;
    private String employerPan;
    private String employerTan;
    private String employerAddress;

    // Section 1: House Rent Allowance (HRA)
    private BigDecimal annualRentPaid;
    private String landlordName;
    private String landlordPan;
    private String landlordAddress;
    private String rentalCityType;

    // Section 2: Leave Travel Concession / Assistance (LTA)
    private BigDecimal ltaClaimAmount;

    // Section 3: Deduction of interest on borrowing (Section 24)
    private BigDecimal homeLoanInterest;
    private String lenderName;
    private String lenderPan;

    // Section 4: Chapter VI-A Deductions
    private BigDecimal sec80cTotal;
    private BigDecimal sec80cEligible;
    private BigDecimal sec80ccdNps;
    private BigDecimal sec80dMedical;
    private BigDecimal sec80eEducation;
    private BigDecimal otherDeductions;
    private BigDecimal totalClaims;

    // Verification
    private String declarationDate;
    private String declarationPlace;
    private String status; // SUBMITTED, VERIFIED
}
