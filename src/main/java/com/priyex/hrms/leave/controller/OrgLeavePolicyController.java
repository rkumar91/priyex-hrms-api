package com.priyex.hrms.leave.controller;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.leave.dto.CreateOrgLeavePolicyRequest;
import com.priyex.hrms.leave.mapper.OrgLeavePolicyMapper;
import com.priyex.hrms.leave.model.OrgLeavePolicy;
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
@RequestMapping("/api/v1/organization/leave-policies")
@RequiredArgsConstructor
@Tag(name = "Organization Leave Policies", description = "Endpoints for configuring annual org-level leave quotas (CL, SL, PL, etc.)")
public class OrgLeavePolicyController {

    private final OrgLeavePolicyMapper orgLeavePolicyMapper;

    @GetMapping
    @Operation(summary = "Get all annual leave policies for current organization")
    public ResponseEntity<ApiResponse<List<OrgLeavePolicy>>> getPolicies(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        List<OrgLeavePolicy> list = orgLeavePolicyMapper.findAllByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create or configure a new organization leave quota")
    public ResponseEntity<ApiResponse<OrgLeavePolicy>> createPolicy(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateOrgLeavePolicyRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        OrgLeavePolicy policy = OrgLeavePolicy.builder()
                .companyId(companyId)
                .leaveCode(request.getLeaveCode().trim().toUpperCase())
                .leaveName(request.getLeaveName().trim())
                .annualDays(request.getAnnualDays())
                .isPaid(request.getIsPaid() != null ? request.getIsPaid() : true)
                .carryForwardAllowed(request.getCarryForwardAllowed() != null ? request.getCarryForwardAllowed() : false)
                .maxCarryForwardDays(request.getMaxCarryForwardDays() != null ? request.getMaxCarryForwardDays() : 0)
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        orgLeavePolicyMapper.insert(policy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(policy, "Leave policy created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update an existing annual leave quota")
    public ResponseEntity<ApiResponse<OrgLeavePolicy>> updatePolicy(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody CreateOrgLeavePolicyRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        OrgLeavePolicy existing = orgLeavePolicyMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "id", id));

        existing.setLeaveName(request.getLeaveName().trim());
        existing.setAnnualDays(request.getAnnualDays());
        if (request.getIsPaid() != null) existing.setIsPaid(request.getIsPaid());
        if (request.getCarryForwardAllowed() != null) existing.setCarryForwardAllowed(request.getCarryForwardAllowed());
        if (request.getMaxCarryForwardDays() != null) existing.setMaxCarryForwardDays(request.getMaxCarryForwardDays());
        existing.setDescription(request.getDescription());
        if (request.getIsActive() != null) existing.setIsActive(request.getIsActive());

        orgLeavePolicyMapper.update(existing);
        return ResponseEntity.ok(ApiResponse.success(existing, "Leave policy updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Delete an organization leave quota")
    public ResponseEntity<ApiResponse<Void>> deletePolicy(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        orgLeavePolicyMapper.delete(id, companyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Leave policy deleted successfully"));
    }
}
