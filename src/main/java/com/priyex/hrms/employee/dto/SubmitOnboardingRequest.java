package com.priyex.hrms.employee.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SubmitOnboardingRequest {
    // Personal Details
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private String maritalStatus;
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;

    // Address Details
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private String permanentAddressLine1;
    private String permanentAddressLine2;
    private String permanentCity;
    private String permanentState;
    private String permanentPostalCode;
    private String permanentCountry;

    // Banking & Statutory Details
    private String bankName;
    private String bankBranch;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountType;

    private String panNumber;
    private String aadhaarNumber;
    private String pfNumber;
    private String uanNumber;
    private String pfNomineeName;
    private String pfNomineeRelationship;
}
