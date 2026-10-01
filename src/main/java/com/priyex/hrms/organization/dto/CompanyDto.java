package com.priyex.hrms.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    private String code;
    private String legalName;
    private String brandName;
    private String industry;
    private String timezone;
    private String currencyCode;
    private String registrationNo;
    private String taxId;
    private String email;
    private String phone;
    private String city;
    private String state;
    private String country;
    private Boolean isActive;
}
