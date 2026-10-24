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
public class SaasInvoice {
    private Long id;
    private String invoiceNumber;
    private Long accountId;
    private String accountName;
    private Long subscriptionId;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDate dueDate;
    private String status; // DRAFT, ISSUED, PAID, PAST_DUE, VOID
    private Instant paidAt;
    private String paymentReference;
    private Instant createdAt;
    private Instant updatedAt;
}
