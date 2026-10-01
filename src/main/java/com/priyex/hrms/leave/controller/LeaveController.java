package com.priyex.hrms.leave.controller;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.leave.mapper.LeaveMapper;
import com.priyex.hrms.leave.model.LeaveRequest;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "Endpoints for applying, listing, approving, and rejecting leave applications")
public class LeaveController {

    private final LeaveMapper leaveMapper;

    @GetMapping
    @Operation(summary = "Get all leave applications for current company")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaves(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        List<LeaveRequest> list = leaveMapper.findAllByCompanyId(companyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @Operation(summary = "Submit a new leave application")
    public ResponseEntity<ApiResponse<LeaveRequest>> createLeave(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody LeaveRequest request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        request.setCompanyId(companyId);
        if (request.getEmployeeId() == null) request.setEmployeeId(1L);
        if (request.getStatus() == null) request.setStatus("PENDING");

        if (request.getStartDate() != null && request.getEndDate() != null) {
            long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
            request.setTotalDays((int) Math.max(1, days));
        } else {
            request.setTotalDays(1);
        }

        leaveMapper.insert(request);
        LeaveRequest created = leaveMapper.findById(request.getId(), companyId).orElse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Leave application submitted"));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve leave application")
    public ResponseEntity<ApiResponse<LeaveRequest>> approveLeave(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorId = currentUser != null ? currentUser.getId() : 1L;

        leaveMapper.updateStatus(id, companyId, "APPROVED", comment, actorId);
        LeaveRequest updated = leaveMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Leave application approved"));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject leave application")
    public ResponseEntity<ApiResponse<LeaveRequest>> rejectLeave(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorId = currentUser != null ? currentUser.getId() : 1L;

        leaveMapper.updateStatus(id, companyId, "REJECTED", comment, actorId);
        LeaveRequest updated = leaveMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Leave application rejected"));
    }
}
