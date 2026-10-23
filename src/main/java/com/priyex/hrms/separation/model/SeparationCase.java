package com.priyex.hrms.separation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeparationCase {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private String caseType;            // RESIGNATION, TERMINATION, MUTUAL_SEPARATION, END_OF_CONTRACT
    private String initiatorRole;       // EMPLOYEE, HR_ADMIN, SUPER_ADMIN, MANAGER
    private Long initiatorUserId;
    private String reasonCategory;      // CAREER_GROWTH, PERSONAL_REASONS, BETTER_OFFER, RELOCATION, HIGHER_STUDIES, PERFORMANCE, MISCONDUCT, REDUNDANCY, RETIREMENT, OTHER
    private String reasonDetails;
    private LocalDate submissionDate;
    private LocalDate requestedLastWorkingDay;
    private LocalDate approvedLastWorkingDay;
    private Integer noticePeriodDays;
    private Boolean isNoticeWaived;
    private Integer noticeShortfallDays;
    private String status;              // SUBMITTED, NOTICE_PERIOD, CLEARANCE_PENDING, SETTLEMENT_PENDING, EXITED, WITHDRAWN, REJECTED
    private String handoverNotes;

    // Manager multi-tier review
    private String managerApprovalStatus; // PENDING, APPROVED, REJECTED, SKIPPED
    private String managerRemarks;
    private LocalDateTime managerReviewedAt;
    private Long managerReviewedBy;

    // HR Operations review
    private String hrApprovalStatus;      // PENDING, APPROVED, REJECTED
    private String hrRemarks;
    private LocalDateTime hrReviewedAt;
    private Long hrReviewedBy;

    // Exit details
    private Boolean exitInterviewCompleted;
    private String exitInterviewNotes;
    private LocalDateTime userDeactivatedAt;
    private Long finalizedBy;
    private LocalDateTime finalizedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Joined Employee fields
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String workEmail;
    private String departmentName;
    private String designationTitle;
    private String reportingManagerName;

    // Clearances list
    private List<ExitClearance> clearances;
}
