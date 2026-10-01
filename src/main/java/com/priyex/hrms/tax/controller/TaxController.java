package com.priyex.hrms.tax.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.employee.model.Employee;
import com.priyex.hrms.employee.service.EmployeeService;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import com.priyex.hrms.tax.dto.*;
import com.priyex.hrms.tax.model.InvestmentDeclaration;
import com.priyex.hrms.tax.service.TaxCalculationService;
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
@RequestMapping("/api/v1/tax")
@RequiredArgsConstructor
@Tag(name = "Tax & Investment Declarations", description = "Endpoints for employee tax declarations, Old vs New Regime calculation, and statutory forms (Form 16 & Form 12BB)")
public class TaxController {

    private final TaxCalculationService taxService;
    private final EmployeeService employeeService;

    @GetMapping("/declarations/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current employee investment declaration for financial year")
    public ResponseEntity<ApiResponse<InvestmentDeclaration>> getMyDeclaration(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "financialYear", required = false, defaultValue = "2026-2027") String financialYear
    ) {
        Long empId = resolveEmployeeId(currentUser);
        InvestmentDeclaration decl = taxService.getDeclaration(empId, financialYear);
        return ResponseEntity.ok(ApiResponse.success(decl));
    }

    @PostMapping("/declarations/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Save or submit employee investment declaration")
    public ResponseEntity<ApiResponse<InvestmentDeclaration>> submitDeclaration(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody SubmitDeclarationRequest request
    ) {
        Long companyId = resolveCompanyId(currentUser);
        Long empId = resolveEmployeeId(currentUser);
        InvestmentDeclaration saved = taxService.saveOrSubmitDeclaration(companyId, empId, request);
        String msg = Boolean.TRUE.equals(request.getIsDraft())
                ? "Investment declaration draft saved successfully!"
                : "Investment declaration submitted to HR for verification!";
        return ResponseEntity.ok(ApiResponse.success(saved, msg));
    }

    @GetMapping("/calculate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Calculate and compare Old vs New Regime tax liabilities based on CTC and declared exemptions")
    public ResponseEntity<ApiResponse<TaxCalculationResponse>> calculateTax(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "financialYear", required = false, defaultValue = "2026-2027") String financialYear,
            @RequestParam(value = "regime", required = false) String regime,
            @RequestParam(value = "employeeId", required = false) Long employeeId
    ) {
        Long companyId = resolveCompanyId(currentUser);
        Long targetEmpId = (employeeId != null && isHrAdminUser(currentUser))
                ? employeeId
                : resolveEmployeeId(currentUser);

        TaxCalculationResponse response = taxService.calculateTax(companyId, targetEmpId, financialYear, regime);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/form16")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate official Form 16 (Part A & Part B) tax certificate")
    public ResponseEntity<ApiResponse<Form16Response>> getForm16(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "financialYear", required = false, defaultValue = "2026-2027") String financialYear,
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "employeeId", required = false) Long employeeId
    ) {
        Long resolvedCompanyId = (companyId != null) ? companyId : resolveCompanyId(currentUser);
        Long targetEmpId = (employeeId != null && isHrAdminUser(currentUser))
                ? employeeId
                : resolveEmployeeId(currentUser);

        Form16Response form16 = taxService.generateForm16(resolvedCompanyId, targetEmpId, financialYear);
        return ResponseEntity.ok(ApiResponse.success(form16));
    }

    @GetMapping("/form12bb")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Generate Form 12BB employee claims statement")
    public ResponseEntity<ApiResponse<Form12bbResponse>> getForm12bb(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "financialYear", required = false, defaultValue = "2026-2027") String financialYear,
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "employeeId", required = false) Long employeeId
    ) {
        Long resolvedCompanyId = (companyId != null) ? companyId : resolveCompanyId(currentUser);
        Long targetEmpId = (employeeId != null && isHrAdminUser(currentUser))
                ? employeeId
                : resolveEmployeeId(currentUser);

        Form12bbResponse form12bb = taxService.generateForm12bb(resolvedCompanyId, targetEmpId, financialYear);
        return ResponseEntity.ok(ApiResponse.success(form12bb));
    }

    @GetMapping("/declarations/company")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "Get all employee investment declarations for company/year (HR / Admin only)")
    public ResponseEntity<ApiResponse<List<InvestmentDeclaration>>> getCompanyDeclarations(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "companyId", required = false) Long companyId,
            @RequestParam(value = "financialYear", required = false, defaultValue = "2026-2027") String financialYear,
            @RequestParam(value = "status", required = false) String status
    ) {
        Long resolvedCompanyId = (companyId != null) ? companyId : resolveCompanyId(currentUser);
        List<InvestmentDeclaration> list = taxService.getDeclarationsForCompany(resolvedCompanyId, financialYear, status);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PutMapping("/declarations/{id}/verify")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('HR_ADMIN')")
    @Operation(summary = "Verify or reject employee investment declaration (HR / Admin only)")
    public ResponseEntity<ApiResponse<InvestmentDeclaration>> verifyDeclaration(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody VerifyDeclarationRequest request
    ) {
        Long companyId = resolveCompanyId(currentUser);
        Long actorId = currentUser != null ? currentUser.getId() : 1L;
        InvestmentDeclaration updated = taxService.verifyDeclaration(companyId, id, actorId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Declaration marked as " + request.getStatus()));
    }

    // ── Helper Resolvers ──

    private Long resolveCompanyId(UserPrincipal currentUser) {
        return (currentUser != null && currentUser.getCompanyId() != null) ? currentUser.getCompanyId() : 1L;
    }

    private Long resolveEmployeeId(UserPrincipal currentUser) {
        if (currentUser == null) return 1L;
        if (currentUser.getEmployeeId() != null) return currentUser.getEmployeeId();
        Employee me = employeeService.getMyProfile(resolveCompanyId(currentUser), currentUser.getId(), null);
        return me != null ? me.getId() : 1L;
    }

    private boolean isHrAdminUser(UserPrincipal currentUser) {
        if (currentUser == null) return false;
        return currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HR_ADMIN")
                        || a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
