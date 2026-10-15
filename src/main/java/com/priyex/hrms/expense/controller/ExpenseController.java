package com.priyex.hrms.expense.controller;

import com.priyex.hrms.common.exception.ResourceNotFoundException;
import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.mapper.EmployeeMapper;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.expense.mapper.ExpenseMapper;
import com.priyex.hrms.expense.model.ExpenseClaim;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expense Claims Management", description = "Endpoints for submitting, multi-tier auditing (Manager -> HR/Finance), approving, and reimbursing corporate expenses")
public class ExpenseController {

    private final ExpenseMapper expenseMapper;
    private final EmployeeMapper employeeMapper;

    @GetMapping
    @Operation(summary = "Get all expense claims for current company")
    public ResponseEntity<ApiResponse<List<ExpenseClaim>>> listExpenses(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "scope", required = false) String scope
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long employeeId = currentUser != null ? currentUser.getEmployeeId() : null;

        if ("team".equalsIgnoreCase(scope) && employeeId != null) {
            List<ExpenseClaim> teamList = expenseMapper.findByManagerEmployeeId(employeeId, companyId);
            return ResponseEntity.ok(ApiResponse.success(teamList));
        }

        if ("mine".equalsIgnoreCase(scope) && employeeId != null) {
            List<ExpenseClaim> myList = expenseMapper.findByEmployeeId(employeeId, companyId);
            return ResponseEntity.ok(ApiResponse.success(myList));
        }

        List<ExpenseClaim> list = expenseMapper.findAll(companyId, status, category);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/team")
    @Operation(summary = "Get expense claims submitted by current manager's direct reports")
    public ResponseEntity<ApiResponse<List<ExpenseClaim>>> getTeamExpenses(@CurrentUser UserPrincipal currentUser) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long managerEmployeeId = currentUser != null ? currentUser.getEmployeeId() : null;

        if (managerEmployeeId == null) {
            return ResponseEntity.ok(ApiResponse.success(Collections.emptyList()));
        }

        List<ExpenseClaim> list = expenseMapper.findByManagerEmployeeId(managerEmployeeId, companyId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @Operation(summary = "Submit a new corporate expense claim with automatic manager routing")
    public ResponseEntity<ApiResponse<ExpenseClaim>> submitExpense(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody ExpenseClaim request
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        request.setCompanyId(companyId);

        if (request.getEmployeeId() == null) {
            Long actorEmpId = currentUser != null ? currentUser.getEmployeeId() : null;
            request.setEmployeeId(actorEmpId != null ? actorEmpId : 1L);
        }

        if (request.getClaimNumber() == null || request.getClaimNumber().isBlank()) {
            request.setClaimNumber("EXP-" + LocalDate.now().getYear() + "-" + (System.currentTimeMillis() % 100000));
        }

        if (request.getCurrency() == null) {
            request.setCurrency("INR");
        }
        if (request.getSubmittedAt() == null) {
            request.setSubmittedAt(LocalDate.now().toString());
        }

        // Determine reporting manager
        Employee applicant = employeeMapper.findById(request.getEmployeeId(), companyId).orElse(null);
        if (applicant != null && applicant.getReportingManagerId() != null) {
            request.setManagerId(applicant.getReportingManagerId());
            request.setStatus("PENDING_MANAGER");
        } else {
            request.setManagerId(null);
            request.setStatus("PENDING_HR");
        }

        expenseMapper.insert(request);
        ExpenseClaim created = expenseMapper.findById(request.getId(), companyId).orElse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Expense claim filed successfully"));
    }

    @PutMapping("/{id}/manager-approve")
    @Operation(summary = "Stage 1: Manager approves expense claim (forwarded to HR/Finance)")
    public ResponseEntity<ApiResponse<ExpenseClaim>> managerApprove(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long managerEmpId = currentUser != null ? currentUser.getEmployeeId() : null;

        expenseMapper.updateManagerDecision(id, companyId, "PENDING_HR", comment != null ? comment : "Approved by Manager", managerEmpId);
        ExpenseClaim updated = expenseMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseClaim", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Expense claim approved by Manager, forwarded to HR/Finance"));
    }

    @PutMapping("/{id}/manager-reject")
    @Operation(summary = "Stage 1: Manager rejects expense claim")
    public ResponseEntity<ApiResponse<ExpenseClaim>> managerReject(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long managerEmpId = currentUser != null ? currentUser.getEmployeeId() : null;

        expenseMapper.updateManagerDecision(id, companyId, "REJECTED_MANAGER", comment != null ? comment : "Rejected by Manager", managerEmpId);
        ExpenseClaim updated = expenseMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseClaim", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Expense claim rejected by Manager"));
    }

    @PutMapping("/{id}/hr-approve")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Stage 2: HR / Finance audits and approves expense claim for payout")
    public ResponseEntity<ApiResponse<ExpenseClaim>> hrApprove(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorUserId = currentUser != null ? currentUser.getId() : 1L;

        expenseMapper.updateHrDecision(id, companyId, "APPROVED", comment != null ? comment : "Approved by HR / Finance", actorUserId);
        ExpenseClaim updated = expenseMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseClaim", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Expense claim approved for payout"));
    }

    @PutMapping("/{id}/hr-reject")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Stage 2: HR / Finance rejects expense claim")
    public ResponseEntity<ApiResponse<ExpenseClaim>> hrReject(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        Long actorUserId = currentUser != null ? currentUser.getId() : 1L;

        expenseMapper.updateHrDecision(id, companyId, "REJECTED_HR", comment != null ? comment : "Non-compliant with expense policy limits", actorUserId);
        ExpenseClaim updated = expenseMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseClaim", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Expense claim rejected"));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Smart approve endpoint: auto-detects Stage 1 (Manager) or Stage 2 (HR/Finance)")
    public ResponseEntity<ApiResponse<ExpenseClaim>> approveExpense(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        ExpenseClaim claim = expenseMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseClaim", "id", id));

        boolean isHr = currentUser != null && (currentUser.hasRole("HR_ADMIN") || currentUser.hasRole("SUPER_ADMIN") || currentUser.hasRole("ADMIN"));

        if ("PENDING_MANAGER".equalsIgnoreCase(claim.getStatus()) && !isHr) {
            return managerApprove(currentUser, id, comment);
        } else {
            return hrApprove(currentUser, id, comment);
        }
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Smart reject endpoint: auto-detects Stage 1 (Manager) or Stage 2 (HR/Finance)")
    public ResponseEntity<ApiResponse<ExpenseClaim>> rejectExpense(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "comment", required = false) String comment
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        ExpenseClaim claim = expenseMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseClaim", "id", id));

        boolean isHr = currentUser != null && (currentUser.hasRole("HR_ADMIN") || currentUser.hasRole("SUPER_ADMIN") || currentUser.hasRole("ADMIN"));

        if ("PENDING_MANAGER".equalsIgnoreCase(claim.getStatus()) && !isHr) {
            return managerReject(currentUser, id, comment);
        } else {
            return hrReject(currentUser, id, comment);
        }
    }

    @PutMapping("/{id}/reimburse")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Mark expense claim as reimbursed via payroll or bank transfer")
    public ResponseEntity<ApiResponse<ExpenseClaim>> reimburseExpense(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @RequestParam(name = "ref", required = false) String ref
    ) {
        Long companyId = (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
        String payoutRef = (ref != null && !ref.isBlank()) ? ref : "NEFT-" + System.currentTimeMillis();

        expenseMapper.updateReimbursed(id, companyId, payoutRef);
        ExpenseClaim updated = expenseMapper.findById(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseClaim", "id", id));
        return ResponseEntity.ok(ApiResponse.success(updated, "Claim marked as reimbursed"));
    }
}
