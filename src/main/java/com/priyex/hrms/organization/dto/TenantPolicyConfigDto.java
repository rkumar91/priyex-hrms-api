package com.priyex.hrms.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enterprise client policy engine configuration DTO.
 * Stores customizable rules per tenant (TCS, Wipro, Infosys, Priyex, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPolicyConfigDto {

    private Long companyId;
    private String companyCode;
    private String companyName;

    // ── Leave Policy Rules ──
    private String leaveCalendarCycle;      // 'CALENDAR_YEAR' (Jan-Dec) or 'FINANCIAL_YEAR' (Apr-Mar)
    private String leaveSandwichRule;        // 'ENABLED' or 'DISABLED'
    private Boolean probationLeaveAllowed;   // true / false

    // ── Attendance & Shift Rules ──
    private Integer attendanceGraceMins;     // e.g. 15, 20, 30 mins
    private Double halfDayMinHours;          // e.g. 4.0 or 4.5 hrs
    private Double fullDayMinHours;          // e.g. 8.0 or 8.5 hrs
    private String geofenceEnforcement;      // 'STRICT', 'FLEXIBLE', 'DISABLED'

    // ── Overtime & C-Off Rules ──
    private Double overtimeMultiplierWeekday;// e.g. 1.5x
    private Double overtimeMultiplierWeekend;// e.g. 2.0x
    private Integer coffValidityDays;        // e.g. 60, 90, 120 days
    private Double coffMinHoursFullDay;      // e.g. 8.0 hrs

    // ── Approval Routing Hierarchy ──
    private Integer approvalTierCount;       // 1 (Direct HR), 2 (Manager->HR), 3 (Lead->Manager->HR)
}
