package com.priyex.hrms.saas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaasCommercialAccount {
    private Long id;
    private String accountRef;
    private String accountName;
    private String legalName;
    private String billingCountry;
    private String billingCurrency;
    private String businessDomain;
    private String primaryContactName;
    private String primaryContactEmail;
    private String primaryContactPhone;
    private String assignedOwner;
    private String lifecycleStatus; // PROSPECT, EVALUATION, CONTRACTING, ACTIVE, AT_RISK, OFFBOARDED, TERMINATED, CLOSED
    private Long linkedCompanyId;
    private String linkedCompanyCode;
    private String tenantSlug;
    private String industry;
    private String taxId;
    private String city;
    private String state;
    private String logoUrl;
    private Integer employeeCount;
    private Integer activeEmployeeCount;
    private Integer licensedSeats;
    private String activePlanName;
    private String activePlanCode;
    private String sourceLeadRef;
    private BigDecimal annualContractValue;
    private String dropReason;
    private String dropNotes;
    private LocalDate dropDate;
    private Instant createdAt;
    private Instant updatedAt;
}
