package com.priyex.hrms.leave.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrgLeavePolicyRequest {
    @NotBlank(message = "Leave code is required (e.g. CL, SL, PL)")
    private String leaveCode;

    @NotBlank(message = "Leave name is required")
    private String leaveName;

    @NotNull(message = "Annual days is required")
    @Min(value = 0, message = "Annual days cannot be negative")
    private Integer annualDays;

    private Boolean isPaid = true;
    private Boolean carryForwardAllowed = false;
    private Integer maxCarryForwardDays = 0;
    private String description;
    private Boolean isActive = true;
}
