package com.priyex.hrms.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientOverviewDto {
    private Long id;
    private String code;
    private String legalName;
    private String brandName;
    private String industry;
    private String timezone;
    private String currencyCode;
    private String email;
    private String phone;
    private String city;
    private String state;
    private String country;
    private String logoUrl;
    private Boolean isActive;
    private List<String> domains;
    private int employeeCount;
    private String adminEmail;
    private String onboardingStatus;
}
