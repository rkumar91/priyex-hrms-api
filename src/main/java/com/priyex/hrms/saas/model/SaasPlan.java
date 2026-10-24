package com.priyex.hrms.saas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaasPlan {
    private Long id;
    private String planCode; // STARTER, GROWTH, ENTERPRISE
    private String planName;
    private String version;
    private String billingInterval; // MONTHLY, ANNUAL
    private String currency;
    private BigDecimal pricePerSeat;
    private Integer minSeats;
    private String featuresSummary;
    private Boolean isActive;
    private Instant createdAt;
}
