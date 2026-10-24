package com.priyex.hrms.saas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaasSubscription {
    private Long id;
    private String subscriptionRef;
    private Long accountId;
    private String accountName;
    private Long planId;
    private String planCode;
    private String planName;
    private String status; // PENDING, TRIAL, ACTIVE, PAST_DUE, GRACE_PERIOD, SUSPENDED, CANCELLED
    private Integer seatsLicensed;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate nextBillingDate;
    private Boolean autoRenew;
    private String paymentMethod;
    private Instant createdAt;
    private Instant updatedAt;
}
