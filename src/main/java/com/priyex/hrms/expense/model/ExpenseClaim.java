package com.priyex.hrms.expense.model;

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
    private String status; // PENDING_MANAGER, PENDING_HR, APPROVED, REJECTED_MANAGER, REJECTED_HR, REIMBURSED
    private Long managerId;
    private String managerName;
    private String managerComment;
    private Instant managerApprovedAt;
    private String hrComment;
    private Long hrApprovedBy;
    private Instant hrApprovedAt;
    private String approverRemarks;
    private LocalDate reimbursedDate;
    private String reimbursementRef;
    private Instant createdAt;
    private Instant updatedAt;
}
