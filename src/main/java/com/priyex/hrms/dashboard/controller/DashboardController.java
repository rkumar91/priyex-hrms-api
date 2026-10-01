package com.priyex.hrms.dashboard.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.dashboard.dto.DashboardStatsResponse;
import com.priyex.hrms.dashboard.service.DashboardService;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for real-time dashboard analytics, staff attendance, and leave statistics")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get real-time HR & workforce dashboard stats (staff, attendance, leaves, payroll)")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        DashboardStatsResponse stats = dashboardService.getDashboardStats(companyId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
