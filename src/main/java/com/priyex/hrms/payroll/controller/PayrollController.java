package com.priyex.hrms.payroll.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.employee.service.EmployeeService;
import com.priyex.hrms.payroll.dto.CtcBreakdownResponse;
import com.priyex.hrms.payroll.dto.ExecutePayrollRequest;
import com.priyex.hrms.payroll.dto.PayrollSummaryResponse;
import com.priyex.hrms.payroll.model.EmployeePayslip;
import com.priyex.hrms.payroll.model.PayrollRun;
import com.priyex.hrms.payroll.service.PayrollService;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll & Compensation", description = "Endpoints for monthly payroll runs, CTC structures, and employee payslips")
public class PayrollController {

    private final PayrollService payrollService;
    private final EmployeeService employeeService;

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get high-level company payroll summary")
    public ResponseEntity<ApiResponse<PayrollSummaryResponse>> getSummary(@CurrentUser UserPrincipal currentUser) {
        Long companyId = resolveCompanyId(currentUser);
        PayrollSummaryResponse summary = payrollService.getPayrollSummary(companyId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/execute")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "Execute monthly payroll run and generate employee payslips (HR / Admin only)")
    public ResponseEntity<ApiResponse<PayrollRun>> executePayroll(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ExecutePayrollRequest request
    ) {
        Long companyId = resolveCompanyId(currentUser);
        Long actorId = currentUser != null ? currentUser.getId() : 1L;
        PayrollRun run = payrollService.executePayrollRun(companyId, actorId, request);
        return ResponseEntity.ok(ApiResponse.success(run, "Monthly payroll run executed successfully!"));
    }

    @GetMapping("/runs")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "Get all payroll run batches (HR / Admin only)")
    public ResponseEntity<ApiResponse<List<PayrollRun>>> getPayrollRuns(@CurrentUser UserPrincipal currentUser) {
        Long companyId = resolveCompanyId(currentUser);
        List<PayrollRun> runs = payrollService.getPayrollRuns(companyId);
        return ResponseEntity.ok(ApiResponse.success(runs));
    }

    @GetMapping("/runs/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "Get single payroll run details (HR / Admin only)")
    public ResponseEntity<ApiResponse<PayrollRun>> getPayrollRun(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        Long companyId = resolveCompanyId(currentUser);
        PayrollRun run = payrollService.getPayrollRun(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(run));
    }

    @GetMapping("/runs/{id}/payslips")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "Get all employee payslips in a specific payroll run (HR / Admin only)")
    public ResponseEntity<ApiResponse<List<EmployeePayslip>>> getRunPayslips(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        Long companyId = resolveCompanyId(currentUser);
        List<EmployeePayslip> payslips = payrollService.getPayslipsForRun(companyId, id);
        return ResponseEntity.ok(ApiResponse.success(payslips));
    }

    @GetMapping("/my-ctc")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current logged-in employee/HR/Admin CTC breakdown")
    public ResponseEntity<ApiResponse<CtcBreakdownResponse>> getMyCtc(@CurrentUser UserPrincipal currentUser) {
        Long companyId = resolveCompanyId(currentUser);
        Long userId = currentUser != null ? currentUser.getId() : null;
        Long employeeId = currentUser != null ? currentUser.getEmployeeId() : null;
        Employee me = employeeService.getMyProfile(companyId, userId, employeeId);
        CtcBreakdownResponse ctc = payrollService.getMyCtc(companyId, me.getId());
        return ResponseEntity.ok(ApiResponse.success(ctc));
    }

    @GetMapping("/my-payslips")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current logged-in employee/HR/Admin payslips list")
    public ResponseEntity<ApiResponse<List<EmployeePayslip>>> getMyPayslips(@CurrentUser UserPrincipal currentUser) {
        Long companyId = resolveCompanyId(currentUser);
        Long userId = currentUser != null ? currentUser.getId() : null;
        Long employeeId = currentUser != null ? currentUser.getEmployeeId() : null;
        Employee me = employeeService.getMyProfile(companyId, userId, employeeId);
        List<EmployeePayslip> payslips = payrollService.getMyPayslips(me.getId());
        return ResponseEntity.ok(ApiResponse.success(payslips));
    }

    @GetMapping("/payslips/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get detailed payslip record")
    public ResponseEntity<ApiResponse<EmployeePayslip>> getPayslip(@PathVariable Long id) {
        EmployeePayslip payslip = payrollService.getPayslip(id);
        return ResponseEntity.ok(ApiResponse.success(payslip));
    }

    private Long resolveCompanyId(UserPrincipal currentUser) {
        return (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
    }
}
