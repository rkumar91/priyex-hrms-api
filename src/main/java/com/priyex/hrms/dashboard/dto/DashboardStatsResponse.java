package com.priyex.hrms.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    // Admin / HR Management stats
    private long totalStaff;
    private long activeStaff;
    private long presentToday;
    private long staffOnLeave;
    private double attendanceRate;
    private double leaveRate;
    private BigDecimal monthlyPayroll;
    private String monthlyPayrollFormatted;
    private int totalDepartments;
    private long pendingLeaves;

    // Self-service metrics for current logged-in employee
    private int myDaysPresent;
    private int myTotalWorkingDays;
    private int myLeaveBalance;
    private BigDecimal myLatestNetPay;
    private String myLatestNetPayFormatted;
    private int myActiveRequests;

    // Live real-time activities
    private List<DashboardActivityDto> recentActivities;
}
