package com.priyex.hrms.organization.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.common.tenant.TenantContext;
import com.priyex.hrms.organization.dto.CompanyDto;
import com.priyex.hrms.organization.dto.TenantPolicyConfigDto;
import com.priyex.hrms.organization.service.TenantPolicyService;
import com.priyex.hrms.security.CurrentUser;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenant")
@Tag(name = "Tenant & Policy Engine Management", description = "Multi-client enterprise policies and tenant switching")
public class TenantPolicyController {

    private final TenantPolicyService tenantPolicyService;
    private final com.priyex.hrms.organization.service.ClientOnboardingService clientOnboardingService;

    public TenantPolicyController(TenantPolicyService tenantPolicyService,
                                  com.priyex.hrms.organization.service.ClientOnboardingService clientOnboardingService) {
        super();
        this.tenantPolicyService = tenantPolicyService;
        this.clientOnboardingService = clientOnboardingService;
    }

    @GetMapping("/detect-domain")
    @Operation(summary = "Detect organization workspace from email domain")
    public ResponseEntity<ApiResponse<com.priyex.hrms.organization.dto.DomainDetectionResult>> detectDomain(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "domain", required = false) String domain) {
        String input = (domain != null && !domain.isBlank()) ? domain : email;
        com.priyex.hrms.organization.dto.DomainDetectionResult result = clientOnboardingService.detectDomain(input);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/policies")
    @Operation(summary = "Get policy engine configuration for the client/tenant")
    public ResponseEntity<ApiResponse<TenantPolicyConfigDto>> getPolicies(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "companyId", required = false) Long companyIdParam) {

        Long companyId = resolveTargetCompanyId(currentUser, companyIdParam);
        TenantPolicyConfigDto config = tenantPolicyService.getPolicyConfig(companyId);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @PutMapping("/policies")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HR_ADMIN', 'ADMIN')")
    @Operation(summary = "Update policy engine configuration for the client/tenant")
    public ResponseEntity<ApiResponse<TenantPolicyConfigDto>> updatePolicies(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "companyId", required = false) Long companyIdParam,
            @RequestBody TenantPolicyConfigDto updateDto) {

        Long companyId = resolveTargetCompanyId(currentUser, companyIdParam);
        TenantPolicyConfigDto updated = tenantPolicyService.updatePolicyConfig(companyId, updateDto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Policies updated successfully for company " + companyId));
    }

    @GetMapping("/clients")
    @Operation(summary = "Get list of all supported enterprise client organizations")
    public ResponseEntity<ApiResponse<List<CompanyDto>>> getClients() {
        List<CompanyDto> clients = tenantPolicyService.getAllClients();
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    private Long resolveTargetCompanyId(UserPrincipal currentUser, Long companyIdParam) {
        boolean isPlatformAdmin = currentUser != null && currentUser.getRoles() != null &&
                (currentUser.getRoles().contains("SUPER_ADMIN") || currentUser.getRoles().contains("PLATFORM_ADMIN"));

        if (isPlatformAdmin) {
            if (companyIdParam != null && companyIdParam > 0) {
                return companyIdParam;
            }
            if (TenantContext.getCompanyId() != null && TenantContext.getCompanyId() > 0) {
                return TenantContext.getCompanyId();
            }
        }
        if (currentUser != null && currentUser.getCompanyId() != null) {
            return currentUser.getCompanyId();
        }
        return 1L;
    }
}
