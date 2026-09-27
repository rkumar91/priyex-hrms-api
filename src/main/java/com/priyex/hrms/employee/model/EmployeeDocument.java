package com.priyex.hrms.employee.model;

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
public class EmployeeDocument {
    private Long id;
    private Long employeeId;
    private String documentType; // AADHAAR_CARD, PAN_CARD, PASSPORT, DEGREE_CERTIFICATE, PREVIOUS_RELIEVING, CANCELLED_CHEQUE, OTHER
    private String documentName;
    private String fileKey;
    private Long fileSize;
    private String mimeType;
    private String fileData; // Base64 data URI for direct inline preview/download
    private LocalDate expiryDate;
    private boolean verified;
    private Long verifiedBy;
    private Instant verifiedAt;
    private String remarks;
    private Instant createdAt;
    private Long createdBy;
    private Instant updatedAt;
}
