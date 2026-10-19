package com.priyex.hrms.dashboard.service.impl;

import com.priyex.hrms.dashboard.dto.DashboardActivityDto;
import com.priyex.hrms.dashboard.dto.DashboardStatsResponse;
import com.priyex.hrms.dashboard.service.DashboardService;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.leave.mapper.LeaveMapper;
import com.priyex.hrms.leave.model.LeaveRequest;
import com.priyex.hrms.organization.dto.DepartmentDTO;
import com.priyex.hrms.organization.mapper.DepartmentMapper;
import com.priyex.hrms.payroll.dto.PayrollSummaryResponse;
import com.priyex.hrms.payroll.mapper.PayrollMapper;
import com.priyex.hrms.payroll.model.EmployeePayslip;
import com.priyex.hrms.payroll.model.PayrollRun;
import com.priyex.hrms.payroll.service.PayrollService;
import com.priyex.hrms.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final EmployeeMapper employeeMapper;
    private final LeaveMapper leaveMapper;
    private final DepartmentMapper departmentMapper;
    private final PayrollMapper payrollMapper;
    private final PayrollService payrollService;

    public DashboardServiceImpl(
            EmployeeMapper employeeMapper,
            LeaveMapper leaveMapper,
            DepartmentMapper departmentMapper,
            PayrollMapper payrollMapper,
            PayrollService payrollService
    ) {
        super();
        this.employeeMapper = employeeMapper;
        this.leaveMapper = leaveMapper;
        this.departmentMapper = departmentMapper;
        this.payrollMapper = payrollMapper;
        this.payrollService = payrollService;
    }

    @Override
    public DashboardStatsResponse getDashboardStats(Long companyId, UserPrincipal currentUser) {
        if (companyId == null) {
            companyId = 1L;
        }

        // 1. Employee Counts
        long totalStaff = employeeMapper.countEmployees(companyId, null, null, null);
        long activeStaff = employeeMapper.countEmployees(companyId, null, null, "ACTIVE");
        long statusOnLeave = employeeMapper.countEmployees(companyId, null, null, "ON_LEAVE");

        // 2. Real-time Leaves & Attendance
        List<LeaveRequest> allLeaves = leaveMapper.findAllByCompanyId(companyId);
        LocalDate today = LocalDate.now();

        long onLeaveToday = allLeaves.stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()))
                .filter(l -> l.getStartDate() != null && l.getEndDate() != null)
                .filter(l -> !today.isBefore(l.getStartDate()) && !today.isAfter(l.getEndDate()))
                .count();

        long effectiveOnLeave = Math.max(onLeaveToday, statusOnLeave);
        long presentToday = Math.max(0, activeStaff - onLeaveToday);
        if (presentToday > totalStaff) {
            presentToday = totalStaff;
        }

        double attendanceRate = totalStaff > 0
                ? Math.round(((double) presentToday / (double) totalStaff) * 1000.0) / 10.0
                : 0.0;
        double leaveRate = totalStaff > 0
                ? Math.round(((double) effectiveOnLeave / (double) totalStaff) * 1000.0) / 10.0
                : 0.0;

        long pendingLeaves = allLeaves.stream()
                .filter(l -> "PENDING".equalsIgnoreCase(l.getStatus()))
                .count();

        // 3. Departments
        List<DepartmentDTO> departments = departmentMapper.findAllByCompanyId(companyId);
        int totalDepartments = departments != null ? departments.size() : 0;

        // 4. Payroll Metrics
        BigDecimal grossEarnings = BigDecimal.ZERO;
        String payrollFormatted = "0.0 L";
        try {
            PayrollSummaryResponse payrollSummary = payrollService.getPayrollSummary(companyId);
            if (payrollSummary != null && payrollSummary.getGrossEarnings() != null) {
                grossEarnings = payrollSummary.getGrossEarnings();
                if (grossEarnings.compareTo(new BigDecimal("100000")) >= 0) {
                    BigDecimal inLakhs = grossEarnings.divide(new BigDecimal("100000"), 1, RoundingMode.HALF_UP);
                    payrollFormatted = inLakhs.toPlainString() + " L";
                } else {
                    payrollFormatted = NumberFormat.getCurrencyInstance(Locale.of("en", "IN")).format(grossEarnings);
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve payroll summary for company {}", companyId, e);
        }

        // 5. Employee Self-Service Metrics
        Long employeeId = currentUser != null ? currentUser.getEmployeeId() : null;
        int myLeaveBalance = 18;
        int myActiveRequests = 0;
        BigDecimal myLatestNetPay = new BigDecimal("82500.00");
        String myLatestNetPayFormatted = "82,500";

        if (employeeId != null) {
            // Count user's approved leave days this year
            int usedDays = allLeaves.stream()
                    .filter(l -> employeeId.equals(l.getEmployeeId()))
                    .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()))
                    .mapToInt(l -> l.getTotalDays() != null ? l.getTotalDays() : 1)
                    .sum();
            myLeaveBalance = Math.max(0, 18 - usedDays);

            // User's pending requests
            myActiveRequests = (int) allLeaves.stream()
                    .filter(l -> employeeId.equals(l.getEmployeeId()))
                    .filter(l -> "PENDING".equalsIgnoreCase(l.getStatus()))
                    .count();

            // User's latest payslip
            try {
                List<EmployeePayslip> payslips = payrollMapper.findPayslipsByEmployeeId(employeeId);
                if (payslips != null && !payslips.isEmpty()) {
                    EmployeePayslip latest = payslips.get(0);
                    if (latest.getNetSalary() != null) {
                        myLatestNetPay = latest.getNetSalary();
                        myLatestNetPayFormatted = NumberFormat.getNumberInstance(Locale.of("en", "IN")).format(myLatestNetPay);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch user payslip", e);
            }
        }

        // 6. Real-time Activities List
        List<DashboardActivityDto> activities = new ArrayList<>();
        boolean isManagerOrAdmin = currentUser != null && currentUser.hasAnyRole("ADMIN", "SUPER_ADMIN", "HR_ADMIN", "MANAGER");

        if (isManagerOrAdmin) {
            // Admin / HR: Live system-wide activities
            // Add recent employees
            List<Employee> recentEmployees = employeeMapper.searchEmployees(companyId, null, null, null, 0, 3);
            if (recentEmployees != null) {
                for (Employee emp : recentEmployees) {
                    activities.add(DashboardActivityDto.builder()
                            .id((long) (activities.size() + 1))
                            .type("Employee Onboarded")
                            .title(emp.getFirstName() + " " + emp.getLastName() + " enrolled (" + emp.getEmployeeCode() + ")")
                            .time(emp.getCreatedAt() != null ? formatRelativeTime(emp.getCreatedAt()) : "Recently")
                            .icon("UserPlus")
                            .iconColor("text-emerald-600 bg-emerald-50")
                            .build());
                }
            }

            // Add recent leave requests
            if (allLeaves != null) {
                allLeaves.stream().limit(3).forEach(lr -> {
                    boolean isApproved = "APPROVED".equalsIgnoreCase(lr.getStatus());
                    activities.add(DashboardActivityDto.builder()
                            .id((long) (activities.size() + 1))
                            .type(isApproved ? "Leave Approved" : "Leave Requested")
                            .title((lr.getEmployeeName() != null ? lr.getEmployeeName() : "Employee") + " - " + lr.getLeaveType() + " (" + lr.getTotalDays() + " Days)")
                            .time(lr.getCreatedAt() != null ? formatRelativeTime(lr.getCreatedAt()) : "Recently")
                            .icon(isApproved ? "CheckCircle2" : "Clock")
                            .iconColor(isApproved ? "text-teal-600 bg-teal-50" : "text-amber-600 bg-amber-50")
                            .build());
                });
            }

            // Add recent payroll runs
            try {
                List<PayrollRun> runs = payrollMapper.findRunsByCompanyId(companyId);
                if (runs != null) {
                    runs.stream().limit(2).forEach(pr -> {
                        activities.add(DashboardActivityDto.builder()
                                .id((long) (activities.size() + 1))
                                .type("Payroll Run")
                                .title("Payroll Run for " + pr.getTotalEmployees() + " employees (" + pr.getStatus() + ")")
                                .time(pr.getCreatedAt() != null ? formatRelativeTime(pr.getCreatedAt()) : "Recently")
                                .icon("FileCheck")
                                .iconColor("text-cyan-600 bg-cyan-50")
                                .build());
                    });
                }
            } catch (Exception e) {
                log.warn("Could not fetch payroll runs for activities", e);
            }
        } else {
            // Employee Self-Service: strictly own activities
            if (employeeId != null) {
                // User's own leave requests
                if (allLeaves != null) {
                    allLeaves.stream()
                            .filter(l -> employeeId.equals(l.getEmployeeId()))
                            .limit(3)
                            .forEach(lr -> {
                                boolean isApproved = "APPROVED".equalsIgnoreCase(lr.getStatus());
                                boolean isRejected = "REJECTED".equalsIgnoreCase(lr.getStatus());
                                String type = isApproved ? "Leave Approved" : (isRejected ? "Leave Rejected" : "Leave Requested");
                                activities.add(DashboardActivityDto.builder()
                                        .id((long) (activities.size() + 1))
                                        .type(type)
                                        .title(lr.getLeaveType() + " (" + lr.getTotalDays() + " Days) - " + lr.getStatus())
                                        .time(lr.getCreatedAt() != null ? formatRelativeTime(lr.getCreatedAt()) : "Recently")
                                        .icon(isApproved ? "CheckCircle2" : (isRejected ? "AlertTriangle" : "Clock"))
                                        .iconColor(isApproved ? "text-teal-600 bg-teal-50" : (isRejected ? "text-rose-600 bg-rose-50" : "text-amber-600 bg-amber-50"))
                                        .build());
                            });
                }

                // User's own payslips
                try {
                    List<EmployeePayslip> payslips = payrollMapper.findPayslipsByEmployeeId(employeeId);
                    if (payslips != null && !payslips.isEmpty()) {
                        payslips.stream().limit(2).forEach(ps -> {
                            String period = ps.getPayPeriod() != null ? ps.getPayPeriod() : (ps.getPayrollMonth() != null ? ps.getPayrollMonth() + "/" + ps.getPayrollYear() : "Monthly");
                            activities.add(DashboardActivityDto.builder()
                                    .id((long) (activities.size() + 1))
                                    .type("Payslip Released")
                                    .title("Monthly Salary Statement (" + period + ") ready for download")
                                    .time(ps.getCreatedAt() != null ? formatRelativeTime(ps.getCreatedAt()) : "Recently")
                                    .icon("FileCheck")
                                    .iconColor("text-cyan-600 bg-cyan-50")
                                    .build());
                        });
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch employee payslips for personal activities", e);
                }

                // User's own enrollment milestone
                try {
                    employeeMapper.findById(employeeId, companyId).ifPresent(emp -> {
                        activities.add(DashboardActivityDto.builder()
                                .id((long) (activities.size() + 1))
                                .type("Employee Onboarded")
                                .title(emp.getFirstName() + " " + emp.getLastName() + " enrolled (" + emp.getEmployeeCode() + ")")
                                .time(emp.getCreatedAt() != null ? formatRelativeTime(emp.getCreatedAt()) : "Recently")
                                .icon("UserPlus")
                                .iconColor("text-emerald-600 bg-emerald-50")
                                .build());
                    });
                } catch (Exception e) {
                    log.warn("Could not fetch employee enrollment for personal activities", e);
                }
            }
        }

        return DashboardStatsResponse.builder()
                .totalStaff(totalStaff)
                .activeStaff(activeStaff)
                .presentToday(presentToday)
                .staffOnLeave(effectiveOnLeave)
                .attendanceRate(attendanceRate)
                .leaveRate(leaveRate)
                .monthlyPayroll(grossEarnings)
                .monthlyPayrollFormatted(payrollFormatted)
                .totalDepartments(totalDepartments)
                .pendingLeaves(pendingLeaves)
                .myDaysPresent(21)
                .myTotalWorkingDays(22)
                .myLeaveBalance(myLeaveBalance)
                .myLatestNetPay(myLatestNetPay)
                .myLatestNetPayFormatted(myLatestNetPayFormatted)
                .myActiveRequests(myActiveRequests)
                .recentActivities(activities)
                .build();
    }

    private String formatRelativeTime(Instant instant) {
        if (instant == null) return "Recently";
        Duration diff = Duration.between(instant, Instant.now());
        long mins = diff.toMinutes();
        if (mins < 1) return "Just now";
        if (mins < 60) return mins + " mins ago";
        long hours = diff.toHours();
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        long days = diff.toDays();
        if (days == 1) return "Yesterday";
        return days + " days ago";
    }
}
