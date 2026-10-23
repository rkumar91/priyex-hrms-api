package com.priyex.hrms.separation.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import com.priyex.hrms.separation.dto.*;
import com.priyex.hrms.separation.model.ExitClearance;
import com.priyex.hrms.separation.model.SeparationCase;
import com.priyex.hrms.separation.model.SeparationPolicy;
import com.priyex.hrms.separation.service.SeparationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/separation")
@RequiredArgsConstructor
@Tag(name = "Employee Separation & Exit Lifecycle", description = "Endpoints for employee resignation, admin termination, clearances, and access deactivation")
public class SeparationController {

    private final SeparationService separationService;

    @PostMapping("/resignation")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit employee self-service resignation")
    public ResponseEntity<ApiResponse<SeparationCase>> submitResignation(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody SubmitResignationRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = (currentUser != null && currentUser.getEmployeeId() != null) ? currentUser.getEmployeeId() : 1L;
        Long userId = currentUser != null ? currentUser.getId() : 1L;

        SeparationCase result = separationService.submitResignation(companyId, employeeId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Resignation submitted successfully. Notice period initiated."));
    }

    @PostMapping("/resignation/withdraw")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Withdraw a submitted resignation")
    public ResponseEntity<ApiResponse<SeparationCase>> withdrawResignation(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(name = "separationId") Long separationId
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = (currentUser != null && currentUser.getEmployeeId() != null) ? currentUser.getEmployeeId() : 1L;

        SeparationCase result = separationService.withdrawResignation(companyId, separationId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(result, "Resignation withdrawn successfully. Active status restored."));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get currently logged-in employee's active separation case")
    public ResponseEntity<ApiResponse<SeparationCase>> getMySeparation(
            @CurrentUser UserPrincipal currentUser
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = (currentUser != null && currentUser.getEmployeeId() != null) ? currentUser.getEmployeeId() : 1L;

        SeparationCase result = separationService.getMySeparation(companyId, employeeId);
        return ResponseEntity.ok(ApiResponse.success(result, "My separation record retrieved"));
    }

    @PostMapping("/terminate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Initiate employee separation / termination (Admin/HR only)")
    public ResponseEntity<ApiResponse<SeparationCase>> initiateTermination(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody InitiateTerminationRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long adminUserId = currentUser != null ? currentUser.getId() : 1L;

        SeparationCase result = separationService.initiateTermination(companyId, adminUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Employee separation initiated and clearance checklist created."));
    }

    @GetMapping("/cases")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HR_ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "List all separation cases across the organization")
    public ResponseEntity<ApiResponse<List<SeparationCase>>> getSeparationCases(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "employeeId", required = false) Long employeeId
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;

        List<SeparationCase> cases = separationService.getSeparationCases(companyId, status, employeeId);
        return ResponseEntity.ok(ApiResponse.success(cases, "Separation cases retrieved"));
    }

    @GetMapping("/cases/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get details of a specific separation case")
    public ResponseEntity<ApiResponse<SeparationCase>> getSeparationById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable(name = "id") Long id
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;

        SeparationCase result = separationService.getSeparationById(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(result, "Separation case details retrieved"));
    }

    @PutMapping("/cases/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HR_ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Review (approve/reject/negotiate LWD) a resignation case")
    public ResponseEntity<ApiResponse<SeparationCase>> reviewSeparation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody ReviewSeparationRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long reviewerUserId = currentUser != null ? currentUser.getId() : 1L;
        boolean isHr = currentUser != null && (currentUser.hasRole("SUPER_ADMIN") || currentUser.hasRole("ADMIN") || currentUser.hasRole("HR_ADMIN"));

        SeparationCase result = separationService.reviewSeparation(companyId, id, reviewerUserId, isHr, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Separation review updated successfully"));
    }

    @PutMapping("/clearances/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HR_ADMIN', 'HR_MANAGER', 'MANAGER')")
    @Operation(summary = "Update departmental exit clearance status (IT, Finance, Manager, Assets, HR)")
    public ResponseEntity<ApiResponse<ExitClearance>> updateClearance(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdateClearanceRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long reviewerUserId = currentUser != null ? currentUser.getId() : 1L;

        ExitClearance result = separationService.updateClearance(companyId, id, reviewerUserId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Clearance updated successfully"));
    }

    @PostMapping("/cases/{id}/finalize")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Finalize employee exit, mark EXITED, and automatically deactivate system access")
    public ResponseEntity<ApiResponse<SeparationCase>> finalizeExit(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable(name = "id") Long id,
            @RequestBody(required = false) FinalizeExitRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long adminUserId = currentUser != null ? currentUser.getId() : 1L;

        if (request == null) {
            request = new FinalizeExitRequest();
        }

        SeparationCase result = separationService.finalizeExit(companyId, id, adminUserId, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Employee exit finalized. Record marked EXITED and user login deactivated."));
    }

    @GetMapping("/policy")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get organizational separation policy")
    public ResponseEntity<ApiResponse<SeparationPolicy>> getPolicy(
            @CurrentUser UserPrincipal currentUser
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        SeparationPolicy policy = separationService.getPolicy(companyId);
        return ResponseEntity.ok(ApiResponse.success(policy, "Separation policy retrieved"));
    }

    @PutMapping("/policy")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'HR_ADMIN')")
    @Operation(summary = "Update organizational separation policy")
    public ResponseEntity<ApiResponse<SeparationPolicy>> updatePolicy(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody SeparationPolicy policy
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        SeparationPolicy updated = separationService.updatePolicy(companyId, policy);
        return ResponseEntity.ok(ApiResponse.success(updated, "Separation policy updated successfully"));
    }
}
