package com.priyex.hrms.saas.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.lead.model.LeadInquiry;
import com.priyex.hrms.saas.mapper.SaasConsoleMapper;
import com.priyex.hrms.saas.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/v1/saas-console")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SAAS_ADMIN', 'SAAS_SALES', 'SAAS_FINANCE', 'SAAS_SUPPORT')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SaaS Owner Console", description = "Control plane for Priyex SaaS business management, tenant lifecycle, billing, and leads")
public class SaasConsoleController {

    private final SaasConsoleMapper saasConsoleMapper;

    @GetMapping("/overview")
    @Operation(summary = "Get SaaS Owner Executive Dashboard Overview")
    public ResponseEntity<ApiResponse<SaasConsoleOverview>> getOverview() {
        SaasConsoleOverview overview = saasConsoleMapper.getOverview();
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/leads")
    @Operation(summary = "Get all prospective demo consultations & sales queries")
    public ResponseEntity<ApiResponse<List<LeadInquiry>>> getLeads(
            @RequestParam(name = "limit", defaultValue = "100") int limit,
            @RequestParam(name = "offset", defaultValue = "0") int offset) {
        List<LeadInquiry> leads = saasConsoleMapper.findAllLeads(limit, offset);
        return ResponseEntity.ok(ApiResponse.success(leads));
    }

    @PutMapping("/leads/{id}/status")
    @Operation(summary = "Update lead pipeline stage")
    public ResponseEntity<ApiResponse<Void>> updateLeadStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Status is required"));
        }
        saasConsoleMapper.updateLeadStatus(id, status.trim().toUpperCase());
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "UPDATE_LEAD_STATUS", "LEAD", String.valueOf(id),
                "Updated lead status to " + status, null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(null, "Lead status updated."));
    }

    @PostMapping("/leads/{id}/drop")
    @Operation(summary = "Drop or disqualify lead inquiry with structured reason")
    public ResponseEntity<ApiResponse<Void>> dropLead(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String dropReason = body.getOrDefault("dropReason", "Not Qualified");
        String notes = body.getOrDefault("notes", "");

        saasConsoleMapper.updateLeadStatus(id, "LOST");

        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "DROP_LEAD_INQUIRY", "LEAD", String.valueOf(id),
                "Dropped Lead. Reason: " + dropReason, notes, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(null, "Lead dropped and archived."));
    }

    @GetMapping("/accounts")
    @Operation(summary = "Get all commercial customer accounts")
    public ResponseEntity<ApiResponse<List<SaasCommercialAccount>>> getAccounts() {
        List<SaasCommercialAccount> accounts = saasConsoleMapper.findAllAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @PostMapping("/accounts")
    @Operation(summary = "Create or convert lead into a commercial customer account")
    public ResponseEntity<ApiResponse<SaasCommercialAccount>> createAccount(
            @RequestBody SaasCommercialAccount account,
            Authentication auth) {
        if (account.getAccountRef() == null || account.getAccountRef().isBlank()) {
            int randomRef = ThreadLocalRandom.current().nextInt(1000, 9999);
            account.setAccountRef("PRX-ACC-" + randomRef);
        }
        if (account.getLifecycleStatus() == null) {
            account.setLifecycleStatus("PROSPECT");
        }
        saasConsoleMapper.insertAccount(account);
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "CREATE_COMMERCIAL_ACCOUNT", "ACCOUNT",
                account.getAccountRef(), "Created commercial account: " + account.getAccountName(), null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(account, "Commercial account created successfully."));
    }

    @PutMapping("/accounts/{id}/status")
    @Operation(summary = "Update commercial account lifecycle status")
    public ResponseEntity<ApiResponse<Void>> updateAccountStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String status = body.get("status");
        saasConsoleMapper.updateAccountStatus(id, status.trim().toUpperCase());
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "UPDATE_ACCOUNT_STATUS", "ACCOUNT", String.valueOf(id),
                "Updated account lifecycle status to " + status, null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(null, "Account status updated."));
    }

    @PostMapping("/accounts/{id}/offboard")
    @Operation(summary = "Execute tenant client offboarding, cancel subscriptions, and optionally deactivate workspace")
    public ResponseEntity<ApiResponse<Void>> offboardAccount(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        String reason = (String) body.getOrDefault("reason", "Client requested termination");
        String notes = (String) body.getOrDefault("notes", "");
        boolean deactivateTenantNow = Boolean.TRUE.equals(body.get("deactivateTenantNow"));

        saasConsoleMapper.offboardCommercialAccount(id, reason, notes, LocalDate.now());
        saasConsoleMapper.cancelSubscriptionsByAccount(id);

        if (deactivateTenantNow) {
            Long linkedCompanyId = saasConsoleMapper.getLinkedCompanyIdByAccountId(id);
            if (linkedCompanyId != null) {
                saasConsoleMapper.deactivateCompany(linkedCompanyId);
            }
        }

        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "OFFBOARD_CLIENT_TENANT", "ACCOUNT", String.valueOf(id),
                "Offboarded client. Reason: " + reason, notes, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(null, "Client offboarding completed successfully."));
    }

    @GetMapping("/plans")
    @Operation(summary = "Get versioned SaaS plan catalog")
    public ResponseEntity<ApiResponse<List<SaasPlan>>> getPlans() {
        List<SaasPlan> plans = saasConsoleMapper.findAllPlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @PostMapping("/plans")
    @Operation(summary = "Add a new plan tier to catalog")
    public ResponseEntity<ApiResponse<SaasPlan>> createPlan(
            @RequestBody SaasPlan plan,
            Authentication auth) {
        saasConsoleMapper.insertPlan(plan);
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "CREATE_PLAN", "PLAN", plan.getPlanCode(),
                "Created plan tier: " + plan.getPlanName(), null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(plan, "Plan created successfully."));
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "Get commercial customer subscriptions")
    public ResponseEntity<ApiResponse<List<SaasSubscription>>> getSubscriptions() {
        List<SaasSubscription> subscriptions = saasConsoleMapper.findAllSubscriptions();
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @PostMapping("/subscriptions")
    @Operation(summary = "Assign a subscription plan to an account")
    public ResponseEntity<ApiResponse<SaasSubscription>> createSubscription(
            @RequestBody SaasSubscription subscription,
            Authentication auth) {
        if (subscription.getSubscriptionRef() == null || subscription.getSubscriptionRef().isBlank()) {
            subscription.setSubscriptionRef("PRX-SUB-" + ThreadLocalRandom.current().nextInt(1000, 9999));
        }
        if (subscription.getStatus() == null) {
            subscription.setStatus("ACTIVE");
        }
        if (subscription.getPeriodStart() == null) {
            subscription.setPeriodStart(LocalDate.now());
        }
        if (subscription.getPeriodEnd() == null) {
            subscription.setPeriodEnd(LocalDate.now().plusYears(1));
        }
        if (subscription.getNextBillingDate() == null) {
            subscription.setNextBillingDate(LocalDate.now().plusYears(1));
        }
        saasConsoleMapper.insertSubscription(subscription);
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "ASSIGN_SUBSCRIPTION", "SUBSCRIPTION",
                subscription.getSubscriptionRef(), "Assigned subscription to account ID " + subscription.getAccountId(), null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(subscription, "Subscription created successfully."));
    }

    @GetMapping("/contracts")
    @Operation(summary = "Get one-time setup fees and recurring maintenance AMC contracts")
    public ResponseEntity<ApiResponse<List<SaasContract>>> getContracts() {
        List<SaasContract> contracts = saasConsoleMapper.findAllContracts();
        return ResponseEntity.ok(ApiResponse.success(contracts));
    }

    @PostMapping("/contracts")
    @Operation(summary = "Create a setup fee or AMC contract")
    public ResponseEntity<ApiResponse<SaasContract>> createContract(
            @RequestBody SaasContract contract,
            Authentication auth) {
        if (contract.getContractRef() == null || contract.getContractRef().isBlank()) {
            String prefix = "ONE_TIME_SETUP".equals(contract.getContractType()) ? "PRX-SET-" : "PRX-AMC-";
            contract.setContractRef(prefix + ThreadLocalRandom.current().nextInt(1000, 9999));
        }
        if (contract.getBillingStatus() == null) {
            contract.setBillingStatus("PENDING");
        }
        if (contract.getStartDate() == null) {
            contract.setStartDate(LocalDate.now());
        }
        saasConsoleMapper.insertContract(contract);
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "CREATE_CONTRACT", "CONTRACT",
                contract.getContractRef(), "Created " + contract.getContractType() + " contract: " + contract.getTitle(), null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(contract, "Contract created successfully."));
    }

    @PutMapping("/contracts/{id}/status")
    @Operation(summary = "Update contract billing settlement status")
    public ResponseEntity<ApiResponse<Void>> updateContractStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String status = body.get("status");
        saasConsoleMapper.updateContractStatus(id, status.trim().toUpperCase());
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "UPDATE_CONTRACT_STATUS", "CONTRACT", String.valueOf(id),
                "Updated contract billing status to " + status, null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(null, "Contract status updated."));
    }

    @GetMapping("/invoices")
    @Operation(summary = "Get billing invoices register")
    public ResponseEntity<ApiResponse<List<SaasInvoice>>> getInvoices() {
        List<SaasInvoice> invoices = saasConsoleMapper.findAllInvoices();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @PostMapping("/invoices")
    @Operation(summary = "Generate a new invoice for an account")
    public ResponseEntity<ApiResponse<SaasInvoice>> createInvoice(
            @RequestBody SaasInvoice invoice,
            Authentication auth) {
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            invoice.setInvoiceNumber("INV-" + LocalDate.now().getYear() + "-" + ThreadLocalRandom.current().nextInt(1000, 9999));
        }
        if (invoice.getStatus() == null) {
            invoice.setStatus("ISSUED");
        }
        if (invoice.getTaxAmount() == null) {
            invoice.setTaxAmount(invoice.getSubtotalAmount().multiply(new BigDecimal("0.18")));
        }
        if (invoice.getTotalAmount() == null) {
            invoice.setTotalAmount(invoice.getSubtotalAmount().add(invoice.getTaxAmount()));
        }
        saasConsoleMapper.insertInvoice(invoice);
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "GENERATE_INVOICE", "INVOICE",
                invoice.getInvoiceNumber(), "Generated invoice of ₹" + invoice.getTotalAmount(), null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(invoice, "Invoice created successfully."));
    }

    @PutMapping("/invoices/{id}/status")
    @Operation(summary = "Update invoice payment status and reference")
    public ResponseEntity<ApiResponse<Void>> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String status = body.get("status");
        String paymentRef = body.get("paymentReference");
        saasConsoleMapper.updateInvoiceStatus(id, status.trim().toUpperCase(), paymentRef);
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "UPDATE_INVOICE_STATUS", "INVOICE", String.valueOf(id),
                "Updated invoice status to " + status, paymentRef, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(null, "Invoice status updated."));
    }

    @GetMapping("/requests")
    @Operation(summary = "Get pre-sales inquiries and tenant feature/billing requests")
    public ResponseEntity<ApiResponse<List<SaasCustomerRequest>>> getCustomerRequests() {
        List<SaasCustomerRequest> requests = saasConsoleMapper.findAllCustomerRequests();
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PostMapping("/requests")
    @Operation(summary = "Log a customer query, feature request or SLA ticket")
    public ResponseEntity<ApiResponse<SaasCustomerRequest>> createCustomerRequest(
            @RequestBody SaasCustomerRequest request,
            Authentication auth) {
        if (request.getRequestRef() == null || request.getRequestRef().isBlank()) {
            request.setRequestRef("PRX-REQ-" + ThreadLocalRandom.current().nextInt(1000, 9999));
        }
        if (request.getStatus() == null) {
            request.setStatus("NEW");
        }
        saasConsoleMapper.insertCustomerRequest(request);
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "CREATE_CUSTOMER_REQUEST", "REQUEST",
                request.getRequestRef(), "Created request ticket: " + request.getTitle(), null, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(request, "Request ticket logged successfully."));
    }

    @PutMapping("/requests/{id}/status")
    @Operation(summary = "Update customer request status & resolution notes")
    public ResponseEntity<ApiResponse<Void>> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String status = body.get("status");
        String notes = body.get("resolutionNotes");
        saasConsoleMapper.updateCustomerRequestStatus(id, status != null ? status.trim().toUpperCase() : null, notes);
        String actor = auth != null ? auth.getName() : "system";
        saasConsoleMapper.insertAuditEvent(actor, "UPDATE_CUSTOMER_REQUEST", "REQUEST", String.valueOf(id),
                "Updated customer request status to " + status, notes, "127.0.0.1");
        return ResponseEntity.ok(ApiResponse.success(null, "Customer request updated."));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get SaaS control plane audit history")
    public ResponseEntity<ApiResponse<List<SaasAuditEvent>>> getAuditLogs(
            @RequestParam(name = "limit", defaultValue = "50") int limit) {
        List<SaasAuditEvent> logs = saasConsoleMapper.findRecentAuditEvents(limit);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
