package com.priyex.hrms.separation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeparationPolicy {
    private Long id;
    private Long companyId;
    private Integer defaultNoticeDays;
    private Integer probationNoticeDays;
    private Boolean allowResignationWithdrawal;
    private Boolean autoDeactivateUser;
    private Boolean requireManagerApproval;
    private Boolean requireItClearance;
    private Boolean requireFinanceClearance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
