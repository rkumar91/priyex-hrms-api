package com.priyex.hrms.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityDto {
    private Long id;
    private String type;
    private String title;
    private String time;
    private String icon;      // e.g. "UserPlus", "CheckCircle2", "FileCheck", "Clock", "AlertTriangle"
    private String iconColor; // e.g. "text-emerald-600 bg-emerald-50"
}
