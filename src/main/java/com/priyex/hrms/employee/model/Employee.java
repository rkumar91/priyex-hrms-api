package com.priyex.hrms.employee.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    private Long id;
    private Long companyId;
    private String employeeCode;
    private Long userId;

    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String maritalStatus;
    private String bloodGroup;
    private String nationality;
    private String photoUrl;

    private String personalEmail;
    private String workEmail;
    private String personalPhone;
    private String workPhone;

    private Long departmentId;
    private Long designationId;
    private Long branchId;
    private Long locationId;
    private Long teamId;
    private Long jobGradeId;
    private Long costCenterId;
    private Long reportingManagerId;

    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, INTERN
    private LocalDate joiningDate;
    private LocalDate confirmationDate;
    private LocalDate probationEndDate;
    private Integer noticePeriodDays;

    private String status; // ACTIVE, PROBATION, ON_LEAVE, EXITED

    // Complete Address Details
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

    // Financial & Banking
    private String bankName;
    private String bankBranch;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountType;

    // PF & Statutory
    private String pfNumber;
    private String uanNumber;
    private String esiNumber;
    private String panNumber;
    private String aadhaarNumber;
    private String pfNomineeName;
    private String pfNomineeRelationship;
    private LocalDate pfJoiningDate;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;

    // Corporate Work Location
    private String workLocation;

    // Joined names for convenience
    private String departmentName;
    private String designationName;
    private String branchName;
    private String reportingManagerName;

    private Instant createdAt;
    private Long createdBy;
    private Instant updatedAt;
    private Long updatedBy;
    private Integer version;
}
