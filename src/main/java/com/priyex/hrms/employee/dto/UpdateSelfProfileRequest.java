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
public class UpdateSelfProfileRequest {
    private String photoUrl;
    private String personalPhone;
    private String personalEmail;
    private String bloodGroup;
    private String maritalStatus;
    private LocalDate dateOfBirth;

    // Current Address
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

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;
}
