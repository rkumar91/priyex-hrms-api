package com.priyex.hrms.lead.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadInquiry {
    private Long id;
    private String inquiryRef;
    private String fullName;
    private String workEmail;
    private String companyName;
    private String phoneNumber;
    private String employeeCount;
    private String interestModule;
    private String inquiryType;
    private String preferredDate;
    private String preferredTimeSlot;
    private String message;
    private String status; // NEW, QUALIFIED, DEMO_SCHEDULED, CONVERTED, CLOSED
    private String assignedConsultant;
    private String emailStatus; // PENDING, SENT, FAILED
    private String whatsappStatus; // PENDING, SENT, MOCK_SENT, SKIPPED, FAILED
    private Instant createdAt;
    private Instant updatedAt;
}
