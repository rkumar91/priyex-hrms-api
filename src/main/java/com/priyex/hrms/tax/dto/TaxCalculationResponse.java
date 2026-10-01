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
public class TaxCalculationResponse {

    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String financialYear; // "2026-2027"
    private String assessmentYear; // "2027-2028"
    private BigDecimal annualCtc;
    private BigDecimal grossSalary;

    // Selected / Active Regime
    private String selectedRegime; // "NEW" or "OLD"
    private String recommendedRegime; // "NEW" or "OLD"
    private BigDecimal taxSavingsWithRecommendation;
    private String recommendationReason;

    // Detailed Breakdown per Regime
    private RegimeComputation newRegime;
    private RegimeComputation oldRegime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegimeComputation {
        private String regimeName;
        private BigDecimal grossSalary;
        private BigDecimal standardDeduction;
        private BigDecimal chapterViADeductions;
        private BigDecimal hraExemption;
        private BigDecimal homeLoanInterestDeduction;
        private BigDecimal professionalTax;
        private BigDecimal otherDeductions;
        private BigDecimal totalDeductions;
        private BigDecimal taxableIncome;

        private List<TaxSlabBreakdown> slabBreakdown;
        private BigDecimal baseIncomeTax;
        private BigDecimal rebate87A;
        private BigDecimal taxAfterRebate;
        private BigDecimal healthAndEducationCess; // 4%
        private BigDecimal totalAnnualTax;
        private BigDecimal monthlyTds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxSlabBreakdown {
        private String slabRange;
        private BigDecimal ratePercent;
        private BigDecimal slabTaxableAmount;
        private BigDecimal slabTaxAmount;
    }
}
