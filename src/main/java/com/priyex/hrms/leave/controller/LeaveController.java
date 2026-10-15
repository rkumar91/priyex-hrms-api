package com.priyex.hrms.leave.controller;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.leave.mapper.LeaveMapper;
import com.priyex.hrms.leave.model.LeaveRequest;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Endpoints for applying, listing, multi-tier approving (Manager -> HR), and rejecting leave applications")
public class LeaveController {

    private final LeaveMapper leaveMapper;
    private final EmployeeMapper employeeMapper;

    @GetMapping
    @Operation(summary = "Get all leave applications for current company")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaves(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(name = "scope", required = false) String scope
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = currentUser != null ? currentUser.getEmployeeId() : null;

        if ("team".equalsIgnoreCase(scope) && employeeId != null) {
            List<LeaveRequest> teamList = leaveMapper.findByManagerEmployeeId(employeeId, companyId);
            return ResponseEntity.ok(ApiResponse.success(teamList));
        }

        if ("mine".equalsIgnoreCase(scope) && employeeId != null) {
            List<LeaveRequest> myList = leaveMapper.findByEmployeeId(employeeId, companyId);
            return ResponseEntity.ok(ApiResponse.success(myList));
        }

        List<LeaveRequest> list = leaveMapper.findAllByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/team")
    @Operation(summary = "Get leave applications submitted by current manager's direct reports")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getTeamLeaves(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long managerEmployeeId = currentUser != null ? currentUser.getEmployeeId() : null;

        if (managerEmployeeId == null) {
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
        }

        List<LeaveRequest> list = leaveMapper.findByManagerEmployeeId(managerEmployeeId, companyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @Operation(summary = "Submit a new leave application with automatic manager routing")
    public ResponseEntity<ApiResponse<LeaveRequest>> createLeave(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody LeaveRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        request.setCompanyId(companyId);
        if (request.getEmployeeId() == null) {
            Long actorEmpId = currentUser != null ? currentUser.getEmployeeId() : null;
            request.setEmployeeId(actorEmpId != null ? actorEmpId : 1L);
        }

        // Calculate days
        if (request.getStartDate() != null && request.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            request.setTotalDays((int) Math.max(1, days));
        } else {
            request.setTotalDays(1);
        }

        // Resolve reporting manager
        Employee applicant = employeeMapper.findById(request.getEmployeeId(), companyId).orElse(null);
        if (applicant != null && applicant.getReportingManagerId() != null) {
            request.setManagerId(applicant.getReportingManagerId());
            request.setStatus("PENDING_MANAGER");
        } else {
            request.setManagerId(null);
            request.setStatus("PENDING_HR");
        }

        leaveMapper.insert(request);
        LeaveRequest created = leaveMapper.findById(request.getId(), companyId).orElse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Leave application submitted"));
    }

    @PutMapping("/{id}/manager-approve")
    @Operation(summary = "Stage 1: Direct Manager approves leave (advances to HR review)")
    public ResponseEntity<ApiResponse<LeaveRequest>> managerApprove(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long managerEmpId = currentUser != null ? currentUser.getEmployeeId() : null;

        leaveMapper.updateManagerDecision(id, companyId, "PENDING_HR", comment, managerEmpId);
        LeaveRequest updated = leaveMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Approved by Manager, forwarded to HR"));
    }

    @PutMapping("/{id}/manager-reject")
    @Operation(summary = "Stage 1: Direct Manager rejects leave")
    public ResponseEntity<ApiResponse<LeaveRequest>> managerReject(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long managerEmpId = currentUser != null ? currentUser.getEmployeeId() : null;

        leaveMapper.updateManagerDecision(id, companyId, "REJECTED_MANAGER", comment, managerEmpId);
        LeaveRequest updated = leaveMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Leave application rejected by Manager"));
    }

    @PutMapping("/{id}/hr-approve")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Stage 2: HR grants final approval for leave")
    public ResponseEntity<ApiResponse<LeaveRequest>> hrApprove(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorUserId = currentUser != null ? currentUser.getId() : 1L;

        leaveMapper.updateHrDecision(id, companyId, "APPROVED", comment, actorUserId);
        LeaveRequest updated = leaveMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Leave application granted final approval by HR"));
    }

    @PutMapping("/{id}/hr-reject")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Stage 2: HR rejects leave")
    public ResponseEntity<ApiResponse<LeaveRequest>> hrReject(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorUserId = currentUser != null ? currentUser.getId() : 1L;

        leaveMapper.updateHrDecision(id, companyId, "REJECTED_HR", comment, actorUserId);
        LeaveRequest updated = leaveMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Leave application rejected by HR"));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Smart approve endpoint: auto-detects Stage 1 (Manager) or Stage 2 (HR)")
    public ResponseEntity<ApiResponse<LeaveRequest>> approveLeave(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        LeaveRequest existing = leaveMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        boolean isHr = currentUser != null && (currentUser.hasRole("HR_ADMIN") || currentUser.hasRole("SUPER_ADMIN") || currentUser.hasRole("ADMIN"));

        if ("PENDING_MANAGER".equalsIgnoreCase(existing.getStatus()) && !isHr) {
            return managerApprove(currentUser, id, comment);
        } else {
            return hrApprove(currentUser, id, comment);
        }
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Smart reject endpoint: auto-detects Stage 1 (Manager) or Stage 2 (HR)")
    public ResponseEntity<ApiResponse<LeaveRequest>> rejectLeave(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        LeaveRequest existing = leaveMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        boolean isHr = currentUser != null && (currentUser.hasRole("HR_ADMIN") || currentUser.hasRole("SUPER_ADMIN") || currentUser.hasRole("ADMIN"));

        if ("PENDING_MANAGER".equalsIgnoreCase(existing.getStatus()) && !isHr) {
            return managerReject(currentUser, id, comment);
        } else {
            return hrReject(currentUser, id, comment);
        }
    }
}
