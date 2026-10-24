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
public class SaasContract {
    private Long id;
    private String contractRef;
    private Long accountId;
    private String accountName;
    private String contractType; // ONE_TIME_SETUP, ANNUAL_MAINTENANCE, PREMIUM_SLA, CUSTOM_DEVELOPMENT
    private String title;
    private BigDecimal amount;
    private String currency;
    private String billingStatus; // PENDING, INVOICED, PAID, WAIVED
    private LocalDate startDate;
    private LocalDate endDate;
    private String termsSummary;
    private Long invoiceId;
    private Instant createdAt;
    private Instant updatedAt;
}
