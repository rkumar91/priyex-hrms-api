package com.priyex.hrms.employee.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String requestType; // BANK_ACCOUNT, CONTACT_NAME, EMAIL, OTHER
    private String fieldName;
    private String currentValue;
    private String requestedValue;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private Long reviewerId;
    private String reviewerName;
    private String reviewerNotes;
    private Instant reviewedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
