package com.priyex.hrms.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Work email is required")
    @Email(message = "Invalid email format")
    private String workEmail;

    private String personalPhone;
    private String workPhone;

    private Long departmentId;
    private Long designationId;
    private Long branchId;

    private String employmentType = "FULL_TIME";

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private String status = "ACTIVE";
}
