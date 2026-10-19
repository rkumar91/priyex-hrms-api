package com.priyex.hrms.organization.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.organization.dto.ClientOverviewDto;
import com.priyex.hrms.organization.dto.OnboardClientRequest;
import com.priyex.hrms.organization.service.ClientOnboardingService;
import com.priyex.hrms.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/super-admin/clients")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Tag(name = "Client Onboarding", description = "Super-Admin exclusive module to onboard and manage client organizations")
public class SuperAdminClientController {

    private final ClientOnboardingService clientOnboardingService;

    public SuperAdminClientController(ClientOnboardingService clientOnboardingService) {
        super();
        this.clientOnboardingService = clientOnboardingService;
    }

    @GetMapping
    @Operation(summary = "Get all client organizations", description = "Lists all onboarded clients with domain and status metrics")
    public ResponseEntity<ApiResponse<List<ClientOverviewDto>>> getAllClients() {
        List<ClientOverviewDto> clients = clientOnboardingService.getAllClients();
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client organization details")
    public ResponseEntity<ApiResponse<ClientOverviewDto>> getClientById(@PathVariable Long id) {
        ClientOverviewDto client = clientOnboardingService.getClientById(id);
        return ResponseEntity.ok(ApiResponse.success(client));
    }

    @PostMapping
    @Operation(summary = "Onboard a new client organization", description = "Creates company, verified domains, initial admin account, branch and default settings")
    public ResponseEntity<ApiResponse<ClientOverviewDto>> onboardClient(
            @Valid @RequestBody OnboardClientRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long actorId = principal != null ? principal.getId() : 1L;
        ClientOverviewDto created = clientOnboardingService.onboardClient(request, actorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Client organization onboarded successfully."));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate a client organization")
    public ResponseEntity<ApiResponse<Void>> toggleClientStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {
        boolean isActive = body.getOrDefault("isActive", true);
        clientOnboardingService.toggleClientStatus(id, isActive);
        String msg = isActive ? "Client organization activated." : "Client organization deactivated.";
        return ResponseEntity.ok(ApiResponse.success(null, msg));
    }

    @PostMapping("/{id}/domains")
    @Operation(summary = "Add an email domain to a client organization")
    public ResponseEntity<ApiResponse<Void>> addDomain(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String domain = (String) body.get("domainName");
        boolean isPrimary = Boolean.TRUE.equals(body.get("isPrimary"));
        clientOnboardingService.addDomain(id, domain, isPrimary);
        return ResponseEntity.ok(ApiResponse.success(null, "Domain mapped successfully."));
    }

    @DeleteMapping("/{id}/domains/{domainId}")
    @Operation(summary = "Remove an email domain from a client organization")
    public ResponseEntity<ApiResponse<Void>> removeDomain(
            @PathVariable Long id,
            @PathVariable Long domainId) {
        clientOnboardingService.removeDomain(id, domainId);
        return ResponseEntity.ok(ApiResponse.success(null, "Domain mapping removed."));
    }

    @PutMapping("/{id}/logo")
    @Operation(summary = "Update client logo via URL or base64 data string")
    public ResponseEntity<ApiResponse<Void>> updateClientLogo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String logoUrl = body.get("logoUrl");
        clientOnboardingService.updateClientLogo(id, logoUrl);
        return ResponseEntity.ok(ApiResponse.success(null, "Client logo updated successfully."));
    }

    @PostMapping(value = "/{id}/logo/upload", consumes = {"multipart/form-data"})
    @Operation(summary = "Upload client logo image file")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadClientLogo(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            throw new com.priyex.hrms.common.exception.BadRequestException("Uploaded file is empty");
        }
        if (file.getSize() > 3 * 1024 * 1024) {
            throw new com.priyex.hrms.common.exception.BadRequestException("Logo image file must be smaller than 3MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
            throw new com.priyex.hrms.common.exception.BadRequestException("Only image files (PNG, JPG, SVG, WebP) are accepted");
        }
        try {
            byte[] bytes = file.getBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String dataUri = "data:" + (contentType != null ? contentType : "image/png") + ";base64," + base64;
            clientOnboardingService.updateClientLogo(id, dataUri);
            return ResponseEntity.ok(ApiResponse.success(Map.of("logoUrl", dataUri), "Logo uploaded successfully."));
        } catch (java.io.IOException e) {
            throw new com.priyex.hrms.common.exception.BadRequestException("Failed to read uploaded image file");
        }
    }
}
