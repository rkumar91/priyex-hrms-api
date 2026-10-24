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
public class SaasCustomerRequest {
    private Long id;
    private String requestRef;
    private Long accountId;
    private String accountName;
    private Long leadId;
    private String contactName;
    private String contactEmail;
    private String requestType; // SALES_QUERY, DEMO_TECHNICAL, FEATURE_REQUEST, INTEGRATION_CUSTOM, BILLING_QUERY, SECURITY_QUESTIONNAIRE
    private String title;
    private String description;
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private String status;   // NEW, TRIAGED, IN_PROGRESS, ANSWERED, DECLINED, CLOSED
    private String assignedTo;
    private LocalDate targetDate;
    private String resolutionNotes;
    private Instant createdAt;
    private Instant updatedAt;
}
