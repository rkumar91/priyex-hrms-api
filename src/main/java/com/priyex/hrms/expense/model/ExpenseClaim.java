package com.priyex.hrms.expense.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseClaim {
    private Long id;
    private String claimNumber;
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String department;
    private String category; // TRAVEL, MEALS, LODGING, OFFICE_SUPPLIES, CLIENT_ENTERTAINMENT, MOBILE_INTERNET, TRAINING
    private String merchant;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;
    private String submittedAt;
    private String description;
    private String projectCode;
    private String gstin;
    private String receiptName;
    private String receiptUrl;
    private String status; // PENDING, APPROVED, REJECTED, REIMBURSED
    private String approverRemarks;
    private LocalDate reimbursedDate;
    private String reimbursementRef;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
