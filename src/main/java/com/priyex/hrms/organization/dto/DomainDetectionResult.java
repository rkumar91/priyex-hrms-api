package com.priyex.hrms.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainDetectionResult {
    private boolean identified;
    private boolean shared;
    private String domain;
    private CompanyDto company;
    private String message;
}
