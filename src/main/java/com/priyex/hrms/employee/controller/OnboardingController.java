package com.priyex.hrms.employee.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.dto.InitiateOnboardingRequest;
import com.priyex.hrms.employee.dto.InitiateOnboardingResponse;
import com.priyex.hrms.employee.dto.PendingOnboardingDto;
import com.priyex.hrms.employee.dto.SubmitOnboardingRequest;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.employee.service.OnboardingService;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/employees/onboarding")
@RequiredArgsConstructor
@Tag(name = "Employee Onboarding Workflow", description = "Endpoints for initiating, submitting, reviewing, and approving employee onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('emp.create')")
    @Operation(summary = "HR initiates onboarding by creating employee record, credentials, and triggering invite email")
    public ResponseEntity<ApiResponse<InitiateOnboardingResponse>> initiateOnboarding(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody InitiateOnboardingRequest request,
            HttpServletRequest httpRequest
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorId = (currentUser != null) ? currentUser.getId() : 1L;

        String origin = httpRequest.getHeader("Origin");
        if (origin == null || origin.isBlank()) {
            origin = httpRequest.getHeader("Referer");
        }
        if (origin != null && origin.endsWith("/")) {
            origin = origin.substring(0, origin.length() - 1);
        }

        InitiateOnboardingResponse response = onboardingService.initiateOnboarding(companyId, actorId, request, origin);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, response.getMessage()));
    }

    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Employee submits complete onboarding profile details for HR verification")
    public ResponseEntity<ApiResponse<Employee>> submitOnboarding(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody SubmitOnboardingRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long userId = (currentUser != null) ? currentUser.getId() : null;
        Long employeeId = (currentUser != null) ? currentUser.getEmployeeId() : null;

        Employee employee = onboardingService.submitOnboarding(companyId, userId, employeeId, request);
        return ResponseEntity.ok(ApiResponse.success(employee, "Onboarding information submitted successfully for HR approval"));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('emp.view')")
    @Operation(summary = "List all pending employee onboarding submissions awaiting HR review")
    public ResponseEntity<ApiResponse<List<PendingOnboardingDto>>> getPendingOnboardings(
            @CurrentUser UserPrincipal currentUser
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        List<PendingOnboardingDto> list = onboardingService.getPendingOnboardings(companyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('emp.edit')")
    @Operation(summary = "HR approves onboarding application and activates employee account")
    public ResponseEntity<ApiResponse<Employee>> approveOnboarding(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorId = (currentUser != null) ? currentUser.getId() : 1L;
        String remarks = (body != null) ? body.get("remarks") : "Approved by HR";

        Employee employee = onboardingService.approveOnboarding(companyId, actorId, id, remarks);
        return ResponseEntity.ok(ApiResponse.success(employee, "Onboarding approved. Employee is now ACTIVE."));
    }

    @PostMapping("/{id}/request-revisions")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN') or hasAuthority('emp.edit')")
    @Operation(summary = "HR requests revisions from employee regarding submitted documents or details")
    public ResponseEntity<ApiResponse<Employee>> requestRevisions(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorId = (currentUser != null) ? currentUser.getId() : 1L;
        String feedback = (body != null) ? body.get("feedback") : "Please review and update required details";

        Employee employee = onboardingService.requestRevisions(companyId, actorId, id, feedback);
        return ResponseEntity.ok(ApiResponse.success(employee, "Revision request sent to employee"));
    }
}
