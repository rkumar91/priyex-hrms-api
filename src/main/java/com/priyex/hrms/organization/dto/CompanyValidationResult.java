package com.priyex.hrms.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyValidationResult {
    private boolean valid;
    private Long companyId;
    private String companyCode;
    private String brandName;
    private String legalName;
    private String logoUrl;
    private String industry;
    private String message;
}
