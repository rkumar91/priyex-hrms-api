package com.priyex.hrms.organization.service.impl;

import com.priyex.hrms.organization.dto.CompanyDto;
import com.priyex.hrms.organization.dto.TenantPolicyConfigDto;
import com.priyex.hrms.organization.mapper.CompanyMapper;
import com.priyex.hrms.organization.mapper.CompanySettingMapper;
import com.priyex.hrms.organization.model.CompanySetting;
import com.priyex.hrms.organization.service.TenantPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TenantPolicyServiceImpl implements TenantPolicyService {

    private final CompanySettingMapper companySettingMapper;
    private final CompanyMapper companyMapper;

    public TenantPolicyServiceImpl(CompanySettingMapper companySettingMapper, CompanyMapper companyMapper) {
        super();
        this.companySettingMapper = companySettingMapper;
        this.companyMapper = companyMapper;
    }

    @Override
    public TenantPolicyConfigDto getPolicyConfig(Long companyId) {
        Long targetId = (companyId != null) ? companyId : 1L;

        List<CompanySetting> settingsList = companySettingMapper.findAllByCompanyId(targetId);
        Map<String, String> map = new HashMap<>();
        for (CompanySetting cs : settingsList) {
            map.put(cs.getSettingKey(), cs.getSettingValue());
        }

        CompanyDto company = companyMapper.findById(targetId);
        String companyCode = (company != null) ? company.getCode() : "PRIYEX";
        String companyName = (company != null) ? company.getLegalName() : "Priyex Technologies";

        return TenantPolicyConfigDto.builder()
                .companyId(targetId)
                .companyCode(companyCode)
                .companyName(companyName)
                .leaveCalendarCycle(map.getOrDefault("LEAVE_CALENDAR_CYCLE", "CALENDAR_YEAR"))
                .leaveSandwichRule(map.getOrDefault("LEAVE_SANDWICH_RULE", "DISABLED"))
                .probationLeaveAllowed(Boolean.parseBoolean(map.getOrDefault("PROBATION_LEAVE_ALLOWED", "true")))
                .attendanceGraceMins(parseInteger(map.get("ATTENDANCE_GRACE_MINS"), 20))
                .halfDayMinHours(parseDouble(map.get("HALF_DAY_MIN_HOURS"), 4.0))
                .fullDayMinHours(parseDouble(map.get("FULL_DAY_MIN_HOURS"), 8.0))
                .geofenceEnforcement(map.getOrDefault("GEOFENCE_ENFORCEMENT", "STRICT"))
                .overtimeMultiplierWeekday(parseDouble(map.get("OVERTIME_MULTIPLIER_WEEKDAY"), 1.5))
                .overtimeMultiplierWeekend(parseDouble(map.get("OVERTIME_MULTIPLIER_WEEKEND"), 2.0))
                .coffValidityDays(parseInteger(map.get("COFF_VALIDITY_DAYS"), 60))
                .coffMinHoursFullDay(parseDouble(map.get("COFF_MIN_HOURS_FULL_DAY"), 8.0))
                .approvalTierCount(parseInteger(map.get("APPROVAL_TIER_COUNT"), 2))
                .build();
    }

    @Override
    @Transactional
    public TenantPolicyConfigDto updatePolicyConfig(Long companyId, TenantPolicyConfigDto updateDto) {
        Long targetId = (companyId != null) ? companyId : 1L;

        if (updateDto.getLeaveCalendarCycle() != null) {
            companySettingMapper.upsert(targetId, "LEAVE_CALENDAR_CYCLE", updateDto.getLeaveCalendarCycle(), "Leave quota cycle: CALENDAR_YEAR or FINANCIAL_YEAR");
        }
        if (updateDto.getLeaveSandwichRule() != null) {
            companySettingMapper.upsert(targetId, "LEAVE_SANDWICH_RULE", updateDto.getLeaveSandwichRule(), "Sandwich rule: if enabled, weekends falling between leave days count as leaves");
        }
        if (updateDto.getProbationLeaveAllowed() != null) {
            companySettingMapper.upsert(targetId, "PROBATION_LEAVE_ALLOWED", String.valueOf(updateDto.getProbationLeaveAllowed()), "Whether employees in probation period can apply for leaves");
        }
        if (updateDto.getAttendanceGraceMins() != null) {
            companySettingMapper.upsert(targetId, "ATTENDANCE_GRACE_MINS", String.valueOf(updateDto.getAttendanceGraceMins()), "Grace period in minutes allowed after shift start before marked late");
        }
        if (updateDto.getHalfDayMinHours() != null) {
            companySettingMapper.upsert(targetId, "HALF_DAY_MIN_HOURS", String.valueOf(updateDto.getHalfDayMinHours()), "Minimum hours worked to qualify for half-day credit");
        }
        if (updateDto.getFullDayMinHours() != null) {
            companySettingMapper.upsert(targetId, "FULL_DAY_MIN_HOURS", String.valueOf(updateDto.getFullDayMinHours()), "Minimum hours worked to qualify for full-day credit");
        }
        if (updateDto.getGeofenceEnforcement() != null) {
            companySettingMapper.upsert(targetId, "GEOFENCE_ENFORCEMENT", updateDto.getGeofenceEnforcement(), "Geofencing check strictness: STRICT, FLEXIBLE, or DISABLED");
        }
        if (updateDto.getOvertimeMultiplierWeekday() != null) {
            companySettingMapper.upsert(targetId, "OVERTIME_MULTIPLIER_WEEKDAY", String.valueOf(updateDto.getOvertimeMultiplierWeekday()), "Overtime wage rate multiplier for weekdays");
        }
        if (updateDto.getOvertimeMultiplierWeekend() != null) {
            companySettingMapper.upsert(targetId, "OVERTIME_MULTIPLIER_WEEKEND", String.valueOf(updateDto.getOvertimeMultiplierWeekend()), "Overtime wage rate multiplier for weekends/holidays");
        }
        if (updateDto.getCoffValidityDays() != null) {
            companySettingMapper.upsert(targetId, "COFF_VALIDITY_DAYS", String.valueOf(updateDto.getCoffValidityDays()), "Calendar days compensatory-off credit remains valid before auto-expiring");
        }
        if (updateDto.getCoffMinHoursFullDay() != null) {
            companySettingMapper.upsert(targetId, "COFF_MIN_HOURS_FULL_DAY", String.valueOf(updateDto.getCoffMinHoursFullDay()), "Minimum holiday/weekend hours worked to earn 1.0 C-Off day credit");
        }
        if (updateDto.getApprovalTierCount() != null) {
            companySettingMapper.upsert(targetId, "APPROVAL_TIER_COUNT", String.valueOf(updateDto.getApprovalTierCount()), "Number of approval tiers for requests: 1 (Direct HR), 2 (Manager->HR), 3 (Lead->Manager->HR)");
        }

        return getPolicyConfig(targetId);
    }

    @Override
    public List<CompanyDto> getAllClients() {
        return companyMapper.findAllActiveCompanies();
    }

    private Integer parseInteger(String val, Integer fallback) {
        if (val == null || val.isBlank()) return fallback;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Double parseDouble(String val, Double fallback) {
        if (val == null || val.isBlank()) return fallback;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
