package com.priyex.hrms.organization.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDomain {
    private Long id;
    private Long companyId;
    private String domainName;
    private Boolean isPrimary;
    private Boolean isVerified;
    private Boolean isShared;
    private Instant createdAt;
    private Instant updatedAt;
}
