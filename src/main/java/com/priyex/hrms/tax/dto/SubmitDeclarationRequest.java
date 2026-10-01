package com.priyex.hrms.tax.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitDeclarationRequest {

    @NotBlank(message = "Financial year is required (e.g. 2026-2027)")
    private String financialYear;

    private String regime; // NEW or OLD

    // Section 80C
    private BigDecimal sec80cEpf;
    private BigDecimal sec80cPpf;
    private BigDecimal sec80cElss;
    private BigDecimal sec80cLifeInsurance;
    private BigDecimal sec80cHousingPrincipal;
    private BigDecimal sec80cTuitionFees;
    private BigDecimal sec80cNscFd;

    // Section 80CCD(1B) - NPS
    private BigDecimal sec80ccdNps;

    // Section 80D - Medical Insurance
    private BigDecimal sec80dSelfFamily;
    private BigDecimal sec80dParents;
    private BigDecimal sec80dPreventiveCheckup;

    // Section 24(b) - Home Loan Interest
    private BigDecimal sec24HomeLoanInterest;

    // HRA
    private BigDecimal annualRentPaid;
    private String landlordName;
    private String landlordPan;
    private String rentalCityType; // METRO or NON_METRO

    // Other
    private BigDecimal sec80eEducationLoan;
    private BigDecimal sec80gDonations;
    private BigDecimal sec80ttaSavingsInterest;

    private String remarks;
    private Boolean isDraft;
}
