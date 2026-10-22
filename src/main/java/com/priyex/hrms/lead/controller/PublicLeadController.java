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

import com.priyex.hrms.common.email.EmailService;
import com.priyex.hrms.common.whatsapp.service.WhatsAppService;
import com.priyex.hrms.lead.mapper.LeadInquiryMapper;
import com.priyex.hrms.lead.model.LeadInquiry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public SaaS & Lead Acquisition", description = "Public endpoints for prospective clients, demo requests, and platform capabilities")
public class PublicLeadController {

    private static final Logger log = LoggerFactory.getLogger(PublicLeadController.class);

    @Autowired(required = false)
    private LeadInquiryMapper leadInquiryMapper;

    @Autowired(required = false)
    private WhatsAppService whatsAppService;

    @Autowired(required = false)
    private EmailService emailService;

    public PublicLeadController() {
    }

    @PostMapping("/leads")
    @Operation(summary = "Submit a demo request or sales consultation", description = "Captures prospect contact details, persists inquiry, and dispatches automated email/WhatsApp confirmations")
    public ResponseEntity<ApiResponse<LeadInquiryResponse>> submitLead(@Valid @RequestBody LeadInquiryRequest request) {
        log.info("Received public lead inquiry: name={}, company={}, email={}, phone={}, type={}",
                request.getFullName(), request.getCompanyName(), request.getWorkEmail(),
                request.getPhoneNumber(), request.getInquiryType());

        int randomRef = ThreadLocalRandom.current().nextInt(1000, 9999);
        String prefix = "DEMO_REQUEST".equalsIgnoreCase(request.getInquiryType()) ? "PRX-DEMO" : "PRX-SALES";
        String inquiryId = prefix + "-2026-" + randomRef;

        String assignedConsultant = "Priyex Enterprise Solutions Specialist";
        String scheduledWindow = (request.getPreferredDate() != null && !request.getPreferredDate().isBlank())
                ? request.getPreferredDate() + (request.getPreferredTimeSlot() != null ? " (" + request.getPreferredTimeSlot() + ")" : " (Morning Slot)")
                : "Within 24 Business Hours";

        String emailStatus = "PENDING";
        String whatsappStatus = "PENDING";

        // 1. Persist lead inquiry to PostgreSQL database
        if (leadInquiryMapper != null) {
            try {
                LeadInquiry lead = LeadInquiry.builder()
                        .inquiryRef(inquiryId)
                        .fullName(request.getFullName())
                        .workEmail(request.getWorkEmail())
                        .companyName(request.getCompanyName())
                        .phoneNumber(request.getPhoneNumber())
                        .employeeCount(request.getEmployeeCount())
                        .interestModule(request.getInterestModule())
                        .inquiryType(request.getInquiryType() != null ? request.getInquiryType() : "DEMO_REQUEST")
                        .preferredDate(request.getPreferredDate())
                        .preferredTimeSlot(request.getPreferredTimeSlot())
                        .message(request.getMessage())
                        .status("NEW")
                        .assignedConsultant(assignedConsultant)
                        .emailStatus(emailStatus)
                        .whatsappStatus(whatsappStatus)
                        .build();

                leadInquiryMapper.insert(lead);
                log.info("Persisted lead inquiry [{}] into database successfully.", inquiryId);
            } catch (Exception e) {
                log.warn("Database persistence for lead inquiry [{}] skipped or failed: {}", inquiryId, e.getMessage());
            }
        }

        // 2. Dispatch Email Confirmation
        if (emailService != null && request.getWorkEmail() != null && !request.getWorkEmail().isBlank()) {
            try {
                boolean emailSent = emailService.sendDemoConfirmation(
                        request.getWorkEmail(),
                        request.getFullName(),
                        request.getCompanyName(),
                        inquiryId,
                        scheduledWindow,
                        assignedConsultant
                );
                emailStatus = emailSent ? "SENT" : "LOGGED";
            } catch (Exception e) {
                log.warn("Email confirmation trigger failed: {}", e.getMessage());
                emailStatus = "FAILED";
            }
        }

        // 3. Dispatch Automated WhatsApp Notifications
        if (whatsAppService != null) {
            // Stream 1: Automated confirmation to End User / Prospect (if phone provided)
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                try {
                    boolean waSent = whatsAppService.sendDemoBookingConfirmation(
                            request.getPhoneNumber(),
                            request.getFullName(),
                            request.getCompanyName(),
                            inquiryId,
                            scheduledWindow,
                            assignedConsultant
                    );
                    whatsappStatus = waSent ? "SENT" : "MOCK_SENT";
                } catch (Exception e) {
                    log.warn("WhatsApp end-user dispatch encountered error: {}", e.getMessage());
                    whatsappStatus = "FAILED";
                }
            } else {
                whatsappStatus = "SKIPPED_NO_PHONE";
            }

            // Stream 2: Automated lead notification to static Priyex Sales Group (+91 9901694683)
            try {
                whatsAppService.sendInternalSalesAlert(
                        null, // defaults to configured sales-alert-phone
                        request.getCompanyName(),
                        request.getFullName(),
                        request.getWorkEmail(),
                        request.getPhoneNumber(),
                        request.getEmployeeCount(),
                        request.getInterestModule(),
                        scheduledWindow,
                        inquiryId
                );
                log.info("Dispatched automated lead alert to Priyex Sales Group for ref [{}]", inquiryId);
            } catch (Exception e) {
                log.warn("Priyex Sales Group alert dispatch encountered error: {}", e.getMessage());
            }
        }

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
