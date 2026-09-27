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
public class EmployeePayslip {
    private Long id;
    private Long companyId;
    private Long payrollRunId;
    private Long employeeId;
    
    // Additional mapped employee info
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String designationName;
    private String panNumber;
    private String uanNumber;
    private String pfNumber;
    private String bankName;
    private String bankAccountNumber;
    private String bankIfsc;
    
    private Integer payrollMonth;
    private Integer payrollYear;
    private String payPeriod;
    private Integer workingDays;
    private Integer paidDays;
    private Integer lopDays;

    private BigDecimal annualCtc;
    private BigDecimal monthlyGross;

    // Earnings
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal specialAllowance;
    private BigDecimal medicalAllowance;
    private BigDecimal conveyanceAllowance;
    private BigDecimal performanceBonus;
    private BigDecimal totalEarnings;

    // Deductions
    private BigDecimal epfEmployee;
    private BigDecimal esicEmployee;
    private BigDecimal professionalTax;
    private BigDecimal tdsTax;
    private BigDecimal totalDeductions;

    // Employer Contributions
    private BigDecimal epfEmployer;
    private BigDecimal esicEmployer;
    private BigDecimal gratuity;

    // Net Take-Home
    private BigDecimal netSalary;
    private String status; // PAID, PROCESSED, HELD
    private LocalDate paymentDate;
    private String paymentMode;
    private String transactionReference;

    private Instant createdAt;
    private Instant updatedAt;
}
