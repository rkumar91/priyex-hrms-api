package com.priyex.hrms.dashboard.service;

import com.priyex.hrms.dashboard.dto.DashboardStatsResponse;
import com.priyex.hrms.security.UserPrincipal;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats(Long companyId, UserPrincipal currentUser);
}
