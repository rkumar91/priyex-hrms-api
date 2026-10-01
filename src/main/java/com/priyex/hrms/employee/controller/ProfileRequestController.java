package com.priyex.hrms.employee.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.dto.CreateProfileRequest;
import com.priyex.hrms.employee.dto.ReviewProfileRequest;
import com.priyex.hrms.employee.model.ProfileRequest;
import com.priyex.hrms.employee.service.ProfileRequestService;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
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
@RequestMapping("/api/v1/profile-requests")
@RequiredArgsConstructor
@Tag(name = "Employee Profile Requests", description = "Endpoints for employee change requests and HR approval workflow")
public class ProfileRequestController {

    private final ProfileRequestService profileRequestService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a profile change request (bank account, legal name, email)")
    public ResponseEntity<ApiResponse<ProfileRequest>> submitRequest(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateProfileRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = currentUser != null ? currentUser.getEmployeeId() : null;
        if (employeeId == null) {
            employeeId = 1L; // Fallback for demo
        }

        ProfileRequest created = profileRequestService.submitRequest(companyId, employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Profile change request submitted successfully"));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get list of profile change requests")
    public ResponseEntity<ApiResponse<List<ProfileRequest>>> getRequests(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String status
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        boolean isHrOrAdmin = currentUser != null && (
                currentUser.hasRole("SUPER_ADMIN") ||
                currentUser.hasRole("ADMIN") ||
                currentUser.hasRole("HR_ADMIN")
        );

        Long employeeIdFilter = null;
        if (!isHrOrAdmin) {
            employeeIdFilter = (currentUser != null && currentUser.getEmployeeId() != null)
                    ? currentUser.getEmployeeId() : 1L;
        }

        List<ProfileRequest> requests = profileRequestService.getRequests(companyId, employeeIdFilter, status);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "HR Manager / Admin approves employee profile change request")
    public ResponseEntity<ApiResponse<ProfileRequest>> approveRequest(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody(required = false) ReviewProfileRequest reviewRequest
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long reviewerId = currentUser != null ? currentUser.getId() : null;
        String notes = reviewRequest != null ? reviewRequest.getReviewerNotes() : "Approved by HR";

        ProfileRequest approved = profileRequestService.approveRequest(companyId, id, reviewerId, notes);
        return ResponseEntity.ok(ApiResponse.success(approved, "Request approved and employee profile updated"));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "HR Manager / Admin rejects employee profile change request")
    public ResponseEntity<ApiResponse<ProfileRequest>> rejectRequest(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody(required = false) ReviewProfileRequest reviewRequest
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long reviewerId = currentUser != null ? currentUser.getId() : null;
        String notes = (reviewRequest != null && reviewRequest.getReviewerNotes() != null)
                ? reviewRequest.getReviewerNotes() : "Rejected by HR";

        ProfileRequest rejected = profileRequestService.rejectRequest(companyId, id, reviewerId, notes);
        return ResponseEntity.ok(ApiResponse.success(rejected, "Request rejected"));
    }
}
