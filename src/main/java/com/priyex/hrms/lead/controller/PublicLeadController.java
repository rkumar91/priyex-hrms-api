package com.priyex.hrms.lead.controller;

import com.priyex.hrms.common.response.ApiResponse;
import com.priyex.hrms.lead.dto.LeadInquiryRequest;
import com.priyex.hrms.lead.dto.LeadInquiryResponse;
import com.priyex.hrms.lead.dto.PlatformStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public SaaS & Lead Acquisition", description = "Public endpoints for prospective clients, demo requests, and platform capabilities")
public class PublicLeadController {

    private static final Logger log = LoggerFactory.getLogger(PublicLeadController.class);

    public PublicLeadController() {
    }

    @PostMapping("/leads")
    @Operation(summary = "Submit a demo request or sales consultation", description = "Captures prospect contact details and returns scheduled confirmation")
    public ResponseEntity<ApiResponse<LeadInquiryResponse>> submitLead(@Valid @RequestBody LeadInquiryRequest request) {
        log.info("Received public lead inquiry: name={}, company={}, email={}, type={}",
                request.getFullName(), request.getCompanyName(), request.getWorkEmail(),
                request.getInquiryType());

        int randomRef = ThreadLocalRandom.current().nextInt(1000, 9999);
        String prefix = "DEMO_REQUEST".equalsIgnoreCase(request.getInquiryType()) ? "PRX-DEMO" : "PRX-SALES";
        String inquiryId = prefix + "-2026-" + randomRef;

        String assignedConsultant = "Priyex Enterprise Solutions Specialist";
        String scheduledWindow = (request.getPreferredDate() != null && !request.getPreferredDate().isBlank())
                ? request.getPreferredDate() + (request.getPreferredTimeSlot() != null ? " (" + request.getPreferredTimeSlot() + ")" : " (Morning Slot)")
                : "Within 24 Business Hours";

        String message = "Thank you, " + request.getFullName() + "! Your consultation request for "
                + request.getCompanyName() + " has been received. Our enterprise team will connect with you shortly.";

        LeadInquiryResponse response = new LeadInquiryResponse(
                inquiryId,
                "CONFIRMED",
                message,
                assignedConsultant,
                scheduledWindow,
                request.getWorkEmail(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Demo consultation request submitted successfully."));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform capabilities for marketing landing page", description = "Returns uptime SLA, supported modules and compliance standards")
    public ResponseEntity<ApiResponse<PlatformStatsResponse>> getPlatformStats() {
        PlatformStatsResponse stats = new PlatformStatsResponse(
                100, // Generic architectural scalability metric
                50000,
                "99.99%",
                "99.98%",
                "Under 45 minutes",
                List.of(
                        "Core HR & Employee Lifecycle Management",
                        "Automated Multi-State Payroll & Statutory Tax",
                        "Real-Time Biometric & Geofenced Time Attendance",
                        "Leave Quotas & Multi-Tier Approval Workflows",
                        "Expense Claims & Reimbursement Management",
                        "Multi-Tenant Cloud Policy Engine",
                        "Employee Help Desk & In-App Broadcast Alerts"
                ),
                List.of(
                        "SOC 2 Type II Ready",
                        "ISO/IEC 27001 Security Standard",
                        "Digital Personal Data Protection (DPDP) Standard",
                        "Statutory EPF, ESI, PT & Income Tax Regulations"
                )
        );

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
