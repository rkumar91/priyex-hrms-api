package com.priyex.hrms.leave.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgLeavePolicy {
    private Long id;
    private Long companyId;
    private String leaveCode;
    private String leaveName;
    private Integer annualDays;
    private Boolean isPaid;
    private Boolean carryForwardAllowed;
    private Integer maxCarryForwardDays;
    private String description;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
