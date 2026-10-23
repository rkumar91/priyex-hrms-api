package com.priyex.hrms.separation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExitClearance {
    private Long id;
    private Long separationCaseId;
    private Long companyId;
    private String departmentType;     // REPORTING_MANAGER, IT_INFRA, ASSETS_ADMIN, FINANCE_ACCOUNTS, HR_OPS
    private String title;
    private String description;
    private String status;             // PENDING, CLEARED, WAIVED, FLAGGED
    private Long clearedByUserId;
    private String clearedByUserName;
    private LocalDateTime clearedAt;
    private BigDecimal deductionAmount;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
