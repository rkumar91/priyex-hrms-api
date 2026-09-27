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
public class HrQuery {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private Long assignedHrId;
    private String assignedHrName;
    private String assignedHrEmail;
    private String category; // PAYROLL, PF_UAN, LEAVE, TAX, GENERAL
    private String subject;
    private String message;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    private String hrResponse;
    private Instant respondedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
