package com.priyex.hrms.tax.service.impl;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.organization.dto.CompanyDto;
import com.priyex.hrms.organization.mapper.CompanyMapper;
import com.priyex.hrms.payroll.mapper.PayrollMapper;
import com.priyex.hrms.payroll.model.EmployeePayslip;
import com.priyex.hrms.tax.dto.*;
import com.priyex.hrms.tax.mapper.InvestmentDeclarationMapper;
import com.priyex.hrms.tax.model.InvestmentDeclaration;
import com.priyex.hrms.tax.service.TaxCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxCalculationServiceImpl implements TaxCalculationService {

    private final InvestmentDeclarationMapper declarationMapper;
    private final EmployeeMapper employeeMapper;
    private final CompanyMapper companyMapper;
    private final PayrollMapper payrollMapper;

    private static final String DEFAULT_FY = "2026-2027";
    private static final String DEFAULT_AY = "2027-2028";

    @Override
    public InvestmentDeclaration getDeclaration(Long employeeId, String financialYear) {
        String fy = (financialYear != null && !financialYear.isBlank()) ? financialYear : DEFAULT_FY;
        return declarationMapper.findByEmployeeAndYear(employeeId, fy)
                .orElseGet(() -> buildDefaultDeclaration(employeeId, fy));
    }

    @Override
    @Transactional
    public InvestmentDeclaration saveOrSubmitDeclaration(Long companyId, Long employeeId, SubmitDeclarationRequest req) {
        String fy = (req.getFinancialYear() != null && !req.getFinancialYear().isBlank()) ? req.getFinancialYear() : DEFAULT_FY;
        String ay = deriveAssessmentYear(fy);

        Optional<InvestmentDeclaration> existingOpt = declarationMapper.findByEmployeeAndYear(employeeId, fy);
        InvestmentDeclaration decl = existingOpt.orElseGet(() -> InvestmentDeclaration.builder()
                .companyId(companyId)
                .employeeId(employeeId)
                .financialYear(fy)
                .assessmentYear(ay)
                .build());

        decl.setRegime(req.getRegime() != null ? req.getRegime() : "NEW");
        decl.setStatus(Boolean.TRUE.equals(req.getIsDraft()) ? "DRAFT" : "SUBMITTED");
        if (!Boolean.TRUE.equals(req.getIsDraft())) {
            decl.setSubmittedAt(OffsetDateTime.now());
        }

        // Section 80C
        decl.setSec80cEpf(nz(req.getSec80cEpf()));
        decl.setSec80cPpf(nz(req.getSec80cPpf()));
        decl.setSec80cElss(nz(req.getSec80cElss()));
        decl.setSec80cLifeInsurance(nz(req.getSec80cLifeInsurance()));
        decl.setSec80cHousingPrincipal(nz(req.getSec80cHousingPrincipal()));
        decl.setSec80cTuitionFees(nz(req.getSec80cTuitionFees()));
        decl.setSec80cNscFd(nz(req.getSec80cNscFd()));

        BigDecimal total80c = decl.getSec80cEpf().add(decl.getSec80cPpf())
                .add(decl.getSec80cElss()).add(decl.getSec80cLifeInsurance())
                .add(decl.getSec80cHousingPrincipal()).add(decl.getSec80cTuitionFees())
                .add(decl.getSec80cNscFd());
        decl.setSec80cTotal(total80c);
        decl.setSec80cEligible(total80c.min(new BigDecimal("150000.00")));

        // Section 80CCD(1B)
        decl.setSec80ccdNps(nz(req.getSec80ccdNps()));
        decl.setSec80ccdEligible(decl.getSec80ccdNps().min(new BigDecimal("50000.00")));

        // Section 80D
        decl.setSec80dSelfFamily(nz(req.getSec80dSelfFamily()));
        decl.setSec80dParents(nz(req.getSec80dParents()));
        decl.setSec80dPreventiveCheckup(nz(req.getSec80dPreventiveCheckup()));
        BigDecimal total80d = decl.getSec80dSelfFamily().add(decl.getSec80dParents()).add(decl.getSec80dPreventiveCheckup());
        decl.setSec80dTotal(total80d);
        decl.setSec80dEligible(decl.getSec80dSelfFamily().min(new BigDecimal("25000.00"))
                .add(decl.getSec80dParents().min(new BigDecimal("50000.00"))));

        // Section 24(b)
        decl.setSec24HomeLoanInterest(nz(req.getSec24HomeLoanInterest()));
        decl.setSec24Eligible(decl.getSec24HomeLoanInterest().min(new BigDecimal("200000.00")));

        // HRA
        decl.setAnnualRentPaid(nz(req.getAnnualRentPaid()));
        decl.setLandlordName(req.getLandlordName());
        decl.setLandlordPan(req.getLandlordPan());
        decl.setRentalCityType(req.getRentalCityType() != null ? req.getRentalCityType() : "METRO");

        // Calculate HRA eligible exemption based on employee basic salary
        Employee emp = employeeMapper.findById(employeeId, companyId).orElse(null);
        BigDecimal annualCtc = (emp != null && emp.getAnnualCtc() != null) ? emp.getAnnualCtc() : new BigDecimal("1200000.00");
        BigDecimal basicSalary = annualCtc.multiply(new BigDecimal("0.45"));
        BigDecimal hraReceived = basicSalary.multiply(new BigDecimal("0.50"));
        BigDecimal rentMinus10Percent = decl.getAnnualRentPaid().subtract(basicSalary.multiply(new BigDecimal("0.10")));
        if (rentMinus10Percent.compareTo(BigDecimal.ZERO) < 0) rentMinus10Percent = BigDecimal.ZERO;
        BigDecimal cityCap = "METRO".equalsIgnoreCase(decl.getRentalCityType())
                ? basicSalary.multiply(new BigDecimal("0.50"))
                : basicSalary.multiply(new BigDecimal("0.40"));
        BigDecimal hraExemption = hraReceived.min(rentMinus10Percent).min(cityCap);
        decl.setHraExemptionEligible(hraExemption);

        // Other
        decl.setSec80eEducationLoan(nz(req.getSec80eEducationLoan()));
        decl.setSec80gDonations(nz(req.getSec80gDonations()));
        decl.setSec80ttaSavingsInterest(nz(req.getSec80ttaSavingsInterest()));

        // Totals
        BigDecimal totalDeclared = total80c.add(decl.getSec80ccdNps())
                .add(total80d).add(decl.getSec24HomeLoanInterest())
                .add(decl.getAnnualRentPaid()).add(decl.getSec80eEducationLoan())
                .add(decl.getSec80gDonations()).add(decl.getSec80ttaSavingsInterest());
        decl.setTotalDeclaredDeductions(totalDeclared);

        BigDecimal totalEligible = decl.getSec80cEligible().add(decl.getSec80ccdEligible())
                .add(decl.getSec80dEligible()).add(decl.getSec24Eligible())
                .add(decl.getHraExemptionEligible()).add(decl.getSec80eEducationLoan())
                .add(decl.getSec80gDonations()).add(decl.getSec80ttaSavingsInterest().min(new BigDecimal("10000.00")));
        decl.setTotalEligibleDeductions(totalEligible);

        decl.setRemarks(req.getRemarks());

        if (decl.getId() == null) {
            declarationMapper.insert(decl);
        } else {
            declarationMapper.update(decl);
        }

        return declarationMapper.findByEmployeeAndYear(employeeId, fy).orElse(decl);
    }

    @Override
    public TaxCalculationResponse calculateTax(Long companyId, Long employeeId, String financialYear, String regimeOverride) {
        String fy = (financialYear != null && !financialYear.isBlank()) ? financialYear : DEFAULT_FY;
        String ay = deriveAssessmentYear(fy);

        Employee emp = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        BigDecimal annualCtc = emp.getAnnualCtc() != null && emp.getAnnualCtc().compareTo(BigDecimal.ZERO) > 0
                ? emp.getAnnualCtc()
                : new BigDecimal("1800000.00");

        InvestmentDeclaration decl = declarationMapper.findByEmployeeAndYear(employeeId, fy)
                .orElseGet(() -> buildDefaultDeclaration(employeeId, fy));

        TaxCalculationResponse.RegimeComputation newRegime = computeNewTaxRegime(annualCtc);
        TaxCalculationResponse.RegimeComputation oldRegime = computeOldTaxRegime(annualCtc, decl);

        String selectedRegime = (regimeOverride != null && !regimeOverride.isBlank())
                ? regimeOverride
                : (decl.getRegime() != null ? decl.getRegime() : "NEW");

        String recommendedRegime;
        BigDecimal diff;
        String reason;

        if (newRegime.getTotalAnnualTax().compareTo(oldRegime.getTotalAnnualTax()) <= 0) {
            recommendedRegime = "NEW";
            diff = oldRegime.getTotalAnnualTax().subtract(newRegime.getTotalAnnualTax());
            reason = String.format("New Tax Regime (Section 115BAC) provides flat lower tax slabs and saves ₹%s compared to Old Regime.",
                    diff.setScale(0, RoundingMode.HALF_UP));
        } else {
            recommendedRegime = "OLD";
            diff = newRegime.getTotalAnnualTax().subtract(oldRegime.getTotalAnnualTax());
            reason = String.format("Old Tax Regime is more beneficial due to your declared deductions (80C, 80D, HRA & Home Loan interest), saving ₹%s.",
                    diff.setScale(0, RoundingMode.HALF_UP));
        }

        return TaxCalculationResponse.builder()
                .employeeId(emp.getId())
                .employeeName(emp.getFirstName() + " " + emp.getLastName())
                .employeeCode(emp.getEmployeeCode())
                .financialYear(fy)
                .assessmentYear(ay)
                .annualCtc(annualCtc)
                .grossSalary(annualCtc)
                .selectedRegime(selectedRegime)
                .recommendedRegime(recommendedRegime)
                .taxSavingsWithRecommendation(diff)
                .recommendationReason(reason)
                .newRegime(newRegime)
                .oldRegime(oldRegime)
                .build();
    }

    @Override
    public Form16Response generateForm16(Long companyId, Long employeeId, String financialYear) {
        String fy = (financialYear != null && !financialYear.isBlank()) ? financialYear : DEFAULT_FY;
        String ay = deriveAssessmentYear(fy);

        Employee emp = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        CompanyDto comp = companyMapper.findById(companyId);
        TaxCalculationResponse taxResp = calculateTax(companyId, employeeId, fy, null);
        InvestmentDeclaration decl = declarationMapper.findByEmployeeAndYear(employeeId, fy)
                .orElseGet(() -> buildDefaultDeclaration(employeeId, fy));

        String regime = taxResp.getSelectedRegime();
        TaxCalculationResponse.RegimeComputation compChosen = "OLD".equalsIgnoreCase(regime)
                ? taxResp.getOldRegime()
                : taxResp.getNewRegime();

        BigDecimal quarterlyGross = taxResp.getGrossSalary().divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);
        BigDecimal quarterlyTds = compChosen.getTotalAnnualTax().divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);

        List<Form16Response.TdsQuarterSummary> quarters = List.of(
                Form16Response.TdsQuarterSummary.builder()
                        .quarter("Q1 (Apr - Jun)")
                        .totalAmountCredited(quarterlyGross)
                        .taxDeducted(quarterlyTds)
                        .taxDeposited(quarterlyTds)
                        .bsrCode("0210089")
                        .challanDate("2026-07-07")
                        .challanSerialNo("00142")
                        .build(),
                Form16Response.TdsQuarterSummary.builder()
                        .quarter("Q2 (Jul - Sep)")
                        .totalAmountCredited(quarterlyGross)
                        .taxDeducted(quarterlyTds)
                        .taxDeposited(quarterlyTds)
                        .bsrCode("0210089")
                        .challanDate("2026-10-07")
                        .challanSerialNo("00289")
                        .build(),
                Form16Response.TdsQuarterSummary.builder()
                        .quarter("Q3 (Oct - Dec)")
                        .totalAmountCredited(quarterlyGross)
                        .taxDeducted(quarterlyTds)
                        .taxDeposited(quarterlyTds)
                        .bsrCode("0210089")
                        .challanDate("2027-01-07")
                        .challanSerialNo("00412")
                        .build(),
                Form16Response.TdsQuarterSummary.builder()
                        .quarter("Q4 (Jan - Mar)")
                        .totalAmountCredited(quarterlyGross)
                        .taxDeducted(quarterlyTds)
                        .taxDeposited(quarterlyTds)
                        .bsrCode("0210089")
                        .challanDate("2027-04-07")
                        .challanSerialNo("00598")
                        .build()
        );

        String employerName = (comp != null && comp.getLegalName() != null) ? comp.getLegalName() : "Priyex Technologies Private Limited";
        String employerPan = (comp != null && comp.getTaxId() != null) ? comp.getTaxId() : "AAACP1234F";

        return Form16Response.builder()
                .certificateNumber("PRY-F16-" + emp.getEmployeeCode() + "-" + fy.replace("-", ""))
                .lastUpdatedOn("31-May-2027")
                .companyId(companyId)
                .employerLegalName(employerName)
                .employerBrandName(comp != null ? comp.getBrandName() : "Priyex")
                .employerAddress("Nirala Estate, Tech Zone IV, Greater Noida West")
                .employerCityState("Greater Noida, Uttar Pradesh, 201306")
                .employerPan(employerPan)
                .employerTan("DELP12345F")
                .employeeId(emp.getId())
                .employeeName(emp.getFirstName() + " " + emp.getLastName())
                .employeeCode(emp.getEmployeeCode())
                .employeePan(emp.getPanNumber() != null ? emp.getPanNumber() : "ABCDE1234F")
                .employeeDesignation(emp.getDesignationName())
                .employeeDepartment(emp.getDepartmentName())
                .employeeAddress(emp.getAddressLine1() != null ? emp.getAddressLine1() + ", " + emp.getCity() : "Noida, Uttar Pradesh")
                .financialYear(fy)
                .assessmentYear(ay)
                .periodFrom("01-Apr-" + fy.substring(0, 4))
                .periodTo("31-Mar-" + fy.substring(5))
                .chosenRegime(regime)
                .quarterlyTds(quarters)
                .totalTdsDeposited(compChosen.getTotalAnnualTax())
                .grossSalary(compChosen.getGrossSalary())
                .allowancesUnderSection10(compChosen.getHraExemption())
                .balanceSalary(compChosen.getGrossSalary().subtract(compChosen.getHraExemption()))
                .standardDeduction(compChosen.getStandardDeduction())
                .professionalTax(compChosen.getProfessionalTax())
                .incomeChargeableUnderSalaries(compChosen.getGrossSalary().subtract(compChosen.getStandardDeduction()).subtract(compChosen.getProfessionalTax()).subtract(compChosen.getHraExemption()))
                .deduction80C(decl.getSec80cEligible())
                .deduction80CCD(decl.getSec80ccdEligible())
                .deduction80D(decl.getSec80dEligible())
                .deductionSection24(decl.getSec24Eligible())
                .otherChapterViADeductions(decl.getSec80eEducationLoan().add(decl.getSec80gDonations()))
                .totalDeductionsChapterViA(compChosen.getChapterViADeductions().add(compChosen.getHomeLoanInterestDeduction()))
                .totalTaxableIncome(compChosen.getTaxableIncome())
                .taxOnTotalIncome(compChosen.getBaseIncomeTax())
                .rebateUnder87A(compChosen.getRebate87A())
                .taxAfterRebate(compChosen.getTaxAfterRebate())
                .cess4Percent(compChosen.getHealthAndEducationCess())
                .netTaxPayable(compChosen.getTotalAnnualTax())
                .totalTaxDeductedAtSource(compChosen.getTotalAnnualTax())
                .refundOrBalanceDue(BigDecimal.ZERO)
                .verificationPlace("Greater Noida")
                .verificationDate("31-May-2027")
                .signatoryName("Priya Sharma")
                .signatoryDesignation("Head of Human Resources & Compliance")
                .build();
    }

    @Override
    public Form12bbResponse generateForm12bb(Long companyId, Long employeeId, String financialYear) {
        String fy = (financialYear != null && !financialYear.isBlank()) ? financialYear : DEFAULT_FY;
        String ay = deriveAssessmentYear(fy);

        Employee emp = employeeMapper.findById(employeeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        CompanyDto comp = companyMapper.findById(companyId);
        InvestmentDeclaration decl = declarationMapper.findByEmployeeAndYear(employeeId, fy)
                .orElseGet(() -> buildDefaultDeclaration(employeeId, fy));

        return Form12bbResponse.builder()
                .financialYear(fy)
                .assessmentYear(ay)
                .employeeId(emp.getId())
                .employeeName(emp.getFirstName() + " " + emp.getLastName())
                .employeePan(emp.getPanNumber() != null ? emp.getPanNumber() : "ABCDE1234F")
                .employeeDesignation(emp.getDesignationName())
                .employeeDepartment(emp.getDepartmentName())
                .employeeAddress(emp.getAddressLine1() != null ? emp.getAddressLine1() + ", " + emp.getCity() : "Noida, Uttar Pradesh")
                .employerLegalName(comp != null ? comp.getLegalName() : "Priyex Technologies Private Limited")
                .employerPan(comp != null && comp.getTaxId() != null ? comp.getTaxId() : "AAACP1234F")
                .employerTan("DELP12345F")
                .employerAddress("Nirala Estate, Greater Noida, UP")
                .annualRentPaid(decl.getAnnualRentPaid())
                .landlordName(decl.getLandlordName())
                .landlordPan(decl.getLandlordPan())
                .landlordAddress(decl.getRentalCityType() + " Region Residential Address")
                .rentalCityType(decl.getRentalCityType())
                .ltaClaimAmount(BigDecimal.ZERO)
                .homeLoanInterest(decl.getSec24HomeLoanInterest())
                .lenderName(decl.getSec24HomeLoanInterest().compareTo(BigDecimal.ZERO) > 0 ? "HDFC Housing Finance Ltd" : null)
                .lenderPan(decl.getSec24HomeLoanInterest().compareTo(BigDecimal.ZERO) > 0 ? "AAACH1234G" : null)
                .sec80cTotal(decl.getSec80cTotal())
                .sec80cEligible(decl.getSec80cEligible())
                .sec80ccdNps(decl.getSec80ccdEligible())
                .sec80dMedical(decl.getSec80dEligible())
                .sec80eEducation(decl.getSec80eEducationLoan())
                .otherDeductions(decl.getSec80gDonations().add(decl.getSec80ttaSavingsInterest()))
                .totalClaims(decl.getTotalEligibleDeductions())
                .declarationDate(decl.getSubmittedAt() != null ? decl.getSubmittedAt().toLocalDate().toString() : "2026-09-15")
                .declarationPlace("Greater Noida")
                .status(decl.getStatus())
                .build();
    }

    @Override
    public List<InvestmentDeclaration> getDeclarationsForCompany(Long companyId, String financialYear, String status) {
        String fy = (financialYear != null && !financialYear.isBlank()) ? financialYear : DEFAULT_FY;
        return declarationMapper.findByCompanyAndYear(companyId, fy, status);
    }

    @Override
    @Transactional
    public InvestmentDeclaration verifyDeclaration(Long companyId, Long id, Long actorId, VerifyDeclarationRequest req) {
        InvestmentDeclaration decl = declarationMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InvestmentDeclaration", "id", id));

        declarationMapper.updateStatus(id, req.getStatus(), req.getHrNotes(), actorId);
        return declarationMapper.findById(id).orElse(decl);
    }

    // ── Internal Calculation Helpers ──

    private TaxCalculationResponse.RegimeComputation computeNewTaxRegime(BigDecimal grossSalary) {
        BigDecimal stdDeduction = new BigDecimal("75000.00");
        BigDecimal taxableIncome = grossSalary.subtract(stdDeduction);
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) taxableIncome = BigDecimal.ZERO;

        List<TaxCalculationResponse.TaxSlabBreakdown> slabs = new ArrayList<>();
        BigDecimal remaining = taxableIncome;
        BigDecimal baseTax = BigDecimal.ZERO;

        // Slab 1: 0 to 3,00,000 (NIL)
        BigDecimal slab1Amt = remaining.min(new BigDecimal("300000.00"));
        slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                .slabRange("₹0 - ₹3,00,000")
                .ratePercent(BigDecimal.ZERO)
                .slabTaxableAmount(slab1Amt)
                .slabTaxAmount(BigDecimal.ZERO)
                .build());
        remaining = remaining.subtract(slab1Amt);

        // Slab 2: 3,00,001 to 7,00,000 (5%)
        BigDecimal slab2Cap = new BigDecimal("400000.00");
        BigDecimal slab2Amt = remaining.min(slab2Cap);
        BigDecimal slab2Tax = slab2Amt.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                .slabRange("₹3,00,001 - ₹7,00,000")
                .ratePercent(new BigDecimal("5"))
                .slabTaxableAmount(slab2Amt)
                .slabTaxAmount(slab2Tax)
                .build());
        baseTax = baseTax.add(slab2Tax);
        remaining = remaining.subtract(slab2Amt);

        // Slab 3: 7,00,001 to 10,00,000 (10%)
        BigDecimal slab3Cap = new BigDecimal("300000.00");
        BigDecimal slab3Amt = remaining.min(slab3Cap);
        BigDecimal slab3Tax = slab3Amt.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                .slabRange("₹7,00,001 - ₹10,00,000")
                .ratePercent(new BigDecimal("10"))
                .slabTaxableAmount(slab3Amt)
                .slabTaxAmount(slab3Tax)
                .build());
        baseTax = baseTax.add(slab3Tax);
        remaining = remaining.subtract(slab3Amt);

        // Slab 4: 10,00,001 to 12,00,000 (15%)
        BigDecimal slab4Cap = new BigDecimal("200000.00");
        BigDecimal slab4Amt = remaining.min(slab4Cap);
        BigDecimal slab4Tax = slab4Amt.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
        slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                .slabRange("₹10,00,001 - ₹12,00,000")
                .ratePercent(new BigDecimal("15"))
                .slabTaxableAmount(slab4Amt)
                .slabTaxAmount(slab4Tax)
                .build());
        baseTax = baseTax.add(slab4Tax);
        remaining = remaining.subtract(slab4Amt);

        // Slab 5: 12,00,001 to 15,00,000 (20%)
        BigDecimal slab5Cap = new BigDecimal("300000.00");
        BigDecimal slab5Amt = remaining.min(slab5Cap);
        BigDecimal slab5Tax = slab5Amt.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                .slabRange("₹12,00,001 - ₹15,00,000")
                .ratePercent(new BigDecimal("20"))
                .slabTaxableAmount(slab5Amt)
                .slabTaxAmount(slab5Tax)
                .build());
        baseTax = baseTax.add(slab5Tax);
        remaining = remaining.subtract(slab5Amt);

        // Slab 6: Above 15,00,000 (30%)
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal slab6Tax = remaining.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
            slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                    .slabRange("Above ₹15,00,000")
                    .ratePercent(new BigDecimal("30"))
                    .slabTaxableAmount(remaining)
                    .slabTaxAmount(slab6Tax)
                    .build());
            baseTax = baseTax.add(slab6Tax);
        }

        // Section 87A rebate for taxable income <= 7,00,000
        BigDecimal rebate = BigDecimal.ZERO;
        if (taxableIncome.compareTo(new BigDecimal("700000.00")) <= 0) {
            rebate = baseTax;
        }
        BigDecimal taxAfterRebate = baseTax.subtract(rebate);
        BigDecimal cess = taxAfterRebate.multiply(new BigDecimal("0.04")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalTax = taxAfterRebate.add(cess);
        BigDecimal monthlyTds = totalTax.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

        return TaxCalculationResponse.RegimeComputation.builder()
                .regimeName("New Tax Regime (Section 115BAC)")
                .grossSalary(grossSalary)
                .standardDeduction(stdDeduction)
                .chapterViADeductions(BigDecimal.ZERO)
                .hraExemption(BigDecimal.ZERO)
                .homeLoanInterestDeduction(BigDecimal.ZERO)
                .professionalTax(BigDecimal.ZERO)
                .otherDeductions(BigDecimal.ZERO)
                .totalDeductions(stdDeduction)
                .taxableIncome(taxableIncome)
                .slabBreakdown(slabs)
                .baseIncomeTax(baseTax)
                .rebate87A(rebate)
                .taxAfterRebate(taxAfterRebate)
                .healthAndEducationCess(cess)
                .totalAnnualTax(totalTax)
                .monthlyTds(monthlyTds)
                .build();
    }

    private TaxCalculationResponse.RegimeComputation computeOldTaxRegime(BigDecimal grossSalary, InvestmentDeclaration decl) {
        BigDecimal stdDeduction = new BigDecimal("50000.00");
        BigDecimal profTax = new BigDecimal("2400.00");

        BigDecimal chapterViA = decl.getSec80cEligible().add(decl.getSec80ccdEligible()).add(decl.getSec80dEligible());
        BigDecimal homeLoan = decl.getSec24Eligible();
        BigDecimal hra = decl.getHraExemptionEligible();
        BigDecimal other = decl.getSec80eEducationLoan().add(decl.getSec80gDonations())
                .add(decl.getSec80ttaSavingsInterest().min(new BigDecimal("10000.00")));

        BigDecimal totalDeductions = stdDeduction.add(profTax).add(chapterViA).add(homeLoan).add(hra).add(other);
        BigDecimal taxableIncome = grossSalary.subtract(totalDeductions);
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) taxableIncome = BigDecimal.ZERO;

        List<TaxCalculationResponse.TaxSlabBreakdown> slabs = new ArrayList<>();
        BigDecimal remaining = taxableIncome;
        BigDecimal baseTax = BigDecimal.ZERO;

        // Slab 1: 0 to 2,50,000 (NIL)
        BigDecimal slab1Amt = remaining.min(new BigDecimal("250000.00"));
        slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                .slabRange("₹0 - ₹2,50,000")
                .ratePercent(BigDecimal.ZERO)
                .slabTaxableAmount(slab1Amt)
                .slabTaxAmount(BigDecimal.ZERO)
                .build());
        remaining = remaining.subtract(slab1Amt);

        // Slab 2: 2,50,001 to 5,00,000 (5%)
        BigDecimal slab2Cap = new BigDecimal("250000.00");
        BigDecimal slab2Amt = remaining.min(slab2Cap);
        BigDecimal slab2Tax = slab2Amt.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
        slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                .slabRange("₹2,50,001 - ₹5,00,000")
                .ratePercent(new BigDecimal("5"))
                .slabTaxableAmount(slab2Amt)
                .slabTaxAmount(slab2Tax)
                .build());
        baseTax = baseTax.add(slab2Tax);
        remaining = remaining.subtract(slab2Amt);

        // Slab 3: 5,00,001 to 10,00,000 (20%)
        BigDecimal slab3Cap = new BigDecimal("500000.00");
        BigDecimal slab3Amt = remaining.min(slab3Cap);
        BigDecimal slab3Tax = slab3Amt.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                .slabRange("₹5,00,001 - ₹10,00,000")
                .ratePercent(new BigDecimal("20"))
                .slabTaxableAmount(slab3Amt)
                .slabTaxAmount(slab3Tax)
                .build());
        baseTax = baseTax.add(slab3Tax);
        remaining = remaining.subtract(slab3Amt);

        // Slab 4: Above 10,00,000 (30%)
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal slab4Tax = remaining.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
            slabs.add(TaxCalculationResponse.TaxSlabBreakdown.builder()
                    .slabRange("Above ₹10,00,000")
                    .ratePercent(new BigDecimal("30"))
                    .slabTaxableAmount(remaining)
                    .slabTaxAmount(slab4Tax)
                    .build());
            baseTax = baseTax.add(slab4Tax);
        }

        // Section 87A rebate for taxable income <= 5,00,000
        BigDecimal rebate = BigDecimal.ZERO;
        if (taxableIncome.compareTo(new BigDecimal("500000.00")) <= 0) {
            rebate = baseTax;
        }
        BigDecimal taxAfterRebate = baseTax.subtract(rebate);
        BigDecimal cess = taxAfterRebate.multiply(new BigDecimal("0.04")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalTax = taxAfterRebate.add(cess);
        BigDecimal monthlyTds = totalTax.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);

        return TaxCalculationResponse.RegimeComputation.builder()
                .regimeName("Old Tax Regime (With Deductions)")
                .grossSalary(grossSalary)
                .standardDeduction(stdDeduction)
                .chapterViADeductions(chapterViA)
                .hraExemption(hra)
                .homeLoanInterestDeduction(homeLoan)
                .professionalTax(profTax)
                .otherDeductions(other)
                .totalDeductions(totalDeductions)
                .taxableIncome(taxableIncome)
                .slabBreakdown(slabs)
                .baseIncomeTax(baseTax)
                .rebate87A(rebate)
                .taxAfterRebate(taxAfterRebate)
                .healthAndEducationCess(cess)
                .totalAnnualTax(totalTax)
                .monthlyTds(monthlyTds)
                .build();
    }

    private InvestmentDeclaration buildDefaultDeclaration(Long employeeId, String financialYear) {
        return InvestmentDeclaration.builder()
                .employeeId(employeeId)
                .financialYear(financialYear)
                .assessmentYear(deriveAssessmentYear(financialYear))
                .regime("NEW")
                .status("DRAFT")
                .sec80cEpf(BigDecimal.ZERO)
                .sec80cPpf(BigDecimal.ZERO)
                .sec80cElss(BigDecimal.ZERO)
                .sec80cLifeInsurance(BigDecimal.ZERO)
                .sec80cHousingPrincipal(BigDecimal.ZERO)
                .sec80cTuitionFees(BigDecimal.ZERO)
                .sec80cNscFd(BigDecimal.ZERO)
                .sec80cTotal(BigDecimal.ZERO)
                .sec80cEligible(BigDecimal.ZERO)
                .sec80ccdNps(BigDecimal.ZERO)
                .sec80ccdEligible(BigDecimal.ZERO)
                .sec80dSelfFamily(BigDecimal.ZERO)
                .sec80dParents(BigDecimal.ZERO)
                .sec80dPreventiveCheckup(BigDecimal.ZERO)
                .sec80dTotal(BigDecimal.ZERO)
                .sec80dEligible(BigDecimal.ZERO)
                .sec24HomeLoanInterest(BigDecimal.ZERO)
                .sec24Eligible(BigDecimal.ZERO)
                .annualRentPaid(BigDecimal.ZERO)
                .rentalCityType("METRO")
                .hraExemptionEligible(BigDecimal.ZERO)
                .sec80eEducationLoan(BigDecimal.ZERO)
                .sec80gDonations(BigDecimal.ZERO)
                .sec80ttaSavingsInterest(BigDecimal.ZERO)
                .totalDeclaredDeductions(BigDecimal.ZERO)
                .totalEligibleDeductions(BigDecimal.ZERO)
                .build();
    }

    private String deriveAssessmentYear(String fy) {
        try {
            String[] parts = fy.split("-");
            int y1 = Integer.parseInt(parts[0]) + 1;
            int y2 = Integer.parseInt(parts[1]) + 1;
            return y1 + "-" + y2;
        } catch (Exception e) {
            return DEFAULT_AY;
        }
    }

    private BigDecimal nz(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
