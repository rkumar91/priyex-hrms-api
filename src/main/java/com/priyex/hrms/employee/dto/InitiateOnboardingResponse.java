package com.priyex.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateOnboardingResponse {
    private Long employeeId;
    private Long userId;
    private String employeeCode;
    private String fullName;
    private String workEmail;
    private String tempPassword;
    private String inviteUrl;
    private String status;
    private String message;
}
