package com.priyex.hrms.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardClientRequest {

    @NotBlank(message = "Company code is required (e.g. ACME)")
    @Size(min = 2, max = 20, message = "Company code must be between 2 and 20 characters")
    private String code;

    @NotBlank(message = "Legal company name is required")
    private String legalName;

    private String brandName;

    private String industry;

    private List<String> domains;

    @NotBlank(message = "Initial Admin email is required")
    @Email(message = "Valid administrator email address is required")
    private String adminEmail;

    @NotBlank(message = "Initial Admin full name is required")
    private String adminName;

    private String adminPassword;

    private String phone;

    private String city;

    private String state;

    private String country;
    private String logoUrl;
}
