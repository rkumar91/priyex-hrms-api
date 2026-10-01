package com.priyex.hrms.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InitiateOnboardingRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Must be a valid email format")
    private String workEmail;

    @NotBlank(message = "Phone number is required")
    private String personalPhone;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Designation ID is required")
    private Long designationId;

    private Long branchId;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private String employmentType = "FULL_TIME";
    private String workLocation = "Bangalore HQ";
    private BigDecimal annualCtc = new BigDecimal("1200000.00");
}
