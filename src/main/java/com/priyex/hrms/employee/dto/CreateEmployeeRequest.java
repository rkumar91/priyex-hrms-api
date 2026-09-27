package com.priyex.hrms.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
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

    private BigDecimal annualCtc;

    // Personal & Corporate
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String maritalStatus;
    private String workLocation;

    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Permanent Address
    private String permanentAddressLine1;
    private String permanentAddressLine2;
    private String permanentCity;
    private String permanentState;
    private String permanentPostalCode;
    private String permanentCountry;

    // Banking
    private String bankName;
    private String bankBranch;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountType;

    // Statutory & PF
    private String panNumber;
    private String aadhaarNumber;
    private String pfNumber;
    private String uanNumber;
    private String esiNumber;
    private String pfNomineeName;
    private String pfNomineeRelationship;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;
}
