package com.priyex.hrms.tax.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentDeclaration {

    private Long id;
    private Long companyId;
    private Long employeeId;
    private String financialYear; // e.g., "2026-2027"
    private String assessmentYear; // e.g., "2027-2028"
    private String regime; // NEW, OLD
    private String status; // DRAFT, SUBMITTED, VERIFIED, REJECTED

    // Section 80C (Max ₹1,50,000)
    private BigDecimal sec80cEpf;
    private BigDecimal sec80cPpf;
    private BigDecimal sec80cElss;
    private BigDecimal sec80cLifeInsurance;
    private BigDecimal sec80cHousingPrincipal;
    private BigDecimal sec80cTuitionFees;
    private BigDecimal sec80cNscFd;
    private BigDecimal sec80cTotal;
    private BigDecimal sec80cEligible;

    // Section 80CCD(1B) - NPS (Max ₹50,000)
    private BigDecimal sec80ccdNps;
    private BigDecimal sec80ccdEligible;

    // Section 80D - Health Insurance
    private BigDecimal sec80dSelfFamily;
    private BigDecimal sec80dParents;
    private BigDecimal sec80dPreventiveCheckup;
    private BigDecimal sec80dTotal;
    private BigDecimal sec80dEligible;

    // Section 24(b) - Home Loan Interest
    private BigDecimal sec24HomeLoanInterest;
    private BigDecimal sec24Eligible;

    // Section 10(13A) - HRA
    private BigDecimal annualRentPaid;
    private String landlordName;
    private String landlordPan;
    private String rentalCityType; // METRO, NON_METRO
    private BigDecimal hraExemptionEligible;

    // Other Deductions
    private BigDecimal sec80eEducationLoan;
    private BigDecimal sec80gDonations;
    private BigDecimal sec80ttaSavingsInterest;

    // Totals & Audit
    private BigDecimal totalDeclaredDeductions;
    private BigDecimal totalEligibleDeductions;
    private String remarks;
    private String hrNotes;
    private OffsetDateTime submittedAt;
    private OffsetDateTime verifiedAt;
    private Long verifiedBy;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Join fields
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String designationName;
    private BigDecimal annualCtc;
}
