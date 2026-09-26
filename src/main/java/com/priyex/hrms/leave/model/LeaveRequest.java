package com.priyex.hrms.leave.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    private Long id;
    private Long companyId;
    private Long employeeId;
    private String employeeName;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private String approverComment;
    private Long approvedBy;
    private Instant approvedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
