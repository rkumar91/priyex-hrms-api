package com.priyex.hrms.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingOnboardingDto {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String workEmail;
    private String personalPhone;
    private Long departmentId;
    private String departmentName;
    private Long designationId;
    private String designationName;
    private LocalDate joiningDate;
    private String status; // ONBOARDING, PENDING_APPROVAL, ACTIVE
    private int documentCount;
    private boolean isSubmitted;
    private String panNumber;
    private String aadhaarNumber;
    private String bankAccountNumber;
    private String bankIfsc;
}
